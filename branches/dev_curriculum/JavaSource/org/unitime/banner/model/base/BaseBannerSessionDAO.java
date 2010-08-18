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

import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao._RootDAO;
import org.unitime.banner.model.dao.BannerSessionDAO;

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
}
