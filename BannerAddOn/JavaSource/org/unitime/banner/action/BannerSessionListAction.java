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
import org.unitime.banner.model.BannerSession;
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
@Action(value = "bannerSessionList", results = {
		@Result(name = "list", type = "tiles", location = "bannerSessionList.tiles"),
		@Result(name = "add", type = "redirect", location = "/bannerSessionEdit.action", params = { "op", "${op}" })
	})
@TilesDefinition(name = "bannerSessionList.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Banner Academic Sessions"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerSessionList.jsp")
	})
public class BannerSessionListAction extends UniTimeAction<BlankForm> {
	private static final long serialVersionUID = 1534495218377965647L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	private Integer order;
	
	public Integer getOrder() { return order; }
	public void setOrder(Integer order) { this.order = order; }
	
	@Override
	public String execute() throws Exception {

	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionAdd);
		
		
		if (order != null)
			WebTable.setOrder(sessionContext, "BannerSessionList.ord", order.toString(), 3);

		if (BMSG.actionAddBannerSession().equals(op))
			return "add";
		
		return "list";	
	}
	
	public String getTable() {
		WebTable webTable = new WebTable(
				12, "", "bannerSessionList.action?order=%%",					
				new String[] {
					MSG.columnAcademicSession(),
					MSG.columnAcademicInitiative(),
					BMSG.colBannerTermCode(),
					BMSG.colBannerCampus(), 
					BMSG.colStoreDataForBanner(),
					BMSG.colSendDataToBanner(),
					BMSG.colLoadingOfferings(),
					BMSG.colFutureTerm(),
					BMSG.colUpdateMode(),
					BMSG.colStudentCampus(),
					BMSG.colUseStudentAreaPrefix(),
					BMSG.colSubjectAreaPrefixDelim()},
				new String[] { "left", "left", "left", "left",
					"center", "center", "center", "left", "left", "left", "center", "left"}, 
				new boolean[] { true, true, false, false, false, true, true, true, true, true, true });
				
		webTable.enableHR("#EFEFEF");
        webTable.setRowStyle("white-space: nowrap");
		
        for (BannerSession s: BannerSession.getAllSessions()) {
        	WebTableLine line = webTable.addLine(
					"onClick=\"document.location='bannerSessionEdit.action?op=Edit&sessionId=" + s.getUniqueId() + "';\"",
					new String[] {
						s.getSession().getLabel(),
						s.getSession().academicInitiativeDisplayString(),
						s.getBannerTermCode(),
						s.getBannerCampus(),
						s.isStoreDataForBanner().booleanValue() ? "<img src='images/accept.png'> " : "&nbsp; ", 
						s.isSendDataToBanner().booleanValue() ? "<img src='images/accept.png'> " : "&nbsp; ", 
						s.isLoadingOfferingsFile().booleanValue() ? "<img src='images/accept.png'> " : "&nbsp; ",
						s.getFutureSession() == null ? "" : s.getFutureSession().getLabel(),
						s.getFutureSessionUpdateModeLabel(),
						s.getStudentCampus(),
						(s.isUseSubjectAreaPrefixAsCampus() != null && s.isUseSubjectAreaPrefixAsCampus().booleanValue()) ? "<img src='images/accept.png'> " : "&nbsp; ",
						s.getSubjectAreaPrefixDelimiter() == null ? "" : s.getSubjectAreaPrefixDelimiter()
						},
					new Comparable[] {
						s.getSession().getLabel(),
						s.getSession().academicInitiativeDisplayString(),
						s.getBannerTermCode(),
						s.getBannerCampus(),
						s.isStoreDataForBanner(),
						s.isSendDataToBanner(),
						s.isLoadingOfferingsFile(),
						s.getFutureSession() == null ? "" : s.getFutureSession().getLabel(),
						s.getFutureSessionUpdateModeLabel(),
						s.getStudentCampus() == null ? "" : s.getStudentCampus(),
						s.isUseSubjectAreaPrefixAsCampus(),
						s.getSubjectAreaPrefixDelimiter() == null ? "" : s.getSubjectAreaPrefixDelimiter()} );
        	line.setUniqueId(s.getUniqueId().toString());
        }
        
        return webTable.printTable(WebTable.getOrder(sessionContext, "BannerSessionList.ord"));
	}

}
