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

package org.unitime.banner.util;

import org.dom4j.Element;
import org.unitime.banner.interfaces.ExternalBannerCampusCodeElementHelperInterface;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.model.Class_;

/**
 * 
 * @author says
 *
 */
public class SampleExternalBannerCampusCodeElementHelper implements
		ExternalBannerCampusCodeElementHelperInterface {

	@Override
	public String getDefaultCampusCode(BannerSection bannerSection,
			BannerSession bannerSession, Class_ clazz) {
		String campusCode = bannerSession.getBannerCampus();
		if (bannerSection.getSession().getAcademicInitiative().equals("PWL") && clazz.getManagingDept() != null && clazz.getManagingDept().getDeptCode().equals("1589")){
			campusCode = "CEC";
		}
		return (campusCode);
	}

}
