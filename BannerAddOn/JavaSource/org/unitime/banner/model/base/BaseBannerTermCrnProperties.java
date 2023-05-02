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

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Transient;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.BannerTermCrnProperties;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseBannerTermCrnProperties implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iBannerTermCode;
	private Integer iLastCrn;
	private Boolean iSearchFlag;
	private Integer iMinCrn;
	private Integer iMaxCrn;

	private Set<BannerSession> iBannerSessions;

	public BaseBannerTermCrnProperties() {
	}

	public BaseBannerTermCrnProperties(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "banner_crn_provider_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "banner_crn_provider_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "term_code", nullable = false, length = 20)
	public String getBannerTermCode() { return iBannerTermCode; }
	public void setBannerTermCode(String bannerTermCode) { iBannerTermCode = bannerTermCode; }

	@Column(name = "last_crn", nullable = false)
	public Integer getLastCrn() { return iLastCrn; }
	public void setLastCrn(Integer lastCrn) { iLastCrn = lastCrn; }

	@Column(name = "search_flag", nullable = false)
	public Boolean isSearchFlag() { return iSearchFlag; }
	@Transient
	public Boolean getSearchFlag() { return iSearchFlag; }
	public void setSearchFlag(Boolean searchFlag) { iSearchFlag = searchFlag; }

	@Column(name = "min_crn", nullable = false)
	public Integer getMinCrn() { return iMinCrn; }
	public void setMinCrn(Integer minCrn) { iMinCrn = minCrn; }

	@Column(name = "max_crn", nullable = false)
	public Integer getMaxCrn() { return iMaxCrn; }
	public void setMaxCrn(Integer maxCrn) { iMaxCrn = maxCrn; }

	@OneToMany(fetch = FetchType.LAZY, mappedBy = "bannerTermCrnProperties")
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<BannerSession> getBannerSessions() { return iBannerSessions; }
	public void setBannerSessions(Set<BannerSession> bannerSessions) { iBannerSessions = bannerSessions; }
	public void addToBannerSessions(BannerSession bannerSession) {
		if (iBannerSessions == null) iBannerSessions = new HashSet<BannerSession>();
		iBannerSessions.add(bannerSession);
	}
	@Deprecated
	public void addTobannerSessions(BannerSession bannerSession) {
		addToBannerSessions(bannerSession);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerTermCrnProperties)) return false;
		if (getUniqueId() == null || ((BannerTermCrnProperties)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerTermCrnProperties)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
