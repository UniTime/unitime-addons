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
import java.util.Date;

import org.unitime.banner.model.BannerResponse;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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


	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_SEQNO = "sequenceNumber";
	public static String PROP_ACTIVITY_DATE = "activityDate";
	public static String PROP_TERM_CODE = "termCode";
	public static String PROP_CRN = "crn";
	public static String PROP_SUBJ_CODE = "subjectCode";
	public static String PROP_CRSE_NUMB = "courseNumber";
	public static String PROP_SEQ_NUMB = "sectionNumber";
	public static String PROP_XLST_GROUP = "xlstGroup";
	public static String PROP_EXTERNAL_ID = "externalId";
	public static String PROP_ACTION = "action";
	public static String PROP_TYPE = "type";
	public static String PROP_MESSAGE = "message";
	public static String PROP_PACKET_ID = "packetId";
	public static String PROP_QUEUE_ID = "queueId";

	public BaseBannerResponse() {
		initialize();
	}

	public BaseBannerResponse(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public Integer getSequenceNumber() { return iSequenceNumber; }
	public void setSequenceNumber(Integer sequenceNumber) { iSequenceNumber = sequenceNumber; }

	public Date getActivityDate() { return iActivityDate; }
	public void setActivityDate(Date activityDate) { iActivityDate = activityDate; }

	public String getTermCode() { return iTermCode; }
	public void setTermCode(String termCode) { iTermCode = termCode; }

	public String getCrn() { return iCrn; }
	public void setCrn(String crn) { iCrn = crn; }

	public String getSubjectCode() { return iSubjectCode; }
	public void setSubjectCode(String subjectCode) { iSubjectCode = subjectCode; }

	public String getCourseNumber() { return iCourseNumber; }
	public void setCourseNumber(String courseNumber) { iCourseNumber = courseNumber; }

	public String getSectionNumber() { return iSectionNumber; }
	public void setSectionNumber(String sectionNumber) { iSectionNumber = sectionNumber; }

	public String getXlstGroup() { return iXlstGroup; }
	public void setXlstGroup(String xlstGroup) { iXlstGroup = xlstGroup; }

	public String getExternalId() { return iExternalId; }
	public void setExternalId(String externalId) { iExternalId = externalId; }

	public String getAction() { return iAction; }
	public void setAction(String action) { iAction = action; }

	public String getType() { return iType; }
	public void setType(String type) { iType = type; }

	public String getMessage() { return iMessage; }
	public void setMessage(String message) { iMessage = message; }

	public String getPacketId() { return iPacketId; }
	public void setPacketId(String packetId) { iPacketId = packetId; }

	public Long getQueueId() { return iQueueId; }
	public void setQueueId(Long queueId) { iQueueId = queueId; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerResponse)) return false;
		if (getUniqueId() == null || ((BannerResponse)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerResponse)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	public String toString() {
		return "BannerResponse["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerResponse[" +
			"\n	Action: " + getAction() +
			"\n	ActivityDate: " + getActivityDate() +
			"\n	CourseNumber: " + getCourseNumber() +
			"\n	Crn: " + getCrn() +
			"\n	ExternalId: " + getExternalId() +
			"\n	Message: " + getMessage() +
			"\n	PacketId: " + getPacketId() +
			"\n	QueueId: " + getQueueId() +
			"\n	SectionNumber: " + getSectionNumber() +
			"\n	SequenceNumber: " + getSequenceNumber() +
			"\n	SubjectCode: " + getSubjectCode() +
			"\n	TermCode: " + getTermCode() +
			"\n	Type: " + getType() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	XlstGroup: " + getXlstGroup() +
			"]";
	}
}
