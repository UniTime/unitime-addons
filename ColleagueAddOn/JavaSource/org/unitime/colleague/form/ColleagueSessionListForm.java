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
package org.unitime.colleague.form;

import java.util.ArrayList;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * 
 * @author says
 *
 */
public class ColleagueSessionListForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1239226473486660059L;
	// --------------------------------------------------------- Instance Variables
	private Collection sessions;
	
	// --------------------------------------------------------- Methods

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {

		sessions = new ArrayList();
	}

	/**
	 * @return Returns the sessions.
	 */
	public Collection getSessions() {
		return sessions;
	}
	/**
	 * @param sessions The sessions to set.
	 */
	public void setSessions(Collection sessions) {
		this.sessions = sessions;
	}
}
