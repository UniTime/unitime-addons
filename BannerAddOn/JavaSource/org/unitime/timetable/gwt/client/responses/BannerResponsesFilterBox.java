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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.unitime.timetable.gwt.client.events.SingleDateSelector;
import org.unitime.timetable.gwt.client.events.UniTimeFilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Chip;
import org.unitime.timetable.gwt.client.widgets.FilterBox.Suggestion;
import org.unitime.timetable.gwt.resources.BannerGwtConstants;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;
import org.unitime.timetable.gwt.shared.BannerResponseInterface;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.logical.shared.ValueChangeEvent;
import com.google.gwt.event.logical.shared.ValueChangeHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.AbsolutePanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;

public class BannerResponsesFilterBox extends UniTimeFilterBox<BannerResponseInterface.BannerResponsesFilterRpcRequest> {
	private static final BannerGwtMessages MESSAGES = GWT.create(BannerGwtMessages.class);
	private static final BannerGwtConstants CONSTANTS = GWT.create(BannerGwtConstants.class);
	private static DateTimeFormat sDateFormat = DateTimeFormat.getFormat(CONSTANTS.filterDateFormat());
	private static DateTimeFormat sLocalDateFormat = DateTimeFormat.getFormat(CONSTANTS.eventDateFormat());
	private ListBox iDepartments;
	private ListBox iManagers;
	private FilterBox.CustomFilter iOther = null;
	private TextBox iCrsNbrBox = new TextBox();
	private TextBox iCrnBox = new TextBox();
	private TextBox iXlstBox = new TextBox();
	private TextBox iMessageBox = new TextBox();
	private TextBox iLimitBox = new TextBox();



//	private FilterBox.CustomFilter iOther = null;
	
	public void manageListBoxChips(String chipName, ListBox listBox, String label) {
		Chip oldChip = getChip(chipName);
		Chip newChip = (listBox.getSelectedIndex() <= 0 ? null : new Chip(chipName, listBox.getValue(listBox.getSelectedIndex())).withTranslatedCommand(label));
		if (oldChip != null) {
			if (newChip == null) {
				removeChip(oldChip, true);
			} else {
				if (!oldChip.getValue().equals(newChip.getValue())) {
					removeChip(oldChip, false);
					addChip(newChip, true);
				}
			}
		} else {
			if (newChip != null)
				addChip(newChip, true);
		}
	}

