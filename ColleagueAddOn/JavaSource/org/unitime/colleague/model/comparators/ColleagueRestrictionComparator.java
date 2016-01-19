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

import org.unitime.colleague.model.ColleagueRestriction;


/**
 * @author says
 *
 */
public class ColleagueRestrictionComparator implements Comparator<ColleagueRestriction>{

	/**
	 * 
	 */
	public ColleagueRestrictionComparator() {
		super();
	}

    public int compare (ColleagueRestriction o1, ColleagueRestriction o2){
        // Check if objects are of class Instructional Offering
        if (! (o1 instanceof ColleagueRestriction)){
            throw new ClassCastException("o1 Class must be of type ColleagueRestriction");
        }
        if (! (o2 instanceof ColleagueRestriction)){
            throw new ClassCastException("o2 Class must be of type ColleagueRestriction");
        }
        
        ColleagueRestriction cr1 = (ColleagueRestriction) o1;
        ColleagueRestriction cr2 = (ColleagueRestriction) o2;

        // Same ColleagueRestriction 
        if (cr1.getUniqueId().equals(cr2.getUniqueId())){
            return 0;
        }
        
        return(cr1.getCode().compareTo(cr2.getCode()));
                
    } 

}
