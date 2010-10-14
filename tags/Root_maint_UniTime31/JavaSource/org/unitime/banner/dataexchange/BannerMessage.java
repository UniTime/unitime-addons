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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.dom4j.Document;
import org.dom4j.Element;
import org.hibernate.Session;
import org.unitime.banner.interfaces.ExternalBannerCampusCodeElementHelperInterface;
import org.unitime.banner.interfaces.ExternalBannerSessionElementHelperInterface;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.util.BannerMessageIdGenerator;
import org.unitime.banner.util.DefaultExternalBannerCampusCodeElementHelper;
import org.unitime.banner.util.DefaultExternalBannerSessionElementHelper;
import org.unitime.banner.util.MeetingElement;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;


/**
 * @author says
 *
 */
public class BannerMessage {
	private Long messageId;
	private Element root;
	private Document document;
	private CourseOffering courseOffering;
	private CourseCreditUnitConfig courseCreditUnitConfig;
	private InstructionalOffering instructionalOffering;
	private Class_ clazz;
	
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("MM/dd/yyyy");
	private static ExternalBannerSessionElementHelperInterface externalSessionElementHelper;
	private static ExternalBannerCampusCodeElementHelperInterface externalCampusCodeElementHelper;

	private HashMap<Long, TreeMap<Date, Date>> datePatternMap;
	private static HashMap<Long, Long> sessionDefaultDatePatternMap;

	
	private void manageDatePatternMapCache(){
		if(datePatternMap == null){
			datePatternMap = new HashMap<Long, TreeMap<Date,Date>>();
		}
	}
	
	private static void manageDefaultDatePatternMapCache(){
		if(sessionDefaultDatePatternMap == null){
			sessionDefaultDatePatternMap = new HashMap<Long, Long>();
		}
	}

	
	public TreeMap<Date, Date> findDatesFor(DatePattern datePattern){
		manageDatePatternMapCache();
		if (!datePatternMap.containsKey(datePattern.getUniqueId())){
			datePatternMap.put(datePattern.getUniqueId(), MeetingElement.datePatternDates(datePattern));
		}
		return(datePatternMap.get(datePattern.getUniqueId()));
	}
	
	public TreeMap<Date, Date> findDatesFor(Long datePatternId){
		manageDatePatternMapCache();
		if (!datePatternMap.containsKey(datePatternId)){
			DatePattern dp = DatePatternDAO.getInstance().get(datePatternId);
			datePatternMap.put(datePatternId, MeetingElement.datePatternDates(dp));
		}
		return(datePatternMap.get(datePatternId));
	}
	
	
	public void updateDatesForDatePattern(DatePattern datePattern){
		manageDatePatternMapCache();
		if (!datePatternMap.containsKey(datePattern.getUniqueId())){
			datePatternMap.put(datePattern.getUniqueId(), MeetingElement.datePatternDates(datePattern));
		}	
	}

	public Long findDefaultDatePatternFor(org.unitime.timetable.model.Session acadSession){		
		manageDefaultDatePatternMapCache();
		if (!sessionDefaultDatePatternMap.containsKey(acadSession.getUniqueId())){
			DatePattern defaultDatePattern = (DatePattern)DatePatternDAO.getInstance().createNewSession().createQuery("from DatePattern dp where dp.session.uniqueId = :sessionId and dp.session.defaultDatePattern.uniqueId = dp.uniqueId").setLong("sessionId", acadSession.getUniqueId().longValue()).uniqueResult();
			sessionDefaultDatePatternMap.put(acadSession.getUniqueId(), defaultDatePattern.getUniqueId());
			updateDatesForDatePattern(defaultDatePattern);
		}
		return(sessionDefaultDatePatternMap.get(acadSession.getUniqueId()));
	}
	
	public static void updateDefaultDatePatternForSession(DatePattern datePattern){
		manageDefaultDatePatternMapCache();
		sessionDefaultDatePatternMap.put(datePattern.getSession().getUniqueId(), datePattern.getUniqueId());
	}

	
	
	/**
	 * 
	 */
	public BannerMessage(org.unitime.timetable.model.Session acadSession, BannerMessageAction action, boolean isTransaction, Session hibSession, Document document) {
		BannerMessageIdGenerator.setMessageId(this);
		this.document = document;
		beginMessage(acadSession, action, isTransaction, hibSession);
	}

	public enum BannerMessageAction {
		UPDATE,
		DELETE,
		AUDIT
	};
	public String bannerDateFormatString = "MM/DD/YYYY";
	
