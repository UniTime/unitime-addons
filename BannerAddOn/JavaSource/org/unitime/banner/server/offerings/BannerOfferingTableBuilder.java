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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerLastSentSectionRestriction;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.FilterInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LinkInteface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface.Alignment;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.LearningManagementSystemInfo;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassCourseComparator;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.comparators.InstrOfferingConfigComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.InstructionalOfferingTableBuilder;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.webutil.Navigation;

public class BannerOfferingTableBuilder extends InstructionalOfferingTableBuilder {
	protected static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	public BannerOfferingTableBuilder(SessionContext context, String backType, String backId) {
		super(context, backType, backId);
	}
	
	@Override
	public boolean isShowDemand() {
		return false;
	}
	
    public void generateTableForBannerOfferings(
            ClassAssignmentProxy classAssignment, 
            FilterInterface filter, 
            String[] subjectAreaIds, 
            List<TableInterface> tables){
    	
    	this.setVisibleColumns(filter);
    	
		String courseNbr = filter.getParameterValue("courseNbr");
		boolean allCoursesAreGiven = (courseNbr==null || courseNbr.isEmpty());

    	List<Long> navigationOfferingIds = new ArrayList<Long>();
    	
    	for (String subjectAreaId: subjectAreaIds) {
    		generateTableForBannerOfferings(classAssignment,
        			InstructionalOffering.search(
        					getCurrentAcademicSessionId(),
        					Long.valueOf(subjectAreaId),
        					filter.getParameterValue("courseNbr"),
        					true, false, false, false, false, false), 
         			Long.valueOf(subjectAreaId),
         			allCoursesAreGiven,
        			tables,
        			new ClassCourseComparator(filter.getParameterValue("sortBy", "NAME"), classAssignment, false),
        			navigationOfferingIds
        	);
    	}
    	
        Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, navigationOfferingIds);
    }
    
    protected void generateTableForBannerOfferings(
            ClassAssignmentProxy classAssignment, 
            TreeSet<InstructionalOffering> insructionalOfferings, 
            Long subjectAreaId, 
            boolean allCoursesAreGiven,
            List<TableInterface> tables,
            ClassCourseComparator classComparator,
            List<Long> navigationOfferingIds) {
    	
    	if (insructionalOfferings == null) return;
    	
    	if (classComparator!=null)
    		setClassComparator(classComparator);
    	
    	SubjectArea subjectArea = SubjectAreaDAO.getInstance().get(Long.valueOf(subjectAreaId));
        
        TreeMap<CourseOffering, InstructionalOffering> notOfferedOfferings = new TreeMap<CourseOffering, InstructionalOffering>(new CourseOfferingComparator());
        TreeMap<CourseOffering, InstructionalOffering> offeredOfferings = new TreeMap<CourseOffering, InstructionalOffering>(new CourseOfferingComparator());
        
        boolean hasOfferedCourses = false;
        boolean hasNotOfferedCourses = false;
        
         for (InstructionalOffering io: insructionalOfferings) {
            if (io.isNotOffered() == null || io.isNotOffered().booleanValue()){
            	hasNotOfferedCourses = true;
            	for (Iterator<CourseOffering> coIt = io.getCourseOfferings().iterator(); coIt.hasNext();){
            		CourseOffering co = coIt.next();
            		if (co.getSubjectArea().getUniqueId().equals(subjectAreaId))
            			notOfferedOfferings.put(co,io);
            	}
            } else {
            	hasOfferedCourses = true;
            	for (Iterator<CourseOffering> coIt = io.getCourseOfferings().iterator(); coIt.hasNext();){
            		CourseOffering co = coIt.next();
            		if (co.getSubjectArea().getUniqueId().equals(subjectAreaId))
            			offeredOfferings.put(co,io);
            	}
            }
        }
         
        if (hasOfferedCourses || allCoursesAreGiven) {
        	Iterator<CourseOffering> it = offeredOfferings.keySet().iterator();
            TableInterface offeredTable = this.initTable(getCurrentAcademicSessionId());
            
            while (it.hasNext()){
                CourseOffering co = it.next();
                if (navigationOfferingIds != null && !navigationOfferingIds.contains(co.getInstructionalOffering().getUniqueId()))
                	navigationOfferingIds.add(co.getInstructionalOffering().getUniqueId());
                this.addBannerCourseRowsToTable(classAssignment, offeredTable, co, subjectAreaId);            	
            }
            
            offeredTable.setAnchor("AO" + subjectAreaId);
            if (isFilterWaitlist())
            	offeredTable.setName(MSG.labelOfferedWaitListedCourses(subjectArea.getSubjectAreaAbbreviation()));
	    	else if (isFilterNonWaitlist())
	    		offeredTable.setName( MSG.labelOfferedNotWaitListedCourses(subjectArea.getSubjectAreaAbbreviation()));
	    	else if (isFilterCoursesAllowingReScheduling())
	    		offeredTable.setName(MSG.labelOfferedCoursesAllowingReScheduling(subjectArea.getSubjectAreaAbbreviation()));
	    	else if (isFilterCoursesNotAllowingReScheduling())
	    		offeredTable.setName(MSG.labelOfferedCoursesNotAllowingReScheduling(subjectArea.getSubjectAreaAbbreviation()));
	    	else if (isFilterNonWaitedCoursesAllowingReScheduling())
	    		offeredTable.setName(MSG.labelOfferedNotWaitListedCoursesAllowingReScheduling(subjectArea.getSubjectAreaAbbreviation()));
	    	else
	    		offeredTable.setName(MSG.labelOfferedCourses(subjectArea.getSubjectAreaAbbreviation()));
            
            if(!hasOfferedCourses) {
            	offeredTable.setErrorMessage(MSG.errorNoCoursesOffered(subjectArea.getSubjectAreaAbbreviation()));
            	if (!isSimple()) offeredTable.getHeader().clear();
            }
            
            if (allCoursesAreGiven)
            	offeredTable.addLink(new LinkInteface()
            			.setHref("#AN" + subjectAreaId)
            			.setText(MSG.labelNotOfferedCourses(subjectArea.getSubjectAreaAbbreviation()))
            			);
            tables.add(offeredTable);
        }
        
        if (hasNotOfferedCourses || allCoursesAreGiven) {
        	Iterator<CourseOffering> it = notOfferedOfferings.keySet().iterator();
            TableInterface notOfferedTable = this.initTable(getCurrentAcademicSessionId());
            while (it.hasNext()){
            	CourseOffering co = it.next();
                if (navigationOfferingIds != null && !navigationOfferingIds.contains(co.getInstructionalOffering().getUniqueId()))
                	navigationOfferingIds.add(co.getInstructionalOffering().getUniqueId());
                this.addBannerCourseRowsToTable(classAssignment, notOfferedTable, co, subjectAreaId);            	
            }
            notOfferedTable.setAnchor("AN" + subjectAreaId);
            notOfferedTable.setName(MSG.labelNotOfferedCourses(subjectArea.getSubjectAreaAbbreviation()));
            
            if (!hasNotOfferedCourses) {
            	notOfferedTable.setErrorMessage(MSG.errorAllCoursesOffered(subjectArea.getSubjectAreaAbbreviation()));
            	if (!isSimple()) notOfferedTable.getHeader().clear();
            }
            
            if (allCoursesAreGiven)
            	notOfferedTable.addLink(new LinkInteface()
            			.setHref("#AO" + subjectAreaId)
            			.setText(MSG.labelOfferedCourses(subjectArea.getSubjectAreaAbbreviation()))
            			);
            
            tables.add(notOfferedTable);            
        }
        
        if (navigationOfferingIds != null)
        	Navigation.set(getSessionContext(), Navigation.sInstructionalOfferingLevel, navigationOfferingIds);
    }
    
    @Override
    protected void buildTableHeader(TableInterface table, Long sessionId, String durationColName){
    	LineInterface row = new LineInterface();
    	LineInterface row2 = new LineInterface();
    	CellInterface cell = null;
    	cell = this.headerCell(null, 2, 1);
    	row.addCell(cell);
    	cell = this.headerCell(BMSG.colInstrType(), 2, 1);
		row.addCell(cell);
    	cell = this.headerCell(BMSG.colSecId(), 2, 1);
		row.addCell(cell);
		if (isShowDemand() && sessionHasEnrollments(sessionId)) {
    		cell = this.headerCell(MSG.columnDemand(), 2, 1);
    		row.addCell(cell);
		}
		cell = this.headerCell(MSG.columnLimit(), 2, 1);
		row.addCell(cell);
		cell = this.headerCell(BMSG.colGradable(), 2, 1);
		row.addCell(cell);
		if (BannerSection.displayLabHours()){
			cell = this.headerCell(BMSG.colLabHours(), 2, 1);
			row.addCell(cell);
		}
		cell = this.headerCell(BMSG.colPrint(), 2, 1);
		row.addCell(cell);
		cell = this.headerCell(BMSG.colXlst(), 2, 1);
		row.addCell(cell);
		cell = this.headerCell(BMSG.colLinkId(), 2, 1);
		row.addCell(cell);
		cell = this.headerCell(BMSG.colLinkConn(), 2, 1);
		row.addCell(cell);
		cell = this.headerCell(MSG.columnConsent(), 2, 1);
		row.addCell(cell);
		cell = this.headerCell(BMSG.colInstrMethod(), 2, 1);
		row.addCell(cell);
		cell = this.headerCell(MSG.columnCredit(), 2, 1);
		row.addCell(cell);
		cell = this.headerCell(MSG.columnCampus(), 2, 1);
		row.addCell(cell);
		cell = this.headerCell(BMSG.colClassLabel(), 2, 1);
		row.addCell(cell);
		cell = this.headerCell(MSG.columnDatePattern(), 2, 1);
		row.addCell(cell);
		
		cell = headerCell("--------" + MSG.columnTimetable() + "--------", 1, 3);
    	cell.setClassName("WebTableHeaderFirstRow");
    	cell.setTextAlignment(Alignment.CENTER);
		row.addCell(cell);
		cell = headerCell(MSG.columnAssignedTime(), 1, 1);
		cell.setNoWrap(true);
		cell.setClassName("WebTableHeaderSecondRow");
		row2.addCell(cell);
		cell = headerCell(MSG.columnAssignedRoom(), 1, 1);
		cell.setNoWrap(true);
		cell.setClassName("WebTableHeaderSecondRow");
		row2.addCell(cell);
		cell = headerCell(MSG.columnAssignedRoomCapacity(), 1, 1);
		cell.setNoWrap(true);
		cell.setClassName("WebTableHeaderSecondRow");
		row2.addCell(cell);
		
		cell = this.headerCell(MSG.columnInstructors(), 2, 1);
		row.addCell(cell);
		if (LearningManagementSystemInfo.isLmsInfoDefinedForSession(sessionId)) {
			cell = this.headerCell(BMSG.colLMSCode(), 2, 1);
			row.addCell(cell);
		}
     	if (BannerLastSentSectionRestriction.areRestrictionsDefinedForTerm(sessionId)) {
     		cell = this.headerCell(BMSG.colRestrictions(), 2, 1);
			row.addCell(cell);
     	}
		if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
			cell = this.headerCell(MSG.columnFundingDepartment(), 2, 1);
			row.addCell(cell);
		}
    	table.addHeader(row);
    	table.addHeader(row2);
    }
    
    private void addBannerCourseRowsToTable(ClassAssignmentProxy classAssignment, TableInterface table, CourseOffering co, Long subjectAreaId){
        InstructionalOffering io = co.getInstructionalOffering();
        boolean isEditable = getSessionContext().hasPermission(io, Right.InstructionalOfferingDetail);
        if (!isEditable && io.getInstrOfferingConfigs() != null && io.getInstrOfferingConfigs().size() > 0) {
        	boolean canEdit = true;
    		for (InstrOfferingConfig ioc: io.getInstrOfferingConfigs()) {
    			if (!getSessionContext().hasPermission(ioc, Right.InstrOfferingConfigEdit)) {
    				canEdit = false; break;
    			}
    		}
    		if (canEdit) isEditable = true;
        }
        BannerCourse bc = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), BannerCourseDAO.getInstance().getSession());
        if (bc == null) return;
        LineInterface row = initRow(true);
        if (isEditable) row.setURL("bannerOffering?bc=" + bc.getUniqueId());
        
        row.addCell(subjectAndCourseInfo(bc, io, co));
        table.addLine(row);
        if (io.getInstrOfferingConfigs() != null & !io.getInstrOfferingConfigs().isEmpty()){
        	TreeSet<InstrOfferingConfig> configs = new TreeSet<InstrOfferingConfig>(new InstrOfferingConfigComparator(io.getControllingCourseOffering().getSubjectArea().getUniqueId()));
        	configs.addAll(io.getInstrOfferingConfigs());
        	buildSectionConfigRows(classAssignment, table, bc, configs, true);
        }
    }
    
    private void buildSectionConfigRows(ClassAssignmentProxy classAssignment, TableInterface table, BannerCourse bc, Set<InstrOfferingConfig> instrOfferingConfigs, boolean printConfigLine) {
        for (InstrOfferingConfig ioc: instrOfferingConfigs) {
        	buildSectionConfigRow(classAssignment, table, bc, ioc, printConfigLine && instrOfferingConfigs.size()>1, true);
        }
    }
    
    protected void buildSectionConfigRow(ClassAssignmentProxy classAssignment, TableInterface table, BannerCourse bc, InstrOfferingConfig ioc, boolean printConfigLine, boolean clickable) {
	    boolean isHeaderRow = true;
	    boolean isEditable = getSessionContext().hasPermission(ioc, Right.InstrOfferingConfigEdit);
	    String configName = ioc.getName();
		if (printConfigLine) {
			LineInterface row = initRow(isHeaderRow);
	        if (clickable) row.setURL("bannerOffering?bc=" + bc.getUniqueId());
	        CellInterface cell = null;
    	    if (configName==null || configName.trim().length()==0)
    	        configName = ioc.getUniqueId().toString();
    	    cell = initNormalCell(MSG.labelConfiguration(configName), isEditable);
    	    cell.setStyle("padding-left: 20 px;");
        	cell.setNoWrap(true);
    	    row.addCell(cell);
    	    cell = initNormalCell(null, false);
    	    cell.setColSpan(18 + (isShowDemand() && sessionHasEnrollments(ioc.getSessionId()) ? 1 : 0));
    	    row.addCell(cell);
         	if (LearningManagementSystemInfo.isLmsInfoDefinedForSession(ioc.getSessionId())) {
         		cell = initNormalCell(null, false);
         		row.addCell(cell);
         	}
         	if (BannerLastSentSectionRestriction.areRestrictionsDefinedForTerm(ioc.getSessionId())) {
         		cell = initNormalCell(null, false);
         		row.addCell(cell);	
         	}
    		if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
    			cell = initNormalCell(null, false);
         		row.addCell(cell);
    		}
    		table.addLine(row);
		}

        ArrayList<SchedulingSubpart> subpartList = new ArrayList<SchedulingSubpart>(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());
        int ct = 0;
        for (SchedulingSubpart ss: subpartList) {  	
			if (ss.getParentSubpart() == null && ss.getClasses() != null) {
				ArrayList<Class_> classes = new ArrayList<Class_>(ss.getClasses());
				Collections.sort(classes, getClassComparator());
				for (Class_ c: classes)
					buildSectionRows(classAssignment, ++ct, table, c, bc, 1, null, clickable);
			}
		}
    }
    
    private void buildSectionRows(ClassAssignmentProxy classAssignment,
			int ct, TableInterface table,
			Class_ aClass, BannerCourse bc, int indentSpaces,
			Integer prevItype, boolean clickable) {
    	Integer currentItype = aClass.getSchedulingSubpart().getItype().getItype();
		if (aClass.isCancelled()) return;
		if (prevItype == null || !prevItype.equals(currentItype))
			buildSectionRow(classAssignment, ct, table, aClass, bc, indentSpaces, clickable);
		
    	Set<Class_> childClasses = aClass.getChildClasses();

    	if (childClasses != null && !childClasses.isEmpty()){
    	    ArrayList<Class_> childClassesList = new ArrayList<Class_>(childClasses);
            Collections.sort(childClassesList, getClassComparator());
            for (Class_ child: childClassesList)
                buildSectionRows(classAssignment, ct, table, child, bc, indentSpaces + (prevItype != null && prevItype.equals(currentItype) ? 0 : 1), currentItype, clickable);
        }
    }
    
    private void buildSectionRow(ClassAssignmentProxy classAssignment,
			int ct, TableInterface table,
			Class_ c, BannerCourse bc, int indentSpaces, boolean clickable) {
		org.hibernate.Session hibSession = Class_DAO.getInstance().getSession();
    	boolean isHeaderRow = false;
        boolean isEditable = true;
        LineInterface row = initRow(isHeaderRow);
        if (clickable)
        	row.setURL("bannerOffering?bc=" + bc.getUniqueId());
		BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOffering(c, bc.getCourseOffering(hibSession), hibSession);
		if (bs == null) return;
    	CellInterface cell = initNormalCell(bs.bannerSectionLabel(), isEditable);
    	if (indentSpaces > 0) {
    		int pad = indentSpaces * 20;
    		cell.setStyle("padding-left: " + pad + "px;");
    	}
    	cell.setNoWrap(true);
    	row.addCell(cell);
    	cell = initNormalCell(c.getSchedulingSubpart().getItype().getSis_ref(), isEditable);
    	row.addCell(cell);
    	cell = initNormalCell(bs.getSectionIndex(), isEditable);
    	row.addCell(cell);
    	if (isShowDemand() && sessionHasEnrollments(c.getSessionId())) {
    		int enrl = 0;
    		if (bs.isCrossListedSection(hibSession)) {
        		for (StudentClassEnrollment e: c.getStudentEnrollments())
        			if (e.getCourseOffering().getUniqueId().equals(bc.getCourseOfferingId())) enrl ++;
    		} else {
    			if (c.getEnrollment() != null)
    				enrl = c.getEnrollment();
    		}
    		cell = initNormalCell(Integer.toString(enrl), isEditable);
    		cell.setTextAlignment(Alignment.RIGHT);
        	row.addCell(cell);
    	}
    	cell = initNormalCell(Integer.toString(bs.calculateMaxEnrl(hibSession)), isEditable);
    	cell.setTextAlignment(Alignment.RIGHT);
    	row.addCell(cell);
		cell = initNormalCell("", isEditable);
    	if (bs.getBannerConfig().getGradableItype() != null && bs.getBannerConfig().getGradableItype().getItype().equals(c.getSchedulingSubpart().getItype().getItype()))
    		cell.addImage().setSource("images/accept.png").setTitle(BMSG.titleGradableSection()).setAlt(MSG.yes());
		cell.setTextAlignment(Alignment.CENTER);
	   	row.addCell(cell);
		if (BannerSection.displayLabHours()){
	     	cell = initNormalCell((bs.getBannerConfig().getLabHours() != null && bs.getBannerConfig().getGradableItype() != null && bs.getBannerConfig().getGradableItype().getItype().equals(c.getSchedulingSubpart().getItype().getItype()))?bs.getBannerConfig().getLabHours().toString():"", isEditable);
	     	cell.setTextAlignment(Alignment.RIGHT);
	     	row.addCell(cell);
		}
	   	cell = initNormalCell("", isEditable);
	   	if (c.isEnabledForStudentScheduling() != null && c.isEnabledForStudentScheduling())
	   		cell.addImage().setSource("images/accept.png").setTitle(BMSG.titlePrintIndicator()).setAlt(MSG.yes());
	   	cell.setTextAlignment(Alignment.CENTER);
		row.addCell(cell);
		cell = initNormalCell(bs.getCrossListIdentifier(), isEditable);
		row.addCell(cell);
		cell = initNormalCell(bs.getLinkIdentifier(), isEditable);
		row.addCell(cell);
		cell = initNormalCell(bs.getLinkConnector(), isEditable);
		row.addCell(cell);
     	cell = initNormalCell(bs.consentLabel(), isEditable);
     	row.addCell(cell);
     	cell = initNormalCell((c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod() == null?"":c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalMethod().getReference()), isEditable);
     	row.addCell(cell);
     	String credit = bs.bannerCourseCreditStr(c);
     	cell = initNormalCell(credit, isEditable);
     	cell.setTextAlignment(Alignment.RIGHT);
     	row.addCell(cell);
     	cell = initNormalCell("", isEditable);
     	cell.setNoWrap(true);
     	BannerSession bsess = BannerSession.findBannerSessionForSession(bs.getSession(), hibSession);
     	cell.add(bs.getCampusCode(bsess, c));
     	row.addCell(cell);

     	row.addCell(bs.buildClassLabeCell(classAssignment).setColor(isEditable ? null : disabledColor));
     	row.addCell(bs.buildDatePatternCell(classAssignment).setColor(isEditable ? null : disabledColor));
     	row.addCell(bs.buildAssignedTimeCell(classAssignment).setColor(isEditable ? null : disabledColor));
     	row.addCell(bs.buildAssignedRoomCell(classAssignment).setColor(isEditable ? null : disabledColor));
     	row.addCell(bs.buildAssignedRoomCapacityCell(classAssignment).setColor(isEditable ? null : disabledColor));
     	row.addCell(bs.buildInstructorCell(getInstructorNameFormat()).setColor(isEditable ? null : disabledColor));

     	if (LearningManagementSystemInfo.isLmsInfoDefinedForSession(c.getSessionId())) {
         	row.addCell(buildLmsInfo(c, isEditable));
     	}
     	if (BannerLastSentSectionRestriction.areRestrictionsDefinedForTerm(bs.getSession().getUniqueId())) {
         	row.addCell(bs.buildRestrictionCell().setColor(isEditable ? null : disabledColor));
     	}
		if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
			row.addCell(buildFundingDepartment(c, isEditable));
		}

    	table.addLine(row);
    }
    
    private CellInterface subjectAndCourseInfo(BannerCourse bc, InstructionalOffering io, CourseOffering co) {
		int span = 19;
		if (BannerSection.displayLabHours()) {
			span++;
		}
		if (isShowDemand() && sessionHasEnrollments(io.getSessionId())) {
			span++;
		}
		if (LearningManagementSystemInfo.isLmsInfoDefinedForSession(io.getSessionId())) {
			span++;	
 		}
		if (BannerLastSentSectionRestriction.areRestrictionsDefinedForTerm(io.getSession().getUniqueId())) {
			span++;
		}
		if (ApplicationProperty.CoursesFundingDepartmentsEnabled.isTrue()) {
			span++;
		}
		CellInterface cell = initNormalCell(null, true);
		cell.setColSpan(span);
		cell.addAnchor("A" + io.getUniqueId());
		cell.addAnchor("A" + co.getUniqueId());
		cell.addAnchor("A" + bc.getUniqueId());
		if ("InstructionalOffering".equals(getBackType()) && io.getUniqueId().toString().equals(getBackId()))
			cell.addAnchor("back");
		cell.setText(co.getCourseNameWithTitle()).setTitle(co.getCourseNameWithTitle());
		TreeSet<CourseOffering> ts = new TreeSet<CourseOffering>(new CourseOfferingComparator());
		ts.addAll(io.getCourseOfferings());
		ts.remove(co);
		for (CourseOffering tempCo: ts) {
			cell.add(tempCo.getCourseNameWithTitle())
				.setTitle(tempCo.getCourseNameWithTitle())
				.addStyle("padding-left: 20 px;")
				.setColor(disabledColor)
				.setNoWrap(true)
				.setInline(false);
		}
		return cell;
    }
}
