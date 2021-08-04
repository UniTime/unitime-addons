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
package org.unitime.timetable.gwt.shared;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.SubjectAreaInterface;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BannerResponseInterface implements IsSerializable, Comparable<BannerResponseInterface> {

	private Long iUniqueId;
	private Integer iSequenceNumber;
	private String iActivityDateStr;
	private String iTermCode;
	private String iCrn;
	private String iCampus;
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

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { this.iUniqueId = uniqueId; }

	public Integer getSequenceNumber() { return iSequenceNumber; }
	public void setSequenceNumber(Integer sequenceNumber) { this.iSequenceNumber = sequenceNumber; }

	public String getActivityDateStr() { return iActivityDateStr; }
	public void setActivityDateStr(String activityDateStr) { this.iActivityDateStr = activityDateStr; }

	public String getTermCode() { return iTermCode; }
	public void setTermCode(String termCode) { this.iTermCode = termCode; }

	public String getCrn() { return iCrn; }
	public void setCrn(String crn) { this.iCrn = crn; }

	public String getSubjectCode() { return iSubjectCode; }
	public void setSubjectCode(String subjectCode) { this.iSubjectCode = subjectCode; }

	public String getCourseNumber() { return iCourseNumber;}
	public void setCourseNumber(String courseNumber) { this.iCourseNumber = courseNumber; }

	public String getSectionNumber() { return iSectionNumber; }
	public void setSectionNumber(String sectionNumber) { this.iSectionNumber = sectionNumber;}

	public String getXlstGroup() { return iXlstGroup; }
	public void setXlstGroup(String xlstGroup) { this.iXlstGroup = xlstGroup; }

	public String getExternalId() { return iExternalId; }
	public void setExternalId(String externalId) { this.iExternalId = externalId; }

	public String getAction() { return iAction; }
	public void setAction(String action) { this.iAction = action; }

	public String getType() { return iType; }
	public void setType(String type) { this.iType = type; }

	public String getMessage() { return iMessage; }
	public void setMessage(String message) { this.iMessage = message; }

	public String getPacketId() { return iPacketId; }
	public void setPacketId(String packetId) { this.iPacketId = packetId; }

	public Long getQueueId() { return iQueueId;}
	public void setQueueId(Long queueId) { this.iQueueId = queueId; }

	public String getCampus() {
		return iCampus;
	}
	public void setCampus(String campus) {
		this.iCampus = campus;
	}
	public BannerResponseInterface() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(BannerResponseInterface o) {
		int cmp = getPacketId().compareTo(o.getPacketId());
		if (cmp != 0) {
			return cmp;
		}
		return(getUniqueId().compareTo(o.getUniqueId()));
	}
	
	public static class BannerResponsesFilterRpcRequest extends FilterRpcRequest {
		private static final long serialVersionUID = 1L;
		
		public BannerResponsesFilterRpcRequest() {}
	}

	public static class BannerResponsesPageRequest implements GwtRpcRequest<GwtRpcResponseList<BannerResponseInterface>>, Serializable {
		private static final long serialVersionUID = 1L;
		private BannerResponsesFilterRpcRequest iRequest = null;
		
		public BannerResponsesPageRequest() {
			iRequest = new BannerResponsesFilterRpcRequest();
		}
		
		public BannerResponsesPageRequest(BannerResponsesFilterRpcRequest request) {
			iRequest = request;
		}
		
		public BannerResponsesPageRequest(Long offeringId) {
			iRequest = new BannerResponsesFilterRpcRequest();
			if (offeringId != null)
				iRequest.setOption("offeringId", offeringId.toString());
		}
		
		public BannerResponsesFilterRpcRequest getFilter() { return iRequest; }
		
		@Override
		public String toString() {
			return iRequest.toString();
		}
	}

	public static class BannerResponsesPagePropertiesRequest implements GwtRpcRequest<BannerResponsesPagePropertiesResponse>, Serializable {
		private static final long serialVersionUID = 1L;
		public BannerResponsesPagePropertiesRequest() {}
	}
	
	public static class BannerResponsesPagePropertiesResponse implements GwtRpcResponse, Serializable {
		private static final long serialVersionUID = 1L;
		private List<SubjectAreaInterface> iSubjecAreas = new ArrayList<SubjectAreaInterface>();
		private List<DepartmentInterface> iDepartments = new ArrayList<DepartmentInterface>();
		private Long iLastSubjectAreaId = null, iLastDepartmentId = null;
		private Boolean iShowAuditActions = null;
		private Boolean iShowUpdateActions = null;
		private Boolean iShowDeleteActions = null;
		private Boolean iShowSuccessResults = null;
		private Boolean iShowWarningResults = null;
		private Boolean iShowErrorResults = null;
		private Boolean iShowNoChangeResults = null;
		private Boolean iCanSelectDepartment = null;
		private Boolean iCanSelectManager = null;
		private String iMessageSearchString = null;
		private String iCrosslistSearchString = null;
		private String iCrnSearchString = null;
		
		public BannerResponsesPagePropertiesResponse() {}
		
		public void addSubjectArea(SubjectAreaInterface subjectArea) { iSubjecAreas.add(subjectArea); }
		public List<SubjectAreaInterface> getSubjectAreas() { return iSubjecAreas; }
		
		public void addDepartment(DepartmentInterface department) { iDepartments.add(department); }
		public List<DepartmentInterface> getDepartments() { return iDepartments; }

		public void setLastSubjectAreaId(Long lastSubjectAreaId) { iLastSubjectAreaId = lastSubjectAreaId; }
		public Long getLastSubjectAreaId() { return iLastSubjectAreaId; }
		
		public void setLastDepartmentId(Long lastDepartmentId) { iLastDepartmentId = lastDepartmentId; }
		public Long getLastDepartmentId() { return iLastDepartmentId; }

		public Boolean getShowAuditActions() { return iShowAuditActions; }
		public void setShowAuditActions(Boolean showAuditActions) { this.iShowAuditActions = showAuditActions; }

		public Boolean getShowUpdateActions() { return iShowUpdateActions; }
		public void setShowUpdateActions(Boolean showUpdateActions) { this.iShowUpdateActions = showUpdateActions; }

		public Boolean getShowDeleteActions() { return iShowDeleteActions; }
		public void setShowDeleteActions(Boolean showDeleteActions) { this.iShowDeleteActions = showDeleteActions; }

		public Boolean getShowSuccessResults() { return iShowSuccessResults; }
		public void setShowSuccessResults(Boolean showSuccessResults) { this.iShowSuccessResults = showSuccessResults; }

		public Boolean getShowWarningResults() { return iShowWarningResults; }
		public void setShowWarningResults(Boolean showWarningResults) { this.iShowWarningResults = showWarningResults; }

		public Boolean getShowErrorResults() { return iShowErrorResults; }
		public void setShowErrorResults(Boolean showErrorResults) { this.iShowErrorResults = showErrorResults; }

		public Boolean getShowNoChangeResults() { return iShowNoChangeResults; }
		public void setShowNoChangeResults(Boolean showNoChangeResults) { this.iShowNoChangeResults = showNoChangeResults; }

		public Boolean getShowDepartmentSelection() { return iCanSelectDepartment; }
		public void setCanSelectDepartment(Boolean canSelectDepartment) { this.iCanSelectDepartment = canSelectDepartment; }

		public Boolean getCanSelectManager() { return iCanSelectManager; }
		public void setCanSelectManager(Boolean canSelectManager) {this.iCanSelectManager = canSelectManager; }

		public String getMessageSearchString() { return iMessageSearchString; }
		public void setMessageSearchString(String messageSearchString) { this.iMessageSearchString = messageSearchString; }

		public String getCrosslistSearchString() { return iCrosslistSearchString;}
		public void setCrosslistSearchString(String crosslistSearchString) { this.iCrosslistSearchString = crosslistSearchString;}

		public String getCrnSearchString() { return iCrnSearchString;}
		public void setCrnSearchString(String crnSearchString) { this.iCrnSearchString = crnSearchString;}

	}	
	public static enum BannerResponsesColumn implements IsSerializable {
		UNIQUEID,
		ACTIVITY_DATE,
		CAMPUS,
		SUBJECT_CODE,
		COURSE_NUMBER,
		SECTION_NUMBER,
		CRN,
		XLST_GROUP,
		ACTION,
		TYPE,
		MESSAGE
		;		
	}


}
