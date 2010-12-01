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

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

import org.dom4j.Element;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.Queue;
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.BaseImport;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicAreaClassification;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;

/**
 * @author says
 *
 */
public class BannerStudentDataUpdate extends BaseImport {

	private static String rootName = "studentUpdates";
	private static String studentElementName = "student";
	private static String enrollmentElementName = "enrollment";
	
	private HashMap<Session,HashSet<Long>> studentIdsSucessfullyProcessed = new HashMap<Session, HashSet<Long>>();
	private HashSet<String> studentIdsNotProcessed = new HashSet<String>();
	private HashSet<String> studentIdsSucessfullyProcessedWithProblems = new HashSet<String>();
	
	private HashSet<Long> classIdsTouched = new HashSet<Long>();
	private HashSet<Long> courseIdsTouched = new HashSet<Long>();
	
	private boolean trimLeadingZerosFromExternalId;
	private HashMap<String, Vector<Long>> bannerSessionIdMap = new HashMap<String, Vector<Long>>();

	/**
	 * 
	 */
	public BannerStudentDataUpdate() {
		super();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		Date start = new Date();
		Date elementStart;
		Date elementEnd;
		long maxElementTime = Long.MIN_VALUE;
		long minElementTime = Long.MAX_VALUE;
		long elementCount = 0;
		long timeDiff;
//		TreeMap<Long, Long> timeMap = new TreeMap<Long, Long>();
		trimLeadingZerosFromExternalId = "true".equals(ApplicationProperties.getProperty("tmtbl.data.exchange.trim.externalId","false"));

		if (rootElement.getName().equalsIgnoreCase(rootName)) {
			for (Iterator<?> eIt = rootElement.elementIterator(studentElementName); eIt.hasNext();) {
				elementStart = new Date();
				Element studentElement = (Element) eIt.next();
				processStudentElement(studentElement);
				elementEnd = new Date();
				timeDiff = elementEnd.getTime() - elementStart.getTime();
				if (timeDiff < minElementTime){
					minElementTime = timeDiff;
				}
				if (timeDiff > maxElementTime){
					maxElementTime = timeDiff;
				}
//				Long timeCount = timeMap.get(new Long(timeDiff));
//				if (timeCount == null){
//					timeCount = new Long(0);
//				}
//				timeMap.put(new Long(timeDiff), new Long(timeCount.longValue() + 1));
				elementCount++;
			}
			beginTransaction();
			info("Updating class enrollments.");
			updateEnrollmentForClassIds();
			info("Updating course offering enrollments.");
			updateCourseOfferingEnrollmentCounts();
			info("Done updating enrollments.");
			commitTransaction();
			beginTransaction();
			int studentCount = 0;
			for(Session session : studentIdsSucessfullyProcessed.keySet()){
				HashSet<Long> updatedStudents = studentIdsSucessfullyProcessed.get(session);
		        if (!updatedStudents.isEmpty()){
	 	 	        StudentSectioningQueue.studentChanged(getHibSession(), session.getUniqueId(), updatedStudents);
	 	 	        studentCount += updatedStudents.size();
		        }
			}
			commitTransaction();
			Date end = new Date();
			
			info(Integer.toString(studentCount) + " student records updated in " + (end.getTime() - start.getTime())+ " milliseconds.");
			info(Integer.toString(studentIdsNotProcessed.size()) + " student records failed to update.");
			info(Integer.toString(studentIdsSucessfullyProcessedWithProblems.size()) + " student records were updated, but had problems.");
			info("Minimum milliseconds required to process a record = " + minElementTime);
			info("Maximum milliseconds required to process a record = " + maxElementTime);
			info("Average milliseconds required to process a record = " + ((end.getTime() - start.getTime())/elementCount));
//			info("The distribution of time to process each record is as follows:");
//			for(Long elapsedTime : timeMap.keySet()){
//				info("	" + elapsedTime.toString() + ":  " + timeMap.get(elapsedTime).toString());
//			}
			if (!studentIdsNotProcessed.isEmpty()){
				error("The following student ids were not successfully processed:  ");
				for(String studentId : studentIdsNotProcessed){
					error("    " + studentId);
				}
			}
			if (!studentIdsSucessfullyProcessedWithProblems.isEmpty()){
				error("The following student ids were successfully processed, but may have had problems finding all classes the student was enrolled in:  ");
				for(String studentId : studentIdsSucessfullyProcessedWithProblems){
					error("    " + studentId);
				}
			}
		}
	}


