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

package org.unitime.banner.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.hibernate.Transaction;
import org.unitime.banner.form.RollForwardBannerSessionForm;
import org.unitime.banner.interfaces.ExternalSessionRollForwardCustomizationInterface;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.RollForwardSessionAction.RollForwardErrors;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.InstructionalOfferingComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.SessionRollForward;


/**
 * @author says
 *
 */
public class BannerSessionRollForward extends SessionRollForward {
	protected static final BannerMessages BMSG = Localization.create(BannerMessages.class);
	private static ExternalSessionRollForwardCustomizationInterface externalSessionRollForwardCustomization;

	/**
	 * 
	 */
	public BannerSessionRollForward(Log log) {
		super(log);
	}
	
	public void rollBannerSessionDataForward(RollForwardErrors errors, RollForwardBannerSessionForm rollForwardBannerSessionForm){
		Session toSession = Session.getSessionById(rollForwardBannerSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardBannerSessionForm.getSessionToRollBannerDataForwardFrom());
		
		try {
			rollForwardBannerSessionData(toSession, fromSession);
			rollForwardBannerCourseData(toSession);
			cleanUpBadLinkIdentifiers(toSession);
			updateClassSuffixes(toSession);
		} catch (Exception e) {
			String type = BMSG.rollForwardBannerSessionData();
			String msg = BMSG.errorFailedToRollBannerSessionData(e.getMessage());
			iLog.error(msg, e);
			errors.addFieldError("rollForward", MSG.errorRollingForward(type, fromSession.getLabel(), toSession.getLabel(), msg));
		}
		try {
			ExternalSessionRollForwardCustomizationInterface customRollForwardAction = getExternalSessionRollForwardCustomization();
			org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
			Transaction trns = null;
			try {
				if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
					trns = hibSession.beginTransaction();
				if (customRollForwardAction != null){
				customRollForwardAction.doCustomRollFowardAction(fromSession, toSession);
				}
				if (trns != null && trns.isActive()) {
					trns.commit();
				}
			} catch (Exception e){
				iLog.error("Failed to perform custom roll banner session data forward action for session: " + toSession.getLabel(), e);
				if (trns != null){
					if (trns.isActive()){
						trns.rollback();
					}
				}
				throw e;
			}	

		} catch (Exception e) {
			String type = BMSG.rollForwardBannerSessionData();
			String msg = BMSG.errorFailedToRollForwardCustomData(e.getMessage());
			iLog.error(msg, e);
			errors.addFieldError("rollForward", MSG.errorRollingForward(type, fromSession.getLabel(), toSession.getLabel(), msg));
		}		
	}
		
