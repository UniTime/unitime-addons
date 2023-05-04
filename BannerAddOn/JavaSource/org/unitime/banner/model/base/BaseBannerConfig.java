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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
import org.unitime.banner.model.BannerSection;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.ItypeDesc;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseBannerConfig implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iInstrOfferingConfigId;
	private Long iUniqueIdRolledForwardFrom;
	private Float iLabHours;

	private ItypeDesc iGradableItype;
	private BannerCourse iBannerCourse;
	private Set<BannerSection> iBannerSections;

	public BaseBannerConfig() {
	}

	public BaseBannerConfig(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "banner_config_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "banner_config_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "instr_offr_config_id", nullable = false, length = 20)
	public Long getInstrOfferingConfigId() { return iInstrOfferingConfigId; }
	public void setInstrOfferingConfigId(Long instrOfferingConfigId) { iInstrOfferingConfigId = instrOfferingConfigId; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@Column(name = "lab_hours", nullable = true)
	public Float getLabHours() { return iLabHours; }
	public void setLabHours(Float labHours) { iLabHours = labHours; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "gradable_itype_id", nullable = true)
	public ItypeDesc getGradableItype() { return iGradableItype; }
	public void setGradableItype(ItypeDesc gradableItype) { iGradableItype = gradableItype; }

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "banner_course_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public BannerCourse getBannerCourse() { return iBannerCourse; }
	public void setBannerCourse(BannerCourse bannerCourse) { iBannerCourse = bannerCourse; }

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "bannerConfig", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<BannerSection> getBannerSections() { return iBannerSections; }
	public void setBannerSections(Set<BannerSection> bannerSections) { iBannerSections = bannerSections; }
	public void addToBannerSections(BannerSection bannerSection) {
		if (iBannerSections == null) iBannerSections = new HashSet<BannerSection>();
		iBannerSections.add(bannerSection);
	}
	@Deprecated
	public void addTobannerSections(BannerSection bannerSection) {
		addToBannerSections(bannerSection);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerConfig)) return false;
		if (getUniqueId() == null || ((BannerConfig)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerConfig)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "BannerConfig["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerConfig[" +
			"\n	BannerCourse: " + getBannerCourse() +
			"\n	GradableItype: " + getGradableItype() +
			"\n	InstrOfferingConfigId: " + getInstrOfferingConfigId() +
			"\n	LabHours: " + getLabHours() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