	private void processStudentElement(Element studentElement) {
		String externalId = null;
		HashMap<Session, HashSet<Long>> studentUpdates = null;
		try {
			externalId = getRequiredStringAttribute(studentElement, "externalId", studentElementName);
            while (trimLeadingZerosFromExternalId && externalId.startsWith("0")) externalId = externalId.substring(1);	
		} catch (Exception e) {
			error(e.getMessage());
			return;
		}
		if (externalId.trim().length() == 0){
			error("For element '" + studentElementName + "' an 'externalId' is required");
		}

		String bannerSession = null;
		try {
			bannerSession = getRequiredStringAttribute(studentElement, "session", studentElementName);
		} catch (Exception e) {
			error(e.getMessage());
			studentIdsNotProcessed.add(externalId);
			return;
		}
		beginTransaction();
		HashSet<Student> studentRecords = new HashSet<Student>();
		for(Long sessionId : getSessionIdsForBannerSession(bannerSession)){
			Student s = Student.findByExternalId(sessionId, externalId);
			if (s != null){
				studentRecords.add(s);
			}
		}
		String firstName = null;
		String lastName = null;
		String academicArea = null;
		String classification = null;
		String major = null;
		try {
			firstName = getRequiredStringAttribute(studentElement, "firstName", studentElementName);
			lastName = getRequiredStringAttribute(studentElement, "lastName", studentElementName);
			academicArea = getRequiredStringAttribute(studentElement, "academicArea", studentElementName);
			classification = getRequiredStringAttribute(studentElement, "classification", studentElementName);
			major = getRequiredStringAttribute(studentElement, "major", studentElementName);			
		} catch (Exception e) {
			error(e.getMessage());
			studentIdsNotProcessed.add(externalId);
			rollbackTransaction();
			return;
		}
		String email = getOptionalStringAttribute(studentElement, "email");
		String middleName = getOptionalStringAttribute(studentElement, "middleName");
		
		HashMap<Session, HashMap<CourseOffering, Vector<Class_>>> classEnrollments = null;
		try {
			classEnrollments = processEnrollmentElements(studentElement, bannerSession, externalId);			
		} catch (Exception e) {
			studentIdsNotProcessed.add(externalId);
			error(e.getMessage());
			rollbackTransaction();
			return;
		}
		try {
			studentUpdates = updateStudent(studentRecords, classEnrollments, externalId, firstName, middleName, lastName, academicArea, classification, major, email);			
		} catch (Exception e) {
			studentIdsNotProcessed.add(externalId);
			error(e.getMessage());
			rollbackTransaction();
			return;
		}
		commitTransaction();
		mergeSessionRecordLists(studentIdsSucessfullyProcessed, studentUpdates);
	}

	private void addSessionRecordToList(HashMap<Session, HashSet<Long>> processedList, Session session, Long uniqueId){
		HashSet<Long> ids = processedList.get(session);
		if (ids == null){
			ids = new HashSet<Long>();
			processedList.put(session, ids);
		}
		ids.add(uniqueId);
	}
	private void mergeSessionRecordLists(HashMap<Session, HashSet<Long>> globalList, HashMap<Session, HashSet<Long>> studentList){
		for(Session session : studentList.keySet()){
			HashSet<Long> ids = studentList.get(session);
			HashSet<Long> globalIds = globalList.get(session);
			if (globalIds == null){
				globalIds = new HashSet<Long>();
				globalList.put(session, globalIds);
			}
			globalIds.addAll(ids);
		}
	}
	
