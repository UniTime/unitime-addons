/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.banner.dataexchange;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.unitime.timetable.dataexchange.StudentImport;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideType;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentGroup;

/**
 * @author Tomas Muller
 */
public class BannerStudentImport extends StudentImport {
	
	@Override
	protected Student importStudent(Element element, String externalId, Hashtable<String, Student> students, Session session, Set<Long> updatedStudents,
			Map<String, AcademicArea> abbv2area, Map<String, AcademicClassification> code2clasf, Map<String, PosMajor> code2major, Map<String, PosMinor> code2minor,
			Map<String, StudentGroup> code2group, Map<String, StudentAccomodation> code2accomodation) {
	
		Student student = super.importStudent(element, externalId, students, session, updatedStudents,
				abbv2area, code2clasf, code2major, code2minor, code2group, code2accomodation);
		
		if (updateStudentOverrides(element, session, student))
			updatedStudents.add(student.getUniqueId());
		
		return student;
	}
	
	protected CourseOffering findCourse(Integer crn, Long sessionId) {
    	return (CourseOffering)
    			getHibSession().createQuery(
    					"select distinct co from BannerSection bs, CourseOffering co where " +
    					"bs.session.uniqueId = :sessionId and bs.crn = :crn and co.uniqueId = bs.bannerConfig.bannerCourse.courseOfferingId"
    			).setLong("sessionId", sessionId).setInteger("crn", crn).uniqueResult();
    }
	
	@SuppressWarnings("unchecked")
	protected List<CourseOffering> findCourses(String subject, String courseNbr, Long sessionId) {
		return (List<CourseOffering>)getHibSession().createQuery(
				"from CourseOffering co where " +
				"co.instructionalOffering.session.uniqueId = :sessionId and " +
				"co.subjectArea.subjectAreaAbbreviation = :subject and co.courseNbr like :course")
				.setString("subject", subject).setString("course", courseNbr + "%").setLong("sessionId", sessionId).list();
	}
	
	@SuppressWarnings("unchecked")
	protected List<Class_> findClasses(Integer crn, Long sessionId) {
		return (List<Class_>)
				getHibSession().createQuery(
						"select distinct c from BannerSection bs inner join bs.bannerSectionToClasses as bstc, Class_ c where " +
						"bs.session.uniqueId = :sessionId and bs.crn = :crn and bstc.classId = c.uniqueId"
				).setLong("sessionId", sessionId).setInteger("crn", crn).list();
    }
	
	protected Map<InstructionalOffering, Map<OverrideType, Set<Class_>>> parseOverrides(Element element, Session session) {
		Map<InstructionalOffering, Map<OverrideType, Set<Class_>>> restrictions = new HashMap<InstructionalOffering, Map<OverrideType, Set<Class_>>>();
		for (Iterator<?> j = element.elementIterator("override"); j.hasNext(); ) {
			Element overrideElement = (Element)j.next();

			OverrideType type = null;
			String t = overrideElement.attributeValue("type");
			for (OverrideType ot: OverrideType.values()) {
				if (ot.getReference().equalsIgnoreCase(t)) { type = ot; break; }
			}
			if (type == null) {
				info("Unknown override type " + t);
				continue;
			}
			
			String subject = overrideElement.attributeValue("subject");
			String course = overrideElement.attributeValue("course", overrideElement.attributeValue("courseNbr"));

			Integer crn = null;
			String crnStr = overrideElement.attributeValue("crn");
			if (crnStr != null && !crnStr.isEmpty()) {
				try {
					crn = Integer.valueOf(crnStr);
				} catch (NumberFormatException e) {
					warn("Failed to parse CRN " + crnStr);
				}
			}
				
			if (crn == null) {
				List<CourseOffering> courses = findCourses(subject, course, session.getUniqueId());
				if (course.isEmpty()) {
					error("No course offering found for subject " + subject + ", course number " + course + " and session " + session.getLabel());
				}
				
				// all matching courses
				for (CourseOffering co: courses) {
					Map<OverrideType, Set<Class_>> type2classes = restrictions.get(co.getInstructionalOffering());
					if (type2classes == null) {
						type2classes = new HashMap<OverrideType, Set<Class_>>();
						restrictions.put(co.getInstructionalOffering(), type2classes);
					}
					Set<Class_> classes = type2classes.get(type);
					if (classes == null) {
						classes = new HashSet<Class_>();
						type2classes.put(type, classes);
					}
				}
			} else {
				CourseOffering co = findCourse(crn, session.getUniqueId());
				if (co == null) {
					error("No course offering found for CRN " + crn + " and session " + session.getLabel());
					continue;
				}
				
				Map<OverrideType, Set<Class_>> type2classes = restrictions.get(co.getInstructionalOffering());
				if (type2classes == null) {
					type2classes = new HashMap<OverrideType, Set<Class_>>();
					restrictions.put(co.getInstructionalOffering(), type2classes);
				}
				Set<Class_> classes = type2classes.get(type);
				if (classes == null) {
					classes = new HashSet<Class_>();
					type2classes.put(type, classes);
				}

				boolean foundClass = false;
				for (Class_ c: findClasses(crn, session.getUniqueId())) {
					classes.add(c);
					foundClass = true;
				}
				if (!foundClass) {
					error("No classes found for CRN " + crn + " and session " + session.getLabel());
					continue;
				}
			}
		}
		
		return restrictions;
	}
	
