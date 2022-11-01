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
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<loc:bundle name="BannerMessages" id="BMSG"><s:set var="bmsg" value="#attr.BMSG"/>
<s:form action="bannerMessageResponses">
<script type="text/javascript" src="scripts/block.js"></script>
	<script type="text/javascript">blToggleHeader('<loc:message name="filter"/>','dispFilter');blStart('dispFilter');</script>
	<table class="unitime-MainTable">
	<TR>
		<TD><loc:message name="filterSubjectArea"/></TD>
		<TD>
			<s:select name="form.subjAreaId" id="subjectAreaId"
				list="#request.subjAreas" listKey="uniqueId" listValue="subjectAreaAbbreviation"
				headerKey="" headerValue="%{#msg.itemAllSubjects()}"/>
		</TD>
	</TR>
	<TR>
		<TD><loc:message name="filterCourseNumber"/></TD>
		<TD>
			<tt:course-number name="filter.courseNumber" configuration="subjectId=\${subjectAreaId};notOffered=include" size="10"/>
		</TD>
	</TR>
	<TR>
		<TD><loc:message name="colCRN" id="BMSG"/>:</TD>
		<TD>
			<s:textfield name="form.crn" maxlength="5" size="8" />
		</TD>
	</TR>
	<TR>
		<TD><loc:message name="colAction" id="BMSG"/>:</TD>
		<TD rowspan="2">
			<TABLE>
				<TR>
					<TD>
					<s:checkbox name="form.actionAudit"/>
					</TD>
					<TD><loc:message name="actionAudit" id="BMSG"/></TD>
					<TD>
					<s:checkbox name="form.actionUpdate"/>
					</TD>
					<TD><loc:message name="actionUpdate" id="BMSG"/></TD>
					<TD>
					<s:checkbox name="form.actionDelete"/>
					</TD>
					<TD><loc:message name="actionDelete" id="BMSG"/></TD>
				</TR>
				<TR>
					<TD>
					<s:checkbox name="form.typeSuccess"/>
					</TD>
					<TD><loc:message name="typeSuccess" id="BMSG"/></TD>
					<TD>
					<s:checkbox name="form.typeError"/>
					</TD>
					<TD><loc:message name="typeError" id="BMSG"/></TD>
					<TD>
					<s:checkbox name="form.typeWarning"/>
					</TD>
					<TD><loc:message name="typeWarning" id="BMSG"/></TD>
				</TR>
			</TABLE>
		</TD>			
	</TR>
	<TR>
		<TD><loc:message name="colType" id="BMSG"/>:</TD>
	</TR>
	<TR>
		<TD><loc:message name="colMessage" id="BMSG"/>:</TD>
		<TD>
			<s:textfield name="form.message" maxlength="50" size="20" />
		</TD>
	</TR>
	<TR>
		<TD><loc:message name="colCrossListId" id="BMSG"/>:</TD>
		<TD>
			<s:textfield name="form.xlst" maxlength="2" size="8" />
		</TD>
	</TR>
	<TR>
		<TD><loc:message name="colStartDate" id="BMSG"/>:</TD>
		<TD>
			<tt:calendar name="form.startDate" format="MM/dd/yyyy"/>
		</TD>
	</TR>
	<TR>
		<TD><loc:message name="colStopDate" id="BMSG"/>:</TD>
		<TD>
			<tt:calendar name="form.stopDate" format="MM/dd/yyyy"/>
		</TD>
	</TR>
	<sec:authorize access="hasPermission(null, null, 'IsAdmin')">
	<TR>
		<TD><loc:message name="columnTimetableManager"/>:</TD>
		<TD>
			<s:select name="form.managerId"
				list="#request.managers" listKey="uniqueId" listValue="name"
				headerKey="-1" headerValue="%{#msg.itemAllManagers()}"/>
		</TD>
	</TR>
	<TR>
		<TD><loc:message name="filterDepartment"/></TD>
		<TD>
			<s:select name="form.departmentId"
				list="#request.departments" listKey="uniqueId" listValue="label"
				headerKey="-1" headerValue="%{#msg.itemAllDepartments()}"/>
		</TD>
	</TR>
	</sec:authorize>
	<TR>
		<TD>
		<loc:message name="filterShowHistory" id="BMSG"/>
		</TD>
		<TD>
		<s:checkbox name="form.showHistory"/>
		</TD>
	</TR>
	<TR>
		<TD><loc:message name="filterNumberOfMessages" id="BMSG"/></TD>
		<TD>
			<s:textfield name="form.n" type="number" min="0" max="99999"/>
		</TD>
	</TR>
	<TR>
		<TD colspan='2' align='right'>
			<s:submit name="op" value="%{#msg.actionFilterApply()}"/>
			<s:submit name="op" value="%{#msg.actionExportPdf()}"/>
			<s:submit name="op" value="%{#msg.actionExportCsv()}"/>
			<s:submit name="op" value="%{#msg.actionRefreshLog()}"/>
		</TD>
	</TR>
	</TABLE>
	<script type="text/javascript">blEnd('dispFilter');blStartCollapsed('dispFilter');</script>
		<table class="unitime-MainTable">
			<TR>
				<TD colspan='2' align='right'>
					<s:submit name="op" value="%{#msg.actionExportPdf()}"/>
					<s:submit name="op" value="%{#msg.actionExportCsv()}"/>
					<s:submit name="op" value="%{#msg.actionRefreshLog()}"/>
				</TD>
			</TR>
		</TABLE>
	<script type="text/javascript">blEndCollapsed('dispFilter');</script>

	<BR><BR>

	<table class="unitime-MainTable">
		<s:property value="#request.table" escapeHtml="false"/>
	</table>
</s:form>
</loc:bundle>
</loc:bundle>






















