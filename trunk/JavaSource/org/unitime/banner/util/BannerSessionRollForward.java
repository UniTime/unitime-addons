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

package org.unitime.banner.util;

import java.util.Iterator;
import java.util.List;
import java.util.TreeSet;

import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.unitime.banner.form.RollForwardBannerSessionForm;
import org.unitime.banner.interfaces.ExternalSessionRollForwardCustomizationInterface;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
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
	public BannerSessionRollForward() {
		// do nothing
	}
	
	public void rollBannerSessionDataForward(ActionMessages errors, RollForwardBannerSessionForm rollForwardBannerSessionForm){
		Session toSession = Session.getSessionById(rollForwardBannerSessionForm.getSessionToRollForwardTo());
		Session fromSession = Session.getSessionById(rollForwardBannerSessionForm.getSessionToRollBannerDataForwardFrom());
		
		try {
			rollForwardBannerSessionData(toSession, fromSession);
			rollForwardBannerCourseData(toSession);
			updateClassSuffixes(toSession);
		} catch (Exception e) {
			Debug.error(e);
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Banner Session Data", fromSession.getLabel(), toSession.getLabel(), "Failed to roll banner session data forward."));
		}
		try {
			ExternalSessionRollForwardCustomizationInterface customRollForwardAction = getExternalSessionRollForwardCustomization();
			if (customRollForwardAction != null){
				customRollForwardAction.doCustomRollFowardAction(fromSession, toSession);
			}
		} catch (Exception e) {
			Debug.error(e);
			errors.add("rollForward", new ActionMessage("errors.rollForward", "Banner Session Data", fromSession.getLabel(), toSession.getLabel(), "Failed to perform custom roll banner session data forward action: " + e.getMessage()));
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
			  .append(" and co.subjectArea.uniqueId = :subjectId");
			String queryString = sb.toString();
			org.hibernate.Session hibSession = BannerCourseDAO.getInstance().getSession();
			for(SubjectArea sa : subjectAreas){
				Debug.info("Rolling Banner Data for Subject Area:  " + sa.getSubjectAreaAbbreviation());
				Iterator fromBannerCourseIt = hibSession.createQuery(queryString)
									.setLong("sessionId", toSessionId.longValue())
									.setLong("subjectId", sa.getUniqueId().longValue())
									.list()
									.iterator();
				while (fromBannerCourseIt.hasNext()){
					BannerCourse fromBc = (BannerCourse) fromBannerCourseIt.next();
					BannerCourse toBc = new BannerCourse();
					toBc.setUniqueIdRolledForwardFrom(fromBc.getUniqueId());
					CourseOffering toCo = CourseOffering.findByIdRolledForwardFrom(toSessionId, fromBc.getCourseOfferingId());
					toBc.setCourseOfferingId(toCo.getUniqueId());
					for(Iterator fromBannerConfigsIt = fromBc.getBannerConfigs().iterator(); fromBannerConfigsIt.hasNext();){
						BannerConfig fromBcfg = (BannerConfig) fromBannerConfigsIt.next();
						BannerConfig toBcfg = new BannerConfig();
						InstrOfferingConfig toIoc = InstrOfferingConfig.findByIdRolledForwardFrom(toSessionId, fromBcfg.getInstrOfferingConfigId());
						if (toIoc == null){
							Debug.info("Not rolling foward Banner Configuration with unique id: " + fromBcfg.getUniqueId().toString() + ", this configuration was orphaned.");		
							continue;
						}
						toBcfg.setUniqueIdRolledForwardFrom(fromBcfg.getUniqueId());
						toBcfg.setInstrOfferingConfigId(toIoc.getUniqueId());
						toBcfg.setBannerCourse(toBc);
						toBcfg.setGradableItype(fromBcfg.getGradableItype());
						toBc.addTobannerConfigs(toBcfg);
						for(Iterator fromBannerSectionsIt = fromBcfg.getBannerSections().iterator(); fromBannerSectionsIt.hasNext();){
							BannerSection fromBs = (BannerSection) fromBannerSectionsIt.next();
							BannerSection toBs = new BannerSection();
							toBs.setBannerConfig(toBcfg);
							toBcfg.addTobannerSections(toBs);
							toBs.setCrossListIdentifier(fromBs.getCrossListIdentifier());
							toBs.setLinkIdentifier(fromBs.getLinkIdentifier());
							toBs.setLinkConnector(fromBs.getLinkConnector());
							toBs.setCrn(fromBs.getCrn());
							toBs.setSectionIndex(fromBs.getSectionIndex());
							toBs.setUniqueIdRolledForwardFrom(fromBs.getUniqueIdRolledForwardFrom());
							toBs.setConsentType(fromBs.getConsentType());
							toBs.setOverrideCourseCredit(fromBs.getOverrideCourseCredit());
							toBs.setOverrideLimit(fromBs.getOverrideLimit());
							toBs.setSession(toSession);
							
							for (Iterator fromBannerSectionToClassIt = fromBs.getBannerSectionToClasses().iterator(); fromBannerSectionToClassIt.hasNext();){
								BannerSectionToClass fromBsc = (BannerSectionToClass) fromBannerSectionToClassIt.next();
								BannerSectionToClass toBsc = new BannerSectionToClass();
								toBsc.setBannerSection(toBs);
								toBs.addTobannerSectionToClasses(toBsc);
								Class_ toClass = Class_.findByIdRolledForwardFrom(toSessionId, fromBsc.getClassId());
								toBsc.setClassId(toClass.getUniqueId());
							}
						}
					}
					hibSession.save(toBc);
					hibSession.flush();
					hibSession.evict(toBc);
					hibSession.evict(fromBc);
				}
			}
		}
	}

	private void updateClassSuffixes(Session toSession) {
		Long toSessionId = toSession.getUniqueId();
		BannerSession bs = BannerSession.findBannerSessionForSession(toSessionId, null);
		if (bs.isStoreDataForBanner().booleanValue()){
			TreeSet<SubjectArea> subjectAreas =  new TreeSet<SubjectArea>();
			for(Iterator saIt = toSession.getSubjectAreas().iterator(); saIt.hasNext();){
				subjectAreas.add((SubjectArea) saIt.next());
			}
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
			org.hibernate.Session hibSession = BannerCourseDAO.getInstance().getSession();
			for(SubjectArea sa : subjectAreas){
				Debug.info("Updating Class External Ids for Subject Area:  " + sa.getSubjectAreaAbbreviation());
				Iterator bannerCourseIt = hibSession.createQuery(queryString)
									.setLong("sessionId", toSessionId.longValue())
									.setLong("subjectId", sa.getUniqueId().longValue())
									.list()
									.iterator();
				List classes = hibSession.createQuery(queryString2)
									.setLong("sessionId", toSessionId.longValue())
									.setLong("subjectId", sa.getUniqueId().longValue())
									.setCacheable(true)
									.list();
				while (bannerCourseIt.hasNext()){
					BannerCourse bc = (BannerCourse) bannerCourseIt.next();
					for(Iterator bannerConfigsIt = bc.getBannerConfigs().iterator(); bannerConfigsIt.hasNext();){
						BannerConfig bcfg = (BannerConfig) bannerConfigsIt.next();
						for(Iterator bannerSectionsIt = bcfg.getBannerSections().iterator(); bannerSectionsIt.hasNext();){
							BannerSection bannerSection = (BannerSection) bannerSectionsIt.next();
							bannerSection.updateClassSuffixForClassesIfNecessaryRefreshClasses(hibSession, false);
						}
					}
					hibSession.evict(bc);
				}
				hibSession.flush();
				for(Iterator cIt = classes.iterator(); cIt.hasNext();){
					hibSession.evict((Class_) cIt.next());
				}
			}
		}
	}
	private void rollForwardBannerSessionData(Session toSession,
			Session fromSession) {
		BannerSession fromBs = BannerSession.findBannerSessionForSession(fromSession, null);
		BannerSession toBs = new BannerSession();
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
		BannerSessionDAO.getInstance().save(toBs);
	}

	private ExternalSessionRollForwardCustomizationInterface getExternalSessionRollForwardCustomization() throws Exception{
		if (externalSessionRollForwardCustomization == null){
            String className = ApplicationProperties.getProperty("tmtbl.banner.session.rollForward.custom");
        	if (className != null && className.trim().length() > 0){
        		try {
					externalSessionRollForwardCustomization = (ExternalSessionRollForwardCustomizationInterface) (Class.forName(className).newInstance());
				} catch (InstantiationException e) {
					Debug.error("Failed to instantiate instance of: " + className + " unable to perfor custom roll forward action.");
					e.printStackTrace();
					throw (new Exception("Failed to instantiate instance of: " + className + " unable to perfor custom roll forward action."));
				} catch (IllegalAccessException e) {
					Debug.error("Illegal Access Exception on: " + className + " unable to perfor custom roll forward action.");
					e.printStackTrace();
					throw (new Exception("Illegal Access Exception on: " + className + " unable to perfor custom roll forward action."));
				} catch (ClassNotFoundException e) {
					Debug.error("Failed to find class: " + className + " using the default session element helper.");
					e.printStackTrace();
					throw (new Exception("Failed to find class: " + className + " using the default session element helper."));
				}
        	} 
		}
		return externalSessionRollForwardCustomization;

	}

}
