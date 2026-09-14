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

import org.unitime.banner.interfaces.ExternalBannerCampusCodeElementHelperInterface;
import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerCampusOverrideDAO;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.SubjectArea;

/**
 * 
 * @author says
 *
 */
public class DefaultExternalBannerCampusCodeElementHelper implements ExternalBannerCampusCodeElementHelperInterface {
	@Override
	public String getDefaultCampusCode(BannerSection bannerSection, BannerSession bannerSession, Class_ clazz) {
		// take default campus from Banner session
		String defaultCampus = bannerSession.getBannerCampus();
		// if subject area prefixes are used, take default campus from there
		if (Boolean.TRUE.equals(bannerSession.isUseSubjectAreaPrefixAsCampus())) {
			String delimiter = bannerSession.getSubjectAreaPrefixDelimiter();
			if (delimiter == null || delimiter.isEmpty())
				delimiter = " - "; // use default delimiter when not set or empty on the Banner session
			BannerConfig bannerConfig = bannerSection.getBannerConfig();
			BannerCourse bannerCourse = (bannerConfig == null ? null : bannerConfig.getBannerCourse());
			CourseOffering course = (bannerCourse == null ? null : bannerCourse.getCourseOffering(null));
			SubjectArea subject = (course == null ? null : course.getSubjectArea());
			int idx = (subject == null ? null : subject.getSubjectAreaAbbreviation().indexOf(delimiter));
			if (idx >= 0)
				defaultCampus = subject.getSubjectAreaAbbreviation().substring(0, idx);
		}
		// check Banner campus overrides with matching first/last term that are enabled to be used for default calculation
		for (BannerCampusOverride override: BannerCampusOverrideDAO.getInstance().getSession().createQuery(
				"from BannerCampusOverride where usedDefaultCalc = true and " +
				"(firstBannerTerm is null or firstBannerTerm <= :term) and " +
				"(lastBannerTerm is null or :term <= lastBannerTerm) " +
				"order by order", BannerCampusOverride.class)
				.setParameter("term", bannerSession.getBannerTermCode()).setCacheable(true).list()) {
			if (!override.matchAcademicInitiative(bannerSession.getSession().getAcademicInitiative()))
				continue; // no match on the academic initiative
			if (!override.matchManagingDeptCode(clazz.getManagingDept().getDeptCode()))
				continue; // no match on the department code
			if (!override.matchCampusCode(defaultCampus))
				continue; // no match on the default campus code (from banner session or subject area prefix)
			// return first matching record
			return override.getBannerCampusCode();
		}
		return defaultCampus;
	}
}
