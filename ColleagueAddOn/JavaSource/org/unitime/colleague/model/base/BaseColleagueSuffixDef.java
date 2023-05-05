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

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;

import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.colleague.model.ColleagueSuffixDef;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseColleagueSuffixDef implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iTermCode;
	private Integer iItypeId;
	private Long iSubjectAreaId;
	private String iCourseSuffix;
	private String iCampusCode;
	private Integer iMinSectionNum;
	private Integer iMaxSectionNum;
	private String iItypePrefix;
	private String iPrefix;
	private String iSuffix;
	private String iNote;


	public BaseColleagueSuffixDef() {
	}

	public BaseColleagueSuffixDef(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "colleague_course_suffix_def_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "colleague_course_suffix_def_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "term_code", nullable = false, length = 20)
	public String getTermCode() { return iTermCode; }
	public void setTermCode(String termCode) { iTermCode = termCode; }

	@Column(name = "itype_id", nullable = true, length = 2)
	public Integer getItypeId() { return iItypeId; }
	public void setItypeId(Integer itypeId) { iItypeId = itypeId; }

	@Column(name = "subject_area_id", nullable = true)
	public Long getSubjectAreaId() { return iSubjectAreaId; }
	public void setSubjectAreaId(Long subjectAreaId) { iSubjectAreaId = subjectAreaId; }

	@Column(name = "course_suffix", nullable = true, length = 5)
	public String getCourseSuffix() { return iCourseSuffix; }
	public void setCourseSuffix(String courseSuffix) { iCourseSuffix = courseSuffix; }

	@Column(name = "campus_code", nullable = true, length = 20)
	public String getCampusCode() { return iCampusCode; }
	public void setCampusCode(String campusCode) { iCampusCode = campusCode; }

	@Column(name = "min_section_num", nullable = false, length = 2)
	public Integer getMinSectionNum() { return iMinSectionNum; }
	public void setMinSectionNum(Integer minSectionNum) { iMinSectionNum = minSectionNum; }

	@Column(name = "max_section_num", nullable = false, length = 2)
	public Integer getMaxSectionNum() { return iMaxSectionNum; }
	public void setMaxSectionNum(Integer maxSectionNum) { iMaxSectionNum = maxSectionNum; }

	@Column(name = "itype_prefix", nullable = true, length = 1)
	public String getItypePrefix() { return iItypePrefix; }
	public void setItypePrefix(String itypePrefix) { iItypePrefix = itypePrefix; }

	@Column(name = "prefix", nullable = true, length = 1)
	public String getPrefix() { return iPrefix; }
	public void setPrefix(String prefix) { iPrefix = prefix; }

	@Column(name = "suffix", nullable = true, length = 1)
	public String getSuffix() { return iSuffix; }
	public void setSuffix(String suffix) { iSuffix = suffix; }

	@Column(name = "note", nullable = true, length = 500)
	public String getNote() { return iNote; }
	public void setNote(String note) { iNote = note; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ColleagueSuffixDef)) return false;
		if (getUniqueId() == null || ((ColleagueSuffixDef)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ColleagueSuffixDef)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ColleagueSuffixDef["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ColleagueSuffixDef[" +
			"\n	CampusCode: " + getCampusCode() +
			"\n	CourseSuffix: " + getCourseSuffix() +
			"\n	ItypeId: " + getItypeId() +
			"\n	ItypePrefix: " + getItypePrefix() +
			"\n	MaxSectionNum: " + getMaxSectionNum() +
			"\n	MinSectionNum: " + getMinSectionNum() +
			"\n	Note: " + getNote() +
			"\n	Prefix: " + getPrefix() +
			"\n	SubjectAreaId: " + getSubjectAreaId() +
			"\n	Suffix: " + getSuffix() +
			"\n	TermCode: " + getTermCode() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}
}
