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

package org.unitime.colleague.queueprocessor.oracle;

import java.io.IOException;
import java.sql.CallableStatement;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.unitime.colleague.dataexchange.SendColleagueMessage;
import org.unitime.colleague.queueprocessor.util.ClobTools;
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
		Debug.info("**  Make sure the stored procedure that is to process the messages for colleague exists.  The call to ");
		Debug.info("**  the stored procedure is defined by the property: 'colleague.storedProcedure.call'.");
		Debug.info("**  The required format of the parameters for the Colleague Stored procedure must be: ");
		Debug.info("**");
		Debug.info("**    in_packet => ?,out_response => ?,out_sync => ?");
		Debug.info("**");
		Debug.info("**  The names of the parameters may change but the first parameter must be a CLOB input,");
		Debug.info("**    the second parameter must be a CLOB output, and the third parameter must be a CLOB output");
		Debug.info("**");
		Debug.info("**  An example of how to define a colleague stored procedure call in the properties file is as follows:");
		Debug.info("**");
		Debug.info("**    colleague.storedProcedure.call=begin sz_unitime.p_process_packet(in_packet => ?,out_response => ?,out_sync => ?); end;");
		Debug.info("**");			
		Debug.info("**  If you have the parameters defined correctly then possbile the error is in documentToCLOB ");
		Debug.info("******************************************************************************************************");
		e.printStackTrace();
		Debug.info("******************************************************************************************************");
	
	}

	private void outputStandardStudentUpdateDebugInfo(Exception e){
		Debug.info("******************************************************************************************************");
		Debug.info("**  Make sure the stored procedure that is to request student updates for colleague exists.  The call to ");
		Debug.info("**  the stored procedure is defined by the property: 'colleague.studentUpdates.storedProcedure.call'.");
		Debug.info("**  The required format of the parameters for the Colleague Stored procedure must be: ");
		Debug.info("**");
		Debug.info("**    out_response => ?, in_request => ?");
		Debug.info("**");
		Debug.info("**  The names of the parameters may change but the first parameter must be a CLOB output,");
		Debug.info("**    the second parameter must be a CLOB input");
		Debug.info("**");
		Debug.info("**  An example of how to define a colleague stored procedure call in the properties file is as follows:");
		Debug.info("**");
		Debug.info("**    colleague.studentUpdates.storedProcedure.call=begin sz_unitime.p_request_student_updates(out_response => ?, in_studentList => ?); end;");
		Debug.info("**");			
		Debug.info("**  If you have the parameters defined correctly then possbile the error is in documentToCLOB ");
		Debug.info("******************************************************************************************************");
		e.printStackTrace();
		Debug.info("******************************************************************************************************");
	
	}

	private void outputStandardCrnValidatorDebugInfo(Exception e){
		Debug.info("******************************************************************************************************");
		Debug.info("**  Make sure the stored procedure that is to validate CRNs for colleague exists.  The call to ");
		Debug.info("**  the stored procedure is defined by the property: 'colleague.crnValidator.storedProcedure.call'.");
		Debug.info("**  The required format of the parameters for the Colleague Stored procedure must be: ");
		Debug.info("**");
		Debug.info("**    in_term => ?, in_crn => ?, out_used => ?");
		Debug.info("**");
		Debug.info("**  The names of the parameters may change but the first parameter must be a String input,");
		Debug.info("**    the second parameter must be an Integer input, and the third parameter must be a String ");
		Debug.info("**    output('Y' if used in Colleague or 'N' if not used in Colleague)");
		Debug.info("**");
		Debug.info("**  An example of how to define a Colleague stored procedure call in the properties file is as follows:");
		Debug.info("**");
		Debug.info("**    colleague.crnValidator.storedProcedure.call=begin sz_unitime.p_validate_crn(in_term => ?, in_crn => ?, out_used => ?); end;");
		Debug.info("**");			
		Debug.info("******************************************************************************************************");
		e.printStackTrace();
		Debug.info("******************************************************************************************************");
	
	}
	
	public Clob processUnitimePacket(Document in_clob) throws SQLException,
			IOException {

		CallableStatement stmt = null;
		try {
			stmt = conn.prepareCall(getColleagueStoredProcedureCall());
		} catch (Exception e1) {
			outputStandardDebugInfo(e1);
		}

		//TODO:  if sync message, then look at response to generate update message, if a section has a response then generate an update for it.
		//          if no response then two systems match
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
		        SendColleagueMessage.writeOutMessage(ClobTools.clobToDocument(out_sync_clob));
			} catch (DocumentException e) {
				Debug.info("******************************************************************************************************");
				Debug.info("** Error in SendColleagueMessage: sending sync CLOB *");
				Debug.info("******************************************************************************************************");
				e.printStackTrace();
				Debug.info("******************************************************************************************************");
			}
 		}
		
		return out_clob;

	}

	
	public Clob requestEnrollmentChanges(Document request) throws SQLException,
	IOException {

		CallableStatement stmt = null;
		try {
			stmt = conn.prepareCall(getColleagueStudentUpdatesStoredProcedureCall());
		} catch (Exception e1) {
			outputStandardStudentUpdateDebugInfo(e1);
		}
		
		try {
			stmt.registerOutParameter(1, java.sql.Types.CLOB);
		} catch (Exception e) {
			outputStandardStudentUpdateDebugInfo(e);
		}
		
		try {
			if (stmt.getParameterMetaData().getParameterCount() == 2) {
				stmt.setClob(2, request == null ? null : ClobTools.documentToCLOB(request, conn));
			}
		} catch(Exception ex) {
			outputStandardStudentUpdateDebugInfo(ex);
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

	public String validateCrnWithColleague(String colleagueTermCode, Integer crn) throws SQLException  {

		CallableStatement stmt = null;
		try {
			String crnValidatorStoredProcedureCall = getColleagueCrnValidatorStoredProcedureCall();
			if (crnValidatorStoredProcedureCall == null){
				return("N");
			}
			stmt = conn.prepareCall(crnValidatorStoredProcedureCall);
		} catch (Exception e1) {
			outputStandardCrnValidatorDebugInfo(e1);
		}
		
		try {
			stmt.registerOutParameter(3, java.sql.Types.CLOB);
            stmt.setString(1, colleagueTermCode);
            stmt.setInt(2, crn.intValue());

		} catch (Exception e) {
			outputStandardCrnValidatorDebugInfo(e);
		}
		
		try {
			stmt.execute();			
		} catch (Exception e) {
			outputStandardCrnValidatorDebugInfo(e);
		}
		
		String result = stmt.getString(3);
		
		stmt.close();
				
		return result;
		
	}

	public void cleanup() throws SQLException {

		if (conn != null)
			conn.close();
	}
	
	private static String getColleagueStoredProcedureCall() throws Exception{
		String colleagueStoredProcedureCall  = ApplicationProperties.getProperty("colleague.storedProcedure.call");
        if (colleagueStoredProcedureCall == null || colleagueStoredProcedureCall.trim().length() == 0){
        	colleagueStoredProcedureCall = "begin sz_unitime.p_process_packet(in_packet => ?,out_response => ?,out_sync => ?); end;";
        }
		return colleagueStoredProcedureCall;
	}

	private static String getColleagueStudentUpdatesStoredProcedureCall() throws Exception{
		String colleagueStudentUpdatesStoredProcedureCall = ApplicationProperties.getProperty("colleague.studentUpdates.storedProcedure.call");
        if (colleagueStudentUpdatesStoredProcedureCall == null || colleagueStudentUpdatesStoredProcedureCall.trim().length() == 0){
        	colleagueStudentUpdatesStoredProcedureCall = "begin sz_unitime.p_request_student_updates(out_response => ?); end;";
        }
		return colleagueStudentUpdatesStoredProcedureCall;
	}
	
	private static String getColleagueCrnValidatorStoredProcedureCall() throws Exception{
		String colleagueCrnValidatorStoredProcedureCall = ApplicationProperties.getProperty("colleague.crnValidator.storedProcedure.call");
        if (colleagueCrnValidatorStoredProcedureCall != null && colleagueCrnValidatorStoredProcedureCall.trim().length() == 0){
        	colleagueCrnValidatorStoredProcedureCall = null;
        }
		return colleagueCrnValidatorStoredProcedureCall;
	}

}
