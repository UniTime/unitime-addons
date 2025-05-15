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
package org.unitime.banner.server.offerings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.Vector;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerConfigDAO;
import org.unitime.banner.model.dao.BannerSectionDAO;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.banner.BannerConfigEditPage.BannerConfigEditRequest;
import org.unitime.timetable.gwt.banner.BannerConfigEditPage.BannerConfigEditResponse;
import org.unitime.timetable.gwt.banner.BannerConfigEditPage.BannerSectionInterface;
import org.unitime.timetable.gwt.client.instructor.survey.InstructorSurveyInterface.IdLabel;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.ItypeDescDAO;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.service.AssignmentService;

@GwtRpcImplements(BannerConfigEditRequest.class)
public class BannerConfigEditBackend implements GwtRpcImplementation<BannerConfigEditRequest, BannerConfigEditResponse>{
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;

	@Override
	public BannerConfigEditResponse execute(BannerConfigEditRequest request, SessionContext context) {
		org.hibernate.Session hibSession = BannerConfigDAO.getInstance().getSession();
		BannerConfig bc = BannerConfigDAO.getInstance().get(request.getBannerConfigId());
		InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(bc.getInstrOfferingConfigId());
		context.checkPermission(ioc, Right.MultipleClassSetup);
		InstructionalOffering io = ioc.getInstructionalOffering();
		CourseOffering co = CourseOfferingDAO.getInstance().get(bc.getBannerCourse().getCourseOfferingId());
		BannerSession bsess = BannerSession.findBannerSessionForSession(io.getSession(), hibSession);
		
		if (request.getData() != null) {
			Transaction tx = null;

	        try {
		        tx = hibSession.beginTransaction();
		        
		        if (request.getData().hasSections()) {
		        	for (BannerSectionInterface section: request.getData().getSections()) {
			        	String bannerSectionIndex = section.getSection();
			        	if (bannerSectionIndex != null) bannerSectionIndex = bannerSectionIndex.toUpperCase();
			        	if (bannerSectionIndex != null && bannerSectionIndex.isEmpty()) bannerSectionIndex = null;
			        	if (bannerSectionIndex != null && !request.getData().hasOldSection(bannerSectionIndex) &&
			        			!BannerSection.isSectionIndexUniqueForCourse(co.getInstructionalOffering().getSession(), co, hibSession, bannerSectionIndex)) {
			        		throw new GwtRpcException(BMSG.errorSectionNewIndexNotUnique(section.getLabel()));
			        	}
					}
		        }
		        
		        bc.setGradableItype(request.getData().getGradableItypeId() == null ? null : ItypeDescDAO.getInstance().get(request.getData().getGradableItypeId().intValue()));
		        if (BannerSection.displayLabHours())
		        	bc.setLabHours(request.getData().getLabHours());
		        hibSession.merge(bc);
		        
		        if (request.getData().hasSections())
			        for (BannerSectionInterface section: request.getData().getSections()) {
			        	BannerSection bs = BannerSectionDAO.getInstance().get(section.getId());
			        	boolean changed = false;
			        	
			        	String bannerSectionIndex = section.getSection();
			        	if (bannerSectionIndex != null) bannerSectionIndex = bannerSectionIndex.toUpperCase();
			        	if (bannerSectionIndex != null && bannerSectionIndex.isEmpty()) bannerSectionIndex = null;
						if ((bannerSectionIndex != null && !bannerSectionIndex.equals(bs.getSectionIndex())) || (bannerSectionIndex == null && bs.getSectionIndex() != null)){
							bs.setSectionIndex(bannerSectionIndex);
							bs.updateClassSuffixForClassesIfNecessary(hibSession);
							changed = true;
						}

						Long consentId = section.getConsentId();
						OfferingConsentType oct = bs.effectiveConsentType(); 
						if ((consentId == null &&  bs.getConsentType() != null)
								||  (consentId != null && oct == null)
								|| (consentId != null && !consentId.equals(oct.getUniqueId()))){
							OfferingConsentType newOct = null;
							if (consentId != null){
								newOct = OfferingConsentTypeDAO.getInstance().get(consentId);
							}
							bs.setConsentType(newOct);
							changed = true;
						}
						
						Float credit = section.getCreditOverride();
						if ((bs.getOverrideCourseCredit() != null && credit == null) ||
							(bs.getOverrideCourseCredit() == null && credit != null) ||
							(bs.getOverrideCourseCredit()!= null && credit != null && !bs.getOverrideCourseCredit().equals(credit))){
							bs.setOverrideCourseCredit(credit);
							changed = true;
						}

						if (request.getData().isShowLimitOverrides()) {
							Integer limit = section.getLimitOverride();
							if ((bs.getOverrideLimit() != null && limit == null) ||
								(bs.getOverrideLimit() == null && limit != null) ||
								(bs.getOverrideLimit() != null && limit != null && !bs.getOverrideLimit().equals(limit))){
								if (limit != null && limit > bs.calculateMaxEnrl(null)){
									limit = null;
								}
								bs.setOverrideLimit(limit);
								changed = true;
							}
						}
						
						Long campusOverrideId = section.getCampusId();
						BannerCampusOverride newCmp = null;
						if ((campusOverrideId != null && bs.getBannerCampusOverride() != null && !bs.getBannerCampusOverride().getUniqueId().equals(campusOverrideId)) ||
							(campusOverrideId != null && bs.getBannerCampusOverride() == null) ||
							(campusOverrideId == null && bs.getBannerCampusOverride() != null)) {
							if (campusOverrideId != null) {
								newCmp = BannerCampusOverride.getBannerCampusOverrideById(campusOverrideId);
							}
							bs.setBannerCampusOverride(newCmp);
							changed = true;
						} 

						if (changed)
							hibSession.merge(bs);
			        }
		        
		        ChangeLog.addChange(
	                    hibSession,
	                    context,
	                    ioc,
	                    ChangeLog.Source.CLASS_SETUP,
	                    ChangeLog.Operation.UPDATE,
	                    ioc.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
	                    null);

	            tx.commit();
		        hibSession.flush();
		        if (bc.getBannerSections() != null){
		        	Vector<BannerSection> list = new Vector<BannerSection>();
		        	list.addAll(bc.getBannerSections());
		        	SendBannerMessage.sendBannerMessage(list, BannerMessageAction.UPDATE, hibSession);
		        }
	        } catch (Exception e) {
	            Debug.error(e);
	            try {
		            if(tx!=null && tx.isActive())
		                tx.rollback();
	            }
	            catch (Exception e1) { }
	            throw e;
	        }
			
			return null;
		}

		BannerConfigEditResponse response = new BannerConfigEditResponse();
		response.setBannerConfigId(bc.getUniqueId());
		response.setConfigId(ioc.getUniqueId());
		response.setBannerCourseId(bc.getBannerCourse().getUniqueId());
		response.setConfigName(ioc.getCourseNameWithTitle() + (ioc.getInstructionalOffering().hasMultipleConfigurations() ? " [" + ioc.getName() + "]" : ""));
		for (OfferingConsentType t: OfferingConsentType.getConsentTypeList())
			response.addConsent(new IdLabel(t.getUniqueId(), t.getLabel(), t.getReference()));
		for (BannerCampusOverride o: BannerCampusOverride.getBannerCampusOverrideList())
			response.addCampusOverride(new IdLabel(o.getUniqueId(), o.getBannerCampusCode() + " - " + o.getBannerCampusName(), o.isVisible() ? "1" : "0"));
		for (ItypeDesc i: ItypeDescDAO.getInstance().getSession().createQuery(
	    		 "select distinct it from ItypeDesc it, BannerConfig bc, SchedulingSubpart ss where bc.uniqueId = :configId and ss.instrOfferingConfig.uniqueId = bc.instrOfferingConfigId and it.itype = ss.itype.itype",
	    		 ItypeDesc.class).setParameter("configId", request.getBannerConfigId()).setCacheable(true).list())
			response.addGradableItype(new IdLabel(Long.valueOf(i.getItype()), i.getDesc(), i.getAbbv()));
		
		ArrayList<SchedulingSubpart> subpartList = new ArrayList(ioc.getSchedulingSubparts());
        Collections.sort(subpartList, new SchedulingSubpartComparator());
        for(Iterator<SchedulingSubpart> it = subpartList.iterator(); it.hasNext();){
        	SchedulingSubpart ss = (SchedulingSubpart) it.next();
    		if (ss.getClasses() == null || ss.getClasses().size() == 0)
    			throw new GwtRpcException("Initial setup of Instructional Offering Config has not been completed.");
    		if (ss.getParentSubpart() == null)
        		loadClasses(response, context, bsess, bc.getBannerCourse(), ss.getClasses(), 0, 0);
        }
		
    	boolean canShowLimitOverridesIfNeeded = (ApplicationProperties.getProperty("tmtbl.banner.section.limit.overrides_allowed", "true").equalsIgnoreCase("true"));
        response.setShowLimitOverrides(canShowLimitOverridesIfNeeded && ioc.getInstructionalOffering().getCourseOfferings().size() > 1);
        response.setShowLabHours(BannerSection.displayLabHours());
        response.setLabHours(bc.getLabHours());
        response.setGradableItypeId(bc.getGradableItype() == null ? null : Long.valueOf(bc.getGradableItype().getItype()));
		return response;
	}
	
