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

package org.unitime.colleague.model;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unitime.colleague.model.base.BaseColleagueResponse;

/**
 * 
 * based on code contributed by Dagmar Murray
 *
 */
public class ColleagueResponse extends BaseColleagueResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4615752901037787775L;
	private Long uniqueId;
	private int sequenceNumber;
	private Date activityDate;
	private String termCode;
	private String colleagueId;
	private String subjectCode;
	private String courseNumber;
	private String sectionNumber;
	private String externalId;
	private String action;
	private String type;
	private String message;
	private String packetId;
	private Long queueId;

   public int compareTo(Object obj) {
        if (obj==null || !(obj instanceof ColleagueResponse)) return -1;
        ColleagueResponse chl = (ColleagueResponse)obj;
        int cmp = getActivityDate().compareTo(chl.getActivityDate());
        if (cmp!=0) return cmp;
        return getUniqueId().compareTo(chl.getUniqueId());
    }

	//Getters & Setters
	public Long getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(Long uniqueId) {
		this.uniqueId = uniqueId;
	}
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	public Date getActivityDate() {
		return activityDate;
	}
	public void setActivityDate(Date activityDate) {
		this.activityDate = activityDate;
	}
	public String getTermCode() {
		return termCode;
	}
	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}
	public String getColleagueId() {
		return colleagueId;
	}
	public void setColleagueId(String colleagueId) {
		this.colleagueId = colleagueId;
	}
	public String getSubjectCode() {
		return subjectCode;
	}
	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}
	public String getCourseNumber() {
		return courseNumber;
	}
	public void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}
	public String getSectionNumber() {
		return sectionNumber;
	}
	public void setSectionNumber(String sectionNumber) {
		this.sectionNumber = sectionNumber;
	}

	public String getExternalId() {
		return externalId;
	}
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getPacketId() {
		return packetId;
	}
	public Long getQueueId() {
		return queueId;
	}
	public void setQueueId(Long queueId) {
		this.queueId = queueId;
	}
	public void setPacketId(String packetId) {
		this.packetId = packetId;
	}

    public static SimpleDateFormat sDF = new SimpleDateFormat("MM/dd/yy hh:mmaa");
    
    public String dateSortOrder() {
    	
           NumberFormat nf = NumberFormat.getInstance();
           nf.setMaximumIntegerDigits(15);
           nf.setMinimumIntegerDigits(15);
           nf.setGroupingUsed(false);
    	
    	return(new SimpleDateFormat("yyyyMMddHHmmss").format(this.getActivityDate()) + nf.format(this.getSequenceNumber()));   			
    }
     
	
	
	
	
	
	
	
	
	
	
}
