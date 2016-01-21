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

import java.util.Properties;

import org.dom4j.Document;
import org.unitime.colleague.dataexchange.ColleagueMessage.MessageAction;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;


/**
 * @author says
 *
 */
public class ColleagueSectionAuditExport extends BaseCollegueSectionExport {

	/**
	 * 
	 */
	public ColleagueSectionAuditExport() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.dataexchange.BaseExport#saveXml(org.dom4j.Document, org.unitime.timetable.model.Session, java.util.Properties)
	 */
	@Override
	public void saveXml(Document document, Session session,
			Properties parameters) throws Exception {
		Long sessionId = session.getUniqueId();
		beginTransaction();
		Session acadSession = SessionDAO.getInstance().get(sessionId, getHibSession());
		ColleagueMessage cm = new ColleagueMessage(acadSession, MessageAction.AUDIT, false, getHibSession(), document);
		addAllColleagueSections(cm, MessageAction.AUDIT, session);
		commitTransaction();
	
	}
	
	
	
}
