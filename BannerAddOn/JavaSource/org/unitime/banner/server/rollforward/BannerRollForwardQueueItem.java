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
import java.util.List;

import org.unitime.banner.util.BannerSessionRollForward;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.timetable.gwt.shared.BannerRollForwardSessionInterface;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardErrorLogger;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.util.queue.RollForwardQueueItem;

public class BannerRollForwardQueueItem extends RollForwardQueueItem {
	private static final long serialVersionUID = -1560347886648798200L;
	protected static final BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	public BannerRollForwardQueueItem(Session session, UserContext owner, BannerRollForwardSessionInterface form) {
		super(session, owner, form);
	}
	
	public BannerRollForwardSessionInterface getBannerForm() { return (BannerRollForwardSessionInterface)iForm; }
	
	@Override
	protected void execute() throws Exception {
		RollForwardErrorLogger logger = new RollForwardErrorLogger() {
			@Override
			public void addFieldError(String type, String message) {
				iErrors.addFieldError(type, message);
				error(message);
			}
			public boolean isEmpty() {
				return iErrors.isEmpty();
			}
		};
		BannerSessionRollForward sessionRollForward = new BannerSessionRollForward(this);
		BannerRollForwardSessionValidators validator = new BannerRollForwardSessionValidators(getBannerForm(), logger);
        
        Session toAcadSession = Session.getSessionById(getBannerForm().getSessionToRollForwardTo());
		if (toAcadSession == null){
			logger.addFieldError("mustSelectSession", MSG.errorRollForwardMissingToSession());
		}
    	if (logger.isEmpty() && getBannerForm().getRollForwardBannerSession()) {
			setStatus(BMSG.rollForwardBannerSessionData() + " ...");
			if (validator.validateRollForwardBannerSession(toAcadSession))
				sessionRollForward.rollBannerSessionDataForward(logger, getBannerForm());	
        }
    	iProgress++;

    	if (logger.isEmpty() && getBannerForm().getCreateMissingBannerSections()) {
				setStatus(BMSG.rollForwardCreateMissingBannerSectionData() + " ...");
				sessionRollForward.createMissingBannerSections(logger, getBannerForm());	
	        }
	
        iProgress++;

        if (!iErrors.isEmpty()) {
        	String lastError = iErrors.get(iErrors.size() - 1).getMessage();
        	setError(new Exception(lastError));
        } else {
        	log(MSG.logAllDone());
        }
	}

	@Override
	public String name() {
		List<String> names = new ArrayList<String>();
    	if (getBannerForm().getRollForwardBannerSession()) names.add(BMSG.rollForwardBannerSession());
     	if (getBannerForm().getCreateMissingBannerSections()) names.add(BMSG.rollForwardCreateMissingBannerSectionData());
    	String name = names.toString().replace("[", "").replace("]", "");
    	if (name.length() > 50) name = name.substring(0, 47) + "...";
    	return name;
	}

	@Override
	public double progress() {
		return 100 * iProgress / ((getBannerForm().getRollForwardBannerSession()?1:0) + (getBannerForm().getCreateMissingBannerSections()?1:0));
	}

}
