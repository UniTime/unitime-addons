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
import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerLastSentSectionRestriction;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseBannerSection implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iCrn;
	private String iSectionIndex;
	private String iCrossListIdentifier;
	private String iLinkIdentifier;
	private String iLinkConnector;
	private Long iUniqueIdRolledForwardFrom;
	private Integer iOverrideLimit;
	private Float iOverrideCourseCredit;

	private BannerConfig iBannerConfig;
	private OfferingConsentType iConsentType;
	private Session iSession;
	private BannerSection iParentBannerSection;
	private BannerCampusOverride iBannerCampusOverride;
	private Set<BannerSectionToClass> iBannerSectionToClasses;
	private Set<BannerSection> iBannerSectionToChildSections;
	private Set<BannerLastSentSectionRestriction> iBannerLastSentBannerRestrictions;

	public BaseBannerSection() {
	}

	public BaseBannerSection(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "banner_section_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "banner_section_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "crn", nullable = true, length = 5)
	public Integer getCrn() { return iCrn; }
	public void setCrn(Integer crn) { iCrn = crn; }

	@Column(name = "section_index", nullable = true, length = 10)
	public String getSectionIndex() { return iSectionIndex; }
	public void setSectionIndex(String sectionIndex) { iSectionIndex = sectionIndex; }

	@Column(name = "cross_list_identifier", nullable = true, length = 2)
	public String getCrossListIdentifier() { return iCrossListIdentifier; }
	public void setCrossListIdentifier(String crossListIdentifier) { iCrossListIdentifier = crossListIdentifier; }

	@Column(name = "link_identifier", nullable = true, length = 2)
	public String getLinkIdentifier() { return iLinkIdentifier; }
	public void setLinkIdentifier(String linkIdentifier) { iLinkIdentifier = linkIdentifier; }

	@Column(name = "link_connector", nullable = true, length = 2)
	public String getLinkConnector() { return iLinkConnector; }
	public void setLinkConnector(String linkConnector) { iLinkConnector = linkConnector; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@Column(name = "override_limit", nullable = true, length = 4)
	public Integer getOverrideLimit() { return iOverrideLimit; }
	public void setOverrideLimit(Integer overrideLimit) { iOverrideLimit = overrideLimit; }

	@Column(name = "override_course_credit", nullable = true)
	public Float getOverrideCourseCredit() { return iOverrideCourseCredit; }
	public void setOverrideCourseCredit(Float overrideCourseCredit) { iOverrideCourseCredit = overrideCourseCredit; }

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "banner_config_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public BannerConfig getBannerConfig() { return iBannerConfig; }
	public void setBannerConfig(BannerConfig bannerConfig) { iBannerConfig = bannerConfig; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "consent_type_id", nullable = true)
	public OfferingConsentType getConsentType() { return iConsentType; }
	public void setConsentType(OfferingConsentType consentType) { iConsentType = consentType; }

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "session_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_banner_section_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public BannerSection getParentBannerSection() { return iParentBannerSection; }
	public void setParentBannerSection(BannerSection parentBannerSection) { iParentBannerSection = parentBannerSection; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "banner_campus_override_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public BannerCampusOverride getBannerCampusOverride() { return iBannerCampusOverride; }
	public void setBannerCampusOverride(BannerCampusOverride bannerCampusOverride) { iBannerCampusOverride = bannerCampusOverride; }

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "bannerSection", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<BannerSectionToClass> getBannerSectionToClasses() { return iBannerSectionToClasses; }
	public void setBannerSectionToClasses(Set<BannerSectionToClass> bannerSectionToClasses) { iBannerSectionToClasses = bannerSectionToClasses; }
	public void addToBannerSectionToClasses(BannerSectionToClass bannerSectionToClass) {
		if (iBannerSectionToClasses == null) iBannerSectionToClasses = new HashSet<BannerSectionToClass>();
		iBannerSectionToClasses.add(bannerSectionToClass);
	}
	@Deprecated
	public void addTobannerSectionToClasses(BannerSectionToClass bannerSectionToClass) {
		addToBannerSectionToClasses(bannerSectionToClass);
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parentBannerSection")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<BannerSection> getBannerSectionToChildSections() { return iBannerSectionToChildSections; }
	public void setBannerSectionToChildSections(Set<BannerSection> bannerSectionToChildSections) { iBannerSectionToChildSections = bannerSectionToChildSections; }
	public void addToBannerSectionToChildSections(BannerSection bannerSection) {
		if (iBannerSectionToChildSections == null) iBannerSectionToChildSections = new HashSet<BannerSection>();
		iBannerSectionToChildSections.add(bannerSection);
	}
	@Deprecated
	public void addTobannerSectionToChildSections(BannerSection bannerSection) {
		addToBannerSectionToChildSections(bannerSection);
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "bannerSection", cascade = {CascadeType.ALL})
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<BannerLastSentSectionRestriction> getBannerLastSentBannerRestrictions() { return iBannerLastSentBannerRestrictions; }
	public void setBannerLastSentBannerRestrictions(Set<BannerLastSentSectionRestriction> bannerLastSentBannerRestrictions) { iBannerLastSentBannerRestrictions = bannerLastSentBannerRestrictions; }
	public void addToBannerLastSentBannerRestrictions(BannerLastSentSectionRestriction bannerLastSentSectionRestriction) {
		if (iBannerLastSentBannerRestrictions == null) iBannerLastSentBannerRestrictions = new HashSet<BannerLastSentSectionRestriction>();
		iBannerLastSentBannerRestrictions.add(bannerLastSentSectionRestriction);
	}
	@Deprecated
	public void addTobannerLastSentBannerRestrictions(BannerLastSentSectionRestriction bannerLastSentSectionRestriction) {
		addToBannerLastSentBannerRestrictions(bannerLastSentSectionRestriction);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerSection)) return false;
		if (getUniqueId() == null || ((BannerSection)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerSection)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "BannerSection["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerSection[" +
			"\n	BannerCampusOverride: " + getBannerCampusOverride() +
			"\n	BannerConfig: " + getBannerConfig() +
			"\n	ConsentType: " + getConsentType() +
			"\n	Crn: " + getCrn() +
			"\n	CrossListIdentifier: " + getCrossListIdentifier() +
			"\n	LinkConnector: " + getLinkConnector() +
			"\n	LinkIdentifier: " + getLinkIdentifier() +
			"\n	OverrideCourseCredit: " + getOverrideCourseCredit() +
			"\n	OverrideLimit: " + getOverrideLimit() +
			"\n	ParentBannerSection: " + getParentBannerSection() +
			"\n	SectionIndex: " + getSectionIndex() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
