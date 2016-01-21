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

package org.unitime.colleague.queueprocessor;

import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.unitime.colleague.dataexchange.ReceiveColleagueResponseMessage;
import org.unitime.colleague.model.QueueIn;
import org.unitime.colleague.model.QueueOut;
import org.unitime.colleague.model.dao.QueueInDAO;
import org.unitime.colleague.model.dao.QueueOutDAO;
import org.unitime.colleague.queueprocessor.exception.LoggableException;
import org.unitime.colleague.queueprocessor.https.HttpsConnector;
import org.unitime.colleague.queueprocessor.oracle.OracleConnector;
import org.unitime.commons.Debug;

/*
 * based on code contributed by Aaron Tyler and Dagmar Murray
 */
public class QueuedItem extends ColleagueCaller {

	QueueOutDAO qod;
	QueueInDAO qid;

	QueueOut item;
	Document testOutgoingXml;

	public QueuedItem(QueueOut item) {
		this.item = item;
		qod = new QueueOutDAO();
	}

	public QueuedItem(Document testOutgoingXml){
		this.testOutgoingXml = testOutgoingXml;
	}
	
	public void processItem() throws SQLException, Exception {

		try {

			item.setPickupDate(new Date());
			item.setStatus(QueueOut.STATUS_POSTED);
			qod.update(item);

			Document result = null;
			String connectionType = getColleagueSectionInterfaceConnectionType();
			if (CONNECTION_TYPES.ORACLE.toString().equals(connectionType.toUpperCase())) {
				result = callOracleProcess(item.getXml());
			} else if (CONNECTION_TYPES.HTTPS.toString().equals(connectionType.toUpperCase())) {
				result = callHTTPProcess(item.getXml());				
			} else {
				throw (new Exception("Connection Type:  " + connectionType + " not found."));
			}

			QueueIn qi = new QueueIn();
			try {
				qi.setPostDate(new Date());

				QueueInDAO qid = new QueueInDAO();

				qi.setMatchId(item.getUniqueId());
				qi.setStatus(QueueIn.STATUS_POSTED);
				qi.setXml(result);

				qid.save(qi);
				
				// Process in UniTime
				boolean sync = ("TRUE".equalsIgnoreCase(item.getXml().getRootElement().attributeValue("SYNC")));
				ReceiveColleagueResponseMessage.receiveResponseDocument(qi, sync);
				
			} catch (Exception ex) {
				LoggableException le = new LoggableException(ex, qi);
				le.logError();
				throw le;
			}
Debug.info("received response for item");
			item.setProcessDate(new Date());
			item.setStatus(QueueOut.STATUS_PROCESSED);
Debug.info("update item process state, before save");

			qod.update(item);
Debug.info("update item process state, after save");

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

	private Document callHTTPProcess(Document xml)
			throws Exception {

		HttpsConnector https = getHTTPSconnection();
		if (item != null){
			Debug.info("\t" + item.getUniqueId() + ": Sending request to Colleague...");
		}
		if (testOutgoingXml != null){
			Debug.info("\tTesting: Sending request to Colleague...");			
		}
		Document outDoc = https.processUniTimePacket(getColleagueSectionHttpsSite(), 
				getColleagueSectionHttpsUser(), getColleagueSectionHttpsPassword(), getColleagueUseSelfSigned(), xml);
		if (item != null){
		Debug.info("\t" + item.getUniqueId() + ": Response received from Colleague.");
		}
		if (testOutgoingXml != null){
			Debug.info("\tTesting: Response received from Colleague.");			
		}
		
		return outDoc;
	}

	private Document callOracleProcess(Document xml)
			throws ClassNotFoundException, SQLException, IOException,
			DocumentException {

		OracleConnector jdbc = getJDBCconnection();

		Debug.info("\t" + item.getUniqueId() + ": Sending request to Colleague...");
		Clob clob = jdbc.processUnitimePacket(xml);
		Debug.info("\t" + item.getUniqueId() + ": Response received from Colleague.");

		Document outDoc = convertClobToDocument(clob);
		
		jdbc.cleanup();

		return outDoc;

	}
	
	public Document sendTestMessage() throws Exception{
		Document result = null;
		result = callHTTPProcess(testOutgoingXml);
		return(result);
	}

}