	private HashMap<Session, HashSet<Long>> updateStudent(HashSet<Student> studentRecords,
			HashMap<Session, HashMap<CourseOffering,Vector<Class_>>> classEnrollments, 
			String externalId, String firstName,
			String middleName, String lastName, String academicArea,
			String classification, String major, String email) {

		HashMap<Session, HashSet<Long>> recordsProcessed = new HashMap<Session, HashSet<Long>>();
		
		if (classEnrollments.isEmpty()){
			//  If the student has no enrollments remove the student records found
			for(Student student : studentRecords){
				addSessionRecordToList(recordsProcessed, student.getSession(), student.getUniqueId());
				if (student.getClassEnrollments() != null && student.getClassEnrollments().size() > 0){
					for(StudentClassEnrollment sce : student.getClassEnrollments()){
						classIdsTouched.add(sce.getClazz().getUniqueId());
						courseIdsTouched.add(sce.getCourseOffering().getUniqueId());
					}
				}
				getHibSession().delete(student);
			}
		} else {
			// If the student has enrollments update the student's information in each session the student
			//  has enrollments in and remove the student from any session the student no longer has enrollments in
			HashSet<Student> records = new HashSet<Student>();
			records.addAll(studentRecords);
			for(Session session : classEnrollments.keySet()){
				boolean changed = false;
				Student record = null;
				for(Student student : records){
					if (student.getSession().getUniqueId().equals(session.getUniqueId())){
						record = student;
						break;
					}
				}
				if (record == null){
					record = new Student();
					record.setExternalUniqueId(externalId);
					record.setSession(session);
					record.setFreeTimeCategory(0);
		            record.setSchedulePreference(0);
		            record.setClassEnrollments(new HashSet<StudentClassEnrollment>());
		            record.setAcademicAreaClassifications(new HashSet<AcademicAreaClassification>());
		            record.setPosMajors(new HashSet<PosMajor>());
		            changed = true;
				} else {
					records.remove(record);
				}
				if (record.getFirstName() == null || !record.getFirstName().equals(firstName)){
					record.setFirstName(firstName);
					changed = true;
				}
				if (record.getMiddleName() == null || !record.getMiddleName().equals(middleName)){
					record.setMiddleName(middleName);
					changed = true;
				}
				if (record.getLastName() == null || !record.getLastName().equals(lastName)){
					record.setLastName(lastName);
					changed = true;
				}
				if (record.getEmail() == null || !record.getEmail().equals(email)){
					record.setEmail(email);
					changed = true;
				}

				// This makes the assumption that when working with Banner students have only one AcademicAreaClassification
				AcademicAreaClassification aac = null;
				for(Iterator<?> it = record.getAcademicAreaClassifications().iterator(); it.hasNext();){
					aac = (AcademicAreaClassification) it.next();
					break;
				}
				if (aac == null || 
					!((aac.getAcademicArea().getExternalUniqueId().equalsIgnoreCase(academicArea) 
								|| aac.getAcademicArea().getAcademicAreaAbbreviation().equalsIgnoreCase(academicArea)) 
					  && (aac.getAcademicClassification().getExternalUniqueId().equalsIgnoreCase(classification) 
							  	|| aac.getAcademicClassification().getCode().equalsIgnoreCase(classification)))) {
					record.getAcademicAreaClassifications().clear();
					aac = new AcademicAreaClassification();
					AcademicArea aa = AcademicArea.findByExternalId(session.getUniqueId(), academicArea);						
					if (aa == null){
						aa = AcademicArea.findByAbbv(session.getUniqueId(), academicArea);
					}
					if (aa == null){
						aa = new AcademicArea();
						aa.setAcademicAreaAbbreviation(academicArea);
						aa.setSession(session);
						aa.setExternalUniqueId(externalId);
						aa.setLongTitle(academicArea);
						aa.setShortTitle(academicArea);
						aa.setUniqueId((Long)getHibSession().save(aa));
						info("Added Academic Area:  " + academicArea);
					}
					aac.setAcademicArea(aa);
					
					AcademicClassification ac = AcademicClassification.findByExternalId(session.getUniqueId(), classification);
					if (ac == null){
						ac = AcademicClassification.findByCode(session.getUniqueId(), classification);
					}
					if (ac == null){
						ac = new AcademicClassification();
						ac.setCode(classification);
						ac.setExternalUniqueId(classification);
						ac.setName(classification);
						ac.setSession(session);
						ac.setUniqueId((Long) getHibSession().save(ac));
						info("Added Academic Classification:  " + classification);
					}
					aac.setAcademicClassification(ac);
					aac.setStudent(record);
					record.getAcademicAreaClassifications().add(aac);
					changed = true;
				}
				
				//This makes the assumption that when working with Banner students have only one Major
				PosMajor m = null;
				for(Iterator<?> it = record.getPosMajors().iterator(); it.hasNext();){
					m = (PosMajor) it.next();
				}
				if (m == null || !(major.equalsIgnoreCase(m.getExternalUniqueId()) || major.equalsIgnoreCase(m.getCode()))){
					record.getPosMajors().clear();
					
					PosMajor posMajor = PosMajor.findByExternalId(session.getUniqueId(), major);
					if (posMajor == null){
						posMajor = PosMajor.findByCode(session.getUniqueId(), major);
					}
					if (posMajor == null){
						posMajor = new PosMajor();
						posMajor.setCode(major);
						posMajor.setExternalUniqueId(major);
						posMajor.setName(major);
						posMajor.setSession(session);
						posMajor.setUniqueId((Long)getHibSession().save(posMajor));
						info("Added Major:  " + major);
					}
					record.addToposMajors(posMajor);
					changed = true;
				}
				Hashtable<Long, StudentClassEnrollment> enrollments = new Hashtable<Long, StudentClassEnrollment>();
            	if (record.getClassEnrollments() != null){
	            	for (StudentClassEnrollment enrollment: record.getClassEnrollments()) {
	            		enrollments.put(enrollment.getClazz().getUniqueId(), enrollment);
	            	}
            	}
            	HashMap<CourseOffering, Vector<Class_>> courseToClassEnrollments = classEnrollments.get(session);
            	for (CourseOffering co : courseToClassEnrollments.keySet()){
	            	for (Class_ clazz: courseToClassEnrollments.get(co)) {
	            		StudentClassEnrollment enrollment = enrollments.remove(clazz.getUniqueId());
	            		if (enrollment != null) continue; // enrollment already exists
	            		enrollment = new StudentClassEnrollment();
	            		enrollment.setStudent(record);
	            		enrollment.setClazz(clazz);
	            		enrollment.setCourseOffering(co);
	            		enrollment.setTimestamp(new java.util.Date());
	            		record.getClassEnrollments().add(enrollment);    
	            		changed = true;
	            		classIdsTouched.add(clazz.getUniqueId());
	            		courseIdsTouched.add(co.getUniqueId());
	        		}
            	}         	
            	if (!enrollments.isEmpty()) {
            		for (StudentClassEnrollment enrollment: enrollments.values()) {
            			classIdsTouched.add(enrollment.getClazz().getUniqueId());
            			courseIdsTouched.add(enrollment.getCourseOffering().getUniqueId());
            			record.getClassEnrollments().remove(enrollment);
            			getHibSession().delete(enrollment);
            		}
            		changed = true;
            	}
            	Long uid = record.getUniqueId();
            	if (uid == null){
            		uid = (Long) getHibSession().save(record);
            	} else if (changed) {
            		getHibSession().update(record);	
            	}
            	addSessionRecordToList(recordsProcessed, session, uid);
            	
			}
			if (!records.isEmpty()){
				for(Student student : records){
					addSessionRecordToList(recordsProcessed, student.getSession(), student.getUniqueId());
					for(StudentClassEnrollment sce : student.getClassEnrollments()){
						classIdsTouched.add(sce.getClazz().getUniqueId());
						courseIdsTouched.add(sce.getCourseOffering().getUniqueId());
					}
					getHibSession().delete(student);
				}
			}
		}	
		
		return(recordsProcessed);
		
	}

