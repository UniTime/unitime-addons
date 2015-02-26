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

import java.util.Collection;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.hibernate.criterion.Order;
import org.unitime.banner.model.base.BaseBannerCampusOverride;
import org.unitime.banner.model.dao.BannerCampusOverrideDAO;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;



/**
 * 
 * @author says
 *
 */
public class BannerCampusOverride extends BaseBannerCampusOverride {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	
	/**
	 * Retrieves all Banner Campus Overrides in the database
	 * ordered by column bannerCampusCode
	 * @return List of BannerCampusOverride objects
	 */
    public static List<BannerCampusOverride> getBannerCampusOverrideList() {
    	return BannerCampusOverrideDAO.getInstance().findAll(Order.asc("bannerCampusCode"));
    }


/*[CONSTRUCTOR MARKER BEGIN]*/
	public BannerCampusOverride () {
		super();
		this.setVisible(new Boolean(true));
	}

	/**
	 * Constructor for primary key
	 */
	public BannerCampusOverride (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/

	public static BannerCampusOverride getBannerCampusOverrideById(Long id) {
		return(BannerCampusOverrideDAO.getInstance().get(id));
	}

	@SuppressWarnings("rawtypes")
	public static Collection getAllBannerCampusOverrides() {
		return(BannerCampusOverrideDAO.getInstance().getQuery("from BannerCampusOverride").list());
	}
			
	public static BannerCampusOverride getBannerCampusOverrideForCode(String bannerCampusCode) {
		return((BannerCampusOverride)BannerCampusOverrideDAO.getInstance().getQuery("from BannerCampusOverride where bannerCampusCode = :code").setString("code", bannerCampusCode).uniqueResult());
	}
}