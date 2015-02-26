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

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.banner.form.BannerCampusOverrideEditForm;
import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.dao.BannerCampusOverrideDAO;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.spring.struts.SpringAwareLookupDispatchAction;

/**
 * 
 * @author says
 *
 */
@Service("/bannerCampusOverrideEdit")
public class BannerCampusOverrideEditAction extends SpringAwareLookupDispatchAction {
	// --------------------------------------------------------- Instance Variables
	

	// --------------------------------------------------------- Methods
	@Autowired SessionContext sessionContext;
	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws HibernateException
	 */
	
	@SuppressWarnings("unchecked")
	protected Map getKeyMethodMap() {
	      Map map = new HashMap();
	      map.put("editSession", "editCampusOverride");
	      map.put("button.addNew", "addCampusOverride");
	      map.put("button.save", "saveCampusOverride");
	      map.put("button.update", "saveCampusOverride");
	      map.put("button.cancel", "cancelCampusOverrideEdit");
	      return map;
	  }

	
	public ActionForward editCampusOverride(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);
		
        BannerCampusOverrideEditForm bannerCampusOverrideForm = (BannerCampusOverrideEditForm) form;		
		Long id =  new Long(Long.parseLong(request.getParameter("campusOverrideId")));
		BannerCampusOverride bannerCampusOverride = BannerCampusOverride.getBannerCampusOverrideById(id);
		bannerCampusOverrideForm.setBannerCampusOverride(bannerCampusOverride);
		bannerCampusOverrideForm.setBannerCampusCode(bannerCampusOverride.getBannerCampusCode());
		bannerCampusOverrideForm.setBannerCampusName(bannerCampusOverride.getBannerCampusName());
		bannerCampusOverrideForm.setVisible(bannerCampusOverride.isVisible());
		return mapping.findForward("showEdit");
	}
	
	public ActionForward addCampusOverride(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);

		return mapping.findForward("showAdd");
	}
	
	public ActionForward saveCampusOverride(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
				
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);
        
        Transaction tx = null;
        org.hibernate.Session hibSession = BannerCampusOverrideDAO.getInstance().getSession();
        
        try {
            tx = hibSession.beginTransaction();

            BannerCampusOverrideEditForm bannerCampusOverrideEditForm = (BannerCampusOverrideEditForm) form;
            BannerCampusOverride campusOverride = bannerCampusOverrideEditForm.getBannerCampusOverride();
            
            if (bannerCampusOverrideEditForm.getCampusOverrideId()!=null && campusOverride.getUniqueId().longValue()!=0) 
                campusOverride = (new BannerCampusOverrideDAO()).get(bannerCampusOverrideEditForm.getCampusOverrideId(),hibSession);
            else 
                campusOverride.setUniqueId(null);
                                    
            ActionMessages errors = bannerCampusOverrideEditForm.validate(mapping, request);
            if (errors.size()>0) {
                saveErrors(request, errors);
                if (campusOverride.getUniqueId()!=null) {
                    return mapping.findForward("showEdit");
                }
                else {
                    return mapping.findForward("showAdd");
                }
            }
           
            campusOverride.setBannerCampusName(bannerCampusOverrideEditForm.getBannerCampusName());
            campusOverride.setBannerCampusCode(bannerCampusOverrideEditForm.getBannerCampusCode());
            campusOverride.setVisible(bannerCampusOverrideEditForm.getVisible() == null?new Boolean(false):bannerCampusOverrideEditForm.getVisible());

            hibSession.saveOrUpdate(campusOverride);

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
        
		return mapping.findForward("showBannerCampusOverrideList");
	}

	public ActionForward cancelCampusOverrideEdit(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		return mapping.findForward("showBannerCampusOverrideList");
	}
	
}
