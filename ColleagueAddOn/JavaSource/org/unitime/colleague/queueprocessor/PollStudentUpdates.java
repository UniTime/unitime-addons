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
import org.dom4j.Element;
import org.unitime.colleague.model.Queue;
import org.unitime.colleague.model.QueueIn;
import org.unitime.colleague.model.QueueOut;
import org.unitime.colleague.model.dao.QueueInDAO;
import org.unitime.colleague.model.dao.QueueOutDAO;
import org.unitime.colleague.onlinesectioning.ColleagueStudentUpdates;
import org.unitime.colleague.queueprocessor.exception.LoggableException;
import org.unitime.colleague.queueprocessor.https.HttpsConnector;
import org.unitime.colleague.queueprocessor.oracle.OracleConnector;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
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

			qi.setMatchId(null);
			qi.setStatus(QueueIn.STATUS_READY);
			qi.setDocument(result);

			QueueInDAO.getInstance().getSession().persist(qi);
			QueueInDAO.getInstance().getSession().flush();
			
			if (!"true".equalsIgnoreCase(getColleagueStudentInterfaceProcessInStudentSectioningSolverServer())){
				Element rootElement = qi.getDocument().getRootElement();
				if (rootElement.getName().equalsIgnoreCase("studentUpdates")){
					try {
						ColleagueStudentUpdates csu = new ColleagueStudentUpdates();
						csu.loadXml(rootElement);
						qi.setProcessDate(new Date());
						qi.setStatus(Queue.STATUS_PROCESSED);
						QueueInDAO.getInstance().getSession().merge(qi);
						QueueInDAO.getInstance().getSession().flush();
						csu.removeOldStudentUpdateMessages(qi.getUniqueId(), qi.getDocument().getRootElement(), QueueInDAO.getInstance().getSession());
					} catch (Exception e) {
						LoggableException le = new LoggableException(e, qi);
						le.logError();
						throw le;
					}
				} 
			}
		} catch (Exception ex) {
			LoggableException le = new LoggableException(ex, qi);
			le.logError();
			throw le;
		}

	}
	
	private void markQueueOutAsProcessed(QueueOut qo){
		if (qo != null) {
			qo.setProcessDate(new Date());
			qo.setStatus(QueueOut.STATUS_PROCESSED);
			QueueOutDAO.getInstance().getSession().merge(qo);
			QueueOutDAO.getInstance().getSession().flush();
		}

	}
	
	public void poll() {
		if (!pollForStudentUpdates) return;
		try {
			
			QueueOut qo = null;
			if (studentUpdateRequests) {
				qo = QueueOut.findFirstByStatus(QueueOut.STATUS_READY);
				if (qo != null) {
					qo.setPickupDate(new Date());
					qo.setStatus(QueueOut.STATUS_PICKED_UP);
					QueueOutDAO.getInstance().getSession().merge(qo);
					QueueOutDAO.getInstance().getSession().flush();
				}
			}

			Document result = null;
			String connectionType = getColleagueStudentInterfaceConnectionType();
			if (CONNECTION_TYPES.ORACLE.toString().equals(connectionType.toUpperCase())) {
				result = callOracleProcess(qo == null ? null : qo.getDocument());
				markQueueOutAsProcessed(qo);
				processResult(result);
				
			} else if (CONNECTION_TYPES.HTTPS.toString().equals(connectionType.toUpperCase())) {
				result = callHTTPProcess(qo == null ? null : qo.getDocument());				
				markQueueOutAsProcessed(qo);
				processResult(result);
			} else if (CONNECTION_TYPES.FILE.toString().equals(connectionType.toUpperCase())) {
				ArrayList<File> resultFiles = callFileProcess(qo == null ? null : qo.getDocument());				
				markQueueOutAsProcessed(qo);
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
		} finally {
			 HibernateUtil.closeCurrentThreadSessions();
		}
	}
	
	private ArrayList<File> callFileProcess(Document document) throws Exception {
		if (document != null){
			documentToFile(createNewFileOfType(FILE_TYPES.STUDENT), document);
		}
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
