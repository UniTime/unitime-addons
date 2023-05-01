package org.unitime.banner.util;

import java.util.List;

import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.model.BannerResponse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.QueueIn;
import org.unitime.commons.Debug;
import org.unitime.timetable.interfaces.ExternalInstrOffrConfigChangeAction;
import org.unitime.timetable.interfaces.ExternalSectionMonitoredUpdateMessage;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;

public class BannerExternalSectionMonitoredUpdateMessage implements ExternalSectionMonitoredUpdateMessage {

	
	private Long getUniqueIdOfQueueMessage(BannerSession bannerSession, BannerSection bannerSection, Long afterQueueUniqueId, org.hibernate.Session hibSession) {
		if (bannerSession == null || bannerSection == null) {
			return null;
		}
		StringBuffer sb = new StringBuffer();
		sb.append("select min(qo.uniqueId) from QueueOut qo where qo.uniqueId > ")
		  .append(afterQueueUniqueId) 
		  .append(" and qo.xml like '%CRN=\"")
		  .append(bannerSection.getCrn())
		  .append("\"%' and qo.xml like '%TERM_CODE=\"")
		  .append(bannerSession.getBannerTermCode())
		  .append("\"%' and qo.xml like '%ACTION=\"")
		  .append(BannerMessageAction.UPDATE)
		  .append("\"%'")
		  ;
		Debug.info(sb.toString());
		return hibSession.createQuery(sb.toString(), Number.class).setCacheable(false).uniqueResult().longValue();
	}
	

	private QueueIn getQueueInMessageFor(Long outgoingUniqueId, int secondsToWait, org.hibernate.Session hibSession) {
		for (int i = 0 ; i < secondsToWait; i = i + 2) {
			QueueIn queueIn = hibSession
					.createQuery("from QueueIn qi where qi.matchId = :queueOutId", QueueIn.class)
					.setParameter("queueOutId", outgoingUniqueId)
					.setCacheable(false)
					.uniqueResult();
			if (queueIn != null) {
				return queueIn;
			} else {
				try {
				    Thread.sleep(2 * 1000);
				} catch (InterruptedException ie) {
				    Thread.currentThread().interrupt();
				}
			}
		}
		return null;
				
	}

	private List<BannerResponse> getResponseMessageFor(Long outgoingUniqueId, Integer crn, org.hibernate.Session hibSession) {
		List<BannerResponse> bannerResponses = hibSession
				.createQuery("from BannerResponse br where br.queueId = :queueOutId and br.crn = :crn order by br.uniqueId", BannerResponse.class)
				.setParameter("queueOutId", outgoingUniqueId)
				.setParameter("crn", crn.toString())
				.setCacheable(false)
				.list();
		if (!bannerResponses.isEmpty()) {
			return bannerResponses;
		}
		return null;
				
	}
	
	@Override
	public ExternalSectionCreationStatus monitorExternalSectionUpdate(CourseOffering courseOffering, Class_ clazz,
			ExternalInstrOffrConfigChangeAction configChangeAction, int secondsToWait, org.hibernate.Session hibSession) {
		
		if (clazz == null) {
			return ExternalSectionCreationStatus.DOES_NOT_EXIST;
		}
		BannerSession bannerSession = BannerSession.findBannerSessionForSession(courseOffering.getInstructionalOffering().getSession(), hibSession);
		if (bannerSession == null || !bannerSession.isSendDataToBanner()) {
			return ExternalSectionCreationStatus.DOES_NOT_EXIST;			
		}
		
		Long latestQueueUid = hibSession.createQuery("select max(qo.uniqueId) from QueueOut qo", Long.class).setCacheable(false).uniqueResult();
		configChangeAction.performExternalInstrOffrConfigChangeAction(courseOffering.getInstructionalOffering(), hibSession);
		BannerSection bannerSection = BannerSection.findBannerSectionForClassAndCourseOffering(clazz, courseOffering, hibSession);
		Long updateRequestOutgoingUid = getUniqueIdOfQueueMessage(bannerSession, bannerSection, latestQueueUid, hibSession);
		if (updateRequestOutgoingUid == null) {
			return ExternalSectionCreationStatus.DOES_NOT_EXIST;	
		}

		QueueIn queueIn = getQueueInMessageFor(updateRequestOutgoingUid, secondsToWait, hibSession);
		if (queueIn == null) {
			return ExternalSectionCreationStatus.PENDING;
		}
		List<BannerResponse> bannerResponses = getResponseMessageFor(updateRequestOutgoingUid, bannerSection.getCrn(), hibSession);
		if (bannerResponses == null) {
			return ExternalSectionCreationStatus.SUCCESS;
		}
		for (BannerResponse bannerResponse: bannerResponses) {
			if (bannerResponse.getType().equals("ERROR")) {
				return ExternalSectionCreationStatus.FAILED;
			}
		}
		return ExternalSectionCreationStatus.SUCCESS;
	}

}
