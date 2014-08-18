<%-- 
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC
 * "/Users/says/Downloads/Examination Catch17 Data/Fal2012-c17.xml"
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
