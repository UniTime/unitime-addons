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
package org.unitime.colleague.dataexchange;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.colleague.dataexchange.ColleagueMessage.MessageAction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.QueueOut;
import org.unitime.colleague.model.dao.QueueOutDAO;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.CourseOffering;


/**
 * @author says
 *
 */
public class SendColleagueMessage {

	/**
	 * 
	 */
	public SendColleagueMessage() {
		
	}

	
	public static void addSectionsToMessage(List<ColleagueSection> sections, MessageAction messageAction, Session hibSession, ColleagueMessage message){
        if (sections == null || sections.isEmpty()){
        	return;
        }
        ColleagueSection cs = sections.get(0);
        ColleagueSession colleagueSession = ColleagueSession.findColleagueSessionForSession(cs.getSession(), hibSession);
        if (ColleagueSession.shouldSendDataToColleagueForSession(cs.getSession(), hibSession) || (colleagueSession.isStoreDataForColleague().booleanValue() && MessageAction.AUDIT.equals(messageAction))){
			ArrayList<ColleagueSection> controlSections = new ArrayList<ColleagueSection>();
			ArrayList<ColleagueSection> notControlSections = new ArrayList<ColleagueSection>();
			for(ColleagueSection colleagueSection : sections){
				CourseOffering co = colleagueSection.getCourseOffering(hibSession);
				if (co != null && co.isIsControl()){
					controlSections.add(colleagueSection);
				} else {
					notControlSections.add(colleagueSection);
				}
			}
			
			for(ColleagueSection colleagueSection : controlSections){
				message.addSectionToMessage(colleagueSection, messageAction, hibSession);
			}
			for(ColleagueSection colleagueSection : notControlSections){
				message.addSectionToMessage(colleagueSection, messageAction, hibSession);
			}
        }
	}
	public static void sendColleagueMessage(List<ColleagueSection> sections, MessageAction messageAction, Session hibSession){
        if (sections == null || sections.isEmpty()){
        	return;
        }
        ColleagueSection cs = sections.get(0);
        ColleagueSession colleagueSession = ColleagueSession.findColleagueSessionForSession(cs.getSession(), hibSession);
        if (ColleagueSession.shouldSendDataToColleagueForSession(cs.getSession(), hibSession) || (colleagueSession.isStoreDataForColleague().booleanValue() && MessageAction.AUDIT.equals(messageAction))){
	 		Document document = DocumentHelper.createDocument();
	 		ColleagueMessage cm = new ColleagueMessage(cs.getSession(), messageAction, true, hibSession, document);
	 		addSectionsToMessage(sections, messageAction, hibSession, cm);
			writeOutMessage(document);
        }
	}
	public static void sendColleagueMessage(ColleagueSection section, MessageAction messageAction, Session hibSession){
        if (section == null){
    		Debug.info("no colleague section ");
    		return;
        }
        ColleagueSession colleagueSession = ColleagueSession.findColleagueSessionForSession(section.getSession(), hibSession);
        if (ColleagueSession.shouldSendDataToColleagueForSession(section.getSession(), hibSession) || (colleagueSession.isStoreDataForColleague().booleanValue() && MessageAction.AUDIT.equals(messageAction))){
			Document document = DocumentHelper.createDocument();
			ColleagueMessage cm = new ColleagueMessage(section.getSession(), messageAction, true, hibSession, document);
			cm.addSectionToMessage(section, messageAction, hibSession);
			
			writeOutMessage(document);
        }
	}
	
	public static void writeOutMessage(Document document){
		QueueOut outQ = new QueueOut();
		if (document.getRootElement().element("SECTION") == null){
			Debug.info("no message to send = " + document.asXML());
			return;
		}
		String asxml = document.asXML();
		if (asxml.length() > 2000) 
		  Debug.info("message = " + asxml.substring(1,2000) + " ....and more....");
		else
		  Debug.info("message = " + asxml);
		outQ.setXml(document);
		outQ.setStatus(QueueOut.STATUS_POSTED);
		outQ.setPostDate(new Date());
		Session newSession = QueueOutDAO.getInstance().createNewSession();
		Transaction trans = newSession.beginTransaction();
		newSession.save(outQ);
		trans.commit();
		newSession.close();
	}
	
}
