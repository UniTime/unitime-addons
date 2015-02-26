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
				<%--
				<html:submit property="op" accesskey="R" styleClass="btn" onclick="displayElement('loading', true);">Refresh</html:submit>
				--%>
			</tt:section-header>
		</TD></TR>
		<TR><TD>
			<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
				<bean:write name="table" scope="request" filter="false"/>
			</TABLE>
		</TD></TR>
		<TR><TD>&nbsp;</TD></TR>
	</logic:notEmpty>
	<logic:notEmpty name="log" scope="request">
		<TR>
			<TD colspan='2'>
				<tt:section-header>
					<tt:section-title>
						Log of <bean:write name="logname" scope="request" filter="false"/>
					</tt:section-title>
					<bean:define id="logid" name="logid" scope="request"/>
					<input type="hidden" name="log" value="<%=logid%>">
					<html:submit onclick="displayLoading();" accesskey="R" property="op" value="Refresh" title="Refresh Log (Alt+R)"/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
  			<TD colspan='2'>
  				<blockquote>
	  				<bean:write name="log" scope="request" filter="false"/>
  				</blockquote>
  			</TD>
		</TR>
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

