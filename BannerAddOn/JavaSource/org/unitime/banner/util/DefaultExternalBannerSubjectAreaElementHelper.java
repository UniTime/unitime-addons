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

import org.unitime.banner.interfaces.ExternalBannerSubjectAreaElementHelperInterface;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.SubjectArea;

/**
 * 
 * @author says
 *
 */
public class DefaultExternalBannerSubjectAreaElementHelper implements
ExternalBannerSubjectAreaElementHelperInterface {


	@Override
	public String getBannerSubjectAreaAbbreviation(BannerSection bannerSection, BannerSession bannerSession,
			Class_ clazz) {
		String subj = null;
		if (bannerSession.isUseSubjectAreaPrefixAsCampus() != null && bannerSession.isUseSubjectAreaPrefixAsCampus()) {
			String delimiter = ((bannerSession.getSubjectAreaPrefixDelimiter() != null && !bannerSession.getSubjectAreaPrefixDelimiter().equals("")) ? bannerSession.getSubjectAreaPrefixDelimiter() : " - ");
			if (bannerSection.getBannerConfig() != null 
					&& bannerSection.getBannerConfig().getBannerCourse() != null 
					&& bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(null) != null
				    && bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(null).getSubjectArea() != null 
				    && bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(null).getSubjectArea().getSubjectAreaAbbreviation().indexOf(delimiter) >= 0) {
				subj = getBannerSubjectAreaAbbreviation(bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(null).getSubjectArea(), bannerSession);			
			}
		}
		if (subj == null) {
			return (bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(null).getSubjectAreaAbbv());
		} else {
			return(subj);
		}
	}

	@Override
	public String getBannerSubjectAreaAbbreviation(SubjectArea subjectArea, BannerSession bannerSession) {
		String subj = null;
		if (bannerSession.isUseSubjectAreaPrefixAsCampus() != null && bannerSession.isUseSubjectAreaPrefixAsCampus()) {
			String delimiter = ((bannerSession.getSubjectAreaPrefixDelimiter() != null && !bannerSession.getSubjectAreaPrefixDelimiter().equals("")) ? bannerSession.getSubjectAreaPrefixDelimiter() : " - ");
			if (subjectArea.getSubjectAreaAbbreviation().indexOf(delimiter) >= 0) {
				subj = subjectArea.getSubjectAreaAbbreviation().substring(subjectArea.getSubjectAreaAbbreviation().indexOf(delimiter) + delimiter.length());				
			}
		}
		if (subj == null) {
			return (subjectArea.getSubjectAreaAbbreviation());
		} else {
			return(subj);
		}
	}

}
