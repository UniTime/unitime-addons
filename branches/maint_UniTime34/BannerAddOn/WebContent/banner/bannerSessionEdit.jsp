<%-- 
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 --%>
<%@ page import="org.unitime.timetable.webutil.JavascriptFunctions" %>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean"%> 
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html"%>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic"%>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

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
