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
import org.unitime.banner.model.BannerInstrMethodCohortRestriction;

public class BannerInstrMethodCohortRestrictionDAO extends _RootDAO<BannerInstrMethodCohortRestriction,Long> {
	private static BannerInstrMethodCohortRestrictionDAO sInstance;

	public BannerInstrMethodCohortRestrictionDAO() {}

	public static BannerInstrMethodCohortRestrictionDAO getInstance() {
		if (sInstance == null) sInstance = new BannerInstrMethodCohortRestrictionDAO();
		return sInstance;
	}

	public Class<BannerInstrMethodCohortRestriction> getReferenceClass() {
		return BannerInstrMethodCohortRestriction.class;
	}

	@SuppressWarnings("unchecked")
	public List<BannerInstrMethodCohortRestriction> findBySession(org.hibernate.Session hibSession, Long sessionId) {
		return hibSession.createQuery("from BannerInstrMethodCohortRestriction x where x.session.uniqueId = :sessionId", BannerInstrMethodCohortRestriction.class).setParameter("sessionId", sessionId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerInstrMethodCohortRestriction> findByInstructionalMethod(org.hibernate.Session hibSession, Long instructionalMethodId) {
		return hibSession.createQuery("from BannerInstrMethodCohortRestriction x where x.instructionalMethod.uniqueId = :instructionalMethodId", BannerInstrMethodCohortRestriction.class).setParameter("instructionalMethodId", instructionalMethodId).list();
	}

	@SuppressWarnings("unchecked")
	public List<BannerInstrMethodCohortRestriction> findByCohort(org.hibernate.Session hibSession, Long cohortId) {
		return hibSession.createQuery("from BannerInstrMethodCohortRestriction x where x.cohort.uniqueId = :cohortId", BannerInstrMethodCohortRestriction.class).setParameter("cohortId", cohortId).list();
	}
}
