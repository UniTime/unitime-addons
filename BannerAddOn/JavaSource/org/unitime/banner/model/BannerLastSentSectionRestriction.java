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

import org.hibernate.Session;
import org.unitime.banner.model.base.BaseBannerLastSentSectionRestriction;
import org.unitime.banner.model.dao.BannerLastSentSectionRestrictionDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;

public abstract class BannerLastSentSectionRestriction extends BaseBannerLastSentSectionRestriction {
	/**
	 * 
	 */
	private static final long serialVersionUID = -421274187778959532L;
	
	public static String restrictionActionInclude = "I";
	public static String restrictionActionExclude = "E";
	protected static final BannerGwtMessages MESSAGES = Localization.create(BannerGwtMessages.class);
	
	public static String restrictionActionLabel(String restrictionAction) {
		if (restrictionActionInclude.equals(restrictionAction)) {
			return(MESSAGES.labelInclude());
		}
		if (restrictionActionExclude.equals(restrictionAction)) {
			return(MESSAGES.labelExclude());
		}
		return("");
	}
	
	public String restrictionActionLabel() {
		return(restrictionActionLabel(getRestrictionAction()));
	}

	public BannerLastSentSectionRestriction() {
		super();
	}

	public static boolean areRestrictionsDefinedForTerm(Long sessionId) {
		Session hibSession = BannerLastSentSectionRestrictionDAO.getInstance().getSession();
		String query1 = "select count(blssr) from BannerLastSentSectionRestriction blssr where blssr.bannerSection.session.uniqueId = :sessId";
		int count1 = ((Long) hibSession.createQuery(query1).setLong("sessId", sessionId).uniqueResult()).intValue();
		if (count1 > 0) {
			return(true);
		}
		String query2 = "select count(bimcr) from BannerInstrMethodCohortRestriction bimcr where bimcr.session.uniqueId = :sessId";
		int count2 = ((Long) hibSession.createQuery(query2).setLong("sessId", sessionId).uniqueResult()).intValue();
		if (count2 > 0) {
			return(true);
		}
		return false;
	}

}
