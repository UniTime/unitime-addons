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
package org.unitime.colleague.util;

import java.io.File;

import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.io.SAXReader;
import org.unitime.colleague.queueprocessor.QueuedItem;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;

/**
 * Usage:
 * <code>
 * 		java -Xmx2g -Dtmtbl.custom.properties=~/Tomcat/conf/unitime.properties -cp timetable.jar \
 * 		     org.unitime.timetable.util.ImportXmlFile fileToImport.xml myExternalId
 * </code>
 * Where tmtbl.custom.properties points to UniTime custom properties (if there are any) and the fileToImport.xml is
 * the XML file to import.
 * The second parameter (external id of the timetabling manager under which the import is to be done) is optional.
 *
 * @author Stephanie Schluttenhofer
 */
public class SendXmlFileToColleague {

	public static void main(String[] args) {
		try {
		   // Configure logging
	       org.apache.log4j.PropertyConfigurator.configure(ApplicationProperties.getProperties());
	        
	       // Configure hibernate
	       HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

	       // Load an XML file
	       Document document = (new SAXReader()).read(new File(args[0]));
	       	        	        	        
	       QueuedItem qi = new QueuedItem(document);
    	   Document outDocument = qi.sendTestMessage();

	       System.out.print("beginText: '" + outDocument.asXML() + "' : endText\n");
	      
	        
	       // Close hibernate
	       HibernateUtil.closeHibernate();
		} catch (Exception e) {
		   Logger.getLogger(SendXmlFileToColleague.class).error("Error: " +e.getMessage(), e);
		}
    }
	
}
