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
package org.unitime.banner.server.rollforward;

import java.util.ArrayList;

import org.unitime.banner.model.BannerSession;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.timetable.gwt.shared.BannerRollForwardSessionInterface;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardErrorLogger;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.SessionRollForwardValidators;

public class BannerRollForwardSessionValidators extends SessionRollForwardValidators {
	protected static final BannerMessages BMSG = Localization.create(BannerMessages.class);

	public BannerRollForwardSessionValidators(BannerRollForwardSessionInterface form, RollForwardErrorLogger errors) {
		super(form, errors);
	}
	
	public BannerRollForwardSessionInterface getBannerForm() { return (BannerRollForwardSessionInterface) iForm; }
	
	public boolean validateRollForwardBannerSession(Session toAcadSession){
		if (getBannerForm().getRollForwardBannerSession()) {
			ArrayList<BannerSession> list = new ArrayList<BannerSession>();
			return validateRollForward(toAcadSession, getBannerForm().getSessionToRollBannerDataForwardFrom(), BMSG.rollForwardBannerSession(), list);
		} else {
			return true;
		}
	}

}
