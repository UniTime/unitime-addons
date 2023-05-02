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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToMany;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseBannerCourse implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iCourseOfferingId;
	private Long iUniqueIdRolledForwardFrom;

	private Set<BannerConfig> iBannerConfigs;

	public BaseBannerCourse() {
	}

	public BaseBannerCourse(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "banner_course_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "banner_course_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "course_offering_id", nullable = false, length = 20)
	public Long getCourseOfferingId() { return iCourseOfferingId; }
	public void setCourseOfferingId(Long courseOfferingId) { iCourseOfferingId = courseOfferingId; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "bannerCourse", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public Set<BannerConfig> getBannerConfigs() { return iBannerConfigs; }
	public void setBannerConfigs(Set<BannerConfig> bannerConfigs) { iBannerConfigs = bannerConfigs; }
	public void addToBannerConfigs(BannerConfig bannerConfig) {
		if (iBannerConfigs == null) iBannerConfigs = new HashSet<BannerConfig>();
		iBannerConfigs.add(bannerConfig);
	}
	@Deprecated
	public void addTobannerConfigs(BannerConfig bannerConfig) {
		addToBannerConfigs(bannerConfig);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerCourse)) return false;
		if (getUniqueId() == null || ((BannerCourse)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerCourse)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
