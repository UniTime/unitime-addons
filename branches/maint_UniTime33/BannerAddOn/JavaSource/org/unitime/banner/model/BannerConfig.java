/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/

package org.unitime.banner.model;

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
		return((BannerConfig)hibSession
				.createQuery("select bc from BannerConfig bc where bc.bannerCourse.courseOfferingId = :courseOfferingId " +
						" and bc.instrOfferingConfigId = :configId")
				.setLong("configId", instrOfferingConfig.getUniqueId().longValue())
				.setLong("courseOfferingId", courseOffering.getUniqueId().longValue())
				.setFlushMode(FlushMode.MANUAL)
				.setCacheable(false)
				.uniqueResult());
	}

	@SuppressWarnings("unchecked")
	public static List<BannerConfig> findBannerConfigsForInstrOfferingConfig(
			InstrOfferingConfig instrOfferingConfig, Session hibSession) {

		return((List<BannerConfig>)hibSession
				.createQuery("select bc from BannerConfig bc where bc.instrOfferingConfigId = :configId")
				.setLong("configId", instrOfferingConfig.getUniqueId().longValue())
				.setFlushMode(FlushMode.MANUAL)
				.setCacheable(false)
				.list());
	}


}