	private void beginMessage(org.unitime.timetable.model.Session acadSession, BannerMessageAction action, boolean isTransaction, Session hibSession){
		if (acadSession == null){
			return;
		}
		BannerSession bs = BannerSession.findBannerSessionForSession(acadSession.getUniqueId(), hibSession);
		
		root = document.addElement("SCHEDULE");
		root.addAttribute("PACKET_ID", messageId.toString());
		root.addAttribute("TERM_CODE", bs.getBannerTermCode());
		root.addAttribute("ORIGIN", "UniTime");
		root.addAttribute("ACTION", (action.equals(BannerMessageAction.AUDIT)?action.toString():BannerMessageAction.UPDATE.toString()));
		root.addAttribute("IS_TRANSACTION", (isTransaction?"Y":"N"));
		root.addAttribute("MODE", (action.equals(BannerMessageAction.AUDIT)?action.toString():BannerMessageAction.UPDATE.toString()));
		root.addAttribute("DATE_FORMAT", bannerDateFormatString);
		root.addAttribute("CLASS_END_DATE", sDateFormat.format(acadSession.getClassesEndDateTime()));

	}

	public void addBannerSectionToMessage(BannerSection bannerSection, BannerMessageAction action, Session hibSession){
		courseOffering = bannerSection.getBannerConfig().getBannerCourse().getCourseOffering(hibSession);
		if (courseOffering == null){
			return;
		}
		if (bannerSection.getClasses(hibSession).isEmpty()){
			clazz = null;
			instructionalOffering = courseOffering.getInstructionalOffering();
		} else {
			clazz =  bannerSection.getClasses(hibSession).iterator().next();
			instructionalOffering = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering();
			courseCreditUnitConfig = instructionalOffering.getCredit();			
		}
		courseCreditUnitConfig = instructionalOffering.getCredit();

		createSectionXmlForBanner(bannerSection, action, hibSession);
	}

	public void addBannerCrossListToMessage(BannerSection bannerSection, BannerMessageAction action, Session hibSession){
		createCrossListXmlForBanner(bannerSection, action, hibSession);
	}

	public String asXmlString(){
		
		return(root.asXML());
	}
	private void createSectionXmlForBanner(BannerSection bannerSection, BannerMessageAction action, Session hibSession){
		BannerSession bs = BannerSession.findBannerSessionForSession(bannerSection.getSession(), hibSession);
		if (bannerSection.getClasses(hibSession).isEmpty() && !BannerMessageAction.DELETE.equals(action)){
			return;
		}
		if (bs == null){
			Debug.info("null banner session");
		}
		if (bs != null && bs.isStoreDataForBanner() && (action.equals(BannerMessageAction.AUDIT) || bs.isSendDataToBanner())) {
			Element sectionElement = beginSectionElement(bannerSection, action, bs, hibSession);
			if (!action.equals(BannerMessageAction.DELETE)){
				createMeetingElementsXml(bannerSection, hibSession, sectionElement);
				createInstructorXml(bannerSection, hibSession, sectionElement);
			}
		}
	}
	
	private void createMeetingElementsXml(BannerSection bannerSection, Session hibSession, Element sectionElement) {
		Set<MeetingElement> initialMeetingElements = getInitialMeetingElements(bannerSection, hibSession);
		TreeSet<MeetingElement> meetingElements = mergeMeetings(initialMeetingElements);
		for(Iterator<MeetingElement> it = meetingElements.iterator(); it.hasNext();){
			MeetingElement me = it.next();
			me.addMeetingElements(sectionElement);
		}
	}
	
	private Set<MeetingElement> getInitialMeetingElements(BannerSection bannerSection, Session hibSession) {
		HashSet<MeetingElement> hs = new HashSet<MeetingElement>();
		for(Iterator<Class_> classIt = bannerSection.getClasses(hibSession).iterator(); classIt.hasNext();){
			Class_ c = classIt.next();
			hs.addAll(MeetingElement.createMeetingElementsFor(bannerSection, c, hibSession, this));
		}
		return(hs);
	}

