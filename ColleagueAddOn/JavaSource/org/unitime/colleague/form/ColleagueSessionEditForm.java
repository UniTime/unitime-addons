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
package org.unitime.colleague.form;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.dao.ColleagueSessionDAO;

/**
 * 
 * @author says
 *
 */
public class ColleagueSessionEditForm extends ActionForm {
	
	// --------------------------------------------------------- Instance Variables
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6616750809381469241L;

	ColleagueSession session = new ColleagueSession();
	
	Long acadSessionId;
	String acadSessionLabel;
	ArrayList availableAcadSessions;
	/**
	 * @return the availableAcadSessions
	 */
	public ArrayList getAvailableAcadSessions() {
		return availableAcadSessions;
	}


	/**
	 * @param availableAcadSessions the availableAcadSessions to set
	 */
	public void setAvailableAcadSessions(ArrayList availableAcadSessions) {
		this.availableAcadSessions = availableAcadSessions;
	}


	/**
	 * @return the acadSessionLabel
	 */
	public String getAcadSessionLabel() {
		return acadSessionLabel;
	}


	/**
	 * @param acadSessionLabel the acadSessionLabel to set
	 */
	public void setAcadSessionLabel(String acadSessionLabel) {
		this.acadSessionLabel = acadSessionLabel;
	}


	/**
	 * @return the acadSessionId
	 */
	public Long getAcadSessionId() {
		return acadSessionId;
	}


	/**
	 * @param acadSessionId the acadSessionId to set
	 */
	public void setAcadSessionId(Long acadSessionId) {
		this.acadSessionId = acadSessionId;
	}


	/**
	 * @return the colleagueTermCode
	 */
	public String getColleagueTermCode() {
		return colleagueTermCode;
	}


	/**
	 * @param colleagueTermCode the colleagueTermCode to set
	 */
	public void setColleagueTermCode(String colleagueTermCode) {
		this.colleagueTermCode = colleagueTermCode;
	}


	/**
	 * @return the colleagueCampus
	 */
	public String getColleagueCampus() {
		return colleagueCampus;
	}


	/**
	 * @param colleagueCampus the colleagueCampus to set
	 */
	public void setColleagueCampus(String colleagueCampus) {
		this.colleagueCampus = colleagueCampus;
	}


	/**
	 * @return the storeDataForColleague
	 */
	public Boolean getStoreDataForColleague() {
		return storeDataForColleague;
	}


	/**
	 * @param storeDataForColleague the storeDataForColleague to set
	 */
	public void setStoreDataForColleague(Boolean storeDataForColleague) {
		this.storeDataForColleague = storeDataForColleague;
	}


	/**
	 * @return the sendDataToColleague
	 */
	public Boolean getSendDataToColleague() {
		return sendDataToColleague;
	}


	/**
	 * @param sendDataToColleague the sendDataToColleague to set
	 */
	public void setSendDataToColleague(Boolean sendDataToColleague) {
		this.sendDataToColleague = sendDataToColleague;
	}


	/**
	 * @return the loadingOfferingsFile
	 */
	public Boolean getLoadingOfferingsFile() {
		return loadingOfferingsFile;
	}


	/**
	 * @param loadingOfferingsFile the loadingOfferingsFile to set
	 */
	public void setLoadingOfferingsFile(Boolean loadingOfferingsFile) {
		this.loadingOfferingsFile = loadingOfferingsFile;
	}



	String colleagueTermCode;
	String colleagueCampus;
	Boolean storeDataForColleague;
	Boolean sendDataToColleague;
	Boolean loadingOfferingsFile;
		
	// --------------------------------------------------------- Methods
	
	public ActionErrors validate(ActionMapping arg0, HttpServletRequest arg1) {
		ActionErrors errors = new ActionErrors();
		
		// Check data fields
		if (colleagueTermCode==null || colleagueTermCode.trim().length()==0) 
			errors.add("colleagueTermCode", new ActionMessage("errors.required", "Colleague Term Code"));
		
		if (colleagueCampus==null || colleagueCampus.trim().length()==0) 
			errors.add("colleagueCampus", new ActionMessage("errors.required", "Colleague Campus"));

		if (acadSessionId==null) 
			errors.add("acadSessionId", new ActionMessage("errors.required", "Academic Session"));
		
				
		// Check for duplicate session
		if (errors.size()==0) {
			ColleagueSession sessn = ColleagueSession.findColleagueSessionForSession(acadSessionId, ColleagueSessionDAO.getInstance().getSession());
			if (session.getUniqueId()==null && sessn!=null)
				errors.add("sessionId", new ActionMessage("errors.generic", "A colleague session for the academic session already exists"));
				
			if (session.getUniqueId()!=null && sessn!=null) {
				if (!session.getUniqueId().equals(sessn.getUniqueId()))
					errors.add("sessionId", new ActionMessage("errors.generic", "Another colleague session for the same academic session already exists"));
			}
		}
		
		return errors;
	}


	/**
	 * @return Returns the session.
	 */
	public ColleagueSession getSession() {
		return session;
	}
	/**
	 * @param session The session to set.
	 */
	public void setSession(ColleagueSession session) {
		this.session = session;
	}
	
	public boolean equals(Object arg0) {
		return session.equals(arg0);
	}
	
	
	public int hashCode() {
		return session.hashCode();
	}
	
	/**
	 * @return
	 */
	public Long getSessionId() {
		return session.getUniqueId();
	}
	/**
	 * @param sessionId
	 */
	public void setSessionId(Long sessionId) {
		if (sessionId!=null && sessionId.longValue()<=0)
			session.setUniqueId(null);
		else
			session.setUniqueId(sessionId);
	}
    
}
