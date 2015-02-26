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

import java.util.Properties;

import org.cpsolver.ifs.util.ToolBox;
import org.unitime.banner.dataexchange.BannerSectionAuditExport;
import org.unitime.commons.Debug;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Session;


/**
 * @author says
 *
 */
public class ExportBannerAuditXmlFile {

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
            ToolBox.configureLogging();
    		HibernateUtil.configureHibernate(ApplicationProperties.getProperties());

    		BannerSectionAuditExport bsea = new BannerSectionAuditExport();
    		Session session = Session.getSessionUsingInitiativeYearTerm(args[0],
    				args[1], args[2]);

	        String fileName = args[3];
         	Debug.info("filename = " + fileName);
     		
    		bsea.saveXml(fileName, session, new Properties());
                         
    }
	
}
