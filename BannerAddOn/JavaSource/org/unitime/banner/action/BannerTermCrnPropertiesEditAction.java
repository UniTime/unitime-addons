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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

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
		
        BannerTermCrnPropertiesEditForm bannerTermCrnPropertiesEditForm = (BannerTermCrnPropertiesEditForm) form;		
		Long id =  new Long(Long.parseLong(request.getParameter("bannerTermCrnPropertiesId")));
		BannerTermCrnProperties bannerTermCrnProperties = BannerTermCrnProperties.getBannerTermCrnPropertiesById(id);
		bannerTermCrnPropertiesEditForm.setBannerTermProperties(bannerTermCrnProperties);
		bannerTermCrnPropertiesEditForm.setBannerTermCode(bannerTermCrnProperties.getBannerTermCode());
		setAvailableSessionInfoInForm(bannerTermCrnPropertiesEditForm);
		String[] bannerSessionIds = new String[bannerTermCrnProperties.getBannerSessions().size()];
		int i = 0;
		for (BannerSession bs : bannerTermCrnProperties.getBannerSessions()) {
			bannerSessionIds[i++] = bs.getUniqueId().toString();
			if (!bannerTermCrnPropertiesEditForm.getAvailableBannerSessions().contains(bs)) {
				bannerTermCrnPropertiesEditForm.getAvailableBannerSessions().add(bs);
			}
		}
		bannerTermCrnPropertiesEditForm.setBannerSessionIds(bannerSessionIds);
		bannerTermCrnPropertiesEditForm.setLastCrn(bannerTermCrnProperties.getLastCrn());
		bannerTermCrnPropertiesEditForm.setMinCrn(bannerTermCrnProperties.getMinCrn());
		bannerTermCrnPropertiesEditForm.setMaxCrn(bannerTermCrnProperties.getMaxCrn());
		bannerTermCrnPropertiesEditForm.setSearchFlag(bannerTermCrnProperties.isSearchFlag());
		return mapping.findForward("showEdit");
	}
	protected void setAvailableSessionInfoInForm(BannerTermCrnPropertiesEditForm sessionEditForm){
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
		sessionEditForm.setAvailableBannerTermCodes(bannerTermCodes);
		sessionEditForm.setAvailableBannerSessions(bannerSessionList);
	}

	
	public ActionForward addSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {

	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);

		BannerTermCrnPropertiesEditForm crnPropEditForm = (BannerTermCrnPropertiesEditForm) form;
		setAvailableSessionInfoInForm(crnPropEditForm);
		crnPropEditForm.setBannerSessionIds(new String[0]);
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
                setAvailableSessionInfoInForm(bannerTermCrnPropertiesEditForm);
                if (bannerTermCrnProperties.getUniqueId()!=null) {
                    return mapping.findForward("showEdit");
                }
                else {

                    return mapping.findForward("showAdd");
                }
            }
           
            bannerTermCrnProperties.setBannerTermCode(bannerTermCrnPropertiesEditForm.getBannerTermCode());
            bannerTermCrnProperties.setLastCrn(bannerTermCrnPropertiesEditForm.getLastCrn());
            bannerTermCrnProperties.setMinCrn(bannerTermCrnPropertiesEditForm.getMinCrn());
            bannerTermCrnProperties.setMaxCrn(bannerTermCrnPropertiesEditForm.getMaxCrn());
            bannerTermCrnProperties.setSearchFlag(bannerTermCrnPropertiesEditForm.getSearchFlag()==null?new Boolean(false):bannerTermCrnPropertiesEditForm.getSearchFlag());
            HashSet<BannerSession> origSessions = new HashSet<BannerSession>();
            if (bannerTermCrnProperties.getBannerSessions() != null) {
            		origSessions.addAll(bannerTermCrnProperties.getBannerSessions());
                bannerTermCrnProperties.getBannerSessions().clear();
            }
            for (String bsId : bannerTermCrnPropertiesEditForm.getBannerSessionIds()) {
            		BannerSession bs = BannerSession.getBannerSessionById(new Long(bsId));
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
