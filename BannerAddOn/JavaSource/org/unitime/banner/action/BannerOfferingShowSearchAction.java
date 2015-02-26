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

import java.util.Collection;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.banner.form.BannerCourseListForm;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.Messages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;



/**
 * @author Stephanie Schluttenhofer
 */

@Service("/bannerOfferingShowSearch")
public class BannerOfferingShowSearchAction extends Action {

	protected final static BannerMessages MSG = Localization.create(BannerMessages.class);

	@Autowired SessionContext sessionContext;
	
	protected Messages getMessages() {
		return MSG;
	}

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {

    	sessionContext.checkPermission(Right.InstructionalOfferings);

        
        BackTracker.markForBack(request, null, null, false, true); //clear back list
        
        sessionContext.setAttribute("callingPage", "bannerOfferingShowSearch");
        BannerCourseListForm frm = (BannerCourseListForm) form;
        
        // Check if subject area / course number saved to session
	    Object sa = sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
	    Object cn = sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
	    String subjectAreaId = "";
	    String courseNbr = "";
	    
	    if ( (sa==null || sa.toString().trim().length()==0) 
	            && (cn==null || cn.toString().trim().length()==0) ) {
		    // use session variables from class search  
		    sa = sessionContext.getAttribute(SessionAttribute.ClassesSubjectAreas);
		    cn = sessionContext.getAttribute(SessionAttribute.ClassesCourseNumber);
		    
		    // Use first subject area
		    if (sa!=null) {
		       String saStr = sa.toString();
		       if (saStr.indexOf(",")>0) {
		           sa = saStr.substring(0, saStr.indexOf(","));
		       }
		    }
	    }
	    
	    
//	    BannerCourseSearchAction.setupInstrOffrListSpecificFormFilters(httpSession, frm);
	    /*
	    if (request.getParameter("subjectAreaId") != null){
	    	frm.setDivSec(request.getParameter("divSec")==null?Boolean.FALSE:new Boolean(request.getParameter("divSec")));
	    	frm.setDemand(request.getParameter("demand")==null?Boolean.FALSE:new Boolean(request.getParameter("demand")));
	    	frm.setProjectedDemand(request.getParameter("projectedDemand")==null?Boolean.FALSE:new Boolean(request.getParameter("projectedDemand")));
	    	frm.setMinPerWk(request.getParameter("minPerWk")==null?Boolean.FALSE:new Boolean(request.getParameter("minPerWk")));
	    	frm.setLimit(request.getParameter("limit")==null?Boolean.FALSE:new Boolean(request.getParameter("limit")));
	    	frm.setRoomLimit(request.getParameter("roomLimit")==null?Boolean.FALSE:new Boolean(request.getParameter("roomLimit")));
	    	frm.setManager(request.getParameter("manager")==null?Boolean.FALSE:new Boolean(request.getParameter("manager")));
	    	frm.setDatePattern(request.getParameter("datePattern")==null?Boolean.FALSE:new Boolean(request.getParameter("datePattern")));
	    	frm.setTimePattern(request.getParameter("timePattern")==null?Boolean.FALSE:new Boolean(request.getParameter("timePattern")));
	    	frm.setPreferences(request.getParameter("preferences")==null?Boolean.FALSE:new Boolean(request.getParameter("preferences")));
	    	frm.setInstructor(request.getParameter("instructor")==null?Boolean.FALSE:new Boolean(request.getParameter("instructor")));
	    	frm.setTimetable(request.getParameter("timetable")==null?Boolean.FALSE:new Boolean(request.getParameter("timetable")));
	    	frm.setCredit(request.getParameter("credit")==null?Boolean.FALSE:new Boolean(request.getParameter("credit")));
	    	frm.setSchedulePrintNote(request.getParameter("schedulePrintNote")==null?Boolean.FALSE:new Boolean(request.getParameter("schedulePrintNote")));
	    }
	    */
	    // Subject Area is saved to the session - Perform automatic search
	    if(sa!=null) {
	        subjectAreaId = sa.toString();
	        
	        try {
	            
		        if(cn!=null && cn.toString().trim().length()>0)
		            courseNbr = cn.toString();
		        
		        Debug.debug("Subject Area: " + subjectAreaId);
		        Debug.debug("Course Number: " + courseNbr);
		        
		        frm.setSubjectAreaId(subjectAreaId);
		        frm.setCourseNbr(courseNbr);
		        
		        if(doSearch(request, frm)) {
					BackTracker.markForBack(
							request, 
							"bannerOfferingSearch.do?doit=Search&loadInstrFilter=1&subjectAreaId="+frm.getSubjectAreaId()+"&courseNbr="+frm.getCourseNbr(), 
							"Instructional Offerings ("+
								(frm.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(new Long(frm.getSubjectAreaId()))).getSubjectAreaAbbreviation():frm.getSubjectAreaAbbv())+
								(frm.getCourseNbr()==null || frm.getCourseNbr().length()==0?"":" "+frm.getCourseNbr())+
								")", 
							true, true);
		            return mapping.findForward("showBannerOfferingList");
		        	
		        }
	        }
	        catch (NumberFormatException nfe) {
	            Debug.error("Subject Area Id session attribute is corrupted. Resetting ... ");
	            sessionContext.removeAttribute(SessionAttribute.OfferingsSubjectArea);
	            sessionContext.removeAttribute(SessionAttribute.OfferingsCourseNumber);
	        }
	    }
	    
	    // No session attribute found - Load subject areas
	    else {	        
	        frm.setCollections(sessionContext, null);
	        
	        // Check if only 1 subject area exists
	        Set s = (Set) frm.getSubjectAreas();
	        if(s.size()==1) {
	            Debug.debug("Exactly 1 subject area found ... ");
	            frm.setSubjectAreaId(((SubjectArea) s.iterator().next()).getUniqueId().toString());
		        if(doSearch(request, frm)) {
					BackTracker.markForBack(
							request, 
							"bannerOfferingSearch.do?doit=Search&loadInstrFilter=1&subjectAreaId="+frm.getSubjectAreaId()+"&courseNbr="+frm.getCourseNbr(), 
							"Instructional Offerings ("+
								(frm.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(new Long(frm.getSubjectAreaId()))).getSubjectAreaAbbreviation():frm.getSubjectAreaAbbv())+
								(frm.getCourseNbr()==null || frm.getCourseNbr().length()==0?"":" "+frm.getCourseNbr())+
								")", 
							true, true);
		            return mapping.findForward("showBannerOfferingList");
		        }
	        }
	    }
	    
        return mapping.findForward("showBannerOfferingSearch");
	}

	/**
	 * Perform search based on form values of subject area and course number
	 * If search produces results - generate html and store the html as a request attribute
	 * @param request
	 * @param frm
	 * @return true if search returned results, false otherwise
	 * @throws Exception
	 */
	private boolean doSearch(
	        HttpServletRequest request,
	        BannerCourseListForm frm) throws Exception {
	    
	    
	    frm.setCollections(sessionContext, BannerCourseSearchAction.getInstructionalOfferings(sessionContext.getUser().getCurrentAcademicSessionId(), request, frm));
		Collection instrOfferings = frm.getInstructionalOfferings();
        
		// Search return results - Generate html
		if (!instrOfferings.isEmpty()) {
		    return true;
		}
		
		return false;
	}
	
}
