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
package org.unitime.banner.export;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.unitime.banner.server.offerings.BannerOfferingTableBuilder;
import org.unitime.timetable.export.ExportHelper;
import org.unitime.timetable.export.courses.OfferingsCSV;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;

@Service("org.unitime.timetable.export.Exporter:banner-offerings.csv")
public class BannerOfferingsCSV extends OfferingsCSV {
	@Override
	public String reference() {
		return "banner-offerings.csv";
	}
	
	@Override
	public void export(ExportHelper helper) throws IOException {
		checkPermission(helper, Right.InstructionalOfferingsExportPDF);
		exportDataCsv(getBannerOfferings(helper, classAssignmentService.getAssignment()), helper);
	}
	
	protected static List<TableInterface> getBannerOfferings(ExportHelper helper, ClassAssignmentProxy classAssignments) {
    	List<TableInterface> response = new ArrayList<TableInterface>();
    	
    	BannerOfferingTableBuilder builder = new BannerOfferingTableBuilder(
    			helper.getSessionContext(),
    			helper.getParameter("backType"),
		        helper.getParameter("backId")
		        );
    	builder.setSimple(true);
    	
    	builder.generateTableForBannerOfferings(
				classAssignments,
		        new Filter(helper), 
		        helper.getParameter("subjectArea").split(","), 
		        response);

    	return response;
	}
}
