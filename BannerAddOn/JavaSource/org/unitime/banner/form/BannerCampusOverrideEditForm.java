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
