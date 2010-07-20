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

import java.util.Iterator;

import org.dom4j.Element;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.banner.model.BannerSession;
import org.unitime.commons.Debug;
import org.unitime.timetable.dataexchange.BaseCourseOfferingImport;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.dao.Class_DAO;


/**
 * @author says
 *
 */
public class BannerCourseOfferingImport extends BaseCourseOfferingImport {

//    static {
//        sImportRegister.put("banner_offerings", BannerCourseOfferingImport.class);
//    }

	/**
	 * 
	 */
	public BannerCourseOfferingImport() {
		super();
		rootElementName = "bannerOfferings";
	}
	
	@Override
	protected boolean handleCustomCourseChildElements(CourseOffering courseOffering,
		Element courseOfferingElement) throws Exception {
 		boolean changed = false;
		
		BannerCourse bc = BannerCourse.findBannerCourseForCourseOffering(courseOffering.getUniqueId(), getHibSession());
		if(bc == null) {
			bc = new BannerCourse();
			bc.setCourseOfferingId(courseOffering.getUniqueId());
			Session newSession = Class_DAO.getInstance().createNewSession();
			Transaction trans = newSession.beginTransaction();
			try {
				newSession.save(bc);
				trans.commit();
			} catch (Exception e) {
				e.printStackTrace();
			} finally {
				newSession.close();
			}
			
			
			
			addNote(courseOfferingLabelString(courseOffering) + ":  Added Banner Course");
			changed = true;
		}
		
		Element bannerCourseElement = courseOfferingElement.element("bannerCourse");
        if (bannerCourseElement == null){
			addNote("Course offering: " + courseOfferingLabelString(courseOffering) + " no 'bannerCourse' element.");
			Debug.info("Course offering: " + courseOfferingLabelString(courseOffering) + " no 'bannerCourse' element.");
			return(changed);
        }		
		
        return(changed);
	}
	@Override

	protected boolean handleCustomInstrOffrConfigChildElements(InstrOfferingConfig instrOfferingConfig,
		Element instrOfferingConfigElement) throws Exception {
		boolean changed = ensureBannerConfigsExistForInstrOfferingConfig(instrOfferingConfig);
		Element bannerConfigElement = instrOfferingConfigElement.element("bannerConfig");
        if (bannerConfigElement == null){
			addNote("Course offering: " + courseOfferingLabelString(instrOfferingConfig.getControllingCourseOffering()) + " no 'bannerConfig' element.");
			Debug.info("Course offering: " + courseOfferingLabelString(instrOfferingConfig.getControllingCourseOffering()) + " no 'bannerConfig' element.");
			return(changed);
        }
        String gradableItypeStr = getOptionalStringAttribute(bannerConfigElement, "gradableType");
        ItypeDesc itype = null;
        if (gradableItypeStr != null){
        	itype = findItypeFromBannerReference(gradableItypeStr);
        }
        if (itype == null && gradableItypeStr != null){
			throw new Exception("Course offering: " + courseOfferingLabelString(instrOfferingConfig.getControllingCourseOffering()) + " in 'bannerConfig' element could not find matching itype for: " + gradableItypeStr +".");
		}
		for (Iterator coIt = instrOfferingConfig.getInstructionalOffering().getCourseOfferings().iterator(); coIt.hasNext();){
			CourseOffering co = (CourseOffering) coIt.next();
			BannerConfig bc = BannerConfig.findBannerConfigForInstrOffrConfigAndCourseOffering(instrOfferingConfig, co, getHibSession());
			if (bc.getGradableItype() == null && itype != null){
				bc.setGradableItype(itype);
				addNote(courseOfferingLabelString(instrOfferingConfig.getControllingCourseOffering()) + ":  Gradable itype set in Banner Course");
				changed = true;
			} else if (bc.getGradableItype() != null && !bc.getGradableItype().getItype().equals(itype.getItype())) {
				bc.setGradableItype(itype);
				addNote(courseOfferingLabelString(instrOfferingConfig.getControllingCourseOffering()) + ":  Gradable itype changed in Banner Course");
				changed = true;
			} else if (gradableItypeStr == null){
				if (bc.getGradableItype() != null){
					bc.setGradableItype(null);
					addNote(courseOfferingLabelString(instrOfferingConfig.getControllingCourseOffering()) + ":  Gradable itype removed from Banner Course");
					changed = true;
				}
			}
			if (changed){
				getHibSession().saveOrUpdate(bc);
			}
		} 
		
        return(changed);
	}
	
