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
import java.util.HashSet;
import java.util.Set;

import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.timetable.model.ItypeDesc;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseBannerConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iInstrOfferingConfigId;
	private Long iUniqueIdRolledForwardFrom;
	private Float iLabHours;

	private ItypeDesc iGradableItype;
	private BannerCourse iBannerCourse;
	private Set<BannerSection> iBannerSections;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_INSTR_OFFR_CONFIG_ID = "instrOfferingConfigId";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_LAB_HOURS = "labHours";

	public BaseBannerConfig() {
		initialize();
	}

	public BaseBannerConfig(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getInstrOfferingConfigId() { return iInstrOfferingConfigId; }
	public void setInstrOfferingConfigId(Long instrOfferingConfigId) { iInstrOfferingConfigId = instrOfferingConfigId; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public Float getLabHours() { return iLabHours; }
	public void setLabHours(Float labHours) { iLabHours = labHours; }

	public ItypeDesc getGradableItype() { return iGradableItype; }
	public void setGradableItype(ItypeDesc gradableItype) { iGradableItype = gradableItype; }

	public BannerCourse getBannerCourse() { return iBannerCourse; }
	public void setBannerCourse(BannerCourse bannerCourse) { iBannerCourse = bannerCourse; }

	public Set<BannerSection> getBannerSections() { return iBannerSections; }
	public void setBannerSections(Set<BannerSection> bannerSections) { iBannerSections = bannerSections; }
	public void addTobannerSections(BannerSection bannerSection) {
		if (iBannerSections == null) iBannerSections = new HashSet<BannerSection>();
		iBannerSections.add(bannerSection);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerConfig)) return false;
		if (getUniqueId() == null || ((BannerConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerConfig)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BannerConfig["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerConfig[" +
			"\n	BannerCourse: " + getBannerCourse() +
			"\n	GradableItype: " + getGradableItype() +
			"\n	InstrOfferingConfigId: " + getInstrOfferingConfigId() +
			"\n	LabHours: " + getLabHours() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
