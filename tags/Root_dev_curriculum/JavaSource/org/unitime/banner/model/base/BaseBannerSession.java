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

package org.unitime.banner.model.base;

import java.io.Serializable;


/**
 * This is an object that contains data related to the banner_session table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="banner_session"
 */

public abstract class BaseBannerSession  implements Serializable {

	public static String REF = "BannerSession";
	public static String PROP_BANNER_CAMPUS = "bannerCampus";
	public static String PROP_BANNER_TERM_CODE = "bannerTermCode";
	public static String PROP_STORE_DATA_FOR_BANNER = "storeDataForBanner";
	public static String PROP_SEND_DATA_TO_BANNER = "sendDataToBanner";
	public static String PROP_LOADING_OFFERINGS_FILE = "loadingOfferingsFile";


	// constructors
	public BaseBannerSession () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseBannerSession (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseBannerSession (
		java.lang.Long uniqueId,
		org.unitime.timetable.model.Session session,
		java.lang.String bannerCampus,
		java.lang.String bannerTermCode,
		java.lang.Boolean storeDataForBanner,
		java.lang.Boolean sendDataToBanner,
		java.lang.Boolean loadingOfferingsFile) {

		this.setUniqueId(uniqueId);
		this.setSession(session);
		this.setBannerCampus(bannerCampus);
		this.setBannerTermCode(bannerTermCode);
		this.setStoreDataForBanner(storeDataForBanner);
		this.setSendDataToBanner(sendDataToBanner);
		this.setLoadingOfferingsFile(loadingOfferingsFile);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String bannerCampus;
	private java.lang.String bannerTermCode;
	private java.lang.Boolean storeDataForBanner;
	private java.lang.Boolean sendDataToBanner;
	private java.lang.Boolean loadingOfferingsFile;

	// many to one
	private org.unitime.timetable.model.Session session;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="org.unitime.commons.hibernate.id.UniqueIdGenerator"
     *  column="UNIQUEID"
     */
	public java.lang.Long getUniqueId () {
		return uniqueId;
	}

	/**
	 * Set the unique identifier of this class
	 * @param uniqueId the new ID
	 */
	public void setUniqueId (java.lang.Long uniqueId) {
		this.uniqueId = uniqueId;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: banner_campus
	 */
	public java.lang.String getBannerCampus () {
		return bannerCampus;
	}

	/**
	 * Set the value related to the column: banner_campus
	 * @param bannerCampus the banner_campus value
	 */
	public void setBannerCampus (java.lang.String bannerCampus) {
		this.bannerCampus = bannerCampus;
	}



	/**
	 * Return the value associated with the column: banner_term_code
	 */
	public java.lang.String getBannerTermCode () {
		return bannerTermCode;
	}

	/**
	 * Set the value related to the column: banner_term_code
	 * @param bannerTermCode the banner_term_code value
	 */
	public void setBannerTermCode (java.lang.String bannerTermCode) {
		this.bannerTermCode = bannerTermCode;
	}



	/**
	 * Return the value associated with the column: store_data_for_banner
	 */
	public java.lang.Boolean isStoreDataForBanner () {
		return storeDataForBanner;
	}

	/**
	 * Set the value related to the column: store_data_for_banner
	 * @param storeDataForBanner the store_data_for_banner value
	 */
	public void setStoreDataForBanner (java.lang.Boolean storeDataForBanner) {
		this.storeDataForBanner = storeDataForBanner;
	}



	/**
	 * Return the value associated with the column: send_data_to_banner
	 */
	public java.lang.Boolean isSendDataToBanner () {
		return sendDataToBanner;
	}

	/**
	 * Set the value related to the column: send_data_to_banner
	 * @param sendDataToBanner the send_data_to_banner value
	 */
	public void setSendDataToBanner (java.lang.Boolean sendDataToBanner) {
		this.sendDataToBanner = sendDataToBanner;
	}



	/**
	 * Return the value associated with the column: loading_offerings_file
	 */
	public java.lang.Boolean isLoadingOfferingsFile () {
		return loadingOfferingsFile;
	}

	/**
	 * Set the value related to the column: loading_offerings_file
	 * @param loadingOfferingsFile the loading_offerings_file value
	 */
	public void setLoadingOfferingsFile (java.lang.Boolean loadingOfferingsFile) {
		this.loadingOfferingsFile = loadingOfferingsFile;
	}



	/**
	 * Return the value associated with the column: session_id
	 */
	public org.unitime.timetable.model.Session getSession () {
		return session;
	}

	/**
	 * Set the value related to the column: session_id
	 * @param session the session_id value
	 */
	public void setSession (org.unitime.timetable.model.Session session) {
		this.session = session;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.BannerSession)) return false;
		else {
			org.unitime.banner.model.BannerSession bannerSession = (org.unitime.banner.model.BannerSession) obj;
			if (null == this.getUniqueId() || null == bannerSession.getUniqueId()) return false;
			else return (this.getUniqueId().equals(bannerSession.getUniqueId()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getUniqueId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getUniqueId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}