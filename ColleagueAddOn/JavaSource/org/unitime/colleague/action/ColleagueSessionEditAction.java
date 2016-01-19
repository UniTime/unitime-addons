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
package org.unitime.colleague.action;

import java.util.ArrayList;
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
import org.unitime.colleague.form.ColleagueSessionEditForm;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.dao.ColleagueSessionDAO;
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
@Service("/colleagueSessionEdit")
public class ColleagueSessionEditAction extends SpringAwareLookupDispatchAction {
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
		
        ColleagueSessionEditForm sessionEditForm = (ColleagueSessionEditForm) form;		
		Long id =  new Long(Long.parseLong(request.getParameter("sessionId")));
		ColleagueSession colleagueSession = ColleagueSession.getColleagueSessionById(id);
		sessionEditForm.setSession(colleagueSession);
		sessionEditForm.setAcadSessionId(colleagueSession.getSession().getUniqueId());
		sessionEditForm.setColleagueTermCode(colleagueSession.getColleagueTermCode());
		sessionEditForm.setColleagueCampus(colleagueSession.getColleagueCampus());
		sessionEditForm.setStoreDataForColleague(colleagueSession.isStoreDataForColleague());
		sessionEditForm.setSendDataToColleague(colleagueSession.isSendDataToColleague());
		sessionEditForm.setLoadingOfferingsFile(colleagueSession.isLoadingOfferingsFile());
		sessionEditForm.setAcadSessionLabel(colleagueSession.getSession().getLabel());
		return mapping.findForward("showEdit");
	}
	protected void setAvailableSessionsInForm(ColleagueSessionEditForm sessionEditForm){
		ArrayList sessionList = new ArrayList();
		sessionList.addAll(SessionDAO.getInstance().getQuery("from Session s where s.uniqueId not in (select bs.session.uniqueId from ColleagueSession bs)").list());
		sessionEditForm.setAvailableAcadSessions(sessionList);
	}
	
	public ActionForward addSession(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);


//		ColleagueSessionEditForm sessionEditForm = (ColleagueSessionEditForm) form;
		setAvailableSessionsInForm((ColleagueSessionEditForm) form);
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
        org.hibernate.Session hibSession = ColleagueSessionDAO.getInstance().getSession();
        
        try {
            tx = hibSession.beginTransaction();

            ColleagueSessionEditForm sessionEditForm = (ColleagueSessionEditForm) form;
            ColleagueSession sessn = sessionEditForm.getSession();
            
            if (sessionEditForm.getSessionId()!=null && sessn.getUniqueId().longValue()!=0) 
                sessn = (new ColleagueSessionDAO()).get(sessionEditForm.getSessionId(),hibSession);
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
            sessn.setColleagueCampus(sessionEditForm.getColleagueCampus());
            sessn.setColleagueTermCode(sessionEditForm.getColleagueTermCode());
            sessn.setStoreDataForColleague(sessionEditForm.getStoreDataForColleague() == null?new Boolean(false):sessionEditForm.getStoreDataForColleague());
            sessn.setSendDataToColleague(sessionEditForm.getSendDataToColleague() == null?new Boolean(false):sessionEditForm.getSendDataToColleague());
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
        
		return mapping.findForward("showColleagueSessionList");
	}

	public ActionForward cancelSessionEdit(
			ActionMapping mapping,
			ActionForm form,
			HttpServletRequest request,
			HttpServletResponse response) {
		return mapping.findForward("showColleagueSessionList");
	}
	
}
