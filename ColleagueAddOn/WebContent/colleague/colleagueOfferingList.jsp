<%--
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
--%>
<%@ page language="java"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ page import="org.unitime.colleague.webutil.WebColleagueCourseListTableBuilder"%>
<%@ page import="org.unitime.colleague.form.ColleagueCourseListForm"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<html:form action="/colleagueOfferingSearch">
<bean:define id="instructionalOfferings" name="colleagueOfferingListForm" property="instructionalOfferings"></bean:define>
<tt:session-context/>
<%	
	String subjectAreaId = (request.getParameter("subjectAreaId")!=null)
							? request.getParameter("subjectAreaId")
							: (String) request.getAttribute("subjectAreaId");
	session.setAttribute("subjArea", subjectAreaId);
	session.setAttribute("callingPage", "colleagueOfferingSearch");

	// Get Form 
	String frmName = "colleagueOfferingListForm";
	ColleagueCourseListForm frm = (ColleagueCourseListForm) request.getAttribute(frmName);
	if (frm.getInstructionalOfferings() != null && frm.getInstructionalOfferings().size() > 0){
		new WebColleagueCourseListTableBuilder()
				    		.htmlTableForColleagueOfferings(
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
 
