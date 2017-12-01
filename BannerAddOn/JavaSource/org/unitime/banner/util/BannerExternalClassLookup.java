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
package org.unitime.banner.util;

import java.util.List;

import org.unitime.timetable.interfaces.ExternalClassLookupInterface;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.dao.CourseOfferingDAO;

/**
 * @author Tomas Muller
 */
public class BannerExternalClassLookup implements ExternalClassLookupInterface {
	
	@Override
	public CourseOffering findCourseByExternalId(Long sessionId, String externalId) {
		return (CourseOffering) CourseOfferingDAO.getInstance().getSession().createQuery(
					"select distinct co from BannerSection bs, CourseOffering co where " +
					"bs.session.uniqueId = :sessionId and bs.crn = :crn and co.uniqueId = bs.bannerConfig.bannerCourse.courseOfferingId"
				).setLong("sessionId",sessionId).setString("crn", externalId).setCacheable(true).setMaxResults(1).uniqueResult();
	}

	@Override
	public List<Class_> findClassesByExternalId(Long sessionId, String externalId) {
		return (List<Class_>) CourseOfferingDAO.getInstance().getSession().createQuery(
				"select c from BannerSection bs inner join bs.bannerSectionToClasses b2c, Class_ c where " +
				"bs.session.uniqueId = :sessionId and b2c.classId = c.uniqueId and bs.crn = :crn"
			).setLong("sessionId",sessionId).setString("crn", externalId).setCacheable(true).list();
	}
}
