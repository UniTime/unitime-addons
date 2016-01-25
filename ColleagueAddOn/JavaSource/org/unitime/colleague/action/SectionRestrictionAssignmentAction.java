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

import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.action.ActionRedirect;
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.colleague.form.SectionRestrictionAssignmentForm;
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.comparators.ColleagueRestrictionComparator;
import org.unitime.colleague.model.comparators.ColleagueSectionComparator;
import org.unitime.colleague.model.dao.ColleagueSectionDAO;
import org.unitime.colleague.util.ColleagueInstrOffrConfigChangeAction;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.timetable.defaults.CommonValues;
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
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.permissions.Permission;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.WebSolver;

/**
 * @author Stephanie Schluttenhofer
 */
@Service("/sectionRestrictionAssignment")
public class SectionRestrictionAssignmentAction extends Action {

	protected final static ColleagueMessages MSG = Localization.create(ColleagueMessages.class);
	
	@Autowired SessionContext sessionContext;
	
	@Autowired Permission<InstructionalOffering> permissionOfferingLockNeeded;
	/**
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        MessageResources rsc = getResources(request);
        SectionRestrictionAssignmentForm frm = (SectionRestrictionAssignmentForm) form;

        // Get operation
        String op = (request.getParameter("op")==null)
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");
						        
        if(op==null)
            op = request.getParameter("hdnOp");

        if(op==null || op.trim().length()==0)
            throw new Exception (MSG.exceptionOperationNotInterpreted() + op);

        // Instructional Offering Config Id
        String instrOffrConfigId = "";
        
        // Course Offering Id
        String courseOfferingId = "";

        // Set the operation
        frm.setOp(op);

        // Set the proxy so we can get the class time and room
        frm.setProxy(WebSolver.getClassAssignmentProxy(request.getSession()));

    	instrOffrConfigId = (request.getParameter("uid")==null)
				? (request.getAttribute("uid")==null)
				        ? frm.getInstrOffrConfigId()!=null
				        		? frm.getInstrOffrConfigId().toString()
				        		: null
				        : request.getAttribute("uid").toString()
				: request.getParameter("uid");

        InstrOfferingConfigDAO iocDao = new InstrOfferingConfigDAO();
        InstrOfferingConfig ioc = iocDao.get(Long.valueOf(instrOffrConfigId));
        frm.setInstrOffrConfigId(Long.valueOf(instrOffrConfigId));

    	courseOfferingId = (request.getParameter("co")==null)
				? (request.getAttribute("co")==null)
				        ? frm.getCourseOfferingId()!=null
				        		? frm.getCourseOfferingId().toString()
				        		: null
				        : request.getAttribute("co").toString()
				: request.getParameter("co");

		CourseOffering co = null;
	    if (courseOfferingId == null || courseOfferingId.trim().isEmpty()){
	    	co = ioc.getControllingCourseOffering();
	    } else {
	    	CourseOfferingDAO coDao = new CourseOfferingDAO();
	        co = coDao.get(Long.valueOf(courseOfferingId));
	        if (!co.getInstructionalOffering().getUniqueId().equals(ioc.getInstructionalOffering().getUniqueId())){
	        	co = ioc.getControllingCourseOffering();
	        }
	    }
        if (co == null){
        	frm.setCourseOfferingId(Long.valueOf(courseOfferingId));
        } else {
        	frm.setCourseOfferingId(Long.valueOf(co.getUniqueId().longValue()));
        }

        
        sessionContext.checkPermission(ioc, Right.InstrOfferingConfigEdit);

        
        ArrayList<ColleagueRestriction> restrictions = new ArrayList<ColleagueRestriction>(ColleagueRestriction.getAllColleagueRestrictionsForSession(iocDao.getSession(), ioc.getInstructionalOffering().getSessionId()));
	    Collections.sort(restrictions, new ColleagueRestrictionComparator());
        request.setAttribute("restrictionList", restrictions);

        // First access to screen
        if(op.equalsIgnoreCase(MSG.actionAssignRestrictions())) {
            doLoad(request, frm, instrOffrConfigId, ioc, co);
        }
        
		if(op.equals(MSG.actionUpdateSectionRestrictionAssignment()) ||
        		op.equals(MSG.actionNextIO()) ||
        		op.equals(MSG.actionPreviousIO()) ||
        		op.equals(MSG.actionUnassignAllRestrictionsFromConfig())) {

            if (op.equals(MSG.actionUnassignAllRestrictionsFromConfig())) {
            	frm.unassignAllRestrictions();
//            	ColleagueInstrOffrConfigChangeAction ciocca = new ColleagueInstrOffrConfigChangeAction();
//            	ciocca.performExternalInstrOffrConfigChangeAction(ioc.getInstructionalOffering(), iocDao.getSession());
            }

        	// Validate input prefs
            ActionMessages errors = frm.validate(mapping, request);

            // No errors - Update class
            if(errors.size()==0) {

            	try {
            		frm.updateSections();

                    InstrOfferingConfig cfg = new InstrOfferingConfigDAO().get(frm.getInstrOffrConfigId());

                    org.hibernate.Session hibSession = InstructionalOfferingDAO.getInstance().getSession();
                    ChangeLog.addChange(
                    		hibSession,
                            sessionContext,
                            cfg,
                            ChangeLog.Source.CLASS_SETUP,
                            ChangeLog.Operation.UPDATE,
                            co.getSubjectArea(),
                            null);
                    
                	if (permissionOfferingLockNeeded.check(sessionContext.getUser(), cfg.getInstructionalOffering())) {
                		StudentSectioningQueue.offeringChanged(hibSession, sessionContext.getUser(), cfg.getInstructionalOffering().getSessionId(), cfg.getInstructionalOffering().getUniqueId());
                	}
                	
                	hibSession.flush();
                	
                	ColleagueInstrOffrConfigChangeAction ciocca = new ColleagueInstrOffrConfigChangeAction();
                	ciocca.performExternalInstrOffrConfigChangeAction(ioc.getInstructionalOffering(), iocDao.getSession());
                	

    	            if (op.equals(MSG.actionNextIO())) {
    	            	response.sendRedirect(response.encodeURL("sectionRestrictionAssignment.do?uid="+frm.getNextId()+"&co="+ co.getUniqueId().toString()+"&op="+URLEncoder.encode(MSG.actionAssignRestrictions(), "UTF-8")));
    	            	return null;
    	            }

    	            if (op.equals(MSG.actionPreviousIO())) {
    	            	response.sendRedirect(response.encodeURL("sectionRestrictionAssignment.do?uid="+frm.getPreviousId()+"&co="+ co.getUniqueId().toString()+"&op="+URLEncoder.encode(MSG.actionAssignRestrictions(), "UTF-8")));
    	            	return null;
    	            }

                    ActionRedirect redirect = new ActionRedirect(mapping.findForward("colleagueOfferingDetail"));
                    redirect.addParameter("io", frm.getInstrOfferingId());
                    redirect.addParameter("co", frm.getCourseOfferingId());
                       redirect.addParameter("op", "view");
                    return redirect;
            	} catch (Exception e) {
            		throw e;
            	}
            }
            else {
                saveErrors(request, errors);
            }
        }

        if (op.equals(MSG.altDelete())) {
        	frm.deleteRestriction();
        }

        if (op.equals(MSG.altAdd())) {
        	frm.addRestriction();
        }

        return mapping.findForward("sectionRestrictionAssignment");
    }

	/**
     * Loads the form with the classes that are part of the instructional offering config
     * @param frm Form object
     * @param instrCoffrConfigId Instructional Offering Config Id
     * @param user User object
     */
    @SuppressWarnings("unchecked")
	private void doLoad(
    		HttpServletRequest request,
    		SectionRestrictionAssignmentForm frm,
            String instrOffrConfigId,
            InstrOfferingConfig ioc, CourseOffering co) throws Exception {

        // Check uniqueid
        if(instrOffrConfigId==null || instrOffrConfigId.trim().length()==0)
            throw new Exception (MSG.exceptionMissingIOConfig());

        // Load details
        InstructionalOffering io = ioc.getInstructionalOffering();

        // Load form properties
        frm.setInstrOffrConfigId(ioc.getUniqueId());
        frm.setInstrOffrConfigLimit(ioc.getLimit());
        frm.setInstrOfferingId(io.getUniqueId());
        frm.setCourseOfferingId(co.getUniqueId());

        frm.setDisplayExternalId(true);

        String name = io.getCourseNameWithTitle();
        if (io.hasMultipleConfigurations()) {
        	name += " [" + ioc.getName() +"]";
        }
        frm.setInstrOfferingName(name);

        if (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().size() == 0)
        	throw new Exception(MSG.exceptionIOConfigUndefined());

        InstrOfferingConfig config = ioc.getNextInstrOfferingConfig(sessionContext);
        if(config != null) {
        	if (!config.getInstructionalOffering().getUniqueId().equals(io.getUniqueId()) && config.getInstructionalOffering().getCourseOfferings().size() > 1) {
        		frm.setNextId(null);
        	} else {
        		frm.setNextId(config.getUniqueId().toString());
        	}
        } else {
        	frm.setNextId(null);
        }

        config = ioc.getPreviousInstrOfferingConfig(sessionContext);
        if(config != null) {
        	if (!config.getInstructionalOffering().getUniqueId().equals(io.getUniqueId()) && config.getInstructionalOffering().getCourseOfferings().size() > 1) {
        		frm.setPreviousId(null);
        	} else {
        		frm.setPreviousId(config.getUniqueId().toString());
        	}
    	} else {
            frm.setPreviousId(null);
        }

        ArrayList<SchedulingSubpart> subpartList = new ArrayList<SchedulingSubpart>(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());

        for(SchedulingSubpart ss : subpartList){
    		if (ss.getClasses() == null || ss.getClasses().size() == 0)
    			throw new Exception(MSG.exceptionInitialIOSetupIncomplete());
    		if (ss.getParentSubpart() == null){
        		loadSections(frm, ColleagueSection.findNotDeletedColleagueSectionsForSchedulingSubpartAndCourse(ss, co, ColleagueSectionDAO.getInstance().getSession()), new String());
        	}
        }
    }

    private void loadSections(SectionRestrictionAssignmentForm frm, Collection<ColleagueSection> sections, String indent){
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
		    		frm.addToSections(cs, !sessionContext.hasPermission(cs.getFirstClass(), Right.ClassEdit), indent);
		    		loadSections(frm, cs.getColleagueSectionToChildSections(), indent + "&nbsp;&nbsp;&nbsp;&nbsp;");
	    		}
	    	}
    	}
    }
}
