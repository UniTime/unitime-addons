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

import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao._RootDAO;
import org.unitime.banner.model.dao.BannerSessionDAO;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
public abstract class BaseBannerSessionDAO extends _RootDAO<BannerSession,Long> {

	private static BannerSessionDAO sInstance;

	public static BannerSessionDAO getInstance() {
		if (sInstance == null) sInstance = new BannerSessionDAO();
		return sInstance;
	}

	public Class<BannerSession> getReferenceClass() {
		return BannerSession.class;
	}

	@SuppressWarnings("unchecked")
	public List<BannerSession> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from BannerSession x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerSession> findByFutureSession(org.hibernate.Session hibSession, Long futureSessionId) {
		return hibSession.createQuery("from BannerSession x where x.futureSession.uniqueId = :futureSessionId").setLong("futureSessionId", futureSessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerSession> findByBannerTermCrnProperties(org.hibernate.Session hibSession, Long bannerTermCrnPropertiesId) {
		return hibSession.createQuery("from BannerSession x where x.bannerTermCrnProperties.uniqueId = :bannerTermCrnPropertiesId").setLong("bannerTermCrnPropertiesId", bannerTermCrnPropertiesId).list();
	}
}
