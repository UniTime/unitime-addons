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

package org.unitime.banner.commons.hibernate.util;

import java.io.Serializable;

import org.hibernate.event.PostUpdateEvent;
import org.hibernate.event.PostUpdateEventListener;
import org.unitime.banner.dataexchange.BannerSectionAuditExport;
import org.unitime.banner.util.MeetingElement;
import org.unitime.timetable.dataexchange.DataExchangeHelper;
import org.unitime.timetable.model.DatePattern;


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
		if (event.getEntity() instanceof DatePattern) {
			DatePattern dp = (DatePattern) event.getEntity();
			MeetingElement.updateDatesForDatePattern(dp);
			if (dp.isDefault()){
				MeetingElement.updateDefaultDatePatternForSession(dp);
			}
		}
	}
}
