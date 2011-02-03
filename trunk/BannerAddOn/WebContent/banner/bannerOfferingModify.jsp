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
<%@ page language="java" autoFlush="true" errorPage="../error.jsp" %>
<%@ page import="org.unitime.timetable.util.Constants" %>
<%@ page import="org.unitime.timetable.util.IdValue" %>
<%@ page import="org.unitime.timetable.model.DatePattern" %>
<%@ page import="org.unitime.banner.form.BannerOfferingModifyForm" %>
<%@page import="org.unitime.timetable.model.OfferingConsentType"%>
<%@page import="org.unitime.timetable.model.ItypeDesc"%>
<%@ taglib uri="/WEB-INF/tld/struts-bean.tld" prefix="bean" %>
<%@ taglib uri="/WEB-INF/tld/struts-html.tld" prefix="html" %>
<%@ taglib uri="/WEB-INF/tld/struts-logic.tld" prefix="logic" %>
<%@ taglib uri="/WEB-INF/tld/struts-tiles.tld" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/tld/timetable.tld" prefix="tt" %>

<SCRIPT language="javascript">
	<!--

		function doClick(op, id) {
			document.forms[0].elements["hdnOp"].value=op;
			document.forms[0].elements["id"].value=id;
			document.forms[0].elements["click"].value="y";
			document.forms[0].submit();
		}
		
	// -->
</SCRIPT>

<tiles:importAttribute />
<% 
	String frmName = "bannerOfferingModifyForm";
	BannerOfferingModifyForm frm = (BannerOfferingModifyForm)request.getAttribute(frmName);
	String crsNbr = "";
	if (session.getAttribute(Constants.CRS_NBR_ATTR_NAME)!=null )
		crsNbr = session.getAttribute(Constants.CRS_NBR_ATTR_NAME).toString();
%>

<script language='JavaScript'>
      function resetAllDisplayFlags(value, baseName) {
            for (var i=0;i<<%=frm.getBannerSectionIds().size()%>;i++) {
                  var chbox = document.getElementsByName(baseName+'['+i+']');
                  if (chbox!=null && chbox.length>0)
                        chbox[0].checked = value;
            }
      }
</script>

<script language="javascript">displayLoading();</script>

<html:form action="/bannerOfferingModify">
	<html:hidden property="bannerCourseOfferingId"/>	
	<html:hidden property="bannerConfigId"/>	
	<html:hidden property="instrOfferingId"/>	
	<html:hidden property="instrOfferingName"/>	
	<html:hidden property="instrOffrConfigId"/>	
	<html:hidden property="configIsEditable"/>
	<html:hidden property="showLimitOverride"/>
	<INPUT type="hidden" name="hdnOp" value = "">
	<INPUT type="hidden" name="id" value = "">
	<INPUT type="hidden" name="click" value = "">

	<TABLE width="93%" border="0" cellspacing="0" cellpadding="3">
