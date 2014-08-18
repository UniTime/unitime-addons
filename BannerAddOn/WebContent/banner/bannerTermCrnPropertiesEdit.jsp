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
<%@ taglib uri="http://struts.apache.org/tags-bean" prefix="bean"%> 
<%@ taglib uri="http://struts.apache.org/tags-html" prefix="html"%>
<%@ taglib uri="http://struts.apache.org/tags-logic" prefix="logic"%>
<%@ taglib uri="http://www.unitime.org/tags-custom" prefix="tt" %>

<html:form method="post" action="bannerTermCrnPropertiesEdit.do">
	
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
