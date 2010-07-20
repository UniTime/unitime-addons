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

import java.sql.CallableStatement;
import java.sql.Connection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.engine.SessionFactoryImplementor;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.dao._RootDAO;



/*
 * based on code contributed by Sean Justice
 */
public class BannerSectionCrosslistHelper {
	private InstructionalOffering instructionalOffering;
	private Session hibSession;
	/**
	 * 
	 */
	public BannerSectionCrosslistHelper(InstructionalOffering instructionalOffering, Session hibSession) {
		this.instructionalOffering = instructionalOffering;
		this.hibSession = hibSession;
	}
	
	private void crossListSections(List<BannerSection> sections){
        // declare the variables
		BannerSection section;
		Boolean needId = false;
		String crossListId = null;
		Iterator<?> iBS;
		
		// these sections should be crosslisted - find out if they already are
		for(iBS = sections.iterator(); iBS.hasNext();){
			section = (BannerSection) iBS.next();
			if (section.getCrossListIdentifier() == null){
				needId = true;
			} else if (crossListId == null){
				crossListId = section.getCrossListIdentifier();
			}
			if (section.getOverrideLimit() != null && section.getOverrideLimit().intValue() > section.maxEnrollBasedOnClasses(hibSession)){
				section.setOverrideLimit(null);
				saveChangesToBannerSectionIfNecessary(section);
			}
		}

		// if any sections are missing their crossListId, fix them
		if (needId){
			if (crossListId == null){
				crossListId = findNextUnusedCrosslistIdForSession(instructionalOffering.getSession());
			}
			for(iBS = sections.iterator(); iBS.hasNext();){
				section = (BannerSection) iBS.next();
				updateCrosslistIdForBannerSectionIfNecessaryAndSave(section, crossListId);
			}
		}
	}
	private void updateCrosslistIdForBannerSectionIfNecessaryAndSave(BannerSection bs, String crosslistId){
		if (bs.getCrossListIdentifier() == null || !bs.getCrossListIdentifier().equals(crosslistId)){
			bs.setCrossListIdentifier(crosslistId);
			saveChangesToBannerSectionIfNecessary(bs);
		}
	}

	private void saveChangesToBannerSectionIfNecessary(BannerSection bs){
		Transaction trans = hibSession.beginTransaction();
		hibSession.update(bs);
		trans.commit();
		hibSession.flush();
		hibSession.refresh(bs);
	}

	
	private void doBasicCrossListing(){
		// All courses should be cross listed, no exceptions found.
		
        // declare the variables
		Iterator<?> iC_;
		Iterator<?> iInOffConfig;
		Iterator<?> iSS;

		for (iInOffConfig = instructionalOffering.getInstrOfferingConfigs().iterator(); iInOffConfig.hasNext();){
			for(iSS = ((InstrOfferingConfig) iInOffConfig.next()).getSchedulingSubparts().iterator(); iSS.hasNext();){
				for (iC_ = ((SchedulingSubpart) iSS.next()).getClasses().iterator(); iC_.hasNext();){
					crossListSections(BannerSection.findBannerSectionsForClass(((Class_) iC_.next()), hibSession));
				}
			}
		}
    }
    
