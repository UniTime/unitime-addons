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
package org.unitime.timetable.gwt.client.responses;

import java.util.Map;
import java.util.Set;

import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.aria.AriaButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.rooms.RoomCookie;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterPanel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.BannerGwtConstants;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;
import org.unitime.timetable.gwt.shared.BannerResponseInterface;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesFilterRpcRequest;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesPagePropertiesRequest;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesPagePropertiesResponse;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesPageRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.EncodeQueryRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcRequest;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Label;

public class BannerResponsesPage extends SimpleForm {
	protected static final  BannerGwtMessages MESSAGES = GWT.create(BannerGwtMessages.class);
	protected static final BannerGwtConstants CONSTANTS = GWT.create(BannerGwtConstants.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	private FilterPanel iFilterPanel;
	private BannerResponsesFilterBox iFilterBox = null;
	private BannerResponsesTable iTable;
	private AriaButton iSearch, iExportCSV, iExportPDF;

	
	public BannerResponsesPage() {
		
		iFilterPanel = new FilterPanel();
		
		Label filterLabel = new Label(MESSAGES.propFilter());
		iFilterPanel.addLeft(filterLabel);
		
		iFilterBox = new BannerResponsesFilterBox();
		iFilterPanel.addLeft(iFilterBox);
		
		iSearch = new AriaButton(UniTimeHeaderPanel.stripAccessKey(MESSAGES.buttonSearch()));
		Character searchAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonSearch());
		if (searchAccessKey != null) {
			iSearch.setAccessKey(searchAccessKey);
		}
		iSearch.addStyleName("unitime-NoPrint");
		iFilterPanel.addRight(iSearch);
		iSearch.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				search();
			}
		});
	
		iExportCSV = new AriaButton(UniTimeHeaderPanel.stripAccessKey(MESSAGES.buttonExportCSV()));
		Character exportCsvAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonExportCSV());
		if (exportCsvAccessKey != null)
			iExportCSV.setAccessKey(exportCsvAccessKey);
		iExportCSV.addStyleName("unitime-NoPrint");
		iFilterPanel.addRight(iExportCSV);
		iExportCSV.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("banner-responses.csv");
			}
		});

		iExportPDF = new AriaButton(UniTimeHeaderPanel.stripAccessKey(MESSAGES.buttonExportPDF()));
		Character exportPdfAccessKey = UniTimeHeaderPanel.guessAccessKey(MESSAGES.buttonExportPDF());
		if (exportPdfAccessKey != null)
			iExportPDF.setAccessKey(exportCsvAccessKey);
		iExportPDF.addStyleName("unitime-NoPrint");
		iFilterPanel.addRight(iExportPDF);
		iExportPDF.addClickHandler(new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				export("banner-responses.pdf");
			}
		});
		addHeaderRow(iFilterPanel);

		iTable = new BannerResponsesTable() ;
		iTable.setVisible(false);
		addRow(iTable);

		
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingPage());
		RPC.execute(new BannerResponsesPagePropertiesRequest(), new AsyncCallback<BannerResponsesPagePropertiesResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilterBox.setErrorHint(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(BannerResponsesPagePropertiesResponse result) {
				LoadingWidget.getInstance().hide();
//				iTable.setProperties(result);
				Chip department = iFilterBox.getChip("department");
				if (department != null) {
					boolean match = false;
					for (DepartmentInterface d: result.getDepartments()) {
						if (d.getDeptCode().equalsIgnoreCase(department.getValue())) {
							match = true;
						}
					}
					if (!match) iFilterBox.setValue("", true);
				}
				if (result.getLastDepartmentId() != null && iFilterBox.getValue().isEmpty()) {
					for (DepartmentInterface d: result.getDepartments()) {
						if (d.getId().equals(result.getLastDepartmentId())) {
							iFilterBox.setValue("department:\"" + d.getDeptCode() + "\"", true);
							break;
						}
					}
				}
			}
		});
			
}

	void search() {

		History.newItem(iFilterBox.getValue(), false);
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingBannerQueueResponses());
		final BannerResponsesFilterRpcRequest filter = iFilterBox.getElementsRequest();
		RPC.execute(new BannerResponsesPageRequest(filter), new AsyncCallback<GwtRpcResponseList<BannerResponseInterface>>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iFilterBox.setErrorHint(MESSAGES.failedToLoadBannerResponses(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToLoadBannerResponses(caught.getMessage()), caught);
			}
	
			@Override
			public void onSuccess(GwtRpcResponseList<BannerResponseInterface> result) {
				LoadingWidget.getInstance().hide();
				iTable.populate(result);
				iTable.setVisible(true);
			}
	
		});
	
	}
	
	void export(String type) {
		RoomCookie cookie = RoomCookie.getInstance();
		String query = "output=" + type;
		FilterRpcRequest requests = iFilterBox.getElementsRequest();
		if (requests.hasOptions()) {
			for (Map.Entry<String, Set<String>> option: requests.getOptions().entrySet()) {
				for (String value: option.getValue()) {
					query += "&r:" + option.getKey() + "=" + URL.encodeQueryString(value);
				}
			}
		}
		if (requests.getText() != null && !requests.getText().isEmpty()) {
			query += "&r:text=" + URL.encodeQueryString(requests.getText());
		}
		query += //TODO: may have to add column and sort info here????
				"&grid=" + (cookie.isGridAsText() ? "0" : "1") +
				"&vertical=" + (cookie.areRoomsHorizontal() ? "0" : "1") +
				(cookie.hasMode() ? "&mode=" + cookie.getMode() : "");
		RPC.execute(EncodeQueryRpcRequest.encode(query), new AsyncCallback<EncodeQueryRpcResponse>() {
			@Override
			public void onFailure(Throwable caught) {
			}
			@Override
			public void onSuccess(EncodeQueryRpcResponse result) {
				ToolBox.open(GWT.getHostPageBaseURL() + "export?q=" + result.getQuery());
			}
		});
	}
	
	
	public void showLoading(String message) { LoadingWidget.getInstance().show(message); }
	
	public void hideLoading() { LoadingWidget.getInstance().hide(); }	
	
}
