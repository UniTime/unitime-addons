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
package org.unitime.banner.onlinesectioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.banner.model.BannerSection;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideType;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XIndividualReservation;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XReservationType;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.server.CheckMaster;
import org.unitime.timetable.onlinesectioning.server.CheckMaster.Master;
import org.unitime.timetable.onlinesectioning.updates.NotifyStudentAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllData;

/**
 * @author Tomas Muller
 */
@CheckMaster(Master.REQUIRED)
public class BannerUpdateStudentAction implements OnlineSectioningAction<BannerUpdateStudentAction.UpdateResult> {
	private static final long serialVersionUID = 1L;
	private String iTermCode, iExternalId, iFName, iMName, iLName, iEmail;
	private List<String[]> iGroups = new ArrayList<String[]>();
	private List<String[]> iAcadAreaClasf = new ArrayList<String[]>();
	private boolean iUpdateAcadAreaClasf = false;
	private List<String[]> iAcadAreaMajor = new ArrayList<String[]>();
	private boolean iUpdateAcadeAreaMajor = false;
	private List<String[]> iOverrides = new ArrayList<String[]>();
	private Set<Integer> iCRNs = new TreeSet<Integer>();
	private Long iStudentId;
	private Session iSession;
	private UpdateResult iResult = UpdateResult.OK;
	
	public BannerUpdateStudentAction forStudent(String externalId, String termCode) {
		iExternalId = externalId;
		iTermCode = termCode;
		return this;
	}
	
	public BannerUpdateStudentAction withName(String firstName, String middleName, String lastName) {
		iFName = firstName;
		iMName = middleName;
		iLName = lastName;
		return this;
	}
	
	public BannerUpdateStudentAction withEmail(String email) {
		iEmail = email;
		return this;
	}
	
	public BannerUpdateStudentAction withAcadAreaClassification(String academicArea, String classification) {
		if (academicArea == null || academicArea.isEmpty() || classification == null || classification.isEmpty()) return this;
		iUpdateAcadAreaClasf = true;
		iAcadAreaClasf.add(new String[] {academicArea, classification});
		return this;
	}
	
	public BannerUpdateStudentAction updateAcadAreaClassifications(boolean update) {
		iUpdateAcadAreaClasf = update;
		return this;
	}
	
	public BannerUpdateStudentAction withAcadAreaMajor(String academicArea, String major) {
		if (academicArea == null || academicArea.isEmpty() || major == null || major.isEmpty()) return this;
		iUpdateAcadeAreaMajor = true;
		iAcadAreaMajor.add(new String[] {academicArea, major});
		return this;
	}
	
	public BannerUpdateStudentAction updateAcadAreaMajors(boolean update) {
		iUpdateAcadeAreaMajor = update;
		return this;
	}


	public BannerUpdateStudentAction withGroup(String externalId, String campus, String abbreviation, String name) {
		iGroups.add(new String[] {externalId, campus, abbreviation, name});
		return this;
	}
	
	public BannerUpdateStudentAction withCRN(Integer crn) {
		iCRNs.add(crn);
		return this;
	}
	
	public BannerUpdateStudentAction withOverride(String type, String subject, String course, String crn) {
		iOverrides.add(new String[] {type, subject, course, crn});
		return this;
	}

