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
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.colleague.form.ColleagueCourseListForm;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.impl.LocalizedLookupDispatchAction;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.Messages;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;



/**
 * @author Stephanie Schluttenhofer
 */

@Service("/colleagueOfferingSearch")
public class ColleagueCourseSearchAction extends LocalizedLookupDispatchAction {
	protected final static ColleagueMessages MSG = Localization.create(ColleagueMessages.class);

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

	public ActionForward searchColleagueCourses(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {


	        sessionContext.checkPermission(Right.InstructionalOfferings);
        
	        // Check that a valid subject area is selected
		    ColleagueCourseListForm frm = (ColleagueCourseListForm) form;
		    ActionMessages errors = null;
		    errors = frm.validate(mapping, request);
		    
		    // Validation fails
		    if(errors.size()>0) {
			    saveErrors(request, errors);
			    frm.setCollections(sessionContext, null);
			    return mapping.findForward("showColleagueOfferingSearch");
		    }
	        
		    // Set Session Variables
		    sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, frm.getSubjectAreaId().toString());
		    sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, frm.getCourseNbr());
	        
	        // Perform Search
		    frm.setCollections(sessionContext, getInstructionalOfferings(sessionContext.getUser().getCurrentAcademicSessionId(), request, frm));
		    TreeSet<InstructionalOffering> instructionalOfferings = frm.getInstructionalOfferings();
			
			// No results returned
			if (instructionalOfferings.isEmpty()) {
			    errors.add("searchResult", new ActionMessage("errors.generic", "No records matching the search criteria were found."));
			    saveErrors(request, errors);
			    return mapping.findForward("showColleagueOfferingSearch");
			} 
			else {

				BackTracker.markForBack(
						request, 
						"colleagueOfferingSearch.do?op=Back&doit=Search&loadInstrFilter=1&subjectAreaId="+frm.getSubjectAreaId()+"&courseNbr="+URLEncoder.encode(frm.getCourseNbr(), "utf-8"), 
						"Colleague Offerings ("+
							(frm.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(new Long(frm.getSubjectAreaId()))).getSubjectAreaAbbreviation():frm.getSubjectAreaAbbv())+
							(frm.getCourseNbr()==null || frm.getCourseNbr().length()==0?"":" "+frm.getCourseNbr())+
							")", 
						true, true);

				if (request.getParameter("op")==null || 
			            (request.getParameter("op")!=null && !request.getParameter("op").equalsIgnoreCase("Back")) )  {
			        
				    // Search produces 1 result - redirect to colleague offering detail
				    if(instructionalOfferings.size()==1 && instructionalOfferings.first().getCourseOfferings().size() == 1) {
				    	Long courseOfferingId = ((CourseOffering)instructionalOfferings.first().getCourseOfferings().iterator().next()).getUniqueId();
				    	if (courseOfferingId != null){
					        request.setAttribute("op", "view");
					        request.setAttribute("co", courseOfferingId.toString());   
					        return mapping.findForward("showColleagueOfferingDetail");
				    	}
				    }
			    }
			    
			    return mapping.findForward("showColleagueOfferingList");
			}
		}
	
	
	public ActionForward exportPdf(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
	    
        ActionForward fwd = searchColleagueCourses(mapping, form, request, response);
                
        return fwd;
	}
	
	
	@SuppressWarnings("unchecked")
	public static TreeSet<InstructionalOffering> getInstructionalOfferings(
            Long sessionId, HttpServletRequest request, ColleagueCourseListForm form) {
                
        boolean fetchStructure = true;
        boolean fetchCredits = false;
        boolean fetchInstructors = false;
        boolean fetchPreferences = false;
        boolean fetchAssignments = true;
        boolean fetchReservations = false;
        
        return(InstructionalOffering.search(sessionId, new Long(form.getSubjectAreaId()), form.getCourseNbr(), fetchStructure, fetchCredits, fetchInstructors, fetchPreferences, fetchAssignments, fetchReservations));
        
    }

	
}