<!-- Buttons -->
		<TR>
			<TD valign="middle" colspan="2">
				 <tt:section-header>
					<tt:section-title>
					<bean:write name="<%=frmName%>" property="instrOfferingName" />
					</tt:section-title>						
				<html:submit property="op"  disabled="true"
					styleClass="btn" accesskey="U" titleKey="title.updateInstructionalOfferingConfig" >
					<bean:message key="button.updateInstructionalOfferingConfig" />
				</html:submit>
				<bean:define id="bannerCourseOfferingId">
					<bean:write name="<%=frmName%>" property="bannerCourseOfferingId" />				
				</bean:define>
				 
				<html:button property="op" 
					styleClass="btn" accesskey="B" titleKey="title.backToInstrOffrDetail" 
					onclick="document.location.href='bannerOfferingDetail.do?op=view&bc=${bannerCourseOfferingId}';">
					<bean:message key="button.backToInstrOffrDetail" />
				</html:button>		
				</tt:section-header>					
												 
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
		<TD align="left" colspan="2">
			Configuration Gradable Itype:
					<logic:equal name="<%=frmName%>" property='<%= "configIsEditable" %>' value="true" ><html:select style="width:200;" property='<%= "itypeId" %>' tabindex="<%=java.lang.Integer.toString(9000)%>"><html:option value="-1">No Itype</html:option><html:options collection="availableItypes" property="itype" labelProperty="desc" /></html:select></logic:equal><logic:equal name="<%=frmName%>" property='<%= "configIsEditable" %>' value="false" ><logic:iterate scope="request" name="availableItypes" id="ityp"><logic:equal name="<%=frmName%>" property='<%= "itypeId" %>' value="<%=((ItypeDesc)ityp).getItype().toString()%>"><bean:write name="ityp" property="desc" /></logic:equal></logic:iterate><html:hidden property='<%= "itypeId" %>'/></logic:equal>
			
		</TD>
		</TR>
		<TR>
			<TD colspan="2" align="left">
				<TABLE align="left" border="0" cellspacing="0" cellpadding="1">
					<TR>
						<TD align="center" valign="bottom" rowSpan="2"><I>&nbsp;</I></TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>&nbsp;</I></TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>Itype</I></TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>Section&nbsp;Id</I></TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>Consent</I></TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>Course Credit<br>Override</I></TD>
						<TD rowspan="2">&nbsp;</TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>Credit</I></TD>
						<logic:equal name="<%=frmName %>" property='showLimitOverride' value="true">
							<TD rowspan="2">&nbsp;</TD>
							<TD align="center" valign="bottom" rowSpan="2"><I>Limit<br>Override</I></TD>
							<TD align="center" valign="bottom" rowSpan="2"><I>Limit</I></TD>
						</logic:equal>
						<TD rowspan="2">&nbsp;</TD>
						<TD align="center" valign="bottom" rowSpan="2"><I>Date Pattern</I></TD>
						<TD rowspan="2">&nbsp;</TD>
						<TD align="center" valign="bottom" rowSpan="1" colspan="3"><I>---- Timetable ----</I></TD>
						<TD rowspan="2">&nbsp;</TD>
						<TD align="left" valign="bottom" rowSpan="2"><I>Instructors</I></TD>
						<TD>&nbsp;</TD>
						<TD>&nbsp;</TD>
					</TR>
					<TR>
						<td align="left" valign="bottom"><I>Time</I></td>
						<TD>&nbsp;</TD>
						<td align="left" valign="bottom"><I>Room</I></td>						
					</TR>					
					<logic:iterate name="<%=frmName%>" property="bannerSectionIds" id="c" indexId="ctr">
					<TR onmouseover="this.style.backgroundColor='rgb(223,231,242)';this.style.cursor='default';" onmouseout="this.style.backgroundColor='transparent';">
						<TD valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "classHasErrors[" + ctr + "]" %>' value="true" ><IMG src="images/Error16.jpg"></logic:equal><logic:equal name="<%=frmName%>" property='<%= "classHasErrors[" + ctr + "]" %>' value="false" >&nbsp;</logic:equal></TD>
						<TD valign="top" nowrap><html:hidden property='<%= "bannerSectionIds[" + ctr + "]" %>'/><html:hidden property='<%= "bannerSectionOriginalSectionIds[" + ctr + "]" %>'/><html:hidden property='<%= "itypes[" + ctr + "]" %>'/><html:hidden property='<%= "readOnlyClasses[" + ctr + "]" %>'/><html:hidden property='<%= "bannerSectionLabels[" + ctr + "]" %>'/><html:hidden property='<%= "bannerSectionLabelIndents[" + ctr + "]" %>'/><html:hidden property='<%= "datePatterns[" + ctr + "]" %>'/><html:hidden property='<%= "times[" + ctr + "]" %>'/><html:hidden property='<%= "rooms[" + ctr + "]" %>'/><html:hidden property='<%= "instructors[" + ctr + "]" %>'/><html:hidden property='<%= "classHasErrors[" + ctr + "]" %>'/><%=frm.getBannerSectionLabelIndents().get(ctr.intValue()).toString()%><bean:write name="<%=frmName%>" property='<%= "bannerSectionLabels[" + ctr + "]" %>'/> &nbsp;</TD>
						<TD align="left" valign="top" nowrap><bean:write name="<%=frmName%>" property='<%= "itypes[" + ctr + "]" %>'/></TD>
						<TD align="left" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:text name="<%=frmName%>" property='<%= "bannerSectionSectionIds[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(8000 + ctr.intValue())%>" maxlength="5" size="5"/></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><bean:write name="<%=frmName%>" property='<%= "bannerSectionSectionIds[" + ctr + "]" %>'/><html:hidden property='<%= "bannerSectionSectionIds[" + ctr + "]" %>'/></logic:equal></TD>
						<TD align="left" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:select style="width:200;" property='<%= "consents[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(10000 + ctr.intValue())%>"><html:option value="-1">No Consent Required</html:option><html:options collection='<%=OfferingConsentType.CONSENT_TYPE_ATTR_NAME%>' property="uniqueId" labelProperty="label" /></html:select></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><logic:iterate scope="request" name="<%=OfferingConsentType.CONSENT_TYPE_ATTR_NAME%>" id="cnst"><logic:equal name="<%=frmName%>" property='<%= "consents[" + ctr + "]" %>' value="<%=((OfferingConsentType)cnst).getUniqueId().toString()%>"><bean:write name="cnst" property="label" /></logic:equal></logic:iterate><html:hidden property='<%= "consents[" + ctr + "]" %>'/></logic:equal></TD>
						<TD align="left" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:text name="<%=frmName%>" property='<%= "courseCreditOverrides[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(8000 + ctr.intValue())%>" maxlength="10" size="10"/></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><bean:write name="<%=frmName%>" property='<%= "courseCreditOverrides[" + ctr + "]" %>'/><html:hidden property='<%= "courseCreditOverrides[" + ctr + "]" %>'/></logic:equal></TD>
						<TD>&nbsp;</TD>
						<TD align="right" valign="top" nowrap><%=frm.getCourseCredits().get(ctr)%>
						<html:hidden property='<%= "courseCredits[" + ctr + "]" %>'/></TD>
