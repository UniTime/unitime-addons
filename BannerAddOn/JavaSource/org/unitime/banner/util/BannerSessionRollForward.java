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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.hibernate.Transaction;
import org.unitime.banner.form.RollForwardBannerSessionForm;
import org.unitime.banner.interfaces.ExternalSessionRollForwardCustomizationInterface;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.InstructionalOfferingComparator;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.util.SessionRollForward;


/**
 * @author says
 *
 */
public class BannerSessionRollForward extends SessionRollForward {
	private static ExternalSessionRollForwardCustomizationInterface externalSessionRollForwardCustomization;

	/**
	 * 
	 */
	public BannerSessionRollForward(Log log) {
		super(log);
	}
	
	public void rollBannerSessionDataForward(ActionMessages errors, RollForwardBannerSessionForm rollForwardBannerSessionForm){
		Session toSession = Session.getSessionById(rollForwardBannerSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardBannerSessionForm.getSessionToRollBannerDataForwardFrom());
		
		try {
			rollForwardBannerSessionData(toSession, fromSession);
			rollForwardBannerCourseData(toSession);
			updateClassSuffixes(toSession);
		} catch (Exception e) {
			iLog.error("Failed to roll banner session data forward.", e);
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Banner Session Data", fromSession.getLabel(), toSession.getLabel(), "Failed to roll banner session data forward."));
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
				if (trns != null) {
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
			iLog.error("Failed to perform custom roll banner session data forward action.", e);
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Banner Session Data", fromSession.getLabel(), toSession.getLabel(), "Failed to perform custom roll banner session data forward action: " + e.getMessage()));
		}
		
	}
	
	@SuppressWarnings("unchecked")
	public void createMissingBannerSections(ActionMessages errors, RollForwardBannerSessionForm rollForwardBannerSessionForm){
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
			iLog.error("Failed to create missing Banner sections.", e);
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Banner Section Data", academicSession.getLabel(), "Failed to create missing Banner sections."));
		}
	}

