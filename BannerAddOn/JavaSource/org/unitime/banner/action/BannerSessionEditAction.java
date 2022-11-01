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
package org.unitime.banner.action;

import java.util.ArrayList;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.banner.form.BannerSessionEditForm;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;

/**
 * 
 * @author says
 *
 */
@Action(value = "bannerSessionEdit", results = {
		@Result(name = "showEdit", type = "tiles", location = "bannerSessionEdit.tiles"),
		@Result(name = "showAdd", type = "tiles", location = "bannerSessionAdd.tiles"),
		@Result(name = "showBannerSessionList", type = "redirect", location = "/bannerSessionList.action",
			params = { "anchor", "${form.sessionId}" })
	})
@TilesDefinitions({
@TilesDefinition(name = "bannerSessionEdit.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Edit Banner Session"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerSessionEdit.jsp")
	}),
@TilesDefinition(name = "bannerSessionAdd.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Add Banner Session"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerSessionEdit.jsp")
	})
})
public class BannerSessionEditAction extends UniTimeAction<BannerSessionEditForm> {
	private static final long serialVersionUID = 2342917602431813183L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	private Long sessionId;
	public Long getSessionId() { return sessionId; }
	public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

	@Override
	public String execute() throws Exception {
		if (form == null) form = new BannerSessionEditForm();
		
		if ("Edit".equals(op))
			return editSession();
		if (BMSG.actionAddBannerSession().equals(op))
			return addSession();
		if (BMSG.actionUpdateBannerSession().equals(op) || BMSG.actionSaveBannerSession().equals(op))
			return saveSession();
		if (BMSG.actionBackToBannerSessions().equals(op))
			return cancelSessionEdit();
		
		return (form.getSessionId() == null ? "showAdd" : "showEdit");
	}

	
	public String editSession() throws Exception {
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);
		
		BannerSession bannerSession = BannerSession.getBannerSessionById(sessionId);
		form.setSessionId(bannerSession.getUniqueId());
		form.setAcadSessionId(bannerSession.getSession().getUniqueId());
		form.setBannerTermCode(bannerSession.getBannerTermCode());
		form.setBannerCampus(bannerSession.getBannerCampus());
		form.setStoreDataForBanner(bannerSession.isStoreDataForBanner());
		form.setSendDataToBanner(bannerSession.isSendDataToBanner());
		form.setLoadingOfferingsFile(bannerSession.isLoadingOfferingsFile());
		form.setAcadSessionLabel(bannerSession.getSession().getLabel());
		form.setFutureSessionId(bannerSession.getFutureSession() == null ? null : bannerSession.getFutureSession().getUniqueId());
		form.setFutureUpdateMode(bannerSession.getFutureSessionUpdateModeInt());
		form.setStudentCampus(bannerSession.getStudentCampus());
		form.setUseSubjectAreaPrefixAsCampus(bannerSession.isUseSubjectAreaPrefixAsCampus());
		form.setSubjectAreaPrefixDelimiter(bannerSession.getSubjectAreaPrefixDelimiter());
		setBannerSessionsInForm();
		return "showEdit";
	}
	protected void setAvailableSessionsInForm(){
		ArrayList sessionList = new ArrayList();
		sessionList.addAll(SessionDAO.getInstance().getQuery("from Session s where s.uniqueId not in (select bs.session.uniqueId from BannerSession bs)").list());
		form.setAvailableAcadSessions(sessionList);
	}
	
	protected void setBannerSessionsInForm(){
		ArrayList bannerSessionList = new ArrayList();
		if (form.getSessionId() != null)
			bannerSessionList.addAll(SessionDAO.getInstance().getQuery("from BannerSession s where s.uniqueId != :id order by s.bannerTermCode desc, s.bannerCampus").setLong("id", form.getSessionId()).list());
		else
			bannerSessionList.addAll(SessionDAO.getInstance().getQuery("from BannerSession s order by s.bannerTermCode desc, s.bannerCampus").list());
		form.setAvailableBannerSessions(bannerSessionList);
	}
	
	public String addSession() throws Exception {
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);

		setAvailableSessionsInForm();
		setBannerSessionsInForm();
		return "showAdd";
	}
	
	public String saveSession() throws Exception {
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);

        form.validate(this);
        if (hasFieldErrors()) {
            setBannerSessionsInForm();
            if (form.getSessionId() != null) {
                return "showEdit";
            } else {
                setAvailableSessionsInForm();
                return "showAdd";
            }
        }

        Transaction tx = null;
        org.hibernate.Session hibSession = BannerSessionDAO.getInstance().getSession();
        
        try {
            tx = hibSession.beginTransaction();
            
            BannerSession sessn = null;
            if (form.getSessionId()!=null) 
                sessn = BannerSessionDAO.getInstance().get(form.getSessionId(), hibSession);
            else 
                sessn = new BannerSession();
            
            sessn.setSession(SessionDAO.getInstance().get(form.getAcadSessionId()));
            sessn.setBannerCampus(form.getBannerCampus());
            sessn.setBannerTermCode(form.getBannerTermCode());
            sessn.setStoreDataForBanner(form.getStoreDataForBanner() == null?Boolean.valueOf(false):form.getStoreDataForBanner());
            sessn.setSendDataToBanner(form.getSendDataToBanner() == null?Boolean.valueOf(false):form.getSendDataToBanner());
            sessn.setLoadingOfferingsFile(form.getLoadingOfferingsFile() == null?Boolean.valueOf(false):form.getLoadingOfferingsFile());
            sessn.setFutureSessionUpdateModeInt(form.getFutureUpdateMode());
            sessn.setFutureSession(form.getFutureSessionId() == null ? null : BannerSessionDAO.getInstance().get(form.getFutureSessionId()));
            sessn.setStudentCampus(form.getStudentCampus());
            sessn.setUseSubjectAreaPrefixAsCampus(form.getUseSubjectAreaPrefixAsCampus());
            sessn.setSubjectAreaPrefixDelimiter(form.getSubjectAreaPrefixDelimiter());

            hibSession.saveOrUpdate(sessn);

            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    sessn, 
                    ChangeLog.Source.SESSION_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    null);
            
            tx.commit() ;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
        
		return "showBannerSessionList";
	}

	public String cancelSessionEdit() {
		return "showBannerSessionList";
	}
}