	private HashMap<Session, HashMap<CourseOffering, Vector<Class_>>> processEnrollmentElements(
			Element studentElement, String bannerSession, String externalId) throws Exception {
		HashMap<Session, HashMap<CourseOffering, Vector<Class_>>> enrollments = new HashMap<Session, HashMap<CourseOffering,Vector<Class_>>>();
		for (Iterator<?> eIt = studentElement.elementIterator(enrollmentElementName); eIt.hasNext();) {
			Element enrollmentElement = (Element) eIt.next();
			Integer crn = getRequiredIntegerAttribute(enrollmentElement, "crn", enrollmentElementName);
			CourseOffering co = BannerSection.findCourseOfferingForCrnAndTermCode(crn, bannerSession);
			if (co == null){
				studentIdsSucessfullyProcessedWithProblems.add(externalId);
				error("Course Offering not found for CRN = " + crn.toString() + ", banner session = " + bannerSession);
				continue;
			}
			boolean foundClasses = false;
			for(Iterator<?> it = BannerSection.findAllClassesForCrnAndTermCode(crn, bannerSession).iterator(); it.hasNext();){
				Class_ c = (Class_) it.next();
				foundClasses = true;
				HashMap<CourseOffering,Vector<Class_>> sessionCourseToClasses = enrollments.get(c.getSessionId());
				if (sessionCourseToClasses == null){
					sessionCourseToClasses = new HashMap<CourseOffering, Vector<Class_>>();
					enrollments.put(c.getSession(), sessionCourseToClasses);
				}
				Vector<Class_> classes = sessionCourseToClasses.get(co);
				if (classes == null){
					classes = new Vector<Class_>();
					sessionCourseToClasses.put(co, classes);
				}
				classes.add(c);
			}
			if (!foundClasses){
				studentIdsSucessfullyProcessedWithProblems.add(externalId);
				error("Classes not found for CRN = " + crn.toString() + ", banner session = " + bannerSession);
			}
		}			
		return(enrollments);
	}

