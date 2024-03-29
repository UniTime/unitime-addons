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
package org.unitime.banner.model.dao;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
import java.util.List;
import org.unitime.banner.model.BannerSection;

public class BannerSectionDAO extends _RootDAO<BannerSection,Long> {
	private static BannerSectionDAO sInstance;

	public BannerSectionDAO() {}

	public static BannerSectionDAO getInstance() {
		if (sInstance == null) sInstance = new BannerSectionDAO();
		return sInstance;
	}

	public Class<BannerSection> getReferenceClass() {
		return BannerSection.class;
	}

	@SuppressWarnings("unchecked")
	public List<BannerSection> findByBannerConfig(org.hibernate.Session hibSession, Long bannerConfigId) {
		return hibSession.createQuery("from BannerSection x where x.bannerConfig.uniqueId = :bannerConfigId", BannerSection.class).setParameter("bannerConfigId", bannerConfigId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerSection> findByConsentType(org.hibernate.Session hibSession, Long consentTypeId) {
		return hibSession.createQuery("from BannerSection x where x.consentType.uniqueId = :consentTypeId", BannerSection.class).setParameter("consentTypeId", consentTypeId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerSection> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from BannerSection x where x.session.uniqueId = :sessionId", BannerSection.class).setParameter("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerSection> findByParentBannerSection(org.hibernate.Session hibSession, Long parentBannerSectionId) {
		return hibSession.createQuery("from BannerSection x where x.parentBannerSection.uniqueId = :parentBannerSectionId", BannerSection.class).setParameter("parentBannerSectionId", parentBannerSectionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerSection> findByBannerCampusOverride(org.hibernate.Session hibSession, Long bannerCampusOverrideId) {
		return hibSession.createQuery("from BannerSection x where x.bannerCampusOverride.uniqueId = :bannerCampusOverrideId", BannerSection.class).setParameter("bannerCampusOverrideId", bannerCampusOverrideId).list();
	}
}
