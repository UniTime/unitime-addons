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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.dom4j.Element;
import org.unitime.banner.model.BannerSection;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.BaseImport;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;

/**
 * @author says
 *
 */
public class BannerStudentEnrollmentMessage extends BaseImport {

	private static final String propertiesElementName = "properties";
	private static final String rootElementName = "enterprise";
	private static String expectedDataSource;
	private static String expectedSource;
	private String messageDateTime;
	
	private Hashtable<Long, Set<Long>> iUpdatedStudents = new Hashtable<Long, Set<Long>>();
	
	@Override
	public void loadXml(Element rootElement) throws Exception {
		try {
			if (!rootElement.getName().equalsIgnoreCase(rootElementName )) {
	        	throw new Exception("Given XML is not a banner enterprise message.");
	        }
	        beginTransaction();
	        
			propertiesElement(rootElement.element(propertiesElementName));
			
			membershipElements(rootElement);

			for (Map.Entry<Long, Set<Long>> entry: iUpdatedStudents.entrySet())
				if (!entry.getValue().isEmpty())
					StudentSectioningQueue.studentChanged(getHibSession(), null, entry.getKey(), entry.getValue());

			commitTransaction();
		} catch (Exception e) {
			fatal("Exception: " + e.getMessage(), e);
			rollbackTransaction();
			throw e;
		}
	}

	private void propertiesElement(Element element) throws Exception {

		String datasource = element.elementTextTrim("datasource");
		if (getExpectedDataSource() != null && !datasource.equalsIgnoreCase(getExpectedDataSource())){
			throw new Exception("Datasource for message does not match expected datasource:  " + datasource + ", expected:  " + getExpectedDataSource());
		}
		
		messageDateTime = element.elementTextTrim("datetime");
		if (messageDateTime == null){
			throw new Exception("Missing date stamp for this message.");			
		}
		
		debug("Processing banner message from:  " + datasource + " with a date time of " + messageDateTime);
	}

	private String sourceIdElement(Element sourceIdElement) throws Exception{
		if (sourceIdElement == null){
			return(null);
		}
		String source = sourceIdElement.elementTextTrim("source");
		if (getExpectedSource() != null && !getExpectedSource().equalsIgnoreCase(source)){
			throw new Exception("Source for membership element does not match expected source:  " + source + ", expected:  " + getExpectedSource());
		}
		return(sourceIdElement.elementTextTrim("id"));
	}
	
	private void membershipElements(Element rootElement) throws Exception {
		for (Element membershipElement : (List<Element>)rootElement.elements("membership")){
			String crnTerm = sourceIdElement(membershipElement.element("sourceid"));
			if (crnTerm == null || crnTerm.length() == 0){
				throw new Exception("Membership element is missing the crn and term data.");				
			}
			int dotIndex = crnTerm.indexOf('.');
			if (dotIndex <= 0){
				throw new Exception("Membership element has badly formed crn and term data:  " + crnTerm);								
			}
			String crnStr = crnTerm.substring(0, dotIndex);
			String termCode = crnTerm.substring(dotIndex + 1, crnTerm.length());
			Integer crn = null;
			try {
				crn = Integer.parseInt(crnStr);
			} catch (Exception e) {
				throw new Exception("Membership element has a non-numeric crn:  " + crnStr);								
			}
			CourseOffering courseOffering = BannerSection.findCourseOfferingForCrnAndTermCode(crn, termCode);
			if (courseOffering == null){
				error("Enrollments for CRN:  " + crn.toString() + " not loaded.  No UniTime course offering found to match CRN:  " + crn.toString() + " in banner term:  " + termCode);
				continue;
			}
			List classes = BannerSection.findAllClassesForCrnAndTermCode(crn, termCode);
			Session acadSession = courseOffering.getSubjectArea().getSession();
			Set<Long> updatedStudents = iUpdatedStudents.get(acadSession.getUniqueId());
			if (updatedStudents == null) {
				updatedStudents = new HashSet<Long>();
				iUpdatedStudents.put(acadSession.getUniqueId(), updatedStudents);
			}
			memberElements(membershipElement, acadSession, courseOffering, classes, updatedStudents);
			flushIfNeeded(true);
		}
	}

	private void memberElements(Element membershipElement, Session acadSession,
			CourseOffering courseOffering, List classes, Set<Long> updatedStudents) throws Exception {
		for (Element memberElement : (List<Element>)membershipElement.elements("member")){
			String id = sourceIdElement(memberElement.element("sourceid"));
			if (id == null || id.length() == 0){
				throw new Exception("Missing student id from member element.");
			}
			String idType = memberElement.elementTextTrim("idtype");
			if (idType == null || !idType.equals("1")){
				throw new Exception("Received invalid id type:  " + id + " expected:  1.");
			}
			Student student = Student.findByExternalId(acadSession.getUniqueId(), id);
			Long studentId = null;
			if (student == null){
				student = new Student();
        		student = new Student();
                student.setSession(acadSession);
	            student.setFirstName("Name");
	            student.setLastName("Unknown");
	            student.setExternalUniqueId(id);
	            student.setFreeTimeCategory(new Integer(0));
	            student.setSchedulePreference(new Integer(0));
	            student.setClassEnrollments(new HashSet<StudentClassEnrollment>());
	            student.setCourseDemands(new HashSet<CourseDemand>());
	            studentId = (Long)getHibSession().save(student);
			} else {
				studentId = student.getUniqueId();
			}
			Element roleElement = memberElement.element("role");
			if (roleElement == null){
				throw new Exception("Missing role element.");				
			}
			String recStatus = getOptionalStringAttribute(roleElement, "recstatus");
			if (recStatus != null && recStatus.equals("3")){
				if (deleteEnrollment(student, courseOffering, classes)) {
					updatedStudents.add(studentId);
				}
			} else {
				if (addUpdateEnrollment(student, courseOffering, classes)) {
					updatedStudents.add(studentId);
				}
			}
		}		
	}
	
