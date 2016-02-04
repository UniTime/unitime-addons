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


public interface ColleagueGwtMessages extends GwtMessages {

	@DefaultMessage("Colleague Restriction")
	String pageColleagueRestriction();
	
	@DefaultMessage("Colleague Restrictions")
	String pageColleagueRestrictions();

	@DefaultMessage("Colleague Suffix Definitions")
	String pageColleagueSuffixDefs();

	@DefaultMessage("Colleague Suffix Definition")
	String pageColleagueSuffixDef();

	@DefaultMessage("Colleague Session")
	String pageColleagueSession();

	@DefaultMessage("Colleague Sessions")
	String pageColleagueSessions();

	@DefaultMessage("Instructional Method")
	String fieldInstructionalMethod();
	
	@DefaultMessage("Subject Area")
	String fieldSubjectArea();

	@DefaultMessage("Course Suffix")
	String fieldCourseSuffix();

	@DefaultMessage("Instructional Method Prefix")
	String fieldMethodPrefix();

	@DefaultMessage("Prefix")
	String fieldPrefix();

	@DefaultMessage("Suffix")
	String fieldSuffix();

	@DefaultMessage("Minimum Section Number")
	String fieldMinSectionNumber();
	
	@DefaultMessage("Maximum Section Number")
	String fieldMaxSectionNumber();

	@DefaultMessage("Colleague Location Code")
	String fieldLocationCode();

	@DefaultMessage("Colleague Term Code")
	String fieldTermCode();

	@DefaultMessage("Store Data for Colleague")
	String fieldStoreDataForColleague();

	@DefaultMessage("Send Data to Colleague")
	String fieldSendDataToColleague();

	@DefaultMessage("Loading Offerings File")
	String fieldLoadingOfferingsFile();



}
