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
import java.util.List;

import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.resources.BannerGwtConstants;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;
import org.unitime.timetable.gwt.resources.BannerGwtResources;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.gwt.shared.BannerResponseInterface;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class BannerResponsesTable extends UniTimeTable<BannerResponseInterface> {
	protected static final  BannerGwtMessages MESSAGES = GWT.create(BannerGwtMessages.class);
	public static final BannerGwtResources RESOURCES =  GWT.create(BannerGwtResources.class);
	protected static final GwtConstants CONSTANTS = GWT.create(BannerGwtConstants.class);
	
	public BannerResponsesTable() {
		setStyleName("unitime-Instructorss");
		
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (BannerResponsesColumn column: BannerResponsesColumn.values()) {
			UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(column, 0), getColumnAlignment(column, 0));
			header.add(h);
		}
		addRow(null, header);

	}

	protected HorizontalAlignmentConstant getColumnAlignment(BannerResponsesColumn column, int idx) {
		switch (column) {
		default:
			return HasHorizontalAlignment.ALIGN_LEFT;
		}
	}


	public void populate(GwtRpcResponseList<BannerResponseInterface> results) {
		clearTable(1);
		
		if (results.isEmpty()) {
			setEmptyMessage(MESSAGES.errorNoMatchingBannerResponsesFound());
			return;
		}
		
		List<Long> ids = new ArrayList<Long>();
		for (BannerResponseInterface bannerResponse: results) {
			addBannerResponse(bannerResponse);
		}
		if (!ids.isEmpty()) {
			setWidget(1, 7, new Image(RESOURCES.loading_small()));
			getFlexCellFormatter().setHorizontalAlignment(1, 7, HasHorizontalAlignment.ALIGN_LEFT);
		}
		
		for (int r = 0; r < getRowCount(); r++) {
			getCellFormatter().setVisible(r, 0, false);
		}
		
	}
	
	public int addBannerResponse(final BannerResponseInterface attribute) {
		List<Widget> widgets = new ArrayList<Widget>();
		
		for (BannerResponsesColumn column: BannerResponsesColumn.values()) {
			Widget cell = getCell(attribute, column, 0);
			if (cell == null)
				cell = new P();
			widgets.add(cell);
		}
		
		int row = addRow(attribute, widgets);
		getRowFormatter().setStyleName(row, "row");
		for (int col = 0; col < getCellCount(row); col++)
			getCellFormatter().setStyleName(row, col, "cell");
		
		return row;
	}

	
	protected Widget getCell(final BannerResponseInterface bannerResponse, final BannerResponsesColumn column, final int idx) {
		switch (column) {
		case UNIQUEID:
			return new Label(bannerResponse.getUniqueId().toString());
		case ACTIVITY_DATE:
			return new Label(bannerResponse.getActivityDateStr());
		case CAMPUS:
			return new Label(bannerResponse.getCampus());
		case SUBJECT_CODE:
			return new Label(bannerResponse.getSubjectCode());
		case COURSE_NUMBER:
			return new Label(bannerResponse.getCourseNumber());
		case SECTION_NUMBER:
			return new Label(bannerResponse.getSectionNumber());
		case CRN:
			return new Label(bannerResponse.getCrn());
		case XLST_GROUP:
			return new Label(bannerResponse.getXlstGroup());
		case ACTION:
			return new Label(bannerResponse.getAction());
		case TYPE:
			return new Label(bannerResponse.getType());
		case MESSAGE:
			return new Label(bannerResponse.getMessage());
		default:
			return null;
		}
	}

	public String getColumnName(BannerResponsesColumn column, int idx) {
		switch (column) {
		case ACTIVITY_DATE: return MESSAGES.colActivityDate();
		case CAMPUS: return MESSAGES.colCampus();
		case SUBJECT_CODE: return MESSAGES.colSubject();
		case COURSE_NUMBER: return MESSAGES.colCourse();
		case SECTION_NUMBER: return MESSAGES.colBannerSectionNumber();
		case CRN: return MESSAGES.colCrn();
		case XLST_GROUP: return MESSAGES.colCrosslistGroup();
		case ACTION: return MESSAGES.colAction();
		case TYPE: return MESSAGES.colType();
		case MESSAGE: return MESSAGES.colMessage();
		default: return column.name();
		}
	}

}
