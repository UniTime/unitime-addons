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

package org.unitime.banner.queueprocessor;

import java.io.IOException;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.unitime.timetable.ApplicationProperties;


import org.unitime.banner.dataexchange.ReceiveBannerResponseMessage;
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.QueueOut;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.model.dao.QueueOutDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;
import org.unitime.banner.queueprocessor.oracle.OracleConnector;
import org.unitime.banner.queueprocessor.util.ClobTools;
import org.unitime.commons.Debug;

/*
 * based on code contributed by Aaron Tyler and Dagmar Murray
 */
public class QueuedItem {

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

			qod.update(item);

			Document result = callOracleProcess(item.getXml());

			QueueIn qi = new QueueIn();
			try {
				qi.setPostDate(new Date());

				QueueInDAO qid = new QueueInDAO();
				//qid.save(qi);

				qi.setMatchId(item.getUniqueId());
				qi.setStatus(QueueIn.STATUS_POSTED);
				qi.setXml(result);

				qid.save(qi);
				
				// Process in UniTime
				ReceiveBannerResponseMessage.receiveResponseDocument(qi);
				
				//Run the Comparator to insert log data into the ComparatorLogHtml table
				// Removed 6/30 for revision to Banner Response strategy
				//Thread t = new Thread(new Comparator(item, qi));
				//t.start();

			} catch (Exception ex) {
				LoggableException le = new LoggableException(ex, qi);
				le.logError();
				throw le;
			}

			item.setProcessDate(new Date());
			item.setStatus(QueueOut.STATUS_PROCESSED);

			qod.update(item);

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

		Clob clob;
		Document outDoc = null;
		
		OracleConnector jdbc = new OracleConnector(
				ApplicationProperties.getProperty("banner.host"), 
				ApplicationProperties.getProperty("banner.database"),
				ApplicationProperties.getProperty("banner.port"),
				ApplicationProperties.getProperty("banner.user"),
				ApplicationProperties.getProperty("banner.password"));

		Debug.info("\t" + item.getUniqueId()
				+ ": Sending request to Banner...");
		clob = jdbc.processUnitimePacket(xml);
		Debug.info("\t" + item.getUniqueId()
				+ ": Response received from Banner.");

		try {
			outDoc = ClobTools.clobToDocument(clob);
			
		} catch (Exception ex) {
			Debug.info("***************************************");
			Debug.info("** Error in callOracleProcess *********");
			Debug.info("***************************************");
			ex.printStackTrace();
			Debug.info("***************************************");
		}

		jdbc.cleanup();

		return outDoc;

	}

	// public void run() {
	//
	// Debug.info("\t" + item.getId() + ": Beginning Request...");
	// this.processItem(item);
	// Debug.info("\t" + item.getId() + ": Finished with Request...");
	//		
	// }
}