	@SuppressWarnings("unchecked")
	private void ensureAllSubpartClassesHaveBannerSection(SchedulingSubpart schedSubpart) throws Exception{
		
		for(Iterator cIt = schedSubpart.getClasses().iterator(); cIt.hasNext();){
			Class_ c = (Class_) cIt.next();
			List<BannerSection> bannerSections = BannerSection.findBannerSectionsForClass(c, hibSession);
			if (bannerSections.isEmpty()){
				if (c.getParentClass() != null && c.getParentClass().getSchedulingSubpart().getItype().getItype().equals(c.getSchedulingSubpart().getItype().getItype())){
					List parentBannerSections = BannerSection.findBannerSectionsForClass(c.getParentClass(), hibSession);
					Transaction trans = hibSession.beginTransaction();
					for(Iterator it = parentBannerSections.iterator(); it.hasNext();){
						BannerSection bs = (BannerSection) it.next();
						bs.addClass(c, hibSession);
						hibSession.update(bs);
					}
					trans.commit();
				} else {
					
					for (Iterator it = instructionalOffering.getCourseOfferings().iterator(); it.hasNext();){
						CourseOffering courseOffering = (CourseOffering) it.next();
						addBannerSectionFor(courseOffering, c);
					}
				}
			
			} else {
				if (c.getParentClass() != null && c.getParentClass().getSchedulingSubpart().getItype().getItype().equals(c.getSchedulingSubpart().getItype().getItype())){
					HashSet<BannerSection> sections = new HashSet<BannerSection>();
					sections.addAll(BannerSection.findBannerSectionsForClass(c, hibSession));
					
					HashSet<BannerSection> parentSections = new HashSet<BannerSection>();
					parentSections.addAll(BannerSection.findBannerSectionsForClass(c.getParentClass(), hibSession));
					
					HashSet<BannerSection> removeSet = new HashSet<BannerSection>();
					removeSet.addAll(sections);
					removeSet.removeAll(parentSections);
					
					
					
					HashSet<BannerSection> addSet = new HashSet<BannerSection>();
					addSet.addAll(parentSections);
					addSet.removeAll(sections);
					
					if (!removeSet.isEmpty()){
						for(BannerSection bs : removeSet){
							Transaction trans = hibSession.beginTransaction();
							SendBannerMessage.sendBannerMessage(bs, BannerMessageAction.DELETE, hibSession);
							BannerConfig bc = bs.getBannerConfig();
							bc.getBannerSections().remove(bs);
							hibSession.update(bc);
							trans.commit();
							hibSession.refresh(bc);
						}
					}
					if (!addSet.isEmpty()){
						for(BannerSection bs : addSet){
							Transaction trans = hibSession.beginTransaction();
							BannerConfig bc = bs.getBannerConfig();
							bs.addClass(c, hibSession);
							hibSession.update(bc);
							trans.commit();
							hibSession.refresh(bc);
						}
					}

				
				} else {
					HashSet<CourseOffering> hs = new HashSet<CourseOffering>();
					HashSet<BannerSection> removeSet = new HashSet<BannerSection>();
					hs.addAll(instructionalOffering.getCourseOfferings());
					CourseOffering co = null;
					for(BannerSection bs : bannerSections){
						co = bs.getBannerConfig().getBannerCourse().getCourseOffering(hibSession);
						if (!hs.remove(co)){
							removeSet.add(bs);
						}
					}
					if (!hs.isEmpty()){
						for (CourseOffering courseOffering : hs){
							addBannerSectionFor(courseOffering, c);
						}
					}
					if (!removeSet.isEmpty()){
						for(BannerSection bs : removeSet){
							Transaction trans = hibSession.beginTransaction();
							SendBannerMessage.sendBannerMessage(bs, BannerMessageAction.DELETE, hibSession);
							BannerConfig bc = bs.getBannerConfig();
							bc.getBannerSections().remove(bs);
							hibSession.update(bc);
							trans.commit();
							hibSession.refresh(bc);
						}
					}
				}
			}
			Transaction trans = hibSession.beginTransaction();
			adjustBannerSectionParentageIfNecessary(c);
			trans.commit();
			hibSession.flush();
		}
		for(Iterator ssIt = schedSubpart.getChildSubparts().iterator(); ssIt.hasNext();){
			SchedulingSubpart ss = (SchedulingSubpart) ssIt.next();
			ensureAllSubpartClassesHaveBannerSection(ss);
		}
	}
	
	private void adjustBannerSectionParentageIfNecessary(Class_ c) {
		List<BannerSection> sections = BannerSection.findBannerSectionsForClass(c, hibSession);
		for(BannerSection bs : sections){
			if (c.getParentClass() == null){
				if (bs.getParentBannerSection() != null){
					BannerSection parentSection = bs.getParentBannerSection();
					bs.setParentBannerSection(null);
					parentSection.getBannerSectionToChildSections().remove(bs);
					hibSession.update(bs);
					hibSession.update(parentSection);
				}
			} else {
				BannerSection parentSection = BannerSection.findBannerSectionForBannerCourseAndClass(bs.getBannerConfig().getBannerCourse(), c.getParentClass());
				Class_ parentClass = c.getParentClass();
				boolean foundParentClass = false;
				while (!foundParentClass && parentSection != null){
					if (!parentClass.getSchedulingSubpart().getItype().getItype().equals(c.getSchedulingSubpart().getItype().getItype())){
						foundParentClass = true;
					} else {
						parentSection = BannerSection.findBannerSectionForBannerCourseAndClass(bs.getBannerConfig().getBannerCourse(), parentClass.getParentClass());
						parentClass = parentClass.getParentClass();
					}
				}
				if (parentSection != null && (bs.getParentBannerSection() == null || !bs.getParentBannerSection().getUniqueId().equals(parentSection.getUniqueId()))){
					bs.setParentBannerSection(parentSection);
					parentSection.addTobannerSectionToChildSections(bs);
					hibSession.update(parentSection);
					hibSession.update(bs);
				} else if (parentSection == null && bs.getParentBannerSection() != null){
					bs.setParentBannerSection(null);
					hibSession.update(bs);
				}
			}
		}
	}

