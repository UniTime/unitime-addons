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
package org.unitime.colleague.model.base;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSectionToClass;
import org.unitime.timetable.model.Session;

public abstract class BaseColleagueSection implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iColleagueId;
	private String iSectionIndex;
	private Boolean iDeleted;
	private Long iCourseOfferingId;
	private Long iSubjectAreaId;
	private Long iUniqueIdRolledForwardFrom;
	private String iColleagueCourseNumber;

	private Session iSession;
	private ColleagueSection iParentColleagueSection;
	private Set<ColleagueSectionToClass> iColleagueSectionToClasses;
	private Set<ColleagueSection> iColleagueSectionToChildSections;
	private Set<ColleagueRestriction> iRestrictions;

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_COLLEAGUE_ID = "colleagueId";
	public static String PROP_SECTION_INDEX = "sectionIndex";
	public static String PROP_DELETED = "deleted";
	public static String PROP_COURSE_OFFERING_ID = "courseOfferingId";
	public static String PROP_SUBJECT_AREA_ID = "subjectAreaId";
	public static String PROP_COLL_CRS_NBR = "colleagueCourseNumber";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	
	public BaseColleagueSection() {
		initialize();
	}

	public BaseColleagueSection(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getColleagueId() { return iColleagueId; }
	public void setColleagueId(String colleagueId) { iColleagueId = colleagueId; }

	public String getSectionIndex() { return iSectionIndex; }
	public void setSectionIndex(String sectionIndex) { iSectionIndex = sectionIndex; }

	public Boolean isDeleted() { return iDeleted; }
	public Boolean getDeleted() { return iDeleted; }
	public void setDeleted(Boolean deleted) { iDeleted = deleted; }

	public Long getCourseOfferingId() { return iCourseOfferingId; }
	public void setCourseOfferingId(Long courseOfferingId) { iCourseOfferingId = courseOfferingId; }

	public Long getSubjectAreaId() { return iSubjectAreaId; }
	public void setSubjectAreaId(Long iSubjectAreaId) { this.iSubjectAreaId = iSubjectAreaId; }

	public String getColleagueCourseNumber() { return iColleagueCourseNumber; }
	public void setColleagueCourseNumber(String iColleagueCourseNumber) { this.iColleagueCourseNumber = iColleagueCourseNumber; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public ColleagueSection getParentColleagueSection() { return iParentColleagueSection; }
	public void setParentColleagueSection(ColleagueSection parentColleagueSection) { iParentColleagueSection = parentColleagueSection; }

	public Set<ColleagueSectionToClass> getColleagueSectionToClasses() { return iColleagueSectionToClasses; }
	public void setColleagueSectionToClasses(Set<ColleagueSectionToClass> colleagueSectionToClasses) { iColleagueSectionToClasses = colleagueSectionToClasses; }
	public void addTocolleagueSectionToClasses(ColleagueSectionToClass colleagueSectionToClass) {
		if (iColleagueSectionToClasses == null) iColleagueSectionToClasses = new HashSet<ColleagueSectionToClass>();
		iColleagueSectionToClasses.add(colleagueSectionToClass);
	}

	public Set<ColleagueSection> getColleagueSectionToChildSections() { return iColleagueSectionToChildSections; }
	public void setColleagueSectionToChildSections(Set<ColleagueSection> colleagueSectionToChildSections) { iColleagueSectionToChildSections = colleagueSectionToChildSections; }
	public void addTocolleagueSectionToChildSections(ColleagueSection colleagueSection) {
		if (iColleagueSectionToChildSections == null) iColleagueSectionToChildSections = new HashSet<ColleagueSection>();
		iColleagueSectionToChildSections.add(colleagueSection);
	}
	
	public Set<ColleagueRestriction> getRestrictions() { return iRestrictions; }
	public void setRestrictions(Set<ColleagueRestriction> restrictions) { iRestrictions = restrictions; }
	public void addTocolleagueRestrictions(ColleagueRestriction colleagueRestriction) {
		if (iRestrictions == null) iRestrictions = new HashSet<ColleagueRestriction>();
		iRestrictions.add(colleagueRestriction);
	}
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ColleagueSection)) return false;
		if (getUniqueId() == null || ((ColleagueSection)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ColleagueSection)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ColleagueSection["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ColleagueSection[" +
			"\n	ColleagueId: " + getColleagueId() +
			"\n	ParentColleagueSection: " + getParentColleagueSection() +
			"\n	SectionIndex: " + getSectionIndex() +
			"\n	Session: " + getSession() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}


}
