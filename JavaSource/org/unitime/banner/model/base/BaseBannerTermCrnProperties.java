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
 * This is an object that contains data related to the banner_crn_provider table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="banner_crn_provider"
 */

public abstract class BaseBannerTermCrnProperties  implements Serializable {

	public static String REF = "BannerTermCrnProperties";
	public static String PROP_BANNER_TERM_CODE = "bannerTermCode";
	public static String PROP_LAST_CRN = "lastCrn";
	public static String PROP_SEARCH_FLAG = "searchFlag";
	public static String PROP_MIN_CRN = "minCrn";
	public static String PROP_MAX_CRN = "maxCrn";


	// constructors
	public BaseBannerTermCrnProperties () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseBannerTermCrnProperties (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseBannerTermCrnProperties (
		java.lang.Long uniqueId,
		java.lang.String bannerTermCode,
		java.lang.Integer lastCrn,
		java.lang.Boolean searchFlag,
		java.lang.Integer minCrn,
		java.lang.Integer maxCrn) {

		this.setUniqueId(uniqueId);
		this.setBannerTermCode(bannerTermCode);
		this.setLastCrn(lastCrn);
		this.setSearchFlag(searchFlag);
		this.setMinCrn(minCrn);
		this.setMaxCrn(maxCrn);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.String bannerTermCode;
	private java.lang.Integer lastCrn;
	private java.lang.Boolean searchFlag;
	private java.lang.Integer minCrn;
	private java.lang.Integer maxCrn;



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
	 * Return the value associated with the column: last_crn
	 */
	public java.lang.Integer getLastCrn () {
		return lastCrn;
	}

	/**
	 * Set the value related to the column: last_crn
	 * @param lastCrn the last_crn value
	 */
	public void setLastCrn (java.lang.Integer lastCrn) {
		this.lastCrn = lastCrn;
	}



	/**
	 * Return the value associated with the column: search_flag
	 */
	public java.lang.Boolean isSearchFlag () {
		return searchFlag;
	}

	/**
	 * Set the value related to the column: search_flag
	 * @param searchFlag the search_flag value
	 */
	public void setSearchFlag (java.lang.Boolean searchFlag) {
		this.searchFlag = searchFlag;
	}



	/**
	 * Return the value associated with the column: min_crn
	 */
	public java.lang.Integer getMinCrn () {
		return minCrn;
	}

	/**
	 * Set the value related to the column: min_crn
	 * @param minCrn the min_crn value
	 */
	public void setMinCrn (java.lang.Integer minCrn) {
		this.minCrn = minCrn;
	}



	/**
	 * Return the value associated with the column: max_crn
	 */
	public java.lang.Integer getMaxCrn () {
		return maxCrn;
	}

	/**
	 * Set the value related to the column: max_crn
	 * @param maxCrn the max_crn value
	 */
	public void setMaxCrn (java.lang.Integer maxCrn) {
		this.maxCrn = maxCrn;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.BannerTermCrnProperties)) return false;
		else {
			org.unitime.banner.model.BannerTermCrnProperties bannerTermCrnProperties = (org.unitime.banner.model.BannerTermCrnProperties) obj;
			if (null == this.getUniqueId() || null == bannerTermCrnProperties.getUniqueId()) return false;
			else return (this.getUniqueId().equals(bannerTermCrnProperties.getUniqueId()));
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