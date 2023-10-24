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
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.banner.model.BannerResponse;
import org.unitime.banner.model.Queue;
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.QueueOut;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.model.dao.QueueOutDAO;
import org.unitime.banner.queueprocessor.exception.LoggableException;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.dataexchange.BaseImport;


/**
 * @author says
 *
 */
public class ReceiveBannerResponseMessage extends BaseImport {

	private static String rootName = "SCHEDULE_RESPONSE";
	private static String bannerResponseName = "MESSAGE";
	private Long iQueueId;
	private String iSentTermCode;
	private boolean iSaveNoChangeMessages;

	public Long getQueueId() {
		return iQueueId;
	}

	public void setQueueId(Long queueId) {
		this.iQueueId = queueId;
	}

	public String getSentTermCode() {
		return iSentTermCode;
	}

	public void setSentTermCode(String sentTermCode) {
		this.iSentTermCode = sentTermCode;
	}

	public ReceiveBannerResponseMessage() {
		super();
		iSaveNoChangeMessages = ApplicationProperties.getProperty("banner.queue.saveNoChangeResponses", "true") == "true";
	}

	public static void receiveResponseDocument(QueueIn queueIn) throws LoggableException  {
		Element rootElement = queueIn.getDocument().getRootElement();
		if (rootElement.getName().equalsIgnoreCase(rootName)){
			try {
				ReceiveBannerResponseMessage rbrm = new ReceiveBannerResponseMessage();
				rbrm.setQueueId(queueIn.getMatchId());
				rbrm.loadXml(rootElement);
				queueIn.setProcessDate(new Date());
				queueIn.setStatus(Queue.STATUS_PROCESSED);
				QueueInDAO.getInstance().getSession().merge(queueIn);
			} catch (Exception e) {
				LoggableException le = new LoggableException(e, queueIn);
				le.logError();
				throw le;
			}
		} 
	}
	
	private BannerResponse createInitialBannerResponseFrom(BannerSectionInfoHelper bannerSectionInfo) {
		BannerResponse bannerResponse = new BannerResponse();
		bannerResponse.setTermCode(bannerSectionInfo.getTermCode());
		bannerResponse.setCrn(bannerSectionInfo.getCrn() != null ? bannerSectionInfo.getCrn().toString() : null);
		bannerResponse.setSubjectCode(bannerSectionInfo.getSubject());
		bannerResponse.setCourseNumber(bannerSectionInfo.getCourse());
		bannerResponse.setBannerSection(bannerSectionInfo.getBannerSection());
		bannerResponse.setCampus(bannerSectionInfo.getCampus());
		bannerResponse.setSession(bannerSectionInfo.getBannerSession() == null?null : bannerSectionInfo.getBannerSession().getSession());
		bannerResponse.setExternalId(bannerSectionInfo.getBannerSectionId() == null? null : bannerSectionInfo.getBannerSectionId().toString());
		bannerResponse.setXlstGroup(bannerSectionInfo.getXlstCode());
		bannerResponse.setSubjectArea(bannerSectionInfo.getSubjectArea());
		if (bannerSectionInfo.getBannerSection() != null) {
			bannerResponse.setSectionNumber(bannerSectionInfo.getBannerSection().getSectionIndex());
		}
		return bannerResponse;
	}
	
