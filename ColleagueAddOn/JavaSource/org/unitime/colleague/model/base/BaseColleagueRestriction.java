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
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.commons.annotations.UniqueIdGenerator;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseColleagueRestriction implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iTermCode;
	private String iCode;
	private String iName;
	private String iDescription;


	public BaseColleagueRestriction() {
	}

	public BaseColleagueRestriction(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "pref_group_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "term_code", nullable = false, length = 20)
	public String getTermCode() { return iTermCode; }
	public void setTermCode(String termCode) { iTermCode = termCode; }

	@Column(name = "code", nullable = false, length = 8)
	public String getCode() { return iCode; }
	public void setCode(String code) { iCode = code; }

	@Column(name = "name", nullable = false, length = 30)
	public String getName() { return iName; }
	public void setName(String name) { iName = name; }

	@Column(name = "description", nullable = true, length = 500)
	public String getDescription() { return iDescription; }
	public void setDescription(String description) { iDescription = description; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ColleagueRestriction)) return false;
		if (getUniqueId() == null || ((ColleagueRestriction)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ColleagueRestriction)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ColleagueRestriction["+getUniqueId()+" "+getName()+"]";
	}

	public String toDebugString() {
		return "ColleagueRestriction[" +
			"\n	Code: " + getCode() +
			"\n	Description: " + getDescription() +
			"\n	Name: " + getName() +
			"\n	TermCode: " + getTermCode() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
