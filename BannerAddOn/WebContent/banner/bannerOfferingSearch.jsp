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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.banner.webutil.WebBannerCourseListTableBuilder" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<script language="JavaScript" type="text/javascript" src="scripts/block.js"></script>

<tiles:importAttribute />
<html:form action="/bannerOfferingSearch">
<loc:bundle name="BannerMessages">
	<html:hidden property="doit" value="Search"/>
	<TABLE border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TH valign="top"><loc:message name="filterSubject"/></TH>
			<TD valign="top">
				<html:select name="bannerOfferingListForm" property="subjectAreaId" styleId="subjectAreaIds">
					<html:option value="<%=Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
					<html:optionsCollection property="subjectAreas"	label="subjectAreaAbbreviation" value="uniqueId" />
				</html:select>
			</TD>
			<TH valign="top"><loc:message name="filterCourseNumber"/></TH>
			<TD valign="top">
				<tt:course-number property="courseNbr" configuration="subjectId=\${subjectAreaIds};notOffered=include" size="10"
					title="Course numbers can be specified using wildcard (*). E.g. 2*"/>
			</TD>
			<TD valign="top">
				&nbsp;&nbsp;&nbsp;
				<html:submit
					accesskey="S" styleClass="btn" titleKey="title.search"
					onclick="doit.value=this.value;displayLoading();">
					<loc:message name="actionSearchBannerOfferings"/>
				</html:submit> 
		<!--  	
				<html:submit
					accesskey="P" styleClass="btn" titleKey="title.exportPDF"
					onclick="doit.value=this.value;displayLoading();">
					<bean:message key="button.exportPDF" />
				</html:submit> 
		-->	
			</TD>
		</TR>
		<TR>
			<TD colspan="5" align="center">
				<html:errors />
			</TD>
		</TR>
	</TABLE>
</loc:bundle>
</html:form>


<logic:notEmpty name="body2">
	<script language="javascript">displayLoading();</script>
	<tiles:insert attribute="body2" />
	<script language="javascript">displayElement('loading', false);</script>
</logic:notEmpty>

