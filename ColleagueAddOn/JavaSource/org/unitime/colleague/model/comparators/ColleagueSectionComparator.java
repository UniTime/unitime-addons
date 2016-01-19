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

package org.unitime.colleague.model.comparators;

import java.util.Comparator;

import org.unitime.colleague.model.ColleagueSection;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.SubjectArea;


/**
 * @author says
 *
 */
public class ColleagueSectionComparator implements Comparator<ColleagueSection>{

	/**
	 * 
	 */
	public ColleagueSectionComparator() {
		super();
	}

    public int compare (ColleagueSection o1, ColleagueSection o2){
        // Check if objects are of class Instructional Offering
        if (! (o1 instanceof ColleagueSection)){
            throw new ClassCastException("o1 Class must be of type ColleagueSection");
        }
        if (! (o2 instanceof ColleagueSection)){
            throw new ClassCastException("o2 Class must be of type ColleagueSection");
        }
        
        ColleagueSection cs1 = (ColleagueSection) o1;
        ColleagueSection cs2 = (ColleagueSection) o2;

        // Same Colleague Section
        if (cs1.getUniqueId().equals(cs2.getUniqueId())){
            return 0;
        }
 
        CourseOffering co1 = cs1.getCourseOffering(null);
    	CourseOffering co2 = cs2.getCourseOffering(null);
    	
    	SubjectArea sa1 = cs1.getSubjectArea(null);
    	SubjectArea sa2 = cs2.getSubjectArea(null);
                
        if (co1 == null && co2 == null){
        	if (sa1 == null && sa2 == null){
        		return(cs1.getUniqueId().compareTo(cs2.getUniqueId()));
        	} 
    		if (sa1 == null && sa2 != null){
    			return(1);
    		}
    		if (sa1 != null && sa2 == null){
    			return(-1);
    		}
    		if (sa1.getSubjectAreaAbbreviation().equals(sa2.getSubjectAreaAbbreviation())){
    			if (cs1.getColleagueCourseNumber().equals(cs2.getColleagueCourseNumber())){
    				return(compareSectionIndexes(cs1, cs2));
    			} else {
    				return(cs1.getColleagueCourseNumber().compareTo(cs2.getColleagueCourseNumber()));
    			}
    		}
    		return(sa1.getSubjectAreaAbbreviation().compareTo(sa2.getSubjectAreaAbbreviation()));
        }
        if (co1 == null && co2 != null){
        	if (sa1 == null) {
        		return(1);
        	}
        	if (sa1.getSubjectAreaAbbreviation().equals(co2.getSubjectArea().getSubjectAreaAbbreviation())){
    			if (cs1.getColleagueCourseNumber().equals(cs2.getColleagueCourseNumber())){
    				return(compareSectionIndexes(cs1, cs2));
    			} else {
    				return(cs1.getColleagueCourseNumber().compareTo(cs2.getColleagueCourseNumber()));
    			}        		
        	} else {
        		return(sa1.getSubjectAreaAbbreviation().compareTo(co2.getSubjectArea().getSubjectAreaAbbreviation()));
        	}
        }
        if (co1 != null && co2 == null){
        	if (sa2 == null){
        		return(-1);
        	}
        	if (co1.getSubjectArea().getSubjectAreaAbbreviation().equals(sa2.getSubjectAreaAbbreviation())){
    			if (cs1.getColleagueCourseNumber().equals(cs2.getColleagueCourseNumber())){
    				return(compareSectionIndexes(cs1, cs2));
    			} else {
    				return(cs1.getColleagueCourseNumber().compareTo(cs2.getColleagueCourseNumber()));
    			}        		      		
        	} else {
        		return(co1.getSubjectArea().getSubjectAreaAbbreviation().compareTo(sa2.getSubjectAreaAbbreviation()));
        	}
        }
        if (co1.getUniqueId().equals(co2.getUniqueId())){
			if (cs1.getColleagueCourseNumber().equals(cs2.getColleagueCourseNumber())){
				return(compareSectionIndexes(cs1, cs2));
			} else {
				return(cs1.getColleagueCourseNumber().compareTo(cs2.getColleagueCourseNumber()));
			}        		      		        	
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
    
    private int compareSectionIndexes(ColleagueSection cs1, ColleagueSection cs2){
    	if (cs1.getSectionIndex() == null && cs2.getSectionIndex() == null){
    		if (cs1.getFirstClass() != null && cs2.getFirstClass() != null){
    			return(cs1.getFirstClass().getSectionNumber().compareTo(cs2.getFirstClass().getSectionNumber()));
    		} else if (cs1.getFirstClass() == null && cs2.getFirstClass() == null) {
    			return(0);
    		} else if (cs1.getFirstClass() == null) {
    			return(-1);
    		} else {
    			return(1);
    		}
    	} else if (cs1.getSectionIndex() == null) {
    		return(-1);
    	} else if (cs2.getSectionIndex() == null){
    		return(1);
    	} else {
    		return(cs1.getSectionIndex().compareTo(cs2.getSectionIndex()));
    	}
    	
    }

}
