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
package org.unitime.banner.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.dom4j.Document;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.banner.model.Queue;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseQueue implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Document iXml;
	private String iStatus;
	private Date iPostDate;
	private Date iProcessDate;


	public BaseQueue() {
	}

	public BaseQueue(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "null_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "queue_seq")
	})
	@GeneratedValue(generator = "null_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "xml", nullable = false)
	public Document getXml() { return iXml; }
	public void setXml(Document xml) { iXml = xml; }

	@Column(name = "status", nullable = true, length = 10)
	public String getStatus() { return iStatus; }
	public void setStatus(String status) { iStatus = status; }

	@Column(name = "postdate", nullable = true)
	public Date getPostDate() { return iPostDate; }
	public void setPostDate(Date postDate) { iPostDate = postDate; }

	@Column(name = "processdate", nullable = true)
	public Date getProcessDate() { return iProcessDate; }
	public void setProcessDate(Date processDate) { iProcessDate = processDate; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Queue)) return false;
		if (getUniqueId() == null || ((Queue)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((Queue)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