	private void rollForwardBannerCourseData(Session toSession) {
		Long toSessionId = toSession.getUniqueId();
		BannerSession bs = BannerSession.findBannerSessionForSession(toSessionId, null);
		if (bs.isStoreDataForBanner().booleanValue()){
			TreeSet<SubjectArea> subjectAreas =  new TreeSet<SubjectArea>();
			for(Iterator saIt = toSession.getSubjectAreas().iterator(); saIt.hasNext();){
				subjectAreas.add((SubjectArea) saIt.next());
			}
			StringBuilder sb = new StringBuilder();
			sb.append("select distinct bc from BannerCourse bc, CourseOffering co where co.instructionalOffering.session.uniqueId = :sessionId")
			  .append(" and bc.courseOfferingId = co.uniqueIdRolledForwardFrom")
			  .append(" and co.uniqueId not in (select bc2.courseOfferingId from BannerCourse bc2)")
			  .append(" and co.subjectArea.uniqueId = :subjectId");
			String queryString = sb.toString();
			org.hibernate.Session hibSession = BannerCourseDAO.getInstance().getSession();
			for(SubjectArea sa : subjectAreas){
				iLog.info("Rolling Banner Data for Subject Area:  " + sa.getSubjectAreaAbbreviation());
				Iterator fromBannerCourseIt = hibSession.createQuery(queryString)
									.setLong("sessionId", toSessionId.longValue())
									.setLong("subjectId", sa.getUniqueId().longValue())
									.list()
									.iterator();
				while (fromBannerCourseIt.hasNext()){
					BannerCourse fromBc = (BannerCourse) fromBannerCourseIt.next();
					Transaction trns = null;
					try {
						if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
							trns = hibSession.beginTransaction();
						BannerCourse toBc = new BannerCourse();
						toBc.setUniqueIdRolledForwardFrom(fromBc.getUniqueId());
						CourseOffering toCo = CourseOffering.findByIdRolledForwardFrom(toSessionId, fromBc.getCourseOfferingId());
						toBc.setCourseOfferingId(toCo.getUniqueId());
						for(Iterator fromBannerConfigsIt = fromBc.getBannerConfigs().iterator(); fromBannerConfigsIt.hasNext();){
							BannerConfig fromBcfg = (BannerConfig) fromBannerConfigsIt.next();
							BannerConfig toBcfg = new BannerConfig();
							InstrOfferingConfig toIoc = InstrOfferingConfig.findByIdRolledForwardFrom(toSessionId, fromBcfg.getInstrOfferingConfigId());
							if (toIoc == null){
								iLog.info("Not rolling foward Banner Configuration with unique id: " + fromBcfg.getUniqueId().toString() + ", this configuration was orphaned.");		
								continue;
							}
							toBcfg.setUniqueIdRolledForwardFrom(fromBcfg.getUniqueId());
							toBcfg.setInstrOfferingConfigId(toIoc.getUniqueId());
							toBcfg.setBannerCourse(toBc);
							toBcfg.setGradableItype(fromBcfg.getGradableItype());
							toBcfg.setLabHours(fromBcfg.getLabHours());
							toBc.addTobannerConfigs(toBcfg);
							for(Iterator fromBannerSectionsIt = fromBcfg.getBannerSections().iterator(); fromBannerSectionsIt.hasNext();){
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
							
								
								for (Iterator fromBannerSectionToClassIt = fromBs.getBannerSectionToClasses().iterator(); fromBannerSectionToClassIt.hasNext();){
									BannerSectionToClass fromBsc = (BannerSectionToClass) fromBannerSectionToClassIt.next();
									Class_ toClass = Class_.findByIdRolledForwardFrom(toSessionId, fromBsc.getClassId());
									if (toClass != null) {
										BannerSectionToClass toBsc = new BannerSectionToClass();
										toBsc.setBannerSection(toBs);
										toBs.addTobannerSectionToClasses(toBsc);
										toBsc.setClassId(toClass.getUniqueId());
									}
								}
								if (toBs.getBannerSectionToClasses() != null){
									toBs.setBannerConfig(toBcfg);
									toBcfg.addTobannerSections(toBs);								
									toBs.setConsentType(fromBs.getConsentType());
									toBs.setSession(toSession);
								}
							}
						}
						hibSession.save(toBc);
						if (trns != null && trns.isActive()) {
							trns.commit();
						}
						hibSession.flush();
						hibSession.evict(toBc);
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

	private void updateClassSuffixes(Session toSession) {
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
					@SuppressWarnings("unchecked")
					List<BannerCourse> bannerCourses = hibSession.createQuery(queryString)
										.setLong("sessionId", toSessionId.longValue())
										.setLong("subjectId", sa.getUniqueId().longValue())
										.list();
					@SuppressWarnings("unchecked")
					List<Class_> classes = hibSession.createQuery(queryString2)
										.setLong("sessionId", toSessionId.longValue())
										.setLong("subjectId", sa.getUniqueId().longValue())
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
			Session fromSession) {
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
				toBs.setSendDataToBanner(new Boolean(false));
				toBs.setLoadingOfferingsFile(new Boolean(false));
				toBs.setSession(toSession);
				toBs.setStudentCampus(fromBs.getStudentCampus());
				toBs.setSubjectAreaPrefixDelimiter(fromBs.getSubjectAreaPrefixDelimiter());
				toBs.setUseSubjectAreaPrefixAsCampus(fromBs.getUseSubjectAreaPrefixAsCampus());
				BannerSessionDAO.getInstance().save(toBs);
				if (trns != null && trns.isActive()) {
					trns.commit();
				}
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
					externalSessionRollForwardCustomization = (ExternalSessionRollForwardCustomizationInterface) (Class.forName(className).newInstance());
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
