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
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSectionToClass;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.ColleagueSuffixDef;
import org.unitime.colleague.util.ColleagueMessageIdGenerator;
import org.unitime.colleague.util.MeetingElement;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DatePattern;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.VariableFixedCreditUnitConfig;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.DatePatternDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;


/**
 * @author says
 *
 */
public class ColleagueMessage {
	private Long messageId;
	private Element root;
	private Document document;
	
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("MM/dd/yyyy");

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
			Session hibSession = DatePatternDAO.getInstance().createNewSession();
			DatePattern defaultDatePattern = (DatePattern)hibSession.createQuery("from DatePattern dp where dp.session.uniqueId = :sessionId and dp.session.defaultDatePattern.uniqueId = dp.uniqueId").setLong("sessionId", acadSession.getUniqueId().longValue()).uniqueResult();
			sessionDefaultDatePatternMap.put(acadSession.getUniqueId(), defaultDatePattern.getUniqueId());
			updateDatesForDatePattern(defaultDatePattern);
			hibSession.close();
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
	public ColleagueMessage(org.unitime.timetable.model.Session acadSession, MessageAction action, boolean isTransaction, Session hibSession, Document document) {
		ColleagueMessageIdGenerator.setMessageId(this);
		this.document = document;
		beginMessage(acadSession, action, isTransaction, hibSession);
	}

	public enum MessageAction {
		UPDATE,
		DELETE,
		AUDIT
	};
	public String dateFormatString = "MM/DD/YYYY";
	
	private void beginMessage(org.unitime.timetable.model.Session acadSession, MessageAction action, boolean isTransaction, Session hibSession){
		if (acadSession == null){
			return;
		}
		ColleagueSession cSess = ColleagueSession.findColleagueSessionForSession(acadSession.getUniqueId(), hibSession);
		
		root = document.addElement("SCHEDULE");
		root.addAttribute("PACKET_ID", messageId.toString());
		root.addAttribute("TERM_CODE", cSess.getColleagueTermCode());
		root.addAttribute("ORIGIN", "UniTime");
		root.addAttribute("ACTION", (action.equals(MessageAction.AUDIT)?action.toString():MessageAction.UPDATE.toString()));
		root.addAttribute("IS_TRANSACTION", (isTransaction?"Y":"N"));
		root.addAttribute("DATE_FORMAT", dateFormatString);

	}

	public void addSectionToMessage(ColleagueSection section, MessageAction action, Session hibSession){
		Class_ clazz;
		CourseOffering courseOffering = section.getCourseOffering(hibSession);

		
		if (!MessageAction.DELETE.equals(action) && courseOffering == null){
			return;
		}
		if (section.getClasses(hibSession).isEmpty()){
			clazz = null;
		} else {
			clazz =  section.getClasses(hibSession).iterator().next();
		}

		createSectionXml(section, clazz, courseOffering, action, hibSession);
	}

	public String asXmlString(){
		
		return(root.asXML());
	}
	private void createSectionXml(ColleagueSection section, Class_ clazz, CourseOffering courseOffering, MessageAction action, Session hibSession){
		ColleagueSession cSession = ColleagueSession.findColleagueSessionForSession(section.getSession(), hibSession);
		if (section.getClasses(hibSession).isEmpty() && !MessageAction.DELETE.equals(action)){
			return;
		}
		if (cSession == null){
			Debug.info("null colleague session");
		}
		if (cSession != null && cSession.isStoreDataForColleague() && (action.equals(MessageAction.AUDIT) || cSession.isSendDataToColleague())) {
			Element sectionElement = beginSectionElement(section, clazz, courseOffering, action, cSession, hibSession);
			if (!action.equals(MessageAction.DELETE)){
				createMeetingElementsXml(section, hibSession, sectionElement);
				createInstructorXml(section, hibSession, sectionElement);
				createCrossListXml(section, clazz, courseOffering, hibSession, sectionElement);
				createRestrictionXml(section, hibSession, sectionElement);
			}
		}
	}
	
	private void createMeetingElementsXml(ColleagueSection colleagueSection, Session hibSession, Element sectionElement) {
		Set<MeetingElement> initialMeetingElements = getInitialMeetingElements(colleagueSection, hibSession);
		TreeSet<MeetingElement> meetingElements = mergeMeetings(initialMeetingElements);
		for(MeetingElement me : meetingElements){
			me.addMeetingElements(sectionElement);
		}
	}
	
	private Set<MeetingElement> getInitialMeetingElements(ColleagueSection colleagueSection, Session hibSession) {
		HashSet<MeetingElement> hs = new HashSet<MeetingElement>();
		for(Class_ c : colleagueSection.getClasses(hibSession)){
			hs.addAll(MeetingElement.createMeetingElementsFor(colleagueSection, c, hibSession, this));
		}
		return(hs);
	}

