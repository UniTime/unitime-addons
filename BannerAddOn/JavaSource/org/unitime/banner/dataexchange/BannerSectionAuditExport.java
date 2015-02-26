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
