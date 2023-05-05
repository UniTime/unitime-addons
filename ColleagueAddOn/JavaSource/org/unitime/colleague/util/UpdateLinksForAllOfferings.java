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

package org.unitime.colleague.util;

import java.util.Set;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.hibernate.query.Query;
import org.unitime.colleague.dataexchange.ColleagueMessage;
import org.unitime.colleague.dataexchange.SendColleagueMessage;
import org.unitime.colleague.dataexchange.ColleagueMessage.MessageAction;
import org.unitime.colleague.model.ColleagueSection;
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
		HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

		Session session = Session.getSessionUsingInitiativeYearTerm(args[0],
				args[1], args[2]);
		
		if (session != null){
		    Document document = DocumentHelper.createDocument();
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			ColleagueMessage bm = new ColleagueMessage(session, MessageAction.UPDATE, false, hibSession, document);

			TreeSet<SubjectArea> subjectAreas = new TreeSet<SubjectArea>();
			subjectAreas.addAll((Set<SubjectArea>)session.getSubjectAreas());
			String qs = "select distinct co.instructionalOffering from CourseOffering co where co.instructionalOffering.session.uniqueId = :sessionId and co.subjectArea.uniqueId = :subjectId and co.isControl = true";
			Query<InstructionalOffering> query = hibSession.createQuery(qs, InstructionalOffering.class);
			ColleagueInstrOffrConfigChangeAction biocca = null;
			for(SubjectArea sa : subjectAreas){
				query.setParameter("sessionId", session.getUniqueId().longValue())
					 .setParameter("subjectId", sa.getUniqueId().longValue());
				TreeSet<InstructionalOffering> instructionalOfferings = new TreeSet<InstructionalOffering>(new InstructionalOfferingComparator(sa.getUniqueId()));
				instructionalOfferings.addAll(query.list());		
				for (InstructionalOffering io : instructionalOfferings){
					biocca = new ColleagueInstrOffrConfigChangeAction();
					biocca.updateInstructionalOffering(io, hibSession);
					SendColleagueMessage.addSectionsToMessage(ColleagueSection.findColleagueSectionsForInstructionalOffering(io, hibSession), MessageAction.UPDATE, hibSession, bm);
					hibSession.evict(io);
				}	
				hibSession.flush();
				hibSession.clear();
			}
			SendColleagueMessage.writeOutMessage(bm.getDocument());
		}             
    }
	
}
