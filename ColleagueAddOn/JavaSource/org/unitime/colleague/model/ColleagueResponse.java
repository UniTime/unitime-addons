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

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;

import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.unitime.colleague.model.base.BaseColleagueResponse;
import org.unitime.colleague.model.dao.ColleagueResponseDAO;
import org.unitime.colleague.queueprocessor.exception.LoggableException;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;

/**
 * 
 * based on code contributed by Dagmar Murray
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "colleague_response")
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
	@Transient
	public Long getUniqueId() {
		return uniqueId;
	}
	public void setUniqueId(Long uniqueId) {
		this.uniqueId = uniqueId;
	}
	@Transient
	public Integer getSequenceNumber() {
		return sequenceNumber;
	}
	public void setSequenceNumber(int sequenceNumber) {
		this.sequenceNumber = sequenceNumber;
	}
	@Transient
	public Date getActivityDate() {
		return activityDate;
	}
	public void setActivityDate(Date activityDate) {
		this.activityDate = activityDate;
	}
	@Transient
	public String getTermCode() {
		return termCode;
	}
	public void setTermCode(String termCode) {
		this.termCode = termCode;
	}
	@Transient
	public String getColleagueId() {
		return colleagueId;
	}
	public void setColleagueId(String colleagueId) {
		this.colleagueId = colleagueId;
	}
	@Transient
	public String getSubjectCode() {
		return subjectCode;
	}
	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}
	@Transient
	public String getCourseNumber() {
		return courseNumber;
	}
	public void setCourseNumber(String courseNumber) {
		this.courseNumber = courseNumber;
	}
	@Transient
	public String getSectionNumber() {
		return sectionNumber;
	}
	public void setSectionNumber(String sectionNumber) {
		this.sectionNumber = sectionNumber;
	}

	@Transient
	public String getExternalId() {
		return externalId;
	}
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}
	@Transient
	public String getAction() {
		return action;
	}
	public void setAction(String action) {
		this.action = action;
	}
	@Transient
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	@Transient
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Transient
	public String getPacketId() {
		return packetId;
	}
	@Transient
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
     
    public static List<ColleagueResponse> find(Long sessionId,
			String startDate,
			String stopDate,
			Long searchSubject,
			Set<SubjectArea> subjects,
			Long searchManager,
			Long searchDepartment,
			String searchCourseNumber,
			String searchColleagueId,
			String searchMessage,
			int maxResults,
			boolean showHistory,
			boolean actionAudit,
			boolean actionUpdate,
			boolean actionDelete,
			boolean typeSuccess,
			boolean typeError,
			boolean typeWarning) throws LoggableException {
		
		
		try {
			String subjectCode = null;
			if(searchSubject != null && searchSubject > 0) {
				subjectCode = SubjectAreaDAO.getInstance().get(searchSubject).getSubjectAreaAbbreviation();
			}
			
			DateFormat df = new SimpleDateFormat("MM/dd/yyyy");
			
			Date startDateDte = null;
			if (startDate != null && !startDate.equals("")) {
				startDateDte = df.parse(startDate);
			}
			
			Date stopDateDte = null;
			if(stopDate != null && !stopDate.equals("")) {
				stopDateDte = df.parse(stopDate);
			}
			
			return find(sessionId,
					startDateDte,
					stopDateDte,
					subjectCode,
					subjects,
					searchManager,
					searchDepartment,
					searchCourseNumber,
					searchColleagueId,
					searchMessage,
					maxResults,
					showHistory,
					actionAudit,
					actionUpdate,
					actionDelete,
					typeSuccess,
					typeError,
					typeWarning);
			
		} catch (ParseException e) {
			throw new LoggableException(e);
		}
		
	}
	
	public static List<ColleagueResponse> find(Long sessionId,
			Date startDate,
			Date stopDate,
			String searchSubject,
			Set<SubjectArea> subjects,
			Long searchManager,
			Long searchDepartment,
			String searchCourseNumber,
			String searchColleagueId,
			String searchMessage,
			int maxResults,
			boolean showHistory,
			boolean actionAudit,
			boolean actionUpdate,
			boolean actionDelete,
			boolean typeSuccess,
			boolean typeError,
			boolean typeWarning) throws LoggableException {
		
        List queueOuts = null;
        Transaction tx = null;
        try {
        	
            tx = ColleagueResponseDAO.getInstance().getSession().beginTransaction();

            String whereHql = " where rp.termCode=:termCode ";
            
            org.hibernate.Session hibSession = ColleagueResponseDAO.getInstance().getSession(); 
    		ColleagueSession bs = ColleagueSession.findColleagueSessionForSession(sessionId, hibSession);
            
    		String joinHql = "";
            if ((searchManager != null && searchManager > 0 ) || (searchDepartment != null && searchDepartment > 0 )) {
            	joinHql += " SubjectArea as sa " +
            	" inner join sa.department as dept ";
            	whereHql += " and sa.session.uniqueId = :sessionId " +
            			"and sa.subjectAreaAbbreviation = rp.subjectCode ";
                if (searchDepartment != null && searchDepartment > 0 ) {
                   	whereHql += " and dept.uniqueId = :departmentId ";
                }
            }
            if (searchManager != null && searchManager > 0 ) {
            	joinHql += " inner join dept.timetableManagers as mgr ";
            	whereHql += " and mgr.uniqueId=:managerId";
            }
            
            if(startDate != null) {
            	whereHql += " and rp.activityDate >= :startDate";
            }
            String fromHql = " from ";
            if (joinHql.length() > 0) {
            	fromHql += joinHql + ",";
            }
            fromHql += " ColleagueResponse as rp";
            

            if(stopDate != null) {
            	whereHql += " and rp.activityDate <= :stopDate";
            }
            
            if(searchSubject != null && searchSubject != "") {
            	whereHql += " and upper(rp.subjectCode) = upper(:searchSubject) ";
            } else {
            	int i = 1;
    	    	for (Iterator<SubjectArea> it = subjects.iterator(); it.hasNext();){
    	    		SubjectArea s = it.next();
    	    		if (i == 1) {
    	    			whereHql += " and ( rp.subjectCode in ( ";
    	    		} else {
    	    			whereHql += " , ";
    	    		}
    	    		whereHql += " '" + s.getSubjectAreaAbbreviation() + "'";
    	    		i++;
    	    	}
            	if (i>1) {
            		whereHql += "))";
            	}
            }
            
            if(searchCourseNumber != null && searchCourseNumber != "") {
            	whereHql += " and upper(rp.courseNumber) = upper(:searchCourseNumber) ";
            }
            
            if(searchColleagueId != null && searchColleagueId != "") {
            	whereHql += " and rp.colleagueId = upper(:searchColleagueId) ";
            }
                        
            if ((actionUpdate || actionAudit || actionDelete ) && !(actionUpdate && actionAudit && actionDelete ))  {
            	whereHql += " and rp.action in (";
            	if (actionUpdate) {
            		whereHql += "'UPDATE'";
            	}
            	if (actionAudit) {
            		if (!whereHql.endsWith("(")) whereHql += ",";
            		whereHql += "'AUDIT'";
            	}
            	if (actionDelete) {
            		if (!whereHql.endsWith("(")) whereHql += ",";
            		whereHql += "'DELETE'";
            	}
            	whereHql += ") ";
            }
            
            if ((typeError || typeSuccess || typeWarning ) && ! (typeError && typeSuccess && typeWarning )) {
            	whereHql += " and rp.type in (";
            	if (typeError) {
            		whereHql += "'ERROR'";
            	}
            	if (typeSuccess) {
            		if (!whereHql.endsWith("(")) whereHql += ",";
            		whereHql += "'SUCCESS'";
            	}
            	if (typeWarning) {
            		if (!whereHql.endsWith("(")) whereHql += ",";
            		whereHql += "'WARNING'";
            	}
            	whereHql += ") ";
            }
            	
            if(searchMessage != null && searchMessage != "") {
            	whereHql += " and upper(rp.message) like upper(:searchMessage) ";
            }
            if (!showHistory) {
            	whereHql += " and rp.queueId = (select max(queueId) from ColleagueResponse rp3 where rp3.termCode = rp.termCode and rp3.colleagueId = rp.colleagueId) ";
            }
            			
            String hql = "select rp " +
            	fromHql + 
            	whereHql +
            	" order by rp.activityDate desc, rp.sequenceNumber desc ";
            
            Query<ColleagueResponse> query = hibSession.createQuery(hql, ColleagueResponse.class);
            query.setParameter("termCode",bs.getColleagueTermCode());
            
            if(startDate != null) {
            	query.setParameter("startDate", startDate);
            }
            
            if(stopDate  != null) {
            	query.setParameter("stopDate", stopDate);
            }
           
            if ((searchManager != null && searchManager > 0 ) || (searchDepartment != null && searchDepartment > 0 )) {
            	query.setParameter("sessionId", sessionId);
                if (searchDepartment != null && searchDepartment > 0 ) {
                   	query.setParameter("departmentId",searchDepartment);
                }
            }
            if (searchManager != null && searchManager > 0 ) {
               	query.setParameter("managerId",searchManager);
            }
            

            if(searchSubject != null && searchSubject != "") {
            	query.setParameter("searchSubject", searchSubject);
            }
            
            if(searchCourseNumber != null && searchCourseNumber != "") {
				query.setParameter("searchCourseNumber", searchCourseNumber);
            }
            
            if(searchColleagueId != null && searchColleagueId != "") {
				query.setParameter("searchColleagueId", searchColleagueId);
            }
                        
            if(searchMessage != null && searchMessage != "") {
            	query.setParameter("searchMessage", searchMessage.replace('*', '%'));
            }
            
            if(maxResults < 0) maxResults = 0;
            
            if(maxResults > 0) {
            	query.setMaxResults(maxResults);
            }
            
            query.setCacheable(false);
            
            queueOuts =  query.list();
            tx.commit();
            
        } catch (Exception e) {
        	tx.rollback();
        	throw new LoggableException(e);
        }
        return queueOuts;
	}
}
