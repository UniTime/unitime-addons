<%--
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
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
--%>
<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ page import="org.unitime.banner.webutil.WebBannerCourseListTableBuilder"%>
<%@ page import="org.unitime.banner.form.BannerCourseListForm"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<html:form action="/bannerOfferingSearch">
<bean:define id="instructionalOfferings" name="bannerOfferingListForm" property="instructionalOfferings"></bean:define>
<tt:session-context/>
<%	
	String subjectAreaId = (request.getParameter("subjectAreaId")!=null)
							? request.getParameter("subjectAreaId")
							: (String) request.getAttribute("subjectAreaId");
	session.setAttribute("subjArea", subjectAreaId);
	session.setAttribute("callingPage", "bannerOfferingSearch");

	// Get Form 
	String frmName = "bannerOfferingListForm";
	BannerCourseListForm frm = (BannerCourseListForm) request.getAttribute(frmName);
	if (frm.getInstructionalOfferings() != null && frm.getInstructionalOfferings().size() > 0){
		new WebBannerCourseListTableBuilder()
				    		.htmlTableForBannerOfferings(
				    				sessionContext,
				    		        WebSolver.getClassAssignmentProxy(session),
				    		        frm, 
				    		        new Long(frm.getSubjectAreaId()),	
				    		        true, 
				    		        frm.getCourseNbr()==null || frm.getCourseNbr().length()==0,
				    		        out,
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
	}
%>
</html:form>
 
