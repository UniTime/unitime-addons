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

package org.unitime.banner.queueprocessor;

import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.unitime.banner.dataexchange.ReceiveBannerResponseMessage;
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.QueueOut;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.model.dao.QueueOutDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;
import org.unitime.banner.queueprocessor.oracle.OracleConnector;
import org.unitime.commons.Debug;

/*
 * based on code contributed by Aaron Tyler and Dagmar Murray
 */
public class QueuedItem extends BannerCaller {

	QueueOutDAO qod;
	QueueInDAO qid;

	QueueOut item;


	public QueuedItem(QueueOut item) {
		this.item = item;
		qod = new QueueOutDAO();
	}

	public void processItem() throws SQLException, Exception {

		try {

			item.setPickupDate(new Date());
			item.setStatus(QueueOut.STATUS_POSTED);

			qod.getSession().merge(item);
			qod.getSession().flush();

			Document result = callOracleProcess(item.getXml());

			QueueIn qi = new QueueIn();
			try {
				qi.setPostDate(new Date());

				QueueInDAO qid = new QueueInDAO();

				qi.setMatchId(item.getUniqueId());
				qi.setStatus(QueueIn.STATUS_POSTED);
				qi.setXml(result);

				qid.getSession().persist(qi);
				qid.getSession().flush();
				
				// Process in UniTime
				ReceiveBannerResponseMessage.receiveResponseDocument(qi);
				
			} catch (Exception ex) {
				LoggableException le = new LoggableException(ex, qi);
				le.logError();
				throw le;
			}

			item.setProcessDate(new Date());
			item.setStatus(QueueOut.STATUS_PROCESSED);

			qod.getSession().merge(item);
			qod.getSession().flush();
		} catch(SQLException sqlEx) {
			throw sqlEx;				
		} catch (LoggableException le) {
			le.setQueuedItem(item);
			le.logError();
		} catch (Exception ex) {
			
			if(ex.getCause().getClass() == SQLException.class) {
				throw ex;
			}
			
			LoggableException le = new LoggableException(ex, item);
			le.logError();
		}

	}

	private Document callOracleProcess(Document xml)
			throws ClassNotFoundException, SQLException, IOException,
			DocumentException {

		OracleConnector jdbc = getJDBCconnection();

		Debug.info("\t" + item.getUniqueId() + ": Sending request to Banner...");
		Clob clob = jdbc.processUnitimePacket(xml);
		Debug.info("\t" + item.getUniqueId() + ": Response received from Banner.");

		Document outDoc = convertClobToDocument(clob);
		
		jdbc.cleanup();

		return outDoc;

	}

}
