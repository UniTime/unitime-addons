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
 <%--
 * based on code submitted by Dagmar Murray
 --%>
<%@ page language="java" autoFlush="true"%>
<%@ page import="org.unitime.banner.form.BannerMessageResponsesForm"%>
<%@ page import="org.unitime.timetable.model.Roles" %>

<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<%
	// Get Form 
	String frmName = "bannerMessageResponsesForm";
	BannerMessageResponsesForm frm = (BannerMessageResponsesForm) request
			.getAttribute(frmName);

//	frm.initialize(request);	
%>

<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>
<tiles:importAttribute />
<html:form action="/bannerMessageResponses">
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
		<TD>CRN:</TD>
		<TD>
			<html:text property="crn" maxlength="5" size="8" />
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
		<TD>Cross-List ID:</TD>
		<TD>
			<html:text property="xlst" maxlength="2" size="8" />
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






















