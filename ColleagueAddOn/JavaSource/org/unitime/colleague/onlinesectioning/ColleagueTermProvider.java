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
package org.unitime.colleague.onlinesectioning;

import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.dao.ColleagueSessionDAO;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;

/**
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ColleagueTermProvider implements ExternalTermProvider {

	@Override
	public String getExternalTerm(AcademicSessionInfo session) {
		org.hibernate.Session hibSession = ColleagueSessionDAO.getInstance().createNewSession();
		try {
			ColleagueSession cs = ColleagueSession.findColleagueSessionForSession(session.getUniqueId(), hibSession);
			if (cs != null) {
				return cs.getColleagueTermCode();
			}
			
			return session.getTerm();
		} finally {
			hibSession.close();
		}
	}
	
	@Override
	public String getExternalCampus(AcademicSessionInfo session) {
		org.hibernate.Session hibSession = ColleagueSessionDAO.getInstance().createNewSession();
		try {
			ColleagueSession cs = ColleagueSession.findColleagueSessionForSession(session.getUniqueId(), hibSession);
			if (cs != null) {
				return cs.getColleagueCampus();
			}
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
		if (courseNumber.contains("-")){
			return(courseNumber.substring(0, courseNumber.indexOf('-')));
		}
		return(courseNumber);
	}

}
