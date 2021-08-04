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
package org.unitime.banner.export.bannerresponses;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.banner.server.bannerresponses.BannerResponsesPageBackend;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.export.CSVPrinter;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;
import org.unitime.timetable.gwt.shared.BannerResponseInterface;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesPageRequest;
import org.unitime.timetable.model.SubjectArea;

@Service("org.unitime.timetable.export.Exporter:banner-responses.csv")
public class BannerResponsesExportCSV implements Exporter {
	protected static final  BannerGwtMessages MESSAGES = Localization.create(BannerGwtMessages.class);

	@Override
	public String reference() {
		return "banner-responses.csv";
	}

	protected String getCell(final BannerResponseInterface bannerResponse, final Column column, final int idx) {
		switch (column.getColumn()) {
		case UNIQUEID:
			return bannerResponse.getUniqueId().toString();
		case ACTIVITY_DATE:
			return bannerResponse.getActivityDateStr();
		case CAMPUS:
			return bannerResponse.getCampus();
		case SUBJECT_CODE:
			return bannerResponse.getSubjectCode();
		case COURSE_NUMBER:
			return bannerResponse.getCourseNumber();
		case SECTION_NUMBER:
			return bannerResponse.getSectionNumber();
		case CRN:
			return bannerResponse.getCrn();
		case XLST_GROUP:
			return bannerResponse.getXlstGroup();
		case ACTION:
			return bannerResponse.getAction();
		case TYPE:
			return bannerResponse.getType();
		case MESSAGE:
			return bannerResponse.getMessage();
		default:
			return null;
		}
	}

	public String getColumnName(Column column, int idx) {
		switch (column.getColumn()) {
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
		default: return column.getColumn().name();
		}
	}
//	@Override
//	public void export(ExportHelper helper) throws IOException {
//		// TODO Auto-generated method stub
//
//	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		BannerResponsesPageRequest request = new BannerResponsesPageRequest();
		for (Enumeration<String> e = helper.getParameterNames(); e.hasMoreElements(); ) {
    		String command = e.nextElement();
    		if (command.equals("r:text")) {
    			request.getFilter().setText(helper.getParameter("r:text"));
    		} else if (command.startsWith("r:")) {
    			for (String value: helper.getParameterValues(command))
    				request.getFilter().addOption(command.substring(2), value);
    		}
		}
		if (helper.getParameter("subjectId") != null) {
			request.getFilter().addOption("subjectId", helper.getParameter("subjectId"));
		} else if (helper.getParameter("subject") != null) {
			Long sessionId = helper.getAcademicSessionId();
			if (sessionId == null)
				throw new IllegalArgumentException("Academic session not provided, please set the term parameter.");
			SubjectArea subject = SubjectArea.findByAbbv(sessionId, helper.getParameter("subject"));
			if (subject == null)
				throw new IllegalArgumentException("Subject area " + helper.getParameter("subject") + " does not exist.");
			request.getFilter().addOption("subjectId", subject.getUniqueId().toString());
		}
		
		List<BannerResponseInterface> list = new ArrayList<BannerResponseInterface>();
		list.addAll(new BannerResponsesPageBackend().execute(request, helper.getSessionContext()));
		export(request, list, helper);
	}
	
	protected void export(BannerResponsesPageRequest request, List<BannerResponseInterface> list, ExportHelper helper) throws IOException {
		Printer out = new CSVPrinter(helper, false);
		helper.setup(out.getContentType(), reference(), false);
		
		List<Column> columns = new ArrayList<Column>();
		for (BannerResponseInterface.BannerResponsesColumn column: BannerResponseInterface.BannerResponsesColumn.values()) {
			Column c = new Column(column, 0);
			columns.add(c);
		}
		
		String[] header = new String[columns.size()];
		for (int i = 0; i < columns.size(); i++)
			header[i] = getColumnName(columns.get(i), 0).replace("<br>", "\n");
		out.printHeader(header);
		out.flush();

		for (BannerResponseInterface a: list) {
			String[] row = new String[columns.size()];
			for (int i = 0; i < columns.size(); i++)
				row[i] = getCell(a, columns.get(i), 0);
			out.printLine(row);
			out.flush();
		}
		
		out.flush(); out.close();
	}

	protected static class Column {
		private BannerResponseInterface.BannerResponsesColumn iColumn;
		private int iIndex;
		
		Column(BannerResponseInterface.BannerResponsesColumn column, int index) { iColumn = column; iIndex = index; }
		
		public int getIndex() { return iIndex; }
		public BannerResponseInterface.BannerResponsesColumn getColumn() { return iColumn; }
	}

}
