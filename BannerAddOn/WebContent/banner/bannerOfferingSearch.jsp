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
<loc:bundle name="CourseMessages"><s:set var="msg" value="#attr.MSG"/>
<loc:bundle name="BannerMessages" id="BMSG"><s:set var="bmsg" value="#attr.BMSG"/>
<s:form action="bannerOfferingSearch">
<table class="unitime-Table">
	<TR>
		<TH valign="middle"><loc:message name="filterSubject"/></TH>
		<TD>
		<s:select name="form.subjectAreaId" id="subjectAreaIds"
			list="form.subjectAreas" listKey="uniqueId" listValue="subjectAreaAbbreviation"
			headerKey="" headerValue="%{#msg.itemSelect()}"/>
		</TD>
		<TH valign="middle"><loc:message name="filterCourseNumber"/></TH>
		<TD>
			<tt:course-number name="form.courseNbr" configuration="subjectId=\${subjectAreaIds};notOffered=include" size="10"/>
		</TD>
		<TD style="padding-left: 10px;">
			<s:submit name='doit' value="%{#bmsg.actionSearchBannerOfferings()}"
					title="%{#bmsg.titleSearchBannerOfferings(#bmsg.accessSearchBannerOfferings())}"
					accesskey="%{#bmsg.accessSearchBannerOfferings()}"/>
		</TD>
		<s:if test="!fieldErrors.isEmpty()">
			<TR><TD colspan="5" align="left" class="errorTable">
				<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
			</TD></TR>
		</s:if>
	</TR>
</table>
<s:if test="showTable == true">
	<s:property value="%{printTable()}" escapeHtml="false"/>
</s:if>
<s:if test="#request.hash != null">
	<SCRIPT type="text/javascript">
		location.hash = '<%=request.getAttribute("hash")%>';
	</SCRIPT>
</s:if>
</s:form>
</loc:bundle>
</loc:bundle>