	private void createInstructorXml(BannerSection bannerSection, Session hibSession, Element sectionElement) {
		TreeMap<DepartmentalInstructor, Integer> tm = bannerSection.findInstructorsWithPercents(hibSession);
		for (Iterator<DepartmentalInstructor> it = tm.keySet().iterator(); it.hasNext();){
			DepartmentalInstructor instructor = it.next();
			int pct = tm.get(instructor).intValue();
			Element instructorElement = sectionElement.addElement("INSTRUCTOR");
			String id = "";
			if (instructor.getExternalUniqueId().length() < 9){
				for (int i = 0; i < (9 - instructor.getExternalUniqueId().length()); i++){
					id += "0";
				}
			}
			id += instructor.getExternalUniqueId();
			instructorElement.addAttribute("ID", id);
			instructorElement.addAttribute("PERCENT", (new Integer(pct)).toString());
			
			if (instructor.getFirstName() != null && instructor.getFirstName().trim().length() > 0){
				instructorElement.addAttribute("FIRST_NAME", instructor.getFirstName().trim());
			} else {
				instructorElement.addAttribute("FIRST_NAME", "");				
			}
			if (instructor.getMiddleName() != null && instructor.getMiddleName().trim().length() > 0){
				instructorElement.addAttribute("MIDDLE_NAME", instructor.getMiddleName().trim());
			} else {
				instructorElement.addAttribute("MIDDLE_NAME", "");				
			}
			if (instructor.getLastName() != null && instructor.getLastName().trim().length() > 0){
				instructorElement.addAttribute("LAST_NAME", instructor.getLastName().trim());
			} else {
				instructorElement.addAttribute("LAST_NAME", "");
			}
		}
	}

	private ExternalBannerSessionElementHelperInterface getExternalSessionElementHelper(){
		if (externalSessionElementHelper == null){
            String className = ApplicationProperties.getProperty("tmtbl.banner.session.element.helper");
        	if (className != null && className.trim().length() > 0){
        		try {
					externalSessionElementHelper = (ExternalBannerSessionElementHelperInterface) (Class.forName(className).newInstance());
				} catch (InstantiationException e) {
					Debug.error("Failed to instantiate instance of: " + className + " using the default session element helper.");
					e.printStackTrace();
	        		externalSessionElementHelper = new DefaultExternalBannerSessionElementHelper();
				} catch (IllegalAccessException e) {
					Debug.error("Illegal Access Exception on: " + className + " using the default session element helper.");
					e.printStackTrace();
	        		externalSessionElementHelper = new DefaultExternalBannerSessionElementHelper();
				} catch (ClassNotFoundException e) {
					Debug.error("Failed to find class: " + className + " using the default session element helper.");
					e.printStackTrace();
	        		externalSessionElementHelper = new DefaultExternalBannerSessionElementHelper();
				}
        	} else {
        		externalSessionElementHelper = new DefaultExternalBannerSessionElementHelper();
        	}
		}
		return externalSessionElementHelper;

	}

	private ExternalBannerCampusCodeElementHelperInterface getExternalCampusCodeElementHelper(){
		if (externalCampusCodeElementHelper == null){
            String className = ApplicationProperties.getProperty("tmtbl.banner.campus.element.helper");
        	if (className != null && className.trim().length() > 0){
        		try {
        			externalCampusCodeElementHelper = (ExternalBannerCampusCodeElementHelperInterface) (Class.forName(className).newInstance());
				} catch (InstantiationException e) {
					Debug.error("Failed to instantiate instance of: " + className + " using the default campus code element helper.");
					e.printStackTrace();
					externalCampusCodeElementHelper = new DefaultExternalBannerCampusCodeElementHelper();
				} catch (IllegalAccessException e) {
					Debug.error("Illegal Access Exception on: " + className + " using the default campus code element helper.");
					e.printStackTrace();
					externalCampusCodeElementHelper = new DefaultExternalBannerCampusCodeElementHelper();
				} catch (ClassNotFoundException e) {
					Debug.error("Failed to find class: " + className + " using the default campus code element helper.");
					e.printStackTrace();
					externalCampusCodeElementHelper = new DefaultExternalBannerCampusCodeElementHelper();
				}
        	} else {
        		externalCampusCodeElementHelper = new DefaultExternalBannerCampusCodeElementHelper();
        	}
		}
		return externalCampusCodeElementHelper;

	}

