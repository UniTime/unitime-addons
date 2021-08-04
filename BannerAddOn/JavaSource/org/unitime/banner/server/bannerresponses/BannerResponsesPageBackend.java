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
package org.unitime.banner.server.bannerresponses;

import java.text.DateFormat;
import java.text.DecimalFormat;

import org.apache.log4j.Logger;
import org.unitime.banner.defaults.BannerRelatedSessionAttribute;
import org.unitime.banner.model.BannerResponse;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.client.GwtRpcResponseList;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.BannerGwtConstants;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.BannerResponseException;
import org.unitime.timetable.gwt.shared.BannerResponseInterface;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesPageRequest;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(BannerResponsesPageRequest.class)
public class BannerResponsesPageBackend implements GwtRpcImplementation<BannerResponsesPageRequest, GwtRpcResponseList<BannerResponseInterface>> {

	private static Logger sLog = Logger.getLogger(BannerResponsesPageBackend.class);
	protected static final BannerGwtConstants CONSTANTS = Localization.create(BannerGwtConstants.class);
	private static DateFormat sDateFormat = Localization.getDateFormat(CONSTANTS.timeStampFormat());
	private static DecimalFormat sDF = new DecimalFormat("0.0");

	@Override
	public GwtRpcResponseList<BannerResponseInterface> execute(BannerResponsesPageRequest request,
			SessionContext context) {
		context.checkPermission(Right.InstructionalOfferings);

		try {
			sLog.debug("findBannerResponse(filter='" + request.getFilter()+"')");
			Long s0 = System.currentTimeMillis();
			GwtRpcResponseList<BannerResponseInterface> results = new GwtRpcResponseList<BannerResponseInterface>();
			context.setAttribute(BannerRelatedSessionAttribute.BannerResponsesLastFilter.key(), request.getFilter().toQueryString());
			for (BannerResponse br: 
						BannerResponsesFilterBackend
						  .bannerResponses(
							context.getUser().getCurrentAcademicSessionId(), 
							request.getFilter().getOptions(), 
							new Query(request.getFilter().getText()), null, Department.getUserDepartments(context.getUser()))) 
			    {
				BannerResponseInterface bri = new BannerResponseInterface();
				bri.setUniqueId(br.getUniqueId());
				bri.setAction(br.getAction());
				bri.setActivityDateStr(sDateFormat.format(br.getActivityDate()));
				bri.setCourseNumber(br.getCourseNumber());
				bri.setCrn(br.getCrn());
				bri.setCampus(br.getCampus());
				bri.setExternalId(br.getExternalId());
				bri.setMessage(br.getMessage());
				bri.setPacketId(br.getPacketId());
				bri.setQueueId(br.getQueueId());
				bri.setSectionNumber(br.getSectionNumber());
				bri.setSequenceNumber(br.getSequenceNumber());
				bri.setSubjectCode(br.getSubjectCode());
				bri.setTermCode(br.getTermCode());
				bri.setType(br.getType());
				bri.setXlstGroup(br.getXlstGroup());
				results.add(bri);
			}
			sLog.debug("Found " + results.size() + " banner responses (took " + sDF.format(0.001 * (System.currentTimeMillis() - s0)) +" s).");
			return results;
		} catch (PageAccessException e) {
			throw e;
		} catch (BannerResponseException e) {
			throw e;
		} catch (Exception e) {
			sLog.error(e.getMessage(), e);
			throw new BannerResponseException(e.getMessage());
		}
	
	}

}
