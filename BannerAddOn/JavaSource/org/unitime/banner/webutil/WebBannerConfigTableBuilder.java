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

package org.unitime.banner.webutil;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.jsp.JspWriter;

import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.webutil.Navigation;


/**
 * @author says
 *
 */
public class WebBannerConfigTableBuilder extends WebBannerCourseListTableBuilder {

	/**
	 * 
	 */
	public WebBannerConfigTableBuilder() {
	}
	
	@Override
	public boolean isShowDemand() {
		return true;
	}

    public void htmlConfigTablesForBannerOffering(
    		ClassAssignmentProxy classAssignment, 
            Long instructionalOffering, 
    		Long bannerCourse,
            SessionContext context,
            JspWriter outputStream,
            String backType,
            String backId){
    	
    	setBackType(backType);
        setBackId(backId);    	
    	
    	if (CommonValues.Yes.eq(UserProperty.ClassesKeepSort.get(context.getUser()))) {
    		setClassComparator(
        			new ClassCourseComparator(
        					UserData.getProperty(context.getUser().getExternalUserId(),"InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
        					classAssignment,
        					false
        			)
    		);
    	}
    	BannerCourse bc = null;
    	if (bannerCourse != null && context.getUser() != null){
    		bc = BannerCourseDAO.getInstance().get(bannerCourse);
    	}
       	if (instructionalOffering != null && context.getUser() != null){
	        InstructionalOfferingDAO iDao = new InstructionalOfferingDAO();
	        InstructionalOffering io = iDao.get(instructionalOffering);
	        
			setUserSettings(context.getUser());
			
			Vector subpartIds = new Vector();

			if (io.getInstrOfferingConfigs() != null){
	        	TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
	        	configs.addAll(io.getInstrOfferingConfigs());
	        	InstrOfferingConfig ioc = null;
	        	int idx = 0;
	        	for(Iterator it = configs.iterator(); it.hasNext();idx++){
	        		ioc = (InstrOfferingConfig) it.next();
	        		if (idx>0) {
	        			try {
	        				outputStream.println("<br><br>");
	        			} catch (IOException e) {}
	        		}
	        		this.htmlTableForBannerOfferingConfig(subpartIds, classAssignment, ioc, bc, context, outputStream);
	        	}
	        }
			
			Navigation.set(context, Navigation.sSchedulingSubpartLevel, subpartIds);
       	}
    }

    private void htmlTableForBannerOfferingConfig(
    		Vector subpartIds,
    		ClassAssignmentProxy classAssignment,
            InstrOfferingConfig ioc,
            BannerCourse bannerCourse,
            SessionContext context,
            JspWriter outputStream){
    	
    	if (CommonValues.Yes.eq(UserProperty.ClassesKeepSort.get(context.getUser()))) {
    		setClassComparator(
        			new ClassCourseComparator(
        					UserData.getProperty(context.getUser().getExternalUserId(),"InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
        					classAssignment,
        					false
        			)
    		);
    	}

    	if (ioc != null && context.getUser() != null){
	        
	        this.setDisplayDistributionPrefs(false);
	        
	        
	        boolean isEditable = context.hasPermission(ioc, Right.InstrOfferingConfigEdit);
	        boolean isFullyEditable = context.hasPermission(ioc, Right.InstrOfferingConfigEdit); //config is editable PLUS all subparts are editable as well
	        boolean isExtManaged = false;
	        if (!isEditable) {
	        	
	            isExtManaged = context.hasPermission(ioc, Right.InstrOfferingConfigEditSubpart);
	        }
	        boolean isLimitedEditable = false;
	        if (ioc.hasClasses()) {
	        	for (Iterator i=ioc.getSchedulingSubparts().iterator();i.hasNext();) {
	        		SchedulingSubpart ss = (SchedulingSubpart)i.next();
	        		if (context.hasPermission(ss, Right.SchedulingSubpartEdit)) {
	        			isLimitedEditable = true;
	        			
	        		}
	        		if (context.hasPermission(ss, Right.SchedulingSubpartEdit))
	        			isFullyEditable = false;
	        	}
	        }	        

    		try {
    			outputStream.write(this.buttonsTable(ioc, bannerCourse, isEditable, isFullyEditable, isLimitedEditable, isExtManaged));
    		} catch (IOException e) {}
       	TableStream configTable = this.initTable(outputStream, context.getUser().getCurrentAcademicSessionId() == null?null: context.getUser().getCurrentAcademicSessionId());
         	this.buildSectionConfigRow(subpartIds, classAssignment, bannerCourse, configTable, ioc, context, false, false);
         	configTable.tableComplete();
	    }
    }

	public String buttonsTable(InstrOfferingConfig ioc, BannerCourse bannerCourse, boolean isEditable, boolean isFullyEditable, boolean isLimitedEditable, boolean isExtManaged){
		StringBuffer btnTable = new StringBuffer("");
		btnTable.append("<table class='BottomBorder' width='100%'><tr><td width='100%' nowrap>");
		btnTable.append("<DIV class='WelcomeRowHeadNoLine'>");
		String configName = ioc.getName();
	    if (configName==null || configName.trim().length()==0) configName = ioc.getUniqueId().toString();
		btnTable.append(MSG.labelConfiguration(configName));
		btnTable.append("</DIV>");
		btnTable.append("</td><td style='padding-bottom: 3px' nowrap>");
		boolean notOffered = ioc.getInstructionalOffering().isNotOffered().booleanValue();
		if (!notOffered && (isEditable || isLimitedEditable || isExtManaged)) {
	        btnTable.append("<table border='0' align='right' cellspacing='1' cellpadding='0'>");
	        
	        if ((isEditable || isExtManaged) && ioc.hasClasses()) {
		        btnTable.append("<td>");
		        btnTable.append("	<form method='post' action='bannerOfferingModify.action' class='FormWithNoPadding'>");
		        btnTable.append("		<input type='hidden' name='uid' value='" + ioc.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='hidden' name='bc' value='" + bannerCourse.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='submit' name='op' value='" + BMSG.actionEditBannerConfig() + "' title='" + BMSG.titleEditBannerConfig() + "' class='btn'> ");
		        btnTable.append("	</form>");
		        btnTable.append("</td>");
	        }
 
	        btnTable.append("</tr>");
	        btnTable.append("</table>");
	    }
		btnTable.append("</td></tr></table>");		
		return(btnTable.toString());
	}
	
	
}
