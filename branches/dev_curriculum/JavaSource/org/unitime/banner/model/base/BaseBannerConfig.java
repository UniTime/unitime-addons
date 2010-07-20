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
 * This is an object that contains data related to the banner_config table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="banner_config"
 */

public abstract class BaseBannerConfig  implements Serializable {

	public static String REF = "BannerConfig";
	public static String PROP_INSTR_OFFERING_CONFIG_ID = "instrOfferingConfigId";
	public static String PROP_UNIQUE_ID_ROLLED_FORWARD_FROM = "uniqueIdRolledForwardFrom";


	// constructors
	public BaseBannerConfig () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseBannerConfig (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseBannerConfig (
		java.lang.Long uniqueId,
		org.unitime.banner.model.BannerCourse bannerCourse,
		java.lang.Long instrOfferingConfigId) {

		this.setUniqueId(uniqueId);
		this.setBannerCourse(bannerCourse);
		this.setInstrOfferingConfigId(instrOfferingConfigId);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Long instrOfferingConfigId;
	private java.lang.Long uniqueIdRolledForwardFrom;

	// many to one
	private org.unitime.timetable.model.ItypeDesc gradableItype;
	private org.unitime.banner.model.BannerCourse bannerCourse;

	// collections
	private java.util.Set bannerSections;



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
	 * Return the value associated with the column: instr_offr_config_id
	 */
	public java.lang.Long getInstrOfferingConfigId () {
		return instrOfferingConfigId;
	}

	/**
	 * Set the value related to the column: instr_offr_config_id
	 * @param instrOfferingConfigId the instr_offr_config_id value
	 */
	public void setInstrOfferingConfigId (java.lang.Long instrOfferingConfigId) {
		this.instrOfferingConfigId = instrOfferingConfigId;
	}



	/**
	 * Return the value associated with the column: uid_rolled_fwd_from
	 */
	public java.lang.Long getUniqueIdRolledForwardFrom () {
		return uniqueIdRolledForwardFrom;
	}

	/**
	 * Set the value related to the column: uid_rolled_fwd_from
	 * @param uniqueIdRolledForwardFrom the uid_rolled_fwd_from value
	 */
	public void setUniqueIdRolledForwardFrom (java.lang.Long uniqueIdRolledForwardFrom) {
		this.uniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom;
	}



	/**
	 * Return the value associated with the column: gradable_itype_id
	 */
	public org.unitime.timetable.model.ItypeDesc getGradableItype () {
		return gradableItype;
	}

	/**
	 * Set the value related to the column: gradable_itype_id
	 * @param gradableItype the gradable_itype_id value
	 */
	public void setGradableItype (org.unitime.timetable.model.ItypeDesc gradableItype) {
		this.gradableItype = gradableItype;
	}



	/**
	 * Return the value associated with the column: banner_course_id
	 */
	public org.unitime.banner.model.BannerCourse getBannerCourse () {
		return bannerCourse;
	}

	/**
	 * Set the value related to the column: banner_course_id
	 * @param bannerCourse the banner_course_id value
	 */
	public void setBannerCourse (org.unitime.banner.model.BannerCourse bannerCourse) {
		this.bannerCourse = bannerCourse;
	}



	/**
	 * Return the value associated with the column: bannerSections
	 */
	public java.util.Set getBannerSections () {
		return bannerSections;
	}

	/**
	 * Set the value related to the column: bannerSections
	 * @param bannerSections the bannerSections value
	 */
	public void setBannerSections (java.util.Set bannerSections) {
		this.bannerSections = bannerSections;
	}

	public void addTobannerSections (org.unitime.banner.model.BannerSection bannerSection) {
		if (null == getBannerSections()) setBannerSections(new java.util.HashSet());
		getBannerSections().add(bannerSection);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.BannerConfig)) return false;
		else {
			org.unitime.banner.model.BannerConfig bannerConfig = (org.unitime.banner.model.BannerConfig) obj;
			if (null == this.getUniqueId() || null == bannerConfig.getUniqueId()) return false;
			else return (this.getUniqueId().equals(bannerConfig.getUniqueId()));
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