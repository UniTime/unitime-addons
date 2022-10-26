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

package org.unitime.colleague.webutil;

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

import org.unitime.colleague.form.ColleagueCourseListForm;
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.commons.web.htmlgen.TableCell;
import org.unitime.commons.web.htmlgen.TableHeaderCell;
import org.unitime.commons.web.htmlgen.TableRow;
import org.unitime.commons.web.htmlgen.TableStream;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
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
public class WebColleagueCourseListTableBuilder extends WebInstructionalOfferingTableBuilder {
	protected final static ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);
 
	private Comparator iClassComparator = new ClassComparator(ClassComparator.COMPARE_BY_ITYPE);

	/**
	 * 
	 */
	public WebColleagueCourseListTableBuilder() {
		// TODO Auto-generated constructor stub
	}
	
	
	public void htmlTableForColleagueOfferings(
			SessionContext context,
            ClassAssignmentProxy classAssignment, 
            ColleagueCourseListForm form, 
            Long subjectAreaId,
            boolean displayHeader,
            boolean allCoursesAreGiven,
            JspWriter outputStream,
            String backType,
            String backId){
    	
    	setBackType(backType); setBackId(backId);
    	
    	htmlTableForColleagueOfferings(context, classAssignment,
    			(TreeSet<InstructionalOffering>) form.getInstructionalOfferings(), 
     			subjectAreaId,
    			displayHeader, allCoursesAreGiven,
    			outputStream,
    			iClassComparator
    	);
    }
	
    public void htmlTableForColleagueOfferings(
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
    		    		outputStream.print("<DIV align=\"right\"><A class=\"l7\" href=\"#notOffered\">" + CMSG.labelCoursesNotOffered() + "</A></DIV>");
    			    outputStream.print("<DIV class=\"WelcomeRowHead\"><A name=\"offered\"></A>" + CMSG.labelOfferedCourses() + "</DIV>");
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
                    this.addColleagueCourseRowsToTable(classAssignment, offeredTable, co, io, subjectAreaId, context);            	
                }
                offeredTable.tableComplete();
            } else {
                if(displayHeader)
    				try {
    					outputStream.print("<font class=\"error\">" + CMSG.infoNoCoursesOffered() + "</font>");
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
    					outputStream.print("<DIV align=\"right\"><A class=\"l7\" href=\"#offered\">" + CMSG.labelOfferedCourses() + "</A></DIV>");
    		        outputStream.print("<DIV class=\"WelcomeRowHead\"><A name=\"notOffered\"></A>" + CMSG.labelCoursesNotOffered() + "</DIV>");
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
                    this.addColleagueCourseRowsToTable(classAssignment, notOfferedTable, co, io, subjectAreaId, context);            	
                }
                notOfferedTable.tableComplete();
            } else {
                if(displayHeader)
    				try {
    					outputStream.print("<font class=\"normal\">&nbsp;<br>" + CMSG.infoAllCoursesOffered() + "</font>");
    				} catch (IOException e) {
    					e.printStackTrace();
    				}
            }
        }
        
        Navigation.set(context, Navigation.sInstructionalOfferingLevel, offeringIds);
    }

    private String subjectOnClickAction(Long courseId){
        return("document.location='colleagueOfferingDetail.do?op=view&co=" + courseId.toString() + "';");
    }

	private void addColleagueCourseRowsToTable(
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
        
        TableRow row = (this.initRow(true));
        row.setOnMouseOver(this.getRowMouseOver(true, isEditable));
        row.setOnMouseOut(this.getRowMouseOut(true));
        row.setOnClick(subjectOnClickAction(co.getUniqueId()));

    	row.addContent(subjectAndCourseInfo(io, co));
        table.addContent(row);
        if (io.getInstrOfferingConfigs() != null & !io.getInstrOfferingConfigs().isEmpty()){
        	TreeSet configs = new TreeSet(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
        	configs.addAll(io.getInstrOfferingConfigs());
        	buildSectionConfigRows(classAssignment, table, co, configs, sessionContext, true);
        }
    }
	
    private void buildSectionConfigRows(ClassAssignmentProxy classAssignment, TableStream table, CourseOffering courseOffering, Set instrOfferingConfigs, SessionContext sessionContext, boolean printConfigLine) {
        Iterator it = instrOfferingConfigs.iterator();
        InstrOfferingConfig ioc = null;
        while (it.hasNext()){
            ioc = (InstrOfferingConfig) it.next();
            buildSectionConfigRow(null, classAssignment, courseOffering, table, ioc, sessionContext, printConfigLine && instrOfferingConfigs.size()>1, true);
        }
    }

	protected void buildSectionConfigRow(Vector subpartIds, ClassAssignmentProxy classAssignment, CourseOffering courseOffering,TableStream table, InstrOfferingConfig ioc, SessionContext sessionContext, boolean printConfigLine, boolean clickable) {
	    boolean isHeaderRow = true;
	    boolean isEditable = sessionContext.hasPermission(ioc, Right.InstrOfferingConfigEdit);
	    String configName = ioc.getName();
		if (printConfigLine) {
		    TableRow row = this.initRow(isHeaderRow);
	        if (clickable){
	            row.setOnClick(subjectOnClickAction(courseOffering.getUniqueId()));
	        }
	        TableCell cell = null;
    	    if (configName==null || configName.trim().length()==0)
    	        configName = ioc.getUniqueId().toString();
    	    cell = this.initNormalCell(MSG.labelConfiguration(configName), isEditable);
    	    cell.setStyle("padding-left: " + indent + "px;");
        	cell.setNoWrap(true);
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
						buildSectionRows(classAssignment, ++ct, table, c, courseOffering, 1, sessionContext, null, clickable);					}
				}
			}
		}

   }

    
    


	private void buildSectionRows(ClassAssignmentProxy classAssignment,
			int ct, TableStream table,
			Class_ aClass, CourseOffering courseOffering, int indentSpaces, SessionContext sessionContext,
			Integer prevItype, boolean clickable) {
		Integer currentItype = aClass.getSchedulingSubpart().getItype().getItype();
		if (prevItype == null){
			buildSectionRow(classAssignment, ct, table, aClass, courseOffering, indentSpaces, sessionContext, clickable);
		}
    	Set childClasses = aClass.getChildClasses();

    	if (childClasses != null && !childClasses.isEmpty()){
        
    	    ArrayList childClassesList = new ArrayList(childClasses);
            Collections.sort(childClassesList, iClassComparator);
            
            Iterator it = childClassesList.iterator();
            Class_ child = null;
            while (it.hasNext()){              
                child = (Class_) it.next();
                buildSectionRows(classAssignment, ct, table, child, courseOffering, indentSpaces, sessionContext, currentItype, clickable);            }
        }
		
	}
	
	private void buildSectionRow(ClassAssignmentProxy classAssignment,
			int ct, TableStream table,
			Class_ c, CourseOffering courseOffering, int indentSpaces, SessionContext sessionContext, boolean clickable) {

		org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
    	boolean isHeaderRow = false;
        boolean isEditable = true;
    	TableRow row = this.initRow(isHeaderRow);
        if (clickable){
            row.setOnClick(subjectOnClickAction(courseOffering.getUniqueId()));
        }
		ColleagueSection cs = ColleagueSection.findColleagueSectionForClassAndCourseOffering(c, courseOffering, hibSession);
		if (cs == null){
			return;
		}
		TableCell cell = initNormalCell(cs.colleagueCourseLabel(), isEditable);
    	if (indentSpaces > 0) {
    		int pad = indentSpaces * indent;
    		cell.setStyle("padding-left: " + pad + "px;");
    	}
    	cell.setNoWrap(true);
    	row.addContent(cell);
    	cell = initNormalCell(c.getSchedulingSubpart().getItype().getSis_ref(), isEditable);
    	row.addContent(cell);
    	cell = initNormalCell(cs.getSectionIndex(), isEditable);
    	row.addContent(cell);
    	cell = initNormalCell((cs.getColleagueId() == null?"": cs.getColleagueId()), isEditable);
    	row.addContent(cell);
    	cell = initNormalCell((cs.findSchedType() == null?"": cs.findSchedType()), isEditable);
    	row.addContent(cell);
    	cell = initNormalCell(Integer.toString(cs.calculateMaxEnrl(hibSession)), isEditable);
    	cell.setAlign("right");
    	row.addContent(cell);
    	cell = initNormalCell(cs.findNumWeeks(), isEditable);
    	cell.setAlign("right");
    	row.addContent(cell);
		cell = initNormalCell((c.isEnabledForStudentScheduling() != null && c.isEnabledForStudentScheduling().booleanValue()?"<IMG border='0' alt='Yes' title='" + CMSG.titlePrintIndicator() + "' align='absmiddle' src='images/accept.png'>":""), isEditable);
		cell.setAlign("center");
		row.addContent(cell);
		String restrictions = "";
		for (ColleagueRestriction cr : cs.getRestrictions()){
			if (restrictions.length() > 0){
				restrictions += ", ";
			}
			restrictions += cr.getCode();
		}
		cell = initNormalCell(restrictions, isEditable);
		row.addContent(cell);
     	cell = initNormalCell((courseOffering.getConsentType() == null?"":courseOffering.getConsentType().getReference()), isEditable);
     	row.addContent(cell);
     	String credit = "";
     	if (courseOffering.getCredit() != null) {
     		credit = courseOffering.getCredit().creditText();
     	}
     	cell = initNormalCell(credit, isEditable);
     	cell.setAlign("right");
     	row.addContent(cell);
		cell = initNormalCell("", isEditable);
     	cell.setNoWrap(true);
     	ColleagueSession bsess = ColleagueSession.findColleagueSessionForSession(cs.getSession(), hibSession);
     	cell.addContent(cs.getCampusCode(bsess, c));
     	row.addContent(cell);

     	cell = initNormalCell("", isEditable);
     	cell.setNoWrap(true);
     	cell.addContent(cs.buildClassLabelHtml(classAssignment));
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
     	timeCell.addContent(cs.buildAssignedTimeHtml(classAssignment));
     	roomCell.addContent(cs.buildAssignedRoomHtml(classAssignment));
     	roomCapacityCell.addContent(cs.buildAssignedRoomCapacityHtml(classAssignment));
     	dpCell.addContent(cs.buildDatePatternHtml(classAssignment));
     	row.addContent(dpCell);
     	row.addContent(timeCell);
     	row.addContent(roomCell);
     	row.addContent(roomCapacityCell);
     	
    	cell = initNormalCell("", isEditable);   
    	cell.setNoWrap(true);
     	cell.addContent(cs.buildInstructorHtml());
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

	
    private TableCell subjectAndCourseInfo(InstructionalOffering io, CourseOffering co) {
        TableCell cell = this.initCell(null, 18, true);
        cell.addContent("<A name=\"A" + io.getUniqueId().toString() + "\"></A>");
        cell.addContent("<A name=\"A" + co.getUniqueId().toString() + "\"></A>");
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
            addlCos.append(indent);
            addlCos.append("<span title='" + tempCo.getCourseNameWithTitle() + "'>");
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
    	row.addContent(cell);
    	cell = this.headerCell(CMSG.colInstrType(), 2, 1);
		row.addContent(cell);
    	cell = this.headerCell(CMSG.colSecId(), 2, 1);
		row.addContent(cell);
    	cell = this.headerCell(CMSG.colColleagueSynonym(), 2, 1);
		row.addContent(cell);
    	cell = this.headerCell(CMSG.colSchedType(), 2, 1);
		row.addContent(cell);
    	cell = this.headerCell(MSG.columnLimit(), 2, 1);
		row.addContent(cell);
    	cell = this.headerCell(CMSG.colNumWeeks(), 2, 1);
		row.addContent(cell);
		cell = this.headerCell(CMSG.colPrint(), 2, 1);
		row.addContent(cell);
		cell = this.headerCell(CMSG.colRestrictions(), 2, 1);
		row.addContent(cell);
		cell = this.headerCell(MSG.columnConsent(), 2, 1);
		row.addContent(cell);
		cell = this.headerCell(MSG.columnCredit(), 2, 1);
		row.addContent(cell);
		cell = this.headerCell(MSG.columnCampus(), 2, 1);
		row.addContent(cell);
		cell = this.headerCell(CMSG.colClassLabel(), 2, 1);
		row.addContent(cell);
		cell = this.headerCell(MSG.columnDatePattern(), 2, 1);
		row.addContent(cell);

		cell = headerCell("--------" + MSG.columnTimetable() + "--------", 1, 3);
    	cell.setStyleClass("WebTableHeaderFirstRow");
		cell.setAlign("center");
		row.addContent(cell);
		cell = headerCell(MSG.columnAssignedTime(), 1, 1);
		cell.setNoWrap(true);
		cell.setStyleClass("WebTableHeaderSecondRow");
		row2.addContent(cell);
		cell = headerCell(MSG.columnAssignedRoom(), 1, 1);
		cell.setNoWrap(true);
		cell.setStyleClass("WebTableHeaderSecondRow");
		row2.addContent(cell);
		cell = headerCell(MSG.columnAssignedRoomCapacity(), 1, 1);
		cell.setNoWrap(true);
		cell.setStyleClass("WebTableHeaderSecondRow");
		row2.addContent(cell);

		cell = this.headerCell(MSG.columnInstructors(), 2, 1);
		row.addContent(cell);

    	table.addContent(row);
    	table.addContent(row2);
   }

}
