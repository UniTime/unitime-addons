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
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseBannerSectionToClass implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Long iClassId;

	private BannerSection iBannerSection;

	public BaseBannerSectionToClass() {
	}

	public BaseBannerSectionToClass(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "banner_section_join_class_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "banner_section_join_class_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "class_id", nullable = false, length = 20)
	public Long getClassId() { return iClassId; }
	public void setClassId(Long classId) { iClassId = classId; }

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "banner_section_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
	public BannerSection getBannerSection() { return iBannerSection; }
	public void setBannerSection(BannerSection bannerSection) { iBannerSection = bannerSection; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerSectionToClass)) return false;
		if (getUniqueId() == null || ((BannerSectionToClass)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerSectionToClass)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
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
