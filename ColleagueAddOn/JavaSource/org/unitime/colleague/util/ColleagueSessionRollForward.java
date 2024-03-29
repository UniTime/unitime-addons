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

package org.unitime.colleague.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.unitime.colleague.form.RollForwardColleagueSessionForm;
import org.unitime.colleague.interfaces.ExternalSessionRollForwardCustomizationInterface;
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSectionToClass;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.ColleagueSuffixDef;
import org.unitime.colleague.model.dao.ColleagueRestrictionDAO;
import org.unitime.colleague.model.dao.ColleagueSectionDAO;
import org.unitime.colleague.model.dao.ColleagueSessionDAO;
import org.unitime.colleague.model.dao.ColleagueSuffixDefDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.RollForwardSessionAction.RollForwardErrors;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.util.SessionRollForward;


/**
 * @author says
 *
 */
public class ColleagueSessionRollForward extends SessionRollForward {
	protected static final ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);
	private static ExternalSessionRollForwardCustomizationInterface externalSessionRollForwardCustomization;

	/**
	 * 
	 */
	public ColleagueSessionRollForward(Log log) {
		super(log);
	}
	
	public void rollColleagueSessionDataForward(RollForwardErrors errors, RollForwardColleagueSessionForm rollForwardColleagueSessionForm){
		Session toSession = Session.getSessionById(rollForwardColleagueSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardColleagueSessionForm.getSessionToRollColleagueDataForwardFrom());
		
		try {
			rollForwardColleagueSessionData(toSession, fromSession);
			rollForwardColleagueSuffixDefData(toSession, fromSession);
			rollForwardColleagueRestrictionData(toSession, fromSession);
			// Do not roll foward course data.  This is generated whenever the session is turned on for data sharing with Colleague.
			//rollForwardColleagueCourseData(toSession);
			//updateClassSuffixes(toSession);
		} catch (Exception e) {
			String type = CMSG.rollForwardColleagueSessionData();
			String msg = CMSG.errorFailedToRollForwardColleagueSessionData();
			iLog.error(msg, e);
			errors.addFieldError("rollForward", MSG.errorRollingForward(type, fromSession.getLabel(), toSession.getLabel(), msg));
		}
		try {
			ExternalSessionRollForwardCustomizationInterface customRollForwardAction = getExternalSessionRollForwardCustomization();
			if (customRollForwardAction != null){
				customRollForwardAction.doCustomRollFowardAction(fromSession, toSession);
			}
		} catch (Exception e) {
			String type = CMSG.rollForwardColleagueSessionData();
			String msg = CMSG.errorFailedToRollForwardCustomData(e.getMessage());
			iLog.error(msg, e);
			errors.addFieldError("rollForward", MSG.errorRollingForward(type, fromSession.getLabel(), toSession.getLabel(), msg));
		}
		
	}

	@SuppressWarnings("unused")
	private void rollForwardColleagueCourseData(Session toSession) {
		Long toSessionId = toSession.getUniqueId();
		ColleagueSession cs = ColleagueSession.findColleagueSessionForSession(toSessionId, null);
		if (cs.isStoreDataForColleague().booleanValue()){
			TreeSet<SubjectArea> subjectAreas =  new TreeSet<SubjectArea>();
			for(Iterator saIt = toSession.getSubjectAreas().iterator(); saIt.hasNext();){
				subjectAreas.add((SubjectArea) saIt.next());
			}
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct cs ")
			  .append(" from ColleagueSection cs, CourseOffering co")
			  .append(" where co.instructionalOffering.session.uniqueId = :sessionId")
			  .append("   and co.subjectArea.uniqueId = :subjectId")
			  .append("   and cs.courseOfferingId = co.uniqueIdRolledForwardFrom")
			  .append("   and cs.deleted = false")
			  .append("   and cs.uniqueId not in ( select cs2.uniqueIdRolledForwardFrom from ColleagueSection cs2 where cs2.uniqueIdRolledForwardFrom = cs.uniqueId )")
			  ;
			String queryString = sb.toString();
			org.hibernate.Session hibSession = ColleagueSectionDAO.getInstance().getSession();
			for(SubjectArea sa : subjectAreas){
				iLog.info("Rolling Colleague Data for Subject Area:  " + sa.getSubjectAreaAbbreviation());
				Iterator<ColleagueSection> fromColleagueSectionIt = hibSession.createQuery(queryString, ColleagueSection.class)
									.setParameter("sessionId", toSessionId.longValue())
									.setParameter("subjectId", sa.getUniqueId().longValue())
									.list()
									.iterator();
				while (fromColleagueSectionIt.hasNext()){
					ColleagueSection fromCs = fromColleagueSectionIt.next();
					ColleagueSection toCs = new ColleagueSection();
					//
					// Do not roll forward the section index.  This should be generated on the first send to Colleague to prevent
					//       creating section indexes that are never used.
					//
					// toCs.setSectionIndex(fromCs.getSectionIndex());
					toCs.setDeleted(Boolean.valueOf(false));
					toCs.setColleagueCourseNumber(fromCs.getColleagueCourseNumber());
					CourseOffering fromCourseOffering = CourseOffering.findByUniqueId(fromCs.getCourseOfferingId());
					CourseOffering toCourseOffering = CourseOffering.findByIdRolledForwardFrom(toSession.getUniqueId(), fromCourseOffering.getUniqueId());
					toCs.setCourseOfferingId(toCourseOffering.getUniqueId());
					toCs.setCourseOffering(toCourseOffering);
					toCs.setSubjectAreaId(toCourseOffering.getSubjectArea().getUniqueId());
					toCs.setUniqueIdRolledForwardFrom(fromCs.getUniqueId());
					toCs.setSession(toSession);			
					
					for (ColleagueSectionToClass fromBsc : fromCs.getColleagueSectionToClasses()){
						Class_ toClass = Class_.findByIdRolledForwardFrom(toSessionId, fromBsc.getClassId());
						if (toClass != null) {
							ColleagueSectionToClass toBsc = new ColleagueSectionToClass();
							toBsc.setColleagueSection(toCs);
							toCs.addToColleagueSectionToClasses(toBsc);
							toBsc.setClassId(toClass.getUniqueId());
						}
					}
					if (toCs.getColleagueSectionToClasses() != null){
						toCs.setSession(toSession);
					}
					
					for (ColleagueRestriction fromRestriction : fromCs.getRestrictions()){
						ColleagueRestriction toRestriction = ColleagueRestriction.findColleagueRestrictionTermCode(fromRestriction.getCode(), cs.getColleagueTermCode(), ColleagueRestrictionDAO.getInstance().getSession());
						toCs.addToRestrictions(toRestriction);
					}
				
					hibSession.persist(toCs);
					hibSession.flush();
					hibSession.evict(toCs);
					hibSession.evict(fromCs);
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void updateClassSuffixes(Session toSession) {
		Long toSessionId = toSession.getUniqueId();
		ColleagueSession bs = ColleagueSession.findColleagueSessionForSession(toSessionId, null);
		if (bs.isStoreDataForColleague().booleanValue()){
			TreeSet<SubjectArea> subjectAreas =  new TreeSet<SubjectArea>();
			for(Iterator saIt = toSession.getSubjectAreas().iterator(); saIt.hasNext();){
				subjectAreas.add((SubjectArea) saIt.next());
			}

			String queryString = "select distinct cs from ColleagueSection cs where cs.subjectAreaId = :subjectId";
			StringBuilder sb2 = new StringBuilder();
			sb2.append("select distinct c from Class_ c, CourseOffering co, ColleagueSectionToClass cstc")
			  .append(" where co.instructionalOffering.session.uniqueId = :sessionId")
			  .append(" and co.subjectArea.uniqueId = :subjectId")
			  .append(" and cstc.colleagueSection.courseOfferingId = co.uniqueId")
			  .append(" and c.uniqueId = cstc.classId");
			String queryString2 = sb2.toString();
			org.hibernate.Session hibSession = ColleagueSectionDAO.getInstance().getSession();
			for(SubjectArea sa : subjectAreas){
				iLog.info("Updating Class External Ids for Subject Area:  " + sa.getSubjectAreaAbbreviation());
				Iterator<ColleagueSection> colleagueSectionIt = hibSession.createQuery(queryString, ColleagueSection.class)
									.setParameter("subjectId", sa.getUniqueId().longValue())
									.list()
									.iterator();
				List<Class_> classes = hibSession.createQuery(queryString2, Class_.class)
									.setParameter("sessionId", toSessionId.longValue())
									.setParameter("subjectId", sa.getUniqueId().longValue())
									.setCacheable(true)
									.list();
				while (colleagueSectionIt.hasNext()){
					ColleagueSection colleagueSection = colleagueSectionIt.next();
					colleagueSection.updateClassSuffixForClassesIfNecessaryRefreshClasses(hibSession, false);
					
					hibSession.evict(colleagueSection);
				}
				hibSession.flush();
				for(Iterator<Class_> cIt = classes.iterator(); cIt.hasNext();){
					hibSession.evict(cIt.next());
				}
			}
		}
	}

	private void rollForwardColleagueSuffixDefData(Session toSession,
			Session fromSession) {
		ColleagueSession toCs = ColleagueSession.findColleagueSessionForSession(toSession, null);
		ColleagueSession fromCs = ColleagueSession.findColleagueSessionForSession(fromSession, null);
		List<ColleagueSuffixDef> fromSuffixes = ColleagueSuffixDefDAO.getInstance().getSession()
				.createQuery("from ColleagueSuffixDef csd where csd.termCode = :termCode", ColleagueSuffixDef.class)
				.setParameter("termCode", fromCs.getColleagueTermCode())
				.list();
		for (ColleagueSuffixDef fromDef : fromSuffixes){
			SubjectArea fromSa = null;
			SubjectArea toSa = null;
			if (fromDef.getSubjectAreaId() != null){
				fromSa = SubjectAreaDAO.getInstance().get(fromDef.getSubjectAreaId());
				toSa = SubjectArea.findByAbbv(toSession.getUniqueId(), fromSa.getSubjectAreaAbbreviation());
			}
			if (fromSa != null && toSa == null){
				if (toSa == null) {
					iLog.warn("Failed to roll forward Colleague Suffix Def with uniqueId:  " + fromDef.getUniqueId().toString() + " due to missing subject area:  " + fromSa.getSubjectAreaAbbreviation());
					continue;
				}
			}
			ColleagueSuffixDef toDef = ColleagueSuffixDef.findColleagueSuffixDefForTermCodeItypeSuffix(toCs.getColleagueTermCode(), (toSa == null?null:toSa.getUniqueId()), fromDef.getItypeId(), fromDef.getCourseSuffix());
			if (toDef == null) {
				toDef = fromDef.clone();
				if (fromDef.getSubjectAreaId() != null ){				
					toDef.setSubjectAreaId(toSa.getUniqueId());
				}
				toDef.setTermCode(toCs.getColleagueTermCode());
				ColleagueSuffixDefDAO.getInstance().getSession().persist(toDef);
			}
		}
		ColleagueSuffixDefDAO.getInstance().getSession().flush();
	}

	private void rollForwardColleagueRestrictionData(Session toSession,
			Session fromSession) {
		ColleagueSession toCs = ColleagueSession.findColleagueSessionForSession(toSession, null);
		ColleagueSession fromCs = ColleagueSession.findColleagueSessionForSession(fromSession, null);
		Collection<ColleagueRestriction> fromRestrictions = ColleagueRestriction.getAllColleagueRestrictionsForTerm(fromCs.getColleagueTermCode());
		for (ColleagueRestriction fromRestriction : fromRestrictions){
			ColleagueRestriction toRestriction = ColleagueRestriction.findColleagueRestrictionTermCode(fromRestriction.getCode(), toCs.getColleagueTermCode(), ColleagueRestrictionDAO.getInstance().getSession());
			if (toRestriction == null) {
				toRestriction = fromRestriction.clone();
				toRestriction.setTermCode(toCs.getColleagueTermCode());
				ColleagueRestrictionDAO.getInstance().getSession().persist(toRestriction);
			}
		}
		ColleagueRestrictionDAO.getInstance().getSession().flush();
	}

	private void rollForwardColleagueSessionData(Session toSession,
			Session fromSession) {
		ColleagueSession toCs = ColleagueSession.findColleagueSessionForSession(toSession, null);
		ColleagueSession fromCs = ColleagueSession.findColleagueSessionForSession(fromSession, null);
		if (toCs == null){
			toCs = new ColleagueSession();
			toCs.setColleagueCampus(fromCs.getColleagueCampus());
			toCs.setColleagueTermCode(toSession.getAcademicTerm());
			toCs.setSendDataToColleague(Boolean.valueOf(false));
			toCs.setStoreDataForColleague(Boolean.valueOf(true));
			toCs.setLoadingOfferingsFile(Boolean.valueOf(false));
			toCs.setSession(toSession);
			toCs.setUniqueIdRolledForwardFrom(fromSession.getUniqueId());
			ColleagueSessionDAO.getInstance().getSession().persist(toCs);
		}
		ColleagueSessionDAO.getInstance().getSession().flush();
	}

	private ExternalSessionRollForwardCustomizationInterface getExternalSessionRollForwardCustomization() throws Exception{
		if (externalSessionRollForwardCustomization == null){
            String className = ApplicationProperties.getProperty("tmtbl.colleague.session.rollForward.custom");
        	if (className != null && className.trim().length() > 0){
        		try {
					externalSessionRollForwardCustomization = (ExternalSessionRollForwardCustomizationInterface) (Class.forName(className).getDeclaredConstructor().newInstance());
				} catch (InstantiationException e) {
					iLog.error("Failed to instantiate instance of: " + className + " unable to perfor custom roll forward action.", e);
					throw (new Exception("Failed to instantiate instance of: " + className + " unable to perfor custom roll forward action."));
				} catch (IllegalAccessException e) {
					iLog.error("Illegal Access Exception on: " + className + " unable to perfor custom roll forward action.", e);
					throw (new Exception("Illegal Access Exception on: " + className + " unable to perfor custom roll forward action."));
				} catch (ClassNotFoundException e) {
					iLog.error("Failed to find class: " + className + " using the default session element helper.", e);
					throw (new Exception("Failed to find class: " + className + " using the default session element helper."));
				}
        	} 
		}
		return externalSessionRollForwardCustomization;

	}

}
