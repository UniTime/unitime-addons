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
import java.util.List;

import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.BannerSession.FutureSessionUpdateMode;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.form.UniTimeForm;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.util.IdValue;

/**
 * 
 * @author says
 *
 */
public class BannerSessionEditForm implements UniTimeForm {
	private static final long serialVersionUID = 6616750809381469241L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);

	Long sessionId;
	Long acadSessionId;
	String acadSessionLabel;
	List<Session> availableAcadSessions;
	List<BannerSession> availableBannerSessions;
	String studentCampus;
	String bannerTermCode;
	String bannerCampus;
	Boolean storeDataForBanner;
	Boolean sendDataToBanner;
	Boolean loadingOfferingsFile;
	Long futureSessionId;
	Integer futureUpdateMode;
	Boolean useSubjectAreaPrefixAsCampus;
	String subjectAreaPrefixDelimiter;
	
	public BannerSessionEditForm() {
		reset();
	}
	
	@Override
	public void reset() {
		sessionId = null;
		acadSessionId = null;
		acadSessionLabel = null;
		availableAcadSessions = null;
		availableBannerSessions = null;
		studentCampus = null;
		bannerTermCode = null;
		bannerCampus = null;
		storeDataForBanner = null;
		sendDataToBanner = null;
		loadingOfferingsFile = null;
		futureSessionId = null;
		futureUpdateMode = null;
		useSubjectAreaPrefixAsCampus = null;
		subjectAreaPrefixDelimiter = null;
	}
	
	/**
	 * @return the availableAcadSessions
	 */
	public List<Session> getAvailableAcadSessions() {
		return availableAcadSessions;
	}

	/**
	 * @param availableAcadSessions the availableAcadSessions to set
	 */
	public void setAvailableAcadSessions(List<Session> availableAcadSessions) {
		this.availableAcadSessions = availableAcadSessions;
	}


	/**
	 * @return the acadSessionLabel
	 */
	public String getAcadSessionLabel() {
		return acadSessionLabel;
	}
	
	public void setAvailableBannerSessions(List<BannerSession> availableBannerSessions) { this.availableBannerSessions = availableBannerSessions; }
	public List<BannerSession> getAvailableBannerSessions() { return availableBannerSessions; }

	/**
	 * @param acadSessionLabel the acadSessionLabel to set
	 */
	public void setAcadSessionLabel(String acadSessionLabel) {
		this.acadSessionLabel = acadSessionLabel;
	}


	/**
	 * @return the acadSessionId
	 */
	public Long getAcadSessionId() {
		return acadSessionId;
	}


	/**
	 * @param acadSessionId the acadSessionId to set
	 */
	public void setAcadSessionId(Long acadSessionId) {
		this.acadSessionId = acadSessionId;
	}


	/**
	 * @return the bannerTermCode
	 */
	public String getBannerTermCode() {
		return bannerTermCode;
	}


	/**
	 * @param bannerTermCode the bannerTermCode to set
	 */
	public void setBannerTermCode(String bannerTermCode) {
		this.bannerTermCode = bannerTermCode;
	}


	/**
	 * @return the bannerCampus
	 */
	public String getBannerCampus() {
		return bannerCampus;
	}


	/**
	 * @param bannerCampus the bannerCampus to set
	 */
	public void setBannerCampus(String bannerCampus) {
		this.bannerCampus = bannerCampus;
	}


	/**
	 * @return the storeDataForBanner
	 */
	public Boolean getStoreDataForBanner() {
		return storeDataForBanner;
	}


	/**
	 * @param storeDataForBanner the storeDataForBanner to set
	 */
	public void setStoreDataForBanner(Boolean storeDataForBanner) {
		this.storeDataForBanner = storeDataForBanner;
	}


	/**
	 * @return the sendDataToBanner
	 */
	public Boolean getSendDataToBanner() {
		return sendDataToBanner;
	}


	/**
	 * @param sendDataToBanner the sendDataToBanner to set
	 */
	public void setSendDataToBanner(Boolean sendDataToBanner) {
		this.sendDataToBanner = sendDataToBanner;
	}


	/**
	 * @return the loadingOfferingsFile
	 */
	public Boolean getLoadingOfferingsFile() {
		return loadingOfferingsFile;
	}


	/**
	 * @param loadingOfferingsFile the loadingOfferingsFile to set
	 */
	public void setLoadingOfferingsFile(Boolean loadingOfferingsFile) {
		this.loadingOfferingsFile = loadingOfferingsFile;
	}

	@Override
	public void validate(UniTimeAction action) {
		// Check data fields
		if (bannerTermCode==null || bannerTermCode.trim().length()==0) 
			action.addFieldError("form.bannerTermCode", MSG.errorRequiredField(BMSG.colBannerTermCode()));
		
		if (bannerCampus==null || bannerCampus.trim().length()==0) 
			action.addFieldError("form.bannerCampus", MSG.errorRequiredField(BMSG.colBannerCampus()));

		if (acadSessionId==null) 
			action.addFieldError("form.acadSessionId", MSG.errorRequiredField(MSG.columnAcademicSession()));
		
				
		// Check for duplicate session
		if (!action.hasFieldErrors()) {
			BannerSession sessn = BannerSession.findBannerSessionForSession(acadSessionId, BannerSessionDAO.getInstance().getSession());
			if (sessionId==null && sessn!=null)
				action.addFieldError("form.sessionId", BMSG.errorBannerSessionAlreadyExists());
				
			if (sessionId!=null && sessn!=null) {
				if (!sessionId.equals(sessn.getUniqueId()))
					action.addFieldError("form.sessionId", BMSG.errorAnoterBannerSessionAlreadyExists());
			}
		}
	}


	public Boolean getUseSubjectAreaPrefixAsCampus() {
		return useSubjectAreaPrefixAsCampus;
	}


	public void setUseSubjectAreaPrefixAsCampus(Boolean useSubjectAreaPrefixAsCampus) {
		this.useSubjectAreaPrefixAsCampus = useSubjectAreaPrefixAsCampus;
	}


	public String getSubjectAreaPrefixDelimiter() {
		return subjectAreaPrefixDelimiter;
	}


	public void setSubjectAreaPrefixDelimiter(String subjectAreaPrefixDelimiter) {
		this.subjectAreaPrefixDelimiter = subjectAreaPrefixDelimiter;
	}


	public Long getSessionId() {
		return sessionId;
	}

	public void setSessionId(Long sessionId) {
		this.sessionId = sessionId;
	}
	
	public Long getFutureSessionId() { return futureSessionId; }
	public void setFutureSessionId(Long futureSessionId) { this.futureSessionId = futureSessionId; }
	public Integer getFutureUpdateMode() { return futureUpdateMode; }
	public void setFutureUpdateMode(Integer futureUpdateMode) { this.futureUpdateMode = futureUpdateMode; }
	public String getStudentCampus() { return studentCampus; }
	public void setStudentCampus(String studentCampus) { this.studentCampus = studentCampus; }
	public List<IdValue> getFutureUpdateModes() {
		List<IdValue> ret = new ArrayList<IdValue>();
		for (FutureSessionUpdateMode m: FutureSessionUpdateMode.values()) {
			switch (m) {
			case NO_UPDATE: ret.add(new IdValue((long)m.ordinal(), BMSG.descUpdateModeDisabled())); break;
			case DIRECT_UPDATE: ret.add(new IdValue((long)m.ordinal(), BMSG.descUpdateModeDirect())); break;
			case SEND_REQUEST: ret.add(new IdValue((long)m.ordinal(), BMSG.descUpdateModeRequest())); break;
			}
		}
		return ret;
	}
}
