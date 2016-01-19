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

import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSectionToClass;

public abstract class BaseColleagueSectionToClass implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iClassId;

	private ColleagueSection iColleagueSection;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CLASS_ID = "classId";

	public BaseColleagueSectionToClass() {
		initialize();
	}

	public BaseColleagueSectionToClass(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getClassId() { return iClassId; }
	public void setClassId(Long classId) { iClassId = classId; }

	public ColleagueSection getColleagueSection() { return iColleagueSection; }
	public void setColleagueSection(ColleagueSection colleagueSection) { iColleagueSection = colleagueSection; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ColleagueSectionToClass)) return false;
		if (getUniqueId() == null || ((ColleagueSectionToClass)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ColleagueSectionToClass)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ColleagueSectionToClass["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ColleagueSectionToClass[" +
			"\n	ColleagueSection: " + getColleagueSection() +
			"\n	ClassId: " + getClassId() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
