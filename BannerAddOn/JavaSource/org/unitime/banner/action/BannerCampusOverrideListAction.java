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
import org.unitime.banner.model.BannerCampusOverride;
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
@Action(value = "bannerCampusOverrideList", results = {
		@Result(name = "list", type = "tiles", location = "bannerCampusOverrideList.tiles"),
		@Result(name = "add", type = "redirect", location = "/bannerCampusOverrideEdit.action", params = { "op", "${op}" })
	})
@TilesDefinition(name = "showBannerCampusOverrideList.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Banner Campus Overrides"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerCampusOverrideList.jsp")
	})
public class BannerCampusOverrideListAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = -7897881232355392403L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	private Integer order;
	
	public Integer getOrder() { return order; }
	public void setOrder(Integer order) { this.order = order; }

	@Override
	public String execute() throws Exception {

	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);
		
		if (BMSG.actionAddCampusOverride().equals(op))
			return "add";

		return "list";
		
	}
	
	public String getTable() {
		WebTable webTable = new WebTable(
				3, "", "bannerCampusOverrideList.action?order=%%",					
				new String[] {
					BMSG.colBannerCampusCode(),
					BMSG.colBannerCampusName(),
					BMSG.colBannerCampusVisible()},
				new String[] { "left", "left", "center" }, 
				new boolean[] { true, true, false });

		webTable.enableHR("#EFEFEF");
        webTable.setRowStyle("white-space: nowrap");
		for (BannerCampusOverride bco: BannerCampusOverride.getAllBannerCampusOverrides()) {
			WebTableLine line = webTable.addLine(
					"onClick=\"document.location='bannerCampusOverrideEdit.action?op=Edit&campusOverrideId=" + bco.getUniqueId() + "';\"",
					new String[] {
						bco.getBannerCampusCode(),
						bco.getBannerCampusName(),
						bco.getVisible() ? "<img src='images/accept.png'> " : "" }, 
					new Comparable[] {
						bco.getBannerCampusCode(),
						bco.getBannerCampusName(),
						bco.getVisible()
					});
			line.setUniqueId(bco.getUniqueId().toString());
		}
        return webTable.printTable(order == null ? 1 : order.intValue());
	}

}
