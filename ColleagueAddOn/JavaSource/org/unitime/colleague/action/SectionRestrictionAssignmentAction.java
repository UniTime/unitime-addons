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
package org.unitime.colleague.action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.colleague.form.SectionRestrictionAssignmentForm;
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.comparators.ColleagueRestrictionComparator;
import org.unitime.colleague.model.comparators.ColleagueSectionComparator;
import org.unitime.colleague.model.dao.ColleagueSectionDAO;
import org.unitime.colleague.util.ColleagueInstrOffrConfigChangeAction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;

/**
 * @author Stephanie Schluttenhofer
 */
@Action(value = "sectionRestrictionAssignment", results = {
		@Result(name = "sectionRestrictionAssignment", type = "tiles", location = "sectionRestrictionAssignment.tiles"),
		@Result(name = "showPrevNextEdit", type = "redirect", location = "/sectionRestrictionAssignment.action",
			params = { "uid", "${uid}", "co", "${co}", "op", "${op}"}),
		@Result(name = "colleagueOfferingDetail", type = "redirect", location = "/colleagueOfferingDetail.action",
			params = { "io", "${form.instrOfferingId}", "co", "${form.courseOfferingId}", "op", "view"})
	})
@TilesDefinition(name = "sectionRestrictionAssignment.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Section Restrictions"),
		@TilesPutAttribute(name = "body", value = "/colleague/sectionRestrictionAssignment.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "false"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
	})
