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

public interface ColleagueMessages extends Messages {

	@DefaultMessage("Assign Restrictions")
	String actionAssignRestrictions();

	@DefaultMessage("Unassign All")
	String actionUnassignAllRestrictionsFromConfig();

	@DefaultMessage("U")
	String accessUpdateSectionRestrictionAssignment();

	@DefaultMessage("Search")
	String actionSearchColleagueOfferings();
	
	@DefaultMessage("S")
	String accessSearchColleagueOfferings();
	
	@DefaultMessage("Search/Display Colleague Offerings (Alt+{0})")
	String titleSearchColleagueOfferings(String action);

	@DefaultMessage("Update")
	String actionUpdateSectionRestrictionAssignment();
	
	@DefaultMessage("Add")
	String altAdd();

	@DefaultMessage("Colleague Synonym")
	String columnColleagueSynonym();

	@DefaultMessage("Do you really want to unassign all restrictions?")
	String confirmUnassignAllRestrictions();

	@DefaultMessage("Duplicate restriction for section.")
	String errorDuplicateRestrictionForSection();
	

	@DefaultMessage("Course Offering data was not correct:  {0}")
	String missingCourseOfferingId(Long courseOfferingId);

	@DefaultMessage("Add Restriction to Section")
	String titleAddRestrictionToSection();

	@DefaultMessage("Delete Restriction from Section")
	String titleDeleteRestrictionFromSection();

	@DefaultMessage("Update Section Restrictions (Alt+{0})")
	String titleUpdateSectionRestrictionsAssignment(String accessKey);

	@DefaultMessage("Unassign All Restrictions")
	String titleUnassignAllRestrictionsFromConfig();

	@DefaultMessage("Edit Colleague Restrictions")
	String titleAssignRestrictions();

	@DefaultMessage("Colleague Session")
	String rollForwardColleagueSession();
	
	@DefaultMessage("Colleague Session Data")
	String rollForwardColleagueSessionData();
	
	@DefaultMessage("Failed to roll colleague session data forward.")
	String errorFailedToRollForwardColleagueSessionData();
	
	@DefaultMessage("Failed to perform custom roll colleague session data forward action: {0}")
	String errorFailedToRollForwardCustomData(String error);
	
	@DefaultMessage("Roll Colleague Session Data Forward From Session:")
	String propRollColleagueSessionDataFrom();
	
	@DefaultMessage("Colleague Offerings")
	String sectColleagueOfferings();
	
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
	
	@DefaultMessage("Instr Type")
	String colInstrType();
	
	@DefaultMessage("Sec&nbsp;Id")
	String colSecId();
	
	@DefaultMessage("Colleague Synonym")
	String colColleagueSynonym();
	
	@DefaultMessage("Sched Type")
	String colSchedType();
	
	@DefaultMessage("Num Weeks")
	String colNumWeeks();
	
	@DefaultMessage("Print")
	String colPrint();
	
	@DefaultMessage("Restrictions")
	String colRestrictions();
	
	@DefaultMessage("Class Label")
	String colClassLabel();
	
	@DefaultMessage("Resend to Colleague")
	String actionResendToColleague();
	
	@DefaultMessage("Colleague Offering")
	String sectColleagueOffering();
	
	@DefaultMessage("Back")
	String actionBackToColleagueOfferings();
	
	@DefaultMessage("I")
	String accessBackToColleagueOfferings();
	
	@DefaultMessage("Back to Colleague Course Offering List (Alt + {0})")
	String titleBackToColleagueOfferings(String access);
	
}
