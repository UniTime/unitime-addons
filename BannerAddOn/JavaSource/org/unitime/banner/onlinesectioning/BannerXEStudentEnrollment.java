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

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.QueueOut;
import org.unitime.banner.onlinesectioning.BannerUpdateStudentAction.Pair;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideIntent;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.purdue.XEInterface;
import org.unitime.timetable.onlinesectioning.custom.purdue.XEStudentEnrollment;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.updates.NotifyStudentAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllData;

/**
 * @author Tomas Muller
 */
public class BannerXEStudentEnrollment extends XEStudentEnrollment {
	
	@Override
	public boolean isCanRequestUpdates() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.studentUpdateRequests.enabled", "false"));
	}

	@Override
	public boolean requestUpdate(OnlineSectioningServer server, OnlineSectioningHelper helper, Collection<XStudent> students) throws SectioningException {
		if (students == null || students.isEmpty()) return false;
		if (!isCanRequestUpdates()) return false;
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("studentUpdateRequest");
		String term = getBannerTerm(server.getAcademicSession());
		for (XStudent student: students) {
			Element studentEl = root.addElement("student");
			studentEl.addAttribute("externalId", getBannerId(student));
			studentEl.addAttribute("session", term);
		}
		QueueOut out = new QueueOut();
		out.setXml(document);
		out.setStatus(QueueOut.STATUS_READY);
		out.setPostDate(new Date());
		helper.getHibSession().save(out);
		helper.getHibSession().flush();
		return true;
	}
	
	protected Map<CourseOffering, List<Class_>> getEnrollments(OnlineSectioningServer server, OnlineSectioningHelper helper, List<XEInterface.Registration> registration) {
		Map<CourseOffering, List<Class_>> enrollments = new HashMap<CourseOffering, List<Class_>>();
		AcademicSessionInfo session = server.getAcademicSession();
		String termCode = getBannerTerm(session);
		for (XEInterface.Registration reg: registration) {
			if (!reg.isRegistered()) continue;
			CourseOffering co = BannerSection.findCourseOfferingForCrnAndTermCode(helper.getHibSession(), Integer.parseInt(reg.courseReferenceNumber), termCode);
			if (co == null) {
				helper.error("No course offering found for CRN " + reg.courseReferenceNumber + " and banner session " + termCode);
				continue;
			}
			if (!session.getUniqueId().equals(co.getInstructionalOffering().getSession().getUniqueId()))
				continue;
			
			boolean foundClasses = false;
			for(Iterator<?> it = BannerSection.findAllClassesForCrnAndTermCode(helper.getHibSession(), Integer.parseInt(reg.courseReferenceNumber), termCode).iterator(); it.hasNext();) {
				Class_ c = (Class_) it.next();
				if (!session.getUniqueId().equals(c.getSession().getUniqueId())) continue;
				
				foundClasses = true;
				List<Class_> classes = enrollments.get(co);
				if (classes == null) {
					classes = new ArrayList<Class_>(); enrollments.put(co, classes);
				}
				classes.add(c);
			}
			if (!foundClasses) {
				helper.error("No classes found for CRN " + reg.courseReferenceNumber + " and banner session " + termCode);
			}
		}
		return enrollments;
	}
	
	public boolean isResetWaitListToggle() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.waitlist.resetWhenEnrolled"));
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
    		
    		if (cr.getCourseDemand().isWaitlist() && isResetWaitListToggle()) {
    			cr.getCourseDemand().setWaitlist(false);
    			changed = true;
    			helper.getHibSession().saveOrUpdate(cr.getCourseDemand());
    		}
    	}
    	
    	
    	Set<CourseDemand> exDropDeletes = new HashSet<CourseDemand>();
    	if (!enrollments.isEmpty()) {
    		for (StudentClassEnrollment enrollment: enrollments.values()) {
    			CourseRequest cr = course2request.get(enrollment.getCourseOffering());
    			if (cr != null && remaining.contains(cr.getCourseDemand())) {
    				if (cr.getCourseRequestOverrideIntent() == CourseRequestOverrideIntent.EX_DROP)
    					exDropDeletes.add(cr.getCourseDemand());
    				else if (cr.getCourseDemand().isWaitlist() && isResetWaitListToggle()) {
    					cr.getCourseDemand().setWaitlist(false);
    					helper.getHibSession().saveOrUpdate(cr.getCourseDemand());
    				}
    			}
    			student.getClassEnrollments().remove(enrollment);
    			helper.getHibSession().delete(enrollment);
    		}
    		changed = true;
    	}

    	if ((fixCourseDemands || !exDropDeletes.isEmpty()) && student.getUniqueId() != null) {
    		// removed intended extended course drops
    		if (!exDropDeletes.isEmpty()) {
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
	
	@Override
	protected boolean updateStudentRegistration(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent s, List<XEInterface.Registration> registration) throws SectioningException {
		if (s == null || s.getExternalId() == null) return false;
		
		try {
			helper.beginTransaction();
			
			Student student = Student.findByExternalIdBringBackEnrollments(helper.getHibSession(), server.getAcademicSession().getUniqueId(), s.getExternalId());
			if (student == null) {
				helper.commitTransaction();
				return false;
			}
		
			Map<CourseOffering, List<Class_>> enrollments = getEnrollments(server, helper, registration);
			
			boolean changed = updateClassEnrollments(student, enrollments, helper);
			
			if (changed)
				helper.getHibSession().update(student);
				
			for (int i = 0; i < helper.getAction().getEnrollmentCount(); i++)
				if (helper.getAction().getEnrollment(i).getType() == OnlineSectioningLog.Enrollment.EnrollmentType.STORED)
					helper.getAction().getEnrollmentBuilder(i).setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);

			// Reload student
			XStudent newStudent = ReloadAllData.loadStudent(student, null, server, helper);
			if (newStudent != null) {
				server.update(newStudent, true);
				OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
				enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
				helper.getAction().clearRequest();
				for (XRequest newRequest: newStudent.getRequests()) {
					helper.getAction().addRequest(OnlineSectioningHelper.toProto(newRequest));
					if (newRequest instanceof XCourseRequest && ((XCourseRequest)newRequest).getEnrollment() != null) {
						XEnrollment enrl = ((XCourseRequest)newRequest).getEnrollment();
						XOffering offering = server.getOffering(enrl.getOfferingId());
						for (XSection section: offering.getSections(enrl))
							enrollment.addSection(OnlineSectioningHelper.toProto(section, enrl));
					}
				}
				helper.getAction().addEnrollment(enrollment);
			}
			
			if (changed)
				server.execute(server.createAction(NotifyStudentAction.class).forStudent(student.getUniqueId()).oldStudent(s), helper.getUser());
			
			helper.commitTransaction();
		} catch (SectioningException e) {
			helper.rollbackTransaction();
			e.printStackTrace();
			throw e;
		} catch (Exception e) {
			helper.rollbackTransaction();
			e.printStackTrace();
			throw new SectioningException(e.getMessage(), e);
		}
		
		return true;
	}
	
	@Override
	protected boolean eligibilityIgnoreBannerRegistration(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, XEInterface.Registration reg) {
		// ignore sections that do not exist in UniTime (and of matching campus)
		// this is to fix synchronization issues when a class is cancelled
		// (it does not exist in UniTime, but still contains enrolled students in Banner)
		Number count = (Number)helper.getHibSession().createQuery(
				"select count(bs) from BannerSession s, BannerSection bs where " +
				"bs.session = s.session and s.bannerTermCode = :term and bs.crn = :crn")
				.setString("term", reg.term)
				.setString("crn", reg.courseReferenceNumber)
				.uniqueResult();
		return count.intValue() == 0 && getBannerCampus(server.getAcademicSession()).equals(reg.campus);
	}

}
