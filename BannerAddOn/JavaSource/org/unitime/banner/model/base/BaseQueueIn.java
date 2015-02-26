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
