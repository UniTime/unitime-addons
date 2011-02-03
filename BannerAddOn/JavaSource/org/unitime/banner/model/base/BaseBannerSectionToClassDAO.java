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
package org.unitime.banner.model.base;

import java.util.List;

import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.banner.model.dao._RootDAO;
import org.unitime.banner.model.dao.BannerSectionToClassDAO;

public abstract class BaseBannerSectionToClassDAO extends _RootDAO<BannerSectionToClass,Long> {

	private static BannerSectionToClassDAO sInstance;

	public static BannerSectionToClassDAO getInstance() {
		if (sInstance == null) sInstance = new BannerSectionToClassDAO();
		return sInstance;
	}

	public Class<BannerSectionToClass> getReferenceClass() {
		return BannerSectionToClass.class;
	}

	@SuppressWarnings("unchecked")
	public List<BannerSectionToClass> findByBannerSection(org.hibernate.Session hibSession, Long bannerSectionId) {
		return hibSession.createQuery("from BannerSectionToClass x where x.bannerSection.uniqueId = :bannerSectionId").setLong("bannerSectionId", bannerSectionId).list();
	}
}