	protected boolean updateStudentOverrides(Element element, Session session, Student student) {
		Map<InstructionalOffering, Map<OverrideType, Set<Class_>>> restrictions = parseOverrides(element, session);
		
		@SuppressWarnings("unchecked")
		List<OverrideReservation> overrides = (List<OverrideReservation>)getHibSession().createQuery(
				"select r from OverrideReservation r inner join r.students s where s.uniqueId = :studentId")
			.setLong("studentId", student.getUniqueId()).list();
		
		boolean changed = false;
		for (Map.Entry<InstructionalOffering, Map<OverrideType, Set<Class_>>> e: restrictions.entrySet()) {
			InstructionalOffering io = e.getKey();
			overrides: for (Map.Entry<OverrideType, Set<Class_>> f: e.getValue().entrySet()) {
				OverrideType type = f.getKey();
				Set<Class_> classes = f.getValue();
				OverrideReservation override = null;
				
				for (Iterator<OverrideReservation> i = overrides.iterator(); i.hasNext(); ) {
					OverrideReservation r = i.next();
					if (r.getOverrideType().equals(type) && r.getInstructionalOffering().equals(io)) {
						int cnt = 0;
						boolean match = true;
						for (Class_ c: classes) {
							if (hasChild(c, classes)) continue;
							if (r.getClasses().contains(c)) {
								cnt ++;
							} else {
								match = false; break;
							}
						}
						if (match && r.getClasses().size() == cnt && r.getConfigurations().isEmpty()) {
							// match found --> no change is needed
							i.remove();
							continue overrides;
						}
						if (r.getStudents().size() == 1) {
							// update an existing override
							override = r;
							i.remove();
							break;
						}
					}
				}
				
				if (override == null) {
					// create a new override reservation
					override = new OverrideReservation();
					override.setOverrideType(type);
					override.setStudents(new HashSet<Student>());
					override.setConfigurations(new HashSet<InstrOfferingConfig>());
					override.setClasses(new HashSet<Class_>());
					override.setInstructionalOffering(io);
					io.addToreservations(override);
					override.addTostudents(student);
				} else {
					override.getConfigurations().clear();
					override.getClasses().clear();
				}
				
				String rx = null;
				for (Class_ c: classes)
					if (!hasChild(c, classes)) {
						override.addToclasses(c);
						rx = (rx == null ? c.getExternalId(io.getControllingCourseOffering()) : rx + ", " + c.getExternalId(io.getControllingCourseOffering()));
					}
				
				info((override.getUniqueId() == null ? "Added " : "Updated ") + type.getReference() + " override for " + io.getCourseName() + (rx == null ? "" : " (" + rx + ")"));

				if (override.getUniqueId() == null)
					override.setUniqueId((Long)getHibSession().save(override));
				else
					getHibSession().update(override);
				changed = true;
			}
		}
		
		for (OverrideReservation override: overrides) {
			info("Removed " + override.getOverrideType().getReference() + " override for " + override.getInstructionalOffering().getCourseName());
			if (override.getStudents().size() > 1) {
				override.getStudents().remove(student);
				getHibSession().update(override);
			} else {
				override.getInstructionalOffering().getReservations().remove(override);
				getHibSession().delete(override);
			}
			changed = true;
		}
		
		return changed;
	}

	protected boolean isParentOf(Class_ c1, Class_ c2) {
		if (c2 == null) return false;
		if (c1.equals(c2.getParentClass())) return true;
		return isParentOf(c1, c2.getParentClass());
	}
	
	protected boolean hasChild(Class_ c1, Collection<Class_> classes) {
		for (Class_ c2: classes) {
			if (isParentOf(c1, c2)) return true;
		}
		return false;
	}
}
