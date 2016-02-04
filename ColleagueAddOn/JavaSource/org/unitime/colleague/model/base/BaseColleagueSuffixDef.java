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

import org.unitime.colleague.model.ColleagueSuffixDef;


public abstract class BaseColleagueSuffixDef implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iTermCode;
	private Long iSubjectAreaId;
	private Integer iItypeId;
	private String iCourseSuffix;
	private String iCampusCode;
	private Integer iMinSectionNum;
	private Integer iMaxSectionNum;
	private String iItypePrefix;
	private String iSuffix;
	private String iPrefix;
	private String iNote;


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_TERM_CODE = "termCode";
	public static String PROP_SUBJECT_AREA_ID = "iSubjectAreaId";
	public static String PROP_ITYPE_ID = "itypeId";
	public static String PROP_COURSE_SUFFIX = "courseSuffix";
	public static String PROP_CAMPUS_CODE = "campusCode";
	public static String PROP_MIN_SECTION_NUM = "minSectionNum";
	public static String PROP_MAX_SECTION_NUM = "maxSectionNum";
	public static String PROP_ITYPE_PREFIX = "itypePrefix";
	public static String PROP_PREFIX = "prefix";
	public static String PROP_SUFFIX = "suffix";

	public BaseColleagueSuffixDef() {
		initialize();
	}

	public BaseColleagueSuffixDef(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getTermCode() { return iTermCode; }
	public void setTermCode(String termCode) { iTermCode = termCode; }

	public Long getSubjectAreaId() { return iSubjectAreaId; }
	public void setSubjectAreaId(Long subjectAreaId) { iSubjectAreaId = subjectAreaId; }

	public Integer getItypeId() { return iItypeId; }
	public void setItypeId(Integer itypeId) { iItypeId = itypeId; }

	public String getCourseSuffix() { return iCourseSuffix; }
	public void setCourseSuffix(String courseSuffix) { iCourseSuffix = courseSuffix; }

	public String getCampusCode() { return iCampusCode; }
	public void setCampusCode(String campusCode) { this.iCampusCode = campusCode; }

	public Integer getMinSectionNum() { return iMinSectionNum; }
	public void setMinSectionNum(Integer minSectionNum) { iMinSectionNum = minSectionNum; }

	public Integer getMaxSectionNum() { return iMaxSectionNum; }
	public void setMaxSectionNum(Integer maxSectionNum) { iMaxSectionNum = maxSectionNum; }

	public String getItypePrefix() { return iItypePrefix; }
	public void setItypePrefix(String itypePrefix) { this.iItypePrefix = itypePrefix; }

	public String getPrefix() { return iPrefix; }
	public void setPrefix(String prefix) { this.iPrefix = prefix; }

	public String getSuffix() { return iSuffix; }
	public void setSuffix(String suffix) { this.iSuffix = suffix; }

	public String getNote() { return iNote; }
	public void setNote(String note) { this.iNote = note; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ColleagueSuffixDef)) return false;
		if (getUniqueId() == null || ((ColleagueSuffixDef)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ColleagueSuffixDef)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "ColleagueSuffixDef["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ColleagueSuffixDef[" +
			"\n	TermCode: " + getTermCode() +
			"\n	SubjectAreaId: " + getSubjectAreaId() +
			"\n	ItypeId: " + getItypeId() +
			"\n	CourseSuffix: " + getCourseSuffix() +
			"\n	CampusCode: " + getCampusCode() +
			"\n	MinSectionNum: " + getMinSectionNum() +
			"\n	MaxSectionNum: " + getMaxSectionNum() +
			"\n	ItypePrefix: " + getItypePrefix() +
			"\n	Prefix: " + getPrefix() +
			"\n	Suffix: " + getSuffix() +
			"\n	Note: " + getNote() +
			"\n	UniqueId: " + getUniqueId() +
			"]";
	}

}
