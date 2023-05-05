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
import org.unitime.banner.model.BannerCohortRestriction;

public class BannerCohortRestrictionDAO extends _RootDAO<BannerCohortRestriction,Long> {
	private static BannerCohortRestrictionDAO sInstance;

	public BannerCohortRestrictionDAO() {}

	public static BannerCohortRestrictionDAO getInstance() {
		if (sInstance == null) sInstance = new BannerCohortRestrictionDAO();
		return sInstance;
	}

	public Class<BannerCohortRestriction> getReferenceClass() {
		return BannerCohortRestriction.class;
	}

	@SuppressWarnings("unchecked")
	public List<BannerCohortRestriction> findByCohort(org.hibernate.Session hibSession, Long cohortId) {
		return hibSession.createQuery("from BannerCohortRestriction x where x.cohort.uniqueId = :cohortId", BannerCohortRestriction.class).setParameter("cohortId", cohortId).list();
	}
}
