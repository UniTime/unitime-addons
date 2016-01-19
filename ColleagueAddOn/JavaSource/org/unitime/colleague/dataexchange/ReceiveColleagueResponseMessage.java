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
package org.unitime.colleague.dataexchange;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.colleague.model.ColleagueResponse;
import org.unitime.colleague.model.Queue;
import org.unitime.colleague.model.QueueIn;
import org.unitime.colleague.model.dao.QueueInDAO;
import org.unitime.colleague.queueprocessor.exception.LoggableException;
import org.unitime.timetable.dataexchange.BaseImport;


;

/**
 * @author says
 *
 */
public class ReceiveColleagueResponseMessage extends BaseImport {

	private static String rootName = "SCHEDULE_RESPONSE";
	private static String colleagueResponseName = "MESSAGE";
	private Long queueId;

	public Long getQueueId() {
		return queueId;
	}

	public void setQueueId(Long queueId) {
		this.queueId = queueId;
	}

	public ReceiveColleagueResponseMessage() {
		super();
	}

	public static void receiveResponseDocument(QueueIn queueIn) throws LoggableException  {
		Element rootElement = queueIn.getXml().getRootElement();
		if (rootElement.getName().equalsIgnoreCase(rootName)){
			try {
				ReceiveColleagueResponseMessage rbrm = new ReceiveColleagueResponseMessage();
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
			for (Iterator eIt = rootElement.elementIterator(colleagueResponseName); eIt.hasNext();) {
				Element colleagueResponseElement = (Element) eIt.next();
				ColleagueResponse resp = new ColleagueResponse();
				String dateStr = getRequiredStringAttribute(colleagueResponseElement, "ACTIVITY_DATE", colleagueResponseName);
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy H:m:s");
				try {
					Date aDate = df.parse(dateStr);
					resp.setActivityDate(aDate);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				resp.setSequenceNumber(getRequiredIntegerAttribute(colleagueResponseElement, "SEQNO", colleagueResponseName));
				resp.setTermCode(getRequiredStringAttribute(colleagueResponseElement, "TERM_CODE", colleagueResponseName));
				resp.setColleagueId(getOptionalStringAttribute(colleagueResponseElement, "COLLEAGUE_SYNONYM"));
				resp.setSubjectCode(getOptionalStringAttribute(colleagueResponseElement, "SUBJ_CODE"));
				resp.setCourseNumber(getOptionalStringAttribute(colleagueResponseElement, "CRSE_NUMB"));
				resp.setSectionNumber(getOptionalStringAttribute(colleagueResponseElement, "SEQ_NUMB"));
				resp.setExternalId(getOptionalStringAttribute(colleagueResponseElement, "EXTERNAL_ID"));
				resp.setAction(getOptionalStringAttribute(colleagueResponseElement, "ACTION"));
				resp.setType(getOptionalStringAttribute(colleagueResponseElement, "TYPE"));
				resp.setMessage(getRequiredStringAttribute(colleagueResponseElement, "MESSAGE", colleagueResponseName));
				resp.setPacketId(getRequiredStringAttribute(colleagueResponseElement, "PACKET_ID", colleagueResponseName));
				resp.setQueueId(queueId);
				getHibSession().save(resp);
			}
			commitTransaction();
		}
	}

}
