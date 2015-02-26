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

<html:form method="post" action="bannerCampusOverrideEdit.do">
	<INPUT type="hidden" name="refresh" value="">
	
	<TABLE width="95%" border="0" cellspacing="0" cellpadding="3">

		<TR>
			<TD colspan="3">
				<tt:section-header>
					<tt:section-title>
						
					</tt:section-title>
					<logic:equal name="bannerCampusOverrideEditForm" property="campusOverrideId"	value="">
						<html:submit styleClass="btn" property="doit" accesskey="S" titleKey="button.save">
							<bean:message key="button.save" />
						</html:submit>
					</logic:equal>
	
					<logic:notEqual name="bannerCampusOverrideEditForm" property="campusOverrideId"	value="">
						<html:submit styleClass="btn" property="doit" accesskey="U" titleKey="button.update">
							<bean:message key="button.update" />
						</html:submit>
					</logic:notEqual>
				
					<html:submit styleClass="btn" property="doit" accesskey="B" titleKey="button.cancel" >
						<bean:message key="button.cancel" />
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
			<TD>Banner Campus Code:</TD>
			<TD colspan='2'>
				<html:text property="bannerCampusCode" maxlength="20" size="20"/>
			</TD>
		</TR>
		
		<TR>
			<TD>Banner Campus Name:</TD>
			<TD colspan='2'>
				<html:text property="bannerCampusName"  maxlength="20" size="20"/>
			</TD>
		</TR>

		<TR>
			<TD>Visible:</TD>
			<TD colspan='2' align="left">
				<html:checkbox property="visible"/>
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
						<logic:equal name="bannerCampusOverrideEditForm" property="campusOverrideId" value="">
							<html:submit styleClass="btn" property="doit" styleId="save" accesskey="S" titleKey="button.save">
								<bean:message key="button.save" />
							</html:submit>
						</logic:equal>
		
						<logic:notEqual name="bannerCampusOverrideEditForm" property="campusOverrideId"	value="">
							<html:submit styleClass="btn" property="doit" styleId="save" accesskey="U" titleKey="button.update">
								<bean:message key="button.update" />
							</html:submit>
						</logic:notEqual>
					
					<html:submit styleClass="btn" property="doit" accesskey="B" titleKey="button.cancel" >
						<bean:message key="button.cancel" />
					</html:submit>
					</TD>
				</TR>
				
			</TABLE>
			
			</TD>
		</TR>


	</TABLE>
	
	<html:hidden property="campusOverrideId" />
</html:form>

</script>
