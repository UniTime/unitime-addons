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
 * This is an object that contains data related to the integrationqueueout table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="integrationqueueout"
 */

public abstract class BaseQueueOut extends org.unitime.banner.model.Queue  implements Serializable {

	public static String REF = "QueueOut";
	public static String PROP_PICKUP_DATE = "pickupDate";


	// constructors
	public BaseQueueOut () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseQueueOut (java.lang.Long uniqueId) {
		super(uniqueId);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.util.Date pickupDate;






	/**
	 * Return the value associated with the column: pickupdate
	 */
	public java.util.Date getPickupDate () {
		return pickupDate;
	}

	/**
	 * Set the value related to the column: pickupdate
	 * @param pickupDate the pickupdate value
	 */
	public void setPickupDate (java.util.Date pickupDate) {
		this.pickupDate = pickupDate;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.QueueOut)) return false;
		else {
			org.unitime.banner.model.QueueOut queueOut = (org.unitime.banner.model.QueueOut) obj;
			if (null == this.getUniqueId() || null == queueOut.getUniqueId()) return false;
			else return (this.getUniqueId().equals(queueOut.getUniqueId()));
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