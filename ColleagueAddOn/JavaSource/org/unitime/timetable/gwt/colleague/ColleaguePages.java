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
package org.unitime.timetable.gwt.colleague;

import org.unitime.timetable.gwt.resources.ColleagueGwtMessages;

import com.google.gwt.user.client.ui.Widget;

public enum ColleaguePages {
	colleagueOfferings(new PageFactory() {
		public Widget create() { return new ColleagueOfferingsPage(); }
		public String name(ColleagueGwtMessages messages) { return messages.pageColleagueOfferings(); }
		}),
	colleagueOffering(new PageFactory() {
		public Widget create() { return new ColleagueOfferingDetailPage(); }
		public String name(ColleagueGwtMessages messages) { return messages.pageColleagueOfferingDetail(); }
		}),
	sectionRestrictions(new PageFactory() {
		public Widget create() { return new SectionRestrictionsPage(); }
		public String name(ColleagueGwtMessages messages) { return messages.pageColleagueOfferingDetail(); }
		}),
	colleagueRollForward(new PageFactory() {
		public Widget create() { return new ColleagueRollForwardSessionPage(); }
		public String name(ColleagueGwtMessages messages) { return messages.pageRollForwardColleagueSession(); }
		}),
	colleagueMessageResponses(new PageFactory() {
		public Widget create() { return new ColleagueMessageResponsesPage(); }
		public String name(ColleagueGwtMessages messages) { return messages.pageColleagueMessageResponses(); }
		}),
	;

	private PageFactory iFactory;
	
	ColleaguePages(PageFactory factory) { iFactory = factory; }
	public String name(ColleagueGwtMessages messages) { return iFactory.name(messages); }
	public Widget widget() { return iFactory.create(); }
	
	public interface PageFactory {
		Widget create();
		String name(ColleagueGwtMessages messages);
	}
}
