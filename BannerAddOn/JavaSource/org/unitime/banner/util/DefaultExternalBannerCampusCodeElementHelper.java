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
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.model.Class_;

/**
 * 
 * @author says
 *
 */
public class DefaultExternalBannerCampusCodeElementHelper implements
		ExternalBannerCampusCodeElementHelperInterface {


	@Override
	public String getDefaultCampusCode(BannerSection bannerSection, BannerSession bannerSession,
			Class_ clazz) {
		String prefix = null;
		if (bannerSession.isUseSubjectAreaPrefixAsCampus() != null && bannerSession.isUseSubjectAreaPrefixAsCampus()) {
			String delimiter = (bannerSession.getSubjectAreaPrefixDelimiter() != null && !bannerSession.getSubjectAreaPrefixDelimiter().equals("") ? bannerSession.getSubjectAreaPrefixDelimiter() : " - ");
			if (bannerSection.getBannerConfig() != null 
					&& bannerSection.getBannerConfig().getBannerCourse() != null 
					&& bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(null) != null
				    && bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(null).getSubjectArea() != null 
				    && bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(null).getSubjectArea().getSubjectAreaAbbreviation().indexOf(delimiter) >= 0) {
				prefix = bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(null).getSubjectArea().getSubjectAreaAbbreviation().substring(0, bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(null).getSubjectArea().getSubjectAreaAbbreviation().indexOf(delimiter));				
			}
		}
		if (prefix == null) {
			return(bannerSession.getBannerCampus());
		} else {
			return(prefix);
		}
	}

}
