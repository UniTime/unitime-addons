/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
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
				resp.setMessage(getOptionalStringAttribute(bannerResponseElement, "MESSAGE"));
				resp.setPacketId(getOptionalStringAttribute(bannerResponseElement, "PACKET_ID"));
				resp.setQueueId(queueId);
				getHibSession().save(resp);
			}
			commitTransaction();
		}
	}

}
