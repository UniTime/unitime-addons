/*
 * Licensed to The Apereo Foundation under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * The Apereo Foundation licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
*/
package org.unitime.colleague.model.base;

import java.io.Serializable;
import java.util.Date;

import org.dom4j.Document;
import org.unitime.colleague.model.Queue;

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