	private void cleanUpBadLinkIdentifiers(Session toSession) {
		Long toSessionId = toSession.getUniqueId();
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		BannerSession bs = BannerSession.findBannerSessionForSession(toSessionId, hibSession);
		if (bs.isStoreDataForBanner().booleanValue()){			
			@SuppressWarnings("unchecked")
			TreeSet<InstructionalOffering> offeringsToRebuild = new TreeSet<InstructionalOffering>(new InstructionalOfferingComparator(null));
			
			StringBuffer sb = new StringBuffer();
			sb.append("select bs1")
			  .append(" from BannerSection bs1, BannerSection bs2, BannerSession bsess1, BannerSession bsess2, CourseOffering co1, CourseOffering co2")
			  .append(" where bs1.session.uniqueId = :sessionId")
			  .append(" and bs1.session != bs2.session")
			  .append(" and bsess1.session = bs1.session")
			  .append(" and bsess2.session = bs2.session")
			  .append(" and bsess1.bannerTermCode = bsess2.bannerTermCode")
			  .append(" and co1.uniqueId = bs1.bannerConfig.bannerCourse.courseOfferingId")
			  .append(" and co2.uniqueId = bs2.bannerConfig.bannerCourse.courseOfferingId")
			  .append(" and (case when bsess1.useSubjectAreaPrefixAsCampus = true then")
			  .append("   case when bsess1.subjectAreaPrefixDelimiter is not null and bsess1.subjectAreaPrefixDelimiter != '' then")
			  .append(" substr(co1.subjectArea.subjectAreaAbbreviation, cast(regexp_instr(co1.subjectArea.subjectAreaAbbreviation, bsess1.subjectAreaPrefixDelimiter, 1, 1, 1) as int))")
			  .append(" else substr(co1.subjectArea.subjectAreaAbbreviation, cast(regexp_instr(co1.subjectArea.subjectAreaAbbreviation, ' - ', 1, 1, 1) as int)) end")
			  .append(" else  co1.subjectArea.subjectAreaAbbreviation end) = ")
			  .append(" (case when bsess2.useSubjectAreaPrefixAsCampus = true then ")
			  .append(" case when bsess2.subjectAreaPrefixDelimiter is not null and bsess2.subjectAreaPrefixDelimiter != '' then")
			  .append(" substr(co2.subjectArea.subjectAreaAbbreviation, cast(regexp_instr(co2.subjectArea.subjectAreaAbbreviation, bsess2.subjectAreaPrefixDelimiter, 1, 1, 1) as int))")
			  .append(" else substr(co2.subjectArea.subjectAreaAbbreviation, cast(regexp_instr(co2.subjectArea.subjectAreaAbbreviation, ' - ', 1, 1, 1) as int)) end")
			  .append(" else  co2.subjectArea.subjectAreaAbbreviation end)")
			  .append(" and substr(co1.courseNbr, 1, 5) = substr(co2.courseNbr, 1, 5)")
			  .append(" and bs1.linkIdentifier = bs2.linkIdentifier")
			  .append(" order by co1.subjectArea.subjectAreaAbbreviation, co1.courseNbr, co2.subjectArea.subjectAreaAbbreviation ")
			  ;
			List<BannerSection> sectionsToFix = hibSession.createQuery(sb.toString(), BannerSection.class).setParameter("sessionId", toSession.getUniqueId()).list();
			for (BannerSection bannerSectionToFix : sectionsToFix) {
				bannerSectionToFix.setLinkIdentifier(null);
				bannerSectionToFix.setLinkConnector(null);
				offeringsToRebuild.add(bannerSectionToFix.getFirstClass().getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering());
			}
			
			BannerInstrOffrConfigChangeAction biocca = null;
			for (InstructionalOffering io : offeringsToRebuild) {
				iLog.info("Rebuild Banner Sections for Offering:  " + io.getCourseNameWithTitle());
				biocca = new BannerInstrOffrConfigChangeAction();
				biocca.updateInstructionalOffering(io, hibSession);
				hibSession.evict(io);
			}	
			hibSession.flush();
			hibSession.clear();

		}		

	}

	@SuppressWarnings("unchecked")
	public void createMissingBannerSections(RollForwardErrors errors, RollForwardBannerSessionForm rollForwardBannerSessionForm){
		Session academicSession = Session.getSessionById(rollForwardBannerSessionForm.getSessionToRollForwardTo());
		try {
			TreeSet<SubjectArea> subjectAreas = new TreeSet<SubjectArea>();
			subjectAreas.addAll((Set<SubjectArea>)academicSession.getSubjectAreas());

			for (SubjectArea sa : subjectAreas) {
				TreeSet<InstructionalOffering> offeringsToUpdate = new TreeSet<InstructionalOffering>(new InstructionalOfferingComparator(null));
				org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
				offeringsToUpdate.addAll(BannerSection.findOfferingsMissingBannerSectionsForSubjectArea(sa, hibSession));
				BannerInstrOffrConfigChangeAction biocca = null;
				for (InstructionalOffering io : offeringsToUpdate) {
					iLog.info("Creating Missing Banner Sections for Offering:  " + io.getCourseNameWithTitle());
					biocca = new BannerInstrOffrConfigChangeAction();
					biocca.updateInstructionalOffering(io, hibSession);
					hibSession.evict(io);
				}	
				hibSession.flush();
				hibSession.clear();
			}				
		} catch (Exception e) {
			String type = BMSG.rollForwardCreateMissingBannerSectionData();
			String msg = BMSG.errorFailedToCreateMissingBannerSections(e.getMessage());
			iLog.error(msg, e);
			errors.addFieldError("rollForward", MSG.errorRollingForwardTo(type, academicSession.getLabel(), msg));
		}
	}
	
