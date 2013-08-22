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

import org.unitime.banner.model.BannerCampusOverride;

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
			"\n	Visible: " + getVisible() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
