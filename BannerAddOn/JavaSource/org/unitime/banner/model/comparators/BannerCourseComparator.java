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

package org.unitime.banner.model.comparators;

import java.util.Comparator;

import org.unitime.banner.model.BannerCourse;
import org.unitime.timetable.model.CourseOffering;


/**
 * @author says
 *
 */
public class BannerCourseComparator implements Comparator<BannerCourse>{

	/**
	 * 
	 */
	public BannerCourseComparator() {
		super();
	}

    public int compare (BannerCourse o1, BannerCourse o2){
        // Check if objects are of class Instructional Offering
        if (! (o1 instanceof BannerCourse)){
            throw new ClassCastException("o1 Class must be of type BannerCourse");
        }
        if (! (o2 instanceof BannerCourse)){
            throw new ClassCastException("o2 Class must be of type BannerCourse");
        }
        
        BannerCourse bc1 = (BannerCourse) o1;
        BannerCourse bc2 = (BannerCourse) o2;

        // Same Banner Course Offering 
        if (bc1.getUniqueId().equals(bc2.getUniqueId())){
            return 0;
        }
 
        CourseOffering co1 = bc1.getCourseOffering(null);
    	CourseOffering co2 = bc2.getCourseOffering(null);
                
        if (co1 == null && co2 == null){
        	return(bc1.getUniqueId().compareTo(bc2.getUniqueId()));
        }
        if (co1 == null && co2 != null){
        	return(1);
        }
        if (co1 != null && co2 == null){
        	return(-1);
        }
        // Compare by course name
        if (co1.getSubjectAreaAbbv().equals(co2.getSubjectAreaAbbv())){
            if (co1.getCourseNbr().equals(co1.getCourseNbr())){
         		if (co1.getTitle() == null && co2.getTitle() == null){
        			return(0);
        		} else if (co1.getTitle() == null){
        			return(-1);
        		} else if (co2.getTitle() == null){
        			return(1);
        		}
        		return(co1.getTitle().compareTo(co2.getTitle()));
            } else {
            	return(co1.getCourseNbr().compareTo(co2.getCourseNbr()));
            }
        } else {
            return(co1.getSubjectAreaAbbv().compareTo(co2.getSubjectAreaAbbv()));
        }                  
    } 

}
