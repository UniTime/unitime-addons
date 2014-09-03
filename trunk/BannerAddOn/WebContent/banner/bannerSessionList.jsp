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
<%@ page import="java.text.DecimalFormat"%>
<%@ page import="java.text.DateFormat"%>
<%@ page import="org.unitime.commons.web.*"%>
<%@ taglib uri="http://struts.apache.org/tags-bean"	prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html"	prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic"	prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<html:form action="bannerSessionEdit">

	<table width="98%" border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td>
				<tt:section-header>
					<tt:section-title>
						
					</tt:section-title>
					<sec:authorize access="hasPermission(null, null, 'AcademicSessionAdd')">
					<html:submit property="doit" styleClass="btn" accesskey="A" titleKey="title.addSession">
						<bean:message key="button.addSession" />
					</html:submit>
					</sec:authorize>
				</tt:section-header>
			</td>
		</tr>
	</table>

	<table width="90%" border="0" cellspacing="0" cellpadding="3">
		<%
			WebTable webTable = new WebTable(
					7, "", "bannerSessionList.do?order=%%",					
					new String[] {
						"Academic<br>Session", "Academic<br>Initiative", "Banner<br>Term&nbsp;Code",
						"Banner<br>Campus", "Store&nbsp;Data<br>For&nbsp;Banner", "Send&nbsp;Data<br>To&nbsp;Banner", "Loading<br>Offerings" },
					new String[] { "left", "left", "left", "left",
						"center", "center", "center" }, 
					new boolean[] { true, true, false, false, false, true });
					
			webTable.enableHR("#EFEFEF");
	        webTable.setRowStyle("white-space: nowrap");
					
		%>

		<logic:iterate name="bannerSessionListForm" property="sessions" id="sessn">
			<%
					org.unitime.banner.model.BannerSession s = (org.unitime.banner.model.BannerSession) sessn;
					webTable
					.addLine(
							"onClick=\"document.location='bannerSessionEdit.do?doit=editSession&sessionId=" + s.getUniqueId() + "';\"",
							new String[] {
								s.getSession().getLabel() + "&nbsp;",
								s.getSession().academicInitiativeDisplayString() + "&nbsp;",
								s.getBannerTermCode() + "&nbsp;",
								s.getBannerCampus() + "&nbsp;",
								s.isStoreDataForBanner().booleanValue() ? "<img src='images/accept.png'> " : "&nbsp; ", 
								s.isSendDataToBanner().booleanValue() ? "<img src='images/accept.png'> " : "&nbsp; ", 
								s.isLoadingOfferingsFile().booleanValue() ? "<img src='images/accept.png'> " : "&nbsp; "},
							new Comparable[] {
								s.getSession().getLabel(),
								s.getSession().academicInitiativeDisplayString(),
								s.getBannerTermCode(),
								s.getBannerCampus(),
								s.isStoreDataForBanner().booleanValue() ? "<img src='images/accept.png'>" : "",
								s.isSendDataToBanner().booleanValue() ? "<img src='images/accept.png'>" : "",
								s.isLoadingOfferingsFile().booleanValue() ? "<img src='images/accept.png'>" : "" } );
			%>

		</logic:iterate>
		<%-- end interate --%>
		<%
		int orderCol = 3;
		if (request.getParameter("order")!=null) {
			try {
				orderCol = Integer.parseInt(request.getParameter("order"));
			}
			catch (Exception e){
				orderCol = 3;
			}
		}
		out.println(webTable.printTable(orderCol));
		%>

		<%-- print out the add link --%>

	</table>
	
	<table width="98%" border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td align="center" class="WelcomeRowHead">
			&nbsp;
			</td>
		</tr>
		<tr>
			<td align="right">
				<sec:authorize access="hasPermission(null, null, 'AcademicSessionAdd')">
					<html:submit property="doit" styleClass="btn" accesskey="A" titleKey="title.addSession">
						<bean:message key="button.addSession" />
					</html:submit>
				</sec:authorize>
			</td>
		</tr>
	</table>

</html:form>
	
