/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package org.unitime.banner.dataexchange;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Element;
import org.unitime.banner.model.BannerSection;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.BaseImport;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Exam;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.test.UpdateExamConflicts;

/**
 * 
 * @author says
 *
 */
public class BannerStudentEnrollmentImport extends BaseImport {
	TimetableManager manager = null;
	HashMap<Integer, Vector<Class_>> classes = new HashMap<Integer, Vector<Class_>>();
	HashMap<Integer, CourseOffering> courseOfferings = new HashMap<Integer, CourseOffering>();
	boolean trimLeadingZerosFromExternalId = false;
	public BannerStudentEnrollmentImport() {
		super();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		String rootElementName = "bannerStudentEnrollments";
		String trimLeadingZeros =
	        ApplicationProperties.getProperty("tmtbl.data.exchange.trim.externalId","false");
		if (trimLeadingZeros.equals("true")){
			trimLeadingZerosFromExternalId = true;
		}

        if (!rootElement.getName().equalsIgnoreCase(rootElementName)) {
        	throw new Exception("Given XML file is not a Student Enrollments load file.");
        }
        
        Session session = null;
		try {
	        String campus = rootElement.attributeValue("campus");
	        String year   = rootElement.attributeValue("year");
	        String term   = rootElement.attributeValue("term");
	        String created = rootElement.attributeValue("created");
			beginTransaction();
	        session = Session.getSessionUsingInitiativeYearTerm(campus, year, term);
	        if(session == null) {
	           	throw new Exception("No session found for the given campus, year, and term.");
	        }
	        loadClasses(session.getUniqueId());
	        info("classes loaded");
	        if (manager == null){
	        	manager = findDefaultManager();
	        }
	        if (created != null) {
				ChangeLog.addChange(getHibSession(), manager, session, session, created, ChangeLog.Source.DATA_IMPORT_STUDENT_ENROLLMENTS, ChangeLog.Operation.UPDATE, null, null);
	        }
         
            /* 
             * If some records of a table related to students need to be explicitly deleted, 
             * hibernate can also be used to delete them. For instance, the following query 
             * deletes all student class enrollments for given academic session:
             *   
             * delete StudentClassEnrollment sce where sce.student.uniqueId in
             *      (select s.uniqueId from Student s where s.session.uniqueId=:sessionId)
             */
            
            getHibSession().createQuery("delete StudentClassEnrollment sce where sce.student.uniqueId in (select s.uniqueId from Student s where s.session.uniqueId=:sessionId)").setLong("sessionId", session.getUniqueId().longValue()).executeUpdate();
            
            flush(true);
            String elementName = "student";
 	        for ( Iterator<?> it = rootElement.elementIterator(); it.hasNext(); ) {
	            Element studentElement = (Element) it.next();
	            String externalId = getRequiredStringAttribute(studentElement, "externalId", elementName);
	            if (trimLeadingZerosFromExternalId){
	            	while (externalId.startsWith("0")) externalId = externalId.substring(1);
	            }
            	Student student = fetchStudent(externalId, session.getUniqueId());
                String firstName = getOptionalStringAttribute(studentElement, "firstName");
	            String lastName = getOptionalStringAttribute(studentElement, "lastName");
            	if (student == null){
            		student = new Student();
	                student.setSession(session);
		            student.setFirstName(firstName==null?"Name":firstName);
		            student.setMiddleName(getOptionalStringAttribute(studentElement, "middleName"));
		            student.setLastName(lastName==null?"Unknown":lastName);
		            student.setEmail(getOptionalStringAttribute(studentElement, "email"));
		            student.setExternalUniqueId(externalId);
		            student.setFreeTimeCategory(new Integer(0));
		            student.setSchedulePreference(new Integer(0));
            	} else {
		            student.setFirstName(firstName==null?"Name":firstName);
		            student.setMiddleName(getOptionalStringAttribute(studentElement, "middleName"));
		            student.setLastName(lastName==null?"Unknown":lastName);
            	}
            	elementSection(studentElement, student);
                getHibSession().save(student);

	            flushIfNeeded(true);
	        }
            
            commitTransaction();
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
        info("Application property: tmtbl.data.import.studentEnrl.class.updateEnrollments = " + ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.class.updateEnrollments","false"));
        if (session != null && "true".equals(ApplicationProperties.getProperty("tmtbl.data.import.studentEnrl.class.updateEnrollments","false"))){
            try {
                beginTransaction();
                info("  Updating class enrollments...");
                Class_.updateClassEnrollmentForSession(session, getHibSession());
                info("  Updating course offering enrollments...");
                CourseOffering.updateCourseOfferingEnrollmentForSession(session, getHibSession());
            } catch (Exception e) {
                fatal("Exception: " + e.getMessage(), e);
            }      	
        }
        info("  Banner Student Enrollment Load Complete");
	}
	
	private void elementSection(Element studentElement, Student student) throws Exception{
		String elementName = "section";
		
        for ( Iterator<?> it = studentElement.elementIterator(); it.hasNext(); ) {
            Element classElement = (Element) it.next();
            Integer crn = getRequiredIntegerAttribute(classElement, "crn", elementName);
            Vector<Class_> clazzes = classes.get(crn);
            if (clazzes != null){
            	for(Iterator<?> cIt = clazzes.iterator(); cIt.hasNext();){
            		Class_ clazz = (Class_) cIt.next();
					StudentClassEnrollment sce = new StudentClassEnrollment();
			    	sce.setStudent(student);
			    	sce.setClazz(clazz);
			    	sce.setCourseOffering(courseOfferings.get(crn));
			    	sce.setTimestamp(new java.util.Date());
			    	student.addToclassEnrollments(sce);
            	}
            }
        }
	}

	Student fetchStudent(String externalId, Long sessionId) {
		return (Student) this.
		getHibSession().
		createQuery("select distinct a from Student as a where a.externalUniqueId=:externalId and a.session.uniqueId=:sessionId").
		setLong("sessionId", sessionId.longValue()).
		setString("externalId", externalId).
		setCacheable(true).
		uniqueResult();
	}
	
	private void loadClasses(Long sessionId) throws Exception {
		HashMap<Long, CourseOffering> baseCourseOfferings = new HashMap<Long, CourseOffering>();
		for (Iterator<?> it = CourseOffering.findAll(sessionId).iterator(); it.hasNext();){
			CourseOffering co = (CourseOffering) it.next();
			baseCourseOfferings.put(co.getUniqueId(), co);
		}
 		for (Iterator<?> it = BannerSection.findAll(sessionId).iterator(); it.hasNext();) {
			BannerSection bs = (BannerSection) it.next();
			if (bs.getCrn() != null && bs.getClasses(getHibSession()) != null && !bs.getClasses(getHibSession()).isEmpty()) {
				Vector<Class_> cls = new Vector<Class_>();
				for (Iterator<?> cIt = bs.getClasses(getHibSession()).iterator(); cIt.hasNext();) {
					Class_ c = (Class_) cIt.next();
					cls.add(c);
				}
				classes.put(bs.getCrn(), cls);
				courseOfferings.put(bs.getCrn(), baseCourseOfferings.get(bs.getBannerConfig().getBannerCourse().getCourseOfferingId()));
			} 
		}
	}

}
