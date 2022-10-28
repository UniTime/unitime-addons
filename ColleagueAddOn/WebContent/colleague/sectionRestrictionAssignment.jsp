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
<s:form action="sectionRestrictionAssignment">
<tt:confirm name="confirmUnassignAll"><loc:message name="confirmUnassignAllRestrictions" id="CMSG"/></tt:confirm>
	<s:hidden name="form.instrOffrConfigId"/>
	<s:hidden name="form.instrOfferingId"/>
	<s:hidden name="form.instrOfferingName"/>
	<s:hidden name="form.courseOfferingId"/>
	<s:hidden name="form.subjectAreaId"/>
	<s:hidden name="form.deletedRestrRowNum" value="" id="deletedRestrRowNum"/>
	<s:hidden name="form.addRestrictionId" value="" id="addRestrictionId"/>
	<s:hidden name="form.op" value="" id="hdnOp"/>
	<s:hidden name="form.displayExternalId"/>
<table class="unitime-Table">
	<TR>
		<TD colspan="2" valign="middle">
			 <tt:section-header>
				<tt:section-title>
						<A  title="${MSG.titleBackToIOList(MSG.accessBackToIOList())}" 
							accesskey="${MSG.accessBackToIOList()}"
							class="l8" 
							href="colleagueOfferingSearch.action?doit=Search&form.subjectAreaId=${form.subjectAreaId}&courseNbr=${crsNbr}#A${form.instrOfferingId}"
						><s:property value="form.instrOfferingName" /></A>
				</tt:section-title>
				
				<!-- dummy submit button to make sure Update button is the first (a.k.a. default) submit button -->
				<s:submit name="op" value="%{#cmsg.actionUpdateSectionRestrictionAssignment()}" style="position: absolute; left: -100%;"/>
				
				<s:submit name="op" value="%{#cmsg.actionUnassignAllRestrictionsFromConfig()}"
					title="%{#cmsg.titleUnassignAllRestrictionsFromConfig()}" onclick="return confirmUnassignAll();"/>

				<s:submit name="op" value="%{#cmsg.actionUpdateSectionRestrictionAssignment()}"
					title="%{#cmsg.titleUpdateSectionRestrictionsAssignment(#cmsg.accessUpdateSectionRestrictionAssignment())}"
					accesskey="%{#cmsg.accessUpdateSectionRestrictionAssignment()}" />
					
				<s:if test="form.previousId != null">
					<s:submit name='op' value='%{#msg.actionPreviousIO()}'
						accesskey='%{#msg.accessPreviousIO()}' title='%{#msg.titlePreviousIO(#msg.accessPreviousIO())}'/>
					<s:hidden name="form.previousId"/>
				</s:if>
				<s:if test="form.nextId != null">
					<s:submit name='op' value='%{#msg.actionNextIO()}'
						accesskey='%{#msg.accessNextIO()}' title='%{#msg.titleNextIO(#msg.accessNextIO())}'/>
					<s:hidden name="form.nextId"/>
				</s:if>
				
				<s:submit name='op' value='%{#msg.actionBackToIODetail()}'
						accesskey='%{#msg.accessBackToIODetail()}' title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'/>
			</tt:section-header>					
		</TD>
	</TR>
	
	<s:if test="!fieldErrors.isEmpty()">
		<TR><TD colspan="2" align="left" class="errorTable">
			<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
		</TD></TR>
	</s:if>	
	
	<TR>
		<TD colspan="2" align="left">
			<TABLE class="unitime-Table" style="width:100%;">
				<TR>
					<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
					<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
					<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
					<s:if test="form.displayExternalId == true">
						<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnColleagueSynonym" id="CMSG"/></TD>
					</s:if>
					<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
					<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
					<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
					<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
					<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnInstructorName"/></TD>
					<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
					<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnAssignedTime"/></TD>
					<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
					<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnAssignedRoom"/></TD>
					<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
					</TR>
				<TR></TR>
					<s:iterator value="form.sectionIds" var="c" status="stat"><s:set var="ctr" value="#stat.index"/>
						<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
							<TD nowrap valign="top">
								<s:hidden name="form.sectionIds[%{#ctr}]"/>
								<s:hidden name="form.sectionLabels[%{#ctr}]"/>
								<s:hidden name="form.sectionLabelIndents[%{#ctr}]"/>
								<s:hidden name="form.rooms[%{#ctr}]"/>
								<s:hidden name="form.times[%{#ctr}]"/>
								<s:hidden name="form.allowDeletes[%{#ctr}]"/>
								<s:hidden name="form.readOnlySections[%{#ctr}]"/>
								<s:hidden name="form.sectionHasErrors[%{#ctr}]"/>
								<s:hidden name="form.showDisplay[%{#ctr}]"/>
								&nbsp;
							</TD>
							<TD nowrap valign="top">
								<s:if test="form.sectionHasErrors[#ctr] == true">
									<IMG src="images/cancel.png">
								</s:if><s:else>
									&nbsp;
								</s:else>
							</TD>
							<TD nowrap valign="top">
								<s:property value="form.sectionLabelIndents[#ctr]" escapeHtml="false"/>
								<s:property value="form.sectionLabels[#ctr]"/> 
								&nbsp;
							</TD>
							
							<s:if test="form.displayExternalId == true">
								<TD>&nbsp;</TD>
								<TD align="left" valign="top" nowrap><s:property value="form.externalIds[#ctr]" escapeHtml="false"/></TD>
							</s:if>
							<TD>&nbsp;</TD>
							<TD align="center" valign="top" nowrap>
								<s:if test="form.readOnlySections[#ctr] == false && form.allowDeletes[#ctr] == true">
									<IMG border="0" src="images/action_delete.png" title="${CMSG.titleDeleteRestrictionFromSection()}"
										onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
										onclick="document.getElementById('hdnOp').value='DR';
												document.getElementById('deletedRestrRowNum').value='${ctr}';
												submit();">
								</s:if>
							</TD>
							<TD align="center" valign="top" nowrap> &nbsp;
								<s:if test="form.readOnlySections[#ctr] == false">
									<IMG border="0" src="images/action_add.png" title="${CMSG.titleAddRestrictionToSection()}"
										onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
										onclick="document.getElementById('hdnOp').value='AR';
												document.getElementById('addRestrictionId').value='${ctr}';
												submit();">
								</s:if>
							</TD>
							<TD>&nbsp;<s:hidden name="form.externalIds[%{#ctr}]"/></TD>
							<TD align="left" valign="top" nowrap>
								<s:if test="form.readOnlySections[#ctr] == false">
									<s:select name="form.restrictionUids[%{#ctr}]" style="min-width:200px;" tabindex="%{#ctr+1000}"
										list="#request.restrictionList" listKey="uniqueId" listValue="optionLabel"
										headerKey="-1" headerValue="-"
										/>
								</s:if><s:else>
									<s:iterator value="#request.restrictionList" var="restriction">
										<s:if test="#restriction.uniqueId == form.restrictionUids[#ctr]">
											<s:property value="#restriction.optionLabel"/>
										</s:if>
									</s:iterator>
									<s:hidden name="form.restrictionUids[%{#ctr}]"/>
								</s:else>
							</TD>
							
							<TD>&nbsp;&nbsp;</TD>
							<TD align="left" valign="top" nowrap>
								<s:property value="form.times[#ctr]" escapeHtml="false"/>
							</TD>
							<TD>&nbsp;</TD>
							<TD align="left" valign="top" nowrap>
								<s:property value="form.rooms[#ctr]" escapeHtml="false"/>
							</TD>
							<TD>&nbsp;</TD>
						</TR>
					</s:iterator>
				</TABLE>
			</TD>
		</TR>