	private String courseOfferingLabelString(CourseOffering courseOffering){
		StringBuilder sb = new StringBuilder();
		sb.append(courseOffering.getSubjectArea().getSubjectAreaAbbreviation())
		  .append(" ")
		  .append(courseOffering.getCourseNbr())
		  .append(" - ")
		  .append(courseOffering.getTitle());
		return(sb.toString());
	}

	private String classLabelString(Class_ clazz){
		StringBuilder sb = new StringBuilder();
		sb.append(courseOfferingLabelString(clazz.getSchedulingSubpart().getControllingCourseOffering()));
		sb.append(" ")
		  .append(clazz.getSchedulingSubpart().getItype().getAbbv())
		  .append(" ")
		  .append(clazz.getSectionNumber().toString());
		return(sb.toString());
	}

	private ItypeDesc findItypeFromBannerReference(String bannerReference){
		return((ItypeDesc) getHibSession().createQuery("from ItypeDesc i where i.sis_ref = :bannerRef").setString("bannerRef", bannerReference).setCacheable(true).setFlushMode(FlushMode.MANUAL).uniqueResult());
	}

	private boolean ensureBannerConfigsExistForInstrOfferingConfig(InstrOfferingConfig instrOfferingConfig){
		boolean changed = false;
		for(Iterator it = instrOfferingConfig.getInstructionalOffering().getCourseOfferings().iterator(); it.hasNext();){
			CourseOffering co = (CourseOffering) it.next();
			BannerConfig bcfg = BannerConfig.findBannerConfigForInstrOffrConfigAndCourseOffering(instrOfferingConfig, co, getHibSession());
			if (bcfg == null){
				Session newSession = Class_DAO.getInstance().createNewSession();
				Transaction trans = newSession.beginTransaction();
				try {
					bcfg = new BannerConfig();
					BannerCourse bc = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), newSession);
					bcfg.setBannerCourse(bc);
					bc.addTobannerConfigs(bcfg);
					bcfg.setInstrOfferingConfigId(instrOfferingConfig.getUniqueId());
					newSession.update(bc);
					trans.commit();
					
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					newSession.close();
				}
				changed = true;
			}
		}
		return(changed);
	}
	
	private boolean ensureBannerSectionsExistForClass(Class_ clazz){
		boolean changed = false;
		for(Iterator it = clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().iterator(); it.hasNext();){
			CourseOffering co = (CourseOffering) it.next();
			BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOffering(clazz, co, getHibSession());
			if (bs == null){
				Session newSession = Class_DAO.getInstance().createNewSession();
				if (clazz.getParentClass() != null && clazz.getParentClass().getSchedulingSubpart().getItype().getItype().equals(clazz.getSchedulingSubpart().getItype().getItype())){
					bs = BannerSection.findBannerSectionForClassAndCourseOffering(clazz.getParentClass(), co, newSession);
				}
				Transaction trans = newSession.beginTransaction();
				try {
					if (bs == null){
						bs = new BannerSection();
						BannerSectionToClass bsc = new BannerSectionToClass();
						bsc.setClassId(clazz.getUniqueId());
						bsc.setBannerSection(bs);
						bs.addTobannerSectionToClasses(bsc);
						BannerConfig bc = BannerConfig.findBannerConfigForInstrOffrConfigAndCourseOffering(clazz.getSchedulingSubpart().getInstrOfferingConfig(), co, newSession);
						bs.setBannerConfig(bc);
						bc.addTobannerSections(bs);
						bs.setSession(session);
						newSession.save(bs);
						addNote("added class to existing banner section");
					} else {
						bs.addClass(clazz, getHibSession());
						newSession.update(bs);
						addNote("banner section added");
					}
					trans.commit();	
					newSession.flush();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					newSession.close();					
				}
				changed = true;
			}
		}
		
		return(changed);
	}
	@Override
	protected boolean handleCustomClassChildElements(Element classElement,
			InstrOfferingConfig ioc, Class_ clazz) throws Exception {
		boolean changed = ensureBannerSectionsExistForClass(clazz);
		String elementName = "section";
		if(classElement.element(elementName) != null) {
			for (Iterator<?> it = classElement.elementIterator(elementName); it.hasNext();){
				Element sectionElement = (Element) it.next();
				Integer crn = getRequiredIntegerAttribute(sectionElement, "crn", elementName);
				String sectionId = getRequiredStringAttribute(sectionElement, "id", elementName);
				String courseExternalId = getRequiredStringAttribute(sectionElement, "courseId", elementName);
				String crossListId = getOptionalStringAttribute(sectionElement, "xlst");
				String linkId = getOptionalStringAttribute(sectionElement, "linkIdent");
				String linkConnector = getOptionalStringAttribute(sectionElement, "linkConn");
				BannerSection bs = BannerSection.findBannerSectionForClassAndCourseExternalId(clazz, courseExternalId, getHibSession(), session);
				if (bs == null){
						throw new Exception("Class: " + classLabelString(clazz) + " matching Banner Section for course external id: " + courseExternalId + " does not exist.");
				}
				if (bs.getCrn() == null || !bs.getCrn().equals(crn)){
					bs.setCrn(crn);
					changed = true;
					addNote("Class: " + classLabelString(clazz) + "banner crn changed.");
				}
				if (bs.getSectionIndex() == null || !bs.getSectionIndex().equals(sectionId)){
					bs.setSectionIndex(sectionId);
					changed = true;
					addNote("Class: " + classLabelString(clazz) + "banner section identifier changed");
				}
				if ((bs.getCrossListIdentifier() == null && crossListId != null) ||
				    (bs.getCrossListIdentifier() != null && crossListId == null) ||
				    (bs.getCrossListIdentifier() != null && !bs.getCrossListIdentifier().equals(crossListId))){
					bs.setCrossListIdentifier(crossListId);
					changed = true;
					addNote("Class: " + classLabelString(clazz) + "banner cross list identifier changed");
				}
				if ((bs.getLinkIdentifier() == null && linkId != null) ||
					    (bs.getLinkIdentifier() != null && linkId == null) ||
					    (bs.getLinkIdentifier() != null && !bs.getLinkIdentifier().equals(linkId))){
					bs.setLinkIdentifier(linkId);
					changed = true;
					addNote("Class: " + classLabelString(clazz) + "banner link identifier changed");
				}
				if ((bs.getLinkConnector() == null && linkConnector != null) ||
					    (bs.getLinkConnector() != null && linkConnector == null) ||
					    (bs.getLinkConnector() != null && !bs.getLinkConnector().equals(linkConnector))){
					bs.setLinkConnector(linkConnector);
					changed = true;
					addNote("Class: " + classLabelString(clazz) + "banner link connector changed");
				}
				if (elementSectionConsent(sectionElement, bs)){
					changed = true;
				}
				getHibSession().update(bs);
			}
		}
		return(changed);
	}
	
	private boolean elementSectionConsent(Element element, BannerSection bs) throws Exception{
		boolean changed = false;
		Element consentElement = element.element("consent");
		if (consentElement != null){
			String consentType = getRequiredStringAttribute(consentElement, "type", "consent");
			if (bs.getConsentType() == null || !bs.getConsentType().getReference().equals(consentType)){
				bs.setConsentType(OfferingConsentType.getOfferingConsentTypeForReference(consentType));				
				changed = true;
				addNote("\tconsent changed");
			} 
		}
		return(changed);
	}

	@Override
	protected void postLoadAction() {
		beginTransaction();
		BannerSession bs = BannerSession.findBannerSessionForSession(session, getHibSession());
		bs.setLoadingOfferingsFile(new Boolean(false));
		getHibSession().update(bs);
		flush(true);
	}

	@Override
	protected void preLoadAction() {
		beginTransaction();
		BannerSession bs = BannerSession.findBannerSessionForSession(session, getHibSession());
		bs.setLoadingOfferingsFile(new Boolean(true));
		getHibSession().update(bs);
		flush(true);
	}
	
}
