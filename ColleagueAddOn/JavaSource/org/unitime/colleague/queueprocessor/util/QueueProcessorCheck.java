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

package org.unitime.colleague.queueprocessor.util;

import org.hibernate.query.Query;
import org.unitime.colleague.queueprocessor.ColleagueCaller;
import org.unitime.colleague.queueprocessor.oracle.OracleConnector;
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
			" where qo.postDate < adddate(sysdate, - :mins /(24*60))" +
			" and (qo.pickupDate is null or qo.processDate is null)";
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		Query<Long> query = hibSession.createQuery(qs, Long.class);
        int minutes = Integer.parseInt(ApplicationProperties.getProperty("tmtbl.queue.processor.check.minutes","60"));
        query.setParameter("mins", minutes);
		Long ct= (Long) query.uniqueResult();
		if (ct > 0) {
			// See if Colleague is up
		
			OracleConnector jdbc = null;
			try {
				jdbc = new OracleConnector(
						ColleagueCaller.getColleagueHost(),
						ColleagueCaller.getCollegueDatabase(),
						ColleagueCaller.getColleaguePort(),
						ColleagueCaller.getColleagueUser(),
						ColleagueCaller.getColleaguePassword());
				mailMessage("UniTime Queue Processor for database " + HibernateUtil.getDatabaseName() + " has " + ct + " unprocessed transactions that are more than "+minutes+" minutes old");
				System.exit(33);
			} catch (Exception e) {
				if (e.getMessage() != null && e.getMessage().contains("Missing required custom application property")){
					mailMessage("UniTime Queue Processor for database " + HibernateUtil.getDatabaseName() + " is not configured to connect to Colleague. " + e.getMessage());
					throw(e);
				} else {
				// do nothing - Colleague is down; messages are supposed to be queueing
				}
			} finally {
				if (jdbc != null) jdbc.cleanup();
			}
		}
	}
	
	static void mailMessage(String msg){
    	try {
           	Email email = Email.createEmail();
           	email.setSubject("UniTime Queue Processor has unprocessed transactions");
           	email.addNotify();
           	email.setText(msg);
           	email.send();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
}
