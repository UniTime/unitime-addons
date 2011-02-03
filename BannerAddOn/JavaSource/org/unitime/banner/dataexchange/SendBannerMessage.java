/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.banner.dataexchange;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.QueueOut;
import org.unitime.banner.model.dao.QueueOutDAO;
import org.unitime.commons.Debug;


/**
 * @author says
 *
 */
public class SendBannerMessage {

	/**
	 * 
	 */
	public SendBannerMessage() {
		
	}

	
	public static void addBannerSectionsToMessage(List<BannerSection> bannerSections, BannerMessageAction bannerMessageAction, Session hibSession, BannerMessage bannerMessage){
        if (bannerSections == null || bannerSections.isEmpty()){
        	return;
        }
        BannerSection bs = bannerSections.get(0);
        BannerSession bannerSession = BannerSession.findBannerSessionForSession(bs.getSession(), hibSession);
        if (BannerSession.shouldSendDataToBannerForSession(bs.getSession(), hibSession) || (bannerSession.isStoreDataForBanner().booleanValue() && BannerMessageAction.AUDIT.equals(bannerMessageAction))){
			HashMap<String, BannerSection> crosslistMap = new HashMap<String, BannerSection>();
			for(Iterator<BannerSection> it = bannerSections.iterator(); it.hasNext();){
				bs = it.next();
				bannerMessage.addBannerSectionToMessage(bs, bannerMessageAction, hibSession);
				if (bs.getCrossListIdentifier() != null && !crosslistMap.containsKey(bs.getCrossListIdentifier())){
					crosslistMap.put(bs.getCrossListIdentifier(), bs);
				}
			}
			for(String crosslistId : crosslistMap.keySet()){
				bannerMessage.addBannerCrossListToMessage(crosslistMap.get(crosslistId), bannerMessageAction, hibSession);
			}
        }
	}
	public static void sendBannerMessage(List<BannerSection> bannerSections, BannerMessageAction bannerMessageAction, Session hibSession){
        if (bannerSections == null || bannerSections.isEmpty()){
        	return;
        }
        BannerSection bs = bannerSections.get(0);
        BannerSession bannerSession = BannerSession.findBannerSessionForSession(bs.getSession(), hibSession);
        if (BannerSession.shouldSendDataToBannerForSession(bs.getSession(), hibSession) || (bannerSession.isStoreDataForBanner().booleanValue() && BannerMessageAction.AUDIT.equals(bannerMessageAction))){
	 		Document document = DocumentHelper.createDocument();
	 		BannerMessage bm = new BannerMessage(bs.getSession(), bannerMessageAction, true, hibSession, document);
	 		addBannerSectionsToMessage(bannerSections, bannerMessageAction, hibSession, bm);
			writeOutMessage(document);
        }
	}
	public static void sendBannerMessage(BannerSection bannerSection, BannerMessageAction bannerMessageAction, Session hibSession){
        if (bannerSection == null){
    		Debug.info("no banner section ");
    		return;
        }
        BannerSession bannerSession = BannerSession.findBannerSessionForSession(bannerSection.getSession(), hibSession);
        if (BannerSession.shouldSendDataToBannerForSession(bannerSection.getSession(), hibSession) || (bannerSession.isStoreDataForBanner().booleanValue() && BannerMessageAction.AUDIT.equals(bannerMessageAction))){
			Document document = DocumentHelper.createDocument();
			BannerMessage bm = new BannerMessage(bannerSection.getSession(), bannerMessageAction, true, hibSession, document);
			bm.addBannerSectionToMessage(bannerSection, bannerMessageAction, hibSession);
			if (bannerSection.getCrossListIdentifier() != null) {
				bm.addBannerCrossListToMessage(bannerSection, bannerMessageAction, hibSession);
			}
			
			writeOutMessage(document);
        }
	}
	
