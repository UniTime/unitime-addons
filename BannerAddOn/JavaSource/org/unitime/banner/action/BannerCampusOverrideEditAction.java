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


import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesDefinitions;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Transaction;
import org.unitime.banner.form.BannerCampusOverrideEditForm;
import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.dao.BannerCampusOverrideDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.security.rights.Right;

/**
 * 
 * @author says
 *
 */
@Action(value = "bannerCampusOverrideEdit", results = {
		@Result(name = "showEdit", type = "tiles", location = "bannerCampusOverrideEdit.tiles"),
		@Result(name = "showAdd", type = "tiles", location = "bannerCampusOverrideAdd.tiles"),
		@Result(name = "showBannerCampusOverrideList", type = "redirect", location = "/bannerCampusOverrideList.action",
			params = { "anchor", "${form.campusOverrideId}" })
	})
@TilesDefinitions({
@TilesDefinition(name = "bannerCampusOverrideEdit.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Edit Banner Campus Override"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerCampusOverrideEdit.jsp")
	}),
@TilesDefinition(name = "bannerCampusOverrideAdd.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Add Banner Campus Override"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerCampusOverrideEdit.jsp")
	})
})
public class BannerCampusOverrideEditAction extends UniTimeAction<BannerCampusOverrideEditForm> {
	private static final long serialVersionUID = 6185143244808321892L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	private Long campusOverrideId;
	public Long getCampusOverrideId() { return campusOverrideId; }
	public void setCampusOverrideId(Long campusOverrideId) { this.campusOverrideId = campusOverrideId; }

	@Override
	public String execute() throws Exception {
		if (form == null) form = new BannerCampusOverrideEditForm();
		
		if (BMSG.actionAddCampusOverride().equals(op))
			return addCampusOverride();
		if (BMSG.actionSaveCampusOverride().equals(op) || BMSG.actionUpdateCampusOverride().equals(op))
			return saveCampusOverride();
		if ("Edit".equals(op))
			return editCampusOverride();
		if (BMSG.actionBackToBannerOfferings().equals(op))
			return cancelCampusOverrideEdit();
		return form.getCampusOverrideId() == null ? "showAdd" : "showEdit";
	  }

	
	public String editCampusOverride() throws Exception {
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);
		
		BannerCampusOverride bannerCampusOverride = BannerCampusOverride.getBannerCampusOverrideById(campusOverrideId);
		form.setCampusOverrideId(bannerCampusOverride.getUniqueId());
		form.setBannerCampusCode(bannerCampusOverride.getBannerCampusCode());
		form.setBannerCampusName(bannerCampusOverride.getBannerCampusName());
		form.setVisible(bannerCampusOverride.isVisible());
		return "showEdit";
	}
	
	public String addCampusOverride() throws Exception {
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);

		return "showAdd";
	}
	
	public String saveCampusOverride() throws Exception {
				
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);
        
        Transaction tx = null;
        org.hibernate.Session hibSession = BannerCampusOverrideDAO.getInstance().getSession();
        
        try {
            tx = hibSession.beginTransaction();
            
            form.validate(this);
            if (hasFieldErrors()) {
                if (form.getCampusOverrideId() != null) {
                    return "showEdit";
                } else {
                    return "showAdd";
                }
            }
           
            BannerCampusOverride campusOverride = null;
            if (form.getCampusOverrideId() != null) 
                campusOverride = (new BannerCampusOverrideDAO()).get(form.getCampusOverrideId(),hibSession);
            else 
                campusOverride = new BannerCampusOverride();
            campusOverride.setBannerCampusName(form.getBannerCampusName());
            campusOverride.setBannerCampusCode(form.getBannerCampusCode());
            campusOverride.setVisible(form.getVisible());

            if (campusOverride.getUniqueId() == null)
            	hibSession.persist(campusOverride);
            else
            	hibSession.merge(campusOverride);
            form.setCampusOverrideId(campusOverride.getUniqueId());

            ChangeLog.addChange(
                    hibSession, 
                    sessionContext, 
                    campusOverride, 
                    ChangeLog.Source.SESSION_EDIT, 
                    ChangeLog.Operation.UPDATE, 
                    null, 
                    null);
            
            tx.commit() ;
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw e;
        }
        
		return "showBannerCampusOverrideList";
	}
	
	public String cancelCampusOverrideEdit() {
		return "showBannerCampusOverrideList";
	}
	
}
