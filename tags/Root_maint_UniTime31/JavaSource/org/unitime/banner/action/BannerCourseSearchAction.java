/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.banner.action;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.LookupDispatchAction;
import org.hibernate.HibernateException;
import org.unitime.banner.form.BannerCourseListForm;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.form.InstructionalOfferingListForm;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.pdf.PdfInstructionalOfferingTableBuilder;



/**
 * @author Stephanie Schluttenhofer
 */

public class BannerCourseSearchAction extends LookupDispatchAction {

	protected Map<String, String> getKeyMethodMap() {
	      Map<String, String> map = new HashMap<String, String>();
	      map.put("button.search", "searchBannerCourses");
	      map.put("button.exportPDF", "exportPdf");
	      map.put("button.cancel", "searchBannerCourses");
	      return map;
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

	public ActionForward searchBannerCourses(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

	    	HttpSession httpSession = request.getSession();
	        if(!Web.isLoggedIn( httpSession )) {
	            throw new Exception ("Access Denied.");
	        }
        
	        // Check that a valid subject area is selected
		    BannerCourseListForm frm = (BannerCourseListForm) form;
		    ActionMessages errors = null;
		    errors = frm.validate(mapping, request);
		    
		    // Validation fails
		    if(errors.size()>0) {
			    saveErrors(request, errors);
			    frm.setCollections(request, null);
			    return mapping.findForward("showBannerOfferingSearch");
		    }

		    // Set Session Variables
	        httpSession.setAttribute(Constants.SUBJ_AREA_ID_ATTR_NAME, frm.getSubjectAreaId());
	        httpSession.setAttribute(Constants.CRS_NBR_ATTR_NAME, frm.getCourseNbr());
	        
	        
	        // Perform Search
		    frm.setCollections(request, getInstructionalOfferings(request, frm));
		    TreeSet<InstructionalOffering> instructionalOfferings = frm.getInstructionalOfferings();
			
			// No results returned
			if (instructionalOfferings.isEmpty()) {
			    if(errors==null) errors = new ActionMessages();
			    errors.add("searchResult", new ActionMessage("errors.generic", "No records matching the search criteria were found."));
			    saveErrors(request, errors);
			    return mapping.findForward("showBannerOfferingSearch");
			} 
			else {

				BackTracker.markForBack(
						request, 
						"bannerOfferingSearch.do?op=Back&doit=Search&loadInstrFilter=1&subjectAreaId="+frm.getSubjectAreaId()+"&courseNbr="+frm.getCourseNbr(), 
						"Banner Offerings ("+
							(frm.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(new Long(frm.getSubjectAreaId()))).getSubjectAreaAbbreviation():frm.getSubjectAreaAbbv())+
							(frm.getCourseNbr()==null || frm.getCourseNbr().length()==0?"":" "+frm.getCourseNbr())+
							")", 
						true, true);

				if (request.getParameter("op")==null || 
			            (request.getParameter("op")!=null && !request.getParameter("op").equalsIgnoreCase("Back")) )  {
			        
				    // Search produces 1 result - redirect to banner offering detail
				    if(instructionalOfferings.size()==1 && instructionalOfferings.first().getCourseOfferings().size() == 1) {
				    	BannerCourse bc = BannerCourse.findBannerCourseForCourseOffering(((CourseOffering)instructionalOfferings.first().getCourseOfferings().iterator().next()).getUniqueId(), BannerCourseDAO.getInstance().getSession());
				    	if (bc != null){
					        request.setAttribute("op", "view");
					        request.setAttribute("bc", bc.getUniqueId().toString());   
					        return mapping.findForward("showBannerOfferingDetail");
				    	}
				    }
			    }
			    
			    return mapping.findForward("showBannerOfferingList");
			}
		}
	
	
	public ActionForward exportPdf(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	    
        ActionForward fwd = searchBannerCourses(mapping, form, request, response);
        
        InstructionalOfferingListForm frm = (InstructionalOfferingListForm) form;
        
        if (getErrors(request).isEmpty()) {
            File pdfFile = 
                (new PdfInstructionalOfferingTableBuilder())
                .pdfTableForInstructionalOfferings(
                        WebSolver.getClassAssignmentProxy(request.getSession()),
                        WebSolver.getExamSolver(request.getSession()),
                        frm, 
                        new Long(frm.getSubjectAreaId()), 
                        Web.getUser(request.getSession()),  
                        true, 
                        frm.getCourseNbr()==null || frm.getCourseNbr().length()==0);
            
            if (pdfFile!=null) {
                request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+pdfFile.getName());
                //response.sendRedirect("temp/"+pdfFile.getName());
            } else {
                getErrors(request).add("searchResult", new ActionMessage("errors.generic", "Unable to create PDF file."));
            }
        }
        
        return fwd;
	}
	
	
	@SuppressWarnings("unchecked")
	public static TreeSet<InstructionalOffering> getInstructionalOfferings(
            HttpServletRequest request, BannerCourseListForm form) {
        
        HttpSession httpSession = request.getSession();
        User user = Web.getUser(httpSession);
        Long sessionId = (Long) user.getAttribute(Constants.SESSION_ID_ATTR_NAME);
        
        
        boolean fetchStructure = true;
        boolean fetchCredits = false;
        boolean fetchInstructors = false;
        boolean fetchPreferences = false;
        boolean fetchAssignments = true;
        boolean fetchReservations = false;
        
        return(InstructionalOffering.search(sessionId, form.getSubjectAreaId(), form.getCourseNbr(), fetchStructure, fetchCredits, fetchInstructors, fetchPreferences, fetchAssignments, fetchReservations));
        
    }

}
