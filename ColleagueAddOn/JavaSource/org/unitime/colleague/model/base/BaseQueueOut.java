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

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.unitime.colleague.model.Queue;
import org.unitime.colleague.model.QueueOut;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseQueueOut extends Queue implements Serializable {
	private static final long serialVersionUID = 1L;

	private Date iPickupDate;


	public BaseQueueOut() {
	}

	public BaseQueueOut(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Column(name = "pickupdate", nullable = true)
	public Date getPickupDate() { return iPickupDate; }
	public void setPickupDate(Date pickupDate) { iPickupDate = pickupDate; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof QueueOut)) return false;
		if (getUniqueId() == null || ((QueueOut)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((QueueOut)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "QueueOut["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "QueueOut[" +
			"\n	PickupDate: " + getPickupDate() +
			"\n	PostDate: " + getPostDate() +
			"\n	ProcessDate: " + getProcessDate() +
			"\n	Status: " + getStatus() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Xml: " + getXml() +
			"]";
	}
}
