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
package org.unitime.colleague.model.base;

import java.util.List;

import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.dao.ColleagueSessionDAO;
import org.unitime.colleague.model.dao._RootDAO;

public abstract class BaseColleagueSessionDAO extends _RootDAO<ColleagueSession,Long> {

	private static ColleagueSessionDAO sInstance;

	public static ColleagueSessionDAO getInstance() {
		if (sInstance == null) sInstance = new ColleagueSessionDAO();
		return sInstance;
	}

	public Class<ColleagueSession> getReferenceClass() {
		return ColleagueSession.class;
	}

	@SuppressWarnings("unchecked")
	public List<ColleagueSession> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from ColleagueSession x where x.session.uniqueId = :sessionId").setLong("sessionId", sessionId).list();
	}
}
