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

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
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
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSectionToClass;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseColleagueSection implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iColleagueId;
	private Long iCourseOfferingId;
	private Long iSubjectAreaId;
	private String iColleagueCourseNumber;
	private String iSectionIndex;
	private Boolean iDeleted;
	private Long iUniqueIdRolledForwardFrom;

	private Session iSession;
	private ColleagueSection iParentColleagueSection;
	private Set<ColleagueSectionToClass> iColleagueSectionToClasses;
	private Set<ColleagueSection> iColleagueSectionToChildSections;
	private Set<ColleagueRestriction> iRestrictions;

	public BaseColleagueSection() {
	}

	public BaseColleagueSection(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "colleague_section_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "colleague_section_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "colleague_id", nullable = true, length = 20)
	public String getColleagueId() { return iColleagueId; }
	public void setColleagueId(String colleagueId) { iColleagueId = colleagueId; }

	@Column(name = "course_offering_id", nullable = false, length = 20)
	public Long getCourseOfferingId() { return iCourseOfferingId; }
	public void setCourseOfferingId(Long courseOfferingId) { iCourseOfferingId = courseOfferingId; }

	@Column(name = "subject_area_id", nullable = false, length = 20)
	public Long getSubjectAreaId() { return iSubjectAreaId; }
	public void setSubjectAreaId(Long subjectAreaId) { iSubjectAreaId = subjectAreaId; }

	@Column(name = "colleague_crs_nbr", nullable = false, length = 5)
	public String getColleagueCourseNumber() { return iColleagueCourseNumber; }
	public void setColleagueCourseNumber(String colleagueCourseNumber) { iColleagueCourseNumber = colleagueCourseNumber; }

	@Column(name = "section_index", nullable = true, length = 3)
	public String getSectionIndex() { return iSectionIndex; }
	public void setSectionIndex(String sectionIndex) { iSectionIndex = sectionIndex; }

	@Column(name = "deleted", nullable = false)
	public Boolean isDeleted() { return iDeleted; }
	@Transient
	public Boolean getDeleted() { return iDeleted; }
	public void setDeleted(Boolean deleted) { iDeleted = deleted; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "session_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "parent_coll_section_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public ColleagueSection getParentColleagueSection() { return iParentColleagueSection; }
	public void setParentColleagueSection(ColleagueSection parentColleagueSection) { iParentColleagueSection = parentColleagueSection; }

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "colleagueSection", cascade = {CascadeType.ALL}, orphanRemoval = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<ColleagueSectionToClass> getColleagueSectionToClasses() { return iColleagueSectionToClasses; }
	public void setColleagueSectionToClasses(Set<ColleagueSectionToClass> colleagueSectionToClasses) { iColleagueSectionToClasses = colleagueSectionToClasses; }
	public void addToColleagueSectionToClasses(ColleagueSectionToClass colleagueSectionToClass) {
		if (iColleagueSectionToClasses == null) iColleagueSectionToClasses = new HashSet<ColleagueSectionToClass>();
		iColleagueSectionToClasses.add(colleagueSectionToClass);
	}
	@Deprecated
	public void addTocolleagueSectionToClasses(ColleagueSectionToClass colleagueSectionToClass) {
		addToColleagueSectionToClasses(colleagueSectionToClass);
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "parentColleagueSection")
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<ColleagueSection> getColleagueSectionToChildSections() { return iColleagueSectionToChildSections; }
	public void setColleagueSectionToChildSections(Set<ColleagueSection> colleagueSectionToChildSections) { iColleagueSectionToChildSections = colleagueSectionToChildSections; }
	public void addToColleagueSectionToChildSections(ColleagueSection colleagueSection) {
		if (iColleagueSectionToChildSections == null) iColleagueSectionToChildSections = new HashSet<ColleagueSection>();
		iColleagueSectionToChildSections.add(colleagueSection);
	}
	@Deprecated
	public void addTocolleagueSectionToChildSections(ColleagueSection colleagueSection) {
		addToColleagueSectionToChildSections(colleagueSection);
	}

	@ManyToMany
	@JoinTable(name = "colleague_sect_join_restr",
		joinColumns = { @JoinColumn(name = "colleague_section_id") },
		inverseJoinColumns = { @JoinColumn(name = "colleague_restriction_id") })
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Set<ColleagueRestriction> getRestrictions() { return iRestrictions; }
	public void setRestrictions(Set<ColleagueRestriction> restrictions) { iRestrictions = restrictions; }
	public void addToRestrictions(ColleagueRestriction colleagueRestriction) {
		if (iRestrictions == null) iRestrictions = new HashSet<ColleagueRestriction>();
		iRestrictions.add(colleagueRestriction);
	}
	@Deprecated
	public void addTorestrictions(ColleagueRestriction colleagueRestriction) {
		addToRestrictions(colleagueRestriction);
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ColleagueSection)) return false;
		if (getUniqueId() == null || ((ColleagueSection)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ColleagueSection)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ColleagueSection["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ColleagueSection[" +
			"\n	ColleagueCourseNumber: " + getColleagueCourseNumber() +
			"\n	ColleagueId: " + getColleagueId() +
			"\n	CourseOfferingId: " + getCourseOfferingId() +
			"\n	Deleted: " + getDeleted() +
			"\n	ParentColleagueSection: " + getParentColleagueSection() +
			"\n	SectionIndex: " + getSectionIndex() +
			"\n	Session: " + getSession() +
			"\n	SubjectAreaId: " + getSubjectAreaId() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
