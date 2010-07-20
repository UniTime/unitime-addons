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
 * This is an object that contains data related to the banner_section table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="banner_section"
 */

public abstract class BaseBannerSection  implements Serializable {

	public static String REF = "BannerSection";
	public static String PROP_CRN = "crn";
	public static String PROP_SECTION_INDEX = "sectionIndex";
	public static String PROP_CROSS_LIST_IDENTIFIER = "crossListIdentifier";
	public static String PROP_LINK_IDENTIFIER = "linkIdentifier";
	public static String PROP_LINK_CONNECTOR = "linkConnector";
	public static String PROP_UNIQUE_ID_ROLLED_FORWARD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_OVERRIDE_LIMIT = "overrideLimit";
	public static String PROP_OVERRIDE_COURSE_CREDIT = "overrideCourseCredit";


	// constructors
	public BaseBannerSection () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseBannerSection (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseBannerSection (
		java.lang.Long uniqueId,
		org.unitime.banner.model.BannerConfig bannerConfig,
		org.unitime.timetable.model.Session session) {

		this.setUniqueId(uniqueId);
		this.setBannerConfig(bannerConfig);
		this.setSession(session);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Integer crn;
	private java.lang.String sectionIndex;
	private java.lang.String crossListIdentifier;
	private java.lang.String linkIdentifier;
	private java.lang.String linkConnector;
	private java.lang.Long uniqueIdRolledForwardFrom;
	private java.lang.Integer overrideLimit;
	private java.lang.Float overrideCourseCredit;

	// many to one
	private org.unitime.banner.model.BannerConfig bannerConfig;
	private org.unitime.timetable.model.OfferingConsentType consentType;
	private org.unitime.banner.model.BannerSection parentBannerSection;
	private org.unitime.timetable.model.Session session;

	// collections
	private java.util.Set bannerSectionToClasses;
	private java.util.Set bannerSectionToChildSections;



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
	 * Return the value associated with the column: crn
	 */
	public java.lang.Integer getCrn () {
		return crn;
	}

	/**
	 * Set the value related to the column: crn
	 * @param crn the crn value
	 */
	public void setCrn (java.lang.Integer crn) {
		this.crn = crn;
	}



	/**
	 * Return the value associated with the column: cross_list_identifier
	 */
	public java.lang.String getCrossListIdentifier () {
		return crossListIdentifier;
	}

	/**
	 * Set the value related to the column: cross_list_identifier
	 * @param crossListIdentifier the cross_list_identifier value
	 */
	public void setCrossListIdentifier (java.lang.String crossListIdentifier) {
		this.crossListIdentifier = crossListIdentifier;
	}



	/**
	 * Return the value associated with the column: link_connector
	 */
	public java.lang.String getLinkConnector () {
		return linkConnector;
	}

	/**
	 * Set the value related to the column: link_connector
	 * @param linkConnector the link_connector value
	 */
	public void setLinkConnector (java.lang.String linkConnector) {
		this.linkConnector = linkConnector;
	}



	/**
	 * Return the value associated with the column: link_identifier
	 */
	public java.lang.String getLinkIdentifier () {
		return linkIdentifier;
	}

	/**
	 * Set the value related to the column: link_identifier
	 * @param linkIdentifier the link_identifier value
	 */
	public void setLinkIdentifier (java.lang.String linkIdentifier) {
		this.linkIdentifier = linkIdentifier;
	}



	/**
	 * Return the value associated with the column: section_index
	 */
	public java.lang.String getSectionIndex () {
		return sectionIndex;
	}

	/**
	 * Set the value related to the column: section_index
	 * @param sectionIndex the section_index value
	 */
	public void setSectionIndex (java.lang.String sectionIndex) {
		this.sectionIndex = sectionIndex;
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
	 * Return the value associated with the column: override_limit
	 */
	public java.lang.Integer getOverrideLimit () {
		return overrideLimit;
	}

	/**
	 * Set the value related to the column: override_limit
	 * @param overrideLimit the override_limit value
	 */
	public void setOverrideLimit (java.lang.Integer overrideLimit) {
		this.overrideLimit = overrideLimit;
	}



	/**
	 * Return the value associated with the column: override_course_credit
	 */
	public java.lang.Float getOverrideCourseCredit () {
		return overrideCourseCredit;
	}

	/**
	 * Set the value related to the column: override_course_credit
	 * @param overrideCourseCredit the override_course_credit value
	 */
	public void setOverrideCourseCredit (java.lang.Float overrideCourseCredit) {
		this.overrideCourseCredit = overrideCourseCredit;
	}



	/**
	 * Return the value associated with the column: banner_config_id
	 */
	public org.unitime.banner.model.BannerConfig getBannerConfig () {
		return bannerConfig;
	}

	/**
	 * Set the value related to the column: banner_config_id
	 * @param bannerConfig the banner_config_id value
	 */
	public void setBannerConfig (org.unitime.banner.model.BannerConfig bannerConfig) {
		this.bannerConfig = bannerConfig;
	}



	/**
	 * Return the value associated with the column: consent_type_id
	 */
	public org.unitime.timetable.model.OfferingConsentType getConsentType () {
		return consentType;
	}

	/**
	 * Set the value related to the column: consent_type_id
	 * @param consentType the consent_type_id value
	 */
	public void setConsentType (org.unitime.timetable.model.OfferingConsentType consentType) {
		this.consentType = consentType;
	}



	/**
	 * Return the value associated with the column: parent_banner_section_id
	 */
	public org.unitime.banner.model.BannerSection getParentBannerSection () {
		return parentBannerSection;
	}

	/**
	 * Set the value related to the column: parent_banner_section_id
	 * @param parentBannerSection the parent_banner_section_id value
	 */
	public void setParentBannerSection (org.unitime.banner.model.BannerSection parentBannerSection) {
		this.parentBannerSection = parentBannerSection;
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



	/**
	 * Return the value associated with the column: bannerSectionToChildSections
	 */
	public java.util.Set getBannerSectionToChildSections () {
		return bannerSectionToChildSections;
	}

	/**
	 * Set the value related to the column: bannerSectionToChildSections
	 * @param bannerSectionToChildSections the bannerSectionToChildSections value
	 */
	public void setBannerSectionToChildSections (java.util.Set bannerSectionToChildSections) {
		this.bannerSectionToChildSections = bannerSectionToChildSections;
	}

	public void addTobannerSectionToChildSections (org.unitime.banner.model.BannerSection bannerSection) {
		if (null == getBannerSectionToChildSections()) setBannerSectionToChildSections(new java.util.HashSet());
		getBannerSectionToChildSections().add(bannerSection);
	}



	/**
	 * Return the value associated with the column: bannerSectionToClasses
	 */
	public java.util.Set getBannerSectionToClasses () {
		return bannerSectionToClasses;
	}

	/**
	 * Set the value related to the column: bannerSectionToClasses
	 * @param bannerSectionToClasses the bannerSectionToClasses value
	 */
	public void setBannerSectionToClasses (java.util.Set bannerSectionToClasses) {
		this.bannerSectionToClasses = bannerSectionToClasses;
	}

	public void addTobannerSectionToClasses (org.unitime.banner.model.BannerSectionToClass bannerSectionToClass) {
		if (null == getBannerSectionToClasses()) setBannerSectionToClasses(new java.util.HashSet());
		getBannerSectionToClasses().add(bannerSectionToClass);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.BannerSection)) return false;
		else {
			org.unitime.banner.model.BannerSection bannerSection = (org.unitime.banner.model.BannerSection) obj;
			if (null == this.getUniqueId() || null == bannerSection.getUniqueId()) return false;
			else return (this.getUniqueId().equals(bannerSection.getUniqueId()));
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