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

import java.util.List;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.unitime.banner.model.base.BaseBannerConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;



/**
 * 
 * @author says
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "banner_config")
public class BannerConfig extends BaseBannerConfig {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public BannerConfig () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public BannerConfig (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/

	public static BannerConfig findBannerConfigForInstrOffrConfigAndCourseOffering(
			InstrOfferingConfig instrOfferingConfig, CourseOffering courseOffering, Session hibSession) {
		return hibSession
				.createQuery("select bc from BannerConfig bc where bc.bannerCourse.courseOfferingId = :courseOfferingId " +
						" and bc.instrOfferingConfigId = :configId", BannerConfig.class)
				.setParameter("configId", instrOfferingConfig.getUniqueId().longValue())
				.setParameter("courseOfferingId", courseOffering.getUniqueId().longValue())
				.setHibernateFlushMode(FlushMode.MANUAL)
				.setCacheable(false)
				.uniqueResult();
	}

	@SuppressWarnings("unchecked")
	public static List<BannerConfig> findBannerConfigsForInstrOfferingConfig(
			InstrOfferingConfig instrOfferingConfig, Session hibSession) {

		return hibSession
				.createQuery("select bc from BannerConfig bc where bc.instrOfferingConfigId = :configId", BannerConfig.class)
				.setParameter("configId", instrOfferingConfig.getUniqueId().longValue())
				.setHibernateFlushMode(FlushMode.MANUAL)
				.setCacheable(false)
				.list();
	}


}
