/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.banner.commons.hibernate.util;

import org.dom4j.Document;
import org.unitime.banner.dataexchange.BannerSectionAuditExport;
import org.unitime.commons.hibernate.util.DatabaseUpdate;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.DataExchangeHelper;

/**
 * @author says
 *
 */
public class BannerAddOnDatabaseUpdate extends DatabaseUpdate {
	    static {
	        DataExchangeHelper.sImportRegister.put("bannerOfferings", org.unitime.banner.dataexchange.BannerCourseOfferingImport.class);
	        DataExchangeHelper.sImportRegister.put("SCHEDULE_RESPONSE", org.unitime.banner.dataexchange.ReceiveBannerResponseMessage.class);
	        DataExchangeHelper.sImportRegister.put("bannerStudentEnrollments", org.unitime.banner.dataexchange.BannerStudentEnrollmentImport.class);
	        DataExchangeHelper.sExportRegister.put("schedule", BannerSectionAuditExport.class);
	        DataExchangeHelper.sImportRegister.put("enterprise", org.unitime.banner.dataexchange.BannerStudentEnrollmentMessage.class);
	        DataExchangeHelper.sImportRegister.put("studentUpdates", org.unitime.banner.onlinesectioning.BannerStudentUpdates.class);
	        DataExchangeHelper.sImportRegister.put("students", org.unitime.banner.dataexchange.BannerStudentImport.class);
	        DataExchangeHelper.sExportRegister.put("students", org.unitime.banner.dataexchange.BannerStudentExport.class);
	    }
		public BannerAddOnDatabaseUpdate(Document document) throws Exception {
	        super(document);
	    }

		public BannerAddOnDatabaseUpdate() throws Exception {
	        super();
	    }

		@Override
		protected String findDbUpdateFileName(){
		 	return(ApplicationProperties.getProperty("tmtbl.db.banner.update","bannerdbupdate.xml"));
		}
		
		@Override
		protected String versionParameterName(){
		 	return("tmtbl.db.banner.version");
		}

		@Override
		protected String updateName() {
			return("Banner Add On");
		}

}
