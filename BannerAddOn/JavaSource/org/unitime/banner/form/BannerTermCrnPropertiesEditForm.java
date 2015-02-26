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
import org.unitime.banner.model.BannerTermCrnProperties;
import org.unitime.banner.model.dao.BannerTermCrnPropertiesDAO;

/**
 * 
 * @author says
 *
 */
public class BannerTermCrnPropertiesEditForm extends ActionForm {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 5925150971448455801L;

	// --------------------------------------------------------- Instance Variables
	
	BannerTermCrnProperties bannerTermProperties = new BannerTermCrnProperties();
	
	ArrayList availableBannerTermCodes;
	String bannerTermCode;
	Integer lastCrn;
	Boolean searchFlag;
	Integer minCrn;
	Integer maxCrn;

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

		
	// --------------------------------------------------------- Methods
	
	/**
	 * @return the lastCrn
	 */
	public Integer getLastCrn() {
		return lastCrn;
	}


	/**
	 * @param lastCrn the lastCrn to set
	 */
	public void setLastCrn(Integer lastCrn) {
		this.lastCrn = lastCrn;
	}


	/**
	 * @return the searchFlag
	 */
	public Boolean getSearchFlag() {
		return searchFlag;
	}


	/**
	 * @param searchFlag the searchFlag to set
	 */
	public void setSearchFlag(Boolean searchFlag) {
		this.searchFlag = searchFlag;
	}


	/**
	 * @return the minCrn
	 */
	public Integer getMinCrn() {
		return minCrn;
	}


	/**
	 * @param minCrn the minCrn to set
	 */
	public void setMinCrn(Integer minCrn) {
		this.minCrn = minCrn;
	}


	/**
	 * @return the maxCrn
	 */
	public Integer getMaxCrn() {
		return maxCrn;
	}


	/**
	 * @param maxCrn the maxCrn to set
	 */
	public void setMaxCrn(Integer maxCrn) {
		this.maxCrn = maxCrn;
	}


	public ActionErrors validate(ActionMapping arg0, HttpServletRequest arg1) {
		ActionErrors errors = new ActionErrors();
		
		// Check data fields
		if (bannerTermCode==null || bannerTermCode.trim().length()==0) 
			errors.add("bannerTermCode", new ActionMessage("errors.required", "Banner Term Code"));
		
		if (lastCrn==null) 
			errors.add("lastCrn", new ActionMessage("errors.required", "Last Crn"));

		if (minCrn==null) 
			errors.add("minCrn", new ActionMessage("errors.required", "Minimum Crn"));
		
		if (maxCrn==null) 
			errors.add("maxCrn", new ActionMessage("errors.required", "Maximum Crn"));
				
		// Check for duplicate session
		if (errors.size()==0) {
			BannerTermCrnProperties termProps = BannerTermCrnProperties.findBannerTermCrnPropertiesForTermCode(bannerTermCode, BannerTermCrnPropertiesDAO.getInstance().getSession());
			if (bannerTermProperties.getUniqueId()==null && termProps!=null)
				errors.add("bannerTermProperties", new ActionMessage("errors.generic", "Banner Term Properties for the banner term code already exist"));
				
			if (bannerTermProperties.getUniqueId()!=null && termProps!=null) {
				if (!bannerTermProperties.getUniqueId().equals(termProps.getUniqueId()))
					errors.add("sessionId", new ActionMessage("errors.generic", "Another Banner Term Properties for the same banner term code already exists"));
			}
		}
		
		return errors;
	}


	public boolean equals(Object arg0) {
		return bannerTermProperties.equals(arg0);
	}
	
	
	public int hashCode() {
		return bannerTermProperties.hashCode();
	}
	
	/**
	 * @return
	 */
	public Long getBannerTermCrnPropertiesId() {
		return bannerTermProperties.getUniqueId();
	}
	/**
	 * @param bannerTermPropertiesId
	 */
	public void setBannerTermCrnPropertiesId(Long bannerTermPropertiesId) {
		if (bannerTermPropertiesId!=null && bannerTermPropertiesId.longValue()<=0)
			bannerTermProperties.setUniqueId(null);
		else
			bannerTermProperties.setUniqueId(bannerTermPropertiesId);
	}

	/**
	 * @return the availableBannerTermCodes
	 */
	public ArrayList getAvailableBannerTermCodes() {
		return availableBannerTermCodes;
	}


	/**
	 * @param availableBannerTermCodes the availableBannerTermCodes to set
	 */
	public void setAvailableBannerTermCodes(ArrayList availableBannerTermCodes) {
		this.availableBannerTermCodes = availableBannerTermCodes;
	}


	/**
	 * @return the bannerTermProperties
	 */
	public BannerTermCrnProperties getBannerTermProperties() {
		return bannerTermProperties;
	}


	/**
	 * @param bannerTermProperties the bannerTermProperties to set
	 */
	public void setBannerTermProperties(BannerTermCrnProperties bannerTermProperties) {
		this.bannerTermProperties = bannerTermProperties;
	}



    
}