	private BannerResponse createBannerResponseForResponseElement(Element bannerResponseElement, 
			BannerSectionInfoHelper bsi,
			HashMap<Long, BannerSectionInfoHelper> bannerSections, 
			HashMap<String, BannerSectionInfoHelper> bannerCrosslists) throws Exception {

		BannerResponse resp = createInitialBannerResponseFrom(bsi);

		String dateStr = getRequiredStringAttribute(bannerResponseElement, "ACTIVITY_DATE", bannerResponseName);
		DateFormat df = new SimpleDateFormat("MM/dd/yyyy H:m:s");
		try {
			Date aDate = df.parse(dateStr);
			resp.setActivityDate(aDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}							
		
		resp.setSequenceNumber(getRequiredIntegerAttribute(bannerResponseElement, "SEQNO", bannerResponseName));
		String secNo = getOptionalStringAttribute(bannerResponseElement, "SEQ_NUMB");
		if (secNo != null && (resp.getSectionNumber() == null || !secNo.equals(resp.getSectionNumber()))) {
			resp.setSectionNumber(secNo);
		}
		resp.setAction(getOptionalStringAttribute(bannerResponseElement, "ACTION"));
		resp.setType(getOptionalStringAttribute(bannerResponseElement, "TYPE"));
		resp.setMessage(getRequiredStringAttribute(bannerResponseElement, "MESSAGE", bannerResponseName));
		resp.setPacketId(getRequiredStringAttribute(bannerResponseElement, "PACKET_ID", bannerResponseName));
		resp.setQueueId(iQueueId);
		return(resp);

	}
	
	private void internalProcessResponseWithNoMatchingSentMessage(Element rootElement, boolean includeBanerSectionLinks) throws Exception {
		HashMap<Long, BannerSectionInfoHelper> bannerSections = new HashMap<Long, BannerSectionInfoHelper>();
		HashMap<String, BannerSectionInfoHelper> bannerCrosslists = new HashMap<String, BannerSectionInfoHelper>();
		for (Iterator<Element> eIt = rootElement.elementIterator(bannerResponseName); eIt.hasNext();) {
			Element bannerResponseElement = (Element) eIt.next();
			BannerSectionInfoHelper bsi = getBannerSectionInfoHelperForResponseElement(bannerResponseElement, bannerSections, bannerCrosslists);
			BannerResponse resp = createBannerResponseForResponseElement(bannerResponseElement, bsi, bannerSections, bannerCrosslists);
			if (!includeBanerSectionLinks) resp.setBannerSection(null);
			getHibSession().persist(resp);
		}
	}
	
	private void processResponseWithNoMatchingSentMessage(Element rootElement)  {
		try {
			if (rootElement.getName().equalsIgnoreCase(rootName)) {
				beginTransaction();
				internalProcessResponseWithNoMatchingSentMessage(rootElement, true);
				getHibSession().getTransaction().commit();
			}
		} catch (Exception e) {
			warn("Unable to store response message, retrying..., reason: "+e.getMessage());
			rollbackTransaction();
			try {
				if (rootElement.getName().equalsIgnoreCase(rootName)) {
					beginTransaction();
					internalProcessResponseWithNoMatchingSentMessage(rootElement, false);
					commitTransaction();
				}
			} catch (Exception f) {
				warn("Unable to store response message, reason: "+f.getMessage(), f);
				rollbackTransaction();
			}
		}
	}
	
	private void internalProcessMessageWithMatchingSentMessage(Element rootElement, Element matchingSentRootElement, boolean includeBanerSectionLinks) throws Exception {
		HashMap<Long, BannerSectionInfoHelper> bannerSections = new HashMap<Long, BannerSectionInfoHelper>();
		HashMap<String, BannerSectionInfoHelper> bannerCrosslists = new HashMap<String, BannerSectionInfoHelper>();
		ArrayList<BannerSectionInfoHelper> sentMessages = new ArrayList<BannerSectionInfoHelper>();
		setSentTermCode(getRequiredStringAttribute(matchingSentRootElement, "TERM_CODE", "SCHEDULE"));
		
		Iterator<Element> matchingSentMessageElementIterator = matchingSentRootElement.elementIterator();
		int order = 0;
		boolean createMessagesForAllSentElements = !BannerMessage.BannerMessageAction.AUDIT.toString().equals(getRequiredStringAttribute(matchingSentRootElement, "ACTION", "SCHEDULE"));
		if (createMessagesForAllSentElements) {
			while (matchingSentMessageElementIterator.hasNext()) {
				Element e = matchingSentMessageElementIterator.next();
				BannerSectionInfoHelper bsih = getBannerSectionInfoHelperForSentElement(e, bannerSections, bannerCrosslists, order);
				sentMessages.add(bsih);	
				order++;
			}
		}
				
		int lastMatchedSent = -1;
		int currentMatchedSent = -1;
		String packetId = getRequiredStringAttribute(rootElement, "PACKET_ID", rootName);
		Iterator<Element> responseElementIterator = rootElement.elementIterator();
		while (responseElementIterator.hasNext()) {
			Element responseMessage = responseElementIterator.next();
			String action = getOptionalStringAttribute(responseMessage, "ACTION");
			BannerSectionInfoHelper bsih = getBannerSectionInfoHelperForResponseElement(responseMessage, bannerSections, bannerCrosslists);
			if (action == null) {
				createMessagesForAllSentElements = false;
				BannerResponse resp = createBannerResponseForResponseElement(responseMessage, bsih, bannerSections, bannerCrosslists);
				if (!includeBanerSectionLinks) resp.setBannerSection(null);
				getHibSession().persist(resp);
				break;
			} else {
				BannerResponse resp = createBannerResponseForResponseElement(responseMessage, bsih, bannerSections, bannerCrosslists);
				String xlstGrp = getOptionalStringAttribute(responseMessage, "XLST_GROUP");
				if (xlstGrp == null) {
	 				if (bsih.getSentSection()) {
	 					bsih.setReceivedSectionResponse(true);
	 					currentMatchedSent = bsih.getSentSectionPosition();
	 				}
				} else {
					if (bsih.getSentCrosslist()) {
						bsih.setReceivedCrosslistResponse(true);
						currentMatchedSent = bsih.getSentCrosslistPosition();
					}
				}
				
				String dateStr = getRequiredStringAttribute(responseMessage, "ACTIVITY_DATE", bannerResponseName);
				DateFormat df = new SimpleDateFormat("MM/dd/yyyy H:m:s");
				Date endTimestamp = null;				
				try {
					endTimestamp = df.parse(dateStr);
				} catch (ParseException e) {
					e.printStackTrace();
				}
				createNoChangeBannerResponsesForUnmatchedSentMessages(
						createMessagesForAllSentElements, 
						sentMessages, 
						lastMatchedSent, 
						currentMatchedSent,
						endTimestamp,
						packetId,
						includeBanerSectionLinks);
				lastMatchedSent = currentMatchedSent;
				if (!includeBanerSectionLinks) resp.setBannerSection(null);
				getHibSession().persist(resp);
				
			}				
		}
					
		String dateStr = getRequiredStringAttribute(rootElement, "END_TIMESTAMP", rootName);
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd H:m:s");
		Date endTimestamp = null;				
		try {
			endTimestamp = df.parse(dateStr);
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		createNoChangeBannerResponsesForUnmatchedSentMessages(
				createMessagesForAllSentElements, 
				sentMessages, 
				lastMatchedSent, 
				sentMessages.size(),
				endTimestamp,
				packetId,
				includeBanerSectionLinks);
	}
		
	private void processMessageWithMatchingSentMessage(Element rootElement, Element matchingSentRootElement) {
		try {
	 		if (rootElement.getName().equalsIgnoreCase(rootName)) {
	 			beginTransaction();
	 			internalProcessMessageWithMatchingSentMessage(rootElement, matchingSentRootElement, true);
	 			getHibSession().getTransaction().commit();
			}
		} catch (Exception e) {
			warn("Unable to store response message, retrying..., reason: "+e.getMessage());
			rollbackTransaction();
			try {
				if (rootElement.getName().equalsIgnoreCase(rootName)) {
		 			beginTransaction();
		 			internalProcessMessageWithMatchingSentMessage(rootElement, matchingSentRootElement, false);
		 			commitTransaction();
				}
			} catch (Exception f) {
				warn("Unable to store response message, reason: " + f.getMessage(), f);
				rollbackTransaction();
			}
		}
	}
	
	private void createNoChangeBannerResponsesForUnmatchedSentMessages(
			boolean responsesNeeded, 
			ArrayList<BannerSectionInfoHelper> sentMessages, 
			int lastMatchedSent, 
			int currentMatchedSent,
			Date endTimestamp,
			String packetId,
			boolean includeBanerSectionLinks) {
		
		if (iSaveNoChangeMessages && responsesNeeded && currentMatchedSent != lastMatchedSent) {
			for (int i = lastMatchedSent + 1 ; i < currentMatchedSent; i++) {
				BannerResponse noChangeResponse = createNoChangeResponse(sentMessages.get(i), endTimestamp, packetId, i);
				if (noChangeResponse != null) {
					if (!includeBanerSectionLinks) noChangeResponse.setBannerSection(null);
					getHibSession().persist(noChangeResponse);
				}
			}
		}
	}
	
	private BannerResponse createNoChangeResponse(BannerSectionInfoHelper bsi,
			Date processedTimestamp, String packetId, int messagePosition) {
		BannerResponse resp = createInitialBannerResponseFrom(bsi);

		resp.setActivityDate(processedTimestamp);
		
		resp.setSequenceNumber(-1);
		resp.setSectionNumber(bsi.getBannerSection() == null? null : bsi.getBannerSection().getSectionIndex());
		if (bsi.getSentSectionPosition() != null 
				&& bsi.getSentSectionPosition().intValue() == messagePosition
				&& (bsi.getReceivedSectionResponse() == null 
			    || !bsi.getReceivedSectionResponse())) {
			resp.setAction(bsi.getSentSectionAction());			
			resp.setMessage("No Change to Section Information");
		} else if (bsi.getSentCrosslistPosition() != null 
				&& bsi.getSentCrosslistPosition().intValue() == messagePosition
				&& !bsi.getReceivedCrosslistResponse()) {
			resp.setAction(bsi.getSentCrossListAction());
			resp.setMessage("No Change to Crosslist Information");
		} else {
			return null;
		}
		resp.setType("NO_CHANGE");
		resp.setPacketId(packetId);
		resp.setQueueId(iQueueId);
		return(resp);
	}

	private BannerSectionInfoHelper getBannerSectionInfoHelperForResponseElement(Element responseElement,
			HashMap<Long, BannerSectionInfoHelper> bannerSections, 
			HashMap<String, BannerSectionInfoHelper> bannerCrosslists) throws Exception {
		Integer crn = getOptionalIntegerAttribute(responseElement, "CRN");				
		String termCode = getRequiredStringAttribute(responseElement, "TERM_CODE", bannerResponseName);
		String subj = getOptionalStringAttribute(responseElement, "SUBJ_CODE");
		String crs = getOptionalStringAttribute(responseElement, "CRSE_NUMB");
		String xlstGrp = getOptionalStringAttribute(responseElement, "XLST_GROUP");		
		Long bannerSectionId = null;
		if (xlstGrp == null) {
			bannerSectionId = getOptionalLongAttribute(responseElement, "EXTERNAL_ID");
		}

		BannerSectionInfoHelper bsi = null;
		if (bannerSectionId != null) {
			bsi = bannerSections.get(bannerSectionId);
			if (bsi != null) {
				bsi.setReceivedSectionResponse(true);
			}
		}
		if (xlstGrp != null && bsi == null) {
			bsi = bannerCrosslists.get(xlstGrp);
		}
		if (xlstGrp != null && bsi != null) {
			bsi.setReceivedCrosslistResponse(true);
		}
		if (bsi == null) {					
			bsi = new BannerSectionInfoHelper(crn, subj, crs, xlstGrp, null, termCode, bannerSectionId);
		} else {
			bsi.fillInMissingFieldsIfNeeded(crn, subj, crs, xlstGrp, null, termCode, bannerSectionId);
		}
		if (bsi.getBannerSectionId() != null && xlstGrp == null) {
			if (bannerSections.get(bsi.getBannerSectionId()) != null) {
				BannerSectionInfoHelper mergeBannerSectionInfoHelper = bannerSections.get(bsi.getBannerSectionId());
				if (mergeBannerSectionInfoHelper != null && !mergeBannerSectionInfoHelper.equals(bsi)) {
					mergeBannerSectionInfoHelper.fillInMissingFieldsIfNeeded(bsi.getCrn(), bsi.getSubject(), bsi.getCourse(), bsi.getXlstCode(), bsi.getCampus(), bsi.getTermCode(), bsi.getBannerSectionId());
					bsi = mergeBannerSectionInfoHelper;
					bsi.setReceivedSectionResponse(true);
				}
			} else {
				bsi.setReceivedSectionResponse(true);
				bannerSections.put(bsi.getBannerSectionId(), bsi);
			}
		}
		
		if (xlstGrp != null && bannerSectionId == null) {
			if (bannerCrosslists.get(xlstGrp) == null) {
				bsi.setReceivedCrosslistResponse(true);
				bannerCrosslists.put(xlstGrp, bsi);
			}
		}
			
        return(bsi);
	}
	

	private BannerSectionInfoHelper getBannerSectionInfoHelperForSentElement(Element sentElement,
			HashMap<Long, BannerSectionInfoHelper> bannerSections, 
			HashMap<String, BannerSectionInfoHelper> bannerCrosslists, 
			int order) throws Exception {
		String action = null;
		Integer crn = null;
		String subj = null;
		String crs = null;
		String campus = null;
		String xlistId = null;
		Long bannerSectionId = null;

		BannerSectionInfoHelper bsih = null;
		boolean section = false;
		boolean crosslist = false;
		action = getRequiredStringAttribute(sentElement, "ACTION", "SECTION");
		if (sentElement.getName().equals("SECTION")) {
			crn = getRequiredIntegerAttribute(sentElement, "CRN", "SECTION");
			subj = getOptionalStringAttribute(sentElement, "SUBJ_CODE");
			crs = getOptionalStringAttribute(sentElement, "CRSE_NUMB");
			campus = getOptionalStringAttribute(sentElement, "CAMP_CODE");
			bannerSectionId = getOptionalLongAttribute(sentElement, "EXTERNAL_ID");
			bsih = bannerSections.get(bannerSectionId);
			section = true;
		} else if (sentElement.getName().equals("CROSSLIST")) {
			xlistId = getRequiredStringAttribute(sentElement, "GROUP", "CROSSLIST");
			bsih = bannerCrosslists.get(xlistId);
			crosslist = true;
		} else {
			Debug.info("Unknown Banner Message Element Name:  " + sentElement.getName());
		}
		if (bsih != null) {
			bsih.fillInMissingFieldsIfNeeded(crn, subj, crs, xlistId, campus, getSentTermCode(), bannerSectionId);
			if (bsih.getBannerSectionId() != null && bannerSections.get(bsih.getBannerSectionId()) == null) {
				bannerSections.put(bsih.getBannerSectionId(), bsih);
			}
			if (bsih.getXlstCode() != null && bannerCrosslists.get(bsih.getXlstCode()) == null) {
				if (bsih.getBannerSection().getBannerConfig() != null
					&& bsih.getBannerSection().getBannerConfig().getBannerCourse() != null
					&& bsih.getBannerSection().getBannerConfig().getBannerCourse().getCourseOffering(getHibSession()) != null
					&& bsih.getBannerSection().getBannerConfig().getBannerCourse().getCourseOffering(getHibSession()).isIsControl()) {
					bannerCrosslists.put(bsih.getXlstCode(), bsih);						
				}
			}
		} else {
			bsih = new BannerSectionInfoHelper(crn, subj, crs, xlistId, campus, getSentTermCode(), bannerSectionId);
			if (bsih.getBannerSectionId() != null) {
				bannerSections.put(bsih.getBannerSectionId(), bsih);
				if (bsih.getXlstCode() != null && bannerCrosslists.get(bsih.getXlstCode()) == null) {
					if (bsih.getBannerSection().getBannerConfig() != null
						&& bsih.getBannerSection().getBannerConfig().getBannerCourse() != null
						&& bsih.getBannerSection().getBannerConfig().getBannerCourse().getCourseOffering(getHibSession()) != null
						&& bsih.getBannerSection().getBannerConfig().getBannerCourse().getCourseOffering(getHibSession()).isIsControl()) {
						bannerCrosslists.put(bsih.getXlstCode(), bsih);						
					}
				}
			}
			if (crosslist) {
				bannerCrosslists.put(xlistId, bsih);
			}
		}		
		if (section) {
			bsih.setSentSection(true);
			bsih.setSentSectionPosition(order);
			bsih.setSentSectionAction(action);
			
		}
		if (crosslist) {
			bsih.setSentCrosslist(true);
			bsih.setSentCrosslistPosition(order);
			bsih.setSentCrossListAction(action);
		}
		return bsih;
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		Debug.info("Starting loadxml");
		QueueOut sentMessage = QueueOutDAO.getInstance().get(iQueueId);
		if (sentMessage != null) {
			processMessageWithMatchingSentMessage(rootElement, sentMessage.getDocument().getRootElement());
		} else {
			processResponseWithNoMatchingSentMessage(rootElement);
		}
		Debug.info("Finishing loadxml");
	}

}