	@Override
	public UpdateResult execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		boolean changed = false;
		try {
			iSession = SessionDAO.getInstance().get(server.getAcademicSession().getUniqueId(), helper.getHibSession());
			
			Student student = getStudent(helper);
			iStudentId = student.getUniqueId();
			if (iStudentId == null) changed = true;
			
			OnlineSectioningLog.Action.Builder action = helper.getAction();
			
			action.setStudent(
					OnlineSectioningLog.Entity.newBuilder()
					.setExternalId(iExternalId)
					.setName(iLName + ", " + iFName + (iMName == null ? "" : " " + iMName))
					.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
			
			helper.getAction().addOptionBuilder().setKey("CRNs").setValue(iCRNs.toString());
			if (iEmail != null)
				helper.getAction().addOptionBuilder().setKey("email").setValue(iEmail);
			if (!iAcadAreaClasf.isEmpty()) {
				if (iAcadAreaClasf.isEmpty()) return null;
				String[] areaClasf = iAcadAreaClasf.get(0);
				String major = null;
				for (String[] areaMajor: iAcadAreaMajor)
					if (areaMajor[0].equals(areaClasf[0])) {
						major = areaMajor[1]; break;
					}
				helper.getAction().addOptionBuilder().setKey("curriculum").setValue(areaClasf[0] + (major == null ? "" : "/" + major) + " " + areaClasf[1]);
			}
				
			
			if (iStudentId != null)
				action.getStudentBuilder().setUniqueId(iStudentId);
			
			Lock lock = (iStudentId == null ? null : server.lockStudent(iStudentId, null, true));
			try {
				helper.beginTransaction();
				if (updateStudentDemographics(student, helper))
					changed = true;
				
				if (updateStudentGroups(student, helper))
					changed = true;
				
				Map<CourseOffering, List<Class_>> enrollments = getEnrollments(helper);
				
				OnlineSectioningLog.Enrollment.Builder external = OnlineSectioningLog.Enrollment.newBuilder();
				external.setType(OnlineSectioningLog.Enrollment.EnrollmentType.EXTERNAL);
				for (Map.Entry<CourseOffering, List<Class_>> e: enrollments.entrySet()) {
					CourseOffering course = e.getKey();
					for (Class_ clazz: e.getValue()) {
						external.addSectionBuilder()
								.setClazz(OnlineSectioningLog.Entity.newBuilder()
										.setUniqueId(clazz.getUniqueId())
										.setExternalId(clazz.getExternalId(course))
										.setName(clazz.getClassSuffix(course))
										)
								.setCourse(OnlineSectioningLog.Entity.newBuilder()
										.setUniqueId(course.getUniqueId())
										.setName(course.getCourseName())
										)
								.setSubpart(OnlineSectioningLog.Entity.newBuilder()
										.setUniqueId(clazz.getSchedulingSubpart().getUniqueId())
										.setName(clazz.getSchedulingSubpart().getItypeDesc())
										)
								;
					}
				}
				helper.getAction().addEnrollment(external);
				
				if (updateClassEnrollments(student, enrollments, helper))
					changed = true;
				
				if (iStudentId == null) {
					iStudentId = (Long)helper.getHibSession().save(student);
					action.getStudentBuilder().setUniqueId(iStudentId);
				} else if (changed)
					helper.getHibSession().update(student);
				
				if (updateStudentOverrides(student, server, helper))
					changed = true;
				
				if (changed) {
					// Unload student
					XStudent oldStudent = server.getStudent(iStudentId);
					if (oldStudent != null) {
						OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
						enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
						for (XRequest oldRequest: oldStudent.getRequests()) {
							if (oldRequest instanceof XCourseRequest && ((XCourseRequest)oldRequest).getEnrollment() != null) {
								XEnrollment enrl = ((XCourseRequest)oldRequest).getEnrollment();
								XOffering offering = server.getOffering(enrl.getOfferingId());
								for (XSection section: offering.getSections(enrl))
									enrollment.addSection(OnlineSectioningHelper.toProto(section, enrl));
							}
						}
						action.addEnrollment(enrollment);
					}
					
					// Load student
					XStudent newStudent = ReloadAllData.loadStudent(student, null, server, helper);
					if (newStudent != null) {
						server.update(newStudent, true);
						OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
						enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
						for (XRequest newRequest: newStudent.getRequests()) {
							action.addRequest(OnlineSectioningHelper.toProto(newRequest));
							if (newRequest instanceof XCourseRequest && ((XCourseRequest)newRequest).getEnrollment() != null) {
								XEnrollment enrl = ((XCourseRequest)newRequest).getEnrollment();
								XOffering offering = server.getOffering(enrl.getOfferingId());
								for (XSection section: offering.getSections(enrl))
									enrollment.addSection(OnlineSectioningHelper.toProto(section, enrl));
							}
						}
						action.addEnrollment(enrollment);
					}

					server.execute(server.createAction(NotifyStudentAction.class).forStudent(iStudentId).oldStudent(oldStudent), helper.getUser());
				}
			
				helper.commitTransaction();
			} catch (Exception e) {
				helper.rollbackTransaction();
				throw e;
			} finally {
				if (lock != null) lock.release();
			}
			
		} catch (Exception e) {
			helper.error("Student update failed: " + e.getMessage(), e);
			helper.getAction().setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			return UpdateResult.FAILURE;
		} finally {
			for (OnlineSectioningLog.Message m: helper.getLog().getMessageList())
				helper.getAction().addMessage(m);
		}
		helper.getAction().setResult(changed ? OnlineSectioningLog.Action.ResultType.TRUE : OnlineSectioningLog.Action.ResultType.FALSE);
		
		if (iResult == UpdateResult.OK && !changed)
			return UpdateResult.NO_CHANGE;
		
		return iResult;
	}
	