	private Element beginSectionElement(BannerSection bannerSection, BannerMessageAction action, BannerSession bs, Session hibSession){
		if (bannerSection.getBannerConfig().getBannerCourse() == null || courseOffering == null){
			Debug.info("Banner section uid = " + bannerSection.getUniqueId().toString() + " does not have a corresponding course offering.");	
			return(null);
		}
		BannerMessageAction xmlAction = action;

		Element sectionElement = root.addElement("SECTION");
		Debug.info((bannerSection.getCrn()==null?"":bannerSection.getCrn().toString()) + " - " + courseOffering.getCourseNameWithTitle());
		sectionElement.addAttribute("ACTION", xmlAction.toString());
		sectionElement.addAttribute("EXTERNAL_ID", bannerSection.getUniqueId().toString());
		sectionElement.addAttribute("CRN", (bannerSection.getCrn()==null?"":bannerSection.getCrn().toString()));
		sectionElement.addAttribute("SUBJ_CODE", courseOffering.getSubjectAreaAbbv());
		sectionElement.addAttribute("CRSE_NUMB", courseOffering.getCourseNbr().substring(0, 5));
		if (!BannerMessageAction.DELETE.equals(xmlAction)) {
			sectionElement.addAttribute("ID", bannerSection.getSectionIndex());
			sectionElement.addAttribute("TITLE", courseOffering.getTitle());
			getExternalSessionElementHelper().addSessionElementIfNeeded(sectionElement, bannerSection);
			getExternalCampusCodeElementHelper().addCampusCodeElement(sectionElement, bannerSection, bs, clazz);
			sectionElement.addAttribute("SCHD_CODE", clazz.getSchedulingSubpart().getItype().getSis_ref());
			sectionElement.addAttribute("GRADABLE", (bannerSection.getBannerConfig().getGradableItype()==null?"N":(clazz.getSchedulingSubpart().getItype().getItype().equals(bannerSection.getBannerConfig().getGradableItype().getItype())?"Y":"N")));
			sectionElement.addAttribute("MAX_ENRL", ((new Integer(bannerSection.calculateMaxEnrl(hibSession))).toString()));

			sectionElement.addAttribute("CREDIT_HRS", "");
			if (bannerSection.getBannerConfig().getGradableItype() != null) {
				if (clazz.getSchedulingSubpart().getItype().getItype().equals(bannerSection.getBannerConfig().getGradableItype().getItype())){
					if (bannerSection.getOverrideCourseCredit() != null){
						sectionElement.addAttribute("CREDIT_HRS", bannerSection.getOverrideCourseCredit().toString());
					} else {
						if (courseCreditUnitConfig != null && courseCreditUnitConfig instanceof FixedCreditUnitConfig) {
							FixedCreditUnitConfig fixed = (FixedCreditUnitConfig) courseCreditUnitConfig;
							sectionElement.addAttribute("CREDIT_HRS", fixed.getFixedUnits().toString());
						}
					}
				} else {
					sectionElement.addAttribute("CREDIT_HRS", "0");				
				}
			}
			
			if (bannerSection.getLinkIdentifier() != null && bannerSection.getLinkIdentifier().trim().length() > 0 
					&& bannerSection.getLinkConnector() != null && bannerSection.getLinkConnector().trim().length() > 0){
				sectionElement.addAttribute("LINK_IDENT", bannerSection.getLinkIdentifier().trim());
				sectionElement.addAttribute("LINK_CONN", bannerSection.getLinkConnector().trim());
			} else {
				sectionElement.addAttribute("LINK_IDENT", "");
				sectionElement.addAttribute("LINK_CONN", "");				
			}
			OfferingConsentType oct = bannerSection.effectiveConsentType();
			if (oct != null){
				sectionElement.addAttribute("APPROVAL", oct.getReference());
			} else {
				sectionElement.addAttribute("APPROVAL", "");
			}
			TreeSet<String> noteSet = new TreeSet<String>();
			if (courseOffering.getScheduleBookNote() != null && courseOffering.getScheduleBookNote().trim().length() > 0){
				noteSet.add(courseOffering.getScheduleBookNote());
			}
			for(Class_ cls : bannerSection.getClasses(hibSession, clazz)){
				if (cls.getSchedulePrintNote() != null && cls.getSchedulePrintNote().trim().length() > 0){
					noteSet.add(cls.getSchedulePrintNote().trim());
				}
			}
			StringBuilder sb = new StringBuilder();
			if (!noteSet.isEmpty()){
				boolean first = true;
				for(String note : noteSet){
					if (!first){
						sb.append(", ");
					} else {
						first = false;
					}
					sb.append(note);
				}
				sectionElement.addAttribute("TEXT_NARRATIVE", sb.toString());
			} else {
				sectionElement.addAttribute("TEXT_NARRATIVE", "");				
			}
			sectionElement.addAttribute("WEB_AVAIL", clazz.isDisplayInScheduleBook().booleanValue()?"Y":"N");
			sectionElement.addAttribute("PRINT_IND", clazz.isDisplayInScheduleBook().booleanValue()?"Y":"N");
			sectionElement.addAttribute("USERID", "UniTime");			
		}
		return(sectionElement);
	}
	
