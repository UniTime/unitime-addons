/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.dom4j.Element;
import org.unitime.banner.model.BannerSection;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.BaseImport;
import org.unitime.timetable.dataexchange.StudentEnrollmentImport.Pair;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.test.UpdateExamConflicts;

/**
 * 
 * @author says
 *
 */
public class BannerStudentEnrollmentImport extends BaseImport {
	public BannerStudentEnrollmentImport() {
		super();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		boolean trimLeadingZerosFromExternalId = "true".equals(ApplicationProperties.getProperty("tmtbl.data.exchange.trim.externalId","false"));

        if (!rootElement.getName().equalsIgnoreCase("bannerStudentEnrollments"))
        	throw new Exception("Given XML file is not a Student Enrollments load file.");

        Session session = null;
        
        Set<Long> updatedStudents = new HashSet<Long>(); 
        
		try {
	        String campus = rootElement.attributeValue("campus");
	        String year   = rootElement.attributeValue("year");
	        String term   = rootElement.attributeValue("term");
	        String created = rootElement.attributeValue("created");
			
	        beginTransaction();

	        session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        
	        if(session == null)
	           	throw new Exception("No session found for the given campus, year, and term.");
	        
	    	HashMap<Integer, List<Class_>> crn2classes = new HashMap<Integer, List<Class_>>();
	    	HashMap<Integer, CourseOffering> crn2course = new HashMap<Integer, CourseOffering>();
			HashMap<Long, CourseOffering> courses = new HashMap<Long, CourseOffering>();
			for (Iterator<?> it = CourseOffering.findAll(session.getUniqueId()).iterator(); it.hasNext();){
				CourseOffering co = (CourseOffering) it.next();
				courses.put(co.getUniqueId(), co);
			}
	 		for (Iterator<?> it = BannerSection.findAll(session.getUniqueId()).iterator(); it.hasNext();) {
				BannerSection bs = (BannerSection) it.next();
				if (bs.getCrn() == null) continue;
				Set<Class_> bsClasses = bs.getClasses(getHibSession());
				if (bsClasses != null && !bsClasses.isEmpty()) {
					crn2classes.put(bs.getCrn(), new ArrayList<Class_>(bsClasses));
					crn2course.put(bs.getCrn(), courses.get(bs.getBannerConfig().getBannerCourse().getCourseOfferingId()));
				}
			}
	        info("classes loaded");

	        TimetableManager manger = getManager();
	        if (manger == null)
	        	manger = findDefaultManager();

	        if (created != null)
				ChangeLog.addChange(getHibSession(), manger, session, session, created, ChangeLog.Source.DATA_IMPORT_STUDENT_ENROLLMENTS, ChangeLog.Operation.UPDATE, null, null);
         
	        Hashtable<String, Student> students = new Hashtable<String, Student>();
	        for (Student student: (List<Student>)getHibSession().createQuery(
                    "select distinct s from Student s " +
                    "left join fetch s.courseDemands as cd " +
                    "left join fetch cd.courseRequests as cr " +
                    "left join fetch s.classEnrollments as e " +
                    "left join fetch cr.classEnrollments as cre "+
                    "where s.session.uniqueId=:sessionId and s.externalUniqueId is not null").
                    setLong("sessionId",session.getUniqueId()).list()) { 
                        students.put(student.getExternalUniqueId(), student);
                }

 	        for (Iterator i = rootElement.elementIterator("student"); i.hasNext(); ) {
	            Element studentElement = (Element) i.next();
	            
	            String externalId = studentElement.attributeValue("externalId");
	            if (externalId == null) continue;
	            while (trimLeadingZerosFromExternalId && externalId.startsWith("0")) externalId = externalId.substring(1);

            	Student student = students.remove(externalId);
            	if (student == null) {
            		student = new Student();
	                student.setSession(session);
		            student.setFirstName(studentElement.attributeValue("firstName", "Name"));
		            student.setMiddleName(studentElement.attributeValue("middleName"));
		            student.setLastName(studentElement.attributeValue("lastName", "Unknown"));
		            student.setEmail(studentElement.attributeValue("email"));
		            student.setExternalUniqueId(externalId);
		            student.setFreeTimeCategory(0);
		            student.setSchedulePreference(0);
		            student.setClassEnrollments(new HashSet<StudentClassEnrollment>());
		            student.setCourseDemands(new HashSet<CourseDemand>());
            	} else {
		            student.setFirstName(studentElement.attributeValue("firstName", student.getFirstName()));
		            student.setMiddleName(studentElement.attributeValue("middleName", student.getMiddleName()));
		            student.setLastName(studentElement.attributeValue("lastName", student.getLastName()));
            	}
            	
            	Hashtable<Pair, StudentClassEnrollment> enrollments = new Hashtable<Pair, StudentClassEnrollment>();
            	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
            		enrollments.put(new Pair(enrollment.getCourseOffering().getUniqueId(), enrollment.getClazz().getUniqueId()), enrollment);
            	}

            	for (Iterator j = studentElement.elementIterator("section"); j.hasNext(); ) {
            		Element classElement = (Element) j.next();
            		
            		Integer crn = null;
            		try {
            			crn = Integer.valueOf(classElement.attributeValue("crn"));
            		} catch (NullPointerException e) {
            		} catch (NumberFormatException e) {
            		}
            		
            		List<Class_> classes = (crn == null ? null : crn2classes.get(crn));
            		if (classes == null) {
            			warn("No classes for CRN " + crn + " found.");
            			continue;
            		}
            		
            		CourseOffering course = crn2course.get(crn);
            		if (course == null) {
            			warn("No course for CRN " + crn + " found.");
            			continue;
            		}
            		
            		for (Class_ clazz: classes) {
                		StudentClassEnrollment enrollment = enrollments.remove(new Pair(course.getUniqueId(), clazz.getUniqueId()));
                		if (enrollment != null) continue; // enrollment already exists
                		
                		enrollment = new StudentClassEnrollment();
                		enrollment.setStudent(student);
                		enrollment.setClazz(clazz);
                		enrollment.setCourseOffering(course);
                		enrollment.setTimestamp(new java.util.Date());
                		student.getClassEnrollments().add(enrollment);

                		demands: for (CourseDemand d: student.getCourseDemands()) {
                			for (CourseRequest r: d.getCourseRequests()) {
                				if (r.getCourseOffering().equals(course)) {
                					r.getClassEnrollments().add(enrollment);
                					enrollment.setCourseRequest(r);
                					break demands;
                				}
                			}
                		}
                		
                		if (student.getUniqueId() != null) updatedStudents.add(student.getUniqueId());
            		}
            	}
            	
            	if (!enrollments.isEmpty()) {
            		for (StudentClassEnrollment enrollment: enrollments.values()) {
            			student.getClassEnrollments().remove(enrollment);
            			if (enrollment.getCourseRequest() != null)
            				enrollment.getCourseRequest().getClassEnrollments().remove(enrollment);
            			getHibSession().delete(enrollment);
                		updatedStudents.add(student.getUniqueId());
            		}
            	}
            	
            	if (student.getUniqueId() == null) {
            		updatedStudents.add((Long)getHibSession().save(student));
            	} else {
            		getHibSession().update(student);
            	}
	            flushIfNeededDoNotClearSession(true);
	        }
            
 	        for (Student student: students.values()) {
        		for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
        			StudentClassEnrollment enrollment = i.next();
        			if (enrollment.getCourseRequest() != null)
        				enrollment.getCourseRequest().getClassEnrollments().remove(enrollment);
        			getHibSession().delete(enrollment);
        			i.remove();
     	        	updatedStudents.add(student.getUniqueId());
        		}
        		getHibSession().update(student);
 	        }
 	        
 	        if (!updatedStudents.isEmpty())
 	 	        StudentSectioningQueue.studentChanged(getHibSession(), session.getUniqueId(), updatedStudents);
            
            commitTransaction();
            
            debug(updatedStudents.size() + " students changed");
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}
		
        if (session!=null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.finalExam.updateConflicts","false"))) {
            try {
                beginTransaction();
                new UpdateExamConflicts(this).update(session.getUniqueId(), Exam.sExamTypeFinal, getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }
        
        if (session!=null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.midtermExam.updateConflicts","false"))) {
            try {
                beginTransaction();
                new UpdateExamConflicts(this).update(session.getUniqueId(), Exam.sExamTypeMidterm, getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }

        if (session != null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.class.updateEnrollments","false"))){
        	org.hibernate.Session hibSession = new _RootDAO().createNewSession();
            try {
                info("  Updating class enrollments...");
                Class_.updateClassEnrollmentForSession(session, hibSession);
                info("  Updating course offering enrollments...");
                CourseOffering.updateCourseOfferingEnrollmentForSession(session, hibSession);
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
            } finally {
            	hibSession.close();
            }      	
        }
        
        info("  Banner Student Enrollment Load Complete");
	}

}
