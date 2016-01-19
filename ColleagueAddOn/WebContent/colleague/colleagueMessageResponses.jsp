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
 <%--
 * based on code submitted by Dagmar Murray
 --%>
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.colleague.form.ColleagueMessageResponsesForm"%>
<%@ page import="org.unitime.timetable.model.Roles" %>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%
	// Get Form 
	String frmName = "colleagueMessageResponsesForm";
	ColleagueMessageResponsesForm frm = (ColleagueMessageResponsesForm) request
			.getAttribute(frmName);

//	frm.initialize(request);	
%>

<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />
<html:form action="/colleagueMessageResponses">
	<script language="JavaScript">blToggleHeader('Filter','dispFilter');blStart('dispFilter');</script>
	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
	<TR>
		<TD>Subject Area:</TD>
		<TD>
			<html:select property="subjAreaId">
				<html:option value="">All Subjects</html:option>
				<html:options collection="subjAreas" labelProperty="subjectAreaAbbreviation" property="uniqueId" />
			</html:select>
		</TD>
	</TR>
	<TR>
		<TD>Course Number:</TD>
		<TD>
			<html:text property="courseNumber" maxlength="10" size="10" />
		</TD>
	</TR>
	<TR>
		<TD>Colleague Synonym:</TD>
		<TD>
			<html:text property="colleagueId" maxlength="5" size="8" />
		</TD>
	</TR>
	<TR>
		<TD>Action:</TD>
		<TD rowspan="2">
			<TABLE>
				<TR>
					<TD>
					<html:checkbox property="actionAudit"/>
					</TD>
					<TD>Audit</TD>
					<TD>
					<html:checkbox property="actionUpdate"/>
					</TD>
					<TD>Update</TD>
					<TD>
					<html:checkbox property="actionDelete"/>
					</TD>
					<TD>Delete</TD>
				</TR>
				<TR>
					<TD>
					<html:checkbox property="typeSuccess"/>
					</TD>
					<TD>Success</TD>
					<TD>
					<html:checkbox property="typeError"/>
					</TD>
					<TD>Error</TD>
					<TD>
					<html:checkbox property="typeWarning"/>
					</TD>
					<TD>Warning</TD>
				</TR>
				
			</TABLE>
		</TD>			
	</TR>
	<TR>
		<TD>Type:</TD>
	</TR>
	<TR>
		<TD>Message:</TD>
		<TD>
			<html:text property="message" maxlength="50" size="20" />
		</TD>
	</TR>
	<TR>
		<TD>Start Date:</TD>
		<TD>
			<html:text property="startDate" maxlength="10" size="8" />
		mm/dd/yyyy
		</TD>
	</TR>
	<TR>
		<TD>Stop Date:</TD>
		<TD>
			<html:text property="stopDate" maxlength="10" size="8" />
		mm/dd/yyyy
		</TD>
	</TR>
	<sec:authorize access="hasPermission(null, null, 'IsAdmin')">
	<TR>
		<TD>Manager:</TD>
		<TD>
			<html:select property="managerId">
				<html:option value="-1">All Managers</html:option>
				<html:options collection="managers" labelProperty="name" property="uniqueId" />
			</html:select>
		</TD>
	</TR>
	<TR>
		<TD>Department:</TD>
		<TD>
			<html:select property="departmentId">
				<html:option value="-1">All Departments</html:option>
				<html:options collection="departments" labelProperty="label" property="uniqueId" />
			</html:select>
		</TD>
	</TR>
	</sec:authorize>
	<TR>
		<TD>
		Show History:
		</TD>
		<TD>
		<html:checkbox property="showHistory"/>
		</TD>
	</TR>
	<TR>
		<TD>Number of Messages:</TD>
		<TD>
			<html:text property="n" maxlength="5" size="8" />
		</TD>
	</TR>
	<TR>
		<TD colspan='2' align='right'>
			<html:submit onclick="displayLoading();" property="op" value="Apply"/>
			<html:submit onclick="displayLoading();" property="op" value="Export PDF"/>
			<html:submit onclick="displayLoading();" property="op" value="Export CSV"/>
			<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
		</TD>
	</TR>
	</TABLE>
	<script language="JavaScript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
			<TR>
				<TD colspan='2' align='right'>
					<html:submit onclick="displayLoading();" property="op" value="Export PDF"/>
					<html:submit onclick="displayLoading();" property="op" value="Export CSV"/>
					<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh"/>
				</TD>
			</TR>
		</TABLE>
	<script language="JavaScript">blEndCollapsed('dispFilter');</script>

	<BR><BR>

	<TABLE width="90%" border="0" cellspacing="0" cellpadding="3">
		<%=request.getAttribute("table")%>
	</TABLE>
</html:form>






