	private Vector<StudentClassEnrollment> findStudentClassEnrollments(Student student, List classes){
		Vector<StudentClassEnrollment> enrollments = new Vector<StudentClassEnrollment>();
		if (student.getClassEnrollments() != null && !student.getClassEnrollments().isEmpty()){
			for(Iterator cit = classes.iterator(); cit.hasNext();){
				Class_ c = (Class_) cit.next();
				StudentClassEnrollment sce = findStudentClassEnrollment(student, c);
				if (sce != null) {
					enrollments.add(sce);
				}	
			}
		}
		return(enrollments);
	}
	
	private StudentClassEnrollment findStudentClassEnrollment(Student student, Class_ clazz){
		StudentClassEnrollment studentClassEnrollment = null;
		if (student.getClassEnrollments() != null){
			for (Iterator ceIt = student.getClassEnrollments().iterator(); ceIt.hasNext();){
				StudentClassEnrollment sce = (StudentClassEnrollment) ceIt.next();
				if (sce.getClazz().getUniqueId().equals(clazz.getUniqueId())){
					studentClassEnrollment = sce;
					break;
				}
			}	
		}
		return(studentClassEnrollment);
	}

	private boolean addUpdateEnrollment(Student student,
			CourseOffering courseOffering, List classes) {
		boolean changed = false;
		for (Iterator it = classes.iterator(); it.hasNext(); ){
			Class_ c = (Class_) it.next();
			StudentClassEnrollment sce = findStudentClassEnrollment(student, c);
			if (sce == null) {
				changed = true;
				sce = new StudentClassEnrollment();
		    	sce.setStudent(student);
		    	sce.setClazz(c);
		    	sce.setCourseOffering(courseOffering);
		    	sce.setTimestamp(new java.util.Date());
		    	student.addToclassEnrollments(sce);
			}
		}
		
		// make sure all class enrollments have a course request filled in
    	boolean fixCourseDemands = false;
    	Set<CourseDemand> remaining = new HashSet<CourseDemand>(student.getCourseDemands());
    	enrollments: for (StudentClassEnrollment sce: student.getClassEnrollments()) {
			if (sce.getCourseRequest() != null) {
				// already set -> no change is needed
    			remaining.remove(sce.getCourseRequest().getCourseDemand());
    		} else {
    			for (CourseDemand d: student.getCourseDemands()) {
        			for (CourseRequest r: d.getCourseRequests()) {
        				if (r.getCourseOffering().equals(courseOffering)) {
        					// not set, but there is one that can be used already
        					sce.setCourseRequest(r);
        					changed = true;
        					continue enrollments;
        				}
        			}
    			}
    			
    			// create a new request
            	CourseDemand cd = new CourseDemand();
    			cd.setTimestamp(new java.util.Date());
    			cd.setCourseRequests(new HashSet<CourseRequest>());
    			cd.setStudent(student);
    			student.addTocourseDemands(cd);
    			cd.setAlternative(false);
    			cd.setPriority(student.getCourseDemands().size() + 1);
    			cd.setWaitlist(false);
    			CourseRequest cr = new CourseRequest();
    			cd.getCourseRequests().add(cr);
    			cr.setCourseDemand(cd);
    			cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
    			cr.setAllowOverlap(false);
    			cr.setCredit(0);
    			cr.setOrder(0);
    			cr.setCourseOffering(sce.getCourseOffering());
    			sce.setCourseRequest(cr);
    			changed = true;
    			fixCourseDemands = true;
    		}
    	}
    	
    	if (fixCourseDemands) {
    		// removed unused course demands
    		for (CourseDemand cd: remaining) {
    			if (cd.getFreeTime() != null)
    				getHibSession().delete(cd.getFreeTime());
    			for (CourseRequest cr: cd.getCourseRequests())
    				getHibSession().delete(cr);
    			student.getCourseDemands().remove(cd);
    			getHibSession().delete(cd);
    		}
    		// fix priorities
    		int priority = 0;
    		for (CourseDemand cd: new TreeSet<CourseDemand>(student.getCourseDemands())) {
    			cd.setPriority(priority++);
    			getHibSession().saveOrUpdate(cd);
    		}
    	}
    	
		if (changed){
			getHibSession().saveOrUpdate(student);
		}
		return changed;
	}

	private boolean deleteEnrollment(Student student, CourseOffering courseOffering, List classes) {
		boolean changed = false;
		Vector<StudentClassEnrollment> enrollments = findStudentClassEnrollments(student, classes);
		for (StudentClassEnrollment sce : enrollments){
			changed = true;
			student.getClassEnrollments().remove(sce);
			getHibSession().delete(sce);
		}
		if (changed){
			getHibSession().update(student);
		}
		return changed;
	}

	/**
	 * @return the expectedDataSource
	 */
	public static String getExpectedDataSource() {
		if (expectedDataSource == null){
			expectedDataSource = ApplicationProperties.getProperty("banner.messaging.expected.datasource", "Purdue University");
		}
		return expectedDataSource;
	}

	/**
	 * @return the expectedSource
	 */
	public static String getExpectedSource() {
		if (expectedSource == null){
			expectedSource = ApplicationProperties.getProperty("banner.messaging.expected.source", "Purdue University");
		}
		return expectedSource;
	}
	
	

}
