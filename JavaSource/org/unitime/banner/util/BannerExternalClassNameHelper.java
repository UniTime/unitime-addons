/*
 * UniTime 3.1 (University Timetabling Application)
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
