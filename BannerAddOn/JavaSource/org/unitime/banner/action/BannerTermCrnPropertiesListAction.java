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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.banner.form.BannerTermCrnPropertiesListForm;
import org.unitime.banner.model.BannerTermCrnProperties;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

/**
 * 
 * @author says
 *
 */
@Service("/bannerTermCrnPropertiesList")
public class BannerTermCrnPropertiesListAction extends Action {

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
	public ActionForward execute(
		ActionMapping mapping,
		ActionForm form,
		HttpServletRequest request,
		HttpServletResponse response) throws Exception {

	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);

        BannerTermCrnPropertiesListForm sessionListForm = (BannerTermCrnPropertiesListForm) form;
		sessionListForm.setSessions(BannerTermCrnProperties.getAllBannerTermCrnProperties());
		return mapping.findForward("showBannerTermCrnPropertiesList");
		
	}

}
