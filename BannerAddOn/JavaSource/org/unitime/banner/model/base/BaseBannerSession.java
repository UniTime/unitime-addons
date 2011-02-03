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
package org.unitime.banner.model.base;

import java.io.Serializable;

import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.model.Session;

public abstract class BaseBannerSession implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iBannerCampus;
	private String iBannerTermCode;
	private Boolean iStoreDataForBanner;
	private Boolean iSendDataToBanner;
	private Boolean iLoadingOfferingsFile;

	private Session iSession;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_BANNER_CAMPUS = "bannerCampus";
	public static String PROP_BANNER_TERM_CODE = "bannerTermCode";
	public static String PROP_STORE_DATA_FOR_BANNER = "storeDataForBanner";
	public static String PROP_SEND_DATA_TO_BANNER = "sendDataToBanner";
	public static String PROP_LOADING_OFFERINGS_FILE = "loadingOfferingsFile";

	public BaseBannerSession() {
		initialize();
	}

	public BaseBannerSession(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getBannerCampus() { return iBannerCampus; }
	public void setBannerCampus(String bannerCampus) { iBannerCampus = bannerCampus; }

	public String getBannerTermCode() { return iBannerTermCode; }
	public void setBannerTermCode(String bannerTermCode) { iBannerTermCode = bannerTermCode; }

	public Boolean isStoreDataForBanner() { return iStoreDataForBanner; }
	public Boolean getStoreDataForBanner() { return iStoreDataForBanner; }
	public void setStoreDataForBanner(Boolean storeDataForBanner) { iStoreDataForBanner = storeDataForBanner; }

	public Boolean isSendDataToBanner() { return iSendDataToBanner; }
	public Boolean getSendDataToBanner() { return iSendDataToBanner; }
	public void setSendDataToBanner(Boolean sendDataToBanner) { iSendDataToBanner = sendDataToBanner; }

	public Boolean isLoadingOfferingsFile() { return iLoadingOfferingsFile; }
	public Boolean getLoadingOfferingsFile() { return iLoadingOfferingsFile; }
	public void setLoadingOfferingsFile(Boolean loadingOfferingsFile) { iLoadingOfferingsFile = loadingOfferingsFile; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerSession)) return false;
		if (getUniqueId() == null || ((BannerSession)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerSession)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BannerSession["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerSession[" +
			"\n	BannerCampus: " + getBannerCampus() +
			"\n	BannerTermCode: " + getBannerTermCode() +
			"\n	LoadingOfferingsFile: " + getLoadingOfferingsFile() +
			"\n	SendDataToBanner: " + getSendDataToBanner() +
			"\n	Session: " + getSession() +
			"\n	StoreDataForBanner: " + getStoreDataForBanner() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