	public UpdateResult execute(Long sessionId, OnlineSectioningHelper helper) {
		helper.beginTransaction();
		boolean changed = false;
		try {
			iSession = SessionDAO.getInstance().get(sessionId, helper.getHibSession());
			
			Student student = getStudent(helper);
			iStudentId = student.getUniqueId();
			if (iStudentId == null) changed = true;
			
			if (updateStudentDemographics(student, helper))
				changed = true;
			
			if (updateStudentGroups(student, helper))
				changed = true;
			
			if (iStudentId == null)
				iStudentId = (Long)helper.getHibSession().save(student);
			else if (changed)
				helper.getHibSession().update(student);

			if (updateStudentOverrides(student, null, helper))
				changed = true;

			if (updateClassEnrollments(student, getEnrollments(helper), helper))
				changed = true;
			
			helper.commitTransaction();
		} catch (Exception e) {
			helper.rollbackTransaction();
			helper.error("Student update failed: " + e.getMessage(), e);
			return UpdateResult.FAILURE;
		}
			
		if (iResult == UpdateResult.OK && !changed)
			return UpdateResult.NO_CHANGE;
		return iResult;
	}
	
	public Long getStudentId() { return iStudentId; }
	
	public Student getStudent(OnlineSectioningHelper helper) {
		Student student = Student.findByExternalIdBringBackEnrollments(helper.getHibSession(), iSession.getUniqueId(), iExternalId);
		if (student == null) {
			student = new Student();
			student.setExternalUniqueId(iExternalId);
			student.setSession(SessionDAO.getInstance().get(iSession.getUniqueId(), helper.getHibSession()));
			student.setFreeTimeCategory(0);
			student.setSchedulePreference(0);
			student.setClassEnrollments(new HashSet<StudentClassEnrollment>());
			student.setAcademicAreaClassifications(new HashSet<AcademicAreaClassification>());
			student.setPosMajors(new HashSet<PosMajor>());
			student.setCourseDemands(new HashSet<CourseDemand>());
			student.setGroups(new HashSet<StudentGroup>());
			student.setAccomodations(new HashSet<StudentAccomodation>());
		}
		return student;
	}
	
