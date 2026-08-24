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
package org.unitime.timetable.gwt.resources;

import com.google.gwt.i18n.client.Messages.DefaultMessage;

public interface BannerGwtMessages extends GwtMessages {

	@DefaultMessage("Banner Instructional Method Cohort Restriction")
	String pageBannerInstrMethodCohortRestriction();
	
	@DefaultMessage("Banner Instructional Method Cohort Restrictions")
	String pageBannerInstrMethodCohortRestrictions();

	@DefaultMessage("Banner Message Responses")
	String pageBannerResponses();
	
	@DefaultMessage("Banner Offerings")
	String pageBannerOfferings();
	
	@DefaultMessage("Banner Offering Detail")
	String pageBannerOfferingDetail();
	
	@DefaultMessage("Banner Offering Edit")
	String pageBannerOfferingEdit();
	
	@DefaultMessage("Course Catalog")
	String pageBannerCourseCatalog();

	@DefaultMessage("Instructional Method")
	String fieldInstructionalMethod();
	
	@DefaultMessage("Cohort")
	String fieldCohort();

	@DefaultMessage("Restriction Action")
	String fieldRestrictionAction();

	@DefaultMessage("Remove(d)")
	String fieldRemoved();
	
	@DefaultMessage("All Managers")
	String itemAllManagers();

	@DefaultMessage("Include")
	String labelInclude();

	@DefaultMessage("Exclude")
	String labelExclude();
	
	@DefaultMessage("Removed")
	String labelRestrictionRemoved();

	@DefaultMessage("The 'COHORT' Student Group Type is not defined.  This page is unusable with the 'COHORT' Student Group Type.")
	String exceptionNoCohortStudentGroupTypeDefined();

	@DefaultMessage("If multiple Instructional Method Cohort Restrictions exist for an Academic Session, all Restriction must be unique for a Session, Instructional Method and Cohort.")
	String exceptionRestrictionMustBeUnique();

	@DefaultMessage("If multiple not removed Instructional Method Cohort Restrictions exist for an Academic Session, all Restriction Actions must match.  To change a Restriction Action when multiple exist, mark all restrictions for the same Instructional Method as Removed, change the Restriction Action for all restrictions, Update, change all restrictions back to not Removed and update again.")
	String exceptionMustHaveSameRestrictionAction();

	@DefaultMessage("Cannot change the Instructional Method for an Instructional Method Cohort Restriction")
	String exceptionRestrictionCannotChangeInstrMethod();
	
	@DefaultMessage("Cannot change the Cohort for an Instructional Method Cohort Restriction")
	String exceptionRestrictionCannotChangeCohort();

	@DefaultMessage("manager")
	String tagManager();
	
	@DefaultMessage("crn")
	String tagCrn();

	@DefaultMessage("Loading banner responses ...")
	String waitLoadingBannerResponses();
	
	@DefaultMessage("Failed to load banner responses: {0}")
	String failedToLoadBannerResponses(String reason);

	@DefaultMessage("No banner responses matching the above filter found.")
	String errorNoMatchingBannerResponsesFound();

	@DefaultMessage("Sec Id")
	String colBannerSectionNumber();

	@DefaultMessage("Activity Date")
	String colActivityDate();

	@DefaultMessage("Action")
	String colAction();

	@DefaultMessage("Campus")
	String colCampus();

	@DefaultMessage("CRN")
	String colCrn();

	@DefaultMessage("Xlst")
	String colCrosslistGroup();

	@DefaultMessage("Message")
	String colMessage();

	@DefaultMessage("Type")
	String colType();

	@DefaultMessage("Loading banner responses ...")
	String waitLoadingBannerQueueResponses();
	
	@DefaultMessage("Activity Date Not Set.")
	String warnActivityDateNotSet();

	@DefaultMessage("CRN:")
	String propCrn();
	
	@DefaultMessage("Course Number:")
	String propCourseNumber();

	@DefaultMessage("Course Number")
	String fieldCourseNumber();

	@DefaultMessage("crsNbr")
	String tagCourseNumber();
	
	@DefaultMessage("Message:")
	String propMessage();

	@DefaultMessage("xlst")
	String tagXlst();

