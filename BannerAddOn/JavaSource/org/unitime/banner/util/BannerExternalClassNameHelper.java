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

import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.dao.BannerSectionDAO;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;

/**
 * @author says
 *
 */
public class BannerExternalClassNameHelper implements
		ExternalClassNameHelperInterface {

	/**
	 * 
	 */
	public BannerExternalClassNameHelper() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassLabel(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	public String getClassLabel(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return(clazz.getClassLabel());
		} else {
			BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, BannerSectionDAO.getInstance().getSession());
			if (bs != null){
				return courseOffering.getCourseName()+" "+clazz.getItypeDesc().trim()+" "+ bs.getCrn().toString();
			} else {
				return(clazz.getClassLabel());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassLabelWithTitle(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	public String getClassLabelWithTitle(Class_ clazz,
			CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return(clazz.getClassLabelWithTitle());
		} else {
			BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, BannerSectionDAO.getInstance().getSession());
	    	if (bs != null) {
	    		return courseOffering.getCourseNameWithTitle()+" "+clazz.getItypeDesc().trim()+" "+bs.getCrn().toString();
	    	} else {
	    		return(clazz.getClassLabelWithTitle());
	    	}
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassSuffix(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	public String getClassSuffix(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return(clazz.getClassSuffix());			
		} else {
			BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, BannerSectionDAO.getInstance().getSession());
			if (bs != null){
				return bs.getCrn().toString() + '-' + bs.getSectionIndex() + (courseOffering.getInstructionalOffering().getCourseOfferings().size() > 1?"*":"");
			} else {
				return(clazz.getClassSuffix());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getExternalId(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	public String getExternalId(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return(clazz.getExternalUniqueId());
		} else {
			BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, BannerSectionDAO.getInstance().getSession());
			if (bs != null) {
				return bs.getCrn().toString();
			} else {
				return(clazz.getExternalUniqueId());
			}
		}
	}

}
