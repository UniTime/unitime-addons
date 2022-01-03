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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.form.RollForwardSessionForm;
import org.unitime.timetable.model.Session;


/**
 * @author says
 *
 */
public class RollForwardBannerSessionForm extends RollForwardSessionForm  {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7189756605666517891L;

	private Long sessionToRollBannerDataForwardFrom;
	private Boolean rollForwardBannerSession;
	private Boolean createMissingBannerSections;
	
	/**
	 * 
	 */
	public RollForwardBannerSessionForm() {
		// do nothing
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

	public void validateSessionToRollForwardTo(ActionErrors errors){

		Session s = Session.getSessionById(getSessionToRollForwardTo());
		if (s == null){
   			errors.add("mustSelectSession", new ActionMessage("errors.rollForward.missingToSession"));
   			return;
		}
		
		if (getRollForwardBannerSession().booleanValue()){
			ArrayList<BannerSession> list = new ArrayList<BannerSession>();
// If banner session exist we will just use it rather that insist it be created.  This is to allow the roll to be restarted if it fails.
//			BannerSession bs = BannerSession.findBannerSessionForSession(s, null);
//			if (bs != null){
//				list.add(bs);
//			}
			validateRollForward(errors, s, getSessionToRollBannerDataForwardFrom(), "Banner Session", list);			
 		}
	
	}

	public Boolean getRollForwardBannerSession() {
		return rollForwardBannerSession;
	}

	public void setRollForwardBannerSession(Boolean rollForwardBannerSession) {
		this.rollForwardBannerSession = rollForwardBannerSession;
	}
	
	public void init() {
		super.init();

		rollForwardBannerSession = Boolean.valueOf(false);
		createMissingBannerSections = Boolean.valueOf(false);
		sessionToRollBannerDataForwardFrom = null;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		init();
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
