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
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld"	prefix="bean"%>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld"	prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld"	prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<html:form action="bannerTermCrnPropertiesEdit">

	<table width="98%" border="0" cellspacing="0" cellpadding="3">
		<tr>
			<td>
				<tt:section-header>
					<tt:section-title>
						
					</tt:section-title>
					<html:submit property="doit" styleClass="btn" accesskey="A" titleKey="title.addSession">
						<bean:message key="button.addSession" />
					</html:submit>
				</tt:section-header>
			</td>
		</tr>
	</table>

	<table width="90%" border="0" cellspacing="0" cellpadding="3">
		<%
			WebTable webTable = new WebTable(
					5, "", "bannerTermCrnPropertiesList.do?order=%%",					
					new String[] {
						"Banner<br>Term&nbsp;Code", "Last&nbsp;CRN", "Minimum<br>CRN",
						"Maximum<br>CRN", "Search<br>Flag" },
					new String[] { "left", "left", "left", "left",
						"center" }, 
					new boolean[] { true, true, true, true, false });
					
			webTable.enableHR("#EFEFEF");
	        webTable.setRowStyle("white-space: nowrap");
					
		%>

		<logic:iterate name="bannerTermCrnPropertiesListForm" property="sessions" id="sessn">
			<%
					org.unitime.banner.model.BannerTermCrnProperties s = (org.unitime.banner.model.BannerTermCrnProperties) sessn;
					webTable
					.addLine(
							"onClick=\"document.location='bannerTermCrnPropertiesEdit.do?doit=editSession&bannerTermCrnPropertiesId=" + s.getUniqueId() + "';\"",
							new String[] {
								s.getBannerTermCode() + "&nbsp;",
								s.getLastCrn().toString() + "&nbsp;",
								s.getMinCrn().toString() + "&nbsp;",
								s.getMaxCrn().toString() + "&nbsp;",
								s.isSearchFlag().booleanValue() ? "<img src='images/tick.gif'> " : "&nbsp; "},
							new Comparable[] {
								s.getBannerTermCode(),
								s.getLastCrn().toString(),
								s.getMinCrn(),
								s.getMaxCrn(),
								s.isSearchFlag().booleanValue() ? "<img src='images/tick.gif'>" : "" } );
			%>

		</logic:iterate>
		<%-- end interate --%>
		<%
		int orderCol = 1;
		if (request.getParameter("order")!=null) {
			try {
				orderCol = Integer.parseInt(request.getParameter("order"));
			}
			catch (Exception e){
				orderCol = 1;
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
				<html:submit property="doit" styleClass="btn" accesskey="A" titleKey="title.addSession">
					<bean:message key="button.addSession" />
				</html:submit>
			</td>
		</tr>
	</table>

</html:form>
	
