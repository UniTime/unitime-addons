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
<s:form action="rollForwardBannerSession">
<table class="unitime-MainTable">
	<s:if test="!fieldErrors.isEmpty()">
		<TR><TD align="left" class="errorTable">
			<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror escape="false"/>
		</TD></TR>
	</s:if>
		
		
	<s:if test="#request.table != null">
		<TR><TD>
			<tt:section-header>
				<tt:section-title><loc:message name="sectRollForwardsInProgress"/></tt:section-title>
			</tt:section-header>
		</TD></TR>
		<TR><TD>
			<table class='unitime-Table' style="width:100%;">
				<s:property value="#request.table" escapeHtml="false"/>
			</table>
		</TD></TR>
		<TR><TD>&nbsp;</TD></TR>
	</s:if>
	
	<s:hidden name="log" value="%{#request.logid}" id="log"/>
	<s:if test="#request.log != null">
		<TR>
			<TD>
				<tt:section-header>
					<tt:section-title>
						<loc:message name="sectionRollForwardLog"><s:property value="#request.logname"/></loc:message>
					</tt:section-title>
					<s:submit name="op" value="%{#msg.actionRefreshLog()}"
						accesskey="%{#msg.accessRefreshLog()}" title="%{#msg.titleRefreshLog(#msg.accessRefreshLog())}"/>
				</tt:section-header>
			</TD>
		</TR>
		<TR>
  			<TD>
  				<blockquote>
  					<s:property value="#request.log" escapeHtml="false"/>
  				</blockquote>
  			</TD>
		</TR>
	</s:if>

	<TR><TD>
		<tt:section-header>
			<tt:section-title><loc:message name="sectRollForwardActions"/></tt:section-title>
				<s:submit name="op" value="%{#msg.actionRollForward()}"
						accesskey="%{#msg.accessRollForward()}" title="%{#msg.titleRollForward(#msg.accessRollForward())}"/>
		</tt:section-header>
	</TD></TR>
		<tr>
			<td valign="middle" nowrap ><b><loc:message name="propSessionToRollForwardTo"/></b>
			<s:select name="form.sessionToRollForwardTo" style="min-width:200px;" onchange="document.getElementById('log').value = '';submit();"
				list="form.toSessions" listKey="uniqueId" listValue="label"/>
		</tr>
		<tr><td>&nbsp;</td></tr>
		
		<tr>
			<td valign="top" nowrap ><s:checkbox name="form.rollForwardBannerSession"/> <loc:message name="propRollBannerSessionDataFrom" id="BMSG"/>
			<s:select name="form.sessionToRollBannerDataForwardFrom" style="min-width:200px;"
				list="form.fromSessions" listKey="uniqueId" listValue="label"/> 
			</td>
		</tr>
		<tr>
			<td valign="top" nowrap ><s:checkbox name="form.createMissingBannerSections"/> <loc:message name="checkCreateMissingBannerSections" id="BMSG"/>
			</td>
		</tr>
		<tr>
			<td class="WelcomeRowHead">
			&nbsp;
			</td>
		</tr>
		<tr>
			<td align="right" colspan="2">
				<s:submit name="op" value="%{#msg.actionRollForward()}"
					accesskey="%{#msg.accessRollForward()}" title="%{#msg.titleRollForward(#msg.accessRollForward())}"/>
			</TD>
		</TR>
	</table></s:form>
</loc:bundle>
</loc:bundle>
