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
package org.unitime.banner.util;

import java.sql.SQLException;

import org.unitime.banner.queueprocessor.BannerCaller;
import org.unitime.banner.queueprocessor.oracle.OracleConnector;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;


public class BannerCrnValidator extends BannerCaller {

	public String isCrnUsedInBannerForTerm(Integer crn, String bannerTermCode) throws Exception{
		String bannerHost = ApplicationProperties.getProperty("banner.host");
		if ( bannerHost == null || bannerHost.trim().length() == 0){
			return("N");			
		}
		String bannerStoredProcedure = ApplicationProperties.getProperty("banner.crnValidator.storedProcedure.call");
		if ( bannerStoredProcedure == null || bannerStoredProcedure.trim().length() == 0){
			return("N");			
		}
		OracleConnector jdbc = getJDBCconnection();
		if (jdbc == null){
			Debug.info("No Connection to Banner, skipping Banner check for CRN: " + crn.toString() + " for Banner Term " + bannerTermCode);
			return("N");
		}

		Debug.info("Sending check for CRN: " + crn.toString() + " for Banner Term " + bannerTermCode + " to Banner...");
		String result;
		boolean responseReceived = false;
		try {
			result = jdbc.validateCrnWithBanner(bannerTermCode, crn);
			responseReceived = true;
		} catch (SQLException e) {
			Debug.error("Failed to receive response from Banner for CRN: " + crn.toString() + " for Banner Term " + bannerTermCode + ".");
			e.printStackTrace();
			try {
				jdbc.cleanup();
			} catch (SQLException e1) {
				Debug.error("Failed to close connection to Banner after not receiving response from Banner for CRN: " + crn.toString() + " for Banner Term " + bannerTermCode + ".");
				e1.printStackTrace();
			}
			throw new Exception("Failed to receive response from Banner for CRN: " + crn.toString() + " for Banner Term " + bannerTermCode + ".");
		}
		
		if (responseReceived){
		    Debug.info("Response of '" + result + "' received from Banner for CRN: " + crn.toString() + " for Banner Term " + bannerTermCode + ".");
		}
		try {
			jdbc.cleanup();
		} catch (SQLException e) {
			Debug.error("Failed to close connection to Banner");
			e.printStackTrace();
		}
		
		return(result);
	}
}
