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
package org.unitime.banner.dataexchange;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.banner.model.BannerResponse;
import org.unitime.banner.model.Queue;
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;
import org.unitime.timetable.dataexchange.BaseImport;


;

/**
 * @author says
 *
 */
public class ReceiveBannerResponseMessage extends BaseImport {

	private static String rootName = "SCHEDULE_RESPONSE";
	private static String bannerResponseName = "MESSAGE";
	private Long queueId;

	public Long getQueueId() {
		return queueId;
	}

	public void setQueueId(Long queueId) {
		this.queueId = queueId;
	}

	public ReceiveBannerResponseMessage() {
		super();
	}

	public static void receiveResponseDocument(QueueIn queueIn) throws LoggableException  {
		Element rootElement = queueIn.getXml().getRootElement();
		if (rootElement.getName().equalsIgnoreCase(rootName)){
			try {
				ReceiveBannerResponseMessage rbrm = new ReceiveBannerResponseMessage();
				rbrm.setQueueId(queueIn.getMatchId());
				rbrm.loadXml(rootElement);
				queueIn.setProcessDate(new Date());
				queueIn.setStatus(Queue.STATUS_PROCESSED);
				QueueInDAO.getInstance().getSession().update(queueIn);
			} catch (Exception e) {
				LoggableException le = new LoggableException(e, queueIn);
				le.logError();
				throw le;
			}
		} 
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadXml(Element rootElement) throws Exception {
		if (rootElement.getName().equalsIgnoreCase(rootName)) {
			beginTransaction();
			for (Iterator eIt = rootElement.elementIterator(bannerResponseName); eIt.hasNext();) {
				Element bannerResponseElement = (Element) eIt.next();
				BannerResponse resp = new BannerResponse();
				String dateStr = getRequiredStringAttribute(bannerResponseElement, "ACTIVITY_DATE", bannerResponseName);
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy H:m:s");
				try {
					Date aDate = df.parse(dateStr);
					resp.setActivityDate(aDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				resp.setSequenceNumber(getRequiredIntegerAttribute(bannerResponseElement, "SEQNO", bannerResponseName));
				resp.setTermCode(getRequiredStringAttribute(bannerResponseElement, "TERM_CODE", bannerResponseName));
				resp.setCrn(getOptionalStringAttribute(bannerResponseElement, "CRN"));
				resp.setSubjectCode(getOptionalStringAttribute(bannerResponseElement, "SUBJ_CODE"));
				resp.setCourseNumber(getOptionalStringAttribute(bannerResponseElement, "CRSE_NUMB"));
				resp.setSectionNumber(getOptionalStringAttribute(bannerResponseElement, "SEQ_NUMB"));
				resp.setXlstGroup(getOptionalStringAttribute(bannerResponseElement, "XLST_GROUP"));
				resp.setExternalId(getOptionalStringAttribute(bannerResponseElement, "EXTERNAL_ID"));
				resp.setAction(getOptionalStringAttribute(bannerResponseElement, "ACTION"));
				resp.setType(getOptionalStringAttribute(bannerResponseElement, "TYPE"));
				resp.setMessage(getRequiredStringAttribute(bannerResponseElement, "MESSAGE", bannerResponseName));
				resp.setPacketId(getRequiredStringAttribute(bannerResponseElement, "PACKET_ID", bannerResponseName));
				resp.setQueueId(queueId);
				getHibSession().save(resp);
			}
			commitTransaction();
		}
	}

}
