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
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.banner.model.BannerResponse;
import org.unitime.banner.model.BannerSection;
import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseBannerResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private Integer iSequenceNumber;
	private Date iActivityDate;
	private String iTermCode;
	private String iCrn;
	private String iSubjectCode;
	private String iCourseNumber;
	private String iSectionNumber;
	private String iXlstGroup;
	private String iExternalId;
	private String iAction;
	private String iType;
	private String iMessage;
	private String iPacketId;
	private Long iQueueId;
	private String iCampus;

	private Session iSession;
	private BannerSection iBannerSection;
	private SubjectArea iSubjectArea;

	public BaseBannerResponse() {
	}

	public BaseBannerResponse(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "banner_response_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "seqno", nullable = false, length = 20)
	public Integer getSequenceNumber() { return iSequenceNumber; }
	public void setSequenceNumber(Integer sequenceNumber) { iSequenceNumber = sequenceNumber; }

	@Column(name = "activity_date", nullable = false, length = 20)
	public Date getActivityDate() { return iActivityDate; }
	public void setActivityDate(Date activityDate) { iActivityDate = activityDate; }

	@Column(name = "term_code", nullable = true, length = 6)
	public String getTermCode() { return iTermCode; }
	public void setTermCode(String termCode) { iTermCode = termCode; }

	@Column(name = "crn", nullable = true, length = 5)
	public String getCrn() { return iCrn; }
	public void setCrn(String crn) { iCrn = crn; }

	@Column(name = "subj_code", nullable = true, length = 4)
	public String getSubjectCode() { return iSubjectCode; }
	public void setSubjectCode(String subjectCode) { iSubjectCode = subjectCode; }

	@Column(name = "crse_numb", nullable = true, length = 5)
	public String getCourseNumber() { return iCourseNumber; }
	public void setCourseNumber(String courseNumber) { iCourseNumber = courseNumber; }

	@Column(name = "seq_numb", nullable = true, length = 10)
	public String getSectionNumber() { return iSectionNumber; }
	public void setSectionNumber(String sectionNumber) { iSectionNumber = sectionNumber; }

	@Column(name = "xlst_group", nullable = true, length = 2)
	public String getXlstGroup() { return iXlstGroup; }
	public void setXlstGroup(String xlstGroup) { iXlstGroup = xlstGroup; }

	@Column(name = "external_id", nullable = true, length = 50)
	public String getExternalId() { return iExternalId; }
	public void setExternalId(String externalId) { iExternalId = externalId; }

	@Column(name = "action", nullable = true, length = 50)
	public String getAction() { return iAction; }
	public void setAction(String action) { iAction = action; }

	@Column(name = "type", nullable = true, length = 50)
	public String getType() { return iType; }
	public void setType(String type) { iType = type; }

	@Column(name = "message", nullable = false, length = 4000)
	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	@Column(name = "packet_id", nullable = false, length = 500)
	public String getPacketId() { return iPacketId; }
	public void setPacketId(String packetId) { iPacketId = packetId; }

	@Column(name = "queue_id", nullable = false, length = 20)
	public Long getQueueId() { return iQueueId; }
	public void setQueueId(Long queueId) { iQueueId = queueId; }

	@Column(name = "campus", nullable = true, length = 5)
	public String getCampus() { return iCampus; }
	public void setCampus(String campus) { iCampus = campus; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "session_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "banner_section_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public BannerSection getBannerSection() { return iBannerSection; }
	public void setBannerSection(BannerSection bannerSection) { iBannerSection = bannerSection; }

	@ManyToOne(optional = true, fetch = FetchType.EAGER)
	@JoinColumn(name = "subject_area_id", nullable = true)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public SubjectArea getSubjectArea() { return iSubjectArea; }
	public void setSubjectArea(SubjectArea subjectArea) { iSubjectArea = subjectArea; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerResponse)) return false;
		if (getUniqueId() == null || ((BannerResponse)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerResponse)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "BannerResponse["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerResponse[" +
			"\n	Action: " + getAction() +
			"\n	ActivityDate: " + getActivityDate() +
			"\n	BannerSection: " + getBannerSection() +
			"\n	Campus: " + getCampus() +
			"\n	CourseNumber: " + getCourseNumber() +
			"\n	Crn: " + getCrn() +
			"\n	ExternalId: " + getExternalId() +
			"\n	Message: " + getMessage() +
			"\n	PacketId: " + getPacketId() +
			"\n	QueueId: " + getQueueId() +
			"\n	SectionNumber: " + getSectionNumber() +
			"\n	SequenceNumber: " + getSequenceNumber() +
			"\n	Session: " + getSession() +
			"\n	SubjectArea: " + getSubjectArea() +
			"\n	SubjectCode: " + getSubjectCode() +
			"\n	TermCode: " + getTermCode() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	XlstGroup: " + getXlstGroup() +
			"]";
	}
}
