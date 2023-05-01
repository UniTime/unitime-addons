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

import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.interfaces.AcademicSessionLookup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;

public class BannerAcademicSessionLookup implements AcademicSessionLookup {

	@Override
	public Session findAcademicSession(String campus, String year, String term, org.hibernate.Session hibSession) {
		Session acadSession = Session.getSessionUsingInitiativeYearTerm(campus, year, term, hibSession);
		if (acadSession == null) {
			String query = "from BannerSession bs where bs.session.academicYear = :year and bs.session.academicTerm = :term";
			for (BannerSession bs : hibSession.createQuery(query, BannerSession.class)
					  .setParameter("year", year)
					  .setParameter("term", term)
					  .setCacheable(true)
					  .list()) {
				if (bs.getUseSubjectAreaPrefixAsCampus()) {
					for (SubjectArea sa : bs.getSession().getSubjectAreas()) {
						if (sa.getSubjectAreaAbbreviation().substring(0, campus.length()).equals(campus)) {
							acadSession = bs.getSession();
							break;
						}
					}
				}
			}
		}
		return acadSession;
	}

	@Override
	public Session findAcademicSession(String campus, String year, String term) {
		return findAcademicSession(campus, year, term, SessionDAO.getInstance().getSession());
	}

	@Override
	public SubjectArea findSubjectAreaForCampusYearTerm(String campus, String year, String term, String subjectAreaAbbreviation,
			org.hibernate.Session hibSession) {
		SubjectArea sa = SubjectArea.findUsingInitiativeYearTermSubjectAbbreviation(campus, year, term, subjectAreaAbbreviation, hibSession);
		if (sa == null) {
			String query = "from BannerSession bs where bs.session.academicYear = :year and bs.session.academicTerm = :term";
			for (BannerSession bs : hibSession.createQuery(query, BannerSession.class)
					  .setParameter("year", year)
					  .setParameter("term", term)
					  .setCacheable(true)
					  .list()) {
				if (bs.getUseSubjectAreaPrefixAsCampus()) {
					for (SubjectArea subj : bs.getSession().getSubjectAreas()) {
						if (subj.getSubjectAreaAbbreviation().equals(campus + bs.getSubjectAreaPrefixDelimiter() + subjectAreaAbbreviation)) {
							sa = subj;
							break;
						}
					}
				}
			}
		}
		return sa;
	}

	@Override
	public SubjectArea findSubjectAreaForCampusYearTerm(String campus, String year, String term, String subjectAreaAbbreviation) {
		return findSubjectAreaForCampusYearTerm(campus, year, term, subjectAreaAbbreviation, SubjectAreaDAO.getInstance().getSession());
	}

}
