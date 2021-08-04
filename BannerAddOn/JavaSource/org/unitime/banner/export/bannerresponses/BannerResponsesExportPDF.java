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
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.Exporter;
import org.unitime.timetable.export.PDFPrinter;
import org.unitime.timetable.export.PDFPrinter.A;
import org.unitime.timetable.export.PDFPrinter.F;
import org.unitime.timetable.gwt.shared.BannerResponseInterface;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesColumn;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesPageRequest;

@Service("org.unitime.timetable.export.Exporter:banner-responses.pdf")
public class BannerResponsesExportPDF extends BannerResponsesExportCSV implements Exporter {

	@Override
	public String reference() {
		return "banner-responses.pdf";
	}

	protected void export(BannerResponsesPageRequest request, List<BannerResponseInterface> list, ExportHelper helper) throws IOException {
		PDFPrinter out = new PDFPrinter(helper.getOutputStream(), false);
		helper.setup(out.getContentType(), reference(), false);
		
		List<Column> columns = new ArrayList<Column>();
		for (BannerResponsesColumn column: BannerResponsesColumn.values()) {
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
			for (int i = 0; i < columns.size(); i++) {
				row[i] = getCell(a, columns.get(i), 0);
				if (row[i] == null) {
					row[i] = new String();
				}
			}
				
			
			out.printLine(row);
			out.flush();
		}
		
		if (list.isEmpty()) {
			A[] row = new A[columns.size()];
			row[0] = new A(MESSAGES.errorNoData(), F.ITALIC).color("#FF0000");
			out.printLine(row);
			out.flush();
		}
		
		out.flush(); out.close();
	}
	
	public A getPdfCell(BannerResponseInterface request, Column column) {
		switch (column.getColumn()) {
		case UNIQUEID:
			if (request.getUniqueId() == null) {
				return new A();
			}
			return new A(request.getUniqueId().toString());
		case ACTIVITY_DATE:
			if (request.getActivityDateStr() == null) {
				return new A();
			}
			return new A(request.getActivityDateStr());
		case CAMPUS:
			if (request.getCampus() == null) {
				return new A();
			}
			return new A(request.getCampus());
		case SUBJECT_CODE:
			if (request.getSubjectCode() == null) {
				return new A();
			}
			return new A(request.getSubjectCode());
		case COURSE_NUMBER:
			if (request.getCourseNumber() == null) {
				return new A();
			}
			return new A(request.getCourseNumber());
		case CRN:
			if (request.getCrn() == null) {
				return new A();
			}
			return new A(request.getCrn());
		case SECTION_NUMBER:
			if (request.getSectionNumber() == null) {
				return new A();
			}
			return new A(request.getSectionNumber());
		case XLST_GROUP:
			if (request.getXlstGroup() == null) {
				return new A();
			}
			return new A(request.getXlstGroup());
		case ACTION:
			if (request.getAction() == null) {
				return new A();
			}
			new A(request.getAction());
		case TYPE :
			if (request.getType() == null) {
				return new A();
			}
			new A(request.getType());
		case MESSAGE:
			if (request.getMessage() == null) {
				return new A();
			}
			new A(request.getMessage());
		default:
			return new A();
		}
	}


}