	@SuppressWarnings("unchecked")
	private TreeSet<Integer> getCrossListedCrns(BannerSection bannerSection, Session hibSession){
		TreeSet<Integer> ts = new TreeSet<Integer>();
		Session querySession = hibSession;
		if (querySession == null){
			querySession = Class_DAO.getInstance().getSession();
		}
		if (bannerSection.isCrossListedSection(querySession)){
			for (Iterator bscIt = bannerSection.getBannerSectionToClasses().iterator(); bscIt.hasNext();){
				BannerSectionToClass bsc = (BannerSectionToClass) bscIt.next();
				for(Iterator crnIt = querySession.createQuery("select distinct bsc.bannerSection.crn from BannerSectionToClass bsc where bsc.classId = :classId").setLong("classId", bsc.getClassId().longValue()).iterate(); crnIt.hasNext();) {
					Integer crn = (Integer) crnIt.next();
					if (crn != null) {
						ts.add(crn);
					}
				}
			}
		}
		return(ts);
	}
	private void createCrossListXmlForBanner(BannerSection bannerSection, BannerMessageAction action, Session hibSession){
		if (bannerSection.isCrossListedSection(hibSession)){
			BannerSession bs = BannerSession.findBannerSessionForSession(bannerSection.getSession(), hibSession);
			if (bs == null){
				Debug.info("null banner session");
			}
			BannerMessageAction xmlAction = action;
			if (bs != null && bs.isStoreDataForBanner() && (action.equals(BannerMessageAction.AUDIT) || bs.isSendDataToBanner())) {

				TreeSet<Integer> ts = getCrossListedCrns(bannerSection, hibSession);
				if ((ts.isEmpty() || ts.size() == 1) && !BannerMessageAction.AUDIT.equals(action)){
					xmlAction = BannerMessageAction.DELETE;
				}
				Element crossListElement = root.addElement("CROSSLIST");
				crossListElement.addAttribute("ACTION", action.toString());
				crossListElement.addAttribute("GROUP", bannerSection.getCrossListIdentifier());
				crossListElement.addAttribute("EXTERNAL_ID", bannerSection.getCrossListIdentifier());
				if (!BannerMessageAction.DELETE.equals(xmlAction)){
					crossListElement.addAttribute("MAX_ENRL",(new Integer(bannerSection.maxEnrollBasedOnClasses(hibSession))).toString());
					for(Iterator<Integer> crnIt = ts.iterator(); crnIt.hasNext();){
						Element memberElement = crossListElement.addElement("MEMBER");
						memberElement.addAttribute("CRN", crnIt.next().toString());
					}
				}
			}
		}
	}
	
	private TreeSet<MeetingElement> mergeMeetings(Set<MeetingElement> meetings){
		TreeSet<MeetingElement> mergedMeetings = new TreeSet<MeetingElement>();
		if(meetings.size() == 0){
			mergedMeetings.addAll(meetings);
		} else {
			HashSet<MeetingElement> unmergedMeetings = new HashSet<MeetingElement>();
			unmergedMeetings.addAll(meetings);
			HashSet<MeetingElement> iterateSet = new HashSet<MeetingElement>();
			iterateSet.addAll(meetings);
			for(Iterator<MeetingElement> meetingIt = meetings.iterator(); meetingIt.hasNext();){
				MeetingElement me = meetingIt.next();
				iterateSet.remove(me);
				if (unmergedMeetings.contains(me)){
					for(Iterator<MeetingElement> checkMeetingIt = iterateSet.iterator(); checkMeetingIt.hasNext();){
						MeetingElement checkMeeting = checkMeetingIt.next();
						if (me.canBeMerged(checkMeeting)){
							me.merge(checkMeeting);
							unmergedMeetings.remove(checkMeeting);
						}
					}
					mergedMeetings.add(me);
				}
			}
		}
		return(mergedMeetings);
	}
	public Long getMessageId() {
		return messageId;
	}
	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public Document getDocument() {
		return document;
	}

	public void setDocument(Document document) {
		this.document = document;
	}

}
