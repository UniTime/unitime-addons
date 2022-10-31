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

import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.form.UniTimeForm;

/**
 * 
 * @author says
 *
 */
public class BannerCampusOverrideEditForm implements UniTimeForm {
	private static final long serialVersionUID = 6616750809381469241L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);

    Long bannerCampusOverrideId;
	String bannerCampusCode;
	String bannerCampusName;
	Boolean visible;
		

	public BannerCampusOverrideEditForm() {
		reset();
	}
	
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


	@Override
	public void validate(UniTimeAction action) {
		// Check data fields
		if (bannerCampusCode==null || bannerCampusCode.trim().length()==0) 
			action.addFieldError("form.bannerCampusCode", MSG.errorRequiredField(BMSG.colBannerCampusCode())); 
		
		if (bannerCampusName==null || bannerCampusName.trim().length()==0)
			action.addFieldError("form.bannerCampusName", MSG.errorRequiredField(BMSG.colBannerCampusName()));
				
		// Check for duplicate campus code
		if (!action.hasFieldErrors()) {
			BannerCampusOverride code = BannerCampusOverride.getBannerCampusOverrideForCode(bannerCampusCode);
			if (bannerCampusOverrideId==null && code!=null)
				action.addFieldError("form.bannerCampusCode", MSG.errorAlreadyExists(bannerCampusCode));
				
			if (bannerCampusOverrideId!=null && code!=null) {
				if (!bannerCampusOverrideId.equals(code.getUniqueId()))
					action.addFieldError("form.bannerCampusCode", MSG.errorAlreadyExists(bannerCampusCode));
			}
		}
	}


	/**
	 * @return
	 */
	public Long getCampusOverrideId() {
		return bannerCampusOverrideId;
	}
	
	/**
	 * @param bannerCampusOverrideId
	 */
	public void setCampusOverrideId(Long bannerCampusOverrideId) {
		this.bannerCampusOverrideId = bannerCampusOverrideId;
	}

	@Override
	public void reset() {
		bannerCampusOverrideId = null;
		bannerCampusCode = null;
		bannerCampusName = null;
		visible = true;
	}
    
}
