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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.timetable.gwt.shared.ClassAssignmentInterface.CourseAssignment;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.basic.ListCourseOfferings;
import org.unitime.timetable.onlinesectioning.model.XCourse;
import org.unitime.timetable.onlinesectioning.model.XCourseId;

/**
 * @author Tomas Muller
 */
public class BannerListCourseOfferings extends ListCourseOfferings {
	private static final long serialVersionUID = 1L;

	@Override
	protected List<CourseAssignment> listCourses(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		List<CourseAssignment> ret = new ArrayList<CourseAssignment>();
		
		Set<Long> courseIds = null;
		if (iQuery != null && iQuery.length() >= 3) {
			try {
				for (Long courseId: (List<Long>)helper.getHibSession().createQuery(
						"select distinct bc.courseOfferingId " +
						"from BannerSection bs inner join bs.bannerConfig.bannerCourse bc, CourseOffering co " +
						"where bs.crn like :crn || '%' and co.uniqueId = bc.courseOfferingId and co.subjectArea.session.uniqueId = :sessionId "
						).setInteger("crn", Integer.parseInt(iQuery)).setLong("sessionId", server.getAcademicSession().getUniqueId()).setCacheable(true).list()) {
					XCourse course = server.getCourse(courseId);
					if (course != null && (iMatcher == null || iMatcher.match(course))) {
						if (courseIds == null) courseIds = new HashSet<Long>();
						courseIds.add(courseId);
						ret.add(convert(course, server));
					}
					if (iLimit != null && iLimit > 0 && ret.size() == iLimit) break;
				}
			} catch (NumberFormatException e) {}
		}
		
		for (XCourseId id: server.findCourses(iQuery, iLimit, iMatcher)) {
			if (courseIds != null && courseIds.contains(id.getCourseId())) continue;
			XCourse course = server.getCourse(id.getCourseId());
			if (course != null)
				ret.add(convert(course, server));
			if (iLimit != null && iLimit > 0 && ret.size() == iLimit) break;
		}
		
		return ret;
	}
}
