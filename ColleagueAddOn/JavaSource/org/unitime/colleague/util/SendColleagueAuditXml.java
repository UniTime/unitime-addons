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

import java.util.Properties;

import org.cpsolver.ifs.util.ToolBox;
import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.Transaction;
import org.unitime.colleague.dataexchange.ColleagueSectionAuditExport;
import org.unitime.colleague.dataexchange.SendColleagueMessage;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.dao.ColleagueResponseDAO;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;


/*
 * based on code contributed by Dagmar Murray
 */
public class SendColleagueAuditXml {

	/**
	 * @param args
	 *            Initiative Year Term Sync=TRUE|FALSE
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 4){
			Debug.error("The following arguments are required:");
			Debug.error("	<Academic Initiative> <Year> <Term> <Syncronize>");
			Debug.error("");
			Debug.error("		Academic Initiative - The value set as the academic initiative for the session.");
			Debug.error("		Year - The value set as the academic year for the session.");
			Debug.error("		Term - The value set as the academic term for the session.");
			Debug.error("		Syncronize - Either TRUE or FALSE.  ");
			Debug.error("    		If the audit should send a message to syncronize colleague to match the values in UniTime set this to TRUE.");
			Debug.error("");
			throw(new Exception("Missing arguments."));			
		}
		
		ToolBox.configureLogging();

		HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

		ColleagueSectionAuditExport csea = new ColleagueSectionAuditExport();
		Session session = Session.getSessionUsingInitiativeYearTerm(args[0],
				args[1], args[2]);

		if (session == null){
			Debug.error("Session not found for:");
			Debug.error("    Academic Initiative = " + (args[0] == null?"<no value>":args[0]));
			Debug.error("    Year = " + (args[1] == null?"<no value>":args[1]));
			Debug.error("    Term = " + (args[2] == null?"<no value>":args[2]));
			throw(new Exception("ERROR:  No Session Found."));
		}
		if (!"TRUE".equalsIgnoreCase(args[3]) && !"FALSE".equalsIgnoreCase(args[3])){
			Debug.error("Invalid value for Syncronize parameter:  " + args[3]);
			Debug.error("   valid values are:  TRUE, FALSE");
			throw(new Exception("ERROR:  Invalid value for Syncronize."));
		}
		Document document = csea.saveXml(session, new Properties());
		Element root = document.getRootElement();
		root.addAttribute("SYNC", args[3].toUpperCase());
		
		// Delete existing AUDIT messages for term
	    Transaction tx = null;
        org.hibernate.Session hibSession = new ColleagueResponseDAO().getSession(); 
		tx = hibSession.beginTransaction();
		ColleagueSession bs = ColleagueSession.findColleagueSessionForSession(session.getUniqueId(), hibSession);
		String termCode = bs.getColleagueTermCode();
		String hqlDelete = "delete ColleagueResponse br where br.termCode = :termCode and br.action = 'AUDIT'";
		int deletedCount = hibSession.createMutationQuery(hqlDelete).setParameter("termCode",termCode).executeUpdate();
		Debug.info(deletedCount + " previous AUDIT messages deleted for " + termCode);
		tx.commit();
		hibSession.close();
	
		
		SendColleagueMessage.writeOutMessage(document);

	}
}
