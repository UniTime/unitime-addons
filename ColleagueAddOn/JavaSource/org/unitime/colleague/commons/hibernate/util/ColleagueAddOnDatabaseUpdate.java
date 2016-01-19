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
package org.unitime.colleague.commons.hibernate.util;

import org.dom4j.Document;
import org.unitime.colleague.dataexchange.ColleagueSectionAuditExport;
import org.unitime.commons.hibernate.util.DatabaseUpdate;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.DataExchangeHelper;

/**
 * @author says
 *
 */
public class ColleagueAddOnDatabaseUpdate extends DatabaseUpdate {
	    static {
	        DataExchangeHelper.sImportRegister.put("SCHEDULE_RESPONSE", org.unitime.colleague.dataexchange.ReceiveColleagueResponseMessage.class);
	        DataExchangeHelper.sExportRegister.put("schedule", ColleagueSectionAuditExport.class);
	        DataExchangeHelper.sImportRegister.put("studentUpdates", org.unitime.colleague.onlinesectioning.ColleagueStudentUpdates.class);
	        DataExchangeHelper.sImportRegister.put("COLLEAGUE_RESTRICTIONS", org.unitime.colleague.dataexchange.ImportColleagueRestrictions.class);
	    }
		public ColleagueAddOnDatabaseUpdate(Document document) throws Exception {
	        super(document);
	    }

		public ColleagueAddOnDatabaseUpdate() throws Exception {
	        super();
	    }

		@Override
		protected String findDbUpdateFileName(){
		 	return(ApplicationProperties.getProperty("tmtbl.db.colleague.update","colleaguedbupdate.xml"));
		}
		
		@Override
		protected String versionParameterName(){
		 	return("tmtbl.db.colleague.version");
		}

		@Override
		protected String updateName() {
			return("Colleague Add On");
		}

}
