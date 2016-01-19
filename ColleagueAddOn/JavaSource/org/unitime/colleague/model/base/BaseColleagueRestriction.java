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

import org.unitime.colleague.model.ColleagueRestriction;


public abstract class BaseColleagueRestriction implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iTermCode;
	private String iCode;
	private String iName;
	private String iDescription;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TERM_CODE = "termCode";
	public static String PROP_CODE = "code";
	public static String PROP_NAME = "name";
	public static String PROP_DESCRIPTION = "description";

	public BaseColleagueRestriction() {
		initialize();
	}

	public BaseColleagueRestriction(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getTermCode() { return iTermCode; }
	public void setTermCode(String termCode) { iTermCode = termCode; }

	public String getCode() { return iCode; }
	public void setCode(String code) { iCode = code; }

	public String getName() { return iName; }
	public void setName(String name) { this.iName = name; }

	public String getDescription() { return iDescription; }
	public void setDescription(String description) { this.iDescription = description; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ColleagueRestriction)) return false;
		if (getUniqueId() == null || ((ColleagueRestriction)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ColleagueRestriction)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ColleagueRestriction["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ColleagueRestriction[" +
			"\n	TermCode: " + getCode() +
			"\n	Code: " + getCode() +
			"\n	Name: " + getName() +
			"\n	Description: " + getDescription() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}

}