	protected boolean updateStudentDemographics(Student student, OnlineSectioningHelper helper) {
		boolean changed = false;
		if (!eq(iFName, student.getFirstName())) {
			student.setFirstName(iFName);
			changed = true;
		}
		if (!eq(iMName, student.getMiddleName())) {
			student.setMiddleName(iMName);
			changed = true;
		}
		if (!eq(iLName, student.getLastName())) {
			student.setLastName(iLName);
			changed = true;
		}
		if (!eq(iEmail, student.getEmail())) {
			student.setEmail(iEmail);
			changed = true;
		}
		
		if (iUpdateAcadAreaClasf) {
			List<AcademicAreaClassification> remaining = new ArrayList<AcademicAreaClassification>(student.getAcademicAreaClassifications());
			aac: for (String[] areaClasf: iAcadAreaClasf) {
				String area = areaClasf[0], clasf = areaClasf[1];
				for (Iterator<AcademicAreaClassification> i = remaining.iterator(); i.hasNext(); ) {
					AcademicAreaClassification aac = i.next();
					if ((area.equalsIgnoreCase(aac.getAcademicArea().getExternalUniqueId()) || area.equalsIgnoreCase(aac.getAcademicArea().getAcademicAreaAbbreviation())) &&
						(clasf.equalsIgnoreCase(aac.getAcademicClassification().getExternalUniqueId()) || clasf.equalsIgnoreCase(aac.getAcademicClassification().getCode()))) {
						i.remove(); continue aac;
					}
				}
					
				AcademicArea aa = AcademicArea.findByExternalId(helper.getHibSession(), student.getSession().getUniqueId(), area);						
				if (aa == null)
					aa = AcademicArea.findByAbbv(helper.getHibSession(), student.getSession().getUniqueId(), area);
				if (aa == null){
					aa = new AcademicArea();
					aa.setPosMajors(new HashSet<PosMajor>());
					aa.setAcademicAreaAbbreviation(area);
					aa.setSession(student.getSession());
					aa.setExternalUniqueId(area);
					aa.setTitle(area);
					aa.setUniqueId((Long)helper.getHibSession().save(aa));
					helper.info("Added Academic Area:  " + area);
				}
				
				AcademicClassification ac = AcademicClassification.findByExternalId(helper.getHibSession(), student.getSession().getUniqueId(), clasf);
				if (ac == null)
					ac = AcademicClassification.findByCode(helper.getHibSession(), student.getSession().getUniqueId(), clasf);
				if (ac == null){
					ac = new AcademicClassification();
					ac.setCode(clasf);
					ac.setExternalUniqueId(clasf);
					ac.setName(clasf);
					ac.setSession(student.getSession());
					ac.setUniqueId((Long) helper.getHibSession().save(ac));
					helper.info("Added Academic Classification:  " + clasf);
				}

				AcademicAreaClassification aac = new AcademicAreaClassification();
				aac.setAcademicArea(aa);
				aac.setAcademicClassification(ac);
				aac.setStudent(student);
				student.addToacademicAreaClassifications(aac);
				changed = true;
			}
			
			for (AcademicAreaClassification aac: remaining) {
				student.getAcademicAreaClassifications().remove(aac);
				helper.getHibSession().delete(aac);
				changed = true;
			}
		}
		
		if (iUpdateAcadeAreaMajor) {
			List<PosMajor> remaining = new ArrayList<PosMajor>(student.getPosMajors());
			mj: for (String[] areaMajor: iAcadAreaMajor){
				String area = areaMajor[0], major = areaMajor[1];
				for (Iterator<PosMajor> i = remaining.iterator(); i.hasNext(); ) {
					PosMajor m = i.next();
					if (!major.equalsIgnoreCase(m.getExternalUniqueId()) && !major.equalsIgnoreCase(m.getCode())) continue;
					for (AcademicArea a: m.getAcademicAreas()) {
						if (area.equalsIgnoreCase(a.getExternalUniqueId()) || area.equalsIgnoreCase(a.getAcademicAreaAbbreviation())) {
							i.remove(); continue mj;
						}
					}
				}
				
				AcademicArea aa = AcademicArea.findByExternalId(helper.getHibSession(), student.getSession().getUniqueId(), area);						
				if (aa == null)
					aa = AcademicArea.findByAbbv(helper.getHibSession(), student.getSession().getUniqueId(), area);
				if (aa == null){
					aa = new AcademicArea();
					aa.setPosMajors(new HashSet<PosMajor>());
					aa.setAcademicAreaAbbreviation(area);
					aa.setSession(student.getSession());
					aa.setExternalUniqueId(area);
					aa.setTitle(area);
					aa.setUniqueId((Long)helper.getHibSession().save(aa));
					helper.info("Added Academic Area:  " + area);
				}
				
				PosMajor posMajor = PosMajor.findByExternalIdAcadAreaExternalId(helper.getHibSession(), student.getSession().getUniqueId(), major, area);
				if (posMajor == null)
					posMajor = PosMajor.findByCodeAcadAreaAbbv(helper.getHibSession(), student.getSession().getUniqueId(), major, area);
				if (posMajor == null) {
					posMajor = new PosMajor();
					posMajor.setCode(major);
					posMajor.setExternalUniqueId(major);
					posMajor.setName(major);
					posMajor.setSession(student.getSession());
					posMajor.setUniqueId((Long)helper.getHibSession().save(posMajor));
					posMajor.addToacademicAreas(aa);
					aa.addToposMajors(posMajor);
					helper.info("Added Major:  " + major + " to Academic Area:  " + area);
				}
				
				student.addToposMajors(posMajor);
				changed = true;
			}
			
			for (PosMajor m: remaining) {
				student.getPosMajors().remove(m);
				changed = true;
			}
		}
		
		return changed;
	}
	
