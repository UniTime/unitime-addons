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
import java.util.HashSet;
import java.util.List;
import java.util.TreeMap;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.banner.form.BannerTermCrnPropertiesEditForm;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.BannerTermCrnProperties;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.banner.model.dao.BannerTermCrnPropertiesDAO;
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
@Action(value = "bannerTermCrnPropertiesEdit", results = {
		@Result(name = "showEdit", type = "tiles", location = "bannerTermCrnPropertiesEdit.tiles"),
		@Result(name = "showAdd", type = "tiles", location = "bannerTermCrnPropertiesAdd.tiles"),
		@Result(name = "showBannerTermCrnPropertiesList", type = "redirect", location = "/bannerTermCrnPropertiesList.action",
			params = { "anchor", "${form.bannerTermPropertiesId}" })
	})
@TilesDefinitions({
@TilesDefinition(name = "bannerTermCrnPropertiesEdit.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Edit Banner Term CRN Properties"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerTermCrnPropertiesEdit.jsp")
	}),
@TilesDefinition(name = "bannerTermCrnPropertiesAdd.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Add Banner Term CRN Properties"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerTermCrnPropertiesEdit.jsp")
	})
})
public class BannerTermCrnPropertiesEditAction extends UniTimeAction<BannerTermCrnPropertiesEditForm> {
	private static final long serialVersionUID = 2342917602431813183L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	private Long bannerTermCrnPropertiesId;
	public Long getBannerTermCrnPropertiesId() { return bannerTermCrnPropertiesId; }
	public void setBannerTermCrnPropertiesId(Long bannerTermCrnPropertiesId) { this.bannerTermCrnPropertiesId = bannerTermCrnPropertiesId; }

	@Override
	public String execute() throws Exception {
		if (form == null) form = new BannerTermCrnPropertiesEditForm();
		
		if ("Edit".equals(op))
			return editSession();
		if (BMSG.actionAddBannerSession().equals(op))
			return addSession();
		if (BMSG.actionUpdateBannerSession().equals(op) || BMSG.actionSaveBannerSession().equals(op))
			return saveSession();
		if (BMSG.actionBackToBannerSessions().equals(op))
			return cancelSessionEdit();
		
		return (form.getBannerTermPropertiesId() == null ? "showAdd" : "showEdit");
	}

	
	public String editSession() throws Exception {
		
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);
		
		BannerTermCrnProperties bannerTermCrnProperties = BannerTermCrnProperties.getBannerTermCrnPropertiesById(bannerTermCrnPropertiesId);
		form.setBannerTermPropertiesId(bannerTermCrnProperties.getUniqueId());
		form.setBannerTermCode(bannerTermCrnProperties.getBannerTermCode());
		setAvailableSessionInfoInForm();
		String[] bannerSessionIds = new String[bannerTermCrnProperties.getBannerSessions().size()];
		int i = 0;
		for (BannerSession bs : bannerTermCrnProperties.getBannerSessions()) {
			bannerSessionIds[i++] = bs.getUniqueId().toString();
			if (!form.getAvailableBannerSessions().contains(bs)) {
				form.getAvailableBannerSessions().add(bs);
			}
		}
		form.setBannerSessionIds(bannerSessionIds);
		form.setLastCrn(bannerTermCrnProperties.getLastCrn());
		form.setMinCrn(bannerTermCrnProperties.getMinCrn());
		form.setMaxCrn(bannerTermCrnProperties.getMaxCrn());
		form.setSearchFlag(bannerTermCrnProperties.isSearchFlag());
		return "showEdit";
	}
	protected void setAvailableSessionInfoInForm(){
		ArrayList<BannerSession> bannerTermCodes = new ArrayList<BannerSession>();
		ArrayList<BannerSession> bannerSessionList = new ArrayList<BannerSession>();
		TreeMap<String, BannerSession> bannerSessions = new TreeMap<String, BannerSession>();
		for (BannerSession bs : (List<BannerSession>)SessionDAO.getInstance().getSession().createQuery("select bs from BannerSession bs").list()){
			if (bs.getBannerTermCrnProperties() == null) {
				bannerSessions.put(bs.getBannerTermCode(), bs);
				bannerSessionList.add(bs);
			}
		}
		for (String bsTermCode : bannerSessions.keySet()) {
			bannerTermCodes.add(bannerSessions.get(bsTermCode));
			
		}
		form.setAvailableBannerTermCodes(bannerTermCodes);
		form.setAvailableBannerSessions(bannerSessionList);
	}

	
	public String addSession() throws Exception {
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);

		setAvailableSessionInfoInForm();
		form.setBannerSessionIds(new String[0]);
		
		return "showAdd";
	}
	
	public String saveSession() throws Exception {
				
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);
        
        Transaction tx = null;
        org.hibernate.Session hibSession = BannerSessionDAO.getInstance().getSession();
        
        try {
            tx = hibSession.beginTransaction();

            form.validate(this);
            if (hasFieldErrors()) {
                setAvailableSessionInfoInForm();
                if (form.getBannerTermPropertiesId()!=null) {
                    return "showEdit";
                } else {
                    return "showAdd";
                }
            }
            
            BannerTermCrnProperties bannerTermCrnProperties = null;
            
            if (form.getBannerTermPropertiesId() != null) 
                bannerTermCrnProperties = BannerTermCrnPropertiesDAO.getInstance().get(form.getBannerTermPropertiesId(), hibSession);
            else 
                bannerTermCrnProperties = new BannerTermCrnProperties();
           
            bannerTermCrnProperties.setBannerTermCode(form.getBannerTermCode());
            bannerTermCrnProperties.setLastCrn(form.getLastCrn());
            bannerTermCrnProperties.setMinCrn(form.getMinCrn());
            bannerTermCrnProperties.setMaxCrn(form.getMaxCrn());
            bannerTermCrnProperties.setSearchFlag(form.getSearchFlag()==null?Boolean.valueOf(false):form.getSearchFlag());
            HashSet<BannerSession> origSessions = new HashSet<BannerSession>();
            if (bannerTermCrnProperties.getBannerSessions() != null) {
            		origSessions.addAll(bannerTermCrnProperties.getBannerSessions());
                bannerTermCrnProperties.getBannerSessions().clear();
            }
            for (String bsId : form.getBannerSessionIds()) {
            		BannerSession bs = BannerSession.getBannerSessionById(Long.valueOf(bsId));
            		bannerTermCrnProperties.addTobannerSessions(bs);
            		bs.setBannerTermCrnProperties(bannerTermCrnProperties);
            		origSessions.remove(bs);
            }
            hibSession.saveOrUpdate(bannerTermCrnProperties);
            for (BannerSession bs : origSessions) {
            	    BannerSession bs2 = BannerSession.getBannerSessionById(bs.getUniqueId());
            	    bs2.setBannerTermCrnProperties(null);
            	    hibSession.update(bs2);
            }
            
            form.setBannerTermPropertiesId(bannerTermCrnProperties.getUniqueId());

            ChangeLog.addChange(
                    hibSession, 
                    sessionContext,
                    bannerTermCrnProperties, 
                    ChangeLog.Source.SESSION_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    null);
            
            tx.commit() ;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
        
		return "showBannerTermCrnPropertiesList";
	}

	public String cancelSessionEdit() {
		return "showBannerTermCrnPropertiesList";
	}
	
	public int getListSize() {
		return Math.min(7, form.getAvailableBannerSessions().size());
	}
}
