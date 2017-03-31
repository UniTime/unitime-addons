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

package org.unitime.colleague.util;

import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.dao.ColleagueSectionDAO;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.util.DefaultExternalClassNameHelper;

/**
 * @author says
 *
 */
public class ColleagueExternalClassNameHelper extends DefaultExternalClassNameHelper {

	/**
	 * 
	 */
	public ColleagueExternalClassNameHelper() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassLabel(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	@Override
	public String getClassLabel(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return super.getClassLabel(clazz, courseOffering);
		} else {
			ColleagueSection cs = ColleagueSection.findColleagueSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, ColleagueSectionDAO.getInstance().getSession());
			if (cs != null){
				if (cs.getColleagueId() != null){
					return courseOffering.getCourseName()+" "+clazz.getItypeDesc().trim()+" "+ cs.getColleagueId();
				} else {
					return courseOffering.getCourseName()+" "+clazz.getItypeDesc().trim();					
				}
			} else {
				return super.getClassLabel(clazz, courseOffering);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassLabelWithTitle(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	@Override
	public String getClassLabelWithTitle(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return super.getClassLabelWithTitle(clazz, courseOffering);
		} else {
			ColleagueSection cs = ColleagueSection.findColleagueSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, ColleagueSectionDAO.getInstance().getSession());
	    	if (cs != null) {
	    		return courseOffering.getCourseNameWithTitle()+" "+clazz.getItypeDesc().trim()+" "+cs.getColleagueId();
	    	} else {
	    		return super.getClassLabelWithTitle(clazz, courseOffering);
	    	}
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassSuffix(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	@Override
	public String getClassSuffix(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return(clazz.getClassSuffix());			
		} else {
			ColleagueSection cs = ColleagueSection.findColleagueSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, ColleagueSectionDAO.getInstance().getSession());
			if (cs != null){
				return(cs.classSuffixFor(clazz));		
			} else {
				return(clazz.getClassSuffix());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getExternalId(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	@Override
	public String getExternalId(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return(clazz.getExternalUniqueId());
		} else {
			ColleagueSection cs = ColleagueSection.findColleagueSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, ColleagueSectionDAO.getInstance().getSession());
			if (cs != null) {
				if(cs.getColleagueId() == null){
					return(null);
				} else {
					return cs.getColleagueId();
				}
			} else {
				return(clazz.getExternalUniqueId());
			}
		}
	}

}
