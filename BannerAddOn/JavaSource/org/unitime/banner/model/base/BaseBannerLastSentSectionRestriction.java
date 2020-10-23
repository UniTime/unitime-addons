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

import org.unitime.banner.model.BannerLastSentSectionRestriction;
import org.unitime.banner.model.BannerSection;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseBannerLastSentSectionRestriction implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iRestrictionAction;
	private Boolean iRemoved;

	private BannerSection iBannerSection;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_RESTRICTION_ACTION = "restrictionAction";
	public static String PROP_REMOVED = "removed";

	public BaseBannerLastSentSectionRestriction() {
		initialize();
	}

	public BaseBannerLastSentSectionRestriction(Long uniqueId) {
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

	public BannerSection getBannerSection() { return iBannerSection; }
	public void setBannerSection(BannerSection bannerSection) { iBannerSection = bannerSection; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerLastSentSectionRestriction)) return false;
		if (getUniqueId() == null || ((BannerLastSentSectionRestriction)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerLastSentSectionRestriction)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BannerLastSentSectionRestriction["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerLastSentSectionRestriction[" +
			"\n	BannerSection: " + getBannerSection() +
			"\n	Removed: " + getRemoved() +
			"\n	RestrictionAction: " + getRestrictionAction() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
