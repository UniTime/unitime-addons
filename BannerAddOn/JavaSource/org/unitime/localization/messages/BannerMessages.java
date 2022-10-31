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
	
	@DefaultMessage("Sec&nbsp;Id")
	String colSecId();
	
	@DefaultMessage("Grade")
	String colGradable();
	
	@DefaultMessage("Lab Hours")
	String colLabHours();
	
	@DefaultMessage("Print")
	String colPrint();
	
	@DefaultMessage("Xlst")
	String colXlst();
	
	@DefaultMessage("Link&nbsp;Id")
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
}
