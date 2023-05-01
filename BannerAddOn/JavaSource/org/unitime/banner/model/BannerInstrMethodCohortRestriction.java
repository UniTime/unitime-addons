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
package org.unitime.banner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.ArrayList;

import org.unitime.banner.model.base.BaseBannerInstrMethodCohortRestriction;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.Session;

@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
@Table(name = "banner_inst_method_cohort_rstrct")
public class BannerInstrMethodCohortRestriction extends BaseBannerInstrMethodCohortRestriction {

	/**
	 * 
	 */
	private static final long serialVersionUID = -97006878645166020L;

	public BannerInstrMethodCohortRestriction() {
		super();
	}

	public static ArrayList<BannerInstrMethodCohortRestriction> findAllWithSameTermAndMethod(BannerInstrMethodCohortRestriction bannerInstrMethodCohortRestriction, org.hibernate.Session hibSession){
		return(findAllWithTermAndMethod(bannerInstrMethodCohortRestriction.getSession().getUniqueId(), bannerInstrMethodCohortRestriction.getInstructionalMethod().getUniqueId(), hibSession));		
	}
	public static ArrayList<BannerInstrMethodCohortRestriction> findAllWithTermAndMethod(Session acadSession, InstructionalMethod instructionalMethod, org.hibernate.Session hibSession) {
		return(findAllWithTermAndMethod(acadSession.getUniqueId(), instructionalMethod.getUniqueId(), hibSession));
	}
	
	public static ArrayList<BannerInstrMethodCohortRestriction> findAllWithTermAndMethod(Long acadSessionId, Long instructionalMethodId, org.hibernate.Session hibSession) {
	    String query = "from BannerInstrMethodCohortRestriction bimcr where bimcr.session.uniqueId = :sessId and bimcr.instructionalMethod.uniqueId = :imId";
	    ArrayList<BannerInstrMethodCohortRestriction> restrictions = new ArrayList<BannerInstrMethodCohortRestriction>();
	    restrictions.addAll(hibSession.createQuery(query, BannerInstrMethodCohortRestriction.class).setParameter("sessId", acadSessionId).setParameter("imId", instructionalMethodId).list());
	    return(restrictions);
	}
}
