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
