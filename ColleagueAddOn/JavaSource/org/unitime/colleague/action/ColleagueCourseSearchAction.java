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
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.colleague.form.ColleagueCourseListForm;
import org.unitime.colleague.webutil.WebColleagueCourseListTableBuilder;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.webutil.BackTracker;



/**
 * @author Stephanie Schluttenhofer
 */
@Action(value="colleagueOfferingSearch", results = {
		@Result(name = "showColleagueOfferingList", type = "tiles", location = "colleagueOfferingSearch.tiles"),
		@Result(name = "showColleagueOfferingSearch", type = "tiles", location = "colleagueOfferingSearch.tiles"),
		@Result(name = "showColleagueOfferingDetail", type = "redirect", location = "/colleagueOfferingDetail.do",
			params = {"op" , "view", "co", "${co}"}
		)
	})
@TilesDefinition(name = "colleagueOfferingSearch.tiles", extend =  "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Colleague Offerings"),
		@TilesPutAttribute(name = "body", value = "/colleague/colleagueOfferingSearch.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
})
public class ColleagueCourseSearchAction extends UniTimeAction<ColleagueCourseListForm> {
	private static final long serialVersionUID = 6887043745334166211L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);

	private String doit;
	private Long co;
	private boolean showTable = false;

	public Long getCo() { return co; }
	public void setCo(Long co) { this.co = co; }
	public String getDoit() { return doit; }
	public void setDoit(String doit) { this.doit = doit; }
	public boolean isShowTable() { return showTable; }
	public void setShowTable(boolean showTable) { this.showTable = showTable; }
			
	@Override
	public String execute() throws Exception {
		if (form == null) form = new ColleagueCourseListForm();
		sessionContext.checkPermission(Right.InstructionalOfferings);
		
		if (CMSG.actionSearchColleagueOfferings().equals(doit) || "Search".equals(doit)) {
			return searchColleagueCourses();
		}
		
		BackTracker.markForBack(request, null, null, false, true); //clear back list
        
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
	    	    
	    // Subject Area is saved to the session - Perform automatic search
	    if(sa!=null) {
	        subjectAreaId = sa.toString();
	        
	        try {
	            
		        if(cn!=null && cn.toString().trim().length()>0)
		            courseNbr = cn.toString();
		        
		        Debug.debug("Subject Area: " + subjectAreaId);
		        Debug.debug("Course Number: " + courseNbr);
		        
		        form.setSubjectAreaId(subjectAreaId);
		        form.setCourseNbr(courseNbr);
		        
		        if(doSearch()) {
					BackTracker.markForBack(
							request, 
							"colleagueOfferingSearch.action?doit=Search&form.subjectAreaId="+form.getSubjectAreaId()+"&form.courseNbr="+form.getCourseNbr(), 
							CMSG.sectColleagueOfferings() + " ("+
								(form.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(Long.valueOf(form.getSubjectAreaId()))).getSubjectAreaAbbreviation():form.getSubjectAreaAbbv())+
								(form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr())+
								")", 
							true, true);
					setShowTable(true);
		            return "showColleagueOfferingList";
		        	
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
	        form.setCollections(sessionContext, null);
	        
	        // Check if only 1 subject area exists
	        Set s = (Set) form.getSubjectAreas();
	        if(s.size()==1) {
	            Debug.debug("Exactly 1 subject area found ... ");
	            form.setSubjectAreaId(((SubjectArea) s.iterator().next()).getUniqueId().toString());
		        if (doSearch()) {
					BackTracker.markForBack(
							request, 
							"colleagueOfferingSearch.action?doit=Search&form.subjectAreaId="+form.getSubjectAreaId()+"&form.courseNbr="+form.getCourseNbr(), 
							CMSG.sectColleagueOfferings() + " ("+
								(form.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(Long.valueOf(form.getSubjectAreaId()))).getSubjectAreaAbbreviation():form.getSubjectAreaAbbv())+
								(form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr())+
								")", 
							true, true);
					setShowTable(true);
		            return "showColleagueOfferingList";
		        }
	        }
	    }
	    
	    setShowTable(false);
        return "showColleagueOfferingSearch";
	}

	public String searchColleagueCourses() throws Exception {
		sessionContext.checkPermission(Right.InstructionalOfferings);
        
        // Check that a valid subject area is selected
	    form.validate(this);
	    
	    // Validation fails
	    if (hasFieldErrors()) {
		    form.setCollections(sessionContext, null);
		    setShowTable(false);
		    return "showColleagueOfferingSearch";
	    }
        
	    // Set Session Variables
	    sessionContext.setAttribute(SessionAttribute.OfferingsSubjectArea, form.getSubjectAreaId().toString());
	    sessionContext.setAttribute(SessionAttribute.OfferingsCourseNumber, form.getCourseNbr());
        
        // Perform Search
	    form.setCollections(sessionContext, getInstructionalOfferings());
	    TreeSet<InstructionalOffering> instructionalOfferings = form.getInstructionalOfferings();
		
		// No results returned
		if (instructionalOfferings.isEmpty()) {
			addFieldError("searchResult", MSG.errorNoRecords());
			setShowTable(false);
		    return "showColleagueOfferingSearch";
		} else {

			BackTracker.markForBack(
					request, 
					"colleagueOfferingSearch.action?op=Back&doit=Search&form.subjectAreaId="+form.getSubjectAreaId()+"&form.courseNbr="+URLEncoder.encode(form.getCourseNbr(), "utf-8"),
					CMSG.sectColleagueOfferings() + " ("+
						(form.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(Long.valueOf(form.getSubjectAreaId()))).getSubjectAreaAbbreviation():form.getSubjectAreaAbbv())+
						(form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr())+
						")", 
					true, true);

			if ("Back".equals(op)) {
		        
			    // Search produces 1 result - redirect to colleague offering detail
			    if(instructionalOfferings.size()==1 && instructionalOfferings.first().getCourseOfferings().size() == 1) {
			    	Long courseOfferingId = ((CourseOffering)instructionalOfferings.first().getCourseOfferings().iterator().next()).getUniqueId();
			    	if (courseOfferingId != null){
			    		setCo(courseOfferingId);   
				        return "showColleagueOfferingDetail";
			    	}
			    }
		    }
		    
			setShowTable(true);
		    return "showColleagueOfferingList";
		}
	}
	
	
	public TreeSet<InstructionalOffering> getInstructionalOfferings() {
		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
        boolean fetchStructure = true;
        boolean fetchCredits = false;
        boolean fetchInstructors = false;
        boolean fetchPreferences = false;
        boolean fetchAssignments = true;
        boolean fetchReservations = false;
        return(InstructionalOffering.search(sessionId, Long.valueOf(form.getSubjectAreaId()), form.getCourseNbr(), fetchStructure, fetchCredits, fetchInstructors, fetchPreferences, fetchAssignments, fetchReservations));
    }
	
	private boolean doSearch() throws Exception {
	    form.setCollections(sessionContext, getInstructionalOfferings());
	    return !form.getInstructionalOfferings().isEmpty();
	}
	
	public String printTable() throws Exception {
		if (form.getInstructionalOfferings() != null && form.getInstructionalOfferings().size() > 0){
			new WebColleagueCourseListTableBuilder().htmlTableForColleagueOfferings(
				sessionContext,
				getClassAssignmentService().getAssignment(),
		        form, 
		        Long.valueOf(form.getSubjectAreaId()),	
		        true, 
		        form.getCourseNbr()==null || form.getCourseNbr().length()==0,
		        getPageContext().getOut(),
		        request.getParameter("backType"),
		        request.getParameter("backId"));
		}
		return "";
	}
}
