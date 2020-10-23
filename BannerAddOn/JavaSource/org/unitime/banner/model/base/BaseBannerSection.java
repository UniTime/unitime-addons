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
import java.util.HashSet;
import java.util.Set;

import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerLastSentSectionRestriction;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_CRN = "crn";
	public static String PROP_SECTION_INDEX = "sectionIndex";
	public static String PROP_CROSS_LIST_IDENTIFIER = "crossListIdentifier";
	public static String PROP_LINK_IDENTIFIER = "linkIdentifier";
	public static String PROP_LINK_CONNECTOR = "linkConnector";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";
	public static String PROP_OVERRIDE_LIMIT = "overrideLimit";
	public static String PROP_OVERRIDE_COURSE_CREDIT = "overrideCourseCredit";

	public BaseBannerSection() {
		initialize();
	}

	public BaseBannerSection(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getCrn() { return iCrn; }
	public void setCrn(Integer crn) { iCrn = crn; }

	public String getSectionIndex() { return iSectionIndex; }
	public void setSectionIndex(String sectionIndex) { iSectionIndex = sectionIndex; }

	public String getCrossListIdentifier() { return iCrossListIdentifier; }
	public void setCrossListIdentifier(String crossListIdentifier) { iCrossListIdentifier = crossListIdentifier; }

	public String getLinkIdentifier() { return iLinkIdentifier; }
	public void setLinkIdentifier(String linkIdentifier) { iLinkIdentifier = linkIdentifier; }

	public String getLinkConnector() { return iLinkConnector; }
	public void setLinkConnector(String linkConnector) { iLinkConnector = linkConnector; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public Integer getOverrideLimit() { return iOverrideLimit; }
	public void setOverrideLimit(Integer overrideLimit) { iOverrideLimit = overrideLimit; }

	public Float getOverrideCourseCredit() { return iOverrideCourseCredit; }
	public void setOverrideCourseCredit(Float overrideCourseCredit) { iOverrideCourseCredit = overrideCourseCredit; }

	public BannerConfig getBannerConfig() { return iBannerConfig; }
	public void setBannerConfig(BannerConfig bannerConfig) { iBannerConfig = bannerConfig; }

	public OfferingConsentType getConsentType() { return iConsentType; }
	public void setConsentType(OfferingConsentType consentType) { iConsentType = consentType; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public BannerSection getParentBannerSection() { return iParentBannerSection; }
	public void setParentBannerSection(BannerSection parentBannerSection) { iParentBannerSection = parentBannerSection; }

	public BannerCampusOverride getBannerCampusOverride() { return iBannerCampusOverride; }
	public void setBannerCampusOverride(BannerCampusOverride bannerCampusOverride) { iBannerCampusOverride = bannerCampusOverride; }

	public Set<BannerSectionToClass> getBannerSectionToClasses() { return iBannerSectionToClasses; }
	public void setBannerSectionToClasses(Set<BannerSectionToClass> bannerSectionToClasses) { iBannerSectionToClasses = bannerSectionToClasses; }
	public void addTobannerSectionToClasses(BannerSectionToClass bannerSectionToClass) {
		if (iBannerSectionToClasses == null) iBannerSectionToClasses = new HashSet<BannerSectionToClass>();
		iBannerSectionToClasses.add(bannerSectionToClass);
	}

	public Set<BannerSection> getBannerSectionToChildSections() { return iBannerSectionToChildSections; }
	public void setBannerSectionToChildSections(Set<BannerSection> bannerSectionToChildSections) { iBannerSectionToChildSections = bannerSectionToChildSections; }
	public void addTobannerSectionToChildSections(BannerSection bannerSection) {
		if (iBannerSectionToChildSections == null) iBannerSectionToChildSections = new HashSet<BannerSection>();
		iBannerSectionToChildSections.add(bannerSection);
	}

	public Set<BannerLastSentSectionRestriction> getBannerLastSentBannerRestrictions() { return iBannerLastSentBannerRestrictions; }
	public void setBannerLastSentBannerRestrictions(Set<BannerLastSentSectionRestriction> bannerLastSentBannerRestrictions) { iBannerLastSentBannerRestrictions = bannerLastSentBannerRestrictions; }
	public void addTobannerLastSentBannerRestrictions(BannerLastSentSectionRestriction bannerLastSentSectionRestriction) {
		if (iBannerLastSentBannerRestrictions == null) iBannerLastSentBannerRestrictions = new HashSet<BannerLastSentSectionRestriction>();
		iBannerLastSentBannerRestrictions.add(bannerLastSentSectionRestriction);
	}

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerSection)) return false;
		if (getUniqueId() == null || ((BannerSection)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerSection)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
