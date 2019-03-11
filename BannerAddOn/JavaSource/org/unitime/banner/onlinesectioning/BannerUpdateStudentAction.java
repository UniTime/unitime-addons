/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.banner.onlinesectioning;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideType;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideIntent;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentNote;
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
	private String iTermCode, iExternalId, iFName, iMName, iLName, iEmail, iCampus;
	private List<String[]> iGroups = new ArrayList<String[]>();
	private List<String[]> iAcadAreaClasfMj = new ArrayList<String[]>();
	private boolean iUpdateAcadAreaClasfMj = false;
	private List<String[]> iOverrides = new ArrayList<String[]>();
	private Set<Integer> iCRNs = new TreeSet<Integer>();
	private Long iStudentId;
	private Session iSession;
	private UpdateResult iResult = UpdateResult.OK;
	private List<String[]> iAdvisors = new ArrayList<String[]>();
	private String iOverrideTypes = null;
	
	public BannerUpdateStudentAction() {
		iOverrideTypes = ApplicationProperties.getProperty("banner.overrides.regexp");
	}
	
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
	
	public BannerUpdateStudentAction withAcadAreaClassificationMajor(String academicArea, String classification, String major, String campus) {
		if (academicArea == null || academicArea.isEmpty() || classification == null || classification.isEmpty() || major == null || major.isEmpty()) return this;
		iUpdateAcadAreaClasfMj = true;
		iAcadAreaClasfMj.add(new String[] {academicArea, classification, major, campus});
		return this;
	}
	
	public BannerUpdateStudentAction updateAcadAreaClassificationMajors(boolean update) {
		iUpdateAcadAreaClasfMj = update;
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
	
	public BannerUpdateStudentAction withAdvisor(String externalId, String type) {
		iAdvisors.add(new String[] {externalId, type});
		return this;
	}

	@Override
	public UpdateResult execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		boolean changed = false;
		try {
			iSession = SessionDAO.getInstance().get(server.getAcademicSession().getUniqueId(), helper.getHibSession());
			BannerSession bs = BannerSession.findBannerSessionForSession(server.getAcademicSession().getUniqueId(), helper.getHibSession());
			iCampus = (bs == null ? iSession.getAcademicInitiative() : bs.getBannerCampus());
			
			iStudentId = getStudentId(helper);
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
			if (!iAcadAreaClasfMj.isEmpty()) {
				for (String[] areaClasf: iAcadAreaClasfMj) {
					if (areaClasf[3] == null || areaClasf[3].equals(iCampus)) {
						helper.getAction().addOptionBuilder().setKey("curriculum").setValue(areaClasf[0] + "/" + areaClasf[2] + " " + areaClasf[1]);
						break;
					}
				}
			}
				
			
			if (iStudentId != null)
				action.getStudentBuilder().setUniqueId(iStudentId);
			
			Lock lock = (iStudentId == null ? null : server.lockStudent(iStudentId, null, name()));
			try {
				helper.beginTransaction();
				Student student = getStudent(helper);

				if (updateStudentDemographics(student, helper))
					changed = true;
				
				if (updateStudentGroups(student, helper))
					changed = true;
				
				if (updateAdvisors(student, helper))
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
			BannerSession bs = BannerSession.findBannerSessionForSession(sessionId, helper.getHibSession());
			iCampus = (bs == null ? iSession.getAcademicInitiative() : bs.getBannerCampus());
			
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
	
	public Long getStudentId(OnlineSectioningHelper helper) {
		return (Long)helper.getHibSession().createQuery("select s.uniqueId from Student s where " +
				"s.session.uniqueId = :sessionId and s.externalUniqueId = :externalId")
				.setLong("sessionId", iSession.getUniqueId()).setString("externalId",iExternalId)
				.setCacheable(true).uniqueResult();
	}
	
	public Student getStudent(OnlineSectioningHelper helper) {
		Student student = Student.findByExternalIdBringBackEnrollments(helper.getHibSession(), iSession.getUniqueId(), iExternalId);
		if (student == null) {
			student = new Student();
			student.setExternalUniqueId(iExternalId);
			student.setSession(SessionDAO.getInstance().get(iSession.getUniqueId(), helper.getHibSession()));
			student.setFreeTimeCategory(0);
			student.setSchedulePreference(0);
			student.setClassEnrollments(new HashSet<StudentClassEnrollment>());
			student.setAreaClasfMajors(new HashSet<StudentAreaClassificationMajor>());
			student.setCourseDemands(new HashSet<CourseDemand>());
			student.setGroups(new HashSet<StudentGroup>());
			student.setAccomodations(new HashSet<StudentAccomodation>());
			student.setNotes(new HashSet<StudentNote>());
			student.setAdvisors(new HashSet<Advisor>());
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
		
		if (iUpdateAcadAreaClasfMj) {
			List<StudentAreaClassificationMajor> remaining = new ArrayList<StudentAreaClassificationMajor>(student.getAreaClasfMajors());
			aac: for (String[] areaClasf: iAcadAreaClasfMj) {
				String area = areaClasf[0], clasf = areaClasf[1], major = areaClasf[2], campus = areaClasf[3];
				if (campus != null && !campus.equals(iCampus)) continue;
				for (Iterator<StudentAreaClassificationMajor> i = remaining.iterator(); i.hasNext(); ) {
					StudentAreaClassificationMajor aac = i.next();
					if ((area.equalsIgnoreCase(aac.getAcademicArea().getExternalUniqueId()) || area.equalsIgnoreCase(aac.getAcademicArea().getAcademicAreaAbbreviation())) &&
						(clasf.equalsIgnoreCase(aac.getAcademicClassification().getExternalUniqueId()) || clasf.equalsIgnoreCase(aac.getAcademicClassification().getCode())) &&
						(major.equalsIgnoreCase(aac.getMajor().getExternalUniqueId()) || major.equalsIgnoreCase(aac.getMajor().getCode()))) {
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

				StudentAreaClassificationMajor aac = new StudentAreaClassificationMajor();
				aac.setAcademicArea(aa);
				aac.setAcademicClassification(ac);
				aac.setMajor(posMajor);
				aac.setStudent(student);
				student.addToareaClasfMajors(aac);
				changed = true;
			}
			
			for (StudentAreaClassificationMajor aac: remaining) {
				student.getAreaClasfMajors().remove(aac);
				helper.getHibSession().delete(aac);
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
		for (Iterator<StudentGroup> i = student.getGroups().iterator(); i.hasNext(); ) {
			StudentGroup g = i.next();
			if (groups.remove(g)) continue;
			if (g.getExternalUniqueId() != null) {
				g.getStudents().remove(student);
				i.remove();
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
	
	protected Map<InstructionalOffering, Map<String, Set<Class_>>> getOverrides(OnlineSectioningHelper helper) {
		Map<InstructionalOffering, Map<String, Set<Class_>>> restrictions = new HashMap<InstructionalOffering, Map<String, Set<Class_>>>();
		for (String[] override: iOverrides) {
			String type = override[0];
			if (type == null || type.isEmpty()) continue;
			if (iOverrideTypes != null && !type.matches(iOverrideTypes)) {
				helper.info("Ignoring override type " + type);
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
					Map<String, Set<Class_>> type2classes = restrictions.get(co.getInstructionalOffering());
					if (type2classes == null) {
						type2classes = new HashMap<String, Set<Class_>>();
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
				
				Map<String, Set<Class_>> type2classes = restrictions.get(co.getInstructionalOffering());
				if (type2classes == null) {
					type2classes = new HashMap<String, Set<Class_>>();
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
	
	protected OverrideType getType(Map<String, Set<Class_>> restrictions) {
		boolean time = false, space = false;
		OverrideType other = null;
		Set<Class_> otherRestrictions = null;
		for (Map.Entry<String, Set<Class_>> e: restrictions.entrySet()) {
			String type = e.getKey();
			if (OverrideType.AllowTimeConflict.getReference().equalsIgnoreCase(type)) time = true;
			else if (OverrideType.AllowOverLimit.getReference().equalsIgnoreCase(type)) space = true;
			else {
				for (OverrideType t: OverrideType.values())
					if (t.getReference().equalsIgnoreCase(type)) {
						if (other == null || e.getValue().size() > otherRestrictions.size() || (e.getValue().size() == otherRestrictions.size() && t.ordinal() < other.ordinal())) {
							other = t;
							otherRestrictions = e.getValue();
						}
					}
			}
		}
		if (time && space) return OverrideType.AllowOverLimitTimeConflict;
		if (time) return OverrideType.AllowTimeConflict;
		if (space) return OverrideType.AllowOverLimit;
		if (other != null) return other;
		return OverrideType.Other;
	}
	
	protected Set<Class_> getClasses(Map<String, Set<Class_>> restrictions) {
		Set<Class_> union = new HashSet<Class_>();
		for (Set<Class_> c: restrictions.values()) {
			union.addAll(c);
		}
		return union;
	}
	
	protected boolean updateStudentOverrides(Student student, OnlineSectioningServer server, OnlineSectioningHelper helper) {
		boolean changed = false;

		Map<InstructionalOffering, Map<String, Set<Class_>>> restrictions = getOverrides(helper);
		
		@SuppressWarnings("unchecked")
		List<OverrideReservation> overrides = (List<OverrideReservation>)helper.getHibSession().createQuery(
				"select r from OverrideReservation r inner join r.students s where s.uniqueId = :studentId")
			.setLong("studentId", student.getUniqueId()).list();
		
		overrides: for (Map.Entry<InstructionalOffering, Map<String, Set<Class_>>> e: restrictions.entrySet()) {
			InstructionalOffering io = e.getKey();
			
			OverrideType type = getType(e.getValue());
			Set<Class_> classes = getClasses(e.getValue());
			
			if (classes.isEmpty() && !type.isAllowOverLimit() && !type.isAllowTimeConflict()) continue;

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
							if (r.getType() == XReservationType.IndividualOverride && r.getReservationId().equals(override.getUniqueId())) {
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
							if (r.getType() == XReservationType.IndividualOverride && r.getReservationId().equals(override.getUniqueId())) {
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
		Date ts = new Date();

		Hashtable<Pair, StudentClassEnrollment> enrollments = new Hashtable<Pair, StudentClassEnrollment>();
		if (student.getClassEnrollments() != null) {
			List<StudentClassEnrollment> duplicates = new ArrayList<StudentClassEnrollment>();
        	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
        		StudentClassEnrollment previous = enrollments.put(new Pair(enrollment.getCourseOffering().getUniqueId(), enrollment.getClazz().getUniqueId()), enrollment);
        		// check for duplicate enrollments
        		if (previous != null) duplicates.add(previous);
        	}
        	// remove duplicate enrollments
        	for (StudentClassEnrollment enrollment: duplicates) {
    			student.getClassEnrollments().remove(enrollment);
    			helper.getHibSession().delete(enrollment);
    			changed = true;
        	}
    	}
    	int nextPriority = 0;
    	for (CourseDemand cd: student.getCourseDemands())
    		if (!cd.isAlternative() && cd.getPriority() >= nextPriority)
    			nextPriority = cd.getPriority() + 1;
    	Set<CourseDemand> remaining = new HashSet<CourseDemand>(student.getCourseDemands());
    	boolean fixCourseDemands = false;
    	
    	// populate course2request, check for course request duplicates
    	Map<CourseOffering, CourseRequest> course2request = new Hashtable<CourseOffering, CourseRequest>();
    	for (CourseDemand cd: student.getCourseDemands())
    		for (CourseRequest cr: cd.getCourseRequests()) {
    			CourseRequest previous = course2request.put(cr.getCourseOffering(), cr);
    			if (previous != null) fixCourseDemands = true;
    		}

    	for (Map.Entry<CourseOffering, List<Class_>> entry: courseToClassEnrollments.entrySet()) {
    		CourseOffering co = entry.getKey();
    		
    		CourseRequest cr = course2request.get(co);
    		if (cr == null) {
    			CourseDemand cd = new CourseDemand();
    			cd.setTimestamp(ts);
    			cd.setCourseRequests(new HashSet<CourseRequest>());
    			cd.setEnrollmentMessages(new HashSet<StudentEnrollmentMessage>());
    			cd.setStudent(student);
    			student.getCourseDemands().add(cd);
    			cd.setAlternative(false);
    			cd.setPriority(nextPriority++);
    			cd.setWaitlist(false);
    			cr = new CourseRequest();
    			cd.getCourseRequests().add(cr);
    			cr.setCourseDemand(cd);
    			cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
    			cr.setAllowOverlap(false);
    			cr.setCredit(0);
    			cr.setOrder(0);
    			cr.setCourseOffering(co);
    			fixCourseDemands = true;
        		changed = true;
    		} else {
    			if (!remaining.remove(cr.getCourseDemand()) && cr.getCourseDemand().getCourseRequests().size() > 1) {
    				// course demand has been already removed -> need to split the course demand
    				cr.getCourseDemand().getCourseRequests().remove(cr);
    				CourseDemand cd = new CourseDemand();
        			cd.setTimestamp(ts);
        			cd.setCourseRequests(new HashSet<CourseRequest>());
        			cd.setEnrollmentMessages(new HashSet<StudentEnrollmentMessage>());
        			cd.setStudent(student);
        			student.getCourseDemands().add(cd);
        			cd.setAlternative(false);
        			cd.setPriority(nextPriority++);
        			cd.setWaitlist(false);
        			cr.setCourseDemand(cd);
        			cd.getCourseRequests().add(cr);
        			fixCourseDemands = true;
        			changed = true;
    			}
    			for (Iterator<StudentEnrollmentMessage> i = cr.getCourseDemand().getEnrollmentMessages().iterator(); i.hasNext(); ) {
					StudentEnrollmentMessage message = i.next();
					helper.getHibSession().delete(message);
					i.remove();
				}
    		}
    		
    		for (Class_ clazz: entry.getValue()) {
    			StudentClassEnrollment enrollment = enrollments.remove(new Pair(co.getUniqueId(), clazz.getUniqueId()));
        		if (enrollment == null) {
            		enrollment = new StudentClassEnrollment();
            		enrollment.setStudent(student);
            		enrollment.setClazz(clazz);
            		enrollment.setCourseOffering(co);
            		enrollment.setTimestamp(ts);
            		student.getClassEnrollments().add(enrollment);    
            		changed = true;
        		}

        		if (enrollment.getCourseRequest() == null || !cr.equals(enrollment.getCourseRequest())) {
        			enrollment.setCourseRequest(cr);
        			changed = true;
        		}       		
    		}
    	}
    	
    	
    	Set<CourseDemand> deletes = new HashSet<CourseDemand>();
    	Set<CourseDemand> exDropDeletes = new HashSet<CourseDemand>();
    	if (!enrollments.isEmpty()) {
    		for (StudentClassEnrollment enrollment: enrollments.values()) {
    			CourseRequest cr = course2request.get(enrollment.getCourseOffering());
    			if (cr != null && remaining.contains(cr.getCourseDemand())) {
    				deletes.add(cr.getCourseDemand());
    				if (cr.getCourseRequestOverrideIntent() == CourseRequestOverrideIntent.EX_DROP)
    					exDropDeletes.add(cr.getCourseDemand());
    			}
    			student.getClassEnrollments().remove(enrollment);
    			helper.getHibSession().delete(enrollment);
    		}
    		changed = true;
    	}

    	if ((fixCourseDemands || !deletes.isEmpty()) && student.getUniqueId() != null) {
    		// removed unused course demands (only when not in the registration mode)
    		if (student.getSession().getStatusType() == null || !student.getSession().getStatusType().canPreRegisterStudents())
    			for (CourseDemand cd: (fixCourseDemands ? remaining : deletes)) {
        			if (cd.getFreeTime() != null)
        				helper.getHibSession().delete(cd.getFreeTime());
        			for (CourseRequest cr: cd.getCourseRequests())
        				helper.getHibSession().delete(cr);
        			student.getCourseDemands().remove(cd);
        			helper.getHibSession().delete(cd);
        		}
    		else if (!exDropDeletes.isEmpty()) {
    			for (CourseDemand cd: exDropDeletes) {
        			if (cd.getFreeTime() != null)
        				helper.getHibSession().delete(cd.getFreeTime());
        			for (CourseRequest cr: cd.getCourseRequests())
        				helper.getHibSession().delete(cr);
        			student.getCourseDemands().remove(cd);
        			helper.getHibSession().delete(cd);
        		}
    		}
    		// fix priorities
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
	
	protected boolean updateAdvisors(Student student, OnlineSectioningHelper helper) {
		Set<Advisor> advisors = new HashSet<Advisor>();
		for (String[] a: iAdvisors) {
			String externalId = a[0], type = a[1];
			Roles role = null;
			if (type != null && !type.isEmpty())
				role = Roles.getRole(type + " Advisor", helper.getHibSession());
			if (role == null)
				role = Roles.getRole("Advisor", helper.getHibSession());
			if (role == null) {
				helper.warn("No advisor role found for " + type);
				continue;
			}
			Advisor advisor = (Advisor)helper.getHibSession().createQuery(
					"from Advisor where externalUniqueId = :externalId and role.roleId = :roleId and session.uniqueId = :sessionId")
					.setString("externalId", externalId).setLong("roleId", role.getRoleId()).setLong("sessionId", iSession.getUniqueId())
					.setCacheable(true).setMaxResults(1).uniqueResult();
			if (advisor == null) {
				advisor = new Advisor();
				advisor.setExternalUniqueId(externalId);
				advisor.setRole(role);
				advisor.setSession(iSession);
				advisor.setStudents(new HashSet<Student>());
				advisor.setUniqueId((Long)helper.getHibSession().save(advisor));
				helper.info("Added Advisor:  " + advisor.getExternalUniqueId() + " - " + advisor.getRole().getReference() + " to session " + advisor.getSession().academicInitiativeDisplayString());
			}
			advisors.add(advisor);
		}
		boolean changed = false;
		if (student.getAdvisors() != null)
			for (Iterator<Advisor> i = student.getAdvisors().iterator(); i.hasNext(); ) {
				Advisor a = i.next();
				if (advisors.remove(a)) continue;
				a.getStudents().remove(student);
				i.remove();
				changed = true;
			}
		for (Advisor a: advisors) {
			a.addTostudents(student);
			student.addToadvisors(a);
			changed = true;
		}
    	return changed;
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
