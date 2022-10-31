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
<s:form action="bannerCampusOverrideEdit">
<table class="unitime-MainTable">
	<s:hidden name="form.campusOverrideId" />	

	<TR>
		<TD colspan="3">
			<tt:section-header>
				<tt:section-title>
				</tt:section-title>
				<s:if test="form.campusOverrideId == null">
					<s:submit name="op" value="%{#bmsg.actionSaveCampusOverride()}"/>
				</s:if><s:else>
					<s:submit name="op" value="%{#bmsg.actionUpdateCampusOverride()}"/>
				</s:else>
				<s:submit name="op" value="%{#bmsg.actionBackToCampusOverrides()}"/>
			</tt:section-header>			
		</TD>
	</TR>

	<s:if test="!fieldErrors.isEmpty()">
		<TR><TD colspan="2" align="left" class="errorTable">
			<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
		</TD></TR>
	</s:if>	

	<TR>
		<TD><loc:message name="colBannerCampusCode" id="BMSG"/>:</TD>
		<TD>
			<s:textfield name="form.bannerCampusCode" maxlength="20" size="20"/>
		</TD>
	</TR>
		
	<TR>
		<TD><loc:message name="colBannerCampusName" id="BMSG"/>:</TD>
		<TD>
			<s:textfield name="form.bannerCampusName"  maxlength="20" size="20"/>
		</TD>
	</TR>

	<TR>
		<TD><loc:message name="colBannerCampusVisible" id="BMSG"/>:</TD>
		<TD align="left">
			<s:checkbox name="form.visible"/>
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
				<s:submit name="op" value="%{#bmsg.actionSaveCampusOverride()}"/>
			</s:if><s:else>
				<s:submit name="op" value="%{#bmsg.actionUpdateCampusOverride()}"/>
			</s:else>
			<s:submit name="op" value="%{#bmsg.actionBackToCampusOverrides()}"/>
		</TD>
	</TR>
</table>
</s:form>
</loc:bundle>
</loc:bundle>