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

public abstract class BaseBannerCourse implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iCourseOfferingId;
	private Long iUniqueIdRolledForwardFrom;

	private Set<BannerConfig> iBannerConfigs;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_COURSE_OFFERING_ID = "courseOfferingId";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BaseBannerCourse() {
		initialize();
	}

	public BaseBannerCourse(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getCourseOfferingId() { return iCourseOfferingId; }
	public void setCourseOfferingId(Long courseOfferingId) { iCourseOfferingId = courseOfferingId; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public Set<BannerConfig> getBannerConfigs() { return iBannerConfigs; }
	public void setBannerConfigs(Set<BannerConfig> bannerConfigs) { iBannerConfigs = bannerConfigs; }
	public void addTobannerConfigs(BannerConfig bannerConfig) {
		if (iBannerConfigs == null) iBannerConfigs = new HashSet<BannerConfig>();
		iBannerConfigs.add(bannerConfig);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerCourse)) return false;
		if (getUniqueId() == null || ((BannerCourse)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerCourse)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BannerCourse["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerCourse[" +
			"\n	CourseOfferingId: " + getCourseOfferingId() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
