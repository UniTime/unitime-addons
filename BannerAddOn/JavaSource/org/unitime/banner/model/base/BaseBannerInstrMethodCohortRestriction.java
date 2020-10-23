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

import org.unitime.banner.model.BannerInstrMethodCohortRestriction;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.StudentGroup;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseBannerInstrMethodCohortRestriction implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iRestrictionAction;
	private Boolean iRemoved;

	private Session iSession;
	private InstructionalMethod iInstructionalMethod;
	private StudentGroup iCohort;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_RESTRICTION_ACTION = "restrictionAction";
	public static String PROP_REMOVED = "removed";

	public BaseBannerInstrMethodCohortRestriction() {
		initialize();
	}

	public BaseBannerInstrMethodCohortRestriction(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getRestrictionAction() { return iRestrictionAction; }
	public void setRestrictionAction(String restrictionAction) { iRestrictionAction = restrictionAction; }

	public Boolean isRemoved() { return iRemoved; }
	public Boolean getRemoved() { return iRemoved; }
	public void setRemoved(Boolean removed) { iRemoved = removed; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public InstructionalMethod getInstructionalMethod() { return iInstructionalMethod; }
	public void setInstructionalMethod(InstructionalMethod instructionalMethod) { iInstructionalMethod = instructionalMethod; }

	public StudentGroup getCohort() { return iCohort; }
	public void setCohort(StudentGroup cohort) { iCohort = cohort; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerInstrMethodCohortRestriction)) return false;
		if (getUniqueId() == null || ((BannerInstrMethodCohortRestriction)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerInstrMethodCohortRestriction)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BannerInstrMethodCohortRestriction["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerInstrMethodCohortRestriction[" +
			"\n	Cohort: " + getCohort() +
			"\n	InstructionalMethod: " + getInstructionalMethod() +
			"\n	Removed: " + getRemoved() +
			"\n	RestrictionAction: " + getRestrictionAction() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
