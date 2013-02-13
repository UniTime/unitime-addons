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

package org.unitime.banner.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.form.BannerOfferingDetailForm;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.interfaces.ExternalLinkLookup;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;



/**
 * @author Stephanie Schluttenhofer
 */

@Service("/bannerOfferingDetail")
public class BannerOfferingDetailAction extends Action {
	protected final static BannerMessages MSG = Localization.create(BannerMessages.class);

	@Autowired SessionContext sessionContext;


    /** 
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    @SuppressWarnings("unchecked")
	public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {

        
        MessageResources rsc = getResources(request);
        BannerOfferingDetailForm frm = (BannerOfferingDetailForm) form;
        
        // Read Parameters
        String op = (request.getParameter("op")==null) 
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");		        
		if (op==null)
		    op = request.getParameter("hdnOp");
		
		// Check operation
		if(op==null || op.trim().length()==0)
		    throw new Exception (MSG.exceptionOperationNotInterpreted() + op);
		
		if ("n".equals(request.getParameter("confirm")))
			op = rsc.getMessage("op.view");

		Debug.debug ("Op: " + op);
		
		// Display detail - default
		if(op.equals(rsc.getMessage("op.view"))
		        || op.equals(rsc.getMessage("button.createClasses")) 
		        || op.equals(rsc.getMessage("button.updateConfig")) 
				|| op.equals("Resend to Banner")) {
		    String bannerCourseOfferingId = (request.getParameter("bc")==null)
		    							? (request.getAttribute("bc")==null)
		    							        ? null
		    							        : request.getAttribute("bc").toString()
		    							: request.getParameter("bc");
		    if (bannerCourseOfferingId==null && frm.getInstrOfferingId()!=null)
		    	bannerCourseOfferingId=frm.getBannerCourseOfferingId().toString();
			if(bannerCourseOfferingId==null || bannerCourseOfferingId.trim().length()==0)
			    throw new Exception (MSG.missingBannerCourseOfferingId(bannerCourseOfferingId));
			else  {
		    	sessionContext.checkPermission(getInstructionalOfferingIdForBannerCourseIdStr(bannerCourseOfferingId), "InstructionalOffering", Right.InstructionalOfferingDetail);

			    doLoad(request, frm, bannerCourseOfferingId);
			}
			
	        if (op.equals("Resend to Banner")) {
	        	response.sendRedirect(response.encodeURL("bannerOfferingDetail.do?bc="+frm.getBannerCourseOfferingId()));
	        	Vector<BannerSection> sections = new Vector<BannerSection>();
	        	BannerCourseDAO bcDao = BannerCourseDAO.getInstance();
	        	BannerCourse bc = (BannerCourse) bcDao.get(frm.getBannerCourseOfferingId());
	        	for (Iterator it = bc.getBannerConfigs().iterator(); it.hasNext();){
	        		BannerConfig bannerConfig = (BannerConfig) it.next();
	        		for(Iterator bsIt = bannerConfig.getBannerSections().iterator(); bsIt.hasNext();){
	        			sections.add((BannerSection) bsIt.next());
	        		}
	        	}
	        	SendBannerMessage.sendBannerMessage(sections, BannerMessageAction.UPDATE, bcDao.getSession());
	        } else {
				BackTracker.markForBack(
						request,
						"bannerOfferingDetail.do?bc="+frm.getBannerCourseOfferingId(),
						"Banner Offering ("+frm.getInstrOfferingNameNoTitle()+")",
						true, false);
	        }
			return mapping.findForward("showBannerConfigDetail");
	        
		}

    	sessionContext.checkPermission(frm.getInstrOfferingId(), "InstructionalOffering", Right.InstructionalOfferingDetail);	

        if (op.equals(rsc.getMessage("button.nextInstructionalOffering"))) {
        	response.sendRedirect(response.encodeURL("bannerOfferingDetail.do?bc="+frm.getNextId()));
        	return null;
        }
        
        if (op.equals(rsc.getMessage("button.previousInstructionalOffering"))) {
        	response.sendRedirect(response.encodeURL("bannerOfferingDetail.do?bc="+frm.getPreviousId()));
        	return null;
        }
		
		BackTracker.markForBack(
				request,
				"bannerOfferingDetail.do?bc="+frm.getBannerCourseOfferingId(),
				"Banner Offering ("+frm.getInstrOfferingName()+")",
				true, false);
		
		
		// Go back to banner offerings
        return mapping.findForward("showBannerOfferings");
        
    }

	/**
     * Loads the form initially
     * @param request
     * @param frm
     * @param bannerCourseOfferingIdStr
     */
    private void doLoad(
            HttpServletRequest request, 
            BannerOfferingDetailForm frm, 
            String bannerCourseOfferingIdStr) throws Exception {
        

        HttpSession httpSession = request.getSession();

        // Load Instr Offering
        Long bannerCourseOfferingId = new Long(bannerCourseOfferingIdStr);
        BannerCourseDAO bcDao = new BannerCourseDAO();
        BannerCourse bc = bcDao.get(bannerCourseOfferingId);
        CourseOffering co = bc.getCourseOffering(bcDao.getSession());
        InstructionalOffering io = co.getInstructionalOffering();
        Long subjectAreaId = co.getSubjectArea().getUniqueId();

    	sessionContext.checkPermission(io, Right.InstructionalOfferingDetail);
        
	    // Set Session Variables
        sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, subjectAreaId.toString());
        if (sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber) != null && !sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber).toString().isEmpty())
            sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, io.getControllingCourseOffering().getCourseNbr());

        
        // Get Configuration
        TreeSet<InstructionalOffering> ts = new TreeSet<InstructionalOffering>();
        ts.add(io);
	    
        // Sort Offerings
        ArrayList offerings = new ArrayList(io.getCourseOfferings());
        Collections.sort(
                offerings, 
                new CourseOfferingComparator(CourseOfferingComparator.COMPARE_BY_CTRL_CRS));
                
	    // Load Form
        frm.setBannerCourseOfferingId(bannerCourseOfferingId);
        frm.setInstrOfferingId(io.getUniqueId());
        frm.setSubjectAreaId(subjectAreaId);
        frm.setInstrOfferingName(co.getCourseNameWithTitle());
        frm.setSubjectAreaAbbr(co.getSubjectAreaAbbv());
        frm.setCourseNbr(co.getCourseNbr());
        frm.setInstrOfferingNameNoTitle(co.getCourseName());
        frm.setCtrlCrsOfferingId(io.getControllingCourseOffering().getUniqueId());
        frm.setDemand(io.getDemand());
        frm.setEnrollment(io.getEnrollment());
        frm.setProjectedDemand(io.getProjectedDemand());
        frm.setLimit(io.getLimit());
        frm.setUnlimited(Boolean.FALSE);
        frm.setCreditText((io.getCredit() != null)?io.getCredit().creditText():"");
        
        if (co.getConsentType()==null)
            frm.setConsentType("None Required");
        else
            frm.setConsentType(co.getConsentType().getLabel());
        
        for (Iterator i=io.getInstrOfferingConfigs().iterator();i.hasNext();)
        	if (((InstrOfferingConfig)i.next()).isUnlimitedEnrollment().booleanValue()) {
        		frm.setUnlimited(Boolean.TRUE); break;
        	}
        frm.setNotOffered(io.isNotOffered());
        frm.setCourseOfferings(offerings);
	    
        // Check limits on courses if cross-listed
        if (io.getCourseOfferings().size() > 1 && !frm.getUnlimited().booleanValue()) {
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
        	ExternalLinkLookup lookup = (ExternalLinkLookup) (Class.forName(linkLookupClass).newInstance());
       		Map results = lookup.getLink(io);
            if (results==null)
                throw new Exception (lookup.getErrorMessage());
            
            frm.setCatalogLinkLabel((String)results.get(ExternalLinkLookup.LINK_LABEL));
            frm.setCatalogLinkLocation((String)results.get(ExternalLinkLookup.LINK_LOCATION));
        }
        
	    BannerCourse next = bc.getNextBannerCourse(sessionContext, false, true);
        frm.setNextId(next==null?null:next.getUniqueId().toString());
        BannerCourse previous = bc.getPreviousBannerCourse(sessionContext, false, true);
        frm.setPreviousId(previous==null?null:previous.getUniqueId().toString());
	                
    }
    
    public Long getInstructionalOfferingIdForBannerCourseIdStr(String bannerCourseId){
    	
        // Load Instr Offering
        Long bannerCourseOfferingId = new Long(bannerCourseId);
        BannerCourseDAO bcDao = new BannerCourseDAO();
        BannerCourse bc = bcDao.get(bannerCourseOfferingId);
        CourseOffering co = bc.getCourseOffering(bcDao.getSession());
        if (co != null && co.getInstructionalOffering() != null){
        	return(co.getInstructionalOffering().getUniqueId());
        }
        return(null);
    }

 }