	public static void writeOutMessage(Document document){
		QueueOut outQ = new QueueOut();
		if (document.getRootElement().element("SECTION") == null && document.getRootElement().element("CROSSLIST") == null){
			Debug.info("no message to send = " + document.asXML());
			return;
		}
		String asxml = document.asXML();
		if (asxml.length() > 2000) 
		  Debug.info("message = " + asxml.substring(1,2000) + " ....and more....");
		else
		  Debug.info("message = " + asxml);
		outQ.setXml(document);
		outQ.setStatus(QueueOut.STATUS_POSTED);
		outQ.setPostDate(new Date());
		Session newSession = QueueOutDAO.getInstance().createNewSession();
		Transaction trans = newSession.beginTransaction();
		newSession.save(outQ);
		trans.commit();
		newSession.close();
	}

//	public static void sendBannerMessage(List<BannerSection> bannerSections,
//			BannerMessageAction bannerMessageAction, Session hibSession, CourseOffering co) {
//	       if (bannerSections == null || bannerSections.isEmpty()){
//	        	return;
//	        }
//			Document document = DocumentHelper.createDocument();
//	        BannerSection bs = bannerSections.get(0);
//			BannerMessage bm = new BannerMessage(bs.getSession(), bannerMessageAction, true, hibSession, document);
//			HashMap<String, BannerSection> crosslistMap = new HashMap<String, BannerSection>();
//			for(Iterator<BannerSection> it = bannerSections.iterator(); it.hasNext();){
//				bs = it.next();
//				bm.addBannerSectionToMessage(bs, bannerMessageAction, hibSession, co);
//				if (bs.getCrossListIdentifier() != null && !crosslistMap.containsKey(bs.getCrossListIdentifier())){
//					crosslistMap.put(bs.getCrossListIdentifier(), bs);
//				}
//			}
//			for(String crosslistId : crosslistMap.keySet()){
//				bm.addBannerCrossListToMessage(crosslistMap.get(crosslistId), bannerMessageAction, hibSession);
//			}
//			
//			writeOutMessage(document);
//
//		
//	}
//
//	public static void sendBannerMessage(List<BannerSection> bannerSections, BannerMessageAction bannerMessageAction,
//			Session hibSession, CourseCreditUnitConfig credit) {
//	       if (bannerSections == null || bannerSections.isEmpty()){
//	        	return;
//	        }
//			Document document = DocumentHelper.createDocument();
//	        BannerSection bs = bannerSections.get(0);
//			BannerMessage bm = new BannerMessage(bs.getSession(), bannerMessageAction, true, hibSession, document);
//			HashMap<String, BannerSection> crosslistMap = new HashMap<String, BannerSection>();
//			for(Iterator<BannerSection> it = bannerSections.iterator(); it.hasNext();){
//				bs = it.next();
//				bm.addBannerSectionToMessage(bs, bannerMessageAction, hibSession, credit);
//				if (bs.getCrossListIdentifier() != null && !crosslistMap.containsKey(bs.getCrossListIdentifier())){
//					crosslistMap.put(bs.getCrossListIdentifier(), bs);
//				}
//			}
//			for(String crosslistId : crosslistMap.keySet()){
//				bm.addBannerCrossListToMessage(crosslistMap.get(crosslistId), bannerMessageAction, hibSession);
//			}
//			
//			writeOutMessage(document);
//
//		
//		
//	}

//	public static void sendBannerMessage(List<BannerSection> bannerSections,
//			BannerMessageAction bannerMessageAction, Session hibSession,
//			InstructionalOffering instructionalOffering) {
//	       if (bannerSections == null || bannerSections.isEmpty()){
//	        	return;
//	        }
//			Document document = DocumentHelper.createDocument();
//	        BannerSection bs = bannerSections.get(0);
//			BannerMessage bm = new BannerMessage(bs.getSession(), bannerMessageAction, true, hibSession, document);
//			HashMap<String, BannerSection> crosslistMap = new HashMap<String, BannerSection>();
//			for(Iterator<BannerSection> it = bannerSections.iterator(); it.hasNext();){
//				bs = it.next();
//				bm.addBannerSectionToMessage(bs, bannerMessageAction, hibSession, instructionalOffering);
//				if (bs.getCrossListIdentifier() != null && !crosslistMap.containsKey(bs.getCrossListIdentifier())){
//					crosslistMap.put(bs.getCrossListIdentifier(), bs);
//				}
//			}
//			for(String crosslistId : crosslistMap.keySet()){
//				bm.addBannerCrossListToMessage(crosslistMap.get(crosslistId), bannerMessageAction, hibSession);
//			}
//			
//			writeOutMessage(document);
//
//		
//	}
//
//	public static void sendBannerMessage(List<BannerSection> bannerSections,
//			BannerMessageAction bannerMessageAction, Session hibSession, Class_ clazz) {
//	       if (bannerSections == null || bannerSections.isEmpty()){
//	        	return;
//	        }
//			Document document = DocumentHelper.createDocument();
//	        BannerSection bs = bannerSections.get(0);
//			BannerMessage bm = new BannerMessage(bs.getSession(), bannerMessageAction, true, hibSession, document);
//			HashMap<String, BannerSection> crosslistMap = new HashMap<String, BannerSection>();
//			for(Iterator<BannerSection> it = bannerSections.iterator(); it.hasNext();){
//				bs = it.next();
//				bm.addBannerSectionToMessage(bs, bannerMessageAction, hibSession, clazz);
//				if (bs.getCrossListIdentifier() != null && !crosslistMap.containsKey(bs.getCrossListIdentifier())){
//					crosslistMap.put(bs.getCrossListIdentifier(), bs);
//				}
//			}
//			for(String crosslistId : crosslistMap.keySet()){
//				bm.addBannerCrossListToMessage(crosslistMap.get(crosslistId), bannerMessageAction, hibSession);
//			}
//			
//			writeOutMessage(document);
//
//		
//	}
//
//	public static void sendBannerMessage(List<BannerSection> bannerSections,
//			BannerMessageAction bannerMessageAction, Session hibSession,
//			InstrOfferingConfig instrOfferingConfig) {
//	       if (bannerSections == null || bannerSections.isEmpty()){
//	        	return;
//	        }
//			Document document = DocumentHelper.createDocument();
//	        BannerSection bs = bannerSections.get(0);
//			BannerMessage bm = new BannerMessage(bs.getSession(), bannerMessageAction, true, hibSession, document);
//			HashMap<String, BannerSection> crosslistMap = new HashMap<String, BannerSection>();
//			for(Iterator<BannerSection> it = bannerSections.iterator(); it.hasNext();){
//				bs = it.next();
//				bm.addBannerSectionToMessage(bs, bannerMessageAction, hibSession, instrOfferingConfig);
//				if (bs.getCrossListIdentifier() != null && !crosslistMap.containsKey(bs.getCrossListIdentifier())){
//					crosslistMap.put(bs.getCrossListIdentifier(), bs);
//				}
//			}
//			for(String crosslistId : crosslistMap.keySet()){
//				bm.addBannerCrossListToMessage(crosslistMap.get(crosslistId), bannerMessageAction, hibSession);
//			}
//			
//			writeOutMessage(document);
//		
//	}
	
}
