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

import org.unitime.banner.model.BannerCampusOverride;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseBannerCampusOverride implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iBannerCampusCode;
	private String iBannerCampusName;
	private Boolean iVisible;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_BANNER_CAMPUS_CODE = "bannerCampusCode";
	public static String PROP_BANNER_CAMPUS_NAME = "bannerCampusName";
	public static String PROP_VISIBLE = "visible";

	public BaseBannerCampusOverride() {
		initialize();
	}

	public BaseBannerCampusOverride(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getBannerCampusCode() { return iBannerCampusCode; }
	public void setBannerCampusCode(String bannerCampusCode) { iBannerCampusCode = bannerCampusCode; }

	public String getBannerCampusName() { return iBannerCampusName; }
	public void setBannerCampusName(String bannerCampusName) { iBannerCampusName = bannerCampusName; }

	public Boolean isVisible() { return iVisible; }
	public Boolean getVisible() { return iVisible; }
	public void setVisible(Boolean visible) { iVisible = visible; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerCampusOverride)) return false;
		if (getUniqueId() == null || ((BannerCampusOverride)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerCampusOverride)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BannerCampusOverride["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerCampusOverride[" +
			"\n	BannerCampusCode: " + getBannerCampusCode() +
			"\n	BannerCampusName: " + getBannerCampusName() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	Visible: " + getVisible() +
			"]";
	}
}
