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

package org.unitime.banner.dataexchange;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Element;
import org.unitime.banner.model.BannerSection;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.BaseImport;
import org.unitime.timetable.dataexchange.StudentEnrollmentImport.Pair;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.ExamType;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.TimetableManager;
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
		boolean trimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();

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
	        for (Student student: getHibSession().createQuery(
                    "select distinct s from Student s " +
                    "left join fetch s.courseDemands as cd " +
                    "left join fetch cd.courseRequests as cr " +
                    "left join fetch s.classEnrollments as e " +
                    "where s.session.uniqueId=:sessionId and s.externalUniqueId is not null", Student.class).
                    setParameter("sessionId",session.getUniqueId()).list()) { 
                        students.put(student.getExternalUniqueId(), student);
                }

	        Date ts = new Date();
 	        for (Iterator i = rootElement.elementIterator("student"); i.hasNext(); ) {
	            Element studentElement = (Element) i.next();
	            
	            String externalId = studentElement.attributeValue("externalId");
	            if (externalId == null) continue;
	            while (trimLeadingZerosFromExternalId && externalId.startsWith("0")) externalId = externalId.substring(1);
	            
	            boolean fixCourseDemands = false;

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
            	int nextPriority = 0;
            	for (CourseDemand cd: student.getCourseDemands())
            		if (!cd.isAlternative() && cd.getPriority() >= nextPriority)
            			nextPriority = cd.getPriority() + 1;
            	Set<CourseDemand> remaining = new HashSet<CourseDemand>(student.getCourseDemands());

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
                		if (enrollment == null) {
                    		enrollment = new StudentClassEnrollment();
                    		enrollment.setStudent(student);
                    		enrollment.setClazz(clazz);
                    		enrollment.setCourseOffering(course);
                    		enrollment.setTimestamp(ts);
                    		student.getClassEnrollments().add(enrollment);

                    		demands: for (CourseDemand d: student.getCourseDemands()) {
                    			for (CourseRequest r: d.getCourseRequests()) {
                    				if (r.getCourseOffering().equals(course)) {
                    					enrollment.setCourseRequest(r);
                    					break demands;
                    				}
                    			}
                    		}
                    		
                    		if (student.getUniqueId() != null) updatedStudents.add(student.getUniqueId());
                		}
                		
                		if (enrollment.getCourseRequest() != null) {
                			remaining.remove(enrollment.getCourseRequest().getCourseDemand());
                		} else {
                			CourseDemand cd = new CourseDemand();
                			cd.setTimestamp(ts);
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
                    		if (student.getUniqueId() != null) updatedStudents.add(student.getUniqueId());
                		}
            		}
            	}
            	
            	if (!enrollments.isEmpty()) {
            		for (StudentClassEnrollment enrollment: enrollments.values()) {
            			student.getClassEnrollments().remove(enrollment);
            			getHibSession().remove(enrollment);
                		updatedStudents.add(student.getUniqueId());
            		}
            	}
            	
            	if (student.getUniqueId() == null) {
            		getHibSession().persist(student);
            	} else {
            		getHibSession().merge(student);
            	}
            	
            	if (fixCourseDemands) {
            		// removed unused course demands
            		for (CourseDemand cd: remaining) {
            			if (cd.getFreeTime() != null)
            				getHibSession().remove(cd.getFreeTime());
            			for (CourseRequest cr: cd.getCourseRequests())
            				getHibSession().remove(cr);
            			student.getCourseDemands().remove(cd);
            			getHibSession().remove(cd);
            		}
            		int priority = 0;
            		for (CourseDemand cd: new TreeSet<CourseDemand>(student.getCourseDemands())) {
            			cd.setPriority(priority++);
            			if (cd.getUniqueId() == null)
                			getHibSession().persist(cd);
            			else
            				getHibSession().merge(cd);
            		}
            	}

	            flushIfNeededDoNotClearSession(true);
	        }
            
 	        for (Student student: students.values()) {
        		for (Iterator<StudentClassEnrollment> i = student.getClassEnrollments().iterator(); i.hasNext(); ) {
        			StudentClassEnrollment enrollment = i.next();
        			getHibSession().remove(enrollment);
        			i.remove();
     	        	updatedStudents.add(student.getUniqueId());
        		}
        		getHibSession().merge(student);
 	        }
 	        
 	        if (!updatedStudents.isEmpty())
 	 	        StudentSectioningQueue.studentChanged(getHibSession(), null, session.getUniqueId(), updatedStudents);
            
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
                for (ExamType type: ExamType.findAllOfType(ExamType.sExamTypeFinal))
                	new UpdateExamConflicts(this).update(session.getUniqueId(), type.getUniqueId(), getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }

        if (session!=null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.midtermExam.updateConflicts","false"))) {
            try {
                beginTransaction();
                for (ExamType type: ExamType.findAllOfType(ExamType.sExamTypeMidterm))
                	new UpdateExamConflicts(this).update(session.getUniqueId(), type.getUniqueId(), getHibSession());
                commitTransaction();
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
                rollbackTransaction();
            }
        }
        
        info("  Banner Student Enrollment Load Complete");
	}

}
