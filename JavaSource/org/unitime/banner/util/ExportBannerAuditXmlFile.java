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

package org.unitime.banner.util;

import java.util.Properties;

import net.sf.cpsolver.ifs.util.ToolBox;

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
