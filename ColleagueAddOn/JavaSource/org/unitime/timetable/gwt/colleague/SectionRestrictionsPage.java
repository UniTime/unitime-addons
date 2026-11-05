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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.admin.AcademicSessionsPage.IdLabel;
import org.unitime.timetable.gwt.client.aria.ImageButton;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableWidget;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.GwtMessages;
import org.unitime.timetable.gwt.resources.GwtResources;

import com.google.gwt.aria.client.Roles;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Cursor;
import com.google.gwt.dom.client.Style.Display;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.VerticalAlign;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasChangeHandlers;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.rpc.IsSerializable;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.Widget;

public class SectionRestrictionsPage extends Composite {
	private static final CourseMessages MSG = GWT.create(CourseMessages.class);
	private static final GwtMessages GMSG = GWT.create(GwtMessages.class);
	private static final ColleagueMessages CMSG = GWT.create(ColleagueMessages.class);
	private static final GwtResources RES = GWT.create(GwtResources.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	private SimpleForm iPanel;
	private UniTimeHeaderPanel iHeader, iFooter;
	private UniTimeTable<SectionInterface> iTable;
	private SectionRestrictionsInterface iSections;
	
	public SectionRestrictionsPage() {
		iPanel = new SimpleForm();
		iPanel.addStyleName("unitime-SectionRestrictions");
		iHeader = new UniTimeHeaderPanel();
		iHeader.addButton("update", CMSG.actionUpdateSectionRestrictionAssignment(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				update(SectionRestrictionsRequest.Operation.UPDATE);
			}
		});
		iHeader.getButton("update").setTitle(CMSG.titleUpdateSectionRestrictionsAssignment(CMSG.accessUpdateSectionRestrictionAssignment()));
		iHeader.getButton("update").setAccessKey(CMSG.accessUpdateSectionRestrictionAssignment().charAt(0));
		iHeader.setEnabled("update", false);
		iHeader.addButton("remove", CMSG.actionUnassignAllRestrictionsFromConfig(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(CMSG.confirmUnassignAllRestrictions(), new Command() {
					@Override
					public void execute() {
						update(SectionRestrictionsRequest.Operation.UNASSIGN_ALL);
					}
				});
				
			}
		});
		iHeader.getButton("remove").setTitle(CMSG.titleUnassignAllRestrictionsFromConfig());
		iHeader.setEnabled("remove", false);
		
