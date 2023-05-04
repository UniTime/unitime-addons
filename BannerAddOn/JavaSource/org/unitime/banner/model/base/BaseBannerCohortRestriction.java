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

import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.banner.model.BannerCohortRestriction;
import org.unitime.banner.model.BannerLastSentSectionRestriction;
import org.unitime.timetable.model.StudentGroup;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseBannerCohortRestriction extends BannerLastSentSectionRestriction implements Serializable {
	private static final long serialVersionUID = 1L;

	private StudentGroup iCohort;

	public BaseBannerCohortRestriction() {
	}

	public BaseBannerCohortRestriction(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "cohort_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public StudentGroup getCohort() { return iCohort; }
	public void setCohort(StudentGroup cohort) { iCohort = cohort; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerCohortRestriction)) return false;
		if (getUniqueId() == null || ((BannerCohortRestriction)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerCohortRestriction)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "BannerCohortRestriction["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerCohortRestriction[" +
			"\n	BannerSection: " + getBannerSection() +
			"\n	Cohort: " + getCohort() +
			"\n	Removed: " + getRemoved() +
			"\n	RestrictionAction: " + getRestrictionAction() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
