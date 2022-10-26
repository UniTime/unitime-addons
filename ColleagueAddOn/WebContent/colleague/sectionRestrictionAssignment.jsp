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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.defaults.UserProperty"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.colleague.model.ColleagueRestriction" %>
<%@ page import="org.unitime.colleague.form.SectionRestrictionAssignmentForm" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %>
<tiles:importAttribute />

<loc:bundle name="ColleagueMessages">

<tt:session-context/>
<% 
	String frmName = "sectionRestrictionAssignmentForm";
	SectionRestrictionAssignmentForm frm = (SectionRestrictionAssignmentForm)request.getAttribute(frmName);
	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>

<html:form action="/sectionRestrictionAssignment">
<html:hidden name="<%=frmName%>" property="instrOffrConfigId"/>
<html:hidden property="instrOfferingId"/>	
<html:hidden property="courseOfferingId"/>	
<INPUT type="hidden" name="deletedRestrRowNum" value = "">
<INPUT type="hidden" name="addRestrictionId" value = "">
<INPUT type="hidden" name="hdnOp" value = "">

<SCRIPT language="javascript">
	<!--
		<%= JavascriptFunctions.getJsConfirm(sessionContext) %>
		
		function confirmUnassignAll() {
			if (jsConfirm!=null && !jsConfirm)
				return true;

			if (!confirm('<%=MSG.confirmUnassignAllRestrictions() %>')) {
				return false;
			}

			return true;
		}

	// -->
