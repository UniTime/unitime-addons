package org.unitime.banner.util;

import java.util.ArrayList;

import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.interfaces.AcademicSessionLookup;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;

public class BannerAcademicSessionLookup implements AcademicSessionLookup {

	@SuppressWarnings("unchecked")
	@Override
	public Session findAcademicSession(String campus, String year, String term, org.hibernate.Session hibSession) {
		Session acadSession = Session.getSessionUsingInitiativeYearTerm(campus, year, term, hibSession);
		if (acadSession == null) {
			String query = "from BannerSession bs where bs.session.academicYear = :year and bs.session.academicTerm = :term";
			for (BannerSession bs : (ArrayList<BannerSession>)hibSession.createQuery(query)
					  .setString("year", year)
					  .setString("term", term)
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

	@SuppressWarnings("unchecked")
	@Override
	public SubjectArea findSubjectAreaForCampusYearTerm(String campus, String year, String term, String subjectAreaAbbreviation,
			org.hibernate.Session hibSession) {
		SubjectArea sa = SubjectArea.findUsingInitiativeYearTermSubjectAbbreviation(campus, year, term, subjectAreaAbbreviation, hibSession);
		if (sa == null) {
			String query = "from BannerSession bs where bs.session.academicYear = :year and bs.session.academicTerm = :term";
			for (BannerSession bs : (ArrayList<BannerSession>)hibSession.createQuery(query)
					  .setString("year", year)
					  .setString("term", term)
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
