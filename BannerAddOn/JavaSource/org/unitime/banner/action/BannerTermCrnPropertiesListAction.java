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
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.banner.model.BannerTermCrnProperties;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.form.BlankForm;
import org.unitime.timetable.security.rights.Right;

/**
 * 
 * @author says
 *
 */
@Action(value = "bannerTermCrnPropertiesList", results = {
		@Result(name = "list", type = "tiles", location = "bannerTermCrnPropertiesList.tiles"),
		@Result(name = "add", type = "redirect", location = "/bannerTermCrnPropertiesEdit.action", params = { "op", "${op}" })
	})
@TilesDefinition(name = "bannerTermCrnPropertiesList.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Banner Term CRN Properties"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerTermCrnPropertiesList.jsp")
	})
public class BannerTermCrnPropertiesListAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = -1477056452451805797L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	private Integer order;
	
	public Integer getOrder() { return order; }
	public void setOrder(Integer order) { this.order = order; }

	@Override
	public String execute() throws Exception {
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);
		
		if (BMSG.actionAddBannerSession().equals(op))
			return "add";

		return "list";
		
	}
	
	public String getTable() {
		WebTable webTable = new WebTable(
				6, "", "bannerTermCrnPropertiesList.action?order=%%",					
				new String[] {
					BMSG.colBannerTermCode(), BMSG.colBannerSessions(), BMSG.colLastCRN(), BMSG.colMinimumCRN(),
					BMSG.colMaximumCRN(), BMSG.colSearchFlag() },
				new String[] { "left", "left", "left", "left", "left", "center" }, 
				new boolean[] { true, true, true, true, true, false });
				
		webTable.enableHR("#EFEFEF");
        webTable.setRowStyle("white-space: nowrap");
        
        for (BannerTermCrnProperties s: BannerTermCrnProperties.getAllBannerTermCrnProperties()) {
    		WebTableLine line = webTable.addLine(
    				"onClick=\"document.location='bannerTermCrnPropertiesEdit.action?op=Edit&bannerTermCrnPropertiesId=" + s.getUniqueId() + "';\"",
    				new String[] {
    					s.getBannerTermCode(),
    					s.getBannerSessionsLabel(),
    					s.getLastCrn().toString(),
    					s.getMinCrn().toString(),
    					s.getMaxCrn().toString(),
    					s.isSearchFlag().booleanValue() ? "<img src='images/accept.png'> " : "&nbsp; "},
    				new Comparable[] {
    					s.getBannerTermCode(),
    					s.getBannerSessionsLabel(),
    					s.getLastCrn(),
    					s.getMinCrn(),
    					s.getMaxCrn(),
    					s.isSearchFlag().booleanValue() ? "<img src='images/accept.png'>" : "" } );
    		line.setUniqueId(s.getUniqueId().toString());
        }
        
        return webTable.printTable(order == null ? 1 : order.intValue());
	}

}
