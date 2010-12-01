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

import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.dao._RootDAO;
import org.unitime.banner.model.dao.BannerSectionDAO;

public abstract class BaseBannerSectionDAO extends _RootDAO<BannerSection,Long> {

	private static BannerSectionDAO sInstance;

	public static BannerSectionDAO getInstance() {
		if (sInstance == null) sInstance = new BannerSectionDAO();
		return sInstance;
	}

	public Class<BannerSection> getReferenceClass() {
		return BannerSection.class;
	}

	@SuppressWarnings("unchecked")
	public List<BannerSection> findByBannerConfig(org.hibernate.Session hibSession, Long bannerConfigId) {
		return hibSession.createQuery("from BannerSection x where x.bannerConfig.uniqueId = :bannerConfigId").setLong("bannerConfigId", bannerConfigId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerSection> findByConsentType(org.hibernate.Session hibSession, Long consentTypeId) {
		return hibSession.createQuery("from BannerSection x where x.consentType.uniqueId = :consentTypeId").setLong("consentTypeId", consentTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerSection> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from BannerSection x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerSection> findByParentBannerSection(org.hibernate.Session hibSession, Long parentBannerSectionId) {
		return hibSession.createQuery("from BannerSection x where x.parentBannerSection.uniqueId = :parentBannerSectionId").setLong("parentBannerSectionId", parentBannerSectionId).list();
	}
}
