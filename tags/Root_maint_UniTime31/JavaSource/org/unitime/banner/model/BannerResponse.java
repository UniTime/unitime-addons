/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package org.unitime.banner.model;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.unitime.banner.model.base.BaseBannerResponse;

/**
 * 
 * based on code contributed by Dagmar Murray
 *
 */
public class BannerResponse extends BaseBannerResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4615752901037787775L;
	private Long uniqueId;
	private int sequenceNumber;
	private Date activityDate;
	private String termCode;
	private String crn;
	private String subjectCode;
	private String courseNumber;
	private String sectionNumber;
	private String xlstGroup;
	private String externalId;
	private String action;
	private String type;
	private String message;
	private String packetId;
	private Long queueId;

	   public int compareTo(Object obj) {
	        if (obj==null || !(obj instanceof BannerResponse)) return -1;
	        BannerResponse chl = (BannerResponse)obj;
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
	public int getSequenceNumber() {
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
	public String getCrn() {
		return crn;
	}
	public void setCrn(String crn) {
		this.crn = crn;
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
	public String getXlstGroup() {
		return xlstGroup;
	}
	public void setXlstGroup(String xlstGroup) {
		this.xlstGroup = xlstGroup;
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
