/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.banner.model.base;

import java.io.Serializable;
import java.util.Date;

import org.dom4j.Document;
import org.unitime.banner.model.Queue;

public abstract class BaseQueue implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Document iXml;
	private String iStatus;
	private Date iPostDate;
	private Date iProcessDate;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_XML = "xml";
	public static String PROP_STATUS = "status";
	public static String PROP_POSTDATE = "postDate";
	public static String PROP_PROCESSDATE = "processDate";

	public BaseQueue() {
		initialize();
	}

	public BaseQueue(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Document getXml() { return iXml; }
	public void setXml(Document xml) { iXml = xml; }

	public String getStatus() { return iStatus; }
	public void setStatus(String status) { iStatus = status; }

	public Date getPostDate() { return iPostDate; }
	public void setPostDate(Date postDate) { iPostDate = postDate; }

	public Date getProcessDate() { return iProcessDate; }
	public void setProcessDate(Date processDate) { iProcessDate = processDate; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof Queue)) return false;
		if (getUniqueId() == null || ((Queue)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Queue)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "Queue["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "Queue[" +
			"\n	PostDate: " + getPostDate() +
			"\n	ProcessDate: " + getProcessDate() +
			"\n	Status: " + getStatus() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Xml: " + getXml() +
			"]";
	}
}