	private void createInstructorXml(ColleagueSection section, Session hibSession, Element sectionElement) {
		TreeMap<DepartmentalInstructor, Integer> tm = section.findInstructorsWithPercents(hibSession);
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

	private void createRestrictionXml(ColleagueSection section, Session hibSession, Element sectionElement) {
		if (section.getRestrictions() != null && !section.getRestrictions().isEmpty()){
			for (ColleagueRestriction cr : section.getRestrictions()){
				Element restrictionElement = sectionElement.addElement("RESTRICTION");
				restrictionElement.addAttribute("COLLEAGUE_RESTRICTION_ID", cr.getCode());	
			}
		}
	}

	private void createCrossListXml(ColleagueSection section, Class_ clazz, CourseOffering courseOffering, Session hibSession, Element sectionElement) {
		if (courseOffering.getInstructionalOffering().getCourseOfferings().size() > 1){			
			Element crosslistElement = sectionElement.addElement("CROSSLIST");
			ColleagueSection ctrlSection = ColleagueSection.findColleagueSectionForClassAndCourseOffering(clazz, courseOffering.getInstructionalOffering().getControllingCourseOffering(), hibSession);
			crosslistElement.addAttribute("PRIMARY_COLLEAGUE_SYNONYM", (ctrlSection.getColleagueId() == null ? "" : ctrlSection.getColleagueId().toString()));	
			crosslistElement.addAttribute("PRIMARY_UNITIME_UID", ctrlSection.getUniqueId().toString());	
			crosslistElement.addAttribute("PRIMARY_CRS_SUBJECT", courseOffering.getInstructionalOffering().getControllingCourseOffering().getSubjectAreaAbbv());	
			crosslistElement.addAttribute("PRIMARY_CRS_NUMBER", ctrlSection.getColleagueCourseNumber());	
			crosslistElement.addAttribute("PRIMARY_SECTION_ID", ctrlSection.getSectionIndex());	
			
		}
	}


	private void addCampusCodeElement(Element sectionElement,
			ColleagueSection colleagueSection, ColleagueSession colleagueSession, Class_ clazz) {

		sectionElement.addAttribute("CAMP_CODE", colleagueSection.getCampusCode(colleagueSession, clazz));

	}


	private Element beginSectionElement(ColleagueSection section, Class_ clazz, CourseOffering courseOffering, MessageAction action, ColleagueSession cSession, Session hibSession){
		if (section.isDeleted() && !action.equals(MessageAction.DELETE)){
			action = MessageAction.DELETE;
		}

		if (!action.equals(MessageAction.DELETE) && (section.getCourseOffering(hibSession) == null || courseOffering == null)){
			Debug.info("Colleague section uid = " + section.getUniqueId().toString() + " does not have a corresponding course offering.");	
			return(null);
		}
		MessageAction xmlAction = action;

		SubjectArea subjectArea = null;
		if (courseOffering == null) { 
			subjectArea = SubjectAreaDAO.getInstance().get(section.getSubjectAreaId(), hibSession);
		} else {
			subjectArea = courseOffering.getSubjectArea();
		}
		Element sectionElement = root.addElement("SECTION");
		Debug.info((section.getColleagueId()==null?"":section.getColleagueId().toString()) + " - " + subjectArea.getSubjectAreaAbbreviation() + " " + section.getColleagueCourseNumber());
		sectionElement.addAttribute("ACTION", xmlAction.toString());
		sectionElement.addAttribute("EXTERNAL_ID", section.getUniqueId().toString());
		sectionElement.addAttribute("COLLEAGUE_SYNONYM", (section.getColleagueId()==null?"":section.getColleagueId().toString()));
		sectionElement.addAttribute("SUBJ_CODE", subjectArea.getSubjectAreaAbbreviation());
		sectionElement.addAttribute("CRSE_NUMB", section.getColleagueCourseNumber());
		if (section.getSectionIndex() != null && !section.getSectionIndex().trim().isEmpty()){
			sectionElement.addAttribute("ID", section.getSectionIndex());			
		} else {
			sectionElement.addAttribute("ID", "***");
		}
		if (!MessageAction.DELETE.equals(xmlAction)) {
			
			ColleagueSession cSess = ColleagueSession.findColleagueSessionForSession(section.getSession().getUniqueId(), hibSession);

			sectionElement.addAttribute("TITLE", courseOffering.getTitle());
			sectionElement.addAttribute("DEPT_CODE", courseOffering.getSubjectArea().getDepartment().getDeptCode());
			ColleagueSuffixDef csd = ColleagueSuffixDef.findColleagueSuffixDefForTermCode(clazz.getSchedulingSubpart().getItype(), courseOffering, cSess.getColleagueTermCode(), hibSession);
			sectionElement.addAttribute("LOCATION_CODE", (csd.getCampusCode() == null?cSess.getColleagueCampus():csd.getCampusCode()));
			addCampusCodeElement(sectionElement, section, cSession, clazz);
			sectionElement.addAttribute("INSTRUCTIONAL_METHOD", clazz.getSchedulingSubpart().getItype().getSis_ref());
			sectionElement.addAttribute("MAX_ENRL", ((new Integer(section.calculateMaxEnrl(hibSession))).toString()));

			CourseCreditUnitConfig courseCreditUnitConfig = courseOffering.getCredit();
			if (courseCreditUnitConfig != null) {
				if (courseCreditUnitConfig instanceof FixedCreditUnitConfig) {
					FixedCreditUnitConfig fixed = (FixedCreditUnitConfig) courseCreditUnitConfig;
					sectionElement.addAttribute("MIN_CREDIT_HOURS", fixed.getFixedUnits().toString());
					sectionElement.addAttribute("MAX_CREDIT_HOURS", "");
				} else if (courseCreditUnitConfig instanceof VariableFixedCreditUnitConfig){
					VariableFixedCreditUnitConfig variable = (VariableFixedCreditUnitConfig) courseCreditUnitConfig;
					sectionElement.addAttribute("MIN_CREDIT_HOURS", variable.getMinUnits().toString());
					sectionElement.addAttribute("MAX_CREDIT_HOURS", variable.getMaxUnits().toString());					
				} else {
					sectionElement.addAttribute("MIN_CREDIT_HOURS", "");				
					sectionElement.addAttribute("MAX_CREDIT_HOURS", "");									
				}
			} else {
				sectionElement.addAttribute("MIN_CREDIT_HOURS", "");				
				sectionElement.addAttribute("MAX_CREDIT_HOURS", "");				
			}

			TreeSet<String> noteSet = new TreeSet<String>();
			if (courseOffering.getScheduleBookNote() != null && courseOffering.getScheduleBookNote().trim().length() > 0){
				noteSet.add(courseOffering.getScheduleBookNote());
			}
			for(Class_ cls : section.getClasses(hibSession, clazz)){
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
			OfferingConsentType oct = courseOffering.getConsentType();
			if (oct != null){
				sectionElement.addAttribute("APPROVAL", oct.getReference());
			} else {
				sectionElement.addAttribute("APPROVAL", "");
			}

			sectionElement.addAttribute("WEB_AVAIL", clazz.isEnabledForStudentScheduling().booleanValue()?"Y":"N");
			sectionElement.addAttribute("PRINT_IND", clazz.isEnabledForStudentScheduling().booleanValue()?"Y":"N");
			sectionElement.addAttribute("UNITIME_UID", "UniTime");			
			
			HashSet<DatePattern> datePatterns = new HashSet<DatePattern>();
			for(Class_ cls : section.getClasses(hibSession)){
				datePatterns.add(cls.effectiveDatePattern());
			}
			Date startDate = null;
			Date endDate = null;
			for (DatePattern dp : datePatterns) {
				if (startDate == null || dp.getStartDate().before(startDate)) {
				    startDate = dp.getStartDate();
				}
				if (endDate == null|| dp.getEndDate().after(endDate)) {
					endDate = dp.getEndDate();
				}
			}
			sectionElement.addAttribute("SECT_START_DATE", sDateFormat.format(startDate));
			sectionElement.addAttribute("SECT_END_DATE", sDateFormat.format(endDate));
			sectionElement.addAttribute("SOFF_START_DATE", sDateFormat.format(startDate));
			sectionElement.addAttribute("SOFF_END_DATE", sDateFormat.format(endDate));
		} 
		return(sectionElement);
	}
	
	@SuppressWarnings("unchecked")
	private TreeSet<Integer> getCrossListedCrns(ColleagueSection colleagueSection, Session hibSession){
		TreeSet<Integer> ts = new TreeSet<Integer>();
		Session querySession = hibSession;
		if (querySession == null){
			querySession = Class_DAO.getInstance().getSession();
		}
		if (colleagueSection.isCrossListedSection(querySession)){
			for (Iterator bscIt = colleagueSection.getColleagueSectionToClasses().iterator(); bscIt.hasNext();){
				ColleagueSectionToClass bsc = (ColleagueSectionToClass) bscIt.next();
				for(Iterator crnIt = querySession.createQuery("select distinct bsc.colleagueSection.crn from ColleagueSectionToClass bsc where bsc.classId = :classId").setLong("classId", bsc.getClassId().longValue()).iterate(); crnIt.hasNext();) {
					Integer crn = (Integer) crnIt.next();
					if (crn != null) {
						ts.add(crn);
					}
				}
			}
		}
		return(ts);
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