</SCRIPT>

	<TABLE width="100%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD colspan="2" valign="middle">
				 <tt:section-header>
					<tt:section-title>
							<A  title="<%=MSG.titleBackToIOList(MSG.accessBackToIOList()) %>" 
								accesskey="<%=MSG.accessBackToIOList() %>"
								class="l8" 
								href="colleagueOfferingSearch.action?doit=Search&form.subjectAreaId=<bean:write name="<%=frmName%>" 
										property="subjectAreaId" />&courseNbr=<%=crsNbr%>#A<bean:write name="<%=frmName%>" property="instrOfferingId" />"
							><bean:write name="<%=frmName%>" property="instrOfferingName" /></A>
							<html:hidden property="instrOfferingId"/>
							<html:hidden property="instrOfferingName"/>
					</tt:section-title>
				
				<!-- dummy submit button to make sure Update button is the first (a.k.a. default) submit button -->
				<html:submit property="op" style="position: absolute; left: -100%;"><loc:message name="actionUpdateSectionRestrictionAssignment" /></html:submit>						

				<html:submit property="op"
					onclick="return confirmUnassignAll();"
					styleClass="btn" 
					title="<%=MSG.titleUnassignAllRestrictionsFromConfig() %>">
					<loc:message name="actionUnassignAllRestrictionsFromConfig" />
				</html:submit>
				 
				&nbsp;
				<html:submit property="op"
					styleClass="btn" 
					accesskey="<%=MSG.accessUpdateSectionRestrictionAssignment() %>" 
					title="<%=MSG.titleUpdateSectionRestrictionsAssignment(MSG.accessUpdateSectionRestrictionAssignment()) %>">
					<loc:message name="actionUpdateSectionRestrictionAssignment" />
				</html:submit>
			
				<bean:define id="instrOfferingId">
					<bean:write name="<%=frmName%>" property="instrOfferingId" />				
				</bean:define>
				<bean:define id="courseOfferingId">
					<bean:write name="<%=frmName%>" property="courseOfferingId" />				
				</bean:define>

				<logic:notEmpty name="<%=frmName%>" property="previousId">
					<html:hidden name="<%=frmName%>" property="previousId"/>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessPreviousIO() %>" 
						title="<%=MSG.titlePreviousIOWithUpdate(MSG.accessPreviousIO()) %>">
						<loc:message name="actionPreviousIO" />
					</html:submit> 
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					<html:hidden name="<%=frmName%>" property="nextId"/>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessNextIO() %>" 
						title="<%=MSG.titleNextIOWithUpdate(MSG.accessNextIO()) %>">
						<loc:message name="actionNextIO" />
					</html:submit> 
				</logic:notEmpty>
				 
				&nbsp;
				<html:button property="op" 
					styleClass="btn" 
					accesskey="<%=MSG.accessBackToIODetail() %>" 
					title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail()) %>" 
					onclick="document.location.href='colleagueOfferingDetail.do?op=view&io=${instrOfferingId}&co=${courseOfferingId}';">
					<loc:message name="actionBackToIODetail" />
				</html:button>		
				</tt:section-header>					
			</TD>
		</TR>

		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
					<B><U><loc:message name="errors"/></U></B><BR>
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

		<TR>
			<TD colspan="2" align="left">
				<TABLE align="left" border="0" cellspacing="0" cellpadding="1">
					<TR>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
						<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'> &nbsp;</TD>
						<logic:equal name="<%=frmName%>" property="displayExternalId" value="true" >
							<TD rowspan="2" class='WebTableHeader'>&nbsp;</TD>
							<TD align="center" valign="bottom" rowspan="2" class='WebTableHeader'><loc:message name="columnColleagueSynonym"/></TD>
						</logic:equal>
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
					<logic:iterate name="<%=frmName%>" property="sectionIds" id="c" indexId="ctr">
						<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
							<TD nowrap valign="top">
								<html:hidden property='<%= "sectionIds[" + ctr + "]" %>'/>
								<html:hidden property='<%= "sectionLabels[" + ctr + "]" %>'/>
								<html:hidden property='<%= "sectionLabelIndents[" + ctr + "]" %>'/>
								<html:hidden property='<%= "rooms[" + ctr + "]" %>'/>
								<html:hidden property='<%= "times[" + ctr + "]" %>'/>
								<html:hidden property='<%= "allowDeletes[" + ctr + "]" %>'/>
								<html:hidden property='<%= "readOnlySections[" + ctr + "]" %>'/>
								<html:hidden property='<%= "sectionHasErrors[" + ctr + "]" %>'/>
								<html:hidden name="<%=frmName%>" property='<%= "showDisplay[" + ctr + "]" %>' />
								&nbsp;
							</TD>
							<TD nowrap valign="top">
								<logic:equal name="<%=frmName%>" property='<%= "sectionHasErrors[" + ctr + "]" %>' value="true" >
									<IMG src="images/cancel.png">
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "sectionHasErrors[" + ctr + "]" %>' value="false" >
									&nbsp;
								</logic:equal>
							</TD>
							<TD nowrap valign="top">
								<%=frm.getSectionLabelIndents().get(ctr.intValue()).toString()%>
								<bean:write name="<%=frmName%>" property='<%= "sectionLabels[" + ctr + "]" %>'/> 
								&nbsp;
							</TD>
	
							<logic:equal name="<%=frmName%>" property="displayExternalId" value="true" >
								<TD>&nbsp;</TD>
								<TD align="left" valign="top" nowrap><%= frm.getExternalIds().get(ctr)%></TD>
							</logic:equal>
							<TD>&nbsp;</TD>
							<TD align="center" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlySections[" + ctr + "]" %>' value="false" >
									<logic:equal name="<%=frmName%>" property='<%= "allowDeletes[" + ctr + "]" %>' value="true" >
										<IMG border="0" src="images/action_delete.png" title="<%=MSG.titleDeleteRestrictionFromSection() %>"
											onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
											onclick="document.forms[0].elements['hdnOp'].value='<%=MSG.altDelete()%>';
													document.forms[0].elements['deletedRestrRowNum'].value='<%= ctr.toString() %>';
													document.forms[0].submit();">
										</logic:equal>
								</logic:equal>
							</TD>
							<TD align="center" valign="top" nowrap> &nbsp;
								<logic:equal name="<%=frmName%>" property='<%= "readOnlySections[" + ctr + "]" %>' value="false" >
									<IMG border="0" src="images/action_add.png" title="<%=MSG.titleAddRestrictionToSection() %>"
										onmouseover="this.style.cursor='hand';this.style.cursor='pointer';"
										onclick="document.forms[0].elements['hdnOp'].value='<%=MSG.altAdd()%>';
												document.forms[0].elements['addRestrictionId'].value='<%= ctr.toString() %>';
												document.forms[0].submit();">
								</logic:equal>
							</TD>
							<TD>&nbsp;<html:hidden property='<%= "externalIds[" + ctr + "]" %>'/></TD>
							<TD align="left" valign="top" nowrap>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlySections[" + ctr + "]" %>' value="false" >
									<html:select style="width:200px;" property='<%= "restrictionUids[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(10000 + ctr.intValue())%>">
										<html:option value="<%= Constants.BLANK_OPTION_VALUE%>"><%=Constants.BLANK_OPTION_LABEL%></html:option>
										<html:options collection="restrictionList" property="uniqueId" labelProperty="optionLabel" />
									</html:select>
								</logic:equal>
								<logic:equal name="<%=frmName%>" property='<%= "readOnlySections[" + ctr + "]" %>' value="true" >
									<% String nameFormat = UserProperty.NameFormat.get(sessionContext.getUser()); %>
									<logic:iterate scope="request" name="restrictionList" id="restriction">
										<logic:equal name="<%=frmName%>" property='<%= "restrictionUids[" + ctr + "]" %>' value="<%=((ColleagueRestriction)restriction).getUniqueId().toString()%>">
											<%=((ColleagueRestriction)restriction).getOptionLabel()%>
										</logic:equal>
									</logic:iterate>
									<html:hidden property='<%= "restrictionUids[" + ctr + "]" %>'/>
								</logic:equal>
							</TD>
							
							<TD>&nbsp;&nbsp;</TD>
							<TD align="left" valign="top" nowrap>
								<%= frm.getTimes().get(ctr)%>
							</TD>
							<TD>&nbsp;</TD>
							<TD align="left" valign="top" nowrap><%= frm.getRooms().get(ctr)%></TD>
							<TD>&nbsp;</TD>
						</TR>
					</logic:iterate>
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
				<html:submit property="op"
					onclick="return confirmUnassignAll();"
					styleClass="btn" 
					title="<%=MSG.titleUnassignAllRestrictionsFromConfig() %>">
					<loc:message name="actionUnassignAllRestrictionsFromConfig" />
				</html:submit>
			 
				&nbsp;
				<html:submit property="op"
					styleClass="btn" 
					accesskey="<%=MSG.accessUpdateSectionRestrictionAssignment() %>" 
					title="<%=MSG.titleUpdateSectionRestrictionsAssignment(MSG.accessUpdateSectionRestrictionAssignment()) %>">
					<loc:message name="actionUpdateSectionRestrictionAssignment" />
				</html:submit>
			
				<bean:define id="instrOfferingId">
					<bean:write name="<%=frmName%>" property="instrOfferingId" />				
				</bean:define>

				<logic:notEmpty name="<%=frmName%>" property="previousId">
					<html:hidden name="<%=frmName%>" property="previousId"/>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessPreviousIO() %>" 
						title="<%=MSG.titlePreviousIOWithUpdate(MSG.accessPreviousIO()) %>">
						<loc:message name="actionPreviousIO" />
					</html:submit> 
				</logic:notEmpty>
				<logic:notEmpty name="<%=frmName%>" property="nextId">
					<html:hidden name="<%=frmName%>" property="nextId"/>
					&nbsp;
					<html:submit property="op" 
						styleClass="btn" 
						accesskey="<%=MSG.accessNextIO() %>" 
						title="<%=MSG.titleNextIOWithUpdate(MSG.accessNextIO()) %>">
						<loc:message name="actionNextIO" />
					</html:submit> 
				</logic:notEmpty>

				&nbsp;
				<html:button property="op" 
					styleClass="btn" 
					accesskey="<%=MSG.accessBackToIODetail() %>" 
					title="<%=MSG.titleBackToIODetail(MSG.accessBackToIODetail()) %>" 
					onclick="document.location.href='colleagueOfferingDetail.do?op=view&io=${instrOfferingId}&co=${courseOfferingId}';">
					<loc:message name="actionBackToIODetail" />
				</html:button>		
					
			</TD>
		</TR>

	</TABLE>

</html:form>

</loc:bundle>