	protected boolean updateStudentGroups(Student student, OnlineSectioningHelper helper) {
		Set<StudentGroup> groups = new HashSet<StudentGroup>();
		for (String[] g: iGroups) {
			if (!iSession.getAcademicInitiative().equals(g[1])) continue;
			StudentGroup sg = StudentGroup.findByExternalId(helper.getHibSession(), g[0], iSession.getUniqueId());
			if (sg == null) {
				sg = new StudentGroup();
				sg.setExternalUniqueId(g[0]);
				sg.setSession(iSession);
				sg.setGroupAbbreviation(g[2] == null ? g[0] : g[2]);
				sg.setGroupName(g[3] == null ? g[0] : g[3]);
				sg.setUniqueId((Long)helper.getHibSession().save(sg));
				helper.info("Added Student Group:  " + sg.getExternalUniqueId() + " -  " + sg.getGroupAbbreviation() + " - " + sg.getGroupName() + " to session " + sg.getSession().academicInitiativeDisplayString());
			} else {
				boolean changed = false;
				if (g[2] != null &&  !g[2].equals(sg.getGroupAbbreviation())){
					helper.info("Changed Student Group:  " + sg.getExternalUniqueId() + " - old abbreviation:  " + sg.getGroupAbbreviation() + ", new abbreviation:  " + g[2] + " in session " + sg.getSession().academicInitiativeDisplayString());
					sg.setGroupAbbreviation(g[2]);
					changed = true;
				} 
				if (g[3] != null && !g[3].equals(sg.getGroupName())){
					helper.info("Changed Student Group:  " + sg.getExternalUniqueId() + " - old name:  " + sg.getGroupName() + ", new name:  " + g[3] + " in session " + sg.getSession().academicInitiativeDisplayString());
					sg.setGroupName(g[3]);
					changed = true;
				}
				if (changed) {
					helper.getHibSession().update(sg);
				}
			}
			groups.add(sg);
		}
		boolean changed = false;
		for (StudentGroup g: student.getGroups()) {
			if (groups.remove(g)) continue;
			if (g.getExternalUniqueId() != null) {
				g.getStudents().remove(student);
				student.getGroups().remove(g);
				changed = true;
			}
		}
		for (StudentGroup g: groups) {
			g.addTostudents(student);
			student.addTogroups(g);
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
	
	protected Map<InstructionalOffering, Map<OverrideType, Set<Class_>>> getOverrides(OnlineSectioningHelper helper) {
		Map<InstructionalOffering, Map<OverrideType, Set<Class_>>> restrictions = new HashMap<InstructionalOffering, Map<OverrideType, Set<Class_>>>();
		for (String[] override: iOverrides) {
			OverrideType type = null;
			for (OverrideType t: OverrideType.values()) {
				if (t.getReference().equalsIgnoreCase(override[0])) { type = t; break; }
			}
			if (type == null) {
				helper.info("Unknown override type " + override[0]);
				continue;
			}
			
			String subject = override[1], course = override[2];

			Integer crn = null;
			if (override[3] != null && !override[3].isEmpty()) {
				try {
					crn = Integer.valueOf(override[3]);
				} catch (NumberFormatException e) {
					helper.warn("Failed to parse CRN " + override[3]);
				}
			}
			
			if (crn == null) {
				@SuppressWarnings("unchecked")
				List<CourseOffering> courses = (List<CourseOffering>)helper.getHibSession().createQuery(
						"from CourseOffering co where " +
						"co.instructionalOffering.session.uniqueId = :sessionId and " +
						"co.subjectArea.subjectAreaAbbreviation = :subject and co.courseNbr like :course")
						.setString("subject", subject).setString("course", course + "%").setLong("sessionId", iSession.getUniqueId()).list();
				if (course.isEmpty()) {
					helper.error("No course offering found for subject " + subject + ", course number " + course + " and banner session " + iTermCode);
					iResult = UpdateResult.PROBLEM;
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
				CourseOffering co = BannerSection.findCourseOfferingForCrnAndTermCode(helper.getHibSession(), crn, iTermCode);
				if (co == null) {
					helper.error("No course offering found for CRN " + crn + " and banner session " + iTermCode);
					iResult = UpdateResult.PROBLEM;
					continue;
				}
				if (!iSession.equals(co.getInstructionalOffering().getSession())) continue;
				
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
				for(Iterator<?> it = BannerSection.findAllClassesForCrnAndTermCode(helper.getHibSession(), crn, iTermCode).iterator(); it.hasNext();) {
					Class_ c = (Class_) it.next();
					if (!iSession.equals(c.getSession())) continue;
					while (c!=null) {
						classes.add(c);
						c = c.getParentClass();
					}
					foundClass = true;
				}
				if (!foundClass) {
					helper.error("No classes found for CRN " + crn + " and banner session " + iTermCode);
					iResult = UpdateResult.PROBLEM;
					continue;
				}
			}
		}
		return restrictions;
	}
	
	protected void mergeTimeAndLimitOverrides(Map<InstructionalOffering, Map<OverrideType, Set<Class_>>> restrictions) {
		for (Map<OverrideType, Set<Class_>> overrides: restrictions.values()) {
			Set<Class_> times = overrides.get(OverrideType.AllowTimeConflict);
			Set<Class_> limits = overrides.get(OverrideType.AllowOverLimit);
			if (times == null || limits == null) continue;
			if (times.isEmpty() || times.containsAll(limits)) {
				if (limits.isEmpty() || limits.containsAll(times)) {
					overrides.put(OverrideType.AllowOverLimitTimeConflict, times);
					overrides.remove(OverrideType.AllowTimeConflict);
					overrides.remove(OverrideType.AllowOverLimit);
				} else {
					overrides.put(OverrideType.AllowOverLimitTimeConflict, times);
					overrides.remove(OverrideType.AllowTimeConflict);
				}
			} else if (limits.isEmpty() || limits.containsAll(times)) {
				overrides.put(OverrideType.AllowOverLimitTimeConflict, limits);
				overrides.remove(OverrideType.AllowOverLimit);
			}
		}
	}
	
	protected boolean updateStudentOverrides(Student student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		boolean changed = false;

		Map<InstructionalOffering, Map<OverrideType, Set<Class_>>> restrictions = getOverrides(helper);

		mergeTimeAndLimitOverrides(restrictions);
		
		@SuppressWarnings("unchecked")
		List<OverrideReservation> overrides = (List<OverrideReservation>)helper.getHibSession().createQuery(
				"select r from OverrideReservation r inner join r.students s where s.uniqueId = :studentId")
			.setLong("studentId", student.getUniqueId()).list();
		
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
				
				helper.info((override.getUniqueId() == null ? "Added " : "Updated ") + type.getReference() + " override for " + io.getCourseName() + (rx == null ? "" : " (" + rx + ")"));

				if (override.getUniqueId() == null)
					override.setUniqueId((Long)helper.getHibSession().save(override));
				else
					helper.getHibSession().update(override);
				
				if (server != null) {
					Lock w = server.writeLock();
					try {
						XOffering offering = server.getOffering(io.getUniqueId());
						if (offering != null) {
							// remove the previous one
							for (Iterator<XReservation> i = offering.getReservations().iterator(); i.hasNext(); ) {
								XReservation r = i.next();
								if (r.getType() == XReservationType.Override && r.getReservationId().equals(override.getUniqueId())) {
									i.remove(); break;
								}
							}
							// load the new one
							offering.getReservations().add(new XIndividualReservation(offering, override));
							server.update(offering);
						}
					} finally {
						w.release();
					}
				}
				
				changed = true;
			}
		}
		
		for (OverrideReservation override: overrides) {
			helper.info("Removed " + override.getOverrideType().getReference() + " override for " + override.getInstructionalOffering().getCourseName());
			if (override.getStudents().size() > 1) {
				override.getStudents().remove(student);
				helper.getHibSession().update(override);
			} else {
				override.getInstructionalOffering().getReservations().remove(override);
				helper.getHibSession().delete(override);
			}
			if (server != null) {
				Lock w = server.writeLock();
				try {
					XOffering offering = server.getOffering(override.getInstructionalOffering().getUniqueId());
					if (offering != null) {
						for (Iterator<XReservation> i = offering.getReservations().iterator(); i.hasNext(); ) {
							XReservation r = i.next();
							if (r.getType() == XReservationType.Override && r.getReservationId().equals(override.getUniqueId())) {
								XIndividualReservation ir = (XIndividualReservation)r;
								if (ir.getStudentIds().size() > 1) {
									ir.getStudentIds().remove(iStudentId);
								} else {
									i.remove();
								}
								break;
							}
						}
						server.update(offering);
					}
				} finally {
					w.release();
				}
			}
			changed = true;
		}
		
		return changed;
	}
	
	protected boolean updateClassEnrollments(Student student, Map<CourseOffering, List<Class_>> courseToClassEnrollments, OnlineSectioningHelper helper) {
		boolean changed = false;

		Hashtable<Pair, StudentClassEnrollment> enrollments = new Hashtable<Pair, StudentClassEnrollment>();
		if (student.getClassEnrollments() != null){
        	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
        		enrollments.put(new Pair(enrollment.getCourseOffering().getUniqueId(), enrollment.getClazz().getUniqueId()), enrollment);
        	}
    	}
    	int nextPriority = 0;
    	for (CourseDemand cd: student.getCourseDemands())
    		if (!cd.isAlternative() && cd.getPriority() >= nextPriority)
    			nextPriority = cd.getPriority() + 1;
    	Set<CourseDemand> remaining = new HashSet<CourseDemand>(student.getCourseDemands());
    	boolean fixCourseDemands = false;

    	for (Map.Entry<CourseOffering, List<Class_>> entry: courseToClassEnrollments.entrySet()) {
    		CourseOffering co = entry.getKey();
    		for (Class_ clazz: entry.getValue()) {
    			StudentClassEnrollment enrollment = enrollments.remove(new Pair(co.getUniqueId(), clazz.getUniqueId()));
        		if (enrollment == null) {
            		enrollment = new StudentClassEnrollment();
            		enrollment.setStudent(student);
            		enrollment.setClazz(clazz);
            		enrollment.setCourseOffering(co);
            		enrollment.setTimestamp(new java.util.Date());
            		student.getClassEnrollments().add(enrollment);    
            		changed = true;
        		}
        		
        		if (enrollment.getCourseRequest() == null) {
            		demands: for (CourseDemand d: student.getCourseDemands()) {
            			for (CourseRequest r: d.getCourseRequests()) {
            				if (r.getCourseOffering().equals(co)) {
            					enrollment.setCourseRequest(r);
            					break demands;
            				}
            			}
            		}
            		changed = true;
        		}
        		
        		if (enrollment.getCourseRequest() != null) {
        			remaining.remove(enrollment.getCourseRequest().getCourseDemand());
        		} else {
        			CourseDemand cd = new CourseDemand();
        			cd.setTimestamp(enrollment.getTimestamp());
        			cd.setCourseRequests(new HashSet<CourseRequest>());
        			cd.setStudent(student);
        			student.getCourseDemands().add(cd);
        			cd.setAlternative(false);
        			cd.setPriority(nextPriority++);
        			cd.setWaitlist(false);
        			CourseRequest cr = new CourseRequest();
        			cd.getCourseRequests().add(cr);
        			cr.setCourseDemand(cd);
        			cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
        			cr.setAllowOverlap(false);
        			cr.setCredit(0);
        			cr.setOrder(0);
        			cr.setCourseOffering(enrollment.getCourseOffering());
        			enrollment.setCourseRequest(cr);
        			fixCourseDemands = true;
            		
            		changed = true;
        		}            		
    		}
    	}
    	
    	if (!enrollments.isEmpty()) {
    		for (StudentClassEnrollment enrollment: enrollments.values()) {
    			student.getClassEnrollments().remove(enrollment);
    			helper.getHibSession().delete(enrollment);
    		}
    		changed = true;
    	}

    	if (fixCourseDemands && student.getUniqueId() != null) {
    		// removed unused course demands
    		for (CourseDemand cd: remaining) {
    			if (cd.getFreeTime() != null)
    				helper.getHibSession().delete(cd.getFreeTime());
    			for (CourseRequest cr: cd.getCourseRequests())
    				helper.getHibSession().delete(cr);
    			student.getCourseDemands().remove(cd);
    			helper.getHibSession().delete(cd);
    		}
    		int priority = 0;
    		for (CourseDemand cd: new TreeSet<CourseDemand>(student.getCourseDemands())) {
    			cd.setPriority(priority++);
    			helper.getHibSession().saveOrUpdate(cd);
    		}
    	}
    	
    	return changed;
	}
	
