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

import java.net.URLEncoder;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.banner.form.BannerCourseListForm;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.banner.webutil.WebBannerCourseListTableBuilder;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.BackTracker;



/**
 * @author Stephanie Schluttenhofer
 */

@Action(value="bannerOfferingSearch", results = {
		@Result(name = "showBannerOfferingList", type = "tiles", location = "bannerOfferingSearch.tiles"),
		@Result(name = "showBannerOfferingSearch", type = "tiles", location = "bannerOfferingSearch.tiles"),
		@Result(name = "showBannerOfferingDetail", type = "redirect", location = "/bannerOfferingDetail.action",
			params = {"op" , "view", "bc", "${bc}"}
		)
	})
@TilesDefinition(name = "bannerOfferingSearch.tiles", extend =  "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Banner Offerings"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerOfferingSearch.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "true"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
})
public class BannerCourseSearchAction extends UniTimeAction<BannerCourseListForm> {
	private static final long serialVersionUID = -1166103695560175698L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	private String doit;
	private Long bc;
	private boolean showTable = false;

	public Long getBc() { return bc; }
	public void setBc(Long bc) { this.bc = bc; }
	public String getDoit() { return doit; }
	public void setDoit(String doit) { this.doit = doit; }
	public boolean isShowTable() { return showTable; }
	public void setShowTable(boolean showTable) { this.showTable = showTable; }
	
	@Override
	public String execute() throws Exception {
		if (form == null) form = new BannerCourseListForm();
		sessionContext.checkPermission(Right.InstructionalOfferings);
		
		if (BMSG.actionSearchBannerOfferings().equals(doit) || "Search".equals(doit)) {
			return searchBannerOfferings();
		}
		
		BackTracker.markForBack(request, null, null, false, true); //clear back list
        
        // Check if subject area / course number saved to session
	    Object sa = sessionContext.getAttribute(SessionAttribute.OfferingsSubjectArea);
	    Object cn = sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
	    
	    if (Constants.ALL_OPTION_VALUE.equals(sa))
	    	sa = null;

	    if ((sa == null || sa.toString().trim().isEmpty()) && (cn == null || cn.toString().trim().isEmpty())) {
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
	        String subjectAreaId = sa.toString();
	        String courseNbr = "";
	        
	        try {
		        if (cn!=null && cn.toString().trim().length()>0)
		            courseNbr = cn.toString();
		        
		        Debug.debug("Subject Area: " + subjectAreaId);
		        Debug.debug("Course Number: " + courseNbr);
		        
		        form.setSubjectAreaId(subjectAreaId);
		        form.setCourseNbr(courseNbr);
		        
		        if(doSearch()) {
					BackTracker.markForBack(
							request, 
							"bannerOfferingSearch.action?doit=Search&form.subjectAreaId="+form.getSubjectAreaId()+"&form.courseNbr="+form.getCourseNbr(), 
							BMSG.sectBannerOfferings() + " ("+
								(form.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(Long.valueOf(form.getSubjectAreaId()))).getSubjectAreaAbbreviation():form.getSubjectAreaAbbv())+
								(form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr())+
								")", 
							true, true);
					setShowTable(true);
		            return "showBannerOfferingList";
		        	
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
	        if (s.size()==1) {
	            Debug.debug("Exactly 1 subject area found ... ");
	            form.setSubjectAreaId(((SubjectArea) s.iterator().next()).getUniqueId().toString());
		        if (doSearch()) {
					BackTracker.markForBack(
							request, 
							"bannerOfferingSearch.action?doit=Search&form.subjectAreaId="+form.getSubjectAreaId()+"&form.courseNbr="+form.getCourseNbr(), 
							BMSG.sectBannerOfferings() + " ("+
								(form.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(Long.valueOf(form.getSubjectAreaId()))).getSubjectAreaAbbreviation():form.getSubjectAreaAbbv())+
								(form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr())+
								")", 
							true, true);
					setShowTable(true);
		            return "showBannerOfferingList";
		        }
	        }
	    }
	    
	    setShowTable(false);
        return "showBannerOfferingSearch";
	}
	
	public String searchBannerOfferings() throws Exception {
		sessionContext.checkPermission(Right.InstructionalOfferings);
        
        // Check that a valid subject area is selected
	    form.validate(this);
	    
	    // Validation fails
	    if (hasFieldErrors()) {
		    form.setCollections(sessionContext, null);
		    setShowTable(false);
		    return "showBannerOfferingSearch";
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
		    return "showBannerOfferingSearch";
		} else {
			BackTracker.markForBack(
					request, 
					"bannerOfferingSearch.action?op=Back&doit=Search&form.subjectAreaId="+form.getSubjectAreaId()+"&form.courseNbr="+URLEncoder.encode(form.getCourseNbr(), "utf-8"), 
					BMSG.sectBannerOfferings() + " ("+
						(form.getSubjectAreaAbbv()==null?((new SubjectAreaDAO()).get(Long.valueOf(form.getSubjectAreaId()))).getSubjectAreaAbbreviation():form.getSubjectAreaAbbv())+
						(form.getCourseNbr()==null || form.getCourseNbr().length()==0?"":" "+form.getCourseNbr())+
						")", 
					true, true);

			if (request.getParameter("op")==null || 
		            (request.getParameter("op")!=null && !request.getParameter("op").equalsIgnoreCase("Back")) )  {
		        
			    // Search produces 1 result - redirect to banner offering detail
			    if(instructionalOfferings.size()==1 && instructionalOfferings.first().getCourseOfferings().size() == 1) {
			    	BannerCourse bc = BannerCourse.findBannerCourseForCourseOffering(((CourseOffering)instructionalOfferings.first().getCourseOfferings().iterator().next()).getUniqueId(), BannerCourseDAO.getInstance().getSession());
			    	if (bc != null){
			    		setBc(bc.getUniqueId());
			    		return "showBannerOfferingDetail";
			    	}
			    }
		    }
			setShowTable(true);
		    return "showBannerOfferingList";
		}
	}
	
	
	public TreeSet<InstructionalOffering> getInstructionalOfferings() {
		Long sessionId = sessionContext.getUser().getCurrentAcademicSessionId();
        boolean fetchStructure = true;
        boolean fetchCredits = false;
        boolean fetchInstructors = false;
        boolean fetchPreferences = false;
        boolean fetchAssignments = false;
        boolean fetchReservations = false;
        return(InstructionalOffering.search(sessionId, Long.valueOf(form.getSubjectAreaId()), form.getCourseNbr(), fetchStructure, fetchCredits, fetchInstructors, fetchPreferences, fetchAssignments, fetchReservations));
    }
	
	private boolean doSearch() throws Exception {
	    form.setCollections(sessionContext, getInstructionalOfferings());
		return !form.getInstructionalOfferings().isEmpty();
	}
	
	public String printTable() throws Exception {
		if (form.getInstructionalOfferings() != null && form.getInstructionalOfferings().size() > 0){
			new WebBannerCourseListTableBuilder().htmlTableForBannerOfferings(
					sessionContext,
					getClassAssignmentService().getAssignment(),
    		        form, 
    		        Long.valueOf(form.getSubjectAreaId()),	
    		        true, 
    		        form.getCourseNbr()==null || form.getCourseNbr().isEmpty(),
    		        getPageContext().getOut(),
    		        request.getParameter("backType"),
    		        request.getParameter("backId"));
		}
		return "";
	}
}
