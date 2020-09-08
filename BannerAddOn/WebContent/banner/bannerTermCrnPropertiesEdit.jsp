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
<%@ page import="org.unitime.banner.form.BannerTermCrnPropertiesEditForm"%>

<html:form method="post" action="bannerTermCrnPropertiesEdit.do">
	<%// Get Form 
	String frmName = "bannerTermCrnPropertiesEditForm";
	BannerTermCrnPropertiesEditForm frm = (BannerTermCrnPropertiesEditForm) request.getAttribute(frmName);
	%>
	
	<TABLE width="95%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD colspan="3">
				<tt:section-header>
					<tt:section-title>
						
					</tt:section-title>
					<logic:equal name="bannerTermCrnPropertiesEditForm" property="bannerTermCrnPropertiesId"	value="">
						<html:submit styleClass="btn" property="doit" accesskey="S" titleKey="title.saveSession">
							<bean:message key="button.saveSession" />
						</html:submit>
					</logic:equal>
	
					<logic:notEqual name="bannerTermCrnPropertiesEditForm" property="bannerTermCrnPropertiesId"	value="">
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
			<TD>Banner Term Code:</TD>
			<TD colspan='2'>
				<logic:equal name="bannerTermCrnPropertiesEditForm" property="bannerTermCrnPropertiesId" value="">
					<html:select style="width:200;" property="bannerTermCode">
					<html:optionsCollection property="availableBannerTermCodes" value="bannerTermCode" label="bannerTermCode" /></html:select>
				</logic:equal>
				<logic:notEqual name="bannerTermCrnPropertiesEditForm" property="bannerTermCrnPropertiesId" value="">
					<html:hidden property="bannerTermCode"/>	
					<bean:write name="bannerTermCrnPropertiesEditForm" property="bannerTermCode" />	
				</logic:notEqual>
			</TD>
		</TR>
		<TR>
			<TD>Banner Sessions:</TD>
			<!-- TODO: convert this to work with Banner Sessions -->
			<TD colspan='2'>
				<html:select size="<%=String.valueOf(Math.min(7,frm.getAvailableBannerSessions().size()))%>" name="<%=frmName%>" styleClass="cmb" property="bannerSessionIds" multiple="true">
				<html:optionsCollection property="availableBannerSessions" value="uniqueId" label="label" /></html:select>
			</TD>
		</TR>

		<TR>
			<TD>Last CRN:</TD>
			<TD colspan='2'>
				<html:text property="lastCrn" maxlength="5" size="10"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Minimum CRN:</TD>
			<TD colspan='2'>
				<html:text property="minCrn" maxlength="5" size="10"/>
			</TD>
		</TR>

		<TR>
			<TD>Maximum CRN:</TD>
			<TD colspan='2'>
				<html:text property="maxCrn" maxlength="5" size="10"/>
			</TD>
		</TR>
		<TR>
			<TD>Search Flag:</TD>
			<TD colspan='2' align="left">
				<html:checkbox property="searchFlag"/>
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
						<logic:equal name="bannerTermCrnPropertiesEditForm" property="bannerTermCrnPropertiesId" value="">
							<html:submit styleClass="btn" property="doit" styleId="save" accesskey="S" titleKey="title.saveSession">
								<bean:message key="button.saveSession" />
							</html:submit>
						</logic:equal>
		
						<logic:notEqual name="bannerTermCrnPropertiesEditForm" property="bannerTermCrnPropertiesId"	value="">
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
	
	<html:hidden property="bannerTermCrnPropertiesId" />
</html:form>

</script>
