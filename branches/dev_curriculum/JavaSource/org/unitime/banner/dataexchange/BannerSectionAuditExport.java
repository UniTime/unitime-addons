/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.banner.dataexchange;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.dom4j.Document;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.dataexchange.BaseExport;
import org.unitime.timetable.model.Session;


/**
 * @author says
 *
 */
public class BannerSectionAuditExport extends BaseExport {

	/**
	 * 
	 */
	public BannerSectionAuditExport() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.dataexchange.BaseExport#saveXml(org.dom4j.Document, org.unitime.timetable.model.Session, java.util.Properties)
	 */
	@Override
	public void saveXml(Document document, Session session,
			Properties parameters) throws Exception {
		beginTransaction();
		BannerMessage bm = new BannerMessage(session, BannerMessageAction.AUDIT, false, getHibSession(), document);
		addAllBannerSections(bm, BannerMessageAction.AUDIT, session);
		commitTransaction();
		beginTransaction();
		addAllBannerCrossLists(bm, BannerMessageAction.AUDIT, session);
		commitTransaction();
	
	}
	
	private void addAllBannerSections(BannerMessage bannerMessage, BannerMessageAction action, Session session){
		BannerSession s = BannerSession.findBannerSessionForSession(session.getUniqueId(), getHibSession());
		
		
		String subjectQuery = "select distinct sa.subjectAreaAbbreviation from SubjectArea sa, BannerSession bs " +
				"where bs.bannerTermCode = :termCode and sa.session.uniqueId = bs.session.uniqueId" +
				" order by sa.subjectAreaAbbreviation";
		String qs = "select bs from BannerSection bs, CourseOffering co, BannerSession b " +
	     "where bs.session.uniqueId = b.session.uniqueId and b.bannerTermCode = :termCode " +
	     "and bs.bannerConfig.bannerCourse.courseOfferingId = co.uniqueId " +
	     "and co.subjectArea.subjectAreaAbbreviation = :subjectAbbv " +
	     "order by co.subjectArea.subjectAreaAbbreviation, co.courseNbr, co.title";

		Iterator subjectIt = getHibSession().createQuery(subjectQuery).setString("termCode", s.getBannerTermCode()).iterate();
		while (subjectIt.hasNext()){
			String subjectAbbv = (String) subjectIt.next();
			Iterator it = getHibSession().createQuery(qs)
			.setString("termCode", s.getBannerTermCode())
			.setString("subjectAbbv", subjectAbbv)
			.iterate();
			while(it.hasNext()){
				BannerSection bs = (BannerSection) it.next();
				bannerMessage.addBannerSectionToMessage(bs, action, getHibSession());
				getHibSession().evict(bs);
			}
			getHibSession().flush();
			getHibSession().clear();
		}
	}
	
	private void addAllBannerCrossLists(BannerMessage bannerMessage, BannerMessageAction action, Session session){
		BannerSession s = BannerSession.findBannerSessionForSession(session.getUniqueId(), getHibSession());
		String qs = "select bs from BannerSection bs, CourseOffering co, BannerSession b " +
				"where bs.session.uniqueId = b.session.uniqueId and b.bannerTermCode = :termCode " +
				"and bs.bannerConfig.bannerCourse.courseOfferingId = co.uniqueId " +
				"and bs.crossListIdentifier is not null " +
				"order by co.subjectArea.subjectAreaAbbreviation, co.courseNbr, co.title";
		Iterator it = getHibSession().createQuery(qs)
		.setString("termCode", s.getBannerTermCode())
		.iterate();
		HashSet<String> hs = new HashSet<String>();
		while(it.hasNext()){
			BannerSection bs = (BannerSection) it.next();
			if (!hs.contains(bs.getCrossListIdentifier())){
				hs.add(bs.getCrossListIdentifier());
				bannerMessage.addBannerCrossListToMessage(bs, action, getHibSession());
				getHibSession().evict(bs);
			}
		}

	}

}
