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
<loc:bundle name="BannerMessages" id="BMSG"><s:set var="bmsg" value="#attr.BMSG"/>
<s:form action="bannerSessionEdit">
<table class="unitime-MainTable">
	<s:hidden name="form.sessionId" />
	<TR>
		<TD colspan="2">
			<tt:section-header>
				<tt:section-title>
				</tt:section-title>
				<s:if test="form.sessionId == null">
					<s:submit name="op" value="%{#bmsg.actionSaveBannerSession()}"/>
				</s:if><s:else>
					<s:submit name="op" value="%{#bmsg.actionUpdateBannerSession()}"/>
				</s:else>
				<s:submit name="op" value="%{#bmsg.actionBackToBannerSessions()}"/>
			</tt:section-header>			
		</TD>
	</TR>
		
	<s:if test="!fieldErrors.isEmpty()">
		<TR><TD colspan="2" align="left" class="errorTable">
			<div class='errorHeader'><loc:message name="formValidationErrors"/></div><s:fielderror/>
		</TD></TR>
	</s:if>		
	
		<TR>
			<TD><loc:message name="columnAcademicSession"/>:</TD>
			<TD>
				<s:if test="form.sessionId == null">
					<s:select name="form.acadSessionId" style="min-width: 200px;"
						list="form.availableAcadSessions" listKey="uniqueId" listValue="label"/>
				</s:if><s:else>
					<s:hidden name="form.acadSessionLabel"/>
					<s:hidden name="form.acadSessionId"/>
					<s:property value="form.acadSessionLabel"/>
				</s:else>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="colBannerTermCode" id="BMSG"/>:</TD>
			<TD>
				<s:textfield name="form.bannerTermCode" maxlength="20" size="20"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="colBannerCampus" id="BMSG"/>:</TD>
			<TD>
				<s:textfield name="form.bannerCampus"  maxlength="20" size="20"/>
			</TD>
		</TR>

		<TR>
			<TD><loc:message name="colStoreDataForBanner" id="BMSG"/>:</TD>
			<TD align="left">
				<s:checkbox name="form.storeDataForBanner"/>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="colSendDataToBanner" id="BMSG"/>:</TD>
			<TD align="left">
				<s:checkbox name="form.sendDataToBanner"/>
			</TD>
		</TR>
		<s:if test="form.loadingOfferingsFile == true || form.sessionId == null">
		<TR>
			<TD valign="top"><loc:message name="colLoadingOfferings" id="BMSG"/>:</TD>
			<TD align="left">
				<s:checkbox name="form.loadingOfferingsFile"/><font color="red">&nbsp;&nbsp;&nbsp;&nbsp;<loc:message name="noteLoadingOfferings" id="BMSG"/></font>
			</TD>
		</TR>
		</s:if><s:else>
			<s:hidden name="form.loadingOfferingsFile"/>
		</s:else>
		<TR>
			<TD><loc:message name="colFutureTerm" id="BMSG"/>:</TD>
			<TD align="left">
				<s:select name="form.futureSessionId" style="min-width: 200px;" 
					list="form.availableBannerSessions" listKey="uniqueId" listValue="label"
					headerKey="" headerValue="-"/>
			</TD>
		</TR>
		
		<TR>
			<TD><loc:message name="colUpdateMode" id="BMSG"/>:</TD>
			<TD align="left">
				<s:select name="form.futureUpdateMode" style="min-width: 200px;"
					list="form.futureUpdateModes" listKey="id" listValue="value"/>
			</TD>
		</TR>
		
		<TR>
			<TD valign="top"><loc:message name="colStudentCampus" id="BMSG"/>:</TD>
			<TD>
				<s:textfield name="form.studentCampus"  maxlength="500" size="50"/><br>
				<font color="red">&nbsp;&nbsp;&nbsp;&nbsp;<loc:message name="noteStudentCampus" id="BMSG"/></font>
			</TD>
		</TR>
		<TR>
			<TD><loc:message name="colUseStudentAreaPrefix" id="BMSG"/>:</TD>
			<TD align="left">
				<s:checkbox name="form.useSubjectAreaPrefixAsCampus"/>
			</TD>
		</TR>
						
		<TR>
			<TD><loc:message name="colSubjectAreaPrefixDelim" id="BMSG"/>:</TD>
			<TD>
				<s:textfield name="form.subjectAreaPrefixDelimiter" maxlength="5" size="5"/>
				<font color="black">&nbsp;&nbsp;&nbsp;&nbsp;<loc:message name="noteSubjectAreaPrefixDelim" id="BMSG"/></font>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2">
			<DIV class="WelcomeRowHeadBlank">&nbsp;</DIV>
			</TD>
		</TR>
		
		<TR>
			<TD colspan="2" align="right">

				<s:if test="form.sessionId == null">
					<s:submit name="op" value="%{#bmsg.actionSaveBannerSession()}"/>
				</s:if><s:else>
					<s:submit name="op" value="%{#bmsg.actionUpdateBannerSession()}"/>
				</s:else>
				<s:submit name="op" value="%{#bmsg.actionBackToBannerSessions()}"/>
			
			</TD>
		</TR>
</table>
</s:form>
</loc:bundle>
</loc:bundle>