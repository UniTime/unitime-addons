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
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.QueueOut;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.model.dao.QueueOutDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;
import org.unitime.banner.queueprocessor.oracle.OracleConnector;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;

public class PollStudentUpdates extends BannerCaller {

	QueueInDAO qid;


	private boolean pollForStudentUpdates = false;
	private boolean studentUpdateRequests = false;


	public PollStudentUpdates() {
		super();
		pollForStudentUpdates = "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.studentUpdates.pollBannerForUpdates", "false"));
		studentUpdateRequests = "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.studentUpdateRequests.enabled", "false"));
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

			Document result = callOracleProcess(qo == null ? null : qo.getDocument());
			
			if (qo != null) {
				qo.setProcessDate(new Date());
				qo.setStatus(QueueOut.STATUS_PROCESSED);
				QueueOutDAO.getInstance().getSession().merge(qo);
				QueueOutDAO.getInstance().getSession().flush();
			}
			
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
			} catch (Exception ex) {
				LoggableException le = new LoggableException(ex, qi);
				le.logError();
				throw le;
			}


		} catch(SQLException sqlEx) {
			LoggableException le = new LoggableException(sqlEx);
			le.logError();
		} catch (Exception ex) {			
			LoggableException le = new LoggableException(ex);
			le.logError();
		}

	}

	private Document callOracleProcess(Document request)
			throws ClassNotFoundException, SQLException, IOException,
			DocumentException {

		OracleConnector jdbc = getJDBCconnection();
		
		Debug.info("\tSending student update request to Banner...");
		Clob clob = jdbc.requestEnrollmentChanges(request);
		Debug.info("\tResponse received from Banner.");

		Document outDoc = convertClobToDocument(clob);

		jdbc.cleanup();

		return outDoc;

	}
}
