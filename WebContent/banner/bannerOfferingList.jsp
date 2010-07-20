<%--
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
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
--%>
<%@ page language="java"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ page import="org.unitime.banner.webutil.WebBannerCourseListTableBuilder"%>
<%@ page import="org.unitime.banner.form.BannerCourseListForm"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<%@ page import="org.unitime.commons.web.Web"%>
<html:form action="/bannerOfferingSearch">
<bean:define id="instructionalOfferings" name="bannerOfferingListForm" property="instructionalOfferings"></bean:define>
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
				    				session,
				    		        WebSolver.getClassAssignmentProxy(session),
				    		        frm, 
				    		        new Long(frm.getSubjectAreaId()), 
				    		        Web.getUser(session),	
				    		        true, 
				    		        frm.getCourseNbr()==null || frm.getCourseNbr().length()==0,
				    		        out,
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
	}
%>
</html:form>
 
