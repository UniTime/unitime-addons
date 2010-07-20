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

package org.unitime.banner.queueprocessor.util;

import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

import org.hibernate.Query;
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
		Properties properties = new Properties();
		properties.put("connection.url", ApplicationProperties
				.getProperty("connection.url"));
		properties.put("connection.username", ApplicationProperties
				.getProperty("connection.username"));
		properties.put("connection.password", ApplicationProperties
				.getProperty("connection.password"));
		HibernateUtil.configureHibernate(properties);

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
		
			try {
				OracleConnector jdbc = new OracleConnector(
						ApplicationProperties.getProperty("banner.host"), 
						ApplicationProperties.getProperty("banner.database"),
						ApplicationProperties.getProperty("banner.port"),
						ApplicationProperties.getProperty("banner.user"),
						ApplicationProperties.getProperty("banner.password"));
				mailMessage("UniTime Queue Processor for database " + HibernateUtil.getDatabaseName() + " has " + ct + " unprocessed transactions that are more than "+minutes+" minutes old");
				System.exit(33);
			} catch (Exception e) {
				// do nothing - Banner is down; messages are supposed to be queueing
			}
		}
	}
	
	static void mailMessage(String msg){
       	Email email = new Email();
       	
       	String subject = "UniTime Queue Processor has unprocessed transactions";
    	
    	try {
			email.sendMail(
					(String)ApplicationProperties.getProperty("tmtbl.smtp.host"), 
					(String)ApplicationProperties.getProperty("tmtbl.smtp.domain"), 
					(String)ApplicationProperties.getProperty("tmtbl.local.support.email", (String)ApplicationProperties.getProperty("tmtbl.inquiry.sender")), 
					(String)ApplicationProperties.getProperty("tmtbl.local.support.email", (String)ApplicationProperties.getProperty("tmtbl.inquiry.sender")), 
					(String)ApplicationProperties.getProperty("tmtbl.local.support.email", (String)ApplicationProperties.getProperty("tmtbl.inquiry.email")), 
					subject, 
					msg, 
					new Vector<Object>());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