	private HashMap<Long, CourseOffering> findByIdRolledForwardFrom(Long sessionId, Long uniqueIdRolledForwardFrom) {
    	HashMap<Long, CourseOffering> cfgIdToCourseMap = new HashMap<Long, CourseOffering>();
    	
        for (CourseOffering co : new CourseOfferingDAO().
            getSession().
            createQuery("select c from CourseOffering c where c.subjectArea.session.uniqueId=:sessionId and c.uniqueIdRolledForwardFrom=:uniqueIdRolledForwardFrom", CourseOffering.class).
            setParameter("sessionId", sessionId.longValue()).
            setParameter("uniqueIdRolledForwardFrom", uniqueIdRolledForwardFrom.longValue()).
            setCacheable(true).
            list()) {
        	for (InstrOfferingConfig ioc : co.getInstructionalOffering().getInstrOfferingConfigs()) {     		
        		cfgIdToCourseMap.put(ioc.getUniqueId(), co);
        	}
        }
        return cfgIdToCourseMap;
    }

	private void rollForwardBannerCourseData(Session toSession) throws Exception {
		Long toSessionId = toSession.getUniqueId();
		BannerSession bs = BannerSession.findBannerSessionForSession(toSessionId, null);
		if (bs.isStoreDataForBanner().booleanValue()){
			TreeSet<SubjectArea> subjectAreas =  new TreeSet<SubjectArea>();
			for(Iterator<SubjectArea> saIt = toSession.getSubjectAreas().iterator(); saIt.hasNext();){
				subjectAreas.add((SubjectArea) saIt.next());
			}
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct bc from BannerCourse bc, CourseOffering co where co.instructionalOffering.session.uniqueId = :sessionId")
			  .append(" and bc.courseOfferingId = co.uniqueIdRolledForwardFrom")
			  .append(" and co.uniqueId not in (select bc2.courseOfferingId from BannerCourse bc2)")
			  .append(" and 0 < (select count(bs) from BannerSection bs where bs.bannerConfig.bannerCourse = bc)")
			  .append(" and co.subjectArea.uniqueId = :subjectId");
			String queryString = sb.toString();
			org.hibernate.Session hibSession = BannerCourseDAO.getInstance().getSession();
			for(SubjectArea sa : subjectAreas){
				iLog.info("Rolling Banner Data for Subject Area:  " + sa.getSubjectAreaAbbreviation());
				Iterator<BannerCourse> fromBannerCourseIt = hibSession.createQuery(queryString, BannerCourse.class)
									.setParameter("sessionId", toSessionId.longValue())
									.setParameter("subjectId", sa.getUniqueId().longValue())
									.list()
									.iterator();
				while (fromBannerCourseIt.hasNext()){
					BannerCourse fromBc = (BannerCourse) fromBannerCourseIt.next();
					HashMap<Long, BannerCourse> toCoToBannerCourseMap = new HashMap<Long, BannerCourse>();
					Transaction trns = null;
					try {
						if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
							trns = hibSession.beginTransaction();
						HashMap<Long, CourseOffering> cfgIdToCourseMap = findByIdRolledForwardFrom(toSessionId, fromBc.getCourseOfferingId());
						for(Iterator<BannerConfig> fromBannerConfigsIt = fromBc.getBannerConfigs().iterator(); fromBannerConfigsIt.hasNext();){
							BannerConfig fromBcfg = (BannerConfig) fromBannerConfigsIt.next();
							InstrOfferingConfig toIoc = InstrOfferingConfig.findByIdRolledForwardFrom(toSessionId, fromBcfg.getInstrOfferingConfigId());
							if (toIoc == null){
								iLog.info("Not rolling foward Banner Configuration with unique id: " + fromBcfg.getUniqueId().toString() + ", this configuration was orphaned.");		
								continue;
							}
							BannerCourse toBc = null;
							if (cfgIdToCourseMap.containsKey(toIoc.getUniqueId())) {
								CourseOffering toCo = cfgIdToCourseMap.get(toIoc.getUniqueId());
								if (toCoToBannerCourseMap.containsKey(toCo.getUniqueId())) {
									toBc = toCoToBannerCourseMap.get(toCo.getUniqueId());
								} else {
									toBc = new BannerCourse();
									toBc.setUniqueIdRolledForwardFrom(fromBc.getUniqueId());
									toBc.setCourseOfferingId(toCo.getUniqueId());
									toCoToBannerCourseMap.put(toCo.getUniqueId(), toBc);
								}								
							} else {
								continue;
							}
							BannerConfig toBcfg = new BannerConfig();
							toBcfg.setUniqueIdRolledForwardFrom(fromBcfg.getUniqueId());
							toBcfg.setInstrOfferingConfigId(toIoc.getUniqueId());
							toBcfg.setBannerCourse(toBc);
							toBcfg.setGradableItype(fromBcfg.getGradableItype());
							toBcfg.setLabHours(fromBcfg.getLabHours());
							toBc.addToBannerConfigs(toBcfg);
							for(Iterator<BannerSection> fromBannerSectionsIt = fromBcfg.getBannerSections().iterator(); fromBannerSectionsIt.hasNext();){
								BannerSection fromBs = (BannerSection) fromBannerSectionsIt.next();
								BannerSection toBs = new BannerSection();
								toBs.setCrossListIdentifier(fromBs.getCrossListIdentifier());
								toBs.setLinkIdentifier(fromBs.getLinkIdentifier());
								toBs.setLinkConnector(fromBs.getLinkConnector());
								toBs.setCrn(fromBs.getCrn());
								toBs.setSectionIndex(fromBs.getSectionIndex());
								toBs.setUniqueIdRolledForwardFrom(fromBs.getUniqueId());
								toBs.setOverrideCourseCredit(fromBs.getOverrideCourseCredit());
								toBs.setOverrideLimit(fromBs.getOverrideLimit());
								toBs.setBannerCampusOverride(fromBs.getBannerCampusOverride());
							
								
								for (Iterator<BannerSectionToClass> fromBannerSectionToClassIt = fromBs.getBannerSectionToClasses().iterator(); fromBannerSectionToClassIt.hasNext();){
									BannerSectionToClass fromBsc = (BannerSectionToClass) fromBannerSectionToClassIt.next();
									Class_ toClass = Class_.findByIdRolledForwardFrom(toSessionId, fromBsc.getClassId());
									if (toClass != null) {
										BannerSectionToClass toBsc = new BannerSectionToClass();
										toBsc.setBannerSection(toBs);
										toBs.addToBannerSectionToClasses(toBsc);
										toBsc.setClassId(toClass.getUniqueId());
									}
								}
								if (toBs.getBannerSectionToClasses() != null){
									toBs.setBannerConfig(toBcfg);
									toBcfg.addToBannerSections(toBs);								
									toBs.setConsentType(fromBs.getConsentType());
									toBs.setSession(toSession);
								}
							}
						}
						for (BannerCourse toBc : toCoToBannerCourseMap.values()) {
							if (toBc.getUniqueId() == null) {
								hibSession.persist(toBc);
							} else {
								hibSession.merge(toBc);
							}
						}
						if (trns != null && trns.isActive()) {
							trns.commit();
						}
						hibSession.flush();
						for (BannerCourse toBc : toCoToBannerCourseMap.values()) {
							hibSession.evict(toBc);
						}
						hibSession.evict(fromBc);
					} catch (Exception e){
						iLog.error("Failed to roll banner course data: " + fromBc.getCourseOffering(hibSession), e);
						if (trns != null){
							if (trns.isActive()){
								trns.rollback();
							}
						}
						throw e;
					}	
				}
			}
		}
	}

