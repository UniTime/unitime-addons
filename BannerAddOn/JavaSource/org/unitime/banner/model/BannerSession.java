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

package org.unitime.banner.model;

import java.util.List;
import java.util.Vector;

import org.hibernate.FlushMode;
import org.unitime.banner.model.base.BaseBannerSession;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.Class_DAO;



/**
 * 
 * @author says
 *
 */
public class BannerSession extends BaseBannerSession {
	private static final long serialVersionUID = 1L;

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

		return((BannerSession) querySession.createQuery("from BannerSession bs where bs.session.uniqueId = :sessionId").setFlushMode(FlushMode.MANUAL).setLong("sessionId", acadSessionId.longValue()).setCacheable(true).uniqueResult());
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

	public static List getAllSessions() {
		BannerSessionDAO bsDao = new BannerSessionDAO();
		List l = bsDao.getSession().createQuery("from BannerSession").list();
		if (l == null){
			l = new Vector();
		}
		return(l);
	}

	public static BannerSession getBannerSessionById(Long id) {
		BannerSessionDAO bsDao = new BannerSessionDAO();
		return(bsDao.get(id));
	}
		
}