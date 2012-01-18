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

package org.unitime.banner.webutil;

import java.io.IOException;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpSession;
import javax.servlet.jsp.JspWriter;

import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.commons.User;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Settings;
import org.unitime.timetable.model.UserData;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.Navigation;


/**
 * @author says
 *
 */
public class WebBannerConfigTableBuilder extends
		WebBannerCourseListTableBuilder {

	/**
	 * 
	 */
	public WebBannerConfigTableBuilder() {
		// TODO Auto-generated constructor stub
	}

    public void htmlConfigTablesForBannerOffering(
    		HttpSession session,
    		ClassAssignmentProxy classAssignment, 
            Long instructionalOffering, 
    		Long bannerCourse,
            User user,
            JspWriter outputStream,
            String backType,
            String backId){
    	
    	setBackType(backType);
        setBackId(backId);    	
    	
    	if ("yes".equals(Settings.getSettingValue(user, Constants.SETTINGS_KEEP_SORT))) {
    		setClassComparator(
        			new ClassCourseComparator(
        					UserData.getProperty(user.getId(),"InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
        					classAssignment,
        					false
        			)
    		);
    	}
    	BannerCourse bc = null;
    	if (bannerCourse != null && user != null){
    		bc = BannerCourseDAO.getInstance().get(bannerCourse);
    	}
       	if (instructionalOffering != null && user != null){
	        InstructionalOfferingDAO iDao = new InstructionalOfferingDAO();
	        InstructionalOffering io = iDao.get(instructionalOffering);
	        
			setUserSettings(user);
			
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
	        		this.htmlTableForBannerOfferingConfig(subpartIds, classAssignment, ioc, bc, user, outputStream);
	        	}
	        }
			
			Navigation.set(session, Navigation.sSchedulingSubpartLevel, subpartIds);
       	}
    }

    private void htmlTableForBannerOfferingConfig(
    		Vector subpartIds,
    		ClassAssignmentProxy classAssignment,
            InstrOfferingConfig ioc,
            BannerCourse bannerCourse,
            User user,
            JspWriter outputStream){
    	
    	if ("yes".equals(Settings.getSettingValue(user, Constants.SETTINGS_KEEP_SORT))) {
    		setClassComparator(
        			new ClassCourseComparator(
        					UserData.getProperty(user.getId(),"InstructionalOfferingList.sortBy",ClassCourseComparator.getName(ClassCourseComparator.SortBy.NAME)),
        					classAssignment,
        					false
        			)
    		);
    	}

    	if (ioc != null && user != null){
	        
	        this.setDisplayDistributionPrefs(false);
	        
	        
	        boolean isEditable = ioc.isEditableBy(user);
	        boolean isFullyEditable = ioc.isEditableBy(user); //config is editable PLUS all subparts are editable as well
	        boolean isExtManaged = false;
	        if (!isEditable) {
	            isExtManaged = ioc.hasExternallyManagedSubparts(user, true);
	        }
	        boolean isLimitedEditable = false;
	        if (ioc.hasClasses()) {
	        	for (Iterator i=ioc.getSchedulingSubparts().iterator();i.hasNext();) {
	        		SchedulingSubpart ss = (SchedulingSubpart)i.next();
	        		if (ss.isLimitedEditable(user)) {
	        			isLimitedEditable = true;
	        		}
	        		if (!ss.isEditableBy(user))
	        			isFullyEditable = false;
	        	}
	        }	        

    		try {
    			outputStream.write(this.buttonsTable(ioc, bannerCourse, isEditable, isFullyEditable, isLimitedEditable, isExtManaged));
    		} catch (IOException e) {}
       	TableStream configTable = this.initTable(outputStream, (Session.getCurrentAcadSession(user) == null?null:Session.getCurrentAcadSession(user).getUniqueId()));
         	this.buildSectionConfigRow(subpartIds, classAssignment, bannerCourse, configTable, ioc, user, false, false);
         	configTable.tableComplete();
	    }
    }

	public String buttonsTable(InstrOfferingConfig ioc, BannerCourse bannerCourse, boolean isEditable, boolean isFullyEditable, boolean isLimitedEditable, boolean isExtManaged){
		StringBuffer btnTable = new StringBuffer("");
		btnTable.append("<table class='BottomBorder' width='100%'><tr><td width='100%' nowrap>");
		btnTable.append("<DIV class='WelcomeRowHeadNoLine'>");
		String configName = ioc.getName();
	    if (configName==null || configName.trim().length()==0) configName = ioc.getUniqueId().toString();
		btnTable.append("Configuration "+configName);
		btnTable.append("</DIV>");
		btnTable.append("</td><td style='padding-bottom: 3px' nowrap>");
		boolean notOffered = ioc.getInstructionalOffering().isNotOffered().booleanValue();
		if (!notOffered && (isEditable || isLimitedEditable || isExtManaged)) {
	        btnTable.append("<table border='0' align='right' cellspacing='1' cellpadding='0'>");
	        
	        if ((isEditable || isExtManaged) && ioc.hasClasses()) {
		        btnTable.append("<td>");
		        btnTable.append("	<form method='post' action='bannerOfferingModify.do' class='FormWithNoPadding'>");
		        btnTable.append("		<input type='hidden' name='uid' value='" + ioc.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='hidden' name='bc' value='" + bannerCourse.getUniqueId().toString() + "'>");
		        btnTable.append("		<input type='submit' name='op' value='Edit' title='Edit Banner Configuration' class='btn'> ");
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
