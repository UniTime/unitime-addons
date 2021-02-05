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

import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;

/**
 * @author Tomas Muller
 */
public class BannerTermProvider implements ExternalTermProvider {
	@Override
	public String getExternalTerm(AcademicSessionInfo session) {
		org.hibernate.Session hibSession = BannerSessionDAO.getInstance().createNewSession();
		try {
			BannerSession bs = BannerSession.findBannerSessionForSession(session.getUniqueId(), hibSession);
			if (bs != null) return bs.getBannerTermCode();
			if (session.getTerm().toLowerCase().startsWith("spr")) return session.getYear() + "20";
			if (session.getTerm().toLowerCase().startsWith("sum")) return session.getYear() + "30";
			if (session.getTerm().toLowerCase().startsWith("fal"))
				return String.valueOf(Integer.parseInt(session.getYear()) + 1) + "10";
			return session.getYear() + session.getTerm().toLowerCase();
		} finally {
			hibSession.close();
		}
	}
	
	@Override
	public String getExternalCampus(AcademicSessionInfo session) {
		org.hibernate.Session hibSession = BannerSessionDAO.getInstance().createNewSession();
		try {
			BannerSession bs = BannerSession.findBannerSessionForSession(session.getUniqueId(), hibSession);
			if (bs != null) return bs.getBannerCampus();
			return session.getCampus();
		} finally {
			hibSession.close();
		}
	}
	
	@Override
	public String getExternalSubject(AcademicSessionInfo session, String subjectArea, String courseNumber) {
		BannerSession bannerSession = BannerSession.findBannerSessionForSession(session.getUniqueId(), null);
		if (bannerSession != null && bannerSession.isUseSubjectAreaPrefixAsCampus() != null && bannerSession.isUseSubjectAreaPrefixAsCampus()){
			return BannerSection.getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(subjectArea, bannerSession);
		}
		else	
			return subjectArea;
	}

	@Override
	public String getExternalCourseNumber(AcademicSessionInfo session, String subjectArea, String courseNumber) {
		return courseNumber.length() > getCourseNumberLength() ? courseNumber.substring(0, getCourseNumberLength()) : courseNumber;
	}
	
	private Integer iCourseNumberLength = null;
	public int getCourseNumberLength() {
		if (iCourseNumberLength == null) {
			iCourseNumberLength = Integer.valueOf(ApplicationProperties.getProperty("tmtbl.banner.courseNumberLength", "5"));
		}
		return iCourseNumberLength;
	}

}