	private void updateClassSuffixes(Session toSession) throws Exception {
		Long toSessionId = toSession.getUniqueId();
		org.hibernate.Session hibSession = BannerCourseDAO.getInstance().getSession();
		BannerSession bs = BannerSession.findBannerSessionForSession(toSessionId, hibSession);
		if (bs.isStoreDataForBanner().booleanValue()){
			TreeSet<SubjectArea> subjectAreas =  new TreeSet<SubjectArea>();
			subjectAreas.addAll(toSession.getSubjectAreas());
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct bc from BannerCourse bc, CourseOffering co where co.instructionalOffering.session.uniqueId = :sessionId")
			  .append(" and bc.courseOfferingId = co.uniqueId")
			  .append(" and co.subjectArea.uniqueId = :subjectId");
			String queryString = sb.toString();
			StringBuilder sb2 = new StringBuilder();
			sb2.append("select distinct c from Class_ c, CourseOffering co, BannerSectionToClass bstc")
			  .append(" where co.instructionalOffering.session.uniqueId = :sessionId")
			  .append(" and co.subjectArea.uniqueId = :subjectId")
			  .append(" and bstc.bannerSection.bannerConfig.bannerCourse.courseOfferingId = co.uniqueId")
			  .append(" and c.uniqueId = bstc.classId");
			String queryString2 = sb2.toString();
			for(SubjectArea sa : subjectAreas){
				Transaction trns = null;
				try {
					if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
						trns = hibSession.beginTransaction();
					iLog.info("Updating Class External Ids for Subject Area:  " + sa.getSubjectAreaAbbreviation());
					List<BannerCourse> bannerCourses = hibSession.createQuery(queryString, BannerCourse.class)
										.setParameter("sessionId", toSessionId.longValue())
										.setParameter("subjectId", sa.getUniqueId().longValue())
										.list();
					List<Class_> classes = hibSession.createQuery(queryString2, Class_.class)
										.setParameter("sessionId", toSessionId.longValue())
										.setParameter("subjectId", sa.getUniqueId().longValue())
										.setCacheable(true)
										.list();
					for (BannerCourse bc : bannerCourses){
						for(BannerConfig bcfg : bc.getBannerConfigs()){
							for(BannerSection bannerSection : bcfg.getBannerSections()){
								bannerSection.updateClassSuffixForClassesIfNecessaryRefreshClasses(hibSession, false);
							}
						}
						hibSession.evict(bc);
					}
					if (trns != null && trns.isActive()) {
						trns.commit();
					}
					hibSession.flush();
					for(Class_ c : classes){
						hibSession.evict(c);
					}
				} catch (Exception e){
					iLog.error("Failed to update classs suffixes for session: " + toSession.getLabel(), e);
					if (trns != null){
						if (trns.isActive()){
							trns.rollback();
						}
					}
					throw e;
				}	
			}
		}
	}
	private void rollForwardBannerSessionData(Session toSession,
			Session fromSession) throws Exception {
		Transaction trns = null;
		org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
		try {
			if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
				trns = hibSession.beginTransaction();

			BannerSession toBs = BannerSession.findBannerSessionForSession(toSession, null);
			if (toBs == null) {
				BannerSession fromBs = BannerSession.findBannerSessionForSession(fromSession, null);
				toBs = new BannerSession();
				toBs.setBannerCampus(fromBs.getBannerCampus());
				String oldYearStr = fromBs.getBannerTermCode().substring(0,4);
				int year = Integer.parseInt(oldYearStr);
				year++;
				String newYearStr = Integer.toString(year) + fromBs.getBannerTermCode().substring(4, 6);
				toBs.setBannerTermCode(newYearStr);
				toBs.setStoreDataForBanner(fromBs.isStoreDataForBanner());
				toBs.setSendDataToBanner(Boolean.valueOf(false));
				toBs.setLoadingOfferingsFile(Boolean.valueOf(false));
				toBs.setSession(toSession);
				toBs.setStudentCampus(fromBs.getStudentCampus());
				toBs.setSubjectAreaPrefixDelimiter(fromBs.getSubjectAreaPrefixDelimiter());
				toBs.setUseSubjectAreaPrefixAsCampus(fromBs.getUseSubjectAreaPrefixAsCampus());
				hibSession.persist(toBs);
			}
			if (trns != null && trns.isActive()) {
				trns.commit();
			} else {
				hibSession.flush();
			}
		} catch (Exception e){
			iLog.error("Failed to roll banner session data: " + toSession.getLabel(), e);
			if (trns != null){
				if (trns.isActive()){
					trns.rollback();
				}
				throw e;
			}
		}	
	}

	private ExternalSessionRollForwardCustomizationInterface getExternalSessionRollForwardCustomization() throws Exception{
		if (externalSessionRollForwardCustomization == null){
            String className = ApplicationProperties.getProperty("tmtbl.banner.session.rollForward.custom");
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
