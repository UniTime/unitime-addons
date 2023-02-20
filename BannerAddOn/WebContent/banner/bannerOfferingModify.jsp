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
<s:form name="bannerOfferingModify">
	<s:hidden name="form.bannerCourseOfferingId"/>	
	<s:hidden name="form.bannerConfigId"/>	
	<s:hidden name="form.instrOfferingId"/>	
	<s:hidden name="form.instrOfferingName"/>	
	<s:hidden name="form.instrOffrConfigId"/>	
	<s:hidden name="form.configIsEditable"/>
	<s:hidden name="form.showLimitOverride"/>
	<table class="unitime-MainTable">
<!-- Buttons -->
	<TR>
		<TD valign="middle" colspan="2">
			 <tt:section-header>
				<tt:section-title>
					<s:property value="form.instrOfferingName" />
				</tt:section-title>
				<s:submit name='op' value='%{#bmsg.actionUpdateBannerConfig()}'
					accesskey='%{#bmsg.accessUpdateBannerConfig()}' title='%{#bmsg.titleUpdateBannerConfig(#bmsg.accessUpdateBannerConfig())}'/>
				<s:submit name='op' value='%{#bmsg.actionBackToBannerOfferingDetail()}'
					accesskey='%{#bmsg.accessBackToBannerOfferingDetail()}' title='%{#bmsg.titleBackToBannerOfferingDetail(#bmsg.accessBackToBannerOfferingDetail())}'/>
			</tt:section-header>					
		</TD>			
	</TR>

	<s:if test="!fieldErrors.isEmpty()">
		<TR><TD colspan="2" align="left" class="errorTable">
			<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
		</TD></TR>
	</s:if>	

	<TR>
		<TD align="left" colspan="2">
			<loc:message name="propConfigGradableItype" id="BMSG"/>
			<s:if test="form.configIsEditable == true">
				<s:select name="form.itypeId" style="min-width:200px;"
					list="#request.availableItypes" listKey="itype" listValue="desc"
					headerKey="-1" headerValue="%{#bmsg.itemNoItype()}"/>
			</s:if><s:else>
				<s:iterator value="#request.availableItypes" var="ityp">
					<s:if test="form.itypeId == itype.itype"><s:property value="#ityp.desc"/></s:if>
					<s:hidden name="form.itypeId"/>
				</s:iterator>
			</s:else>
			
			<s:if test="form.showLabHours == true">
				&nbsp;&nbsp;&nbsp;&nbsp;<loc:message name="propLabHours" id="BMSG"/>
				<s:if test="form.configIsEditable == true">
					<s:textfield name="form.labHours" maxlength="10" size="10"/>
				</s:if><s:else>
					<s:property value="form.labHours"/>
					<s:hidden name="form.labHours"/>
				</s:else>
			</s:if>			
		</TD>
	</TR>
	<TR>
		<TD colspan="2" align="left">
			<table class='unitime-Table'>
					<TR>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="fieldIType"/></TD>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="colSectionId" id="BMSG"/></TD>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnConsent"/></TD>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="colCourseCreditOverride" id="BMSG"/></TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnCredit"/></TD>
						<s:if test="form.showLimitOverride == true">
							<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
							<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="colLimitOverride" id="BMSG"/></TD>
							<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnLimit"/></TD>
						</s:if>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="colCampusOverride" id="BMSG"/></TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="center" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnDatePattern"/></TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="center" valign="bottom" rowSpan="1" colspan="3"class='WebTableHeaderFirstRow'>---- <loc:message name="columnTimetable"/> ----</TD>
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="left" valign="bottom" rowSpan="2" class='WebTableHeader'><loc:message name="columnInstructors"/></TD>
					</TR>
					<TR>
						<td align="left" valign="bottom" class='WebTableHeaderSecondRow'><loc:message name="columnAssignedTime"/></td>
						<TD class='WebTableHeaderSecondRow'>&nbsp;</TD>
						<td align="left" valign="bottom" class='WebTableHeaderSecondRow'><loc:message name="columnAssignedRoom"/></td>						
					</TR>
					<s:iterator value="form.bannerSectionIds" var="c" status="stat"><s:set var="ctr" value="#stat.index"/>
					<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
						<TD valign="top" nowrap>
							<s:if test="form.classHasErrors[#ctr] == true"><IMG src="images/cancel.png"></s:if><s:else>&nbsp;</s:else>
						</TD>
						<TD valign="top" nowrap>
							<s:hidden name="form.bannerSectionIds[%{#ctr}]"/>
							<s:hidden name="form.bannerSectionOriginalSectionIds[%{#ctr}]"/>
							<s:hidden name="form.itypes[%{#ctr}]"/>
							<s:hidden name="form.readOnlyClasses[%{#ctr}]"/>
							<s:hidden name="form.bannerSectionLabels[%{#ctr}]"/>
							<s:hidden name="form.bannerSectionLabelIndents[%{#ctr}]"/>
							<s:hidden name="form.datePatterns[%{#ctr}]"/>
							<s:hidden name="form.times[%{#ctr}]"/>
							<s:hidden name="form.rooms[%{#ctr}]"/>
							<s:hidden name="form.instructors[%{#ctr}]"/>
							<s:hidden name="form.classHasErrors[%{#ctr}]"/>
							<s:property value="form.bannerSectionLabelIndents[#ctr]" escapeHtml="false"/><s:property value="form.bannerSectionLabels[#ctr]"/>
						</TD>
						<TD align="left" valign="top" nowrap><s:property value="form.itypes[#ctr]"/></TD>
						<TD align="left" valign="top" nowrap>
							<s:if test="form.readOnlyClasses[#ctr] == true">
								<s:property value="form.bannerSectionSectionIds[#ctr]"/>
								<s:hidden name="form.bannerSectionSectionIds[%{#ctr}]"/>
							</s:if><s:else>
								<s:textfield name="form.bannerSectionSectionIds[%{#ctr}]" maxlength="5" size="5" tabindex="%{#ctr + 1000}"/>
							</s:else>
						</TD>
						<TD align="left" valign="top" nowrap>
							<s:if test="form.readOnlyClasses[#ctr] == true">
								<s:iterator value="#request.consentTypeList" var="cnst">
									<s:if test="#cnst.uniqueId == form.consents[#ctr]">
										<s:property value="#cnst.label"/>
									</s:if>
								</s:iterator>
								<s:hidden name="form.consents[%{#ctr}]"/>
							</s:if><s:else>
								<s:select name="form.consents[%{#ctr}]" tabindex="%{#ctr + 2000}" style="min-width:200px;"
									list="#request.consentTypeList" listKey="uniqueId" listValue="label"
									headerKey="-1" headerValue="%{#msg.noConsentRequired()}"/>
							</s:else>
						</TD>
						<TD align="left" valign="top" nowrap>
							<s:if test="form.readOnlyClasses[#ctr] == true">
								<s:property value="form.courseCreditOverrides[#ctr]"/>
								<s:hidden name="form.courseCreditOverrides[%{#ctr}]"/>
							</s:if><s:else>
								<s:textfield name="form.courseCreditOverrides[%{#ctr}]" maxlength="10" size="10" tabindex="%{#ctr + 3000}"/>
							</s:else>
						</TD>
						<TD>&nbsp;</TD>
						<TD align="right" valign="top" nowrap>
							<s:property value="form.courseCredits[#ctr]"/>
							<s:hidden name="form.courseCredits[%{#ctr}]"/>
						</TD>
						<s:if test="form.showLimitOverride == true">
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap>
							<s:if test="form.readOnlyClasses[#ctr] == true">
								<s:property value="form.limitOverrides[#ctr]"/>
								<s:hidden name="form.limitOverrides[%{#ctr}]"/>
							</s:if><s:else>
								<s:textfield name="form.limitOverrides[%{#ctr}]" maxlength="5" size="5" tabindex="%{#ctr + 4000}"/>
							</s:else>
						</TD>	
						<TD align="right" valign="top" nowrap>
							<s:property value="form.classLimits[#ctr]"/>
							<s:hidden name="form.classLimits[%{#ctr}]"/>
						</s:if>
 						<TD align="left" valign="top" nowrap>
 							<s:hidden name="form.defaultCampus[%{#ctr}]"/>
							<s:hidden name="form.campuses[%{#ctr}]"/>
 							<s:if test="form.readOnlyClasses[#ctr] == true">
								<s:property value="form.campuses[#ctr]"/>
								<s:hidden name="form.campusOverrides[%{#ctr}]"/>
							</s:if><s:else>
								<s:select name="form.campusOverrides[%{#ctr}]" tabindex="%{#ctr + 5000}" style="min-width:200px;"
									list="form.bannerCampusOverrideOptions[#ctr]" listKey="id" listValue="value"/>
							</s:else>
 						</TD>
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap><s:property value="form.datePatterns[#ctr]" escapeHtml="false"/></TD>
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap><s:property value="form.times[#ctr]" escapeHtml="false"/></TD>
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap><s:property value="form.rooms[#ctr]" escapeHtml="false"/></TD>
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap><s:property value="form.instructors[#ctr]" escapeHtml="false"/></TD>						
					</TR>
					</s:iterator>
				</TABLE>
			</TD>
		</TR>

		
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<s:submit name='op' value='%{#bmsg.actionUpdateBannerConfig()}'
					accesskey='%{#bmsg.accessUpdateBannerConfig()}' title='%{#bmsg.titleUpdateBannerConfig(#bmsg.accessUpdateBannerConfig())}'/>
				<s:submit name='op' value='%{#bmsg.actionBackToBannerOfferingDetail()}'
					accesskey='%{#bmsg.accessBackToBannerOfferingDetail()}' title='%{#bmsg.titleBackToBannerOfferingDetail(#bmsg.accessBackToBannerOfferingDetail())}'/>
			</TD>
		</TR>
</table>
</s:form>
</loc:bundle>
</loc:bundle>
