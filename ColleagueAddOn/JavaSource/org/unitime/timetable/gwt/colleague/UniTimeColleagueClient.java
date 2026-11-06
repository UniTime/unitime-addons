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
package org.unitime.timetable.gwt.colleague;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.page.SolverWarnings;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.resources.ColleagueGwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RootPanel;

public class UniTimeColleagueClient extends Client {
	protected static final ColleagueGwtMessages BANNER_MESSAGES = GWT.create(ColleagueGwtMessages.class);
	
	@Override
	public void init(String page) {
		try {
			// load banner page
			for (ColleaguePages p: ColleaguePages.values()) {
				if (p.name().equals(page)) {
					RootPanel loading = RootPanel.get("UniTimeGWT:Loading");
					if (loading != null) loading.setVisible(false);
					LoadingWidget.getInstance().setMessage(MESSAGES.waitLoading(p.name(BANNER_MESSAGES)));
					UniTimePageLabel.getInstance().setPageName(p.name(BANNER_MESSAGES));
					Window.setTitle("UniTime " + CONSTANTS.version() + "| " + p.name(BANNER_MESSAGES));
					RootPanel.get("UniTimeGWT:Body").add(p.widget());
					RootPanel warns = RootPanel.get("UniTimeGWT:SolverWarnings");
					if (warns != null)
						warns.add(SolverWarnings.createInstance(SolverWarnings.Type.locks));
					return;
				}
			}
			// fallback to other pages
			super.init(page);
		} catch (Exception e) {
			Label error = new Label(MESSAGES.failedToLoadPage(e.getMessage()));
			error.setStyleName("unitime-ErrorMessage");
			RootPanel.get("UniTimeGWT:Body").add(error);
			UniTimeNotifications.error(MESSAGES.failedToLoadPage(e.getMessage()), e);
		}
	}
	
	@Override
	public void onModuleLoadDeferred() {
		super.onModuleLoadDeferred();
	}
}
