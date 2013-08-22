/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.banner.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.banner.model.BannerCampusOverride;

/**
 * 
 * @author says
 *
 */
public class BannerCampusOverrideEditForm extends ActionForm {
	
	// --------------------------------------------------------- Instance Variables
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 6616750809381469241L;



	/**
	 * @return the bannerCampusCode
	 */
	public String getBannerCampusCode() {
		return bannerCampusCode;
	}


	/**
	 * @param bannerCampusCode the bannerCampusCode to set
	 */
	public void setBannerCampusCode(String bannerCampusCode) {
		this.bannerCampusCode = bannerCampusCode;
	}


	/**
	 * @return the bannerCampusName
	 */
	public String getBannerCampusName() {
		return bannerCampusName;
	}


	/**
	 * @param bannerCampusName the bannerCampusName to set
	 */
	public void setBannerCampusName(String bannerCampusName) {
		this.bannerCampusName = bannerCampusName;
	}


	/**
	 * @return the visible
	 */
	public Boolean getVisible() {
		return visible;
	}


	/**
	 * @param storeDataForBanner the visible to set
	 */
	public void setVisible(Boolean visible) {
		this.visible = visible;
	}


	/**
	 * @return the sendDataToBanner
	 */


    BannerCampusOverride bannerCampusOverride = new BannerCampusOverride();
	String bannerCampusCode;
	String bannerCampusName;
	Boolean visible;
		
	// --------------------------------------------------------- Methods
	
	public ActionErrors validate(ActionMapping arg0, HttpServletRequest arg1) {
		ActionErrors errors = new ActionErrors();
		
		// Check data fields
		if (bannerCampusCode==null || bannerCampusCode.trim().length()==0) 
			errors.add("bannerCampusCode", new ActionMessage("errors.required", "Banner Campus Code"));
		
		if (bannerCampusName==null || bannerCampusName.trim().length()==0) 
			errors.add("bannerCampusName", new ActionMessage("errors.required", "Banner Campus Name"));
				
		// Check for duplicate campus code
		if (errors.size()==0) {
			BannerCampusOverride code = BannerCampusOverride.getBannerCampusOverrideForCode(bannerCampusCode);
			if (bannerCampusOverride.getUniqueId()==null && code!=null)
				errors.add("sessionId", new ActionMessage("errors.generic", "Banner campus code '" + bannerCampusCode + "' already exists."));
				
			if (bannerCampusOverride.getUniqueId()!=null && code!=null) {
				if (!bannerCampusOverride.getUniqueId().equals(code.getUniqueId()))
					errors.add("sessionId", new ActionMessage("errors.generic", "Banner campus code '" + bannerCampusCode + "' already exists."));
			}
		}
		
		return errors;
	}


	/**
	 * @return Returns the bannerCampusOverride.
	 */
	public BannerCampusOverride getBannerCampusOverride() {
		return bannerCampusOverride;
	}
	/**
	 * @param session The bannerCampusOverride to set.
	 */
	public void setBannerCampusOverride(BannerCampusOverride bannerCampusOverride) {
		this.bannerCampusOverride = bannerCampusOverride;
	}
	
	public boolean equals(Object arg0) {
		return bannerCampusOverride.equals(arg0);
	}
	
	
	public int hashCode() {
		return bannerCampusOverride.hashCode();
	}
	
	/**
	 * @return
	 */
	public Long getCampusOverrideId() {
		return bannerCampusOverride.getUniqueId();
	}
	
	/**
	 * @param bannerCampusOverrideId
	 */
	public void setCampusOverrideId(Long bannerCampusOverrideId) {
		if (bannerCampusOverrideId!=null && bannerCampusOverrideId.longValue()<=0)
			bannerCampusOverride.setUniqueId(null);
		else
			bannerCampusOverride.setUniqueId(bannerCampusOverrideId);
	}
    
    
}
