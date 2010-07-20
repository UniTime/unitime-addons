/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/
package org.unitime.banner.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.unitime.banner.form.RollForwardBannerSessionForm;
import org.unitime.banner.util.BannerSessionRollForward;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.timetable.action.RollForwardSessionAction;



/**
 * 
 * @author says
 *
 */
public class RollForwardBannerSessionAction extends RollForwardSessionAction {
	/*
	 * Generated Methods
	 */

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws Exception 
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response) throws Exception {
	    HttpSession webSession = request.getSession();
        if(!Web.isLoggedIn( webSession )) {
            throw new Exception ("Access Denied.");
        }
        MessageResources rsc = getResources(request);
        
        RollForwardBannerSessionForm rollForwardBannerSessionForm = (RollForwardBannerSessionForm) form;
		User user = Web.getUser(request.getSession());
        // Get operation
        String op = request.getParameter("op");		  
        
        BannerSessionRollForward sessionRollForward = new BannerSessionRollForward();
        			               
   
        if (op != null && op.equals(rsc.getMessage("button.rollForward"))) {
            ActionMessages errors = rollForwardBannerSessionForm.validate(mapping, request);

            if(errors.size() == 0 && rollForwardBannerSessionForm.getRollForwardBannerSession().booleanValue()){
	        	sessionRollForward.rollBannerSessionDataForward(errors, rollForwardBannerSessionForm);
	        }

            if (errors.size() != 0) {
                saveErrors(request, errors);
            }

        }            
		rollForwardBannerSessionForm.setAdmin(user.isAdmin());
		setToFromSessionsInForm(rollForwardBannerSessionForm);
  		return mapping.findForward("displayRollForwardBannerSessionForm");
	}
	

}