<logic:equal name="<%=frmName %>" property='showLimitOverride' value="true">
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="false" ><html:text name="<%=frmName%>" property='<%= "limitOverrides[" + ctr + "]" %>' tabindex="<%=java.lang.Integer.toString(8000 + ctr.intValue())%>" maxlength="5" size="5"/></logic:equal><logic:equal name="<%=frmName%>" property='<%= "readOnlyClasses[" + ctr + "]" %>' value="true" ><bean:write name="<%=frmName%>" property='<%= "limitOverrides[" + ctr + "]" %>'/><html:hidden property='<%= "limitOverrides[" + ctr + "]" %>'/></logic:equal></TD>	
						<TD align="right" valign="top" nowrap><%=frm.getClassLimits().get(ctr)%>
						<html:hidden property='<%= "classLimits[" + ctr + "]" %>'/></TD>
</logic:equal>
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap><%=frm.getDatePatterns().get(ctr)%></TD>
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap><%=frm.getTimes().get(ctr)%></TD>
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap><%=frm.getRooms().get(ctr)%></TD>
						<TD>&nbsp;</TD>
						<TD align="left" valign="top" nowrap><%=frm.getInstructors().get(ctr)%></TD>						
					</TR>
					</logic:iterate>
				</TABLE>
			</TD>
		</TR>

		
		
<SCRIPT language="javascript">
	<!--		
			document.forms[0].elements["op"][0].disabled="";	
	// -->
	</SCRIPT>

<!-- Buttons -->
<SCRIPT language="javascript">
	<!--		
			document.forms[0].elements["op"][0].disabled="";	
	// -->
</SCRIPT>
		<TR>
			<TD colspan="2" valign="middle">
				<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>

		<TR>
			<TD colspan="2" align="right">
				<html:submit property="op" 
					styleClass="btn" accesskey="U" titleKey="title.updateInstructionalOfferingConfig">
					<bean:message key="button.updateInstructionalOfferingConfig" />
				</html:submit>
						 
				<html:button property="op" 
					styleClass="btn" accesskey="B" titleKey="title.backToInstrOffrDetail" 
					onclick="document.location.href='bannerOfferingDetail.do?op=view&bc=${bannerCourseOfferingId}';">
					<bean:message key="button.backToInstrOffrDetail" />
				</html:button>
					
			</TD>
		</TR>

	</TABLE>
</html:form>
	<script language="javascript">displayElement('loading', false);</script>

