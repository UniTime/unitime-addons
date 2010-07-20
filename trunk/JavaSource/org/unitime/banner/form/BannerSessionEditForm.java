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
    
}
