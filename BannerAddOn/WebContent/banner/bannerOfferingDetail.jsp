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

<%@ page import="org.unitime.timetable.defaults.SessionAttribute"%>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.model.DistributionPref" %>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ page import="org.unitime.banner.webutil.WebBannerConfigTableBuilder"%>
<%@ page import="org.unitime.banner.form.BannerOfferingDetailForm"%>
<%@ page import="org.unitime.timetable.solver.WebSolver"%>
<%@ page import="org.unitime.timetable.model.CourseOffering" %>
<%@ page import="org.unitime.timetable.model.Reservation" %>

<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean" %>
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html" %>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic" %>
<%@ taglib uri="http://struts.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>
<%@ taglib uri="http://www.unitime.org/tags-localization" prefix="loc" %> 
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>

<tiles:importAttribute />
<tt:session-context />
<% 
	String frmName = "bannerOfferingDetailForm";
	BannerOfferingDetailForm frm = (BannerOfferingDetailForm) request.getAttribute(frmName);

	String crsNbr = (String)sessionContext.getAttribute(SessionAttribute.OfferingsCourseNumber);
%>
<loc:bundle name="CourseMessages">
<loc:bundle name="BannerMessages" id="BMSG">


	<bean:define name="bannerOfferingDetailForm" property="instrOfferingName" id="instrOfferingName"/>
	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
		<TR>
			<TD valign="middle" colspan='2'>
				<html:form action="/bannerOfferingDetail" styleClass="FormWithNoPadding">
					<input type='hidden' name='confirm' value='y'/>
					<html:hidden property="bannerCourseOfferingId"/>	
					<html:hidden property="instrOfferingId"/>	
					<html:hidden property="nextId"/>
					<html:hidden property="previousId"/>
					<html:hidden property="catalogLinkLabel"/>
					<html:hidden property="catalogLinkLocation"/>
					
				<tt:section-header>
					<tt:section-title>
							<A  title="Back to Banner Course Offering List (Alt+I)" 
								accesskey="I"
								class="l7" 
								href="bannerOfferingSearch.action?doit=Search&form.subjectAreaId=<bean:write name="bannerOfferingDetailForm" property="subjectAreaId" />&form.courseNbr=<%=crsNbr%>#A<bean:write name="bannerOfferingDetailForm" property="instrOfferingId" />"
							><bean:write name="bannerOfferingDetailForm" property="instrOfferingName" /></A> 
					</tt:section-title>						
					<bean:define id="instrOfferingId">
						<bean:write name="bannerOfferingDetailForm" property="instrOfferingId" />				
					</bean:define>
					<bean:define id="subjectAreaId">
						<bean:write name="bannerOfferingDetailForm" property="subjectAreaId" />				
					</bean:define>

					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingCanLock')">
						<html:submit property="op" styleClass="btn" 
								accesskey="<%=MSG.accessLockIO() %>" 
								title="<%=MSG.titleLockIO(MSG.accessLockIO()) %>"
								onclick="<%=MSG.jsSubmitLockIO((String)instrOfferingName)%>">
							<loc:message name="actionLockIO"/>
						</html:submit>
					</sec:authorize>
					 <sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingCanUnlock')">
						<html:submit property="op" styleClass="btn" 
								accesskey="<%=MSG.accessUnlockIO() %>" 
								title="<%=MSG.titleUnlockIO(MSG.accessUnlockIO()) %>"
								onclick="<%=MSG.jsSubmitUnlockIO((String)instrOfferingName)%>">
							<loc:message name="actionUnlockIO"/>
						</html:submit>
					</sec:authorize>
	
					<input type='submit' name='op' value="Resend to Banner" title="Resend Data to Banner" class='btn'>
				
					<logic:notEmpty name="bannerOfferingDetailForm" property="previousId">
						<html:submit property="op" 
								styleClass="btn" accesskey="P" titleKey="title.previousInstructionalOffering">
							<bean:message key="button.previousInstructionalOffering" />
						</html:submit> 
					</logic:notEmpty>
					<logic:notEmpty name="bannerOfferingDetailForm" property="nextId">
						<html:submit property="op" 
							styleClass="btn" accesskey="N" titleKey="title.nextInstructionalOffering">
							<bean:message key="button.nextInstructionalOffering" />
						</html:submit> 
					</logic:notEmpty>

					<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="InstructionalOffering">
						<bean:write name="bannerOfferingDetailForm" property="bannerCourseOfferingId"/>
					</tt:back>
				</tt:section-header>					
				
				</html:form>
			</TD>
		</TR>		

		<logic:messagesPresent>
		<TR>
			<TD colspan="2" align="left" class="errorCell">
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

		<TR>
			<TD width="20%" valign="top">Course Offerings: </TD>
			<TD>
				<TABLE border="0" width="100%" cellspacing="0" cellpadding="2">
					<TR>
						<TD align="center" class="WebTableHeader">Control</TD>
						<TD align="left" class="WebTableHeader">Title</TD>
						<TD align="left" class="WebTableHeader">&nbsp;</TD>
					</TR>
				<logic:iterate id="co" name="bannerOfferingDetailForm" property="courseOfferings" >
					<TR>
						<TD align="center">&nbsp;<logic:equal name="co" property="isControl" value="true"><IMG src="images/accept.png" alt="Controlling Course" title="Controlling Course" border="0"></logic:equal>&nbsp;</TD>
						<TD class="BottomBorderGray"><bean:write name="co" property="courseNameWithTitle"/></TD>
						
						<TD align="right" class="BottomBorderGray">
						&nbsp;
						</TD>
					</TR>
				</logic:iterate>
				</TABLE>
			</TD>
		</TR>
		
		<logic:notEmpty name="bannerOfferingDetailForm" property="catalogLinkLabel">
		<TR>
			<TD>Course Catalog: </TD>
			<TD>
				<A href="<bean:write name="bannerOfferingDetailForm" property="catalogLinkLocation" />" target="_blank"><bean:write name="bannerOfferingDetailForm" property="catalogLinkLabel" /></A>
			</TD>
		</TR>
		</logic:notEmpty>
		
		<TR>
			<TD colspan="2" >&nbsp;</TD>
		</TR>

