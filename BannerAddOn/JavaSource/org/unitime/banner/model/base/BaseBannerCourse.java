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
