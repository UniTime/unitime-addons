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
 * This is an object that contains data related to the integrationqueueerror table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="integrationqueueerror"
 */

public abstract class BaseQueueError  implements Serializable {

	public static String REF = "QueueError";
	public static String PROP_ERROR_TEXT = "errorText";


	// constructors
	public BaseQueueError () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseQueueError (org.unitime.banner.model.QueueErrorId id) {
		this.setId(id);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private org.unitime.banner.model.QueueErrorId id;

	// fields
	private java.lang.String errorText;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     */
	public org.unitime.banner.model.QueueErrorId getId () {
		return id;
	}

	/**
	 * Set the unique identifier of this class
	 * @param id the new ID
	 */
	public void setId (org.unitime.banner.model.QueueErrorId id) {
		this.id = id;
		this.hashCode = Integer.MIN_VALUE;
	}




	/**
	 * Return the value associated with the column: errortext
	 */
	public java.lang.String getErrorText () {
		return errorText;
	}

	/**
	 * Set the value related to the column: errortext
	 * @param errorText the errortext value
	 */
	public void setErrorText (java.lang.String errorText) {
		this.errorText = errorText;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.QueueError)) return false;
		else {
			org.unitime.banner.model.QueueError queueError = (org.unitime.banner.model.QueueError) obj;
			if (null == this.getId() || null == queueError.getId()) return false;
			else return (this.getId().equals(queueError.getId()));
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			if (null == this.getId()) return super.hashCode();
			else {
				String hashStr = this.getClass().getName() + ":" + this.getId().hashCode();
				this.hashCode = hashStr.hashCode();
			}
		}
		return this.hashCode;
	}


	public String toString () {
		return super.toString();
	}


}