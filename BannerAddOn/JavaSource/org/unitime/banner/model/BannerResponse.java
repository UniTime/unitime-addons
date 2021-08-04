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

import java.text.NumberFormat;
import java.text.SimpleDateFormat;

import org.unitime.banner.model.base.BaseBannerResponse;

/**
 * 
 * based on code contributed by Dagmar Murray
 *
 */
public class BannerResponse extends BaseBannerResponse {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4615752901037787775L;

	   public int compareTo(Object obj) {
	        if (obj==null || !(obj instanceof BannerResponse)) return -1;
	        BannerResponse chl = (BannerResponse)obj;
	        int cmp = getActivityDate().compareTo(chl.getActivityDate());
	        if (cmp!=0) return cmp;
	        return getUniqueId().compareTo(chl.getUniqueId());
	    }

    public static SimpleDateFormat sDF = new SimpleDateFormat("MM/dd/yy hh:mmaa");
    
    public String dateSortOrder() {
    	
           NumberFormat nf = NumberFormat.getInstance();
           nf.setMaximumIntegerDigits(15);
           nf.setMinimumIntegerDigits(15);
           nf.setGroupingUsed(false);
    	
    	return(new SimpleDateFormat("yyyyMMddHHmmss").format(this.getActivityDate()) + nf.format(this.getSequenceNumber()));   			
    }
     
	public String filterLabelShort() {
		return getSubjectCode() + " " + getCourseNumber() + " " + getCrn();
	}
	
	public String filterLabelLong() {
		return getSubjectCode() + " " + getCourseNumber() + " " + getCrn() + " - " + getSectionNumber();
	}
	
	
	
	
	
	
	
	
}
