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
import java.util.HashSet;

import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.BannerTermCrnProperties;
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
public class BannerTermCrnPropertiesEditForm implements UniTimeForm {
	private static final long serialVersionUID = 5925150971448455801L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);

	Long bannerTermPropertiesId;
	ArrayList<BannerSession> availableBannerTermCodes;
	ArrayList<BannerSession> availableBannerSessions;
	String bannerTermCode;
	String[] bannerSessionIds;
	Integer lastCrn;
	Boolean searchFlag;
	Integer minCrn;
	Integer maxCrn;
	
	public BannerTermCrnPropertiesEditForm() {
		reset();
	}
	
	@Override
	public void reset() {
		bannerTermPropertiesId = null;
		availableBannerTermCodes = null;
		availableBannerSessions = null;
		bannerTermCode = null;
		bannerSessionIds = null;
		lastCrn = null;
		searchFlag = null;
		minCrn = null;
		maxCrn = null;
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


	public ArrayList<BannerSession> getAvailableBannerSessions() {
		return availableBannerSessions;
	}


	public void setAvailableBannerSessions(ArrayList<BannerSession> availableBannerSessions) {
		this.availableBannerSessions = availableBannerSessions;
	}


	public String[] getBannerSessionIds() {
		return bannerSessionIds;
	}


	public void setBannerSessionIds(String[] bannerSessionIds) {
		this.bannerSessionIds = bannerSessionIds;
	}

    public ArrayList<Long> getBannerSessionIdsConvertedToLongs(){
    		ArrayList<Long> ids = new ArrayList<Long>();
    		for (String id : getBannerSessionIds()) {
    			ids.add(Long.valueOf(id));
    		}
    		return(ids);
    }


    @Override
	public void validate(UniTimeAction action) {
		// Check data fields
		if (bannerSessionIds == null || bannerSessionIds.length == 0) {
			action.addFieldError("form.bannerSessions", MSG.errorRequiredField(BMSG.colBannerSessions()));			
		}
		
		if (bannerTermCode==null || bannerTermCode.trim().length()==0) {
			action.addFieldError("form.bannerTermCode", MSG.errorRequiredField(BMSG.colBannerTermCode()));
		}
		
		if (lastCrn==null) 
			action.addFieldError("form.lastCrn", MSG.errorRequiredField(BMSG.colLastCRN()));

		if (minCrn==null) 
			action.addFieldError("form.minCrn", MSG.errorRequiredField(BMSG.colMinimumCRN()));
		
		if (maxCrn==null) 
			action.addFieldError("form.maxCrn", MSG.errorRequiredField(BMSG.colMaximumCRN()));
				
		// Check for duplicate session and same term code
		if (!action.hasFieldErrors()) {
			HashSet<BannerTermCrnProperties> crnProps = BannerTermCrnProperties.findAllBannerTermCrnPropertiesForBannerSessions(getBannerSessionIdsConvertedToLongs());
			if (crnProps.size() > 1)
				action.addFieldError("form.bannerTermProperties", "Banner Term Properties for the banner term code already exist for one or more selected Banner Sessions");
            if (crnProps.size() == 1) {
            	    BannerTermCrnProperties termProps = crnProps.iterator().next();
				if (bannerTermPropertiesId==null && termProps!=null)
					action.addFieldError("form.bannerTermProperties", "Banner Term Properties for the banner term code and banner session combination already exists");
					
				if (bannerTermPropertiesId!=null && termProps!=null) {
					if (!bannerTermPropertiesId.equals(termProps.getUniqueId()))
						action.addFieldError("form.sessionId", "Another Banner Term Properties for the same banner term code and banner session combination already exists");
				}
            }
            for (String bsIdStr : getBannerSessionIds()) {
            		BannerSession bs = BannerSession.getBannerSessionById(Long.valueOf(bsIdStr));
            	    if (!bs.getBannerTermCode().equals(bannerTermCode)) {
        				action.addFieldError("form.bannerTermProperties", "Banner Term Code (" + bs.getBannerTermCode() + ") for the BannerSession: " + bs.getLabel() + " does not match the selected Term Code(" + bannerTermCode + ")");            	    	
            	    }
            }
		}
	}

	/**
	 * @return the availableBannerTermCodes
	 */
	public ArrayList<BannerSession> getAvailableBannerTermCodes() {
		return availableBannerTermCodes;
	}

	/**
	 * @param availableBannerTermCodes the availableBannerTermCodes to set
	 */
	public void setAvailableBannerTermCodes(ArrayList<BannerSession> availableBannerTermCodes) {
		this.availableBannerTermCodes = availableBannerTermCodes;
	}


	/**
	 * @return the bannerTermProperties
	 */
	public Long getBannerTermPropertiesId() {
		return bannerTermPropertiesId;
	}


	/**
	 * @param bannerTermProperties the bannerTermProperties to set
	 */
	public void setBannerTermPropertiesId(Long bannerTermPropertiesId) {
		this.bannerTermPropertiesId = bannerTermPropertiesId;
	}



    
}
