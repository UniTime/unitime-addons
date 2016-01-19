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

import java.io.File;
import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.unitime.colleague.model.QueueIn;
import org.unitime.colleague.model.QueueOut;
import org.unitime.colleague.model.dao.QueueInDAO;
import org.unitime.colleague.model.dao.QueueOutDAO;
import org.unitime.colleague.queueprocessor.exception.LoggableException;
import org.unitime.colleague.queueprocessor.https.HttpsConnector;
import org.unitime.colleague.queueprocessor.oracle.OracleConnector;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;

public class PollStudentUpdates extends ColleagueCaller {

	QueueInDAO qid;


	private boolean pollForStudentUpdates = false;
	private boolean studentUpdateRequests = false;


	public PollStudentUpdates() {
		super();
		pollForStudentUpdates = "true".equalsIgnoreCase(ApplicationProperties.getProperty("colleague.studentUpdates.pollColleagueForUpdates", "false"));
		studentUpdateRequests = "true".equalsIgnoreCase(ApplicationProperties.getProperty("colleague.studentUpdateRequests.enabled", "false"));
	}
	
	private void processResultFiles(ArrayList<File> resultFiles) throws LoggableException {
		for (File file : resultFiles){
			Document doc = documentFromFile(file);
			processResult(doc);
		}
	}

	private void processResult(Document result) throws LoggableException{
		// Skip null and empty messages
		if (result == null || result.getRootElement().elements().isEmpty()) return;

		QueueIn qi = new QueueIn();
		try {
			qi.setPostDate(new Date());

			QueueInDAO qid = new QueueInDAO();

			qi.setMatchId(null);
			qi.setStatus(QueueIn.STATUS_READY);
			qi.setXml(result);

			qid.save(qi);
		} catch (Exception ex) {
			LoggableException le = new LoggableException(ex, qi);
			le.logError();
			throw le;
		}

	}
	
	private void markQueueOutAsProcessed(QueueOutDAO qod, QueueOut qo){
		if (qo != null) {
			qo.setProcessDate(new Date());
			qo.setStatus(QueueOut.STATUS_PROCESSED);
			qod.update(qo);
		}

	}
	
	public void poll() {
		if (!pollForStudentUpdates) return;
		try {
			
			QueueOutDAO qod = QueueOutDAO.getInstance();
			QueueOut qo = null;
			if (studentUpdateRequests) {
				qo = qod.findFirstByStatus(QueueOut.STATUS_READY);
				if (qo != null) {
					qo.setPickupDate(new Date());
					qo.setStatus(QueueOut.STATUS_PICKED_UP);
					qod.update(qo);
				}
			}

			Document result = null;
			String connectionType = getColleagueSectionInterfaceConnectionType();
			if (CONNECTION_TYPES.ORACLE.toString().equals(connectionType.toUpperCase())) {
				result = callOracleProcess(qo == null ? null : qo.getXml());
				markQueueOutAsProcessed(qod, qo);
				processResult(result);
				
			} else if (CONNECTION_TYPES.HTTPS.toString().equals(connectionType.toUpperCase())) {
				result = callHTTPProcess(qo == null ? null : qo.getXml());				
				markQueueOutAsProcessed(qod, qo);
				processResult(result);
			} else if (CONNECTION_TYPES.FILE.toString().equals(connectionType.toUpperCase())) {
				ArrayList<File> resultFiles = callFileProcess(qo == null ? null : qo.getXml());				
				markQueueOutAsProcessed(qod, qo);
				processResultFiles(resultFiles);
			} else {
				throw (new Exception("Connection Type:  " + connectionType + " not found."));
			}

			
			

		} catch(SQLException sqlEx) {
			LoggableException le = new LoggableException(sqlEx);
			le.logError();
		} catch (Exception ex) {			
			LoggableException le = new LoggableException(ex);
			le.logError();
		}

	}
	
	private ArrayList<File> callFileProcess(Document document) throws Exception {
		documentToFile(createNewFileOfType(FILE_TYPES.STUDENT), document);
		return(filesToProcess(getColleagueStudentInterfaceConnectionFileDirectory(), getColleagueStudentInterfaceConnectionIncomingFileBaseFilename()));
	}

	private Document callHTTPProcess(Document xml) throws Exception {

		HttpsConnector https = getHTTPSconnection();
		Document outDoc = https.processUniTimePacket(getColleagueStudentHttpsSite(), 
				getColleagueStudentHttpsUser(), getColleagueStudentHttpsPassword(), getColleagueUseSelfSigned(), xml);
		
		return outDoc;
	}


	private Document callOracleProcess(Document request)
			throws ClassNotFoundException, SQLException, IOException,
			DocumentException {

		OracleConnector jdbc = getJDBCconnection();
		
		Debug.info("\tSending student update request to Colleague...");
		Clob clob = jdbc.requestEnrollmentChanges(request);
		Debug.info("\tResponse received from Colleague.");

		Document outDoc = convertClobToDocument(clob);

		jdbc.cleanup();

		return outDoc;

	}
}
