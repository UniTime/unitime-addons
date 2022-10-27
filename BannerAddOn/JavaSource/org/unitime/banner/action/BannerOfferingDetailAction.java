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

package org.unitime.banner.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.form.BannerOfferingDetailForm;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.banner.webutil.WebBannerConfigTableBuilder;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;



/**
 * @author Stephanie Schluttenhofer
 */

@Action(value = "bannerOfferingDetail", results = {
		@Result(name = "showBannerConfigDetail", type = "tiles", location = "bannerOfferingDetail.tiles"),
		@Result(name = "showPrevNextDetail", type = "redirect", location = "/bannerOfferingDetail.action",
			params = { "bc", "${bc}", "op", "view"}),
		@Result(name = "showBannerOfferings", type = "redirect", location = "/bannerOfferingSearch.action",
			params = { "anchor", "A${bc}"})
	})
@TilesDefinition(name = "bannerOfferingDetail.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Banner Offering Detail"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerOfferingDetail.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
	})
public class BannerOfferingDetailAction extends UniTimeAction<BannerOfferingDetailForm> {
	private static final long serialVersionUID = 1680440094462959564L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);

	private Long bannerCourseOfferingId;
	
	public Long getBc() { return bannerCourseOfferingId; }
	public void setBc(Long bc) { this.bannerCourseOfferingId = bc; }

	@Override
	public String execute() throws Exception {
		if (form == null) form = new BannerOfferingDetailForm();
		
        // Read Parameters
		if (op == null) op = form.getOp();
		
		// Check operation
		if (op==null || op.trim().isEmpty())
		    throw new Exception (MSG.exceptionOperationNotInterpreted() + op);
		
		if (getBc() == null)
        	setBc(form.getBannerCourseOfferingId());

		Debug.debug ("Op: " + op);
		
		if (op.equals(BMSG.actionBackToBannerOfferings())) {
			return "showBannerOfferings";
		}
		
		// Display detail - default
		if (op.equals("view") || op.equals(BMSG.actionResendToBanner())) {
			if (bannerCourseOfferingId==null || bannerCourseOfferingId < 0l)
			    throw new Exception (BMSG.missingBannerCourseOfferingId(bannerCourseOfferingId));
			else  {
		    	sessionContext.checkPermission(getInstructionalOfferingIdForBannerCourseIdStr(bannerCourseOfferingId), "InstructionalOffering", Right.InstructionalOfferingDetail);

			    doLoad(bannerCourseOfferingId);
			}
			
	        if (op.equals(BMSG.actionResendToBanner())) {
	        	Vector<BannerSection> sections = new Vector<BannerSection>();
	        	BannerCourseDAO bcDao = BannerCourseDAO.getInstance();
	        	BannerCourse bc = (BannerCourse) bcDao.get(form.getBannerCourseOfferingId());
	        	for (Iterator it = bc.getBannerConfigs().iterator(); it.hasNext();){
	        		BannerConfig bannerConfig = (BannerConfig) it.next();
	        		for(Iterator bsIt = bannerConfig.getBannerSections().iterator(); bsIt.hasNext();){
	        			sections.add((BannerSection) bsIt.next());
	        		}
	        	}
	        	SendBannerMessage.sendBannerMessage(sections, BannerMessageAction.UPDATE, bcDao.getSession());
	        	return "showPrevNextDetail";
	        } else {
				BackTracker.markForBack(
						request,
						"bannerOfferingDetail.action?bc="+form.getBannerCourseOfferingId(),
						BMSG.sectBannerOffering() + " ("+form.getInstrOfferingNameNoTitle()+")",
						true, false);
	        }
			return "showBannerConfigDetail";
		}

    	sessionContext.checkPermission(form.getInstrOfferingId(), "InstructionalOffering", Right.InstructionalOfferingDetail);	

        if (op.equals(MSG.actionNextIO())) {
	    	setBc(form.getNextId());
        	return "showPrevNextDetail";

        }
        
        if (op.equals(MSG.actionPreviousIO())) {
        	setBc(form.getPreviousId());
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
				"bannerOfferingDetail.action?bc="+form.getBannerCourseOfferingId(),
				BMSG.sectBannerOffering() + " ("+form.getInstrOfferingName()+")",
				true, false);
		
		
		// Go back to banner offerings
        if (getBc() == null)
        	setBc(form.getBannerCourseOfferingId());
        return "showBannerOfferings";
        
    }

	/**
     * Loads the form initially
     */
    private void doLoad(Long bannerCourseOfferingId) throws Exception {
        // Load Instr Offering
        BannerCourseDAO bcDao = new BannerCourseDAO();
        BannerCourse bc = bcDao.get(bannerCourseOfferingId);
        CourseOffering co = bc.getCourseOffering(bcDao.getSession());
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
        form.setBannerCourseOfferingId(bannerCourseOfferingId);
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
        
	    BannerCourse next = bc.getNextBannerCourse(sessionContext, false, true);
        form.setNextId(next==null?null:next.getUniqueId());
        BannerCourse previous = bc.getPreviousBannerCourse(sessionContext, false, true);
        form.setPreviousId(previous==null?null:previous.getUniqueId());
	                
    }
    
    public Long getInstructionalOfferingIdForBannerCourseIdStr(Long bannerCourseOfferingId){
        // Load Instr Offering
        BannerCourseDAO bcDao = new BannerCourseDAO();
        BannerCourse bc = bcDao.get(bannerCourseOfferingId);
        CourseOffering co = bc.getCourseOffering(bcDao.getSession());
        if (co != null && co.getInstructionalOffering() != null){
        	return(co.getInstructionalOffering().getUniqueId());
        }
        return(null);
    }

    
    public String getCsrNbr() {
    	return (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
    }
    
    public String printTable() throws Exception {
    	new WebBannerConfigTableBuilder().htmlConfigTablesForBannerOffering(
    								getClassAssignmentService().getAssignment(),
				    		        form.getInstrOfferingId(),
				    		        form.getBannerCourseOfferingId(),
				    		        sessionContext,
				    		        getPageContext().getOut(),
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
		return "";
    }
 }
