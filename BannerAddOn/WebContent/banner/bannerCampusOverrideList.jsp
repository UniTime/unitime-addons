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
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tt" uri="http://www.unitime.org/tags-custom" %>
<%@ taglib prefix="loc" uri="http://www.unitime.org/tags-localization" %>
<%@ taglib prefix="sec" uri="http://www.springframework.org/security/tags" %>
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<loc:bundle name="BannerMessages" id="BMSG"><s:set var="bmsg" value="#attr.BMSG"/>
<s:form action="bannerCampusOverrideList">
<table class="unitime-MainTable">
	<tr>
		<td colspan="1">
			<tt:section-header>
				<tt:section-title>
				</tt:section-title>
				<sec:authorize access="hasPermission(null, null, 'AcademicSessionAdd')">
					<s:submit name="op" value="%{#bmsg.actionAddCampusOverride()}"/>
				</sec:authorize>
			</tt:section-header>
		</td>
	</tr>
	<s:property value="table" escapeHtml="false"/>
	<tr>
		<td align="center" class="WelcomeRowHead" colspan="3">&nbsp;</td>
	</tr>
	<tr>
		<td align="right">
			<sec:authorize access="hasPermission(null, null, 'AcademicSessionAdd')">
				<s:submit name="op" value="%{#bmsg.actionAddCampusOverride()}"/>
			</sec:authorize>
		</td>
	</tr>
</table>
</s:form>
</loc:bundle>
</loc:bundle>
