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
package org.unitime.timetable.gwt.banner;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class BannerCatalogPage extends SimpleForm {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	public BannerCatalogPage() {
		addStyleName("unitime-BannerCatalogPage");
		removeStyleName("unitime-NotPrintableBottomLine");
		
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		RPC.execute(new CatalogRequest(Window.Location.getParameter("term"), Window.Location.getParameter("subjectCode"), Window.Location.getParameter("courseNumber")),
				new AsyncCallback<CatalogResponse>() {
					@Override
					public void onSuccess(CatalogResponse response) {
						LoadingWidget.getInstance().hide();
						if (response.hasPageLabel())
							UniTimePageLabel.getInstance().setPageName(response.getPageLabel());
						if (response.hasSections())
							for (CatalogSection section: response.getSections()) {
								addHeaderRow(section.getTitle());
								HTML content = new HTML(section.getContent());
								content.addStyleName("content");
								addRow(content);
							}
						if (response.hasDisclaimer()) {
							HTML disclaimer = new HTML(response.getDisclaimer());
							disclaimer.addStyleName("disclaimer");
							addBottomRow(new Label());
							addRow(disclaimer);
						}
					}
					
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
						
					}
				});
	}
	
	public static class CatalogRequest implements GwtRpcRequest<CatalogResponse>{
		private String iTerm;
		private String iSubject;
		private String iCourseNbr;
		private boolean iDetailsFirst = true;
		
		public CatalogRequest() {}
		public CatalogRequest(String term, String subject, String courseNbr) {
			iTerm = term;
			iSubject = subject;
			iCourseNbr = courseNbr;
		}
		public CatalogRequest(String term, String subject, String courseNbr, boolean detailsFirst) {
			this(term, subject, courseNbr);
			setDetailsFirst(detailsFirst);
		}
		
		public String getTerm() { return iTerm; }
		public void setTerm(String term) { iTerm = term; }
		public String getSubject() { return iSubject; }
		public void setSubject(String subject) { iSubject = subject; }
		public String getCourseNbr() { return iCourseNbr; }
		public void setCourseNbr(String courseNbr) { iCourseNbr = courseNbr; }
		
		public boolean isDetailsFirst() { return iDetailsFirst; }
		public void setDetailsFirst(boolean detailsFirst) { iDetailsFirst = detailsFirst; }
		
		@Override
		public String toString() {
			return getSubject() + " " + getCourseNbr() + " (" + getTerm() + ")";
		}
	}
	
	public static class CatalogSection implements IsSerializable {
		private String iTitle;
		private String iContent;
		
		public CatalogSection() {}
		public CatalogSection(String title, String content) {
			iTitle = title;
			iContent = content;
		}
		
		public String getTitle() { return iTitle; }
		public void setTitle(String title) { iTitle = title; }
		
		public String getContent() { return iContent; }
		public void setConcent(String content) { iContent = content; }
	}
	
	public static class CatalogResponse implements GwtRpcResponse{
		private String iDisclaimer;
		private List<CatalogSection> iSections;
		private String iPageLabel;
		
		public CatalogResponse() {}
		
		public boolean hasSections() { return iSections != null && !iSections.isEmpty(); }
		public List<CatalogSection> getSections() { return iSections; }
		public void addSection(CatalogSection section) {
			if (section == null || section.getContent() == null || section.getContent().isEmpty()) return;
			if (iSections == null) iSections = new ArrayList<CatalogSection>();
			iSections.add(section);
		}
		public void addSection(String title, String content) {
			addSection(new CatalogSection(title, content));
		}
		public boolean hasDisclaimer() { return iDisclaimer != null && !iDisclaimer.isEmpty(); }
		public String getDisclaimer() { return iDisclaimer; }
		public void setDisclaimer(String disclaimer) { iDisclaimer = disclaimer; }

		public boolean hasPageLabel() { return iPageLabel != null && !iPageLabel.isEmpty(); }
		public String getPageLabel() { return iPageLabel; }
		public void setPageLabel(String pageLabel) { iPageLabel = pageLabel; }
	}

}