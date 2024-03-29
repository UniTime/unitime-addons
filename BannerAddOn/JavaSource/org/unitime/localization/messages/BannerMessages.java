/*
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
*/
package org.unitime.localization.messages;

public interface BannerMessages extends Messages {
	@DefaultMessage("Search")
	String actionSearchBannerOfferings();

	@DefaultMessage("Banner Offering data was not correct:  {0}")
	String missingBannerCourseOfferingId(Long bannerCourseOfferingId);
	
	@DefaultMessage("Banner Session")
	String rollForwardBannerSession();
	
	@DefaultMessage("Banner Session Data")
	String rollForwardBannerSessionData();
	
	@DefaultMessage("Create Missing Banner Section Data")
	String rollForwardCreateMissingBannerSectionData();
	
	@DefaultMessage("Failed to roll banner session data forward: {0}")
	String errorFailedToRollBannerSessionData(String error);
	
	@DefaultMessage("Failed to create missing Banner sections: {0}")
	String errorFailedToCreateMissingBannerSections(String error);
	
	@DefaultMessage("Failed to perform custom roll banner session data forward action: {0}")
	String errorFailedToRollForwardCustomData(String error);
	
	@DefaultMessage("Roll Banner Session Data Forward From Session:")
	String propRollBannerSessionDataFrom();
	
	@DefaultMessage("Create Missing Banner Sections.")
	String checkCreateMissingBannerSections();
	
	@DefaultMessage("Banner Offerings")
	String sectBannerOfferings();
	
	@DefaultMessage("S")
	String accessSearchBannerOfferings();
	
	@DefaultMessage("Search/Display Banner Offerings (Alt+{0})")
	String titleSearchBannerOfferings(String accessKey);
	
	@DefaultMessage("Courses Not Offered")
	String labelCoursesNotOffered();
	
	@DefaultMessage("Offered Courses")
	String labelOfferedCourses();
	
	@DefaultMessage("There are no courses currently offered for this subject.")
	String infoNoCoursesOffered();
	
	@DefaultMessage("All courses are currently being offered for this subject.")
	String infoAllCoursesOffered();
	
	@DefaultMessage("Print Indicator.")
	String titlePrintIndicator();
	
	@DefaultMessage("Section is gradable.")
	String titleGradableSection();
	
	@DefaultMessage("Instr Type")
	String colInstrType();
	
	@DefaultMessage("Sec Id")
	String colSecId();
	
	@DefaultMessage("Grade")
	String colGradable();
	
	@DefaultMessage("Lab Hours")
	String colLabHours();
	
	@DefaultMessage("Print")
	String colPrint();
	
	@DefaultMessage("Xlst")
	String colXlst();
	
	@DefaultMessage("Link Id")
	String colLinkId();
	
	@DefaultMessage("Link Conn")
	String colLinkConn();
	
	@DefaultMessage("Instr. Method")
	String colInstrMethod();
	
	@DefaultMessage("Class Label")
	String colClassLabel();
	
	@DefaultMessage("LMS Code")
	String colLMSCode();
	
	@DefaultMessage("Restrictions")
	String colRestrictions();
	
	@DefaultMessage("Resend to Banner")
	String actionResendToBanner();
	
	@DefaultMessage("Banner Offering")
	String sectBannerOffering();
	
	@DefaultMessage("Back")
	String actionBackToBannerOfferings();
	
	@DefaultMessage("I")
	String accessBackToBannerOfferings();

	@DefaultMessage("Back to Banner Course Offering List (Alt + {0})")
	String titleBackToBannerOfferings(String access);
	
	@DefaultMessage("Edit")
	String actionEditBannerConfig();
	
	@DefaultMessage("Edit Banner Configuration")
	String titleEditBannerConfig();
	
	@DefaultMessage("Update")
	String actionUpdateBannerConfig();
	
	@DefaultMessage("U")
	String accessUpdateBannerConfig();
	
