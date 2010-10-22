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

	private static String  bannerHost;
	private static String  bannerDatabase;
	private static String  bannerPort;
	private static String  bannerUser;
	private static String  bannerPassword;


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
		
		OracleConnector jdbc = null;
		try {
			jdbc = new OracleConnector(
					getBannerHost(), 
					getBannerDatabase(),
					getBannerPort(),
					getBannerUser(),
					getBannerPassword());
		} catch (Exception e) {
			Debug.info("*********************************************************************");
			Debug.info("** Error setting up OracleConnector in in callOracleProcess *********");
			Debug.info("*********************************************************************");
			Debug.info(e.getMessage());
			e.printStackTrace();
			Debug.info("*********************************************************************");
		}

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

	public static String getBannerHost() throws Exception{
		if (bannerHost == null){
            bannerHost = ApplicationProperties.getProperty("banner.host");
            if (bannerHost == null || bannerHost.trim().length() == 0){
            	throw new Exception("Missing required custom application property:  'banner.host', this property must be set to the host machine for the banner database.");
            }
		}
		return bannerHost;
	}

	public static String getBannerPort() throws Exception{
		if (bannerPort == null){
			bannerPort = ApplicationProperties.getProperty("banner.port");
            if (bannerPort == null || bannerPort.trim().length() == 0){
            	throw new Exception("Missing required custom application property:  'bannerPort', this property must be set to the port number used to connect to the banner database.");
            }
		}
		return bannerPort;
	}

	public static String getBannerDatabase() throws Exception{
		if (bannerDatabase == null){
			bannerDatabase = ApplicationProperties.getProperty("banner.database");
            if (bannerDatabase == null || bannerDatabase.trim().length() == 0){
            	throw new Exception("Missing required custom application property:  'bannerDatabase', this property must be set to the name of the database that hosts the banner schema.");
            }
		}
		return bannerDatabase;
	}

	public static String getBannerUser() throws Exception{
		if (bannerUser == null){
			bannerUser = ApplicationProperties.getProperty("banner.user");
            if (bannerUser == null || bannerUser.trim().length() == 0){
            	throw new Exception("Missing required custom application property:  'bannerUser', this property must be set to the name of the user used to access the banner schema.");
            }
		}
		return bannerUser;
	}

	public static String getBannerPassword() throws Exception{
		if (bannerPassword == null){
			bannerPassword = ApplicationProperties.getProperty("banner.password");
            if (bannerPassword == null || bannerPassword.trim().length() == 0){
            	throw new Exception("Missing required custom application property:  'bannerPassword', this property must be set to the password of the user used to access the banner schema.");
            }
		}
		return bannerPassword;
	}

	// public void run() {
	//
	// Debug.info("\t" + item.getId() + ": Beginning Request...");
	// this.processItem(item);
	// Debug.info("\t" + item.getId() + ": Finished with Request...");
	//		
	// }
}
