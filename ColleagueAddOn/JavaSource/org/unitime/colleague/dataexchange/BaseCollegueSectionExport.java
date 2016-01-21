package org.unitime.colleague.dataexchange;

import java.util.ArrayList;
import java.util.Iterator;

import org.unitime.colleague.dataexchange.ColleagueMessage.MessageAction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.timetable.dataexchange.BaseExport;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao._RootDAO;

public abstract class BaseCollegueSectionExport extends BaseExport {

	public BaseCollegueSectionExport() {
		super();
	}

	protected void addAllColleagueSections(ColleagueMessage message, MessageAction action,
			Session session) {
				ColleagueSession s = ColleagueSession.findColleagueSessionForSession(session.getUniqueId(), getHibSession());
				
				
				String subjectQuery = "select distinct sa.subjectAreaAbbreviation from SubjectArea sa, ColleagueSession cs " +
						"where cs.colleagueTermCode = :termCode and sa.session.uniqueId = cs.session.uniqueId" +
						" order by sa.subjectAreaAbbreviation";
				String qs = "select cs from ColleagueSection cs, CourseOffering co, ColleagueSession b " +
			     "where cs.session.uniqueId = b.session.uniqueId and b.colleagueTermCode = :termCode " +
			     "and cs.courseOfferingId = co.uniqueId " +
			     "and (cs.deleted = false or (cs.deleted = true and cs.sectionIndex is not null))" +
			     "and co.subjectArea.subjectAreaAbbreviation = :subjectAbbv " +
			     "order by co.subjectArea.subjectAreaAbbreviation, co.courseNbr, co.title";
			
				Iterator subjectIt = getHibSession().createQuery(subjectQuery).setString("termCode", s.getColleagueTermCode()).iterate();
				ArrayList<String> subjectAreaAbbreviations = new ArrayList<String>();
				while (subjectIt.hasNext()){
					subjectAreaAbbreviations.add((String) subjectIt.next());
				}
				for(String subjectAbbv : subjectAreaAbbreviations) {
					Iterator it = getHibSession().createQuery(qs)
					.setString("termCode", s.getColleagueTermCode())
					.setString("subjectAbbv", subjectAbbv)
					.iterate();
					while(it.hasNext()){
						ColleagueSection cs = (ColleagueSection) it.next();
						ColleagueSession colleagueSessionForColleagueSection = ColleagueSession.findColleagueSessionForSession(cs.getSession().getUniqueId(), getHibSession());
						if (colleagueSessionForColleagueSection.isSendDataToColleague()){
							message.addSectionToMessage(cs, action, getHibSession());
						}
						getHibSession().evict(cs);
					}
					getHibSession().flush();
					getHibSession().clear();
				}
			}

	@SuppressWarnings("rawtypes")
	@Override
    public boolean beginTransaction() {
        try {
            iHibSession = new _RootDAO().createNewSession();
            iTx = iHibSession.beginTransaction();
            debug("Transaction started.");
            return true;
        } catch (Exception e) {
            fatal("Unable to begin transaction, reason: "+e.getMessage(),e);
            return false;
        }
    }

}