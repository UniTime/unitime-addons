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
package org.unitime.banner.commons.hibernate.util;

import org.dom4j.Document;
import org.unitime.banner.dataexchange.BannerSectionAuditExport;
import org.unitime.banner.reports.pointintimedata.AllWSCHForDepartmentByClassAndInstructor;
import org.unitime.commons.hibernate.util.DatabaseUpdate;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.reports.pointintimedata.BasePointInTimeDataReports;

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
	        BasePointInTimeDataReports.sPointInTimeDataReportRegister.put("allWSCHforDeptbyClassAndInstructor", AllWSCHForDepartmentByClassAndInstructor.class);
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
