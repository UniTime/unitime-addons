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
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.banner.model.BannerSection;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
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
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
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
	private String iTermCode, iExternalId, iFName, iMName, iLName, iEmail, iAcademicArea, iClassification, iMajor;
	private List<String[]> iGroups = new ArrayList<String[]>();
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
	
	public BannerUpdateStudentAction withCurriculum(String academicArea, String classification, String major) {
		iAcademicArea = academicArea;
		iClassification = classification;
		iMajor = major;
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
			if (iAcademicArea != null && iClassification != null)
				helper.getAction().addOptionBuilder().setKey("curriculum").setValue(iAcademicArea + "/" + iMajor + " " + iClassification);
			
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
			
			if (updateClassEnrollments(student, getEnrollments(helper), helper))
				changed = true;
			
			if (iStudentId == null)
				iStudentId = (Long)helper.getHibSession().save(student);
			else if (changed)
				helper.getHibSession().update(student);
			
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
	
	private boolean updateStudentDemographics(Student student, OnlineSectioningHelper helper) {
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
		
		// This makes the assumption that when working with Banner students have only one AcademicAreaClassification
		AcademicAreaClassification aac = null;
		for(Iterator<?> it = student.getAcademicAreaClassifications().iterator(); it.hasNext();){
			aac = (AcademicAreaClassification) it.next();
			break;
		}
		if (iAcademicArea != null && iClassification != null) {
			if (aac == null || !(
					(aac.getAcademicArea().getExternalUniqueId().equalsIgnoreCase(iAcademicArea) || aac.getAcademicArea().getAcademicAreaAbbreviation().equalsIgnoreCase(iAcademicArea)) &&
					(aac.getAcademicClassification().getExternalUniqueId().equalsIgnoreCase(iClassification) || aac.getAcademicClassification().getCode().equalsIgnoreCase(iAcademicArea)))) {
				AcademicArea aa = AcademicArea.findByExternalId(helper.getHibSession(), student.getSession().getUniqueId(), iAcademicArea);						
				if (aa == null)
					aa = AcademicArea.findByAbbv(helper.getHibSession(), student.getSession().getUniqueId(), iAcademicArea);
				if (aa == null){
					aa = new AcademicArea();
					aa.setPosMajors(new HashSet<PosMajor>());
					aa.setAcademicAreaAbbreviation(iAcademicArea);
					aa.setSession(student.getSession());
					aa.setExternalUniqueId(iAcademicArea);
					aa.setTitle(iAcademicArea);
					aa.setUniqueId((Long)helper.getHibSession().save(aa));
					helper.info("Added Academic Area:  " + iAcademicArea);
				}
				if (aac == null) 
					aac = new AcademicAreaClassification();
				aac.setAcademicArea(aa);
				
				AcademicClassification ac = AcademicClassification.findByExternalId(helper.getHibSession(), student.getSession().getUniqueId(), iClassification);
				if (ac == null)
					ac = AcademicClassification.findByCode(helper.getHibSession(), student.getSession().getUniqueId(), iClassification);
				if (ac == null){
					ac = new AcademicClassification();
					ac.setCode(iClassification);
					ac.setExternalUniqueId(iClassification);
					ac.setName(iClassification);
					ac.setSession(student.getSession());
					ac.setUniqueId((Long) helper.getHibSession().save(ac));
					helper.info("Added Academic Classification:  " + iClassification);
				}
				aac.setAcademicClassification(ac);
				if (aac.getStudent() == null) {
					aac.setStudent(student);
					student.addToacademicAreaClassifications(aac);				
				}
				changed = true;
			}
		}
		
		//This makes the assumption that when working with Banner students have only one Major
		if (iMajor != null && aac != null) {
			PosMajor m = null;
			for(Iterator<?> it = student.getPosMajors().iterator(); it.hasNext();) {
				m = (PosMajor) it.next();
				break;
			}
			if (m == null || !(iMajor.equalsIgnoreCase(m.getExternalUniqueId()) || iMajor.equalsIgnoreCase(m.getCode())) ||
					!(m.getAcademicAreas() != null && !m.getAcademicAreas().isEmpty() && m.getAcademicAreas().contains(aac.getAcademicArea()))) {
				student.getPosMajors().clear();
				
				PosMajor posMajor = PosMajor.findByExternalIdAcadAreaExternalId(helper.getHibSession(), student.getSession().getUniqueId(), iMajor, iAcademicArea);
				if (posMajor == null)
					posMajor = PosMajor.findByCodeAcadAreaAbbv(helper.getHibSession(), student.getSession().getUniqueId(), iMajor, iAcademicArea);
				if (posMajor == null) {
					posMajor = new PosMajor();
					posMajor.setCode(iMajor);
					posMajor.setExternalUniqueId(iMajor);
					posMajor.setName(iMajor);
					posMajor.setSession(student.getSession());
					posMajor.setUniqueId((Long)helper.getHibSession().save(posMajor));
					posMajor.addToacademicAreas(aac.getAcademicArea());
					aac.getAcademicArea().addToposMajors(posMajor);
					helper.info("Added Major:  " + iMajor + " to Academic Area:  " + iAcademicArea);
				}
				student.addToposMajors(posMajor);
				changed = true;
			} else if (m.getAcademicAreas() == null || m.getAcademicAreas().isEmpty()) {
				m.addToacademicAreas(aac.getAcademicArea());
				aac.getAcademicArea().addToposMajors(m);
				helper.info("Added Academic Area: " + iAcademicArea + " to existing Major:  " + iMajor);
				changed = true;
			}
		}
		return changed;
	}
	
	private boolean updateStudentGroups(Student student, OnlineSectioningHelper helper) {
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
	
	private boolean updateClassEnrollments(Student student, Map<CourseOffering, List<Class_>> courseToClassEnrollments, OnlineSectioningHelper helper) {
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
