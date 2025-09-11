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
<loc:bundle name="ColleagueMessages" id="CMSG"><s:set var="cmsg" value="#attr.CMSG"/>
<table class="unitime-Table">
	<s:form action="colleagueOfferingDetail">
		<s:hidden name="form.courseOfferingId"/>	
		<s:hidden name="form.instrOfferingId"/>	
		<s:hidden name="form.nextId"/>
		<s:hidden name="form.previousId"/>
		<TR>
			<TD valign="middle" colspan='2'>
				<tt:section-header>
					<tt:section-title>
						<A title="${CMSG.titleBackToColleagueOfferings(CMSG.accessBackToColleagueOfferings())}"
						accesskey="${CMSG.accessBackToColleagueOfferings()}" class="l7"
						href="colleagueOfferingSearch.action?op=Back&doit=Search&form.subjectAreaId=${form.subjectAreaId}&form.courseNbr=${csrNbr}#A${form.instrOfferingId}"
						><s:property value="form.instrOfferingName"/></A> 
					</tt:section-title>
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingCanLock')">
						<s:submit name='op' value='%{#msg.actionLockIO()}'
							accesskey='%{#msg.accessLockIO()}' title='%{#msg.titleLockIO(#msg.accessLockIO())}'
							onclick='%{#msg.jsSubmitLockIO(form.instrOfferingName)}'/>
					</sec:authorize>
					 <sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingCanUnlock')">
					 	<s:submit name='op' value='%{#msg.actionUnlockIO()}'
							accesskey='%{#msg.accessUnlockIO()}' title='%{#msg.titleUnlockIO(#msg.accessUnlockIO())}'
							onclick='%{#msg.jsSubmitUnlockIO(form.instrOfferingName)}'/>
					</sec:authorize>
	
					<s:submit name='op' value='%{#cmsg.actionResendToColleague()}'/>
					
					<s:if test="form.previousId != null">
						<s:submit name='op' value='%{#msg.actionPreviousIO()}'
							accesskey='%{#msg.accessPreviousIO()}' title='%{#msg.titlePreviousIO(#msg.accessPreviousIO())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit name='op' value='%{#msg.actionNextIO()}'
							accesskey='%{#msg.accessNextIO()}' title='%{#msg.titleNextIO(#msg.accessNextIO())}'/>
					</s:if>
					
					<tt:back styleClass="btn" 
							name="${CMSG.actionBackToColleagueOfferings()}" 
							title="${CMSG.titleBackToColleagueOfferings(BMSG.accessBackToColleagueOfferings())}" 
							accesskey="${CMSG.accessBackToColleagueOfferings()}" 
							type="InstructionalOffering">
						<s:property value="form.instrOfferingId"/>
					</tt:back>
				</tt:section-header>					
			</TD>
		</TR>		

	<s:if test="!fieldErrors.isEmpty()">
		<TR><TD colspan="2" align="left" class="errorTable">
			<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
		</TD></TR>
	</s:if>	

	<TR>
		<TD width="20%" valign="top"><loc:message name="propertyCourseOfferings"/></TD>
			<TD>
				<div class='unitime-ScrollTableCell'>
				<TABLE style="border-spacing:0px; width: 100%;">
					<TR>
						<TD align="center" class="WebTableHeader">&nbsp;</TD>
						<s:if test="form.hasCourseTypes == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnCourseType"/></TD>
						</s:if>
						<TD align="left" class="WebTableHeader"><loc:message name="columnTitle"/></TD>
						<s:if test="form.hasCourseExternalId == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnExternalId"/></TD>
						</s:if>
						<s:if test="form.hasCourseReservation == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnReserved"/></TD>
						</s:if>
						<s:if test="form.hasCredit == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnCredit"/></TD>
						</s:if>
						<s:if test="form.hasScheduleBookNote == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnScheduleOfClassesNote"/></TD>
						</s:if>
						<s:if test="form.hasDemandOfferings == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnDemandsFrom"/></TD>
						</s:if>
						<s:if test="form.hasAlternativeCourse == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnAlternativeCourse"/></TD>
						</s:if>
						<s:if test="form.hasParentCourse == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnParentCourse"/></TD>
						</s:if>
						<TD align="left" class="WebTableHeader"><loc:message name="columnConsent"/></TD>
						<s:if test="form.hasDisabledOverrides == true">
							<TD align="left" class="WebTableHeader"><loc:message name="columnDisabledOverrides"/></TD>
						</s:if>
						<tt:hasProperty name="unitime.custom.CourseUrlProvider">
						<TD align="left" class="WebTableHeader"><loc:message name="columnCourseCatalog"/></TD>
						</tt:hasProperty>
					</TR>
				<s:iterator value="form.courseOfferings" var="cx">
					<TR>
						<TD align="center" class="BottomBorderGray">
							&nbsp;
							<s:if test="#cx.isControl == true">
								<IMG src="images/accept.png" alt="${MSG.altControllingCourse()}" title="${MSG.titleControllingCourse()}" border="0">
							</s:if>
							&nbsp;
						</TD>
						<s:if test="form.hasCourseTypes == true">
							<TD class="BottomBorderGray">
								<s:if test="#cx.courseType != null">
									<span title='${cx.courseType.label}'><s:property value="#cx.courseType.reference"/></span>
								</s:if>
							</TD>
						</s:if>
						<TD class="BottomBorderGray"><s:property value="#cx.courseNameWithTitle"/></TD>
						<s:if test="form.hasCourseExternalId == true">
							<TD class="BottomBorderGray">
								<s:if test="#cx.externalUniqueId != null">
									<s:property value="#cx.externalUniqueId"/>
								</s:if>
							</TD>
						</s:if>
						<s:if test="form.hasCourseReservation == true">
							<TD class="BottomBorderGray">
								<s:if test="#cx.reservation != null">
									<s:property value="#cx.reservation"/>
								</s:if>
							</TD>
						</s:if>
						<s:if test="form.hasCredit == true">
							<TD class="BottomBorderGray">
								<s:if test="#cx.credit != null">
									<span title='${cx.credit.creditText()}'><s:property value="#cx.credit.creditAbbv()"/></span>
								</s:if>
							</TD>
						</s:if>
						<s:if test="form.hasScheduleBookNote == true">
							<TD class="BottomBorderGray" style="white-space: pre-wrap;"><s:property value="#cx.scheduleBookNote" escapeHtml="false"/></TD>
						</s:if>
						<s:if test="form.hasDemandOfferings == true">
							<TD class="BottomBorderGray">&nbsp;
								<s:if test="#cx.demandOffering != null">
									<s:property value="#cx.demandOffering.courseName"/>
								</s:if>
							</TD>
						</s:if>
						<s:if test="form.hasAlternativeCourse == true">
							<TD class="BottomBorderGray">&nbsp;
								<s:if test="#cx.alternativeOffering != null">
									<s:property value="#cx.alternativeOffering.courseName"/>
								</s:if>
							</TD>
						</s:if>
						<s:if test="form.hasParentCourse == true">
							<TD class="BottomBorderGray">&nbsp;
								<s:if test="#cx.parentOffering != null">
									<s:property value="#cx.parentOffering.courseName"/>
								</s:if>
							</TD>
						</s:if>
						<TD class="BottomBorderGray">
							<s:if test="#cx.consentType == null">
								<loc:message name="noConsentRequired"/>
							</s:if>
							<s:else>
								<s:property value="#cx.consentType.abbv"/>
							</s:else>
						</TD>
						<s:if test="form.hasDisabledOverrides == true">
							<TD class="BottomBorderGray">
								<s:iterator value="#cx.disabledOverrides" var="override" status="stat">
									<span title='${override.label}'><s:property value="#override.reference"/></span><s:if test="!#stat.last">, </s:if>
								</s:iterator>
							</TD>
						</s:if>
						<tt:hasProperty name="unitime.custom.CourseUrlProvider">
							<TD class="BottomBorderGray">
								<span name='UniTimeGWT:CourseLink' style="display: none;"><s:property value="#cx.uniqueId"/></span>
							</TD>
						</tt:hasProperty>
					</TR>
				</s:iterator>
				</TABLE>
				</div>
			</TD>
	</TR>
		
	<s:if test="form.catalogLinkLabel != null">
		<TR>
			<TD><loc:message name="propertyCourseCatalog"/> </TD>
			<TD>
				<A href="${form.catalogLinkLocation}" target="_blank"><s:property value="form.catalogLinkLabel"/></A>
			</TD>
		</TR>
	</s:if>
	
		
	<TR>
		<TD colspan="2" >&nbsp;</TD>
	</TR>
	</s:form>

<!-- Configuration -->
	<TR>
		<TD colspan="2" valign="middle">
			<s:property value="%{printTable()}" escapeHtml="false"/>
		</TD>
	</TR>

	<TR>
		<TD valign="middle" colspan='3' align='left'>
			<tt:displayPrefLevelLegend/>
		</TD>
	</TR>
	
	<s:form action="colleagueOfferingDetail">
		<s:hidden name="form.courseOfferingId"/>	
		<s:hidden name="form.instrOfferingId"/>	
		<s:hidden name="form.nextId"/>
		<s:hidden name="form.previousId"/>
	
		<tt:last-change type='InstructionalOffering'>
			<s:property value="form.instrOfferingId"/>
		</tt:last-change>

<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
					<sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingCanLock')">
						<s:submit name='op' value='%{#msg.actionLockIO()}'
							accesskey='%{#msg.accessLockIO()}' title='%{#msg.titleLockIO(#msg.accessLockIO())}'
							onclick='%{#msg.jsSubmitLockIO(form.instrOfferingName)}'/>
					</sec:authorize>
					 <sec:authorize access="hasPermission(#form.instrOfferingId, 'InstructionalOffering', 'OfferingCanUnlock')">
					 	<s:submit name='op' value='%{#msg.actionUnlockIO()}'
							accesskey='%{#msg.accessUnlockIO()}' title='%{#msg.titleUnlockIO(#msg.accessUnlockIO())}'
							onclick='%{#msg.jsSubmitUnlockIO(form.instrOfferingName)}'/>
					</sec:authorize>
	
					<s:submit name='op' value='%{#cmsg.actionResendToColleague()}'/>
					
					<s:if test="form.previousId != null">
						<s:submit name='op' value='%{#msg.actionPreviousIO()}'
							accesskey='%{#msg.accessPreviousIO()}' title='%{#msg.titlePreviousIO(#msg.accessPreviousIO())}'/>
					</s:if>
					<s:if test="form.nextId != null">
						<s:submit name='op' value='%{#msg.actionNextIO()}'
							accesskey='%{#msg.accessNextIO()}' title='%{#msg.titleNextIO(#msg.accessNextIO())}'/>
					</s:if>
					
					<tt:back styleClass="btn" 
							name="${CMSG.actionBackToColleagueOfferings()}" 
							title="${CMSG.titleBackToColleagueOfferings(BMSG.accessBackToColleagueOfferings())}" 
							accesskey="${CMSG.accessBackToColleagueOfferings()}" 
							type="InstructionalOffering">
						<s:property value="form.instrOfferingId"/>
					</tt:back>
				</TD>
		</TR>
	</s:form>
	</TABLE>
</loc:bundle>
</loc:bundle>
