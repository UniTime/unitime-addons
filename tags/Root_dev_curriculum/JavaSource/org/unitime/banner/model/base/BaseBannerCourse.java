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
 * This is an object that contains data related to the banner_course table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="banner_course"
 */

public abstract class BaseBannerCourse  implements Serializable {

	public static String REF = "BannerCourse";
	public static String PROP_COURSE_OFFERING_ID = "courseOfferingId";
	public static String PROP_UNIQUE_ID_ROLLED_FORWARD_FROM = "uniqueIdRolledForwardFrom";


	// constructors
	public BaseBannerCourse () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseBannerCourse (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseBannerCourse (
		java.lang.Long uniqueId,
		java.lang.Long courseOfferingId) {

		this.setUniqueId(uniqueId);
		this.setCourseOfferingId(courseOfferingId);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Long courseOfferingId;
	private java.lang.Long uniqueIdRolledForwardFrom;

	// collections
	private java.util.Set bannerConfigs;



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
	 * Return the value associated with the column: course_offering_id
	 */
	public java.lang.Long getCourseOfferingId () {
		return courseOfferingId;
	}

	/**
	 * Set the value related to the column: course_offering_id
	 * @param courseOfferingId the course_offering_id value
	 */
	public void setCourseOfferingId (java.lang.Long courseOfferingId) {
		this.courseOfferingId = courseOfferingId;
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
	 * Return the value associated with the column: bannerConfigs
	 */
	public java.util.Set getBannerConfigs () {
		return bannerConfigs;
	}

	/**
	 * Set the value related to the column: bannerConfigs
	 * @param bannerConfigs the bannerConfigs value
	 */
	public void setBannerConfigs (java.util.Set bannerConfigs) {
		this.bannerConfigs = bannerConfigs;
	}

	public void addTobannerConfigs (org.unitime.banner.model.BannerConfig bannerConfig) {
		if (null == getBannerConfigs()) setBannerConfigs(new java.util.HashSet());
		getBannerConfigs().add(bannerConfig);
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.BannerCourse)) return false;
		else {
			org.unitime.banner.model.BannerCourse bannerCourse = (org.unitime.banner.model.BannerCourse) obj;
			if (null == this.getUniqueId() || null == bannerCourse.getUniqueId()) return false;
			else return (this.getUniqueId().equals(bannerCourse.getUniqueId()));
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