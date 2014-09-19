/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.banner.onlinesectioning;

import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerSessionDAO;
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
		return subjectArea;
	}

	@Override
	public String getExternalCourseNumber(AcademicSessionInfo session, String subjectArea, String courseNumber) {
		return courseNumber.length() > 5 ? courseNumber.substring(0, 5) : courseNumber;
	}

}
