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

import org.unitime.banner.model.BannerTermCrnProperties;

public abstract class BaseBannerTermCrnProperties implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iBannerTermCode;
	private Integer iLastCrn;
	private Boolean iSearchFlag;
	private Integer iMinCrn;
	private Integer iMaxCrn;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TERM_CODE = "bannerTermCode";
	public static String PROP_LAST_CRN = "lastCrn";
	public static String PROP_SEARCH_FLAG = "searchFlag";
	public static String PROP_MIN_CRN = "minCrn";
	public static String PROP_MAX_CRN = "maxCrn";

	public BaseBannerTermCrnProperties() {
		initialize();
	}

	public BaseBannerTermCrnProperties(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getBannerTermCode() { return iBannerTermCode; }
	public void setBannerTermCode(String bannerTermCode) { iBannerTermCode = bannerTermCode; }

	public Integer getLastCrn() { return iLastCrn; }
	public void setLastCrn(Integer lastCrn) { iLastCrn = lastCrn; }

	public Boolean isSearchFlag() { return iSearchFlag; }
	public Boolean getSearchFlag() { return iSearchFlag; }
	public void setSearchFlag(Boolean searchFlag) { iSearchFlag = searchFlag; }

	public Integer getMinCrn() { return iMinCrn; }
	public void setMinCrn(Integer minCrn) { iMinCrn = minCrn; }

	public Integer getMaxCrn() { return iMaxCrn; }
	public void setMaxCrn(Integer maxCrn) { iMaxCrn = maxCrn; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerTermCrnProperties)) return false;
		if (getUniqueId() == null || ((BannerTermCrnProperties)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerTermCrnProperties)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BannerTermCrnProperties["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerTermCrnProperties[" +
			"\n	BannerTermCode: " + getBannerTermCode() +
			"\n	LastCrn: " + getLastCrn() +
			"\n	MaxCrn: " + getMaxCrn() +
			"\n	MinCrn: " + getMinCrn() +
			"\n	SearchFlag: " + getSearchFlag() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
