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