	public BannerResponsesFilterBox() {
		super(null);
	
		setShowSuggestionsOnFocus(false);


		addFilter(new FilterBox.StaticSimpleFilter("action", MESSAGES.tagAction()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("update".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.bannerMessageActionType()[0];
				else if ("delete".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.bannerMessageActionType()[1];
				else if ("audit".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.bannerMessageActionType()[2];
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		addFilter(new FilterBox.StaticSimpleFilter("rspType", MESSAGES.tagResponseType()) {
			@Override
			public void validate(String text, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				if ("success".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.bannerMessageResponseType()[0];
				else if ("warning".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.bannerMessageResponseType()[1];
				else if ("error".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.bannerMessageResponseType()[2];
				else if ("no change".equalsIgnoreCase(text))
					translatedValue = CONSTANTS.bannerMessageResponseType()[3];
				callback.onSuccess(new Chip(getCommand(), text).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});

		iDepartments = new ListBox();
		iDepartments.setMultipleSelect(false);
		iDepartments.setWidth("100%");

		addFilter(new FilterBox.CustomFilter("department", MESSAGES.tagDepartment(), iDepartments) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					Chip oldChip = getChip("department");
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					for (int i = 0; i < iDepartments.getItemCount(); i++) {
						Chip chip = new Chip("department", iDepartments.getValue(i)).withTranslatedCommand(MESSAGES.tagDepartment());
						String name = iDepartments.getItemText(i);
						if (iDepartments.getValue(i).toLowerCase().startsWith(text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						} else if (text.length() > 2 && (name.toLowerCase().contains(" " + text.toLowerCase()) || name.toLowerCase().contains(" (" + text.toLowerCase()))) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						}
					}
					callback.onSuccess(suggestions);
				}
			}
		});
		iDepartments.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				manageListBoxChips("department", iDepartments, MESSAGES.tagDepartment());
			}
		});
		
		
		addFilter(new FilterBox.StaticSimpleFilter("subj", MESSAGES.tagSubjectArea()));

		addFilter(new FilterBox.StaticSimpleFilter("crn", MESSAGES.tagCrn()));
		addFilter(new FilterBox.StaticSimpleFilter("crsnbr", MESSAGES.tagCrn()));
		addFilter(new FilterBox.StaticSimpleFilter("xlst", MESSAGES.tagXlst()));
		addFilter(new FilterBox.StaticSimpleFilter("msg", MESSAGES.tagMessage()));
		addFilter(new FilterBox.StaticSimpleFilter("limit", MESSAGES.tagMaxResults()));
		

		iCrsNbrBox.setStyleName("unitime-TextArea");
		iCrsNbrBox.setMaxLength(5); iCrsNbrBox.setWidth("60px");

		Label crnLab = new Label(MESSAGES.propCrn());
		crnLab.getElement().getStyle().setMarginLeft(10, Unit.PX);
		
		iCrnBox.setStyleName("unitime-TextArea");
		iCrnBox.setMaxLength(5); iCrnBox.setWidth("60px");
		
		Label xlstLab = new Label(MESSAGES.propXlst());
		xlstLab.getElement().getStyle().setMarginLeft(10, Unit.PX);

		iXlstBox.setStyleName("unitime-TextArea");
		iXlstBox.setMaxLength(2); iXlstBox.setWidth("30px");

		iMessageBox.setStyleName("unitime-TextArea");
		iMessageBox.setMaxLength(200); iMessageBox.setWidth("400px");

		Label limitLab = new Label(MESSAGES.propMaxResults());
		limitLab.getElement().getStyle().setMarginLeft(10, Unit.PX);

		iLimitBox.setStyleName("unitime-TextArea");
		iLimitBox.setMaxLength(4); iLimitBox.setWidth("40px");


		iOther = new FilterBox.CustomFilter("other",MESSAGES.tagOther(), 
				new Label(MESSAGES.propCourseNumber()), iCrsNbrBox,
				crnLab, iCrnBox,
				xlstLab, iXlstBox,
				limitLab, iLimitBox,
				new HTML("<br><br>"),
				new Label(MESSAGES.propMessage()), iMessageBox
				
				) {
				private void addSuggestionForNumberIfNeeded(List<FilterBox.Suggestion> suggestions, final List<Chip> chips, String text, String tag, String translatedCommand, int minInt, int maxInt) {
					Chip old = null;
					for (Chip c: chips) { if (c.getCommand().equals(tag)) { old = c; break; } }
					boolean foundNum = false;
					try {
						if (Integer.parseInt(text) >= minInt && Integer.parseInt(text) <= maxInt) {
							suggestions.add(new Suggestion(new Chip(tag, text).withTranslatedCommand(translatedCommand), old));	
							foundNum = true;
						}
					} catch (NumberFormatException e) {}
					if (!foundNum && text.indexOf(tag +":") >= 0 || text.indexOf(translatedCommand + ":") >= 0) {
						int numIndex = text.indexOf(":") + 1;
						if ( text.length() > numIndex) {
							String numString = text.substring(numIndex);
							try {
								if (Integer.parseInt(numString) >= minInt && Integer.parseInt(numString) <= maxInt) {
								suggestions.add(new Suggestion(new Chip(tag, numString).withTranslatedCommand(translatedCommand), old));
								}
							} catch (NumberFormatException e) {}
						}
					}
				}
				@Override
				public void getSuggestions(final List<Chip> chips, final String text, AsyncCallback<Collection<FilterBox.Suggestion>> callback) {
					System.out.println("Filter text:  " + text);
					if (text.isEmpty()) {
						callback.onSuccess(null);
					} else {
						List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
						addSuggestionForNumberIfNeeded(suggestions, chips, text, "limit", MESSAGES.tagMaxResults(), 1, 9999);
						addSuggestionForNumberIfNeeded(suggestions, chips, text, "crn", MESSAGES.tagCrn(), 10000, 99999);
						addSuggestionForNumberIfNeeded(suggestions, chips, text, "crsnbr", MESSAGES.tagCourseNumber(), 10000, 99999);
						
						Chip old = null;
						for (Chip c: chips) { if (c.getCommand().equals("xlst")) { old = c; break; } }
						if (text.length() == 2 ) {
							suggestions.add(new Suggestion(new Chip("xlst", text).withTranslatedCommand(MESSAGES.tagXlst()), old));								
						}
						for (Chip c: chips) { if (c.getCommand().equals("msg")) { old = c; break; } }
						if (text.length() > 2 ) {
							suggestions.add(new Suggestion(new Chip("msg", text).withTranslatedCommand(MESSAGES.tagMessage()), old));								
						}
						
						callback.onSuccess(suggestions);
					}
				}
		};
		addFilter(iOther);
		
		iCrnBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean removed = removeChip(new Chip("crn", null), false);
				if (iCrnBox.getText().isEmpty()) {
					if (removed)
						fireValueChangeEvent();
				} else {
					addChip(new Chip("crn", iCrnBox.getText()).withTranslatedCommand(MESSAGES.tagCrn()), true);
				}
			}
		});		
				
		iCrsNbrBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean removed = removeChip(new Chip("crsnbr", null), false);
				if (iCrsNbrBox.getText().isEmpty()) {
					if (removed)
						fireValueChangeEvent();
				} else {
					addChip(new Chip("crsnbr", iCrsNbrBox.getText()).withTranslatedCommand(MESSAGES.tagCourseNumber()), true);
				}
			}
		});

		iXlstBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean removed = removeChip(new Chip("xlst", null), false);
				if (iXlstBox.getText().isEmpty()) {
					if (removed)
						fireValueChangeEvent();
				} else {
					addChip(new Chip("xlst", iXlstBox.getText()).withTranslatedCommand(MESSAGES.tagXlst()), true);
				}
			}
		});		
				
		iMessageBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean removed = removeChip(new Chip("msg", null), false);
				if (iMessageBox.getText().isEmpty()) {
					if (removed)
						fireValueChangeEvent();
				} else {
					addChip(new Chip("msg", iMessageBox.getText()).withTranslatedCommand(MESSAGES.tagMessage()), true);
				}
			}
		});

		iLimitBox.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				boolean removed = removeChip(new Chip("limit", null), false);
				if (iLimitBox.getText().isEmpty()) {
					if (removed)
						fireValueChangeEvent();
				} else {
					addChip(new Chip("limit", iLimitBox.getText()).withTranslatedCommand(MESSAGES.tagMaxResults()), true);
				}
			}
		});

		iManagers = new ListBox();
		iManagers.setMultipleSelect(false);
		iManagers.setWidth("100%");

		addFilter(new FilterBox.CustomFilter("manager", MESSAGES.tagManager(), iManagers) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				if (text.isEmpty()) {
					callback.onSuccess(null);
				} else {
					Chip oldChip = getChip("manager");
					List<Suggestion> suggestions = new ArrayList<Suggestion>();
					for (int i = 0; i < iManagers.getItemCount(); i++) {
						Chip chip = new Chip("manager", iManagers.getValue(i)).withTranslatedCommand(MESSAGES.tagManager());
						String name = iManagers.getItemText(i);
						if (iManagers.getValue(i).toLowerCase().startsWith(text.toLowerCase())) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						} else if (text.length() > 2 && (name.toLowerCase().contains(" " + text.toLowerCase()) || name.toLowerCase().contains(" (" + text.toLowerCase()))) {
							suggestions.add(new Suggestion(name, chip, oldChip));
						}
					}
					callback.onSuccess(suggestions);
				}
			}
		});
		iManagers.addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				manageListBoxChips("manager", iManagers, MESSAGES.tagManager());
			}
		});



				
		AbsolutePanel m = new AbsolutePanel();
		m.setStyleName("unitime-DateSelector");
		final SingleDateSelector.SingleMonth m1 = new SingleDateSelector.SingleMonth(MESSAGES.tagDateFrom());
		m1.setAllowDeselect(true);
		m.add(m1);
		final SingleDateSelector.SingleMonth m2 = new SingleDateSelector.SingleMonth(MESSAGES.tagDateTo());
		m2.setAllowDeselect(true);
		m.add(m2);
		addFilter(new FilterBox.CustomFilter("date", MESSAGES.tagDate(), m) {
			@Override
			public void getSuggestions(List<Chip> chips, String text, AsyncCallback<Collection<Suggestion>> callback) {
				List<FilterBox.Suggestion> suggestions = new ArrayList<FilterBox.Suggestion>();
				Chip chFrom = null, chTo = null;
				for (Chip c: chips) {
					if (c.getCommand().equals("from")) chFrom = c;
					if (c.getCommand().equals("to")) chTo = c;
				}
				try {
					Date date = DateTimeFormat.getFormat("MM/dd").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("dd.MM").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("MM/dd/yy").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("dd.MM.yy").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("MMM dd").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				try {
					Date date = DateTimeFormat.getFormat("MMM dd yy").parse(text);
					suggestions.add(new FilterBox.Suggestion(new Chip("from", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(date)), chFrom));
					suggestions.add(new FilterBox.Suggestion(new Chip("to", sDateFormat.format(date)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(date)), chTo));
				} catch (Exception e) {
				}
				callback.onSuccess(suggestions);
			}
		});
		addFilter(new FilterBox.StaticSimpleFilter("from", MESSAGES.tagDateFrom()) {
			@Override
			public void validate(String value, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				try {
					translatedValue = sLocalDateFormat.format(sDateFormat.parse(value));
				} catch (IllegalArgumentException e) {}
				callback.onSuccess(new Chip(getCommand(), value).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		addFilter(new FilterBox.StaticSimpleFilter("to", MESSAGES.tagDateTo()) {
			@Override
			public void validate(String value, AsyncCallback<Chip> callback) {
				String translatedValue = null;
				try {
					translatedValue = sLocalDateFormat.format(sDateFormat.parse(value));
				} catch (IllegalArgumentException e) {}
				callback.onSuccess(new Chip(getCommand(), value).withTranslatedCommand(getLabel()).withTranslatedValue(translatedValue));
			}
		});
		m1.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				Chip ch = getChip("from");
				Date value = event.getValue();
				if (value == null) {
					if (ch != null) removeChip(ch, true);	
				} else {
					if (ch != null) {
						if (ch.getValue().equals(sDateFormat.format(value))) return;
						removeChip(ch, false);
					}
					addChip(new Chip("from", sDateFormat.format(value)).withTranslatedCommand(MESSAGES.tagDateFrom()).withTranslatedValue(sLocalDateFormat.format(value)), true);
				}
			}
		});
		
		m2.addValueChangeHandler(new ValueChangeHandler<Date>() {
			@Override
			public void onValueChange(ValueChangeEvent<Date> event) {
				Chip ch = getChip("to");
				Date value = event.getValue();
				if (value == null) {
					if (ch != null) removeChip(ch, true);	
				} else {
					if (ch != null) {
						if (ch.getValue().equals(sDateFormat.format(value))) return;
						removeChip(ch, false);
					}
					addChip(new Chip("to", sDateFormat.format(value)).withTranslatedCommand(MESSAGES.tagDateTo()).withTranslatedValue(sLocalDateFormat.format(value)), true);
				}
			}
		});


		addValueChangeHandler(new ValueChangeHandler<String>() {
			@Override
			public void onValueChange(ValueChangeEvent<String> event) {
				if (!isFilterPopupShowing()) {
					iDepartments.setSelectedIndex(0);
					for (int i = 1; i < iDepartments.getItemCount(); i++) {
						String value = iDepartments.getValue(i);
						if (hasChip(new Chip("department", value))) {
							iDepartments.setSelectedIndex(i);
							break;
						}
					}
					iManagers.setSelectedIndex(0);
					for (int i = 1; i < iManagers.getItemCount(); i++) {
						String value = iManagers.getValue(i);
						if (hasChip(new Chip("manager", value))) {
							iManagers.setSelectedIndex(i);
							break;
						}
					}					
				}
				init(false, getAcademicSessionId(), new Command() {
					@Override
					public void execute() {
						if (isFilterPopupShowing())
							showFilterPopup();
					}
				});
			}
		});

	}

	@Override
	protected boolean populateFilter(FilterBox.Filter filter, List<FilterRpcResponse.Entity> entities) {
		if ("department".equals(filter.getCommand())) {
			iDepartments.clear();
			iDepartments.addItem(MESSAGES.itemAllDepartments(), "");
			if (entities != null)
				for (FilterRpcResponse.Entity entity: entities)
					iDepartments.addItem(entity.getName() + " (" + entity.getCount() + ")", entity.getAbbreviation());
			
			iDepartments.setSelectedIndex(0);
			Chip dept = getChip("department");
			if (dept != null)
				for (int i = 1; i < iDepartments.getItemCount(); i++)
					if (dept.getValue().equals(iDepartments.getValue(i))) {
						iDepartments.setSelectedIndex(i);
						break;
					}
			return true;			
		} else if ("manager".equals(filter.getCommand())) {
				iManagers.clear();
				iManagers.addItem(MESSAGES.itemAllManagers(), "");
				if (entities != null)
					for (FilterRpcResponse.Entity entity: entities)
						iManagers.addItem(entity.getName() + " (" + entity.getCount() + ")", entity.getAbbreviation());
				
				iManagers.setSelectedIndex(0);
				Chip mgr = getChip("manager");
				if (mgr != null)
					for (int i = 1; i < iManagers.getItemCount(); i++)
						if (mgr.getValue().equals(iManagers.getValue(i))) {
							iManagers.setSelectedIndex(i);
							break;
						}
				return true;
		} else if ("crsnbr".equals(filter.getCommand())) {
			iCrsNbrBox.setText("");
			
			Chip crsNbr = getChip("crsnbr");
			if (crsNbr != null) {
				iCrsNbrBox.setText(crsNbr.getValue());
			}
			return true;
		} else if ("crn".equals(filter.getCommand())) {
			iCrnBox.setText("");
			
			Chip crn = getChip("crn");
			if (crn != null) {
				iCrnBox.setText(crn.getValue());
			}
			return true;
		} else if ("xlst".equals(filter.getCommand())) {
			iXlstBox.setText("");
			
			Chip xlst = getChip("xlst");
			if (xlst != null) {
				iXlstBox.setText(xlst.getValue());
			}
			return true;
		} else if ("msg".equals(filter.getCommand())) {
			iMessageBox.setText("");
			
			Chip msg = getChip("msg");
			if (msg != null) {
				iMessageBox.setText(msg.getValue());
			}
			return true;
		} else if ("limit".equals(filter.getCommand())) {
			iLimitBox.setText("");
			
			Chip msg = getChip("limit");
			if (msg != null) {
				iLimitBox.setText(msg.getValue());
			}
			return true;
		} else if (filter != null && filter instanceof FilterBox.StaticSimpleFilter) {
			FilterBox.StaticSimpleFilter simple = (FilterBox.StaticSimpleFilter)filter;
			List<FilterBox.Chip> chips = new ArrayList<FilterBox.Chip>();
			if (entities != null) {
				for (FilterRpcResponse.Entity entity: entities)
					chips.add(new FilterBox.Chip(filter.getCommand(), entity.getAbbreviation())
							.withLabel(entity.getName())
							.withCount(entity.getCount())
							.withTranslatedCommand(filter.getLabel())
							.withTranslatedValue(entity.getProperty("translated-value", null)));
			}
			simple.setValues(chips);
			return true;
			//TODO: do this for other filters
		} else {
			return false;
		}
	}

	@Override
	protected BannerResponsesFilterRpcRequest createRpcRequest() {
		return new BannerResponseInterface.BannerResponsesFilterRpcRequest();
	}


}
