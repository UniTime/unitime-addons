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

import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.RollForwardSessionPage.ErrorsWidget;
import org.unitime.timetable.gwt.client.admin.RollForwardSessionPage.SimpleAction;
import org.unitime.timetable.gwt.client.admin.RollForwardSessionPage.SingleIdListBox;
import org.unitime.timetable.gwt.client.exams.ReportQueueTable;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.shared.BannerRollForwardSessionInterface;
import org.unitime.timetable.gwt.shared.BannerRollForwardSessionInterface.BannerRollForwardSessionRequest;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.Operation;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardSessionRequest;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardSessionResponse;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueItemInterface;
import org.unitime.timetable.gwt.shared.ScriptInterface.QueueType;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.user.client.History;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;

public class BannerRollForwardSessionPage extends Composite {
	protected static CourseMessages MSG = GWT.create(CourseMessages.class);
	protected static GwtMessages GWT_MSG = GWT.create(GwtMessages.class);
	protected static BannerMessages BMSG = GWT.create(BannerMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iPanel;
	private ReportQueueTable iQueue;
	private RollForwardSessionResponse iConfig;
	private UniTimeHeaderPanel iHeader, iFooter;
	private BannerRollForwardSessionInterface iData;
	private int iFirstRow = -1;
	private ErrorsWidget iErrors;	
	
	public BannerRollForwardSessionPage() {
		iPanel = new SimpleForm(3);
		iPanel.addStyleName("unitime-RollForwardSessionPage");
		initWidget(iPanel);
		
		iQueue = new ReportQueueTable(QueueType.RollForward);
		iQueue.addMouseClickListener(new MouseClickListener<QueueItemInterface>() {
			@Override
			public void onMouseClick(TableEvent<QueueItemInterface> event) {
				if (event.getData() != null && iQueue.isSelected(event.getRow())) {
					History.newItem(event.getData().getId(), false);
					populate(event.getData().getId());
				} else {
					History.newItem("", false);
					clearForm();
				}
			}
		});
		iQueue.attach(iPanel, MSG.sectRollForwardsInProgress());
		iConfig = new RollForwardSessionResponse();
		iData = new BannerRollForwardSessionInterface();

		iErrors = new ErrorsWidget();
		
		iHeader = new UniTimeHeaderPanel(MSG.sectRollForwardActions());
		iHeader.addButton("refresh", MSG.buttonRefresh(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				init();
				iQueue.refreshQueue(null);
			}
		});
		iHeader.addButton("execute", MSG.actionRollForward(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				executeRoll();
			}
		});
		iHeader.getButton("execute").setTitle(MSG.titleRollForward(MSG.accessRollForward()));
		iHeader.getButton("execute").setAccessKey(MSG.accessRollForward().charAt(0));
		iHeader.setEnabled("execute", false);
		iFooter = iHeader.clonePanel("");
		
		init();
		
