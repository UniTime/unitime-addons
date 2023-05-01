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

package org.unitime.banner.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;

import org.hibernate.FlushMode;
import org.unitime.banner.model.base.BaseBannerSession;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.Class_DAO;



/**
 * 
 * @author says
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.TRANSACTIONAL, includeLazy = false)
@Table(name = "banner_session")
public class BannerSession extends BaseBannerSession {
	private static final long serialVersionUID = 1L;
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);

/*[CONSTRUCTOR MARKER BEGIN]*/
	public BannerSession () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public BannerSession (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/
	
	public static enum FutureSessionUpdateMode {
		NO_UPDATE,
		DIRECT_UPDATE,
		SEND_REQUEST,
	}


	public static BannerSession findBannerSessionForSession(Session acadSession, org.hibernate.Session hibSession){
		if (acadSession == null){
			return(null);
		}
		return(findBannerSessionForSession(acadSession.getUniqueId(), hibSession));
	}
	
	public static BannerSession findBannerSessionForSession(Long acadSessionId, org.hibernate.Session hibSession){
		if (acadSessionId == null){
			return(null);
		}
		org.hibernate.Session querySession = hibSession;
		if (querySession == null){
			querySession = BannerSessionDAO.getInstance().getSession();
		}

		return querySession.createQuery("from BannerSession bs where bs.session.uniqueId = :sessionId", BannerSession.class)
				.setHibernateFlushMode(FlushMode.MANUAL).setParameter("sessionId", acadSessionId.longValue()).setCacheable(true).uniqueResult();
	}
	
	public static boolean shouldGenerateBannerDataFieldsForSession(Session acadSession, org.hibernate.Session hibSession){
		return(shouldGenerateBannerDataFieldsForSession(acadSession.getUniqueId(), hibSession));
	}
	public static boolean shouldGenerateBannerDataFieldsForSession(Long acadSessionId, org.hibernate.Session hibSession){
		if (acadSessionId == null){
			return(false);
		}
		BannerSession bs = findBannerSessionForSession(acadSessionId, hibSession);
		if (bs == null){
			return(false);
		}
		return(!bs.isLoadingOfferingsFile());
	}

	public static boolean shouldCreateBannerDataForSession(Session acadSession, org.hibernate.Session hibSession){
		return(shouldCreateBannerDataForSession(acadSession.getUniqueId(), hibSession));
	}
	public static boolean shouldCreateBannerDataForSession(Long acadSessionId, org.hibernate.Session hibSession){
		if (acadSessionId == null){
			return(false);
		}
		org.hibernate.Session querySession = hibSession;
		if (querySession == null){
			querySession = Class_DAO.getInstance().getSession();
		}
		BannerSession bs = findBannerSessionForSession(acadSessionId, hibSession);
		if (bs == null){
			return(false);
		}
		return(bs.isStoreDataForBanner());
	}
	public static boolean shouldSendDataToBannerForSession(Session acadSession, org.hibernate.Session hibSession){
		return(shouldSendDataToBannerForSession(acadSession.getUniqueId(), hibSession));
	}
	public static boolean shouldSendDataToBannerForSession(Long acadSessionId, org.hibernate.Session hibSession){
		if (acadSessionId == null){
			return(false);
		}
		BannerSession bs = findBannerSessionForSession(acadSessionId, hibSession);
		if (bs == null){
			return(false);
		}
		return(bs.isStoreDataForBanner() && bs.isSendDataToBanner() && !bs.isLoadingOfferingsFile());
	}

	@Transient
	public static List<BannerSession> getAllSessions() {
		return BannerSessionDAO.getInstance().getSession().createQuery("from BannerSession", BannerSession.class).list();
	}

	public static BannerSession getBannerSessionById(Long id) {
		BannerSessionDAO bsDao = new BannerSessionDAO();
		return(bsDao.get(id));
	}
	
	public void setFutureSessionUpdateMode(FutureSessionUpdateMode mode) {
		if (mode == null)
			setFutureSessionUpdateModeInt(null);
		else
			setFutureSessionUpdateModeInt(mode.ordinal());
	}
	
	@Transient
	public FutureSessionUpdateMode getFutureSessionUpdateMode() {
		if (getFutureSessionUpdateModeInt() == null)
			return FutureSessionUpdateMode.NO_UPDATE;
		else
			return FutureSessionUpdateMode.values()[getFutureSessionUpdateModeInt()];
	}
	
	@Transient
	public String getFutureSessionUpdateModeLabel() {
		if (getFutureSession() == null) return "";
		switch (getFutureSessionUpdateMode()) {
		case NO_UPDATE: return BMSG.nameUpdateModeDisabled();
		case DIRECT_UPDATE: return BMSG.nameUpdateModeDirect();
		case SEND_REQUEST: return BMSG.nameUpdateModeRequest();
		default: return "";
		}
	}
	
	@Transient
	public String getLabel() {
		return getBannerTermCode() + " (" + getBannerCampus() + ")";
	}
}
