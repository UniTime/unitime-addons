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

	protected void addColleagueSections(ColleagueMessage message, MessageAction action,
			ColleagueSession colleagueSession, boolean isControl){
		
		String subjectQuery = "select distinct sa.subjectAreaAbbreviation from SubjectArea sa, ColleagueSession cs " +
				"where cs.colleagueTermCode = :termCode and sa.session.uniqueId = cs.session.uniqueId" +
				" order by sa.subjectAreaAbbreviation";
		StringBuilder sb = new StringBuilder();
		sb.append("select cs from ColleagueSection cs, CourseOffering co, ColleagueSession b ")
		  .append("where cs.session.uniqueId = b.session.uniqueId and b.colleagueTermCode = :termCode ")
		  .append("and cs.courseOfferingId = co.uniqueId ");
		if (isControl){
			sb.append("and co.isControl = true ");
		} else {
			sb.append("and co.isControl = false ");			
		}
		sb.append("and (cs.deleted = false or (cs.deleted = true and cs.sectionIndex is not null)) ")
		  .append("and co.subjectArea.subjectAreaAbbreviation = :subjectAbbv ")
		  .append("order by co.subjectArea.subjectAreaAbbreviation, co.courseNbr, co.title");
		String qs = sb.toString();
	
		Iterator<String> subjectIt = getHibSession().createQuery(subjectQuery, String.class).setParameter("termCode", colleagueSession.getColleagueTermCode()).list().iterator();
		ArrayList<String> subjectAreaAbbreviations = new ArrayList<String>();
		while (subjectIt.hasNext()){
			subjectAreaAbbreviations.add((String) subjectIt.next());
		}
		for(String subjectAbbv : subjectAreaAbbreviations) {
			Iterator<ColleagueSection> it = getHibSession().createQuery(qs, ColleagueSection.class)
			.setParameter("termCode", colleagueSession.getColleagueTermCode())
			.setParameter("subjectAbbv", subjectAbbv)
			.list().iterator();
			while(it.hasNext()){
				ColleagueSection cs = it.next();
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
	protected void addAllColleagueSections(ColleagueMessage message, MessageAction action,
			Session session) {
		ColleagueSession colleagueSession = ColleagueSession.findColleagueSessionForSession(session.getUniqueId(), getHibSession());
		
		addColleagueSections(message, action, colleagueSession, true);
		addColleagueSections(message, action, colleagueSession, false);
		
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