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
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%> 
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<html:form method="post" action="bannerSessionEdit.do">
	<INPUT type="hidden" name="refresh" value="">
	
	<TABLE width="95%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD colspan="3">
				<tt:section-header>
					<tt:section-title>
						
					</tt:section-title>
					<logic:equal name="bannerSessionEditForm" property="sessionId"	value="">
						<html:submit styleClass="btn" property="doit" accesskey="S" titleKey="title.saveSession">
							<bean:message key="button.saveSession" />
						</html:submit>
					</logic:equal>
	
					<logic:notEqual name="bannerSessionEditForm" property="sessionId"	value="">
						<html:submit styleClass="btn" property="doit" accesskey="U" titleKey="title.updateSession">
							<bean:message key="button.updateSession" />
						</html:submit>
					</logic:notEqual>
				
					<html:submit styleClass="btn" property="doit" accesskey="B" titleKey="title.cancelSessionEdit" >
						<bean:message key="button.cancelSessionEdit" />
					</html:submit>
				</tt:section-header>			
			</TD>
		</TR>
		
		<logic:messagesPresent>
		<TR>
			<TD colspan="3" align="left" class="errorCell">
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
			<TD>Academic Session:</TD>
			<TD colspan='2'>
				<logic:equal name="bannerSessionEditForm" property="sessionId" value="">
					<html:select style="width:200;" property="acadSessionId">
					<html:optionsCollection property="availableAcadSessions" value="uniqueId" label="label" /></html:select>
				</logic:equal>
				<logic:notEqual name="bannerSessionEditForm" property="sessionId" value="">
					<html:hidden property="acadSessionLabel"/>	
					<html:hidden property="acadSessionId"/>
					<bean:write name="bannerSessionEditForm" property="acadSessionLabel" />	
				</logic:notEqual>
			</TD>
		</TR>

		<TR>
			<TD>Banner Term Code:</TD>
			<TD colspan='2'>
				<html:text property="bannerTermCode" maxlength="20" size="20"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Banner Campus:</TD>
			<TD colspan='2'>
				<html:text property="bannerCampus"  maxlength="20" size="20"/>
			</TD>
		</TR>

		<TR>
			<TD>Store Data for Banner:</TD>
			<TD colspan='2' align="left">
				<html:checkbox property="storeDataForBanner"/>
			</TD>
		</TR>
		<TR>
			<TD>Send Data to Banner:</TD>
			<TD colspan='2' align="left">
				<html:checkbox property="sendDataToBanner"/>
			</TD>
		</TR>
		<logic:equal name="bannerSessionEditForm" property="loadingOfferingsFile" value="true">
		<TR>
			<TD valign="top">Loading Offerings File:</TD>
			<TD colspan='2' align="left">
				<html:checkbox property="loadingOfferingsFile"/><font color="red">&nbsp;&nbsp;&nbsp;&nbsp;<b>Note -</b> Do not make changes to this field unless recovering from a failed banner offerings XML load.</font>
			</TD>
		</TR>
		</logic:equal>
		<TR>
			<TD>Future Session:</TD>
			<TD colspan='2' align="left">
				<html:select style="width:200;" property="futureSessionId">
					<html:option value=""></html:option>
					<html:optionsCollection property="availableBannerSessions" value="uniqueId" label="label"/>
				</html:select>
			</TD>
		</TR>
		
		<TR>
			<TD>Update Mode:</TD>
			<TD colspan='2' align="left">
				<html:select style="width:200;" property="futureUpdateMode">
					<html:option value="0">Disabled (no automatic future term updates)</html:option>
					<html:option value="1">Direct Update (student changes automatically propagated into the future term)</html:option>
					<html:option value="2">Send Request (when student changed, automatically request future term student update)</html:option>
				</html:select>
			</TD>
		</TR>
						
		
		<TR>
			<TD colspan="3">
			<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="3" align="right">

			<TABLE>
				<TR>
					<TD align="right">
						<logic:equal name="bannerSessionEditForm" property="sessionId" value="">
							<html:submit styleClass="btn" property="doit" styleId="save" accesskey="S" titleKey="title.saveSession">
								<bean:message key="button.saveSession" />
							</html:submit>
						</logic:equal>
		
						<logic:notEqual name="bannerSessionEditForm" property="sessionId"	value="">
							<html:submit styleClass="btn" property="doit" styleId="save" accesskey="U" titleKey="title.updateSession">
								<bean:message key="button.updateSession" />
							</html:submit>
						</logic:notEqual>
					
					<html:submit styleClass="btn" property="doit" accesskey="B" titleKey="title.cancelSessionEdit" >
						<bean:message key="button.cancelSessionEdit" />
					</html:submit>
					</TD>
				</TR>
				
			</TABLE>
			
			</TD>
		</TR>


	</TABLE>
	
	<html:hidden property="sessionId" />
</html:form>

</script>
