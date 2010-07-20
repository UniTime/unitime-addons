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
 * This is an object that contains data related to the banner_section_join_class table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="banner_section_join_class"
 */

public abstract class BaseBannerSectionToClass  implements Serializable {

	public static String REF = "BannerSectionToClass";
	public static String PROP_CLASS_ID = "classId";


	// constructors
	public BaseBannerSectionToClass () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseBannerSectionToClass (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseBannerSectionToClass (
		java.lang.Long uniqueId,
		org.unitime.banner.model.BannerSection bannerSection,
		java.lang.Long classId) {

		this.setUniqueId(uniqueId);
		this.setBannerSection(bannerSection);
		this.setClassId(classId);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private java.lang.Long classId;

	// many to one
	private org.unitime.banner.model.BannerSection bannerSection;



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
	 * Return the value associated with the column: class_id
	 */
	public java.lang.Long getClassId () {
		return classId;
	}

	/**
	 * Set the value related to the column: class_id
	 * @param classId the class_id value
	 */
	public void setClassId (java.lang.Long classId) {
		this.classId = classId;
	}



	/**
	 * Return the value associated with the column: banner_section_id
	 */
	public org.unitime.banner.model.BannerSection getBannerSection () {
		return bannerSection;
	}

	/**
	 * Set the value related to the column: banner_section_id
	 * @param bannerSection the banner_section_id value
	 */
	public void setBannerSection (org.unitime.banner.model.BannerSection bannerSection) {
		this.bannerSection = bannerSection;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.BannerSectionToClass)) return false;
		else {
			org.unitime.banner.model.BannerSectionToClass bannerSectionToClass = (org.unitime.banner.model.BannerSectionToClass) obj;
			if (null == this.getUniqueId() || null == bannerSectionToClass.getUniqueId()) return false;
			else return (this.getUniqueId().equals(bannerSectionToClass.getUniqueId()));
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