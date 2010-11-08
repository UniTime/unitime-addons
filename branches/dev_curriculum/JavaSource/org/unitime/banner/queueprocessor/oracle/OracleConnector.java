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
import org.unitime.timetable.ApplicationProperties;
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

	private void outputStandardDebugInfo(Exception e){
		Debug.info("******************************************************************************************************");
		Debug.info("**  Make sure the stored procedure that is to process the messages for banner exists.  The call to ");
		Debug.info("**  the stored procedure is defined by the property: 'banner.storedProcedure.call'.");
		Debug.info("**  The required format of the parameters for the Banner Stored procedure must be: ");
		Debug.info("**");
		Debug.info("**    in_packet => ?,out_response => ?,out_sync => ?");
		Debug.info("**");
		Debug.info("**  The names of the parameters may change but the first parameter must be a CLOB input,");
		Debug.info("**    the second parameter must be a CLOB output, and the third parameter must be a CLOB output");
		Debug.info("**");
		Debug.info("**  An example of how to define a banner stored procedure call in the properties file is as follows:");
		Debug.info("**");
		Debug.info("**    banner.storedProcedure.call=begin sz_unitime.p_process_packet(in_packet => ?,out_response => ?,out_sync => ?); end;");
		Debug.info("**");			
		Debug.info("**  If you change the value of the 'banner.storedProcedure.call' the queue processor must be");
		Debug.info("**    restarted to begin using the new procedure call.");
		Debug.info("**");
		Debug.info("**  If you have the parameters defined correctly then possbile he error is in documentToCLOB ");
		Debug.info("******************************************************************************************************");
		e.printStackTrace();
		Debug.info("******************************************************************************************************");
	
	}

	private void outputStandardStudentUpdateDebugInfo(Exception e){
		Debug.info("******************************************************************************************************");
		Debug.info("**  Make sure the stored procedure that is to request student updates for banner exists.  The call to ");
		Debug.info("**  the stored procedure is defined by the property: 'banner.studentUpdates.storedProcedure.call'.");
		Debug.info("**  The required format of the parameters for the Banner Stored procedure must be: ");
		Debug.info("**");
		Debug.info("**    out_response => ?");
		Debug.info("**");
		Debug.info("**  The names of the parameters may change but the first parameter must be a CLOB input,");
		Debug.info("**    the second parameter must be a CLOB output, and the third parameter must be a CLOB output");
		Debug.info("**");
		Debug.info("**  An example of how to define a banner stored procedure call in the properties file is as follows:");
		Debug.info("**");
		Debug.info("**    banner.storedProcedure.call=begin sz_unitime.p_request_student_updates(out_response => ?); end;");
		Debug.info("**");			
		Debug.info("**  If you change the value of the 'banner.studentUpdates.storedProcedure.call' the queue processor must be");
		Debug.info("**    restarted to begin using the new procedure call.");
		Debug.info("**");
		Debug.info("**  If you have the parameters defined correctly then possbile he error is in documentToCLOB ");
		Debug.info("******************************************************************************************************");
		e.printStackTrace();
		Debug.info("******************************************************************************************************");
	
	}

	public Clob processUnitimePacket(Document in_clob) throws SQLException,
			IOException {

		CallableStatement stmt = null;
		try {
			stmt = conn.prepareCall(getBannerStoredProcedureCall());
		} catch (Exception e1) {
			outputStandardDebugInfo(e1);
		}

		try {
		stmt.setClob(1, ClobTools.documentToCLOB(in_clob, conn));
		} catch(Exception ex) {
			outputStandardDebugInfo(ex);
		}
		try {
			stmt.registerOutParameter(2, java.sql.Types.CLOB);
			stmt.registerOutParameter(3, java.sql.Types.CLOB);			
		} catch (Exception e) {
			outputStandardDebugInfo(e);
		}

		try {
			stmt.execute();			
		} catch (Exception e) {
			outputStandardDebugInfo(e);
		}

		Clob out_clob = stmt.getClob(2);
		Clob out_sync_clob = stmt.getClob(3);

		stmt.close();

		if(out_sync_clob != null) {
			//Put the "Sync" XML into the IntegrationQueueOut table
	        try {
		        SendBannerMessage.writeOutMessage(ClobTools.clobToDocument(out_sync_clob));
			} catch (DocumentException e) {
				Debug.info("******************************************************************************************************");
				Debug.info("** Error in SendBannerMessage: sending sync CLOB *");
				Debug.info("******************************************************************************************************");
				e.printStackTrace();
				Debug.info("******************************************************************************************************");
			}
 		}
		
		return out_clob;

	}

	
	public Clob requestEnrollmentChanges() throws SQLException,
	IOException {

		CallableStatement stmt = null;
		try {
			stmt = conn.prepareCall(getBannerStudentUpdatesStoredProcedureCall());
		} catch (Exception e1) {
			outputStandardStudentUpdateDebugInfo(e1);
		}
		
		try {
			stmt.registerOutParameter(1, java.sql.Types.CLOB);
		} catch (Exception e) {
			outputStandardStudentUpdateDebugInfo(e);
		}
		
		try {
			stmt.execute();			
		} catch (Exception e) {
			outputStandardStudentUpdateDebugInfo(e);
		}
		
		Clob out_clob = stmt.getClob(1);
		
		stmt.close();
				
		return out_clob;
		
	}

	public void cleanup() throws SQLException {

		if (conn != null)
			conn.close();
	}
	
	private static String getBannerStoredProcedureCall() throws Exception{
		String bannerStoredProcedureCall  = ApplicationProperties.getProperty("banner.storedProcedure.call");
        if (bannerStoredProcedureCall == null || bannerStoredProcedureCall.trim().length() == 0){
        	bannerStoredProcedureCall = "begin sz_unitime.p_process_packet(in_packet => ?,out_response => ?,out_sync => ?); end;";
        }
		return bannerStoredProcedureCall;
	}

	private static String getBannerStudentUpdatesStoredProcedureCall() throws Exception{
		String bannerStudentUpdatesStoredProcedureCall = ApplicationProperties.getProperty("banner.studentUpdates.storedProcedure.call");
        if (bannerStudentUpdatesStoredProcedureCall == null || bannerStudentUpdatesStoredProcedureCall.trim().length() == 0){
        	bannerStudentUpdatesStoredProcedureCall = "begin sz_unitime.p_request_student_updates(out_response => ?); end;";
        }
		return bannerStudentUpdatesStoredProcedureCall;
	}

}
