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
package org.unitime.banner.form;

import java.util.ArrayList;

import org.unitime.banner.model.BannerSession;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.RollForwardSessionAction.RollForwardErrors;
import org.unitime.timetable.form.RollForwardSessionForm;
import org.unitime.timetable.model.Session;


/**
 * @author says
 *
 */
public class RollForwardBannerSessionForm extends RollForwardSessionForm  {
	private static final long serialVersionUID = -7189756605666517891L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final BannerMessages BMSG = Localization.create(BannerMessages.class);

	private Long sessionToRollBannerDataForwardFrom;
	private Boolean rollForwardBannerSession;
	private Boolean createMissingBannerSections;
	
	public RollForwardBannerSessionForm() {
		super();
	}

	public Long getSessionToRollBannerDataForwardFrom() {
		return sessionToRollBannerDataForwardFrom;
	}

	public void setSessionToRollBannerDataForwardFrom(
			Long sessionToRollBannerDataForwardFrom) {
		this.sessionToRollBannerDataForwardFrom = sessionToRollBannerDataForwardFrom;
	}

	public Boolean getCreateMissingBannerSections() {
		return createMissingBannerSections;
	}

	public void setCreateMissingBannerSections(Boolean createMissingBannerSections) {
		this.createMissingBannerSections = createMissingBannerSections;
	}

	public void validateSessionToRollForwardTo(RollForwardErrors action){

		Session s = Session.getSessionById(getSessionToRollForwardTo());
		if (s == null){
			action.addFieldError("mustSelectSession", MSG.errorRollForwardMissingToSession());
   			return;
		}
		
		if (getRollForwardBannerSession().booleanValue()){
			ArrayList<BannerSession> list = new ArrayList<BannerSession>();
			validateRollForward(action, s, getSessionToRollBannerDataForwardFrom(), BMSG.rollForwardBannerSession(), list);			
 		}
	
	}

	public Boolean getRollForwardBannerSession() {
		return rollForwardBannerSession;
	}

	public void setRollForwardBannerSession(Boolean rollForwardBannerSession) {
		this.rollForwardBannerSession = rollForwardBannerSession;
	}
	
	public void reset() {
		super.reset();
		rollForwardBannerSession = Boolean.valueOf(false);
		createMissingBannerSections = Boolean.valueOf(false);
		sessionToRollBannerDataForwardFrom = null;
	}
	
	public void copyTo(RollForwardBannerSessionForm form) {
		form.setButtonAction(getButtonAction());
		form.setToSessions(getToSessions());
		form.setFromSessions(getFromSessions());
		form.setSessionToRollForwardTo(getSessionToRollForwardTo());
		form.setRollForwardBannerSession(getRollForwardBannerSession());
		form.setSessionToRollBannerDataForwardFrom(getSessionToRollBannerDataForwardFrom());
		form.setCreateMissingBannerSections(getCreateMissingBannerSections());
	}
	
	public Object clone() {
		RollForwardBannerSessionForm form = new RollForwardBannerSessionForm();
		copyTo(form);
		return form;
	}

	
}
