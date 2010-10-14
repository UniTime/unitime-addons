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

import org.dom4j.Document;


/**
 * This is an object that contains data related to the  table.
 * Do not modify this class because it will be overwritten if the configuration file
 * related to this class is modified.
 *
 * @hibernate.class
 *  table=""
 */

public abstract class BaseQueue  implements Serializable {

	public static String REF = "Queue";
	public static String PROP_XML = "xml";
	public static String PROP_STATUS = "status";
	public static String PROP_POST_DATE = "postDate";
	public static String PROP_PROCESS_DATE = "processDate";


	// constructors
	public BaseQueue () {
		initialize();
	}

	/**
	 * Constructor for primary key
	 */
	public BaseQueue (java.lang.Long uniqueId) {
		this.setUniqueId(uniqueId);
		initialize();
	}

	/**
	 * Constructor for required fields
	 */
	public BaseQueue (
		java.lang.Long uniqueId,
		Document xml) {

		this.setUniqueId(uniqueId);
		this.setXml(xml);
		initialize();
	}

	protected void initialize () {}



	private int hashCode = Integer.MIN_VALUE;

	// primary key
	private java.lang.Long uniqueId;

	// fields
	private Document xml;
	private java.lang.String status;
	private java.util.Date postDate;
	private java.util.Date processDate;



	/**
	 * Return the unique identifier of this class
     * @hibernate.id
     *  generator-class="increment"
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
	 * Return the value associated with the column: xml
	 */
	public Document getXml () {
		return xml;
	}

	/**
	 * Set the value related to the column: xml
	 * @param xml the xml value
	 */
	public void setXml (Document xml) {
		this.xml = xml;
	}



	/**
	 * Return the value associated with the column: status
	 */
	public java.lang.String getStatus () {
		return status;
	}

	/**
	 * Set the value related to the column: status
	 * @param status the status value
	 */
	public void setStatus (java.lang.String status) {
		this.status = status;
	}



	/**
	 * Return the value associated with the column: postdate
	 */
	public java.util.Date getPostDate () {
		return postDate;
	}

	/**
	 * Set the value related to the column: postdate
	 * @param postDate the postdate value
	 */
	public void setPostDate (java.util.Date postDate) {
		this.postDate = postDate;
	}



	/**
	 * Return the value associated with the column: processdate
	 */
	public java.util.Date getProcessDate () {
		return processDate;
	}

	/**
	 * Set the value related to the column: processdate
	 * @param processDate the processdate value
	 */
	public void setProcessDate (java.util.Date processDate) {
		this.processDate = processDate;
	}





	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.Queue)) return false;
		else {
			org.unitime.banner.model.Queue queue = (org.unitime.banner.model.Queue) obj;
			if (null == this.getUniqueId() || null == queue.getUniqueId()) return false;
			else return (this.getUniqueId().equals(queue.getUniqueId()));
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