	private void addBannerSectionFor(CourseOffering courseOffering, Class_ cls){
		BannerConfig bc = BannerConfig.findBannerConfigForInstrOffrConfigAndCourseOffering(cls.getSchedulingSubpart().getInstrOfferingConfig(), courseOffering, hibSession);
		Transaction trans = hibSession.beginTransaction();
		if(bc == null) {
			bc = new BannerConfig();
			BannerCourse bannerCourse = BannerCourse.findBannerCourseForCourseOffering(courseOffering.getUniqueId(), hibSession);
			if (bannerCourse == null){
				bannerCourse = new  BannerCourse();
				bannerCourse.setCourseOfferingId(courseOffering.getUniqueId());
				hibSession.save(bannerCourse);
			}
			bc.setBannerCourse(bannerCourse);
			bc.setInstrOfferingConfigId(cls.getSchedulingSubpart().getInstrOfferingConfig().getUniqueId());
			bannerCourse.addTobannerConfigs(bc);
			hibSession.save(bc);
		}
		BannerSection bs = new BannerSection();
		bs.setBannerConfig(bc);
		bs.setSession(instructionalOffering.getSession());
		bs.addClass(cls, hibSession);
		
		bc.addTobannerSections(bs);
		hibSession.update(bc);
		trans.commit();
		hibSession.flush();
		hibSession.refresh(bc);
	}
	
	@SuppressWarnings("unchecked")
	private void ensureAllClassesHaveBannerSection() throws Exception{
		// Remove any orphaned banner sections
		BannerSection.removeOrphanedBannerSections(hibSession);
		
		for(Iterator iocIt = instructionalOffering.getInstrOfferingConfigs().iterator(); iocIt.hasNext();){
			InstrOfferingConfig ioc = (InstrOfferingConfig) iocIt.next();
			for(Iterator ssIt = ioc.getSchedulingSubparts().iterator(); ssIt.hasNext();){
				SchedulingSubpart ss = (SchedulingSubpart) ssIt.next();
				if (ss.getParentSubpart() == null){
					ensureAllSubpartClassesHaveBannerSection(ss);
				}
			}
		}
	}

	public static String findNextUnusedCrosslistIdForSession(
			org.unitime.timetable.model.Session acadSession) {
		String nextCrosslistId = null;
	   	try {
    		String nextXlistSql = ApplicationProperties.getProperty("banner.crosslist_id.generator", "{?= call timetable.cross_list_processor.get_cross_list_id(?)}");

            SessionFactoryImplementor hibSessionFactory = (SessionFactoryImplementor)new _RootDAO().getSession().getSessionFactory();
            Connection connection = hibSessionFactory.getConnectionProvider().getConnection();
            CallableStatement call = connection.prepareCall(nextXlistSql);
            call.registerOutParameter(1, java.sql.Types.VARCHAR);
            call.setLong(2, acadSession.getUniqueId().longValue());
            call.execute();
            nextCrosslistId = call.getString(1);
            call.close();
            hibSessionFactory.getConnectionProvider().closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
		} 

		return(nextCrosslistId);
	}

	private void removeCrosslistFromSections(List<BannerSection> sections){
        // declare the variables
        BannerSection aSection;
		Iterator<BannerSection> iBS;
        
		for(iBS = sections.iterator(); iBS.hasNext();){
			aSection = (BannerSection) iBS.next();
			if (aSection.getCrossListIdentifier() != null){
				updateCrosslistIdForBannerSectionIfNecessaryAndSave(aSection, null);
				if (aSection.getCrn() != null){
					SendBannerMessage.sendBannerMessage(aSection, BannerMessageAction.UPDATE, hibSession);
				}
			}
		}
	}

	public void updateCrosslists(){
		Debug.info("Manage crosslisting for an offering");

		// ensure all associated classes have at least one banner section if not create them
		try {
			ensureAllClassesHaveBannerSection();
		} catch (Exception e) {
			// TODO decide how to handle a failure to ensure all classes have at least one banner section
			e.printStackTrace();
		}

		// manage cross-listing for this instructional offering.
		if (instructionalOffering.getCourseOfferings().size() > 1){
			doBasicCrossListing();
		} else {
			removeCrosslistFromSections(BannerSection.findBannerSectionsForInstructionalOffering(instructionalOffering, hibSession));
		}
	}
	
}
