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
package org.unitime.banner.form;

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
public class BannerCampusOverrideListForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1239226473486660059L;
	// --------------------------------------------------------- Instance Variables
	private Collection campusOverrides;
	
	// --------------------------------------------------------- Methods

	/** 
	 * Method reset
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {

		campusOverrides = new ArrayList();
	}

	/**
	 * @return Returns the campusOverrides.
	 */
	public Collection getCampusOverrides() {
		return campusOverrides;
	}
	/**
	 * @param campusOverrides The campusOverrides to set.
	 */
	public void setCampusOverrides(Collection campusOverrides) {
		this.campusOverrides = campusOverrides;
	}
}