	@DefaultMessage("Update Banner Configuration (Alt + {0})")
	String titleUpdateBannerConfig(String action);
	
	@DefaultMessage("Back")
	String actionBackToBannerOfferingDetail();
	
	@DefaultMessage("B")
	String accessBackToBannerOfferingDetail();
	
	@DefaultMessage("Back to Banner Offering (Alt + {0})")
	String titleBackToBannerOfferingDetail(String access);
	
	@DefaultMessage("Instructional Offering Config")
	String labelInstructionalOfferingConfig();
	
	@DefaultMessage("Section Index must be set: {0}")
	String errorSectionIndexMustBeSet(String section);
	
	@DefaultMessage("Section Index can only consist of numbers or alpha characters: {0}")
	String errorSectionIndexNumbersAndLetters(String section);
	
	@DefaultMessage("Section Index cannot be matched to regular expression {0} for: {1}, reason: {2}")
	String errorSectionIndexDoesNotMatchExpression(String regexp, String section, String error);
	
	@DefaultMessage("Section Index must be unique for: {0}")
	String errorSectionIndexNotUnique(String section);
	
	@DefaultMessage("Section Index must be less than 999: {0}")
	String errorSectionIndex999(String section);
	
	@DefaultMessage("New Section Index must be unique for: {0}")
	String errorSectionNewIndexNotUnique(String section);
	
	@DefaultMessage("The limit override must be an integer number: {0}")
	String errorLimitOverrideNotANumber(String section);
	
	@DefaultMessage("The limit override cannot get greater than the class limit: {0}")
	String errorLimitOverrideOverClassLimit(String section);
	
	@DefaultMessage("The limit override must be greater than or equal to 0: {0}")
	String errorLimitOverrideBelowZero(String section);
	
	@DefaultMessage("The course credit override must be a number: {0}")
	String errorCourseCreditOverrideNotNumber(String section);
	
	@DefaultMessage("The course credit override must be greater than or equal to 0: {0}")
	String errorCourseCreditOverrideBelowZero(String section);
	
	@DefaultMessage("Configuration Gradable Itype:")
	String propConfigGradableItype();
	
	@DefaultMessage("No Itype")
	String itemNoItype();
	
	@DefaultMessage("Lab Hours:")
	String propLabHours();
	
	@DefaultMessage("Section&nbsp;Id")
	String colSectionId();
	
	@DefaultMessage("Course Credit<br>Override")
	String colCourseCreditOverride();
	
	@DefaultMessage("Limit<br>Override")
	String colLimitOverride();
	
	@DefaultMessage("Campus&nbsp;Override")
	String colCampusOverride();
	
	@DefaultMessage("Default ({0})")
	String defaultCampusOverride(String defaultCampusOverride);
		
	@DefaultMessage("Banner Campus Code")
	String colBannerCampusCode();
	
	@DefaultMessage("Banner Campus Name")
	String colBannerCampusName();
	
	@DefaultMessage("Visible")
	String colBannerCampusVisible();
	
	@DefaultMessage("Add Campus Override")
	String actionAddCampusOverride();
	
	@DefaultMessage("Save")
	String actionSaveCampusOverride();
	
	@DefaultMessage("Update")
	String actionUpdateCampusOverride();
	
	@DefaultMessage("Back")
	String actionBackToCampusOverrides();
	
	@DefaultMessage("Banner Term Code")
	String colBannerTermCode();
	
	@DefaultMessage("Banner Sessions")
	String colBannerSessions();
	
	@DefaultMessage("Last CRN")
	String colLastCRN();
	
	@DefaultMessage("Minimum CRN")
	String colMinimumCRN();
	
	@DefaultMessage("Maximum CRN")
	String colMaximumCRN();
	
	@DefaultMessage("Search Flag")
	String colSearchFlag();
	
	@DefaultMessage("Add Session")
	String actionAddBannerSession();
	
	@DefaultMessage("Update")
	String actionUpdateBannerSession();
	
	@DefaultMessage("Save")
	String actionSaveBannerSession();
	
