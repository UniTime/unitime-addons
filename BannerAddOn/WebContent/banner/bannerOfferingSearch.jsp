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

