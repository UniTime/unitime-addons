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

package org.unitime.banner.queueprocessor;

import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.unitime.banner.dataexchange.BannerStudentDataUpdate;
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;
import org.unitime.banner.queueprocessor.oracle.OracleConnector;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;

public class PollStudentUpdates extends BannerCaller {

	QueueInDAO qid;


	private boolean pollForStudentUpdates = false;


	public PollStudentUpdates() {
		super();
		pollForStudentUpdates = "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.studentUpdates.pollBannerForUpdates", "false"));
	}

	public void poll() {
		if (!pollForStudentUpdates) return;
		try {

			Document result = callOracleProcess();

			QueueIn qi = new QueueIn();
			try {
				qi.setPostDate(new Date());

				QueueInDAO qid = new QueueInDAO();

				qi.setMatchId(null);
				qi.setStatus(QueueIn.STATUS_POSTED);
				qi.setXml(result);

				qid.save(qi);
				
				// Process in UniTime
				BannerStudentDataUpdate.receiveResponseDocument(qi);
				
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

	private Document callOracleProcess()
			throws ClassNotFoundException, SQLException, IOException,
			DocumentException {

		OracleConnector jdbc = getJDBCconnection();

		Debug.info("\tSending student update request to Banner...");
		Clob clob = jdbc.requestEnrollmentChanges();
		Debug.info("\tResponse received from Banner.");

		Document outDoc = convertClobToDocument(clob);

		jdbc.cleanup();

		return outDoc;

	}
}