	@DefaultMessage("Back")
	String actionBackToBannerSessions();
	
	@DefaultMessage("Banner Campus")
	String colBannerCampus();
	
	@DefaultMessage("Store Data For Banner")
	String colStoreDataForBanner();
	
	@DefaultMessage("Send Data To Banner")
	String colSendDataToBanner();
	
	@DefaultMessage("Loading Offerings File")
	String colLoadingOfferings();
	
	@DefaultMessage("Future Term")
	String colFutureTerm();
	
	@DefaultMessage("Update Mode")
	String colUpdateMode();
	
	@DefaultMessage("Student Campus")
	String colStudentCampus();
	
	@DefaultMessage("Use Subject Area Prefix As Campus")
	String colUseStudentAreaPrefix();
	
	@DefaultMessage("Subject Area Prefix Delimiter")
	String colSubjectAreaPrefixDelim();
	
	@DefaultMessage("A banner session for the academic session already exists")
	String errorBannerSessionAlreadyExists();
	
	@DefaultMessage("Another banner session for the same academic session already exists")
	String errorAnoterBannerSessionAlreadyExists();
	
	@DefaultMessage("Banner Term Properties for the banner term code already exist for one or more selected Banner Sessions")
	String errorBannerTermPropertiesAlreadyExistsForSelectedSession();
	
	@DefaultMessage("Banner Term Properties for the banner term code and banner session combination already exists")
	String errorBannerTermPropertiesAlreadyForTermAndSessionCombo();
	
	@DefaultMessage("Another Banner Term Properties for the same banner term code and banner session combination already exists")
	String errorAnoterBannerTermPropertiesAlreadyExists();
	
	@DefaultMessage("Banner Term Code ({0}) for the BannerSession: {1} does not match the selected Term Code({2})")
	String errorBannerTermCodeDoesNotMatch(String code, String session, String selected);
	
	@DefaultMessage("<b>Note:</b> Do not make changes to this field unless recovering from a failed banner offerings XML load.")
	String noteLoadingOfferings();
	
	@DefaultMessage("<b>Note:</b> May contain a regular expression that the student campus code must match.")
	String noteStudentCampus();
	
	@DefaultMessage("<b>Default:</b> \" - \"")
	String noteSubjectAreaPrefixDelim();
	
	@DefaultMessage("Disabled")
	String nameUpdateModeDisabled();
	
	@DefaultMessage("Direct Update")
	String nameUpdateModeDirect();
	
	@DefaultMessage("Send Request")
	String nameUpdateModeRequest();
	
	@DefaultMessage("Disabled (no automatic future term updates)")
	String descUpdateModeDisabled();
	
	@DefaultMessage("Direct Update (student changes automatically propagated into the future term)")
	String descUpdateModeDirect();
	
	@DefaultMessage("Send Request (when student changed, automatically request future term student update)")
	String descUpdateModeRequest();
	
	@DefaultMessage("Banner Responses")
	String sectBannerResponses();
	
	@DefaultMessage("CRN")
	String colCRN();
	
	@DefaultMessage("Action")
	String colAction();
	
	@DefaultMessage("Type")
	String colType();
	
	@DefaultMessage("Message")
	String colMessage();
	
	@DefaultMessage("Audit")
	String actionAudit();
	
	@DefaultMessage("Update")
	String actionUpdate();
	
	@DefaultMessage("Delete")
	String actionDelete();
	
	@DefaultMessage("Success")
	String typeSuccess();
	
	@DefaultMessage("Error")
	String typeError();
	
	@DefaultMessage("Warning")
	String typeWarning();
	
	@DefaultMessage("Cross-List ID")
	String colCrossListId();
	
	@DefaultMessage("Start Date")
	String colStartDate();
	
	@DefaultMessage("Stop Date")
	String colStopDate();
	
	@DefaultMessage("Show History:")
	String filterShowHistory();
	
	@DefaultMessage("Number of Messages:")
	String filterNumberOfMessages();
}

