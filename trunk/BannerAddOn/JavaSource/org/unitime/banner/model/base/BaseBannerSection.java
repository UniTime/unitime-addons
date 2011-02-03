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
import java.util.HashSet;
import java.util.Set;

import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.Session;

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
	private Set<BannerSectionToClass> iBannerSectionToClasses;
	private Set<BannerSection> iBannerSectionToChildSections;

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
