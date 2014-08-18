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
<%@ page language="java" pageEncoding="ISO-8859-1"%>
<%@ page import="org.unitime.banner.form.RollForwardBannerSessionForm"%>
<%@ taglib uri="http://struts.apache.org/tags-bean"	prefix="bean"%>
<%@ taglib uri="http://struts.apache.org/tags-html"	prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
 
<html> 
	<head>
		<title>Roll Forward Session</title>
	</head>
	<body>
<script language="javascript">displayLoading();</script>
	<%// Get Form 
			String frmName = "rollForwardBannerSessionForm";
			RollForwardBannerSessionForm frm = (RollForwardBannerSessionForm) request
					.getAttribute(frmName);
%>
		<html:form action="/rollForwardBannerSession">
		<TABLE border="0" cellspacing="5" cellpadding="5">
		<logic:messagesPresent>
		<TR>
			<TD align="left" class="errorCell">
					<B><U>ERRORS</U></B><BR>
				<BLOCKQUOTE>
				<UL>
				    <html:messages id="error">
				      <LI>
						${error}
				      </LI>
				    </html:messages>
			    </UL>
			    </BLOCKQUOTE>
			</TD>
		</TR>
		</logic:messagesPresent>
	<logic:notEmpty name="table" scope="request">
		<TR><TD>
			<tt:section-header>
				<tt:section-title>Roll Forward(s) In Progress</tt:section-title>
				<html:submit property="op" accesskey="R" styleClass="btn" onclick="displayElement('loading', true);">Refresh</html:submit>
			</tt:section-header>
		</TD></TR>
		<TR><TD>
			<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
				<bean:write name="table" scope="request" filter="false"/>
			</TABLE>
		</TD></TR>
		<TR><TD>&nbsp;</TD></TR>
	</logic:notEmpty>
			
	<TR><TD>
		<tt:section-header>
			<tt:section-title>Roll Forward Actions</tt:section-title>
					<html:submit property="op" accesskey="M" styleClass="btn" onclick="displayElement('loading', true);">
					<bean:message key="button.rollForward" />
				</html:submit>
		</tt:section-header>
	</TD></TR>
		
		<tr>
			<td valign="top" nowrap ><b>Session To Roll Foward To: </b>
			<html:select style="width:200;" property="sessionToRollForwardTo" onchange="displayElement('loading', true);submit();">
			<html:optionsCollection property="toSessions" value="uniqueId" label="label"  /></html:select>
			</td>			
		</tr>
		<tr>
		<td>&nbsp;
		</td>
		</tr>
		<tr>
			<td valign="top" nowrap ><html:checkbox name="<%=frmName%>" property="rollForwardBannerSession"/> Roll Banner Session Data Forward From Session: 
			<html:select style="width:200;" property="sessionToRollBannerDataForwardFrom">
			<html:optionsCollection property="fromSessions" value="uniqueId" label="label" /></html:select>
			</td>			
		</tr>
		<tr><td>&nbsp;<br>&nbsp;<br></td></tr>
				<tr>
			<td align="right">
					<html:submit property="op" accesskey="M" styleClass="btn" onclick="displayElement('loading', true);">
						<bean:message key="button.rollForward" />
					</html:submit>
			</TD>
		</TR>
		</TABLE>
		</html:form>
	<script language="javascript">displayElement('loading', false);</script>
	</body>
</html>

