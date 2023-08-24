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
package org.unitime.banner.api;

import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.springframework.stereotype.Service;
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.onlinesectioning.BannerStudentUpdates;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.api.ApiConnector;
import org.unitime.timetable.api.ApiHelper;
import org.unitime.timetable.api.XmlApiHelper;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.shared.ReservationInterface;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.dao.StudentDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Tomas Muller
 */
@Service("/api/banner-student-update")
public class BannerStudentUpdateConnector extends ApiConnector {
	
	@Override
	protected ApiHelper createHelper(HttpServletRequest request, HttpServletResponse response) {
		return new XmlApiHelper(request, response, sessionContext, getCacheMode());
	}
	
	protected String getStudentId(Student student) {
		String id = student.getExternalUniqueId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected void exportStudent(Element root, String term, String puid) {
		Element studentEl = null;
		Element studentAcadAreaClassEl = null; Set<String> acadAreas = new HashSet<String>();
		Element studentMajorsEl = null; Set<String> majors = new HashSet<String>();
		Element studentMinorsEl = null; Set<String> minors = new HashSet<String>();
		Element studentGroupsEl = null;
		
		Set<String> crns = new TreeSet<String>();
		for (Student student: StudentDAO.getInstance().getSession().createQuery(
				"select s from Student s, BannerSession bs where bs.bannerTermCode = :term and bs.session = s.session and s.externalUniqueId = :puid", Student.class)
				.setParameter("term", term).setParameter("puid", puid).list()) {
			if (studentEl == null) {
				studentEl = root.addElement("student");
				studentEl.addAttribute("externalId", getStudentId(student));
				if (student.getFirstName() != null)
					studentEl.addAttribute("firstName", student.getFirstName());
				if (student.getMiddleName() != null)
					studentEl.addAttribute("middleName", student.getMiddleName());
				if (student.getLastName() != null)
					studentEl.addAttribute("lastName", student.getLastName());
				if (student.getEmail() != null)
					studentEl.addAttribute("email", student.getEmail());
				studentEl.addAttribute("session", term);
			}
			for (StudentAreaClassificationMajor acm: student.getAreaClasfMajors()) {
				if (acadAreas.add(acm.getAcademicArea().getAcademicAreaAbbreviation() + ":" + acm.getAcademicClassification().getCode())) {
					if (studentAcadAreaClassEl == null) studentAcadAreaClassEl = studentEl.addElement("studentAcadAreaClass");
					studentAcadAreaClassEl.addElement("acadAreaClass").addAttribute("academicArea", acm.getAcademicArea().getAcademicAreaAbbreviation()).addAttribute("academicClass", acm.getAcademicClassification().getCode());
				}
				if (majors.add(acm.getAcademicArea().getAcademicAreaAbbreviation() + ":" + acm.getAcademicClassification().getCode() + ":" + acm.getMajor().getCode())) {
					if (studentMajorsEl == null) studentMajorsEl = studentEl.addElement("studentMajors");
					studentMajorsEl.addElement("major")
						.addAttribute("academicArea", acm.getAcademicArea().getAcademicAreaAbbreviation())
						.addAttribute("academicClass", acm.getAcademicClassification().getCode())
						.addAttribute("code", acm.getMajor().getCode());
				}
			}
			for (StudentAreaClassificationMinor acm: student.getAreaClasfMinors()) {
				if (acadAreas.add(acm.getAcademicArea().getAcademicAreaAbbreviation() + ":" + acm.getAcademicClassification().getCode())) {
					if (studentAcadAreaClassEl == null) studentAcadAreaClassEl = studentEl.addElement("studentAcadAreaClass");
					studentAcadAreaClassEl.addElement("acadAreaClass").addAttribute("academicArea", acm.getAcademicArea().getAcademicAreaAbbreviation()).addAttribute("academicClass", acm.getAcademicClassification().getCode());
				}
				if (minors.add(acm.getAcademicArea().getAcademicAreaAbbreviation() + ":" + acm.getAcademicClassification().getCode() + ":" + acm.getMinor().getCode())) {
					if (studentMinorsEl == null) studentMajorsEl = studentEl.addElement("studentMinors");
					studentMinorsEl.addElement("minor")
						.addAttribute("academicArea", acm.getAcademicArea().getAcademicAreaAbbreviation())
						.addAttribute("academicClass", acm.getAcademicClassification().getCode())
						.addAttribute("code", acm.getMinor().getCode());
				}
			}
			for (StudentGroup group: student.getGroups()) {
				if (studentGroupsEl == null) studentGroupsEl = studentEl.addElement("studentGroups");
				Element groupEl = studentGroupsEl.addElement("studentGroup");
				if (group.getExternalUniqueId() != null && !group.getExternalUniqueId().isEmpty())
					groupEl.addAttribute("externalId", group.getExternalUniqueId());
				groupEl.addAttribute("campus", student.getSession().getAcademicInitiative());
				groupEl.addAttribute("abbreviation", group.getGroupAbbreviation());
				groupEl.addAttribute("name", group.getGroupName());
			}
			for (StudentClassEnrollment e: student.getClassEnrollments())
				crns.add(e.getClazz().getExternalId(e.getCourseOffering()));
			
		}
		for (String crn: crns)
			studentEl.addElement("crn").setText(crn);

		Set<OverridePair> overrides = new TreeSet<OverridePair>();
		for (OverrideReservation reservation: StudentDAO.getInstance().getSession().createQuery(
				"select r from OverrideReservation r inner join r.students s, BannerSession bs where bs.bannerTermCode = :term and bs.session = s.session and s.externalUniqueId = :puid",
				OverrideReservation.class).setParameter("term", term).setParameter("puid", puid).list()) {
			if (reservation.getClasses().isEmpty()) {
				CourseOffering course = reservation.getInstructionalOffering().getControllingCourseOffering();
				switch (reservation.getOverrideType()) {
				case AllowOverLimitTimeConflict:
					overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowOverLimit.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
					overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowTimeConflict.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
					break;
				case AllowOverLimitTimeConflictLink:
					overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowTimeConflict.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
					overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowOverLimit.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
					overrides.add(new OverridePair(ReservationInterface.OverrideType.CoReqOverride.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
					break;
				case AllowOverLimitLink:
					overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowOverLimit.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
					overrides.add(new OverridePair(ReservationInterface.OverrideType.CoReqOverride.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
					break;
				case AllowTimeConflictLink:
					overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowTimeConflict.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
					overrides.add(new OverridePair(ReservationInterface.OverrideType.CoReqOverride.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
					break;
				default:
					overrides.add(new OverridePair(reservation.getOverrideType().getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
				}
			} else {
				CourseOffering course = null;
				classes: for (Class_ clazz: reservation.getClasses()) {
					for (CourseOffering co: reservation.getInstructionalOffering().getCourseOfferings()) {
						if (crns.contains(clazz.getExternalId(co))) course = co; break classes;
					}
				}
				if (course == null) course = reservation.getInstructionalOffering().getControllingCourseOffering();
				for (Class_ clazz: reservation.getClasses()) {
					switch (reservation.getOverrideType()) {
					case AllowOverLimitTimeConflict:
						overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowOverLimit.getReference(), clazz.getExternalId(course)));
						overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowTimeConflict.getReference(), clazz.getExternalId(course)));
						break;
					case AllowOverLimitTimeConflictLink:
						overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowTimeConflict.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
						overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowOverLimit.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
						overrides.add(new OverridePair(ReservationInterface.OverrideType.CoReqOverride.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
						break;
					case AllowOverLimitLink:
						overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowOverLimit.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
						overrides.add(new OverridePair(ReservationInterface.OverrideType.CoReqOverride.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
						break;
					case AllowTimeConflictLink:
						overrides.add(new OverridePair(ReservationInterface.OverrideType.AllowTimeConflict.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
						overrides.add(new OverridePair(ReservationInterface.OverrideType.CoReqOverride.getReference(), course.getSubjectAreaAbbv(), course.getCourseNbr()));
						break;
					default:
						overrides.add(new OverridePair(reservation.getOverrideType().getReference(), clazz.getExternalId(course)));
					}
				}
			}
		}
		for (OverridePair p: overrides) {
			Element overrideEl = studentEl.addElement("override");
			overrideEl.addAttribute("type", p.getType().toUpperCase());
			if (p.hasCRN()) {
				overrideEl.addAttribute("crn", p.getCRN());
			} else {
				overrideEl.addAttribute("subject", p.getSubject());
				overrideEl.addAttribute("course", p.getCourse());
			}
		}
	}
	
	@Override
	public void doGet(final ApiHelper helper) throws IOException {
		helper.getSessionContext().checkPermissionAnyAuthority(Right.ApiOnlineStudentScheduliung);
		String term = helper.getRequiredParameter("term");
		Document document = DocumentHelper.createDocument();
        Element root = document.addElement("studentUpdates");
        boolean trimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();
		for (String puid: helper.getParameterValues("puid")) {
			while (trimLeadingZerosFromExternalId && puid.startsWith("0")) puid = puid.substring(1);
			exportStudent(root, term, puid);
		}
		helper.setResponse(document);
	}
	
	@Override
	public void doPost(final ApiHelper helper) throws IOException {
		helper.getSessionContext().checkPermissionAnyAuthority(Right.ApiOnlineStudentScheduliung);
		Document document = helper.getRequest(Document.class);
		if ("true".equalsIgnoreCase(helper.getOptinalParameter("queue", "false"))) {
			QueueIn qi = new QueueIn();
			qi.setPostDate(new Date());
			qi.setStatus(QueueIn.STATUS_READY);
			qi.setDocument(document);
			QueueInDAO.getInstance().getSession().persist(qi);
			QueueInDAO.getInstance().getSession().flush();
		} else {
			try {
				new BannerStudentUpdates().processMessage(document.getRootElement());
			} catch (Exception e) {
				throw new IOException("Failed to process message: " + e.getMessage(), e);
			} finally {
				HibernateUtil.closeCurrentThreadSessions();
			}
		}
	}

	@Override
	protected String getName() {
		return "banner-student-update";
	}
	
	public static class OverridePair implements Comparable<OverridePair>{
		private String iType;
		private String iCRN;
		
		public OverridePair(String type, String crn) {
			iType = type; iCRN = crn;
		}
		public OverridePair(String type, String subject, String course) {
			iType = type; iCRN = subject + "|" + course;
		}
		
		public String getType() { return iType; }
		public boolean hasCRN() { return !iCRN.contains("|"); }
		public String getCRN() { return iCRN; }
		public String getSubject() { return iCRN.split("\\|")[0]; }
		public String getCourse() { return iCRN.split("\\|")[1]; }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof OverridePair)) return false;
			OverridePair p = (OverridePair)o;
			return getType().equals(p.getType()) && getCRN().equals(p.getCRN());
		}
		public int hashCode() {
			return getType().hashCode() ^ getCRN().hashCode();
		}
		@Override
		public int compareTo(OverridePair p) {
			return (getCRN().equals(p.getCRN()) ? getType().compareTo(p.getType()) : getCRN().compareTo(p.getCRN()));
		}
	}

}
