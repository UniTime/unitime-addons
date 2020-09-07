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
package org.unitime.banner.model.base;

import java.util.List;

import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.dao._RootDAO;
import org.unitime.banner.model.dao.BannerConfigDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
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
