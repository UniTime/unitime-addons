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

package org.unitime.banner.queueprocessor.util;

import org.hibernate.Query;
import org.unitime.banner.queueprocessor.BannerCaller;
import org.unitime.banner.queueprocessor.oracle.OracleConnector;
import org.unitime.commons.Email;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.dao.SessionDAO;


/*
 * based on code contributed by Dagmar Murray
 */
public class QueueProcessorCheck {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		// UniTime connection
		HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

		// See if there are unprocessed items in the queue
		String qs = 
			"select count(*)" +
			" from QueueOut qo" +
			" where qo.postDate < sysdate - :mins /(24*60)" +
			" and (qo.pickupDate is null or qo.processDate is null)";
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		Query query = hibSession.createQuery(qs);
        int minutes = Integer.parseInt(ApplicationProperties.getProperty("tmtbl.queue.processor.check.minutes","60"));
		query.setInteger("mins", minutes);
		Long ct= (Long) query.uniqueResult();
		if (ct > 0) {
			// See if Banner is up
		
			OracleConnector jdbc = null;
			try {
				jdbc = new OracleConnector(
						BannerCaller.getBannerHost(),
						BannerCaller.getBannerDatabase(),
						BannerCaller.getBannerPort(),
						BannerCaller.getBannerUser(),
						BannerCaller.getBannerPassword());
				mailMessage("UniTime Queue Processor for database " + HibernateUtil.getDatabaseName() + " has " + ct + " unprocessed transactions that are more than "+minutes+" minutes old");
				System.exit(33);
			} catch (Exception e) {
				if (e.getMessage() != null && e.getMessage().contains("Missing required custom application property")){
					mailMessage("UniTime Queue Processor for database " + HibernateUtil.getDatabaseName() + " is not configured to connect to Banner. " + e.getMessage());
					throw(e);
				} else {
				// do nothing - Banner is down; messages are supposed to be queueing
				}
			} finally {
				if (jdbc != null) jdbc.cleanup();
			}
		}
	}
	
	static void mailMessage(String msg){
    	try {
           	Email email = new Email();
           	email.setSubject("UniTime Queue Processor has unprocessed transactions");
           	email.addNotify();
           	email.setText(msg);
           	email.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
