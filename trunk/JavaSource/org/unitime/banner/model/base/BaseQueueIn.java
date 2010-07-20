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
 * This is an object that contains data related to the integrationqueuein table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table="integrationqueuein"
 */

public abstract class BaseQueueIn extends org.unitime.banner.model.Queue  implements Serializable {

	public static String REF = "QueueIn";
	public static String PROP_MATCH_ID = "matchId";


	// constructors
	public BaseQueueIn () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseQueueIn (java.lang.Long uniqueId) {
		super(uniqueId);
	}



	private int hashCode = Integer.MIN_VALUE;


	// fields
	private java.lang.Long matchId;






	/**
	 * Return the value associated with the column: matchid
	 */
	public java.lang.Long getMatchId () {
		return matchId;
	}

	/**
	 * Set the value related to the column: matchid
	 * @param matchId the matchid value
	 */
	public void setMatchId (java.lang.Long matchId) {
		this.matchId = matchId;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.QueueIn)) return false;
		else {
			org.unitime.banner.model.QueueIn queueIn = (org.unitime.banner.model.QueueIn) obj;
			if (null == this.getUniqueId() || null == queueIn.getUniqueId()) return false;
			else return (this.getUniqueId().equals(queueIn.getUniqueId()));
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