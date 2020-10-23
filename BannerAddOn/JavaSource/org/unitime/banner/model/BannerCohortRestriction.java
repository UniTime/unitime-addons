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

import org.unitime.banner.model.base.BaseBannerCohortRestriction;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;

public class BannerCohortRestriction extends BaseBannerCohortRestriction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2935292588188333916L;

	public BannerCohortRestriction() {
		super();
	}
	
	public BannerCohortRestriction clone() {
		BannerCohortRestriction bcr = new BannerCohortRestriction();
		bcr.setBannerSection(this.getBannerSection());
		bcr.setCohort(this.getCohort());
		bcr.setRemoved(this.getRemoved());
		bcr.setRestrictionAction(this.getRestrictionAction());
		return(bcr);
	}
	
	public static BannerCohortRestriction createFromInstructionalMethodCohortRestriction(BannerInstrMethodCohortRestriction bannerInstrMethodCohortRestriction) {
		BannerCohortRestriction bcr = new BannerCohortRestriction();
		bcr.setCohort(bannerInstrMethodCohortRestriction.getCohort());
		bcr.setRemoved(bannerInstrMethodCohortRestriction.getRemoved());
		bcr.setRestrictionAction(bannerInstrMethodCohortRestriction.getRestrictionAction());
		return(bcr);
	}
	
	public boolean matches(BannerInstrMethodCohortRestriction bannerInstrMethodCohortRestriction) {
		if (this.getBannerSection() != null 
				&& this.getBannerSection().getSession().equals(bannerInstrMethodCohortRestriction.getSession())) {
			InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(this.getBannerSection().getBannerConfig().getInstrOfferingConfigId());
			if (ioc != null 
					&& ioc.getEffectiveInstructionalMethod() != null 
					&& ioc.getEffectiveInstructionalMethod().equals(bannerInstrMethodCohortRestriction.getInstructionalMethod())
					&& this.getCohort() != null 
					&& this.getCohort().equals(bannerInstrMethodCohortRestriction.getCohort())) {
				return(true);
			}	
		}		
		return(false);
	}
	
	public String restrictionText() {
		StringBuilder sb = new StringBuilder();
		sb.append(BannerLastSentSectionRestriction.restrictionActionLabel(getRestrictionAction()))
		  .append(" ")
		  .append(getCohort().getGroupAbbreviation());
		if (isRemoved()) {
			sb.append(" - ")
			  .append(MESSAGES.labelRestrictionRemoved());
		}
		return(sb.toString());
	}
}
