/*
 * UniTime 3.4 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
