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
package org.unitime.banner.action;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.actions.LookupDispatchAction;
import org.hibernate.HibernateException;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.banner.form.BannerTermCrnPropertiesEditForm;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.BannerTermCrnProperties;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.banner.model.dao.BannerTermCrnPropertiesDAO;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.spring.struts.SpringAwareLookupDispatchAction;

/**
 * 
 * @author says
 *
 */
@Service("/bannerTermCrnPropertiesEdit")
public class BannerTermCrnPropertiesEditAction extends SpringAwareLookupDispatchAction {
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
	
	protected Map<String, String> getKeyMethodMap() {
	      Map<String, String> map = new HashMap<String, String>();
	      map.put("editSession", "editSession");
	      map.put("button.addSession", "addSession");
	      map.put("button.saveSession", "saveSession");
	      map.put("button.updateSession", "saveSession");
	      map.put("button.cancelSessionEdit", "cancelSessionEdit");
	      return map;
	  }

	
	public ActionForward editSession(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {
		
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);
		
        BannerTermCrnPropertiesEditForm bannerTermCrnPropertiesEditFrom = (BannerTermCrnPropertiesEditForm) form;		
		Long id =  new Long(Long.parseLong(request.getParameter("bannerTermCrnPropertiesId")));
		BannerTermCrnProperties bannerTermCrnProperties = BannerTermCrnProperties.getBannerTermCrnPropertiesById(id);
		bannerTermCrnPropertiesEditFrom.setBannerTermProperties(bannerTermCrnProperties);
		bannerTermCrnPropertiesEditFrom.setBannerTermCode(bannerTermCrnProperties.getBannerTermCode());
		bannerTermCrnPropertiesEditFrom.setLastCrn(bannerTermCrnProperties.getLastCrn());
		bannerTermCrnPropertiesEditFrom.setMinCrn(bannerTermCrnProperties.getMinCrn());
		bannerTermCrnPropertiesEditFrom.setMaxCrn(bannerTermCrnProperties.getMaxCrn());
		bannerTermCrnPropertiesEditFrom.setSearchFlag(bannerTermCrnProperties.isSearchFlag());
		return mapping.findForward("showEdit");
	}
	protected void setAvailableSessionsInForm(BannerTermCrnPropertiesEditForm sessionEditForm){
		ArrayList<BannerSession> bannerTermCodes = new ArrayList<BannerSession>();

		@SuppressWarnings("rawtypes")
		Iterator it = SessionDAO.getInstance().getQuery("select bs from BannerSession bs where bs.bannerTermCode not in (select btcp.bannerTermCode from BannerTermCrnProperties btcp)").iterate();
		TreeMap<String, BannerSession> bannerSessions = new TreeMap<String, BannerSession>();
		while(it.hasNext()){
			BannerSession bs = (BannerSession) it.next();
			bannerSessions.put(bs.getBannerTermCode(), bs);
		}
		for(String key : bannerSessions.keySet()){
			bannerTermCodes.add(bannerSessions.get(key));
		}
		sessionEditForm.setAvailableBannerTermCodes(bannerTermCodes);
	}

	
	public ActionForward addSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);

//		BannerTermCrnPropertiesEditForm sessionEditForm = (BannerTermCrnPropertiesEditForm) form;
		setAvailableSessionsInForm((BannerTermCrnPropertiesEditForm) form);
		return mapping.findForward("showAdd");
	}
	
	public ActionForward saveSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
				
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);
        
        Transaction tx = null;
        org.hibernate.Session hibSession = BannerSessionDAO.getInstance().getSession();
        
        try {
            tx = hibSession.beginTransaction();

            BannerTermCrnPropertiesEditForm bannerTermCrnPropertiesEditForm = (BannerTermCrnPropertiesEditForm) form;
            BannerTermCrnProperties bannerTermCrnProperties = bannerTermCrnPropertiesEditForm.getBannerTermProperties();
            
            if (bannerTermCrnPropertiesEditForm.getBannerTermCrnPropertiesId()!=null && bannerTermCrnProperties.getUniqueId().longValue()!=0) 
                bannerTermCrnProperties = BannerTermCrnPropertiesDAO.getInstance().get(bannerTermCrnPropertiesEditForm.getBannerTermCrnPropertiesId(),hibSession);
            else 
                bannerTermCrnProperties.setUniqueId(null);
                                    
            ActionMessages errors = bannerTermCrnPropertiesEditForm.validate(mapping, request);
            if (errors.size()>0) {
                saveErrors(request, errors);
                if (bannerTermCrnProperties.getUniqueId()!=null) {
                    return mapping.findForward("showEdit");
                }
                else {
                    setAvailableSessionsInForm(bannerTermCrnPropertiesEditForm);
                    return mapping.findForward("showAdd");
                }
            }
           
            bannerTermCrnProperties.setBannerTermCode(bannerTermCrnPropertiesEditForm.getBannerTermCode());
            bannerTermCrnProperties.setLastCrn(bannerTermCrnPropertiesEditForm.getLastCrn());
            bannerTermCrnProperties.setMinCrn(bannerTermCrnPropertiesEditForm.getMinCrn());
            bannerTermCrnProperties.setMaxCrn(bannerTermCrnPropertiesEditForm.getMaxCrn());
            bannerTermCrnProperties.setSearchFlag(bannerTermCrnPropertiesEditForm.getSearchFlag()==null?new Boolean(false):bannerTermCrnPropertiesEditForm.getSearchFlag());

            hibSession.saveOrUpdate(bannerTermCrnProperties);

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
        
		return mapping.findForward("showBannerTermCrnPropertiesList");
	}

	public ActionForward cancelSessionEdit(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		return mapping.findForward("showBannerTermCrnPropertiesList");
	}
	
}