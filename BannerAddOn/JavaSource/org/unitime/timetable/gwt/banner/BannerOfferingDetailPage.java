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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.offerings.OfferingsInterface.OfferingConfigInterface;
import org.unitime.timetable.gwt.client.page.UniTimeNavigation;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.PropertyInterface;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Anchor;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class BannerOfferingDetailPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	private static final BannerMessages BANNER = GWT.create(BannerMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private BannerOfferingDetailResponse iResponse;
	
	public BannerOfferingDetailPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-OfferingDetailPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);
		
		String id = Window.Location.getParameter("id");
		if (id == null)
			id = Window.Location.getParameter("bc");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(COURSE.errorNoOfferingId());
		} else {
			load(Long.valueOf(id), null);	
		}
		
		iHeader.addButton("lock", COURSE.actionLockIO(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				load(iResponse.getBannerCourseId(), BannerOfferingDetailRequest.Action.Lock);
			}
		});
		iHeader.setEnabled("lock", false);
		iHeader.getButton("lock").setAccessKey(COURSE.accessLockIO().charAt(0));
		iHeader.getButton("lock").setTitle(COURSE.titleLockIO(COURSE.accessLockIO()));
		
		iHeader.addButton("unlock", COURSE.actionUnlockIO(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				load(iResponse.getBannerCourseId(), BannerOfferingDetailRequest.Action.Unlock);
			}
		});
		iHeader.setEnabled("unlock", false);
		iHeader.getButton("unlock").setAccessKey(COURSE.accessUnlockIO().charAt(0));
		iHeader.getButton("unlock").setTitle(COURSE.titleUnlockIO(COURSE.accessUnlockIO()));
		
		iHeader.addButton("resend", BANNER.actionResendToBanner(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				load(iResponse.getBannerCourseId(), BannerOfferingDetailRequest.Action.ResendToBanner);
			}
		});
		iHeader.setEnabled("resend", false);

		iHeader.addButton("previous", COURSE.actionPreviousIO(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "bannerOffering?bc=" + iResponse.getPreviousId());
			}
		});
		iHeader.setEnabled("previous", false);
		iHeader.getButton("previous").setAccessKey(COURSE.accessPreviousIO().charAt(0));
		iHeader.getButton("previous").setTitle(COURSE.titlePreviousIO(COURSE.accessPreviousIO()));
		
		iHeader.addButton("next", COURSE.actionNextIO(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "bannerOffering?bc=" + iResponse.getNextId());
			}
		});
		iHeader.setEnabled("next", false);
		iHeader.getButton("next").setAccessKey(COURSE.accessNextIO().charAt(0));
		iHeader.getButton("next").setTitle(COURSE.titleNextIO(COURSE.accessNextIO()));
		
		iHeader.addButton("back", BANNER.actionBackToBannerOfferings(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "back.action?uri=" + URL.encodeQueryString(iResponse.getBackUrl()) +
						"&backId=" + iResponse.getOfferingId() + "&backType=InstructionalOffering");
			}
		});
		iHeader.setEnabled("back", false);
		iHeader.getButton("back").setTitle(BANNER.titleBackToBannerOfferingDetail(BANNER.accessBackToBannerOfferingDetail()));
		iHeader.getButton("back").setAccessKey(BANNER.accessBackToBannerOfferingDetail().charAt(0));

		iFooter = iHeader.clonePanel();
	}
	
	protected void load(Long bannerCourseId, BannerOfferingDetailRequest.Action action) {
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		BannerOfferingDetailRequest req = new BannerOfferingDetailRequest();
		req.setBannerCourseId(bannerCourseId);
		req.setAction(action);
		req.setBackId(Window.Location.getParameter("backId"));
		req.setBackType(Window.Location.getParameter("backType"));
		req.setExamId(Window.Location.getParameter("examId"));
		RPC.execute(req, new AsyncCallback<BannerOfferingDetailResponse>() {

			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(final BannerOfferingDetailResponse response) {
				iResponse = response;
				if (response.hasUrl()) {
					ToolBox.open(GWT.getHostPageBaseURL() + response.getUrl());
					return;
				}
				LoadingWidget.getInstance().hide();
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				
				iHeader.getHeaderTitlePanel().clear();
				Anchor anchor = new Anchor(response.getName());
				anchor.setHref("bannerOfferings?subjectArea=" + response.getSubjectAreaId() + "&courseNbr=" + response.getCoruseNumber() + "#A" + response.getOfferingId());
				anchor.setAccessKey(COURSE.accessBackToIOList().charAt(0));
				anchor.setTitle(COURSE.titleBackToIOList(COURSE.accessBackToIOList()));
				anchor.setStyleName("l8");
				iHeader.getHeaderTitlePanel().add(anchor);
				
				TableWidget coursesTable = new TableWidget(response.getCourses());
				coursesTable.setStyleName("unitime-InnerTable");
				coursesTable.getElement().getStyle().setWidth(100.0, Unit.PCT);
				iPanel.addRow(COURSE.propertyCourseOfferings(), coursesTable);
				if (response.hasProperties())
					for (PropertyInterface property: response.getProperties().getProperties())
						iPanel.addRow(property.getName(), new TableWidget.CellWidget(property.getCell(), true));
				if (response.hasConfigs()) {
					for (final BannerOfferingConfigInterface config: response.getConfigs()) {
						UniTimeHeaderPanel hp = new UniTimeHeaderPanel(config.getName());
						iPanel.addHeaderRow(hp);
						if (config.hasSchedulingDisclaimer()) {
							Label disclaimer = new Label(config.getSchedulingDisclaimer());
							disclaimer.addStyleName("note");
							iPanel.addRow(COURSE.propertySchedulingDisclaimer(), disclaimer);
						}
						iPanel.addRow(new TableWidget(config));
						if (config.hasAnchor()) {
							Anchor a = new Anchor(); a.setName(config.getAnchor()); a.getElement().setId(config.getAnchor());
							hp.insertLeft(a, false);
						}
						if (config.hasOperation("banner-config-edit")) {
							hp.addButton("banner-config-edit", BANNER.actionEditBannerConfig(), new ClickHandler() {
								@Override
								public void onClick(ClickEvent evt) {
									ToolBox.open(GWT.getHostPageBaseURL() + "bannerConfigEdit?id=" + config.getBannerConfigId());
								}
							});
							hp.getButton("banner-config-edit").setTitle(BANNER.titleEditBannerConfig());
						}
					}
				}
				
				if (response.hasLastChanges()) {
					UniTimeHeaderPanel hp = new UniTimeHeaderPanel(response.getLastChanges().getName());
					iPanel.addHeaderRow(hp);
					iPanel.addRow(new TableWidget(response.getLastChanges()));
				}
				
				iPanel.addBottomRow(iFooter);
				
				Scheduler.get().scheduleDeferred(new ScheduledCommand() {
					@Override
					public void execute() {
						String token = Window.Location.getHash();
						if (token != null && (token.startsWith("#A") || token.equals("#back")) || token.startsWith("#ioc")) {
							Element e = Document.get().getElementById(token.substring(1));
							if (e != null) ToolBox.scrollToElement(e);
						}
						Element e = Document.get().getElementById("back");
						if (e != null)
							ToolBox.scrollToElement(e);
					}
				});
				
				for (String op: iHeader.getOperations())
					iHeader.setEnabled(op, response.hasOperation(op));
				UniTimeNavigation.getInstance().refresh();
			}
		});
	}
	
	public static class BannerOfferingDetailRequest implements GwtRpcRequest<BannerOfferingDetailResponse> {
		private Long iBannerCourseId;
		private String iBackId, iBackType, iExamId;
		private Action iAction;

		public static enum Action {
			Lock, Unlock, ResendToBanner,
		}
		
		public BannerOfferingDetailRequest() {}
		
		public void setBannerCourseId(Long bannerCourseId) { iBannerCourseId = bannerCourseId; }
		public Long getBannerCourseId() { return iBannerCourseId; }
		
		public String getBackId() { return iBackId; }
		public void setBackId(String backId) { iBackId = backId; }
		public String getBackType() { return iBackType; }
		public void setBackType(String backType) { iBackType = backType; }
		public String getExamId() { return iExamId; }
		public void setExamId(String examId) { iExamId = examId; }
		public Action getAction() { return iAction; }
		public void setAction(Action action) { iAction = action; }
	}
	
	public static class BannerOfferingConfigInterface extends OfferingConfigInterface {
		private Long iBannerConfigId;
		
		public Long getBannerConfigId() { return iBannerConfigId; }
		public void setiBannerConfigId(Long bannerConfigId) { iBannerConfigId = bannerConfigId; }
	}
	
	public static class BannerOfferingDetailResponse implements GwtRpcResponse {
		private Long iOfferingId, iBannerCourseId, iPreviousId, iNextId;
		private Long iSubjectAreaId, iCourseId;
		private String iCourseNumber;
		private String iName;
		private TableInterface iCourses;
		private TableInterface iProperties;
		private List<BannerOfferingConfigInterface> iConfigurations;
		private Set<String> iOperations;
		private TableInterface iLastChanges;
		private boolean iOffered;
		private boolean iConfirms;
		private String iUrl;
		private String iBackUrl, iBackTitle;
		
		public BannerOfferingDetailResponse() {}

		public void setOfferingId(Long offeringId) { iOfferingId = offeringId; }
		public Long getOfferingId() { return iOfferingId; }
		public void setBannerCourseId(Long bannerCourseId) { iBannerCourseId = bannerCourseId; }
		public Long getBannerCourseId() { return iBannerCourseId; }
		public void setPreviousId(Long id) { iPreviousId = id; }
		public Long getPreviousId() { return iPreviousId; }
		public void setNextId(Long id) { iNextId = id; }
		public Long getNextId() { return iNextId; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public boolean isOffered() { return iOffered; }
		public void setOffered(boolean offered) { iOffered = offered; }
		public boolean isConfirms() { return iConfirms; }
		public void setConfirms(boolean confirms) { iConfirms = confirms; }
		public boolean hasUrl() { return iUrl != null && !iUrl.isEmpty(); }
		public void setUrl(String url) { iUrl = url; }
		public String getUrl() { return iUrl; }
		public Long getSubjectAreaId() { return iSubjectAreaId; }
		public void setSubjectAreaId(Long subjectAreaId) { iSubjectAreaId = subjectAreaId; }
		public String getCoruseNumber() { return iCourseNumber; }
		public void setCourseNumber(String courseNumber) { iCourseNumber = courseNumber; }
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public boolean hasBackUrl() { return iBackUrl != null && !iBackUrl.isEmpty(); }
		public void setBackUrl(String backUrl) { iBackUrl = backUrl; }
		public String getBackUrl() { return iBackUrl; }
		public boolean hasBackTitle() { return iBackTitle != null && !iBackTitle.isEmpty(); }
		public void setBackTitle(String backTitle) { iBackTitle = backTitle; }
		public String getBackTitle() { return iBackTitle; }
		
		public boolean hasOperation(String operation) { return iOperations != null && iOperations.contains(operation); }
		public void addOperation(String operation) {
			if (iOperations == null) iOperations = new HashSet<String>();
			iOperations.add(operation);
		}
		
		public boolean hasProperties() { return iProperties != null && !iProperties.hasProperties(); }
		public void addProperty(PropertyInterface property) {
			if (iProperties == null) iProperties = new TableInterface();
			iProperties.addProperty(property);
		}
		public TableInterface getProperties() { return iProperties; }
		public CellInterface addProperty(String text) {
			PropertyInterface p = new PropertyInterface();
			p.setName(text);
			p.setCell(new CellInterface());
			addProperty(p);
			return p.getCell();
		}
		
		public boolean hasConfigs() { return iConfigurations != null && !iConfigurations.isEmpty(); }
		public List<BannerOfferingConfigInterface> getConfigs() { return iConfigurations; }
		public void addConfig(BannerOfferingConfigInterface configuration) {
			if (iConfigurations == null) iConfigurations = new ArrayList<BannerOfferingConfigInterface>();
			iConfigurations.add(configuration);
		}
		
		public TableInterface getCourses() { return iCourses; }
		public void setCourses(TableInterface courses) { iCourses = courses; }

		public boolean hasLastChanges() { return iLastChanges != null; }
		public TableInterface getLastChanges() { return iLastChanges; }
		public void setLastChanges(TableInterface lastChanges) { iLastChanges = lastChanges; }
	}
}
