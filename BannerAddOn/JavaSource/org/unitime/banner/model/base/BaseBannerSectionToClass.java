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

import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;

public abstract class BaseBannerSectionToClass implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iClassId;

	private BannerSection iBannerSection;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CLASS_ID = "classId";

	public BaseBannerSectionToClass() {
		initialize();
	}

	public BaseBannerSectionToClass(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Long getClassId() { return iClassId; }
	public void setClassId(Long classId) { iClassId = classId; }

	public BannerSection getBannerSection() { return iBannerSection; }
	public void setBannerSection(BannerSection bannerSection) { iBannerSection = bannerSection; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerSectionToClass)) return false;
		if (getUniqueId() == null || ((BannerSectionToClass)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerSectionToClass)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BannerSectionToClass["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerSectionToClass[" +
			"\n	BannerSection: " + getBannerSection() +
			"\n	ClassId: " + getClassId() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
