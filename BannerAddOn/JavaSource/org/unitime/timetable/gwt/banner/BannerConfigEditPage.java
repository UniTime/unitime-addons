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
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.IdLabel;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.NumberBox;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.HasCellAlignment;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

public class BannerConfigEditPage extends Composite {
	private static final GwtMessages MESSAGES = GWT.create(GwtMessages.class);
	private static final CourseMessages COURSE = GWT.create(CourseMessages.class);
	private static final BannerMessages BANNER = GWT.create(BannerMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	public static NumberFormat sDF = NumberFormat.getFormat("##0.0");
	
	private SimplePanel iRootPanel;
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private BannerConfigEditResponse iData;
	
	public BannerConfigEditPage() {
		iPanel = new SimpleForm(2);
		iPanel.removeStyleName("unitime-NotPrintableBottomLine");
		
		iRootPanel = new SimplePanel(iPanel);
		iRootPanel.addStyleName("unitime-OfferingDetailPage");
		initWidget(iRootPanel);
		
		iHeader = new UniTimeHeaderPanel();
		iPanel.addHeaderRow(iHeader);

		iHeader.addButton("update", BANNER.actionUpdateBannerConfig(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				String error = validate();
				if (error == null) {
					iHeader.clearMessage();
					BannerConfigEditRequest request = new BannerConfigEditRequest();
					request.setBannerConfigId(iData.getBannerConfigId());
					request.setData(iData);
					LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
					RPC.execute(request, new AsyncCallback<BannerConfigEditResponse>() {
						@Override
						public void onFailure(Throwable caught) {
							LoadingWidget.getInstance().hide();
							iHeader.setErrorMessage(caught.getMessage());
							UniTimeNotifications.error(caught.getMessage(), caught);
							ToolBox.checkAccess(caught);
						}

						@Override
						public void onSuccess(BannerConfigEditResponse response) {
							ToolBox.open(GWT.getHostPageBaseURL() + "bannerOffering?bc=" + iData.getBannerCourseId() + "#ioc" + iData.getConfigId());
						}
					});
				} else {
					iHeader.setErrorMessage(error);
				}
			}
		});
		iHeader.setEnabled("update", false);
		iHeader.getButton("update").setTitle(BANNER.titleBackToBannerOfferingDetail(BANNER.accessUpdateBannerConfig()));
		iHeader.getButton("update").setAccessKey(BANNER.accessUpdateBannerConfig().charAt(0));		

		iHeader.addButton("back", BANNER.actionBackToBannerOfferingDetail(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent evt) {
				ToolBox.open(GWT.getHostPageBaseURL() + "bannerOffering?bc=" + iData.getBannerCourseId() + "#ioc" + iData.getConfigId());
			}
		});
		iHeader.setEnabled("back", false);
		iHeader.getButton("back").setTitle(BANNER.titleBackToBannerOfferingDetail(BANNER.accessBackToBannerOfferingDetail()));
		iHeader.getButton("back").setAccessKey(BANNER.accessBackToBannerOfferingDetail().charAt(0));		
		iFooter = iHeader.clonePanel();
		