<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<s:submit name="op" value="%{#cmsg.actionUnassignAllRestrictionsFromConfig()}"
					title="%{#cmsg.titleUnassignAllRestrictionsFromConfig()}" onclick="return confirmUnassignAll();"/>

				<s:submit name="op" value="%{#cmsg.actionUpdateSectionRestrictionAssignment()}"
					title="%{#cmsg.titleUpdateSectionRestrictionsAssignment(#cmsg.accessUpdateSectionRestrictionAssignment())}"
					accesskey="%{#cmsg.accessUpdateSectionRestrictionAssignment()}" />
					
				<s:if test="form.previousId != null">
					<s:submit name='op' value='%{#msg.actionPreviousIO()}'
						accesskey='%{#msg.accessPreviousIO()}' title='%{#msg.titlePreviousIO(#msg.accessPreviousIO())}'/>
				</s:if>
				<s:if test="form.nextId != null">
					<s:submit name='op' value='%{#msg.actionNextIO()}'
						accesskey='%{#msg.accessNextIO()}' title='%{#msg.titleNextIO(#msg.accessNextIO())}'/>
				</s:if>
				
				<s:submit name='op' value='%{#msg.actionBackToIODetail()}'
						accesskey='%{#msg.accessBackToIODetail()}' title='%{#msg.titleBackToIODetail(#msg.accessBackToIODetail())}'/>
			</TD>
		</TR>
	</TABLE>
</s:form>
</loc:bundle>
</loc:bundle>