	public Map<CourseOffering, List<Class_>> getEnrollments(OnlineSectioningHelper helper) {
		Map<CourseOffering, List<Class_>> enrollments = new HashMap<CourseOffering, List<Class_>>();
		for (Integer crn: iCRNs) {
			CourseOffering co = BannerSection.findCourseOfferingForCrnAndTermCode(helper.getHibSession(), crn, iTermCode);
			if (co == null) {
				helper.error("No course offering found for CRN " + crn + " and banner session " + iTermCode);
				iResult = UpdateResult.PROBLEM;
				continue;
			}
			if (!iSession.equals(co.getInstructionalOffering().getSession()))
				continue;
			
			boolean foundClasses = false;
			for(Iterator<?> it = BannerSection.findAllClassesForCrnAndTermCode(helper.getHibSession(), crn, iTermCode).iterator(); it.hasNext();) {
				Class_ c = (Class_) it.next();
				if (!iSession.equals(c.getSession())) continue;
				
				foundClasses = true;
				List<Class_> classes = enrollments.get(co);
				if (classes == null) {
					classes = new ArrayList<Class_>(); enrollments.put(co, classes);
				}
				classes.add(c);
			}
			if (!foundClasses) {
				helper.error("No classes found for CRN " + crn + " and banner session " + iTermCode);
				iResult = UpdateResult.PROBLEM;
			}
		}
		return enrollments;
	}
	
	protected boolean eq(Object o1, Object o2) {
		if (o1 == null)
			return o2 == null;
		else
			return o1.equals(o2);
	}

	@Override
	public String name() {
		return "banner-update";
	}
	
	public static enum UpdateResult {
		NO_CHANGE,
		OK,
		PROBLEM,
		FAILURE
	}

	public static class Pair {
		private Long iCourseId, iClassId;
		public Pair(Long courseId, Long classId) {
			iCourseId = courseId; iClassId = classId;
		}
		public Long getCourseId() { return iCourseId; }
		public Long getClassId() { return iClassId; }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Pair)) return false;
			Pair p = (Pair)o;
			return getCourseId().equals(p.getCourseId()) && getClassId().equals(p.getClassId());
		}
		public int hashCode() {
			return getCourseId().hashCode() ^ getClassId().hashCode();
		}
	}

}