		History.addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (event.getValue().isEmpty()) {
					if (iQueue.getSelectedRow() >= 0)
						iQueue.setSelected(iQueue.getSelectedRow(), false);
					iQueue.refreshQueue(null);
					clearForm();
				} else {
					iQueue.refreshQueue(event.getValue());
					populate(event.getValue());
				}
			}
		});
		if (History.getToken() != null && !History.getToken().isEmpty()) {
			iQueue.refreshQueue(History.getToken());
			populate(History.getToken());
		}
	}
	
	protected void clearForm() {
		iData.setRollForwardBannerSession(false);
		iData.setCreateMissingBannerSections(false);
		initForm();
		iErrors.clearErrors();
	}
	
	protected void init() {
		RPC.execute(new BannerRollForwardSessionRequest(Operation.LOAD, iData), new AsyncCallback<RollForwardSessionResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				UniTimeNotifications.error(GWT_MSG.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(RollForwardSessionResponse result) {
				iConfig = result;
				iData.setSessionToRollForwardTo(iConfig.getToSessionId());
				initForm();
			}
		});		
	}
	
	protected void populate(final String id) {
		iHeader.showLoading();
		RPC.execute(new RollForwardSessionRequest(Operation.POPULATE, id), new AsyncCallback<RollForwardSessionResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				iHeader.clearMessage();
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(RollForwardSessionResponse result) {
				iHeader.clearMessage();
				if (result.getData() != null) {
					if (result.getData() instanceof BannerRollForwardSessionInterface)
						iData = (BannerRollForwardSessionInterface)result.getData();
					else
						clearForm();
					initForm();
					iErrors.clearErrors();
				}
			}
		});
	}
	
	protected void executeRoll() {
		if (!validateRoll()) return;
		LoadingWidget.getInstance().show(GWT_MSG.waitPlease());
		iHeader.clearMessage();
		RPC.execute(new BannerRollForwardSessionRequest(Operation.EXECUTE, iData), new AsyncCallback<RollForwardSessionResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(caught.getMessage());
				UniTimeNotifications.error(caught.getMessage(), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(RollForwardSessionResponse result) {
				LoadingWidget.getInstance().hide();
				iQueue.refreshQueue(result.getQueueId());
			}
		});
	}
	
	protected boolean validateRoll() {
		List<String> errors = new ArrayList<String>();
		if (iData.getSessionToRollForwardTo() == null)
			errors.add(MSG.errorRollForwardMissingToSession());
		boolean oneSelected = false;
		if (iData.getRollForwardBannerSession()) {
			oneSelected = true;
			if (iData.getSessionToRollBannerDataForwardFrom() == null)
				errors.add(MSG.errorRollForwardMissingFromSession(BMSG.rollForwardBannerSession()));
		}
		if (iData.getCreateMissingBannerSections()) {
			oneSelected = true;
		}

		if (!oneSelected) {
			errors.add(MSG.errorRollForwardMissingAction());
		}

		iErrors.setErrors(errors);

		return errors.isEmpty();
	}
	
	protected void initForm() {
		if (iFirstRow >= 0)
			for (int row = iPanel.getRowCount() - 1; row >= iFirstRow; row--)
				iPanel.removeRow(row);
		iFirstRow = iPanel.addHeaderRow(iHeader);
		iPanel.addRow(iErrors);
		
		SingleIdListBox sessionToRollForwardTo = new SingleIdListBox(iConfig.getToSessions(), iData.getSessionToRollForwardTo());
		sessionToRollForwardTo.addValueChangeHandler(new ValueChangeHandler<Long>() {
			@Override
			public void onValueChange(ValueChangeEvent<Long> event) {
				iData.setSessionToRollForwardTo(event.getValue());
				init();
			}
		});
		
		int row = iPanel.addRow(MSG.propSessionToRollForwardTo(), sessionToRollForwardTo);
		iPanel.getRowFormatter().addStyleName(row, "extra-space-below");
		
		if (!iConfig.hasFromSessions()) {
			iHeader.setEnabled("execute", false);
			iPanel.addBottomRow(iFooter);
			return;
		}

		iPanel.addRow(new SimpleAction(BMSG.propRollBannerSessionDataFrom(), null,
				iData.getRollForwardBannerSession(), iData.getSessionToRollBannerDataForwardFrom(), iConfig) {
			@Override
			public void update(Boolean check, Long fromSessionId) {
				iData.setRollForwardBannerSession(check);
				iData.setSessionToRollBannerDataForwardFrom(fromSessionId);
			}
		});
		
		CheckBox createMissingBannerSections = new CheckBox(BMSG.checkCreateMissingBannerSections());
		createMissingBannerSections.setValue(Boolean.TRUE.equals(iData.getCreateMissingBannerSections()));
		createMissingBannerSections.addValueChangeHandler(new ValueChangeHandler<Boolean>() {
			@Override
			public void onValueChange(ValueChangeEvent<Boolean> event) {
				iData.setCreateMissingBannerSections(event.getValue());
			}
		});
		iPanel.addRow(createMissingBannerSections);
		
		iHeader.setEnabled("execute", true);
		iPanel.addBottomRow(iFooter);
	}
	

}
