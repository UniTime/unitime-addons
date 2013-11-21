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

import java.io.Serializable;

import org.hibernate.event.spi.PostUpdateEvent;
import org.hibernate.event.spi.PostUpdateEventListener;
import org.unitime.banner.dataexchange.BannerSectionAuditExport;
import org.unitime.timetable.dataexchange.DataExchangeHelper;


/**
 * @author says
 *
 */
public class BannerUpdateListener implements PostUpdateEventListener, Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -2515499154754576672L;

    static {
        DataExchangeHelper.sImportRegister.put("bannerOfferings", org.unitime.banner.dataexchange.BannerCourseOfferingImport.class);
        DataExchangeHelper.sImportRegister.put("SCHEDULE_RESPONSE", org.unitime.banner.dataexchange.ReceiveBannerResponseMessage.class);
        DataExchangeHelper.sImportRegister.put("bannerStudentEnrollments", org.unitime.banner.dataexchange.BannerStudentEnrollmentImport.class);
        DataExchangeHelper.sExportRegister.put("schedule", BannerSectionAuditExport.class);
        DataExchangeHelper.sImportRegister.put("enterprise", org.unitime.banner.dataexchange.BannerStudentEnrollmentMessage.class);
        DataExchangeHelper.sImportRegister.put("studentUpdates", org.unitime.banner.dataexchange.BannerStudentDataUpdate.class);
    }

	/**
	 * 
	 */
	public BannerUpdateListener() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.hibernate.event.PostInsertEventListener#onPostInsert(org.hibernate.event.PostInsertEvent)
	 */
	public void onPostUpdate(PostUpdateEvent event) {
		// Add post update actions here if needed.  Currently not needed.
	}
}
