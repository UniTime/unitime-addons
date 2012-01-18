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
import org.unitime.commons.hibernate.util.DatabaseUpdate;
import org.unitime.timetable.ApplicationProperties;

/**
 * @author says
 *
 */
public class BannerAddOnDatabaseUpdate extends DatabaseUpdate {

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
