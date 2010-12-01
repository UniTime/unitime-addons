/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
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

	public void validateSessionToRollForwardTo(ActionErrors errors){

		Session s = Session.getSessionById(getSessionToRollForwardTo());
		if (s == null){
   			errors.add("mustSelectSession", new ActionMessage("errors.rollForward.missingToSession"));
   			return;
		}
		
		if (getRollForwardBannerSession().booleanValue()){
			ArrayList<BannerSession> list = new ArrayList<BannerSession>();
			BannerSession bs = BannerSession.findBannerSessionForSession(s, null);
			if (bs != null){
				list.add(bs);
			}
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

		rollForwardBannerSession = new Boolean(false);
		sessionToRollBannerDataForwardFrom = null;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		init();
	}
	
	
}
