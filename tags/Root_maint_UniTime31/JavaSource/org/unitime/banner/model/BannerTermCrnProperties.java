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

package org.unitime.banner.model;

import java.util.Collection;

import org.hibernate.Session;
import org.unitime.banner.model.base.BaseBannerTermCrnProperties;
import org.unitime.banner.model.dao.BannerTermCrnPropertiesDAO;


/**
 * 
 * @author says
 *
 */
public class BannerTermCrnProperties extends BaseBannerTermCrnProperties {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public BannerTermCrnProperties () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public BannerTermCrnProperties (java.lang.Long uniqueId) {
		super(uniqueId);
	}

	/**
	 * Constructor for required fields
	 */
	public BannerTermCrnProperties (
		java.lang.Long uniqueId,
		java.lang.String bannerTermCode,
		java.lang.Integer lastCrn,
		java.lang.Boolean searchFlag,
		java.lang.Integer minCrn,
		java.lang.Integer maxCrn) {

		super (
			uniqueId,
			bannerTermCode,
			lastCrn,
			searchFlag,
			minCrn,
			maxCrn);
	}

/*[CONSTRUCTOR MARKER END]*/

	public static BannerTermCrnProperties findBannerTermCrnPropertiesForTermCode(
			String bannerTermCode, Session session) {

		return((BannerTermCrnProperties)BannerTermCrnPropertiesDAO.getInstance().getQuery("from BannerTermCrnProperties btcp where btcp.bannerTermCode = :bannerTermCode", session).setString("bannerTermCode", bannerTermCode).uniqueResult());
	}

	public static BannerTermCrnProperties getBannerTermCrnPropertiesById(Long id) {
		return(BannerTermCrnPropertiesDAO.getInstance().get(id));
	}

	public static Collection getAllBannerTermCrnProperties() {
		return(BannerTermCrnPropertiesDAO.getInstance().getQuery("from BannerTermCrnProperties").list());
	}


}