		iHeader.addButton("back", MSG.actionBackToIODetail(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (iSections != null)
					ToolBox.open(GWT.getHostPageBaseURL() + "colleagueOffering?co=" + iSections.getCourseId());
				else
					ToolBox.open(GWT.getHostPageBaseURL() + "colleagueOffering?co=" + Window.Location.getParameter("co"));
			}
		});
		iHeader.getButton("back").setTitle(MSG.titleBackToIODetail(MSG.accessBackToIODetail()));
		iHeader.getButton("back").setAccessKey(MSG.accessBackToIODetail().charAt(0));
		
		iTable = new UniTimeTable<SectionInterface>();
		iTable.addStyleName("unitime-SectionRestrictionsTable");
		List<Widget> header = new ArrayList<Widget>();
		header.add(new UniTimeTableHeader(CMSG.columnSection()));
		header.add(new UniTimeTableHeader(CMSG.columnColleagueSynonym()));
		header.add(new UniTimeTableHeader(MSG.columnInstructorName()));
		header.add(new UniTimeTableHeader(MSG.columnAssignedTime()));
		header.add(new UniTimeTableHeader(MSG.columnAssignedRoom()));
		iTable.addRow(null, header);
		
		iFooter = iHeader.clonePanel();
		iPanel.addHeaderRow(iHeader);
		iPanel.addRow(iTable);
		iPanel.addBottomRow(iFooter);
		
		initWidget(iPanel);
		
		String co = Window.Location.getParameter("co");
		String uid = Window.Location.getParameter("uid");
		
		LoadingWidget.getInstance().show(GMSG.waitPlease());
		RPC.execute(new SectionRestrictionsRequest(SectionRestrictionsRequest.Operation.LOAD,
				Long.valueOf(uid), co == null ? null : Long.valueOf(co)), new AsyncCallback<SectionRestrictionsResponse>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(caught.getMessage());
						UniTimeNotifications.error(caught.getMessage(), caught);
						ToolBox.checkAccess(caught);
					}

					@Override
					public void onSuccess(SectionRestrictionsResponse result) {
						LoadingWidget.getInstance().hide();
						populate(result);
					}
				});
	}
	
	protected void populate(SectionRestrictionsResponse result) {
		if (result.hasSections()) {
			iTable.clearTable(1);
			iSections = result.getSections();
			iHeader.setHeaderTitle(iSections.getName());
			for (SectionInterface section: iSections.getSections()) {
				iTable.addRow(section, getLine(section, result));
			}
			iHeader.setEnabled("update", true);
			iHeader.setEnabled("remove", result.isCanUnassignAll());
		}
	}
	
	protected List<Widget> getLine(SectionInterface section, SectionRestrictionsResponse result) {
		List<Widget> line = new ArrayList<Widget>();
		Label name = new Label(section.getName());
		name.getElement().getStyle().setPaddingLeft(section.getIndent() * 20, Unit.PX);
		line.add(name);
		line.add(new Label(section.hasSynonym() ? section.getSynonym() : ""));
		line.add(new RestrictionsWidget(section, result));
		line.add(new TableWidget.CellWidget(section.getTime()));
		line.add(new TableWidget.CellWidget(section.getRoom()));
		return line;
	}
	
	protected void update(SectionRestrictionsRequest.Operation op) {
		LoadingWidget.getInstance().show(GMSG.waitPlease());
		RPC.execute(new SectionRestrictionsRequest(op, iSections), new AsyncCallback<SectionRestrictionsResponse>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iHeader.setErrorMessage(GMSG.failedToSaveData(caught.getMessage()));
				UniTimeNotifications.error(GMSG.failedToSaveData(caught.getMessage()), caught);
				ToolBox.checkAccess(caught);
			}

			@Override
			public void onSuccess(SectionRestrictionsResponse result) {
				LoadingWidget.getInstance().hide();
				if (result.hasSections()) {
					populate(result);
				} else {
					ToolBox.open(GWT.getHostPageBaseURL() + "colleagueOffering?co=" + result.getCourseId());
				}
			}
		});
	}
	
	public static class RestrictionsWidget extends P {
		SectionRestrictionsResponse iResponse;
		SectionInterface iSection;
		ChangeHandler iChangeHandler;
		
		public RestrictionsWidget(SectionInterface section, SectionRestrictionsResponse result) {
			iSection = section;
			iResponse = result;
			if (iSection.hasRestrictionIds() && iResponse.hasRestrictions()) {
				for (IdLabel id: iResponse.getRestrictions())
					if (iSection.hasRestrictionId(id.getId()) ) {
						RestrictionsLine line = new RestrictionsLine();
						line.setValue(id.getId());
						add(line);
					}
			} else {
				RestrictionsLine line = new RestrictionsLine();
				add(line);
			}
			fixButtons();
		}
		
		protected void updateSection() {
			Set<Long> ids = new HashSet<Long>();
			for (int i = 0; i < getWidgetCount(); i++) {
				RestrictionsLine p = ((RestrictionsLine)getWidget(i)); 
				Long id = p.getValue();
				if (id != null) ids.add(id);
			}
			iSection.setRestrictionIds(ids);
		}
		
		public class RestrictionsLine extends P implements HasChangeHandlers, TakesValue<Long> {
			private ListBox iList = new ListBox();
			private ImageButton iButton;
			boolean iButtonAdd = false;
			
			public RestrictionsLine() {
				super("restrictions-line");
				getElement().getStyle().setPaddingTop(1, Unit.PX);
				iList.addItem("-", "");
				if (iResponse.hasRestrictions())
					for (IdLabel id: iResponse.getRestrictions())
						iList.addItem(id.getLabel(), id.getId().toString());
				add(iList);
				Roles.getListboxRole().setAriaLabelProperty(iList.getElement(), MSG.columnInstructorName());
				iList.addChangeHandler(new ChangeHandler() {
					@Override
					public void onChange(ChangeEvent event) {
						updateSection();
					}
				});
				
				iButton = new ImageButton(RES.delete());
				iButton.setTitle(GMSG.titleDeleteRow());
				iButton.setAltText(GMSG.titleDeleteRow());
				iButton.addStyleName("restrictions-button");
				iButton.getElement().getStyle().setCursor(Cursor.POINTER);
				iButton.getElement().getStyle().setMarginTop(1, Unit.PX);
				iButton.getElement().getStyle().setMarginLeft(5, Unit.PX);
				iButton.getElement().getStyle().setVerticalAlign(VerticalAlign.TEXT_TOP);
				iButton.getElement().getStyle().setDisplay(Display.INLINE_BLOCK);
				iButton.addClickHandler(new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						if (iButtonAdd) {
							RestrictionsWidget.this.add(new RestrictionsLine());
						} else {
							RestrictionsWidget.this.remove(RestrictionsLine.this);
						}
						RestrictionsWidget.this.fixButtons();
					}
				});
				add(iButton);
			}
			
			public void setButtonAdd(boolean add) {
				iButtonAdd = add;
				if (add) {
					iButton.setImage(RES.add());
					iButton.setTitle(GMSG.titleAddRow());
					iButton.setAltText(GMSG.titleAddRow());
				} else {
					iButton.setImage(RES.delete());
					iButton.setTitle(GMSG.titleDeleteRow());
					iButton.setAltText(GMSG.titleDeleteRow());
				}
			}

			@Override
			public void setValue(Long value) {
				if (value == null)
					iList.setSelectedIndex(0);
				else
					for (int i = 1; i < iList.getItemCount(); i++)
						if (value.equals(Long.valueOf(iList.getValue(i)))) {
							iList.setSelectedIndex(i);
							break;
						}
			}

			@Override
			public Long getValue() {
				String val = iList.getSelectedValue();
				return val == null || val.isEmpty() ? null : Long.valueOf(val);
			}
			
			@Override
			public HandlerRegistration addChangeHandler(ChangeHandler handler) {
				return addDomHandler(handler, ChangeEvent.getType());
			}
		}
		
		protected void fixButtons() {
			if (getWidgetCount() == 1)
				((RestrictionsLine)getWidget(0)).setButtonAdd(true);
			else {
				for (int i = 0; i < getWidgetCount(); i++) {
					RestrictionsLine p = ((RestrictionsLine)getWidget(i)); 
					if (i < getWidgetCount() - 1) {
						p.setButtonAdd(false);
					} else {
						p.setButtonAdd(true);
					}
				}
			}
		}
		
	}
	
	public static class SectionInterface implements IsSerializable {
		private Long iId;
		private String iName;
		private String iSynonym;
		private Set<Long> iRestrictionIds;
		private CellInterface iTime;
		private CellInterface iRoom;
		private int iIndent = 0;
		private boolean iCanEdit = true;
		
		public Long getId() { return iId; }
		public void setId(Long id) { iId = id; }
		public boolean isCanEdit() { return iCanEdit; }
		public void setCanEdit(boolean canEdit) { iCanEdit = canEdit; }
		public int getIndent() { return iIndent; }
		public void setIndent(int indent) { iIndent = indent; }
		
		public boolean hasRestrictionIds() { return iRestrictionIds != null && !iRestrictionIds.isEmpty(); }
		public void addRestrictionId(Long id) {
			if (iRestrictionIds == null) iRestrictionIds = new HashSet<Long>();
			iRestrictionIds.add(id);
		}
		public void removeRestrictionId(Long id) {
			if (iRestrictionIds != null && id != null) iRestrictionIds.remove(id);
		}
		public Set<Long> getRestrictionIds() { return iRestrictionIds; }
		public boolean hasRestrictionId(Long id) {
			if (iRestrictionIds == null) return false;
			return iRestrictionIds.contains(id);
		}
		public void setRestrictionIds(Set<Long> restrictioIds) { iRestrictionIds = restrictioIds; }

		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		public boolean hasName() { return iName != null && !iName.isEmpty(); }
		public String getSynonym() { return iSynonym; }
		public void setSynonym(String Synonym) { iSynonym = Synonym; }
		public boolean hasSynonym() { return iSynonym != null && !iSynonym.isEmpty(); }
		public CellInterface getTime() { return iTime; }
		public void setTime(CellInterface Time) { iTime = Time; }
		public boolean hasTime() { return iTime != null; }
		public CellInterface getRoom() { return iRoom; }
		public void setRoom(CellInterface Room) { iRoom = Room; }
		public boolean hasRoom() { return iRoom != null; }
	}
	
	public static class SectionRestrictionsInterface implements GwtRpcResponse {
		private Long iConfigId;
		private Long iCourseId;
		private String iName;
		private List<SectionInterface> iSections;
		
		public Long getConfigId() { return iConfigId; }
		public void setConfigId(Long configId) { iConfigId = configId; }
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public String getName() { return iName; }
		public void setName(String name) { iName = name; }
		
		public void addSection(SectionInterface section) {
			if (iSections == null) iSections = new ArrayList<SectionInterface>();
			iSections.add(section);
		}
		public List<SectionInterface> getSections() { return iSections; }
		public boolean hasSections() { return iSections != null && !iSections.isEmpty(); }
		public SectionInterface getSection(Long id) {
			if (iSections == null || id == null) return null;
			for (SectionInterface item: iSections)
				if (id.equals(item.getId())) return item;
			return null;
		}
	}
	
	public static class SectionRestrictionsRequest implements GwtRpcRequest<SectionRestrictionsResponse> {
		private Long iConfigId;
		private Long iCourseId;
		private SectionRestrictionsInterface iRestrictions;
		private Operation iOperation;
		
		public SectionRestrictionsRequest() {}
		public SectionRestrictionsRequest(Operation operation, Long configId, Long courseId) {
			iOperation = operation;
			iConfigId = configId;
			iCourseId = courseId;
		}
		public SectionRestrictionsRequest(Operation operation, SectionRestrictionsInterface restrictions) {
			iOperation = operation;
			iRestrictions = restrictions;
			iConfigId = (restrictions == null ? null : restrictions.getConfigId());
			iCourseId = (restrictions == null ? null : restrictions.getCourseId());
		}

		public Long getConfigId() { return iConfigId; }
		public void setConfigId(Long configId) { iConfigId = configId; }
		public Long getCourseId() { return iCourseId; }
		public void setCourseId(Long courseId) { iCourseId = courseId; }
		public SectionRestrictionsInterface getRestrictions() { return iRestrictions; }
		public void setRestrictions(SectionRestrictionsInterface restrictions) { iRestrictions = restrictions; }
		public Operation getOperation() { return iOperation; }
		public void setOperation(Operation operation) { iOperation = operation; }

		public static enum Operation {
			LOAD, UPDATE, UNASSIGN_ALL,
		}
	}
	
	public static class SectionRestrictionsResponse implements GwtRpcResponse {
		private SectionRestrictionsInterface iSections;
		private List<IdLabel> iRestrictions;
		
		public SectionRestrictionsInterface getSections() { return iSections; }
		public void setSections(SectionRestrictionsInterface sections) { iSections = sections; }
		public boolean hasSections() { return iSections != null && iSections.hasSections(); }
		public Long getConfigId() { return iSections == null ? null : iSections.getConfigId(); }
		public Long getCourseId() { return iSections == null ? null : iSections.getCourseId(); }
		
		public void addRestriction(Long id, String label) {
			if (iRestrictions == null) iRestrictions = new ArrayList<IdLabel>();
			iRestrictions.add(new IdLabel(id, label));
		}
		public List<IdLabel> getRestrictions() { return iRestrictions; }
		public boolean hasRestrictions() { return iRestrictions != null && !iRestrictions.isEmpty(); }
		public IdLabel getRestriction(Long id) {
			if (iRestrictions == null || id == null) return null;
			for (IdLabel item: iRestrictions)
				if (id.equals(item.getId())) return item;
			return null;
		}
		
		public boolean isCanUnassignAll() {
			if (!hasSections()) return false;
			for (SectionInterface section: getSections().getSections()) {
				if (!section.isCanEdit()) return false;
			}
			return true;
		}
	}
}
