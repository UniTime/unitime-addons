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

			qod.update(item);

			Document result = callOracleProcess(item.getXml());

			QueueIn qi = new QueueIn();
			try {
				qi.setPostDate(new Date());

				QueueInDAO qid = new QueueInDAO();

				qi.setMatchId(item.getUniqueId());
				qi.setStatus(QueueIn.STATUS_POSTED);
				qi.setXml(result);

				qid.save(qi);
				
				// Process in UniTime
				ReceiveBannerResponseMessage.receiveResponseDocument(qi);
				
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

		OracleConnector jdbc = getJDBCconnection();

		Debug.info("\t" + item.getUniqueId() + ": Sending request to Banner...");
		Clob clob = jdbc.processUnitimePacket(xml);
		Debug.info("\t" + item.getUniqueId() + ": Response received from Banner.");

		Document outDoc = convertClobToDocument(clob);
		
		jdbc.cleanup();

		return outDoc;

	}

}
