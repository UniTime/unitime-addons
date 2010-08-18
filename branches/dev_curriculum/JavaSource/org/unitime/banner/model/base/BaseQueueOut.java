/*
 * UniTime 3.2 (University Timetabling Application)
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
import java.util.Date;

import org.unitime.banner.model.Queue;
import org.unitime.banner.model.QueueOut;

public abstract class BaseQueueOut extends Queue implements Serializable {
	private static final long serialVersionUID = 1L;

	private Date iPickupDate;


	public static String PROP_PICKUPDATE = "pickupDate";

	public BaseQueueOut() {
		initialize();
	}

	public BaseQueueOut(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Date getPickupDate() { return iPickupDate; }
	public void setPickupDate(Date pickupDate) { iPickupDate = pickupDate; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof QueueOut)) return false;
		if (getUniqueId() == null || ((QueueOut)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((QueueOut)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