	private Vector<Long> getSessionIdsForBannerSession(String bannerSessionId) {
		if (bannerSessionIdMap.get(bannerSessionId) == null){
			Vector<Long> sessionIds = new Vector<Long>();
			bannerSessionIdMap.put(bannerSessionId, sessionIds);
			for (Iterator<?> it = getHibSession().createQuery("from BannerSession bs where bs.bannerTermCode = :termCode)").setString("termCode", bannerSessionId).list().iterator(); it.hasNext();){
				BannerSession bs = (BannerSession) it.next();
				sessionIds.add(bs.getSession().getUniqueId());
			}
		}
		return(bannerSessionIdMap.get(bannerSessionId));
	}

	public static void receiveResponseDocument(QueueIn queueIn) throws LoggableException {
		Element rootElement = queueIn.getXml().getRootElement();
		if (rootElement.getName().equalsIgnoreCase(rootName)){
			try {
				BannerStudentDataUpdate rbrm = new BannerStudentDataUpdate();
				rbrm.loadXml(rootElement);
				queueIn.setProcessDate(new Date());
				queueIn.setStatus(Queue.STATUS_PROCESSED);
				QueueInDAO.getInstance().getSession().update(queueIn);
			} catch (Exception e) {
				LoggableException le = new LoggableException(e, queueIn);
				le.logError();
				throw le;
			}
		} 	
	}

   private void updateEnrollmentForClassIds(){
    	String ids = "";
    	int count = 0;
    	for (Long id: classIdsTouched) {
    		if (count > 0) ids += ",";
    		ids += id;
    		count ++;
    		if (count == 1000) {
    			getHibSession().createQuery("update Class_  c " +
    	        		"set c.enrollment=(select count(distinct d.student) " +
    	                " from StudentClassEnrollment d " +
    	                " where d.clazz.uniqueId =c.uniqueId) " + 
    	                " where c.uniqueId in (" + ids + ")").executeUpdate();
    			ids = ""; count = 0;
    		}
    	}
    	if (count > 0) {
			getHibSession().createQuery("update Class_  c " +
	        		"set c.enrollment=(select count(distinct d.student) " +
	                " from StudentClassEnrollment d " +
	                " where d.clazz.uniqueId =c.uniqueId) " + 
	                " where c.uniqueId in (" + ids + ")").executeUpdate();
    	}
	
    }
   
	private void updateCourseOfferingEnrollmentCounts() {

    	String ids = "";
    	int count = 0;
    	for (Long id: courseIdsTouched) {
    		if (count > 0) ids += ",";
    		ids += id;
    		count ++;
    		if (count == 1000) {
    			getHibSession().createQuery("update CourseOffering  c " +
    	        		"set c.enrollment=(select count(distinct d.student) " +
    	                " from StudentClassEnrollment d " +
    	                " where d.courseOffering.uniqueId =c.uniqueId) " + 
    	                " where c.uniqueId in (" + ids + ")").executeUpdate();
    			ids = ""; count = 0;
    		}
    	}
    	if (count > 0) {
			getHibSession().createQuery("update CourseOffering  c " +
	        		"set c.enrollment=(select count(distinct d.student) " +
	                " from StudentClassEnrollment d " +
	                " where d.courseOffering.uniqueId =c.uniqueId) " + 
	                " where c.uniqueId in (" + ids + ")").executeUpdate();
    	}
	
    		
	}


}
