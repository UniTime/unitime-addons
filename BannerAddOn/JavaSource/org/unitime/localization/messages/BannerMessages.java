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
	String missingBannerCourseOfferingId(String bannerCourseOfferingId);

	
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
}
