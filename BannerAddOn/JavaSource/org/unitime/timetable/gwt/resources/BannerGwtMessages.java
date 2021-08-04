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

public interface BannerGwtMessages extends GwtMessages {

	@DefaultMessage("Banner Instructional Method Cohort Restriction")
	String pageBannerInstrMethodCohortRestriction();
	
	@DefaultMessage("Banner Instructional Method Cohort Restrictions")
	String pageBannerInstrMethodCohortRestrictions();

	@DefaultMessage("Banner Message Responses")
	String pageBannerResponses();

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

	

}
