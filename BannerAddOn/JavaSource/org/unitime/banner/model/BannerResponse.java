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

package org.unitime.banner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import org.hibernate.Transaction;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import java.text.DateFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unitime.banner.model.base.BaseBannerResponse;
import org.unitime.banner.model.dao.BannerResponseDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;

/**
 * 
 * based on code contributed by Dagmar Murray
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
@Table(name = "banner_response")
public class BannerResponse extends BaseBannerResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4615752901037787775L;

	   public int compareTo(Object obj) {
	        if (obj==null || !(obj instanceof BannerResponse)) return -1;
	        BannerResponse chl = (BannerResponse)obj;
	        int cmp = getActivityDate().compareTo(chl.getActivityDate());
	        if (cmp!=0) return cmp;
	        return getUniqueId().compareTo(chl.getUniqueId());
	    }

    public static SimpleDateFormat sDF = new SimpleDateFormat("MM/dd/yy hh:mmaa");
    
    public String dateSortOrder() {
    	
           NumberFormat nf = NumberFormat.getInstance();
           nf.setMaximumIntegerDigits(15);
           nf.setMinimumIntegerDigits(15);
           nf.setGroupingUsed(false);
    	
    	return(new SimpleDateFormat("yyyyMMddHHmmss").format(this.getActivityDate()) + nf.format(this.getSequenceNumber()));   			
    }
     
	public String filterLabelShort() {
		return getSubjectCode() + " " + getCourseNumber() + " " + getCrn();
	}
	
	public String filterLabelLong() {
		return getSubjectCode() + " " + getCourseNumber() + " " + getCrn() + " - " + getSectionNumber();
	}
	
	
	public static List<BannerResponse> find(Long sessionId,
			String startDate,
			String stopDate,
			Long searchSubject,
			Set<SubjectArea> subjects,
			Long searchManager,
			Long searchDepartment,
			String searchCourseNumber,
			String searchCrn,
			String searchXlst,
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
					searchCrn,
					searchXlst,
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
	
	public static List<BannerResponse> find(Long sessionId,
			Date startDate,
			Date stopDate,
			String searchSubject,
			Set<SubjectArea> subjects,
			Long searchManager,
			Long searchDepartment,
			String searchCourseNumber,
			String searchCrn,
			String searchXlst,
			String searchMessage,
			int maxResults,
			boolean showHistory,
			boolean actionAudit,
			boolean actionUpdate,
			boolean actionDelete,
			boolean typeSuccess,
			boolean typeError,
			boolean typeWarning) throws LoggableException {
		
        List<BannerResponse> queueOuts = null;
        Transaction tx = null;
        try {
        	
            tx = BannerResponseDAO.getInstance().getSession().beginTransaction();

            String whereHql = " where rp.termCode=:termCode ";
            
            org.hibernate.Session hibSession = new BannerResponseDAO().getSession(); 
    		BannerSession bs = BannerSession.findBannerSessionForSession(sessionId, hibSession);
            
    		String joinHql = "";
            if ((searchManager != null && searchManager > 0 ) || (searchDepartment != null && searchDepartment > 0 )) {
            	joinHql += " SubjectArea as sa " +
            	" inner join sa.department as dept ";
            	whereHql += " and sa.session = :sessionId " +
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
            fromHql += " BannerResponse as rp";
            

            if(stopDate != null) {
            	whereHql += " and rp.activityDate <= :stopDate";
            }
            
            if(searchSubject != null && searchSubject != "") {
            	whereHql += " and upper(rp.subjectCode) = upper(:searchSubject) ";
            } else {
            	int i = 1;
    	    	for (SubjectArea s: subjects) {
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
            
            if(searchCrn != null && searchCrn != "") {
            	whereHql += " and rp.crn = upper(:searchCrn) ";
            }
            
            if(searchXlst != null && searchXlst != "") {
            	whereHql += " and upper(rp.xlstGroup) like upper(:searchXlst) ";
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
            	whereHql += " and rp.queueId = (select max(queueId) from BannerResponse rp3 where rp3.termCode = rp.termCode and rp3.crn = rp.crn and ((rp3.xlstGroup is null and rp.xlstGroup is null) or (rp3.xlstGroup = rp.xlstGroup))) ";
            }
            			
            String hql = "select rp " +
            	fromHql + 
            	whereHql +
            	" order by rp.activityDate desc, rp.sequenceNumber desc ";
            
            Query<BannerResponse> query = BannerResponseDAO.getInstance().getSession().createQuery(hql, BannerResponse.class);
            query.setParameter("termCode",bs.getBannerTermCode());
            
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
            
            if(searchCrn != null && searchCrn != "") {
				query.setParameter("searchCrn", searchCrn);
            }
            
            if(searchXlst != null && searchXlst != "") {
            	query.setParameter("searchXlst", searchXlst.replace("*", "%"));
            }
            
            if(searchMessage != null && searchMessage != "") {
            	query.setParameter("searchMessage", searchMessage.replace('*', '%'));
            }
            
            if(maxResults < 0) maxResults = 0;
            
            if(maxResults > 0) {
            	query.setMaxResults(maxResults);
            }
            
            query.setCacheable(false);
            
            queueOuts = query.list();
            tx.commit();
            
        } catch (Exception e) {
        	tx.rollback();
        	throw new LoggableException(e);
        } finally {
//        	BannerResponseDAO.getInstance().getSession().close();
        }
        return queueOuts;
	}
}
