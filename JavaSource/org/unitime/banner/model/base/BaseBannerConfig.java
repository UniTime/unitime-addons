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
import java.util.HashSet;
import java.util.Set;

import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.timetable.model.ItypeDesc;

public abstract class BaseBannerConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iInstrOfferingConfigId;
	private Long iUniqueIdRolledForwardFrom;

	private ItypeDesc iGradableItype;
	private BannerCourse iBannerCourse;
	private Set<BannerSection> iBannerSections;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_INSTR_OFFR_CONFIG_ID = "instrOfferingConfigId";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

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
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
