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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Query;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.server.DayCode;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface;
import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.ListCourseOfferings;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XInstructor;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XRoom;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XSubpart;

/**
 * @author Tomas Muller
 */
public class BannerListCourseOfferings extends ListCourseOfferings {
	private static final long serialVersionUID = 1L;

	@Override
	protected List<CourseAssignment> listCourses(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<CourseAssignment> ret = customCourseLookup(server, helper);
		if (ret != null && !ret.isEmpty()) return ret;

		ret = new ArrayList<CourseAssignment>();
		
		Map<Long, CourseAssignment> courses = null;
		if (iQuery != null && iQuery.length() >= 3) {
			Pattern pattern = Pattern.compile(ApplicationProperties.getProperty("banner.list-courses.pattern", "(([A-Za-z]{2,4}) ([0-9]{3,5}[A-Za-z]{0,5}) )?(\\d{3,5})(\\-\\w{0,3})?"));
			Matcher matcher = pattern.matcher(iQuery);
			if (matcher.matches()) {
				Query query = null;
				if (matcher.group(1) == null) {
					query = helper.getHibSession().createQuery(
							"select bc.courseOfferingId, bsc.classId " +
							"from BannerSection bs inner join bs.bannerSectionToClasses bsc inner join bs.bannerConfig.bannerCourse bc, CourseOffering co " +
							"where bs.crn like :crn || '%' and co.uniqueId = bc.courseOfferingId and co.subjectArea.session.uniqueId = :sessionId " +
							"order by co.subjectArea.subjectAreaAbbreviation, co.courseNbr, bs.crn"
						).setInteger("crn", Integer.parseInt(matcher.group(4)));					
				} else {
					query = helper.getHibSession().createQuery(
							"select bc.courseOfferingId, bsc.classId " +
							"from BannerSection bs inner join bs.bannerSectionToClasses bsc inner join bs.bannerConfig.bannerCourse bc, CourseOffering co " +
							"where bs.crn like :crn || '%' and lower(co.subjectArea.subjectAreaAbbreviation) = :subject and lower(co.courseNbr) like :course || '%' and " +
							"co.uniqueId = bc.courseOfferingId and co.subjectArea.session.uniqueId = :sessionId " +
							"order by co.subjectArea.subjectAreaAbbreviation, co.courseNbr, bs.crn"
						).setInteger("crn", Integer.parseInt(matcher.group(4))).setString("subject", matcher.group(2).toLowerCase()).setString("course", matcher.group(3).toLowerCase());
				}
				for (Object[] courseClassId: (List<Object[]>)query.setLong("sessionId", server.getAcademicSession().getUniqueId()).setCacheable(true).list()) {
					Long courseId = (Long)courseClassId[0];
					Long sectionId = (Long)courseClassId[1];
					XCourse course = server.getCourse(courseId);
					if (course != null && (iMatcher == null || iMatcher.match(course))) {
						XOffering offering = server.getOffering(course.getOfferingId());
						XSection section = (offering == null ? null : offering.getSection(sectionId));
						if (section != null) {
							XSection parent = (section.getParentId() == null ? null : offering.getSection(section.getParentId()));
							if (parent != null && parent.getName(courseId).equals(section.getName(courseId)))
								continue;
							if (courses == null) courses = new HashMap<Long, CourseAssignment>();
							CourseAssignment ca = courses.get(courseId);
							if (ca == null) {
								ca = convert(course, server);
								courses.put(courseId, ca);
								ret.add(ca);
							}
							ClassAssignmentInterface.ClassAssignment a = ca.addClassAssignment();
							a.setClassId(section.getSectionId());
							XSubpart subpart = offering.getSubpart(section.getSubpartId());
							a.setSubpart(subpart.getName());
							a.setSection(section.getName(courseId));
							a.setExternalId(section.getExternalId(courseId));
							a.setClassNumber(section.getName(-1l));
							a.setCancelled(section.isCancelled());
							a.addNote(course.getNote());
							a.addNote(section.getNote());
							a.setCredit(subpart.getCredit(courseId));
							Float creditOverride = section.getCreditOverride(courseId);
							if (creditOverride != null) a.setCredit(FixedCreditUnitConfig.formatCredit(creditOverride));
							if (section.getTime() != null) {
								for (DayCode d: DayCode.toDayCodes(section.getTime().getDays()))
									a.addDay(d.getIndex());
								a.setStart(section.getTime().getSlot());
								a.setLength(section.getTime().getLength());
								a.setBreakTime(section.getTime().getBreakTime());
								a.setDatePattern(section.getTime().getDatePatternName());
							}
							if (section.getRooms() != null) {
								for (XRoom rm: section.getRooms()) {
									a.addRoom(rm.getUniqueId(), rm.getName());
								}
							}
							for (XInstructor instructor: section.getInstructors()) {
								a.addInstructor(instructor.getName());
								a.addInstructoEmail(instructor.getEmail() == null ? "" : instructor.getEmail());
							}
							if (section.getParentId() != null)
								a.setParentSection(offering.getSection(section.getParentId()).getName(courseId));
							a.setSubpartId(subpart.getSubpartId());
							if (a.getParentSection() == null)
								a.setParentSection(course.getConsentLabel());
						}
					}
					if (iLimit != null && iLimit > 0 && ret.size() == iLimit) break;
				}
			}
		}
		
		for (XCourseId id: server.findCourses(iQuery, iLimit, iMatcher)) {
			if (courses != null && courses.containsKey(id.getCourseId())) continue;
			XCourse course = server.getCourse(id.getCourseId());
			if (course != null)
				ret.add(convert(course, server));
			if (iLimit != null && iLimit > 0 && ret.size() == iLimit) break;
		}
		
		return ret;
	}
	
	public static void main(String[] args) {
		String[] queries = new String[] { "", "123", "1234", "12345", "123456", "12345-012", "12345-XXX",  "ENGL 10600", "ENGL 106 12345", "ENGL 10600 12345", "ENGL 10600X 12345-002" };
		Pattern pattern = Pattern.compile("(([A-Za-z]{2,4}) ([0-9]{3,5}[A-Za-z]{0,5}) )?(\\d{3,5})(\\-\\w{0,3})?");
		for (String q: queries) {
			Matcher m = pattern.matcher(q);
			if (m.matches()) {
				if (m.group(1) != null)
					System.out.println("Subject: " + m.group(2) + ", Course: " + m.group(3) + ", CRN:" + m.group(4) +
							(m.group(5) == null ? "" : ", Section: " + m.group(5).substring(1)) + "     --- " + m.group(0));
				else
					System.out.println("CRN:" + m.group(4) + (m.group(5) == null ? "" : ", Section: " + m.group(5).substring(1)) + "     --- " + m.group(0));
			} else {
				System.out.println("NO MATCH     --- " + q);
			}
		}
	}
}