public class SectionRestrictionAssignmentAction extends UniTimeAction<SectionRestrictionAssignmentForm> {
	private static final long serialVersionUID = 8234876065656939813L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);
	
	private Long instrOffrConfigId = null;
	private Long courseOfferingId = null;
	
	public Long getUid() { return instrOffrConfigId; }
	public void setUid(Long instrOffrConfigId) { this.instrOffrConfigId = instrOffrConfigId; }
	public Long getCo() { return courseOfferingId; }
	public void setCo(Long courseOfferingId) { this.courseOfferingId = courseOfferingId; }

	
	@Override
    public String execute() throws Exception {
		if (form == null) form = new SectionRestrictionAssignmentForm();

        // Get operation
		if (op == null) op = form.getOp();

        if(op==null || op.trim().length()==0)
            throw new Exception (MSG.exceptionOperationNotInterpreted() + op);

        // Set the operation
        form.setOp(op);

        // Set the proxy so we can get the class time and room
        form.setProxy(getClassAssignmentService().getAssignment());
        
        if (instrOffrConfigId == null) instrOffrConfigId = form.getInstrOffrConfigId();
        if (courseOfferingId == null) courseOfferingId = form.getCourseOfferingId();

        InstrOfferingConfigDAO iocDao = new InstrOfferingConfigDAO();
        InstrOfferingConfig ioc = iocDao.get(instrOffrConfigId);
        form.setInstrOffrConfigId(instrOffrConfigId);

		CourseOffering co = null;
	    if (courseOfferingId == null){
	    	co = ioc.getControllingCourseOffering();
	    } else {
	    	CourseOfferingDAO coDao = new CourseOfferingDAO();
	        co = coDao.get(courseOfferingId);
	        if (co == null || !co.getInstructionalOffering().getUniqueId().equals(ioc.getInstructionalOffering().getUniqueId())){
	        	co = ioc.getControllingCourseOffering();
	        }
	    }
        if (co == null){
        	form.setCourseOfferingId(courseOfferingId);
        } else {
        	form.setCourseOfferingId(co.getUniqueId());
        }

        
        sessionContext.checkPermission(ioc, Right.InstrOfferingConfigEdit);

        
        ArrayList<ColleagueRestriction> restrictions = new ArrayList<ColleagueRestriction>(ColleagueRestriction.getAllColleagueRestrictionsForSession(iocDao.getSession(), ioc.getInstructionalOffering().getSessionId()));
	    Collections.sort(restrictions, new ColleagueRestrictionComparator());
        request.setAttribute("restrictionList", restrictions);

        // First access to screen
        if(op.equalsIgnoreCase(CMSG.actionAssignRestrictions())) {
            doLoad(instrOffrConfigId, ioc, co);
        }
        
        if (op.equals(MSG.actionBackToIODetail())) {
        	return "colleagueOfferingDetail";
        }
        
		if(op.equals(CMSG.actionUpdateSectionRestrictionAssignment()) ||
        		op.equals(MSG.actionNextIO()) ||
        		op.equals(MSG.actionPreviousIO()) ||
        		op.equals(CMSG.actionUnassignAllRestrictionsFromConfig())) {

            if (op.equals(CMSG.actionUnassignAllRestrictionsFromConfig())) {
            	form.unassignAllRestrictions();
//            	ColleagueInstrOffrConfigChangeAction ciocca = new ColleagueInstrOffrConfigChangeAction();
//            	ciocca.performExternalInstrOffrConfigChangeAction(ioc.getInstructionalOffering(), iocDao.getSession());
            }

        	// Validate input prefs
            form.validate(this);

            // No errors - Update class
            if (!hasFieldErrors()) {

            	try {
            		form.updateSections();

                    InstrOfferingConfig cfg = new InstrOfferingConfigDAO().get(form.getInstrOffrConfigId());

                    org.hibernate.Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
                    ChangeLog.addChange(
                    		hibSession,
                            sessionContext,
                            cfg,
                            ChangeLog.Source.CLASS_SETUP,
                            ChangeLog.Operation.UPDATE,
                            co.getSubjectArea(),
                            null);
                    
                	if (getPermissionOfferingLockNeeded().check(sessionContext.getUser(), cfg.getInstructionalOffering())) {
                		StudentSectioningQueue.offeringChanged(hibSession, sessionContext.getUser(), cfg.getInstructionalOffering().getSessionId(), cfg.getInstructionalOffering().getUniqueId());
                	}
                	
                	hibSession.flush();
                	
                	ColleagueInstrOffrConfigChangeAction ciocca = new ColleagueInstrOffrConfigChangeAction();
                	ciocca.performExternalInstrOffrConfigChangeAction(ioc.getInstructionalOffering(), iocDao.getSession());
                	

    	            if (op.equals(MSG.actionNextIO())) {
    	            	setUid(form.getNextId());
    	            	setCo(co.getUniqueId());
    	            	setOp(CMSG.actionAssignRestrictions());
    	            	return "showPrevNextEdit";
    	            }

    	            if (op.equals(MSG.actionPreviousIO())) {
    	            	setUid(form.getPreviousId());
    	            	setCo(co.getUniqueId());
    	            	setOp(CMSG.actionAssignRestrictions());
    	            	return "showPrevNextEdit";
    	            }
                    return "colleagueOfferingDetail";
            	} catch (Exception e) {
            		throw e;
            	}
            }
        }

        if (op.equals("DR")) {
        	form.deleteRestriction();
        }

        if (op.equals("AR")) {
        	form.addRestriction();
        }

        return "sectionRestrictionAssignment";
    }

	/**
     * Loads the form with the classes that are part of the instructional offering config
     * @param form Form object
     * @param instrCoffrConfigId Instructional Offering Config Id
     * @param user User object
     */
    @SuppressWarnings("unchecked")
	private void doLoad(Long instrOffrConfigId, InstrOfferingConfig ioc, CourseOffering co) throws Exception {
        // Check uniqueid
        if(instrOffrConfigId==null)
            throw new Exception (MSG.exceptionMissingIOConfig());

        // Load details
        InstructionalOffering io = ioc.getInstructionalOffering();

        // Load form properties
        form.setInstrOffrConfigId(ioc.getUniqueId());
        form.setInstrOffrConfigLimit(ioc.getLimit());
        form.setInstrOfferingId(io.getUniqueId());
        form.setCourseOfferingId(co.getUniqueId());
        form.setSubjectAreaId(co.getSubjectArea().getUniqueId());

        form.setDisplayExternalId(true);

        String name = io.getCourseNameWithTitle();
        if (io.hasMultipleConfigurations()) {
        	name += " [" + ioc.getName() +"]";
        }
        form.setInstrOfferingName(name);

        if (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().size() == 0)
        	throw new Exception(MSG.exceptionIOConfigUndefined());

        InstrOfferingConfig config = ioc.getNextInstrOfferingConfig(sessionContext);
        if(config != null) {
        	if (!config.getInstructionalOffering().getUniqueId().equals(io.getUniqueId()) && config.getInstructionalOffering().getCourseOfferings().size() > 1) {
        		form.setNextId(null);
        	} else {
        		form.setNextId(config.getUniqueId());
        	}
        } else {
        	form.setNextId(null);
        }

        config = ioc.getPreviousInstrOfferingConfig(sessionContext);
        if(config != null) {
        	if (!config.getInstructionalOffering().getUniqueId().equals(io.getUniqueId()) && config.getInstructionalOffering().getCourseOfferings().size() > 1) {
        		form.setPreviousId(null);
        	} else {
        		form.setPreviousId(config.getUniqueId());
        	}
    	} else {
            form.setPreviousId(null);
        }

        ArrayList<SchedulingSubpart> subpartList = new ArrayList<SchedulingSubpart>(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());

        for(SchedulingSubpart ss : subpartList){
    		if (ss.getClasses() == null || ss.getClasses().size() == 0)
    			throw new Exception(MSG.exceptionInitialIOSetupIncomplete());
    		if (ss.getParentSubpart() == null){
        		loadSections(form, ColleagueSection.findNotDeletedColleagueSectionsForSchedulingSubpartAndCourse(ss, co, ColleagueSectionDAO.getInstance().getSession()), new String());
        	}
        }
    }

    private void loadSections(SectionRestrictionAssignmentForm form, Collection<ColleagueSection> sections, String indent){
    	if (sections != null && sections.size() > 0){
    		ArrayList<ColleagueSection> sectionList = new ArrayList<ColleagueSection>(sections);

    		if (CommonValues.Yes.eq(UserProperty.ClassesKeepSort.get(sessionContext.getUser()))) {
        		Collections.sort(sectionList,new ColleagueSectionComparator()
        		);
        	} else {
        		Collections.sort(sectionList, new ColleagueSectionComparator());
        	}

	    	for(ColleagueSection cs : sectionList){
	    		if (!cs.isDeleted() && !cs.isCanceled(ColleagueSectionDAO.getInstance().getSession())){
		    		form.addToSections(cs, !sessionContext.hasPermission(cs.getFirstClass(), Right.ClassEdit), indent);
		    		loadSections(form, cs.getColleagueSectionToChildSections(), indent + "&nbsp;&nbsp;&nbsp;&nbsp;");
	    		}
	    	}
    	}
    }
    
    public String getCrsNbr() {
    	return (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
    }
    
    protected Permission<InstructionalOffering> getPermissionOfferingLockNeeded() {
    	return getPermission("permissionOfferingLockNeeded");
    }
}
