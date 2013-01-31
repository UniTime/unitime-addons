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
import java.util.Map;

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
import org.unitime.banner.form.BannerSessionEditForm;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerSessionDAO;
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
@Service("/bannerSessionEdit")
public class BannerSessionEditAction extends SpringAwareLookupDispatchAction {
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
		
        BannerSessionEditForm sessionEditForm = (BannerSessionEditForm) form;		
		Long id =  new Long(Long.parseLong(request.getParameter("sessionId")));
		BannerSession bannerSession = BannerSession.getBannerSessionById(id);
		sessionEditForm.setSession(bannerSession);
		sessionEditForm.setAcadSessionId(bannerSession.getSession().getUniqueId());
		sessionEditForm.setBannerTermCode(bannerSession.getBannerTermCode());
		sessionEditForm.setBannerCampus(bannerSession.getBannerCampus());
		sessionEditForm.setStoreDataForBanner(bannerSession.isStoreDataForBanner());
		sessionEditForm.setSendDataToBanner(bannerSession.isSendDataToBanner());
		sessionEditForm.setLoadingOfferingsFile(bannerSession.isLoadingOfferingsFile());
		sessionEditForm.setAcadSessionLabel(bannerSession.getSession().getLabel());
		return mapping.findForward("showEdit");
	}
	protected void setAvailableSessionsInForm(BannerSessionEditForm sessionEditForm){
		ArrayList sessionList = new ArrayList();
		sessionList.addAll(SessionDAO.getInstance().getQuery("from Session s where s.uniqueId not in (select bs.session.uniqueId from BannerSession bs)").list());
		sessionEditForm.setAvailableAcadSessions(sessionList);
	}
	
	public ActionForward addSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);


//		BannerSessionEditForm sessionEditForm = (BannerSessionEditForm) form;
		setAvailableSessionsInForm((BannerSessionEditForm) form);
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

            BannerSessionEditForm sessionEditForm = (BannerSessionEditForm) form;
            BannerSession sessn = sessionEditForm.getSession();
            
            if (sessionEditForm.getSessionId()!=null && sessn.getUniqueId().longValue()!=0) 
                sessn = (new BannerSessionDAO()).get(sessionEditForm.getSessionId(),hibSession);
            else 
                sessn.setUniqueId(null);
                                    
            ActionMessages errors = sessionEditForm.validate(mapping, request);
            if (errors.size()>0) {
                saveErrors(request, errors);
                if (sessn.getUniqueId()!=null) {
                    return mapping.findForward("showEdit");
                }
                else {
                    setAvailableSessionsInForm(sessionEditForm);
                    return mapping.findForward("showAdd");
                }
            }
           
            sessn.setSession(SessionDAO.getInstance().get(sessionEditForm.getAcadSessionId()));
            sessn.setBannerCampus(sessionEditForm.getBannerCampus());
            sessn.setBannerTermCode(sessionEditForm.getBannerTermCode());
            sessn.setStoreDataForBanner(sessionEditForm.getStoreDataForBanner() == null?new Boolean(false):sessionEditForm.getStoreDataForBanner());
            sessn.setSendDataToBanner(sessionEditForm.getSendDataToBanner() == null?new Boolean(false):sessionEditForm.getSendDataToBanner());
            sessn.setLoadingOfferingsFile(sessionEditForm.getLoadingOfferingsFile() == null?new Boolean(false):sessionEditForm.getLoadingOfferingsFile());

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
        
		return mapping.findForward("showBannerSessionList");
	}

	public ActionForward cancelSessionEdit(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		return mapping.findForward("showBannerSessionList");
	}
	
}
