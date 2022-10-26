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
package org.unitime.colleague.form;

import java.util.ArrayList;

import org.unitime.colleague.model.ColleagueSession;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.timetable.action.RollForwardSessionAction.RollForwardErrors;
import org.unitime.timetable.form.RollForwardSessionForm;
import org.unitime.timetable.model.Session;


/**
 * @author says
 *
 */
public class RollForwardColleagueSessionForm extends RollForwardSessionForm  {
	private static final long serialVersionUID = -5708531647077677270L;
	protected static final ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);

	private Long sessionToRollColleagueDataForwardFrom;
	private Boolean rollForwardColleagueSession;
	
	public RollForwardColleagueSessionForm() {
		super();
	}

	public Long getSessionToRollColleagueDataForwardFrom() {
		return sessionToRollColleagueDataForwardFrom;
	}

	public void setSessionToRollColleagueDataForwardFrom(
			Long sessionToRollColleagueDataForwardFrom) {
		this.sessionToRollColleagueDataForwardFrom = sessionToRollColleagueDataForwardFrom;
	}

	public void validateSessionToRollForwardTo(RollForwardErrors errors){

		Session s = Session.getSessionById(getSessionToRollForwardTo());
		if (s == null){
			errors.addFieldError("mustSelectSession", MSG.errorRollForwardMissingToSession());
   			return;
		}
		
		if (getRollForwardColleagueSession().booleanValue()){
			ArrayList<ColleagueSession> list = new ArrayList<ColleagueSession>();
			validateRollForward(errors, s, getSessionToRollColleagueDataForwardFrom(), CMSG.rollForwardColleagueSession(), list);			
 		}
	
	}

	public Boolean getRollForwardColleagueSession() {
		return rollForwardColleagueSession;
	}

	public void setRollForwardColleagueSession(Boolean rollForwardColleagueSession) {
		this.rollForwardColleagueSession = rollForwardColleagueSession;
	}

	@Override
	public void reset() {
		super.reset();
		rollForwardColleagueSession = Boolean.valueOf(false);
		sessionToRollColleagueDataForwardFrom = null;
	}
	
	public void copyTo(RollForwardColleagueSessionForm form) {
		form.setButtonAction(getButtonAction());
		form.setToSessions(getToSessions());
		form.setFromSessions(getFromSessions());
		form.setSessionToRollForwardTo(getSessionToRollForwardTo());
		form.setRollForwardColleagueSession(getRollForwardColleagueSession());
		form.setSessionToRollColleagueDataForwardFrom(getSessionToRollColleagueDataForwardFrom());
	}
	
	public Object clone() {
		RollForwardColleagueSessionForm form = new RollForwardColleagueSessionForm();
		copyTo(form);
		return form;
	}

	
}
