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
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.colleague.dataexchange.ColleagueMessage.MessageAction;
import org.unitime.colleague.dataexchange.SendColleagueMessage;
import org.unitime.colleague.form.ColleagueOfferingDetailForm;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.dao.ColleagueSectionDAO;
import org.unitime.colleague.webutil.WebColleagueConfigTableBuilder;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;



/**
 * @author Stephanie Schluttenhofer
 */

@Action(value = "colleagueOfferingDetail", results = {
		@Result(name = "showColleagueConfigDetail", type = "tiles", location = "colleagueOfferingDetail.tiles"),
		@Result(name = "showPrevNextDetail", type = "redirect", location = "/colleagueOfferingDetail.action",
			params = { "co", "${co}", "op", "view"}),
		@Result(name = "showColleagueOfferings", type = "redirect", location = "/colleagueOfferingSearch.action",
			params = { "anchor", "A${co}"})
	})
@TilesDefinition(name = "colleagueOfferingDetail.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Colleague Offering Detail"),
		@TilesPutAttribute(name = "body", value = "/colleague/colleagueOfferingDetail.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
	})
public class ColleagueOfferingDetailAction extends UniTimeAction<ColleagueOfferingDetailForm> {
	private static final long serialVersionUID = -741344115271613055L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);

	private Long courseOfferingId;
	
	public Long getCo() { return courseOfferingId; }
	public void setCo(Long co) { this.courseOfferingId = co; }

    @Override
	public String execute() throws Exception {
    	if (form == null) form = new ColleagueOfferingDetailForm();

        // Read Parameters
    	if (op == null) op = form.getOp();
		
		// Check operation
		if (op==null || op.trim().isEmpty())
		    throw new Exception (MSG.exceptionOperationNotInterpreted() + op);
		
		if (getCo() == null)
			setCo(form.getCourseOfferingId());

		Debug.debug ("Op: " + op);
		
		// Display detail - default
		if (op.equals("view") || op.equals(CMSG.actionResendToColleague())) {
			if (courseOfferingId==null || courseOfferingId < 0)
			    throw new Exception (CMSG.missingCourseOfferingId(courseOfferingId));
			else  {
		    	sessionContext.checkPermission(getInstructionalOfferingIdForCourseIdStr(courseOfferingId), "InstructionalOffering", Right.InstructionalOfferingDetail);

			    doLoad(courseOfferingId);
			}
			
	        if (op.equals(CMSG.actionResendToColleague())) {
	        	ColleagueSectionDAO csDao = ColleagueSectionDAO.getInstance();
	        	List<ColleagueSection> colleagueSections = ColleagueSection.findColleagueSectionsForCourseOfferingId(Long.valueOf(form.getCourseOfferingId()), csDao.getSession());
	        	
	        	SendColleagueMessage.sendColleagueMessage(colleagueSections, MessageAction.UPDATE, csDao.getSession());
	        	return "showPrevNextDetail";
	        } else {
				BackTracker.markForBack(
						request,
						"colleagueOfferingDetail.action?co="+form.getCourseOfferingId(),
						CMSG.sectColleagueOffering() + " ("+form.getInstrOfferingNameNoTitle()+")",
						true, false);
	        }
			return "showColleagueConfigDetail";
	        
		}

    	sessionContext.checkPermission(form.getInstrOfferingId(), "InstructionalOffering", Right.InstructionalOfferingDetail);	

    	if (op.equals(MSG.actionNextIO())) {
	    	setCo(form.getNextId());
        	return "showPrevNextDetail";

        }
        
        if (op.equals(MSG.actionPreviousIO())) {
        	setCo(form.getPreviousId());
        	return "showPrevNextDetail";
        }
        
        if (op.equals(MSG.actionLockIO())) {
		    InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(form.getInstrOfferingId());

	    	sessionContext.checkPermission(io, Right.OfferingCanLock);

	    	io.getSession().lockOffering(io.getUniqueId());
	    	return "showPrevNextDetail";
        }
		
        if (op.equals(MSG.actionUnlockIO())) {
	    	InstructionalOfferingDAO idao = new InstructionalOfferingDAO();
	        InstructionalOffering io = idao.get(form.getInstrOfferingId());

	    	sessionContext.checkPermission(io, Right.OfferingCanUnlock);

	        io.getSession().unlockOffering(io, sessionContext.getUser());
	        return "showPrevNextDetail";
        }

        BackTracker.markForBack(
				request,
				"colleagueOfferingDetail.action?co="+form.getCourseOfferingId(),
				CMSG.sectColleagueOffering() + " ("+form.getInstrOfferingName()+")",
				true, false);
		
		
		// Go back to colleague offerings
        return "showColleagueOfferings";
    }

	/**
     * Loads the form initially
     */
    private void doLoad(Long courseOfferingId) throws Exception {
        // Load Instr Offering
        CourseOfferingDAO coDao = new CourseOfferingDAO();

        CourseOffering co = coDao.get(courseOfferingId);
        InstructionalOffering io = co.getInstructionalOffering();
        Long subjectAreaId = co.getSubjectArea().getUniqueId();

    	sessionContext.checkPermission(io, Right.InstructionalOfferingDetail);
        
	    // Set Session Variables
    	/*
        sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjectAreaId.toString());
        if (sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber) != null && !sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber).toString().isEmpty())
            sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, io.getControllingCourseOffering().getCourseNbr());
        */

	    
        // Sort Offerings
        ArrayList offerings = new ArrayList(io.getCourseOfferings());
        Collections.sort(
                offerings, 
                new CourseOfferingComparator(CourseOfferingComparator.COMPARE_BY_CTRL_CRS));
                
	    // Load Form
        form.setCourseOfferingId(courseOfferingId);
        form.setInstrOfferingId(io.getUniqueId());
        form.setSubjectAreaId(subjectAreaId);
        form.setInstrOfferingName(co.getCourseNameWithTitle());
        form.setSubjectAreaAbbr(co.getSubjectAreaAbbv());
        form.setCourseNbr(co.getCourseNbr());
        form.setInstrOfferingNameNoTitle(co.getCourseName());
        form.setCtrlCrsOfferingId(io.getControllingCourseOffering().getUniqueId());
        form.setDemand(io.getDemand());
        form.setEnrollment(io.getEnrollment());
        form.setProjectedDemand(io.getProjectedDemand());
        form.setLimit(io.getLimit());
        form.setUnlimited(Boolean.FALSE);
        form.setCreditText((co.getCredit() != null)?co.getCredit().creditText():"");
        
        if (co.getConsentType()==null)
            form.setConsentType(MSG.noConsentRequired());
        else
            form.setConsentType(co.getConsentType().getLabel());
        
        for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();)
        	if (((InstrOfferingConfig)i.next()).isUnlimitedEnrollment().booleanValue()) {
        		form.setUnlimited(Boolean.TRUE); break;
        	}
        form.setNotOffered(io.isNotOffered());
        form.setCourseOfferings(offerings);
	    
        // Check limits on courses if cross-listed
        if (io.getCourseOfferings().size() > 1 && !form.getUnlimited().booleanValue()) {
        	int lim = 0;
        	for (CourseOffering course: io.getCourseOfferings()) {
        		if (course.getReservation() != null)
        			lim += course.getReservation();
        	}
            if (io.getLimit() != null && lim != io.getLimit().intValue()) {
                request.setAttribute("limitsDoNotMatch", "" + lim);
            }
        }
    
        // Catalog Link
        String linkLookupClass = ApplicationProperties.getProperty("tmtbl.catalogLink.lookup.class");
        if (linkLookupClass!=null && linkLookupClass.trim().length()>0) {
        	ExternalLinkLookup lookup = (ExternalLinkLookup) (Class.forName(linkLookupClass).getDeclaredConstructor().newInstance());
       		Map results = lookup.getLink(io);
            if (results==null)
                throw new Exception (lookup.getErrorMessage());
            
            form.setCatalogLinkLabel((String)results.get(ExternalLinkLookup.LINK_LABEL));
            form.setCatalogLinkLocation((String)results.get(ExternalLinkLookup.LINK_LOCATION));
        }
        
        
	    InstructionalOffering next = io.getNextInstructionalOffering(sessionContext);
        form.setNextId(next==null?null:next.getControllingCourseOffering().getUniqueId());
        InstructionalOffering previous = io.getPreviousInstructionalOffering(sessionContext);
        form.setPreviousId(previous==null?null:previous.getControllingCourseOffering().getUniqueId());
	                
    }
    
    public Long getInstructionalOfferingIdForCourseIdStr(Long courseOfferingId){
        // Load Instr Offering
        CourseOfferingDAO coDao = new CourseOfferingDAO();
        CourseOffering co = coDao.get(courseOfferingId);
        if (co != null && co.getInstructionalOffering() != null){
        	return(co.getInstructionalOffering().getUniqueId());
        }
        return(null);
    }
    
    public String getCsrNbr() {
    	return (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
    }
    
    public String printTable() throws Exception {
    	new WebColleagueConfigTableBuilder().htmlConfigTablesForColleagueOffering(
    								getClassAssignmentService().getAssignment(),
				    		        form.getInstrOfferingId(),
				    		        form.getCourseOfferingId(),
				    		        sessionContext,
				    		        getPageContext().getOut(),
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
		return "";
    }

 }
