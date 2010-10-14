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

package org.unitime.banner.queueprocessor.oracle;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dom4j.Document;
import org.dom4j.DocumentException;


import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.queueprocessor.util.ClobTools;
import org.unitime.commons.Debug;
/*
 * based on code contributed by Aaron Tyler and Dagmar Murray
 */
public class OracleConnector {

	private String driver = "oracle.jdbc.driver.OracleDriver";
	private String url = "jdbc:oracle:thin:@";
	private Connection conn = null;

	public OracleConnector(String host, String db, String port, String user,
			String password) throws ClassNotFoundException, SQLException {

		// construct the url
		url = url + host + ":" + port + ":" + db;

		// load the Oracle driver and establish a connection
		try {
			Class.forName(driver);
			conn = DriverManager.getConnection(url, user, password);
		} catch (ClassNotFoundException ex) {
			Debug.info("Failed to find driver class: " + driver);
			throw ex;
		} catch (SQLException ex) {
			Debug.info("Failed to establish a connection to: " + url);
			throw ex;
		}
	}

	public Clob processUnitimePacket(Document in_clob) throws SQLException,
			IOException {

		String oracleQuery = "begin sz_unitime.p_process_packet(in_packet => ?,out_response => ?,out_sync => ?); end;";

		CallableStatement stmt = conn.prepareCall(oracleQuery);

		try {
		stmt.setClob(1, ClobTools.documentToCLOB(in_clob, conn));
		} catch(Exception ex) {
			Debug.info("***************************************");
			Debug.info("** Error in documentToCLOB (probably) *");
			Debug.info("***************************************");
			ex.printStackTrace();
			Debug.info("***************************************");
		}
		stmt.registerOutParameter(2, java.sql.Types.CLOB);
		stmt.registerOutParameter(3, java.sql.Types.CLOB);

		stmt.execute();

		Clob out_clob = stmt.getClob(2);
		Clob out_sync_clob = stmt.getClob(3);

		stmt.close();

		if(out_sync_clob != null) {
			//Put the "Sync" XML into the IntegrationQueueOut table
	        try {
		        SendBannerMessage.writeOutMessage(ClobTools.clobToDocument(out_sync_clob));
			} catch (DocumentException e) {
				Debug.info("***************************************");
				Debug.info("** Error in SendBannerMessage: sending sync CLOB *");
				Debug.info("***************************************");
				e.printStackTrace();
				Debug.info("***************************************");
			}
 		}
		
		return out_clob;

	}

	public void cleanup() throws SQLException {

		if (conn != null)
			conn.close();
	}
}
