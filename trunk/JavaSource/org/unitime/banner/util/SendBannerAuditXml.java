/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/

package org.unitime.banner.util;

import java.util.Properties;

import net.sf.cpsolver.ifs.util.ToolBox;

import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.Transaction;
import org.unitime.banner.dataexchange.BannerSectionAuditExport;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerResponseDAO;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;


/*
 * based on code contributed by Dagmar Murray
 */
public class SendBannerAuditXml {

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
			Debug.error("    		If the audit should send a message to syncronize banner to match the values in UniTime set this to TRUE.");
			Debug.error("");
			throw(new Exception("Missing arguments."));			
		}
		
		ToolBox.configureLogging();

		HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

		BannerSectionAuditExport bsea = new BannerSectionAuditExport();
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
		Document document = bsea.saveXml(session, new Properties());
		Element root = document.getRootElement();
		root.addAttribute("SYNC", args[3].toUpperCase());
		
		// Delete existing AUDIT messages for term
	    Transaction tx = null;
        org.hibernate.Session hibSession = new BannerResponseDAO().getSession(); 
		tx = hibSession.beginTransaction();
		BannerSession bs = BannerSession.findBannerSessionForSession(session.getUniqueId(), hibSession);
		String termCode = bs.getBannerTermCode();
		String hqlDelete = "delete BannerResponse br where br.termCode = :termCode and br.action = 'AUDIT'";
		int deletedCount = hibSession.createQuery(hqlDelete).setString("termCode",termCode).executeUpdate();
		Debug.info(deletedCount + " previous AUDIT messages deleted for " + termCode);
		tx.commit();
		hibSession.close();
	
		
		SendBannerMessage.writeOutMessage(document);

	}
}
