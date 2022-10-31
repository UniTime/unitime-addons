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
<s:form action="bannerTermCrnPropertiesEdit">
<table class="unitime-MainTable">
	<s:hidden name="form.bannerTermPropertiesId" />	
	<TR>
		<TD colspan="2">
			<tt:section-header>
				<tt:section-title>
				</tt:section-title>
				<s:if test="form.campusOverrideId == null">
					<s:submit name="op" value="%{#bmsg.actionSaveBannerSession()}"/>
				</s:if><s:else>
					<s:submit name="op" value="%{#bmsg.actionUpdateBannerSession()}"/>
				</s:else>
				<s:submit name="op" value="%{#bmsg.actionBackToBannerSessions()}"/>
			</tt:section-header>			
		</TD>
	</TR>
		
	<s:if test="!fieldErrors.isEmpty()">
		<TR><TD colspan="2" align="left" class="errorTable">
			<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
		</TD></TR>
	</s:if>	

	<TR>
		<TD><loc:message name="colBannerTermCode" id="BMSG"/>:</TD>
		<TD>
			<s:if test="form.campusOverrideId == null">
				<s:select name="form.bannerTermCode" style="min-width:200px;"
					list="form.availableBannerTermCodes" listKey="bannerTermCode" listValue="bannerTermCode"/>
			</s:if><s:else>
				<s:hidden name="form.bannerTermCode"/>
				<s:property value="form.bannerTermCode"/>
			</s:else>
		</TD>
	</TR>
	<TR>
		<TD><loc:message name="colBannerSessions" id="BMSG"/>:</TD>
		<TD>
			<s:select name="form.bannerSessionIds" size="%{listSize}" multiple="true"
				list="form.availableBannerSessions" listKey="uniqueId" listValue="label"/>
		</TD>
	</TR>

	<TR>
		<TD><loc:message name="colLastCRN" id="BMSG"/>:</TD>
		<TD>
			<s:textfield name="form.lastCrn" type="number" min="0" max="9999999999"/>
		</TD>
	</TR>
		
	<TR>
		<TD><loc:message name="colMinimumCRN" id="BMSG"/>:</TD>
		<TD>
			<s:textfield name="form.minCrn" type="number" min="0" max="9999999999"/>
		</TD>
	</TR>

	<TR>
		<TD><loc:message name="colMaximumCRN" id="BMSG"/>:</TD>
		<TD>
			<s:textfield name="form.maxCrn" type="number" min="0" max="9999999999"/>
		</TD>
	</TR>
	<TR>
		<TD><loc:message name="colSearchFlag" id="BMSG"/>:</TD>
		<TD align="left">
			<s:checkbox name="form.searchFlag"/>
		</TD>
	</TR>

	<TR>
		<TD colspan="2">
			<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
		</TD>
	</TR>
		
	<TR>
		<TD colspan="2" align="right">
			<s:if test="form.campusOverrideId == null">
				<s:submit name="op" value="%{#bmsg.actionSaveBannerSession()}"/>
			</s:if><s:else>
				<s:submit name="op" value="%{#bmsg.actionUpdateBannerSession()}"/>
			</s:else>
			<s:submit name="op" value="%{#bmsg.actionBackToBannerSessions()}"/>
		</TD>
	</TR>
</table>
</s:form>
</loc:bundle>
</loc:bundle>