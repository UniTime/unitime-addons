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

import org.unitime.banner.model.Queue;
import org.unitime.banner.model.QueueIn;

public abstract class BaseQueueIn extends Queue implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iMatchId;


	public static String PROP_MATCHID = "matchId";

	public BaseQueueIn() {
		initialize();
	}

	public BaseQueueIn(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getMatchId() { return iMatchId; }
	public void setMatchId(Long matchId) { iMatchId = matchId; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof QueueIn)) return false;
		if (getUniqueId() == null || ((QueueIn)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((QueueIn)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "QueueIn["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "QueueIn[" +
			"\n	MatchId: " + getMatchId() +
			"\n	PostDate: " + getPostDate() +
			"\n	ProcessDate: " + getProcessDate() +
			"\n	Status: " + getStatus() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Xml: " + getXml() +
			"]";
	}
}