		String id = Window.Location.getParameter("id");
		if (id == null || id.isEmpty()) {	
			LoadingWidget.getInstance().hide();
			iHeader.setErrorMessage(COURSE.errorNoOfferingId());
		} else {
			load(Long.valueOf(id));	
		}
	}
	
	private ListBox iGradableItype;
	private NumberBox iLabHours;
	private UniTimeTable<BannerSectionInterface> iTable;
	
	protected void load(Long bannerConfigId) {
		BannerConfigEditRequest request = new BannerConfigEditRequest();
		request.setBannerConfigId(bannerConfigId);
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
		RPC.execute(request, new AsyncCallback<BannerConfigEditResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(MESSAGES.failedToInitialize(caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedToInitialize(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(BannerConfigEditResponse response) {
				iData = response;
				iPanel.clear();
				iPanel.addHeaderRow(iHeader);
				iHeader.setHeaderTitle(response.getConfigName());
				iGradableItype = new ListBox();
				iGradableItype.addItem(BANNER.itemNoItype(), "");
				iGradableItype.getElement().getStyle().setProperty("min-width", "200px");
				if (iData.hasGradableItypes())
					for (IdLabel item: iData.getGradableItypes()) {
						iGradableItype.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(iData.getGradableItypeId()))
							iGradableItype.setSelectedIndex(iGradableItype.getItemCount() - 1);
					}
				iGradableItype.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent e) {
						String id = iGradableItype.getSelectedValue();
						if (id.isEmpty())
							iData.setGradableItypeId(null);
						else
							iData.setGradableItypeId(Long.valueOf(id));
					}
				});
				iPanel.addRow(BANNER.propConfigGradableItype(), iGradableItype);
				if (iData.isShowLabHours()) {
					iLabHours = new NumberBox();
					iLabHours.setDecimal(true); iLabHours.setNegative(false);
					iLabHours.setValue(iData.getLabHours());
					iLabHours.addValueChangeHandler(new ValueChangeHandler<String>() {
						public void onValueChange(ValueChangeEvent<String> e) {
							iData.setLabHours(iLabHours.toFloat());							
						}
					});
					Label name = new Label(BANNER.propLabHours()); name.getElement().getStyle().setPaddingLeft(20, Unit.PX);
					iPanel.addRow(name, iLabHours);
				}
				
				iTable = new UniTimeTable<BannerSectionInterface>();
				List<Widget> header = new ArrayList<Widget>();
				header.add(new UniTimeTableHeader("\u00a0"));
				header.add(new UniTimeTableHeader(COURSE.fieldIType()));
				header.add(new UniTimeTableHeader(BANNER.colSectionId()));
				header.add(new UniTimeTableHeader(COURSE.columnConsent()));
				header.add(new UniTimeTableHeader(BANNER.colCourseCreditOverride()));
				header.add(new UniTimeTableHeader(COURSE.columnCredit()));
				if (iData.isShowLimitOverrides()) {
					header.add(new UniTimeTableHeader(BANNER.colLimitOverride()));
					header.add(new UniTimeTableHeader(COURSE.columnLimit()));
				}
				header.add(new UniTimeTableHeader(BANNER.colCampusOverride()));
				header.add(new UniTimeTableHeader(COURSE.columnAssignedDatePattern()));
				header.add(new UniTimeTableHeader(COURSE.columnAssignedTime()));
				header.add(new UniTimeTableHeader(COURSE.columnAssignedRoom()));
				header.add(new UniTimeTableHeader(COURSE.columnInstructors()));
				
				iTable.addRow(null, header);

				if (iData.hasSections()) {
					int index = 0;
					for (BannerSectionInterface section: iData.getSections())
						iTable.addRow(section, toTableLine(section, index++));
				}
				
				iPanel.addRow(iTable);
				
				iPanel.addBottomRow(iFooter);
				
				iHeader.setEnabled("back", true);
				iHeader.setEnabled("update", true);
				LoadingWidget.getInstance().hide();
			}
		});
	}
	
	public List<Widget> toTableLine(final BannerSectionInterface section, int index) {
		List<Widget> line = new ArrayList<Widget>();
		Label label = new Label(section.getLabel());
		label.getElement().getStyle().setPaddingLeft(section.getIndent() * 20, Unit.PX);
		line.add(label);
		line.add(new Label(section.getItype()));
		if (section.isEditable()) {
			TextBox sect = new TextBox(); sect.setMaxLength(3); sect.setTabIndex(1000 + index);
			sect.setWidth("60px");
			sect.setText(section.getSection() == null ? "" : section.getSection());
			sect.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> e) {
					section.setSection(e.getValue() == null || e.getValue().isEmpty() ? null : e.getValue().toUpperCase());
				}
			});
			line.add(sect);
		} else {
			line.add(new Label(section.getSection()));
		}
		if (section.isEditable()) {
			final ListBox consent = new ListBox();
			consent.setTabIndex(2000 + index);
			consent.getElement().getStyle().setProperty("min-width", "200px");
			consent.addItem(COURSE.noConsentRequired(), "");
			if (iData.hasConsents())
				for (IdLabel item: iData.getConsents()) {
					consent.addItem(item.getLabel(), item.getId().toString());
					if (item.getId().equals(section.getConsentId()))
						consent.setSelectedIndex(consent.getItemCount() - 1);
				}
			consent.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent e) {
					String id = consent.getSelectedValue();
					section.setConsentId(id.isEmpty() ? null : Long.valueOf(id));
				}
			});
			line.add(consent);
		} else {
			IdLabel consent = iData.getConsent(section.getConsentId());
			line.add(new Label(consent == null ? "" : consent.getLabel()));
		}
		if (section.isEditable()) {
			final NumberBox credit = new NumberBox();
			credit.setTabIndex(3000 + index);
			credit.setWidth("60px");
			credit.setDecimal(true); credit.setNegative(false);
			credit.setValue(section.getCreditOverride());
			line.add(credit);
			credit.addValueChangeHandler(new ValueChangeHandler<String>() {
				@Override
				public void onValueChange(ValueChangeEvent<String> e) {
					section.setCreditOverride(credit.toFloat());
				}
			});
		} else {
			line.add(new RightLabel(section.getCreditOverride() == null ? "" : sDF.format(section.getCreditOverride())));
		}
		line.add(new RightLabel(section.getCredit() == null ? "" : section.getCredit()));
		if (iData.isShowLimitOverrides()) {
			if (section.isEditable()) {
				final NumberBox limit = new NumberBox();
				limit.setDecimal(false); limit.setNegative(false);
				limit.setValue(section.getLimitOverride());
				limit.setTabIndex(4000 + index);
				limit.setWidth("60px");
				line.add(limit);
				limit.addValueChangeHandler(new ValueChangeHandler<String>() {
					@Override
					public void onValueChange(ValueChangeEvent<String> e) {
						section.setLimitOverride(limit.toInteger());
					}
				});
			} else {
				line.add(new RightLabel(section.getCreditOverride() == null ? "" : sDF.format(section.getCreditOverride())));
			}
			line.add(new RightLabel(section.getLimit() == null ? "" : section.getLimit().toString()));
		}
		if (section.isEditable()) {
			final ListBox campus = new ListBox();
			campus.setTabIndex(5000 + index);
			campus.getElement().getStyle().setProperty("min-width", "200px");
			campus.addItem(BANNER.defaultCampusOverride(section.getDefaultCampusCode()), "");
			if (iData.hasCampusOverrides())
				for (IdLabel item: iData.getCampusOverrides()) {
					if ("1".equals(item.getDescription()) || item.getId().equals(section.getCampusId())) {
						campus.addItem(item.getLabel(), item.getId().toString());
						if (item.getId().equals(section.getCampusId()))
							campus.setSelectedIndex(campus.getItemCount() - 1);
					}
				}
			campus.addChangeHandler(new ChangeHandler() {
				@Override
				public void onChange(ChangeEvent e) {
					String id = campus.getSelectedValue();
					section.setCampusId(id.isEmpty() ? null : Long.valueOf(id));
				}
			});
			line.add(campus);
		} else {
			IdLabel campus = iData.getCampusOverride(section.getCampusId());
			line.add(new Label(campus != null ? campus.getLabel() : BANNER.defaultCampusOverride(section.getDefaultCampusCode())));
		}
		line.add(section.getDate() == null ? new Label() : new TableWidget.CellWidget(section.getDate()));
		line.add(section.getTime() == null ? new Label() : new TableWidget.CellWidget(section.getTime()));
		line.add(section.getRoom() == null ? new Label() : new TableWidget.CellWidget(section.getRoom()));
		line.add(section.getInstructor() == null ? new Label() : new TableWidget.CellWidget(section.getInstructor()));
		return line;
	}
	
	static class RightLabel extends Label implements HasCellAlignment {
		RightLabel(String label) {
			super(label);
		}

		@Override
		public HorizontalAlignmentConstant getCellAlignment() {
			return HasHorizontalAlignment.ALIGN_RIGHT;
		}
	}
	
	protected String validate() {
		if (iData.hasSections()) {
			Set<String> sections = new HashSet<String>();
			for (BannerSectionInterface section: iData.getSections()) {
				if (!section.isEditable() && section.getSection() != null)
					sections.add(section.getSection());
			}
			for (BannerSectionInterface section: iData.getSections()) {
				if (!section.isEditable()) continue;
				if (section.getSection() == null || section.getSection().isEmpty()) {
					return BANNER.errorSectionIndexMustBeSet(section.getLabel());
				}
				if (!section.getSection().matches("^[0-9A-Z]{1,3}$")) {
					return BANNER.errorSectionIndexNumbersAndLetters(section.getLabel());
				}
				if (!sections.add(section.getSection())) {
					return BANNER.errorSectionIndexNotUnique(section.getLabel());
				}
				if (section.getSection().equals("999")) {
					return BANNER.errorSectionIndex999(section.getLabel());
				}
				if (iData.isShowLimitOverrides()) {
					if (section.getLimitOverride() != null && section.getLimitOverride() > section.getLimit())
						return BANNER.errorLimitOverrideOverClassLimit(section.getLabel());
					if (section.getLimitOverride() != null && section.getLimitOverride() < 0)
						return BANNER.errorLimitOverrideBelowZero(section.getLabel());
				}
				if (section.getCreditOverride() != null && section.getCreditOverride() < 0)
					return BANNER.errorCourseCreditOverrideBelowZero(section.getLabel());
			}
		}
		return null;
	}
	
	
	public static class BannerSectionInterface implements IsSerializable {
		private Long iId;
		private Integer iIndent;
		private String iLabel;
		private String iItype;
		private String iSection, iOldSection;
		private Long iConsentId;
		private Float iCreditOverride;
		private String iCredit;
		private Long iCampusId;
		private CellInterface iDate;
		private CellInterface iTime;
		private CellInterface iRoom;
		private CellInterface iInstructor;
		private Integer iLimit, iLimitOverride;
		private boolean iEditable = true;
		private String iDefaultCampusCode;
		
		public BannerSectionInterface() {}
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public int getIndent() { return iIndent == null ? 0 : iIndent.intValue(); }
		public void setIndent(int indent) { iIndent = indent; }
		public String getLabel() { return iLabel; }
		public void setLabel(String label) { iLabel = label; }
		public String getItype() { return iItype; }
		public void setItype(String itype) { iItype = itype; }
		public String getSection() { return iSection; }
		public void setSection(String section) { iSection = section; }
		public String getOldSection() { return iOldSection; }
		public void setOldSection(String section) { iOldSection = section; }
		public Long getConsentId() { return iConsentId; }
		public void setConsentId(Long consentId) { iConsentId = consentId; }
		public Float getCreditOverride() { return iCreditOverride; }
		public void setCreditOverride(Float override) { iCreditOverride = override; }
		public String getCredit() { return iCredit; }
		public void setCredit(String credit) { iCredit = credit; }
		public Long getCampusId() { return iCampusId; }
		public void setCampusId(Long campusId) { iCampusId = campusId; }
		public CellInterface getDate() { return iDate; }
		public void setDate(CellInterface date) { iDate = date; }
		public CellInterface getTime() { return iTime; }
		public void setTime(CellInterface time) { iTime = time; }
		public CellInterface getRoom() { return iRoom; }
		public void setRoom(CellInterface room) { iRoom = room; }
		public CellInterface getInstructor() { return iInstructor; }
		public void setInstructor(CellInterface instructor) { iInstructor = instructor; }
		public Integer getLimit() { return iLimit; }
		public void setLimit(Integer limit) { iLimit = limit; }
		public boolean isEditable() { return iEditable; }
		public void setEditable(boolean editable) { iEditable = editable; }
		public boolean hasLimitOverride() { return iLimitOverride != null; }
		public void setLimitOverride(Integer limitOverride) { iLimitOverride = limitOverride; }
		public Integer getLimitOverride() { return iLimitOverride; }
		public String getDefaultCampusCode() { return iDefaultCampusCode; }
		public boolean hasDefaultCampusCode() { return iDefaultCampusCode != null && !iDefaultCampusCode.isEmpty(); }
		public void setDefaultCampusCode(String defaultCampusCode) {  iDefaultCampusCode = defaultCampusCode; }
	}
	
	public static class BannerConfigEditRequest implements GwtRpcRequest<BannerConfigEditResponse> {
		private Long iBannerConfigId;
		private BannerConfigEditResponse iData;
	
		public Long getBannerConfigId() { return iBannerConfigId; }
		public void setBannerConfigId(Long bannerConfigId) { iBannerConfigId = bannerConfigId; }

		public BannerConfigEditResponse getData() { return iData; }
		public void setData(BannerConfigEditResponse data) { iData = data; }
	}
	
	public static class BannerConfigEditResponse implements GwtRpcResponse{
		private Long iBannerConfigId;
		private Long iBannerCourseId;
		private Long iConfigId;
		private String iConfigName;
		private Long iGradableItypeId;
		private boolean iShowLimitOverrides = false, iShowLabHours = false;
		private Float iLabHours;
		private List<IdLabel> iGradableItypes;
		private List<IdLabel> iCampusOverrides;
		private List<IdLabel> iConsents;
		private List<BannerSectionInterface> iSections;
		
		public Long getBannerConfigId() { return iBannerConfigId; }
		public void setBannerConfigId(Long bannerConfigId) { iBannerConfigId = bannerConfigId; }
		public Long getConfigId() { return iConfigId; }
		public void setConfigId(Long configId) { iConfigId = configId; }
		public Long getBannerCourseId() { return iBannerCourseId; }
		public void setBannerCourseId(Long bannerCourseId) { iBannerCourseId = bannerCourseId; }
		public String getConfigName() { return iConfigName; }
		public void setConfigName(String configName) { iConfigName = configName; }
		public Long getGradableItypeId() { return iGradableItypeId; }
		public void setGradableItypeId(Long gradableItypeId) { iGradableItypeId = gradableItypeId; }
		
		public boolean hasGradableItypes() { return iGradableItypes != null && !iGradableItypes.isEmpty(); }
		public void addGradableItype(IdLabel gradableItype) {
			if (iGradableItypes == null) iGradableItypes = new ArrayList<IdLabel>();
			iGradableItypes.add(gradableItype);
		}
		public List<IdLabel> getGradableItypes() { return iGradableItypes; }
		public IdLabel getGradableItype(Long id) {
			if (iGradableItypes == null) return null;
			for (IdLabel item: iGradableItypes)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public boolean hasCampusOverrides() { return iCampusOverrides != null && !iCampusOverrides.isEmpty(); }
		public void addCampusOverride(IdLabel campusOverride) {
			if (iCampusOverrides == null) iCampusOverrides = new ArrayList<IdLabel>();
			iCampusOverrides.add(campusOverride);
		}
		public List<IdLabel> getCampusOverrides() { return iCampusOverrides; }
		public IdLabel getCampusOverride(Long id) {
			if (iCampusOverrides == null) return null;
			for (IdLabel item: iCampusOverrides)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public boolean hasConsents() { return iConsents != null && !iConsents.isEmpty(); }
		public void addConsent(IdLabel consent) {
			if (iConsents == null) iConsents = new ArrayList<IdLabel>();
			iConsents.add(consent);
		}
		public List<IdLabel> getConsents() { return iConsents; }
		public IdLabel getConsent(Long id) {
			if (iConsents == null) return null;
			for (IdLabel item: iConsents)
				if (item.getId().equals(id)) return item;
			return null;
		}
		
		public boolean hasSections() { return iSections != null && !iSections.isEmpty(); }
		public void addSection(BannerSectionInterface section) {
			if (iSections == null) iSections = new ArrayList<BannerSectionInterface>();
			iSections.add(section);
		}
		public List<BannerSectionInterface> getSections() { return iSections; }
		public BannerSectionInterface getSection(Long id) {
			if (iSections == null) return null;
			for (BannerSectionInterface section: iSections)
				if (section.getId().equals(id)) return section;
			return null;
		}
		
		public boolean isShowLimitOverrides() { return iShowLimitOverrides; }
		public void setShowLimitOverrides(boolean showLimitOverrides) { iShowLimitOverrides = showLimitOverrides; }
		public boolean isShowLabHours() { return iShowLabHours; }
		public void setShowLabHours(boolean showLabHours) { iShowLabHours = showLabHours; }
		public Float getLabHours() { return iLabHours; }
		public void setLabHours(Float labHours) { iLabHours = labHours; }
		public boolean hasOldSection(String sectionId) {
			if (iSections != null)
				for (BannerSectionInterface section: iSections)
					if (section.getOldSection() != null && section.getOldSection().equals(sectionId)) return true;
			return false;
		}
	}

}
