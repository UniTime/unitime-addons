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

package org.unitime.banner.util;

import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.hibernate.Query;
import org.unitime.banner.dataexchange.BannerMessage;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.model.BannerSection;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.InstructionalOfferingComparator;
import org.unitime.timetable.model.dao.SessionDAO;



/**
 * @author says
 *
 */
public class UpdateLinksForAllOfferings {

	/**
	 * @param args
	 * @throws Exception 
	 */
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception {
		Properties properties = new Properties();
		properties.put("connection.url", ApplicationProperties
				.getProperty("connection.url"));
		properties.put("connection.username", ApplicationProperties
				.getProperty("connection.username"));
		properties.put("connection.password", ApplicationProperties
				.getProperty("connection.password"));
		HibernateUtil.configureHibernate(properties);

		Session session = Session.getSessionUsingInitiativeYearTerm(args[0],
				args[1], args[2]);
		
		if (session != null){
		    Document document = DocumentHelper.createDocument();
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			BannerMessage bm = new BannerMessage(session, BannerMessageAction.UPDATE, false, hibSession, document);

			TreeSet<SubjectArea> subjectAreas = new TreeSet<SubjectArea>();
			subjectAreas.addAll((Set<SubjectArea>)session.getSubjectAreas());
			String qs = "select distinct co.instructionalOffering from CourseOffering co where co.instructionalOffering.session.uniqueId = :sessionId and co.subjectArea.uniqueId = :subjectId and co.isControl = true";
			Query query = hibSession.createQuery(qs);
			BannerInstrOffrConfigChangeAction biocca = null;
			for(SubjectArea sa : subjectAreas){
				query.setLong("sessionId", session.getUniqueId().longValue())
					 .setLong("subjectId", sa.getUniqueId().longValue());
				TreeSet<InstructionalOffering> instructionalOfferings = new TreeSet<InstructionalOffering>(new InstructionalOfferingComparator(sa.getUniqueId()));
				instructionalOfferings.addAll((List<InstructionalOffering>)query.list());		
				for (InstructionalOffering io : instructionalOfferings){
					biocca = new BannerInstrOffrConfigChangeAction();
					biocca.updateInstructionalOffering(io, hibSession);
					SendBannerMessage.addBannerSectionsToMessage(BannerSection.findBannerSectionsForInstructionalOffering(io, hibSession), BannerMessageAction.UPDATE, hibSession, bm);
					hibSession.evict(io);
				}	
				hibSession.flush();
				hibSession.clear();
			}
			SendBannerMessage.writeOutMessage(bm.getDocument());
		}             
    }
	
}
