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