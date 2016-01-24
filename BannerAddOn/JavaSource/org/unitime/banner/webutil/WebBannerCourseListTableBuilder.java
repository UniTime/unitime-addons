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
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.jsp.JspWriter;

import org.unitime.banner.form.BannerCourseListForm;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableHeaderCell;
import org.unitime.commons.web.htmlgen.TableRow;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.webutil.Navigation;
import org.unitime.timetable.webutil.WebInstructionalOfferingTableBuilder;


/**
 * @author says
 *
 */
public class WebBannerCourseListTableBuilder extends
		WebInstructionalOfferingTableBuilder {
 
	private Comparator iClassComparator = new ClassComparator(ClassComparator.COMPARE_BY_ITYPE);

	/**
	 * 
	 */
	public WebBannerCourseListTableBuilder() {
	}
	
	
    @SuppressWarnings("unchecked")
	public void htmlTableForBannerOfferings(
			SessionContext context,
            ClassAssignmentProxy classAssignment, 
            BannerCourseListForm form, 
            Long subjectAreaId,
            boolean displayHeader,
            boolean allCoursesAreGiven,
            JspWriter outputStream,
            String backType,
            String backId){
    	
    	setBackType(backType); setBackId(backId);
    	
    	htmlTableForBannerOfferings(context, classAssignment,
    			(TreeSet<InstructionalOffering>) form.getInstructionalOfferings(), 
     			subjectAreaId,
    			displayHeader, allCoursesAreGiven,
    			outputStream,
    			iClassComparator
    	);
    }
	
    public void htmlTableForBannerOfferings(
    		SessionContext context,
            ClassAssignmentProxy classAssignment, 
            TreeSet<InstructionalOffering> instructionalOfferings, 
            Long subjectAreaId,
            boolean displayHeader, boolean allCoursesAreGiven,
            JspWriter outputStream,
            Comparator classComparator){
    	
    	if (classComparator!=null)
    		setClassComparator(classComparator);
        
    	    	
        TreeMap<CourseOffering, InstructionalOffering> notOfferedOfferings = new TreeMap<CourseOffering, InstructionalOffering>(new CourseOfferingComparator());
        TreeMap<CourseOffering, InstructionalOffering> offeredOfferings = new TreeMap<CourseOffering, InstructionalOffering>(new CourseOfferingComparator());
        Vector offeringIds = new Vector();
        
        Iterator it = instructionalOfferings.iterator();
        CourseOffering co = null;
        InstructionalOffering io = null;
        boolean hasOfferedCourses = false;
        boolean hasNotOfferedCourses = false;
		setUserSettings(context.getUser());
        
         while (it.hasNext()){
            io = (InstructionalOffering) it.next();
            if (io.isNotOffered() == null || io.isNotOffered().booleanValue()){
            	hasNotOfferedCourses = true;
            	for(Iterator coIt = io.getCourseOfferings().iterator(); coIt.hasNext();){
            		co = (CourseOffering)coIt.next();
            		if (co.getSubjectArea().getUniqueId().equals(subjectAreaId)){
            			notOfferedOfferings.put(co,io);
            		}
            	}
            } else {
            	hasOfferedCourses = true;
            	for(Iterator coIt = io.getCourseOfferings().iterator(); coIt.hasNext();){
            		co = (CourseOffering)coIt.next();
            		if (co.getSubjectArea().getUniqueId().equals(subjectAreaId)){
            			offeredOfferings.put(co,io);
            		}
            	}
            }
        }
         
        if (hasOfferedCourses || allCoursesAreGiven) {
    		if(displayHeader) {
    		    try {
    		    	if (allCoursesAreGiven)
    		    		outputStream.print("<DIV align=\"right\"><A class=\"l7\" href=\"#notOffered\">Courses Not Offered</A></DIV>");
    			    outputStream.print("<DIV class=\"WelcomeRowHead\"><A name=\"offered\"></A>Offered Courses</DIV>");
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
    		}
                  
            if (hasOfferedCourses){
                it = offeredOfferings.keySet().iterator();
                TableStream offeredTable = this.initTable(outputStream, (context.getUser().getCurrentAcademicSessionId() == null?null:context.getUser().getCurrentAcademicSessionId()));
                
                while (it.hasNext()){
                    co = (CourseOffering) it.next();
                    io = (InstructionalOffering) offeredOfferings.get(co);
                    if (!offeringIds.contains(io.getUniqueId())){
                    	offeringIds.add(io.getUniqueId());
                    }
                    this.addBannerCourseRowsToTable(classAssignment, offeredTable, co, io, subjectAreaId, context);            	
                }
                offeredTable.tableComplete();
            } else {
                if(displayHeader)
    				try {
    					outputStream.print("<font class=\"error\">There are no courses currently offered for this subject.</font>");
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
            }
        }
        
        if (hasNotOfferedCourses || allCoursesAreGiven) {
            if(displayHeader) {
    	        try {
    				outputStream.print("<br>");
    				if (allCoursesAreGiven)
    					outputStream.print("<DIV align=\"right\"><A class=\"l7\" href=\"#offered\">Offered Courses</A></DIV>");
    		        outputStream.print("<DIV class=\"WelcomeRowHead\"><A name=\"notOffered\"></A>Not Offered Courses</DIV>");
    			} catch (IOException e) {
    				e.printStackTrace();
    			}
            }
            
            if (hasNotOfferedCourses){
                it = notOfferedOfferings.keySet().iterator();
                TableStream notOfferedTable = this.initTable(outputStream, (context.getUser().getCurrentAcademicSessionId() == null?null:context.getUser().getCurrentAcademicSessionId()));
                while (it.hasNext()){
                	co = (CourseOffering) it.next();
                    io = (InstructionalOffering) notOfferedOfferings.get(co);
                    if (!offeringIds.contains(io.getUniqueId())){
                    	offeringIds.add(io.getUniqueId());
                    }
                    this.addBannerCourseRowsToTable(classAssignment, notOfferedTable, co, io, subjectAreaId, context);            	
                }
                notOfferedTable.tableComplete();
            } else {
                if(displayHeader)
    				try {
    					outputStream.print("<font class=\"normal\">&nbsp;<br>All courses are currently being offered for this subject.</font>");
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
            }
        }
        
        Navigation.set(context, Navigation.sInstructionalOfferingLevel, offeringIds);
    }

    private String subjectOnClickAction(Long bannerCourseId){
        return("document.location='bannerOfferingDetail.do?op=view&bc=" + bannerCourseId.toString() + "';");
    }

	private void addBannerCourseRowsToTable(
			ClassAssignmentProxy classAssignment, TableStream table, CourseOffering co,
			InstructionalOffering io, Long subjectAreaId, SessionContext sessionContext) {
        boolean isEditable = sessionContext.hasPermission(io, Right.InstructionalOfferingDetail);
        if (!isEditable){
        	if (io.getInstrOfferingConfigs() != null && io.getInstrOfferingConfigs().size() > 0){
        		boolean canEdit = true;
        		Iterator it = io.getInstrOfferingConfigs().iterator();
        		InstrOfferingConfig ioc = null;
        		while(canEdit && it.hasNext()){
        			ioc = (InstrOfferingConfig) it.next();
        			if(!sessionContext.hasPermission(ioc, Right.InstrOfferingConfigEdit)){
        				canEdit = false;
        			}
        		}
        		isEditable = canEdit;
        	}
        }
        BannerCourse bc = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), BannerCourseDAO.getInstance().getSession());
        if (bc == null){
        	return;
        }
        TableRow row = (this.initRow(true));
        row.setOnMouseOver(this.getRowMouseOver(true, isEditable));
        row.setOnMouseOut(this.getRowMouseOut(true));
        row.setOnClick(subjectOnClickAction(bc.getUniqueId()));

    	row.addContent(subjectAndCourseInfo(bc, io, co));
        table.addContent(row);
        if (io.getInstrOfferingConfigs() != null & !io.getInstrOfferingConfigs().isEmpty()){
        	TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
        	configs.addAll(io.getInstrOfferingConfigs());
        	buildSectionConfigRows(classAssignment, table, bc, configs, sessionContext, true);
        }
    }
	
    private void buildSectionConfigRows(ClassAssignmentProxy classAssignment, TableStream table, BannerCourse bc, Set instrOfferingConfigs, SessionContext sessionContext, boolean printConfigLine) {
        Iterator it = instrOfferingConfigs.iterator();
        InstrOfferingConfig ioc = null;
        while (it.hasNext()){
            ioc = (InstrOfferingConfig) it.next();
            buildSectionConfigRow(null, classAssignment, bc, table, ioc, sessionContext, printConfigLine && instrOfferingConfigs.size()>1, true);
        }
    }

	protected void buildSectionConfigRow(Vector subpartIds, ClassAssignmentProxy classAssignment, BannerCourse bc, TableStream table, InstrOfferingConfig ioc, SessionContext sessionContext, boolean printConfigLine, boolean clickable) {
	    boolean isHeaderRow = true;
	    boolean isEditable = sessionContext.hasPermission(ioc, Right.InstrOfferingConfigEdit);
	    String configName = ioc.getName();
		if (printConfigLine) {
		    TableRow row = this.initRow(isHeaderRow);
	        if (clickable){
	            row.setOnClick(subjectOnClickAction(bc.getUniqueId()));
	        }
	        TableCell cell = null;
    	    if (configName==null || configName.trim().length()==0)
    	        configName = ioc.getUniqueId().toString();
    	    cell = this.initNormalCell("Configuration " + configName, isEditable);
    	    cell.setStyle("padding-left: " + indent + "px;");
        	cell.setNoWrap(true);
    	    row.addContent(cell);
    	    cell = this.initCell(" &nbsp;", 17, false);
    	    row.addContent(cell);    
	        table.addContent(row);
		}
        ArrayList subpartList = new ArrayList(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());
        Iterator it = subpartList.iterator();
        SchedulingSubpart ss = null;
        int ct = 0;
        while (it.hasNext()) {   	
			ss = (SchedulingSubpart) it.next();
			if (ss.getParentSubpart() == null) {
				if (ss.getClasses() != null) {
					Vector classes = new Vector(ss.getClasses());
					Collections.sort(classes,iClassComparator);
					Iterator cIt = classes.iterator();					
					Class_ c = null;
					while (cIt.hasNext()) {
						c = (Class_) cIt.next();
						buildSectionRows(classAssignment, ++ct, table, c, bc, 1, sessionContext, null, clickable);
					}
				}
			}
		}

   }

    
    


	private void buildSectionRows(ClassAssignmentProxy classAssignment,
			int ct, TableStream table,
			Class_ aClass, BannerCourse bc, int indentSpaces, SessionContext sessionContext,
			Integer prevItype, boolean clickable) {
		Integer currentItype = aClass.getSchedulingSubpart().getItype().getItype();
		if (aClass.isCancelled().booleanValue()){
			return;
		}
		if (prevItype == null || !prevItype.equals(currentItype)){
			buildSectionRow(classAssignment, ct, table, aClass, bc, indentSpaces, sessionContext, clickable);
		}
    	Set childClasses = aClass.getChildClasses();

    	if (childClasses != null && !childClasses.isEmpty()){
        
    	    ArrayList childClassesList = new ArrayList(childClasses);
            Collections.sort(childClassesList, iClassComparator);
            
            Iterator it = childClassesList.iterator();
            Class_ child = null;
            while (it.hasNext()){              
                child = (Class_) it.next();
                buildSectionRows(classAssignment, ct, table, child, bc, indentSpaces + (prevItype != null && prevItype.equals(currentItype) ? 0 : 1), sessionContext, currentItype, clickable);
            }
        }
		
	}
	
	private void buildSectionRow(ClassAssignmentProxy classAssignment,
			int ct, TableStream table,
			Class_ c, BannerCourse bc, int indentSpaces, SessionContext sessionContext, boolean clickable) {

		org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
    	boolean isHeaderRow = false;
        boolean isEditable = true;
    	TableRow row = this.initRow(isHeaderRow);
        if (clickable){
            row.setOnClick(subjectOnClickAction(bc.getUniqueId()));
        }
		BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOffering(c, bc.getCourseOffering(hibSession), hibSession);
		if (bs == null){
			return;
		}
    	TableCell cell = initNormalCell(bs.bannerSectionLabel(), isEditable);
    	if (indentSpaces > 0) {
    		int pad = indentSpaces * indent;
    		cell.setStyle("padding-left: " + pad + "px;");
    	}
    	cell.setNoWrap(true);
    	row.addContent(cell);
    	cell = initNormalCell(c.getSchedulingSubpart().getItype().getSis_ref(), isEditable);
    	row.addContent(cell);
    	cell = initNormalCell(bs.getSectionIndex(), isEditable);
    	row.addContent(cell);
    	cell = initNormalCell(Integer.toString(bs.calculateMaxEnrl(hibSession)), isEditable);
    	cell.setAlign("right");
    	row.addContent(cell);
		cell = initNormalCell((bs.getBannerConfig().getGradableItype() != null && bs.getBannerConfig().getGradableItype().getItype().equals(c.getSchedulingSubpart().getItype().getItype()))?"<IMG border='0' alt='Yes' title='Section is gradable.' align='absmiddle' src='images/accept.png'>":"", isEditable);
		cell.setAlign("center");
	   	row.addContent(cell);
		if (BannerSection.displayLabHours()){
	     	cell = initNormalCell((bs.getBannerConfig().getLabHours() != null && bs.getBannerConfig().getGradableItype() != null && bs.getBannerConfig().getGradableItype().getItype().equals(c.getSchedulingSubpart().getItype().getItype()))?bs.getBannerConfig().getLabHours().toString():"", isEditable);
	     	cell.setAlign("right");
	     	row.addContent(cell);
		}
	   	cell = initNormalCell((c.isEnabledForStudentScheduling() != null && c.isEnabledForStudentScheduling().booleanValue()?"<IMG border='0' alt='Yes' title='Print Indicator.' align='absmiddle' src='images/accept.png'>":""), isEditable);
		cell.setAlign("center");
		row.addContent(cell);
		cell = initNormalCell(bs.getCrossListIdentifier(), isEditable);
		row.addContent(cell);
		cell = initNormalCell(bs.getLinkIdentifier(), isEditable);
		row.addContent(cell);
		cell = initNormalCell(bs.getLinkConnector(), isEditable);
		row.addContent(cell);
     	cell = initNormalCell(bs.consentLabel(), isEditable);
     	row.addContent(cell);
     	cell = initNormalCell((c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() == null?"":c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getReference()), isEditable);
     	row.addContent(cell);
     	String credit = bs.bannerCourseCreditStr(c);
     	cell = initNormalCell(credit, isEditable);
     	cell.setAlign("right");
     	row.addContent(cell);
     	cell = initNormalCell("", isEditable);
     	cell.setNoWrap(true);
     	BannerSession bsess = BannerSession.findBannerSessionForSession(bs.getSession(), hibSession);
     	cell.addContent(bs.getCampusCode(bsess, c));
     	row.addContent(cell);

     	cell = initNormalCell("", isEditable);
     	cell.setNoWrap(true);
     	cell.addContent(bs.buildClassLabelHtml(classAssignment));
     	row.addContent(cell);

     	TableCell dpCell = initNormalCell("", isEditable);
     	dpCell.setNoWrap(true);
     	TableCell timeCell = initNormalCell("", isEditable);
     	timeCell.setNoWrap(true);
     	TableCell roomCell = initNormalCell("", isEditable);
     	roomCell.setNoWrap(true);
     	TableCell roomCapacityCell = initNormalCell("", isEditable);
     	roomCapacityCell.setNoWrap(true);
     	roomCapacityCell.setAlign("right");
     	timeCell.addContent(bs.buildAssignedTimeHtml(classAssignment));
     	roomCell.addContent(bs.buildAssignedRoomHtml(classAssignment));
     	roomCapacityCell.addContent(bs.buildAssignedRoomCapacityHtml(classAssignment));
     	dpCell.addContent(bs.buildDatePatternHtml(classAssignment));
     	row.addContent(dpCell);
     	row.addContent(timeCell);
     	row.addContent(roomCell);
     	row.addContent(roomCapacityCell);
     	
    	cell = initNormalCell("", isEditable);   
    	cell.setNoWrap(true);
     	cell.addContent(bs.buildInstructorHtml());
     	row.addContent(cell);
     	
    	table.addContent(row);
	}

	private TableCell initCell(String onClick, int cols, boolean nowrap){
        TableCell cell = new TableCell();
        cell.setValign("top");
        if (cols > 1){
            cell.setColSpan(cols);
        }
        if (nowrap){
            cell.setNoWrap(true);
        }
        if (onClick != null && onClick.length() > 0){
        	cell.setOnClick(onClick);
        }
        return (cell);
    }

    private void endCell(TableCell cell){
    	//do nothing
    }

	
    private TableCell subjectAndCourseInfo(BannerCourse bc, InstructionalOffering io, CourseOffering co) {
        TableCell cell = this.initCell(null, 18, true);
        cell.addContent("<A name=\"A" + io.getUniqueId().toString() + "\"></A>");
        cell.addContent("<A name=\"A" + co.getUniqueId().toString() + "\"></A>");
        cell.addContent("<A name=\"A" + bc.getUniqueId().toString() + "\"></A>");
        cell.addContent(co != null? ("<span title='" + co.getCourseNameWithTitle() + "'><b>" + co.getCourseNameWithTitle() + "</b></span>") :"");
        TreeSet ts = new TreeSet(new CourseOfferingComparator());
        ts.addAll(io.getCourseOfferings());
        ts.remove(co);
        Iterator it = ts.iterator();
        StringBuffer addlCos = new StringBuffer();
        CourseOffering tempCo = null;
        addlCos.append("<font color='"+disabledColor+"'>");
        while(it.hasNext()){
            tempCo = (org.unitime.timetable.model.CourseOffering) it.next();
            addlCos.append("<br>"); 
            addlCos.append("<span title='" + tempCo.getCourseNameWithTitle() + "' style='padding-left: " + indent + "px;'>");
            addlCos.append(tempCo.getCourseNameWithTitle());
            addlCos.append("</span>");
        }
        addlCos.append("</font>");
        if (tempCo != null){
            cell.addContent(addlCos.toString());
        }
        this.endCell(cell);

        return (cell);
    }  

    protected void buildTableHeader(TableStream table, Long sessionId, String durationColName){  
    	TableRow row = new TableRow();
    	TableRow row2 = new TableRow();
    	TableHeaderCell cell = null;
    	cell = this.headerCell(LABEL, 2, 1);
    	cell.addContent("<hr>");
    	row.addContent(cell);
    	cell = this.headerCell("Instr Type", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
    	cell = this.headerCell("Sec&nbsp;Id", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
    	cell = this.headerCell("Limit", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = this.headerCell("Grade", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		if (BannerSection.displayLabHours()){
			cell = this.headerCell("Lab Hours", 2, 1);
			cell.addContent("<hr>");
			row.addContent(cell);
		}
		cell = this.headerCell("Print", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = this.headerCell("Xlst", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = this.headerCell("Link&nbsp;Id", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = this.headerCell("Link Conn", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = this.headerCell("Consent", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = this.headerCell("Instr. Method", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = this.headerCell("Credit", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = this.headerCell("Campus", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = this.headerCell("Class Label", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = this.headerCell("DatePattern", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);
		cell = headerCell("--------" + MSG.columnTimetable() + "--------", 1, TIMETABLE_COLUMN_ORDER.length);
		cell.setAlign("center");
    	row.addContent(cell);
    	for(int j = 0; j < TIMETABLE_COLUMN_ORDER.length; j++){
    		cell = headerCell(TIMETABLE_COLUMN_ORDER[j], 1, 1);
    		cell.addContent("<hr>");
    		cell.setNoWrap(true);
    		row2.addContent(cell);     
    	} 
		cell = this.headerCell("Instructors", 2, 1);
		cell.addContent("<hr>");
		row.addContent(cell);

    	table.addContent(row);
    	table.addContent(row2);
   }

}
