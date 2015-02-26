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

import java.sql.Clob;

import org.dom4j.Document;
import org.unitime.banner.queueprocessor.oracle.OracleConnector;
import org.unitime.banner.queueprocessor.util.ClobTools;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author says
 *
 */
public abstract class BannerCaller {

	public BannerCaller() {
		super();
	}

	public static String getBannerHost() throws Exception{
		String bannerHost = ApplicationProperties.getProperty("banner.host");
        if (bannerHost == null || bannerHost.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'banner.host', this property must be set to the host machine for the banner database.");
        }
		return bannerHost;
	}

	public static String getBannerPort() throws Exception{
		String bannerPort = ApplicationProperties.getProperty("banner.port");
        if (bannerPort == null || bannerPort.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'bannerPort', this property must be set to the port number used to connect to the banner database.");
        }
		return bannerPort;
	}

	public static String getBannerDatabase() throws Exception{
		String  bannerDatabase = ApplicationProperties.getProperty("banner.database");
        if (bannerDatabase == null || bannerDatabase.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'bannerDatabase', this property must be set to the name of the database that hosts the banner schema.");
        }
		return bannerDatabase;
	}

	public static String getBannerUser() throws Exception{
		String bannerUser = ApplicationProperties.getProperty("banner.user");
        if (bannerUser == null || bannerUser.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'bannerUser', this property must be set to the name of the user used to access the banner schema.");
        }
		return bannerUser;
	}

	public static String getBannerPassword() throws Exception{
		String bannerPassword = ApplicationProperties.getProperty("banner.password");
        if (bannerPassword == null || bannerPassword.trim().length() == 0){
        	throw new Exception("Missing required custom application property:  'bannerPassword', this property must be set to the password of the user used to access the banner schema.");
        }
		return bannerPassword;
	}

	
	protected OracleConnector getJDBCconnection(){
		OracleConnector jdbc = null;
		try {
			jdbc = new OracleConnector(
					QueuedItem.getBannerHost(), 
					QueuedItem.getBannerDatabase(),
					QueuedItem.getBannerPort(),
					QueuedItem.getBannerUser(),
					QueuedItem.getBannerPassword());
		} catch (Exception e) {
			Debug.info("*********************************************************************");
			Debug.info("** Error setting up OracleConnector in in callOracleProcess *********");
			Debug.info("*********************************************************************");
			Debug.info(e.getMessage());
			e.printStackTrace();
			Debug.info("*********************************************************************");
		}
		return(jdbc);
	}
	
	protected Document convertClobToDocument(Clob clob){
		Document outDoc = null;
		try {
			outDoc = ClobTools.clobToDocument(clob);
			
		} catch (Exception ex) {
			Debug.info("***************************************");
			Debug.info("** Error in callOracleProcess *********");
			Debug.info("***************************************");
			ex.printStackTrace();
			Debug.info("***************************************");
		}	
		return(outDoc);
	}
		
}