    protected void loadClasses(BannerConfigEditResponse response, SessionContext context, BannerSession bsess, BannerCourse bc, Set<Class_> classes, int indent, Integer previousItype) {
    	ClassAssignmentProxy ca = classAssignmentService.getAssignment();
    	String nameFormat = UserProperty.NameFormat.get(context.getUser());
    	if (classes != null && classes.size() > 0){
    		ArrayList<Class_> classesList = new ArrayList<Class_>(classes);
            Collections.sort(classesList, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE) );
	    	for(Class_ cls : classesList) {
	    		if (cls.isCancelled()) continue;
	    		if (previousItype == null || !previousItype.equals(cls.getSchedulingSubpart().getItype().getItype())){
		    		BannerSection bs = BannerSection.findBannerSectionForBannerCourseAndClass(bc, cls);
		    		BannerSectionInterface section = new BannerSectionInterface();
		    		section.setId(bs.getUniqueId());
		    		section.setIndent(indent);
		    		section.setLabel(bs.bannerSectionLabel());
		    		section.setSection(bs.getSectionIndex());
		    		section.setOldSection(bs.getSectionIndex());
		    		section.setItype(cls.getSchedulingSubpart().getItype().getSis_ref());
		    		section.setConsentId(bs.effectiveConsentType() == null ? null : bs.effectiveConsentType().getUniqueId());
		    		section.setEditable(context.hasPermission(cls, Right.MultipleClassSetupClass));
		    		section.setDate(bs.buildDatePatternCell(ca));
		    		section.setTime(bs.buildAssignedTimeCell(ca));
		    		section.setRoom(bs.buildAssignedRoomCell(ca));
		    		section.setInstructor(bs.buildInstructorCell(nameFormat));
		    		section.setLimitOverride(bs.getOverrideLimit());
		    		section.setLimit(bs.maxEnrollBasedOnClasses(null));
		    		section.setDefaultCampusCode(bs.getDefaultCampusCode(bsess, cls));
		    		section.setCampusId(bs.getBannerCampusOverride() == null ? null : bs.getBannerCampusOverride().getUniqueId());
		    		section.setCredit(bs.courseCreditStringBasedOnClass(cls));
		    		section.setCreditOverride(bs.getOverrideCourseCredit());
		    		response.addSection(section);
		    	}
	    		loadClasses(response, context, bsess, bc, cls.getChildClasses(), indent + ((previousItype == null || !previousItype.equals(cls.getSchedulingSubpart().getItype().getItype())) ? 1 : 0),
	    				cls.getSchedulingSubpart().getItype().getItype());
	    	}
    	}
    }

}