	@DefaultMessage("Cross List Group")
	String fieldXlst();

	@DefaultMessage("Cross List Group:")
	String propXlst();

	@DefaultMessage("message")
	String tagMessage();

	@DefaultMessage("action")
	String tagAction();

	@DefaultMessage("respType")
	String tagResponseType();
	
	@DefaultMessage("Max Results:")
	String propMaxResults();
	
	@DefaultMessage("Max Results")
	String fieldMaxResults();

	@DefaultMessage("maxResults")
	String tagMaxResults();

	@DefaultMessage("Add Banner Campus Override")
	String pageAddBannerCampusOverride();

	@DefaultMessage("Banner Campus Override")
	String pageBannerCampusOverride();

	@DefaultMessage("Banner Campus Overrides")
	String pageBannerCampusOverrides();

	@DefaultMessage("Banner Campus Code")
	String colBannerCampusCode();
	
	@DefaultMessage("Banner Campus Name")
	String colBannerCampusName();
	
	@DefaultMessage("Visible")
	String colBannerCampusVisible();
	
	@DefaultMessage("First Banner <br>Term Code")
	String colFirstBannerTermCode();
	
	@DefaultMessage("Last Banner <br>Term Code")
	String colLastBannerTermCode();
		
	@DefaultMessage("Visible On <br>Banner Offering <br>Page")
	String colBannerCampusVisibleOnBannerOfferingPage();
	
	@DefaultMessage("Used In <br>Campus Code <br>Calculation")
	String colUsedCampusCodeCalc();
	
	@DefaultMessage("Override <br>Calculated <br>Campus Code")
	String colOverrideCalcCampusCode();
	
	@DefaultMessage("Regular Expression <br>Academic Initiative")
	String colRegexAcademicInitiative();

	@DefaultMessage("Regular Expression <br>Managing Dept Code")
	String colRegexManagingDeptCode();

	@DefaultMessage("Regular Expression <br>Code to Override")
	String colRegexCampusCodeToOverride();

	@DefaultMessage("First Banner Term Code")
	String fieldFirstBannerTermCode();
	
	@DefaultMessage("Last Banner Term Code")
	String fieldLastBannerTermCode();
		
	@DefaultMessage("Visible On Banner Offering Page")
	String fieldBannerCampusVisibleOnBannerOfferingPage();
	
	@DefaultMessage("Used In Campus Code Calculation")
	String fieldUsedCampusCodeCalc();
	
	@DefaultMessage("Override Calculated Campus Code")
	String fieldOverrideCalcCampusCode();
	
	@DefaultMessage("Regular Expression Academic Initiative")
	String fieldRegexAcademicInitiative();

	@DefaultMessage("Regular Expression Managing Dept Code")
	String fieldRegexManagingDeptCode();

	@DefaultMessage("Regular Expression Code to Override")
	String fieldRegexCampusCodeToOverride();
	
	@DefaultMessage("Add")
	String buttonAddBannerCampusOverride();
	
	@DefaultMessage("Edit")
	String buttonEditBannerCampusOverride();
	
	@DefaultMessage("bannerCampusOverride")
	String objectBannerCampusOverride();

	@DefaultMessage("Add Campus Override")
	String actionAddCampusOverride();
	
	@DefaultMessage("Save")
	String actionSaveCampusOverride();
	
	@DefaultMessage("Update")
	String actionUpdateCampusOverride();
	
	@DefaultMessage("Back")
	String actionBackToCampusOverrides();
	
	@DefaultMessage("Banner Campus Overrides")
	String sectBannerCampusOverrides();

	@DefaultMessage("Add Banner Campus Overrides")
	String sectAddBannerCampusOverrides();

	@DefaultMessage("Add Banner Campus Override")
	String sectAddBannerCampusOverride();

	@DefaultMessage("Edit Banner Campus Override")
	String sectEditBannerCampusOverride();

	@DefaultMessage("Update Data")
	String buttonBannerCampusOverridesUpdateData();

	@DefaultMessage("Banner Campus Code is required.")
	String errorBannerCampusCodeIsEmpty();

	@DefaultMessage("Banner Campus Name is required.")
	String errorBannerCampusNameIsEmpty();

}
