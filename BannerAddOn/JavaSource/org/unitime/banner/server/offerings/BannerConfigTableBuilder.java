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
package org.unitime.banner.server.offerings;

import java.util.TreeSet;

import org.unitime.banner.model.BannerCourse;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.banner.BannerOfferingDetailPage.BannerOfferingDetailResponse;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingConfigInterface;
import org.unitime.timetable.model.ClassDurationType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;

public class BannerConfigTableBuilder extends BannerOfferingTableBuilder {
	
	public BannerConfigTableBuilder(SessionContext context, String backType, String backId) {
		super(context, backType, backId);
	}
	
	@Override
	public boolean isShowDemand() {
		return true;
	}
	
	public void generateConfigTablesForBannerOffering(
    		ClassAssignmentProxy classAssignment, 
    		InstructionalOffering io,
    		BannerCourse bc,
    		BannerOfferingDetailResponse response) {
    	
        if (CommonValues.Yes.eq(getUser().getProperty(UserProperty.ClassesKeepSort))) {
    		setClassComparator(
    			new ClassCourseComparator(
    					getUser().getProperty("InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
    					classAssignment,
    					false
    			)
    		);
    	}
		
		if (io.getInstrOfferingConfigs() != null){
        	TreeSet<InstrOfferingConfig> configs = new TreeSet<InstrOfferingConfig>(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
        	configs.addAll(io.getInstrOfferingConfigs());
        	for (InstrOfferingConfig ioc: configs) {
        		response.addConfig(generateTableForBannerConfig(classAssignment, ioc, bc));
        	}
        }
    }			
	
    private OfferingConfigInterface generateTableForBannerConfig(ClassAssignmentProxy classAssignment, InstrOfferingConfig ioc, BannerCourse bc) {
    	OfferingConfigInterface ret = new OfferingConfigInterface();
    	
    	ret.setConfigId(ioc.getUniqueId());
	    if (ioc.getInstructionalMethod() != null)
	    	ret.setName(MSG.labelConfigurationWithInstructionalMethod(ioc.getName(), ioc.getInstructionalMethod().getLabel()));
	    else
	    	ret.setName(MSG.labelConfiguration(ioc.getName()));

	    boolean notOffered = ioc.getInstructionalOffering().isNotOffered();
        boolean isEditable = getSessionContext().hasPermission(ioc, Right.InstrOfferingConfigEdit);
        boolean isExtManaged = false;
        if (!isEditable) isExtManaged = getSessionContext().hasPermission(ioc, Right.InstrOfferingConfigEditSubpart);
        boolean isLimitedEditable = false;
        if (ioc.hasClasses()) {
        	for (SchedulingSubpart ss: ioc.getSchedulingSubparts()) {
        		if (getSessionContext().hasPermission(ss, Right.SchedulingSubpartEdit))
        			isLimitedEditable = true;
        	}
        }
        
        if (!notOffered && (isEditable || isLimitedEditable || isExtManaged)) {
        	if ((isEditable || isExtManaged) && ioc.hasClasses()) {
        		ret.addOperation("banner-config-edit");
        	}
        }

    	setDisplayDistributionPrefs(false);

    	ClassDurationType dtype = ioc.getEffectiveDurationType();
        
        buildTableHeader(ret, getCurrentAcademicSessionId(), dtype == null ? MSG.columnMinPerWk() : dtype.getLabel());
        buildSectionConfigRow(classAssignment, ret, bc, ioc, false, false);
        ret.setAnchor("ioc" + ioc.getUniqueId());
        
        return ret;
    }

	

}
