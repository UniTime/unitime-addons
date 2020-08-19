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
package org.unitime.banner.form;

import java.util.ArrayList;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerSessionDAO;

/**
 * 
 * @author says
 *
 */
public class BannerSessionEditForm extends ActionForm {
	
	// --------------------------------------------------------- Instance Variables
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6616750809381469241L;

	BannerSession session = new BannerSession();
	
	Long acadSessionId;
	String acadSessionLabel;
	ArrayList availableAcadSessions;
	ArrayList availableBannerSessions;
	String studentCampus;
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
	
	public void setAvailableBannerSessions(ArrayList availableBannerSessions) { this.availableBannerSessions = availableBannerSessions; }
	public ArrayList getAvailableBannerSessions() { return availableBannerSessions; }

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
	 * @return the bannerTermCode
	 */
	public String getBannerTermCode() {
		return bannerTermCode;
	}


	/**
	 * @param bannerTermCode the bannerTermCode to set
	 */
	public void setBannerTermCode(String bannerTermCode) {
		this.bannerTermCode = bannerTermCode;
	}


	/**
	 * @return the bannerCampus
	 */
	public String getBannerCampus() {
		return bannerCampus;
	}


	/**
	 * @param bannerCampus the bannerCampus to set
	 */
	public void setBannerCampus(String bannerCampus) {
		this.bannerCampus = bannerCampus;
	}


	/**
	 * @return the storeDataForBanner
	 */
	public Boolean getStoreDataForBanner() {
		return storeDataForBanner;
	}


	/**
	 * @param storeDataForBanner the storeDataForBanner to set
	 */
	public void setStoreDataForBanner(Boolean storeDataForBanner) {
		this.storeDataForBanner = storeDataForBanner;
	}


	/**
	 * @return the sendDataToBanner
	 */
	public Boolean getSendDataToBanner() {
		return sendDataToBanner;
	}


	/**
	 * @param sendDataToBanner the sendDataToBanner to set
	 */
	public void setSendDataToBanner(Boolean sendDataToBanner) {
		this.sendDataToBanner = sendDataToBanner;
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



	String bannerTermCode;
	String bannerCampus;
	Boolean storeDataForBanner;
	Boolean sendDataToBanner;
	Boolean loadingOfferingsFile;
	Long futureSessionId;
	Integer futureUpdateMode;
		
	// --------------------------------------------------------- Methods
	
	public ActionErrors validate(ActionMapping arg0, HttpServletRequest arg1) {
		ActionErrors errors = new ActionErrors();
		
		// Check data fields
		if (bannerTermCode==null || bannerTermCode.trim().length()==0) 
			errors.add("bannerTermCode", new ActionMessage("errors.required", "Banner Term Code"));
		
		if (bannerCampus==null || bannerCampus.trim().length()==0) 
			errors.add("bannerCampus", new ActionMessage("errors.required", "Banner Campus"));

		if (acadSessionId==null) 
			errors.add("acadSessionId", new ActionMessage("errors.required", "Academic Session"));
		
				
		// Check for duplicate session
		if (errors.size()==0) {
			BannerSession sessn = BannerSession.findBannerSessionForSession(acadSessionId, BannerSessionDAO.getInstance().getSession());
			if (session.getUniqueId()==null && sessn!=null)
				errors.add("sessionId", new ActionMessage("errors.generic", "A banner session for the academic session already exists"));
				
			if (session.getUniqueId()!=null && sessn!=null) {
				if (!session.getUniqueId().equals(sessn.getUniqueId()))
					errors.add("sessionId", new ActionMessage("errors.generic", "Another banner session for the same academic session already exists"));
			}
		}
		
		return errors;
	}


	/**
	 * @return Returns the session.
	 */
	public BannerSession getSession() {
		return session;
	}
	/**
	 * @param session The session to set.
	 */
	public void setSession(BannerSession session) {
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
	
	public Long getFutureSessionId() { return futureSessionId; }
	public void setFutureSessionId(Long futureSessionId) { this.futureSessionId = futureSessionId; }
	public Integer getFutureUpdateMode() { return futureUpdateMode; }
	public void setFutureUpdateMode(Integer futureUpdateMode) { this.futureUpdateMode = futureUpdateMode; }
	public String getStudentCampus() { return studentCampus; }
	public void setStudentCampus(String studentCampus) { this.studentCampus = studentCampus; }
}
