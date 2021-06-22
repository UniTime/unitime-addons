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
package org.unitime.timetable.gwt.banner.test;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;

public class TestPage extends SimpleForm {
	
	public TestPage() {
		UniTimeHeaderPanel header = new UniTimeHeaderPanel("Header");
		header.addButton("button", "Button", new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeNotifications.info("Button clicked.");
			}
		});
		addHeaderRow(header);
		addRow("Text:", new TextBox());
		addRow("Toggle:", new CheckBox("Check me"));
		ListBox list = new ListBox();
		list.addItem("One"); list.addItem("Two"); list.addItem("Three");
		addRow("List:", list);
		addBottomRow(header.clonePanel());
	}
	
	public void insert(RootPanel panel) {
		panel.add(this);
		panel.setVisible(true);
	}
	
	public static native void registerTriggers()/*-{
		$wnd.alert('Create test triggers.');
	}-*/;
}