<!-- Configuration -->
		<TR>
			<TD colspan="2" valign="middle">
	<% //output configuration
	if (frm.getInstrOfferingId() != null){
		WebBannerConfigTableBuilder bcTableBuilder = new WebBannerConfigTableBuilder();
		bcTableBuilder.htmlConfigTablesForBannerOffering(
				    		        WebSolver.getClassAssignmentProxy(session),
				    		        frm.getInstrOfferingId(),
				    		        frm.getBannerCourseOfferingId(),
				    		        sessionContext, out,
				    		        request.getParameter("backType"),
				    		        request.getParameter("backId"));
	}
	%>
			</TD>
		</TR>

		<TR>
			<TD valign="middle" colspan='3' align='left'>
				<tt:displayPrefLevelLegend/>
			</TD>
		</TR>
		
				
		<tt:last-change type='InstructionalOffering'>
			<bean:write name="<%=frmName%>" property="instrOfferingId"/>
		</tt:last-change>		


<!-- Buttons -->
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
			
				<html:form action="/bannerOfferingDetail" styleClass="FormWithNoPadding">
					<input type='hidden' name='confirm' value='y'/>
					<html:hidden property="bannerCourseOfferingId"/>	
					<html:hidden property="instrOfferingId"/>	
					<html:hidden property="nextId"/>
					<html:hidden property="previousId"/>
					
					<sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingCanLock')">
						<html:submit property="op" styleClass="btn" 
								accesskey="<%=MSG.accessLockIO() %>" 
								title="<%=MSG.titleLockIO(MSG.accessLockIO()) %>"
								onclick="<%=MSG.jsSubmitLockIO((String)instrOfferingName)%>">
							<loc:message name="actionLockIO"/>
						</html:submit>
					</sec:authorize>
					 <sec:authorize access="hasPermission(#instrOfferingId, 'InstructionalOffering', 'OfferingCanUnlock')">
						<html:submit property="op" styleClass="btn" 
								accesskey="<%=MSG.accessUnlockIO() %>" 
								title="<%=MSG.titleUnlockIO(MSG.accessUnlockIO()) %>"
								onclick="<%=MSG.jsSubmitUnlockIO((String)instrOfferingName)%>">
							<loc:message name="actionUnlockIO"/>
						</html:submit>
					</sec:authorize>

				<input type='submit' name='op' value="Resend to Banner" title="Resend Data to Banner" class='btn'>

				<logic:notEmpty name="bannerOfferingDetailForm" property="previousId">
					<html:submit property="op" 
							styleClass="btn" accesskey="P" titleKey="title.previousInstructionalOffering">
						<bean:message key="button.previousInstructionalOffering" />
					</html:submit> 
				</logic:notEmpty>
				<logic:notEmpty name="bannerOfferingDetailForm" property="nextId">
					<html:submit property="op" 
						styleClass="btn" accesskey="N" titleKey="title.nextInstructionalOffering">
						<bean:message key="button.nextInstructionalOffering" />
					</html:submit> 
				</logic:notEmpty>

				<tt:back styleClass="btn" name="Back" title="Return to %% (Alt+B)" accesskey="B" type="InstructionalOffering">
					<bean:write name="bannerOfferingDetailForm" property="bannerCourseOfferingId"/>
				</tt:back>
				
				</html:form>					
			</TD>
		</TR>

	</TABLE>
	</loc:bundle>
	</loc:bundle>
