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
		ToolBox.configureLogging();

		Properties properties = new Properties();
		properties.put("connection.url", ApplicationProperties
				.getProperty("connection.url"));
		properties.put("connection.username", ApplicationProperties
				.getProperty("connection.username"));
		properties.put("connection.password", ApplicationProperties
				.getProperty("connection.password"));
		HibernateUtil.configureHibernate(properties);

		BannerSectionAuditExport bsea = new BannerSectionAuditExport();
		Session session = Session.getSessionUsingInitiativeYearTerm(args[0],
				args[1], args[2]);

		Document document = bsea.saveXml(session, new Properties());
		Element root = document.getRootElement();
		root.addAttribute("SYNC", args[3]);
		
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
