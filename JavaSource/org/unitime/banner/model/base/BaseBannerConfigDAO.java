/*
 * UniTime 3.2 (University Timetabling Application)
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
package org.unitime.banner.model.base;

import java.util.List;

import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.dao._RootDAO;
import org.unitime.banner.model.dao.BannerConfigDAO;

public abstract class BaseBannerConfigDAO extends _RootDAO<BannerConfig,Long> {

	private static BannerConfigDAO sInstance;

	public static BannerConfigDAO getInstance() {
		if (sInstance == null) sInstance = new BannerConfigDAO();
		return sInstance;
	}

	public Class<BannerConfig> getReferenceClass() {
		return BannerConfig.class;
	}

	@SuppressWarnings("unchecked")
	public List<BannerConfig> findByGradableItype(org.hibernate.Session hibSession, Long gradableItypeId) {
		return hibSession.createQuery("from BannerConfig x where x.gradableItype.uniqueId = :gradableItypeId").setLong("gradableItypeId", gradableItypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerConfig> findByBannerCourse(org.hibernate.Session hibSession, Long bannerCourseId) {
		return hibSession.createQuery("from BannerConfig x where x.bannerCourse.uniqueId = :bannerCourseId").setLong("bannerCourseId", bannerCourseId).list();
	}
}
