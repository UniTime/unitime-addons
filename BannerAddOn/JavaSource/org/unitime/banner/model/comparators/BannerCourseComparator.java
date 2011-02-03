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
