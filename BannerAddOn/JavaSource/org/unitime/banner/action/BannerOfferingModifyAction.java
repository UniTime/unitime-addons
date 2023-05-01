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
package org.unitime.banner.action;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.form.BannerOfferingModifyForm;
import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerConfigDAO;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.banner.model.dao.BannerSectionDAO;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.DepartmentStatusType;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.ItypeDescDAO;
import org.unitime.timetable.model.dao.OfferingConsentTypeDAO;
import org.unitime.timetable.security.permissions.Permission.PermissionDepartment;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.spring.SpringApplicationContextHolder;
import org.unitime.timetable.util.LookupTables;


/**
 * @author Stephanie Schluttenhofer
 */
@Action(value = "bannerOfferingModify", results = {
		@Result(name = "bannerOfferingModify", type = "tiles", location = "bannerOfferingModify.tiles"),
		@Result(name = "bannerOfferingDetail", type = "redirect", location = "/bannerOfferingDetail.action",
			params = { "io", "${form.instrOfferingId}", "bc", "${form.bannerCourseOfferingId}", "op", "view"})
	})
@TilesDefinition(name = "bannerOfferingModify.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Banner Offering Edit"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerOfferingModify.jsp"),
		@TilesPutAttribute(name = "showNavigation", value = "false"),
		@TilesPutAttribute(name = "showSolverWarnings", value = "assignment")
	})
public class BannerOfferingModifyAction extends UniTimeAction<BannerOfferingModifyForm> {
	private static final long serialVersionUID = 1777414232394219529L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);

	private Long instrOffrConfigId = null;
	private Long bannerCourseOfferingId = null;
	
	public Long getUid() { return instrOffrConfigId; }
	public void setUid(Long instrOffrConfigId) { this.instrOffrConfigId = instrOffrConfigId; }
	public Long getBc() { return bannerCourseOfferingId; }
	public void setBc(Long bannerCourseOfferingId) { this.bannerCourseOfferingId = bannerCourseOfferingId; }

	@Override
    public String execute() throws Exception {
		if (form == null) form = new BannerOfferingModifyForm();

		LookupTables.setupConsentType(request);
		form.setBannerCampusOverrides(BannerCampusOverride.getBannerCampusOverrideList());
       // Get operation
		if (op == null) op = form.getOp();

        if(op==null || op.trim().length()==0)
            throw new Exception (MSG.errorOperationNotInterpreted() + op);

        // Set up Lists
        form.setOp(op);
        
        if (op.equals(BMSG.actionBackToBannerOfferingDetail())) {
        	return "bannerOfferingDetail";
        }

        // First access to screen
        if (op.equalsIgnoreCase("edit") || op.equals(BMSG.actionEditBannerConfig())) {
            doLoad(instrOffrConfigId, bannerCourseOfferingId);
            setupItypeChoices();
        }

		LookupTables.setupExternalDepts(request, sessionContext.getUser().getCurrentAcademicSessionId());
		Department contrDept = InstrOfferingConfigDAO.getInstance().get(form.getInstrOffrConfigId()).getInstructionalOffering().getControllingCourseOffering().getSubjectArea().getDepartment();
		TreeSet<Department> ts = new TreeSet<Department>();
		for (@SuppressWarnings("unchecked")
		Iterator<Department> it = ((TreeSet<Department>) request.getAttribute(Department.EXTERNAL_DEPT_ATTR_NAME)).iterator(); it.hasNext();){
			Department d = it.next();
			if (sessionContext.hasPermission(d, Right.MultipleClassSetupDepartment) &&
				getPermissionDepartment().check(sessionContext.getUser(), contrDept, DepartmentStatusType.Status.OwnerEdit, d, DepartmentStatusType.Status.ManagerEdit))
				ts.add(d);
		}
		request.setAttribute((Department.EXTERNAL_DEPT_ATTR_NAME + "list"), ts);
 
        // Update the classes
        if (op.equalsIgnoreCase(BMSG.actionUpdateBannerConfig())) {
            // Validate data input
            form.validate(this);
            setupItypeChoices();
            if (!hasFieldErrors()) {
                doUpdate();
                return "bannerOfferingDetail";
            }
        }

        return "bannerOfferingModify";
    }

    private void setupItypeChoices() {
	    request.setAttribute("availableItypes", ItypeDescDAO.getInstance().getSession().createQuery(
	    		 "select distinct it from ItypeDesc it, BannerConfig bc, SchedulingSubpart ss where bc.uniqueId = :configId and ss.instrOfferingConfig.uniqueId = bc.instrOfferingConfigId and it.itype = ss.itype.itype",
	    		 ItypeDesc.class).setParameter("configId", form.getBannerConfigId()).setCacheable(true).list());
	}

	/**
     * Loads the form with the classes that are part of the instructional offering config
     */
    private void doLoad(Long instrOffrConfigId, Long bannerCourseOfferingId) throws Exception {

		// Check uniqueid
        if(instrOffrConfigId==null)
            throw new Exception ("Missing Instructional Offering Config.");

		sessionContext.checkPermission(instrOffrConfigId, "InstrOfferingConfig", Right.MultipleClassSetup);

		// Check banner course uniqueid
        if(bannerCourseOfferingId==null)
            throw new Exception ("Missing Banner Course Offering.");

        // Load details
        InstrOfferingConfigDAO iocDao = new InstrOfferingConfigDAO();
        InstrOfferingConfig ioc = iocDao.get(instrOffrConfigId);
        InstructionalOffering io = ioc.getInstructionalOffering();
        
        BannerCourseDAO bcDao = new BannerCourseDAO();
        BannerCourse bc = bcDao.get(bannerCourseOfferingId);
        
        BannerConfig bannerConfig = BannerConfig.findBannerConfigForInstrOffrConfigAndCourseOffering(ioc, bc.getCourseOffering(bcDao.getSession()), bcDao.getSession());
        BannerSession bsess = BannerSession.findBannerSessionForSession(io.getSession(), bcDao.getSession());
        
        // Load form properties
        form.setInstrOffrConfigId(ioc.getUniqueId());
        form.setInstrOfferingId(io.getUniqueId());
        form.setBannerCourseOfferingId(bc.getUniqueId());
        form.setBannerConfigId(bannerConfig.getUniqueId());
        form.setItypeId(bannerConfig.getGradableItype() != null?bannerConfig.getGradableItype().getItype():null);
        form.setShowLabHours(BannerSection.displayLabHours());
        form.setLabHours(bannerConfig.getLabHours());
        form.setConfigIsEditable(sessionContext.hasPermission(ioc, Right.MultipleClassSetup));

        String name = bc.getCourseOffering(bcDao.getSession()).getCourseNameWithTitle();
        if (io.hasMultipleConfigurations()) {
        	name += " [" + ioc.getName() +"]";
        }
        form.setInstrOfferingName(name);

        if (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().size() == 0)
        	throw new Exception("Instructional Offering Config has not been defined.");

        @SuppressWarnings({ "rawtypes", "unchecked" })
		ArrayList<SchedulingSubpart> subpartList = new ArrayList(ioc.getSchedulingSubparts());
        
        Collections.sort(subpartList, new SchedulingSubpartComparator());
        ClassAssignmentProxy cap = getClassAssignmentService().getAssignment();
        for(Iterator<SchedulingSubpart> it = subpartList.iterator(); it.hasNext();){
        	SchedulingSubpart ss = (SchedulingSubpart) it.next();
    		if (ss.getClasses() == null || ss.getClasses().size() == 0)
    			throw new Exception("Initial setup of Instructional Offering Config has not been completed.");
    		if (ss.getParentSubpart() == null){
        		loadClasses(bsess, bc, ss.getClasses(), Boolean.valueOf(true), new String(), null, cap);
        	}
        }
      }


    private void loadClasses(BannerSession bsess, BannerCourse bc, Set<Class_> classes, Boolean isReadOnly, String indent, Integer previousItype, ClassAssignmentProxy classAssignmentProxy){
    	if (classes != null && classes.size() > 0){
    		ArrayList<Class_> classesList = new ArrayList<Class_>(classes);
            Collections.sort(classesList, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE) );
	    	Boolean readOnlyClass = Boolean.valueOf(false);
	    	boolean canShowLimitOverridesIfNeeded = (ApplicationProperties.getProperty("tmtbl.banner.section.limit.overrides_allowed", "true").equalsIgnoreCase("true"));
	    	for(Class_ cls : classesList){
	    		if (cls.isCancelled().booleanValue()){
	    			continue;
	    		}
	    		if (previousItype == null || !previousItype.equals(cls.getSchedulingSubpart().getItype().getItype())){
	    			if (!isReadOnly.booleanValue()){
		    			readOnlyClass = Boolean.valueOf(isReadOnly.booleanValue());
		    		} else {
		    			readOnlyClass = Boolean.valueOf(!sessionContext.hasPermission(cls, Right.MultipleClassSetupClass));
		    		}
		    		BannerSection bs = BannerSection.findBannerSectionForBannerCourseAndClass(bc, cls);
		    		form.addToBannerSections(bsess, bs, cls, classAssignmentProxy, readOnlyClass, indent, canShowLimitOverridesIfNeeded);
		    	}
	    		loadClasses(bsess, bc, cls.getChildClasses(), Boolean.valueOf(true), indent + ((previousItype == null || !previousItype.equals(cls.getSchedulingSubpart().getItype().getItype()))?"&nbsp;&nbsp;&nbsp;&nbsp;":""), cls.getSchedulingSubpart().getItype().getItype(), classAssignmentProxy);
	    	}
    	}
    }

    /**
     * Update the instructional offering config
     */
    @SuppressWarnings("unchecked")
	private void doUpdate()
    	throws Exception {

        // Get Instructional Offering Config
        InstrOfferingConfigDAO iocdao = new InstrOfferingConfigDAO();
        InstrOfferingConfig ioc = iocdao.get(form.getInstrOffrConfigId());
        Session hibSession = iocdao.getSession();

        BannerCourse bc = BannerCourseDAO.getInstance().get(form.getBannerCourseOfferingId());      
        BannerConfig bannerConfig = BannerConfigDAO.getInstance().get(form.getBannerConfigId());
  
		Transaction tx = null;

        try {
	        tx = hibSession.beginTransaction();

	        // If the banner offering config gradable itype has changed update it.
	        if ((form.getItypeId() == null && bannerConfig.getGradableItype() != null)  
	        		|| (form.getItypeId() != null && bannerConfig.getGradableItype() == null)
	        		|| (form.getItypeId() != null && !form.getItypeId().equals(bannerConfig.getGradableItype().getItype()))){
	        	ItypeDesc itype = null;
	        	if (form.getItypeId() != null) {
	        		itype = ItypeDescDAO.getInstance().get(form.getItypeId());
	        	}
	        	bannerConfig.setGradableItype(itype);
	        	hibSession.merge(bannerConfig);
	        }

	        // If the banner offering config gradable itype has changed update it.
	        if ((form.getLabHours() == null && bannerConfig.getLabHours() != null)  
	        		|| (form.getLabHours() != null && bannerConfig.getLabHours() == null)
	        		|| (form.getLabHours() != null && !form.getLabHours().equals(bannerConfig.getLabHours()))){
	        	
	        	bannerConfig.setLabHours(form.getLabHours());
	        	hibSession.merge(bannerConfig);
	        }

	        // For all changed classes, update them
	        modifyClasses(ioc, bc, hibSession);

            ChangeLog.addChange(
                    hibSession,
                    sessionContext,
                    ioc,
                    ChangeLog.Source.CLASS_SETUP,
                    ChangeLog.Operation.UPDATE,
                    ioc.getInstructionalOffering().getControllingCourseOffering().getSubjectArea(),
                    null);

            tx.commit();
	        hibSession.flush();
	        if (bannerConfig.getBannerSections() != null){
	        	Vector<BannerSection> list = new Vector<BannerSection>();
	        	list.addAll(bannerConfig.getBannerSections());
	        	SendBannerMessage.sendBannerMessage(list, BannerMessageAction.UPDATE, hibSession);
	        }
        }
        catch (Exception e) {
            Debug.error(e);
            try {
	            if(tx!=null && tx.isActive())
	                tx.rollback();
            }
            catch (Exception e1) { }
            throw e;
        }
    }

  
	private void modifyClasses(InstrOfferingConfig ioc, BannerCourse bc, Session hibSession){
		BannerSectionDAO bsDao = new BannerSectionDAO();
	
		Iterator<Long> it1 = form.getBannerSectionIds().listIterator();
		Iterator<String> it2 = form.getBannerSectionSectionIds().listIterator();
		Iterator<Long> it3 = form.getConsents().listIterator();
		Iterator<String> it4 = form.getCourseCreditOverrides().listIterator();
		Iterator<String> it5 = form.getLimitOverrides().listIterator();
		Iterator<Long> it6 = form.getCampusOverrides().listIterator();
		
		for(;it1.hasNext();){
			boolean changed = false;
			Long sectionId = it1.next();
			String bannerSectionIndex = it2.next();
			if (bannerSectionIndex != null){
				bannerSectionIndex = bannerSectionIndex.toUpperCase();
			}
			BannerSection bs = bsDao.get(sectionId);
			if ((bannerSectionIndex != null && !bannerSectionIndex.equals(bs.getSectionIndex())) || (bannerSectionIndex == null && bs.getSectionIndex() != null)){
				bs.setSectionIndex(bannerSectionIndex);
				bs.updateClassSuffixForClassesIfNecessary(bsDao.getSession());
				changed = true;
			}
			Long consentId = it3.next();
			if (consentId != null && consentId < 0) {
				consentId = null;
			}
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
			String creditStr = it4.next().toString();
			Float credit = null;
			if (creditStr != null && creditStr.trim().length() > 0){
				credit = Float.valueOf(creditStr);
			}
			if ((bs.getOverrideCourseCredit() != null && credit == null) ||
				(bs.getOverrideCourseCredit() == null && credit != null) ||
				(bs.getOverrideCourseCredit()!= null && credit != null && !bs.getOverrideCourseCredit().equals(credit))){
				bs.setOverrideCourseCredit(credit);
				changed = true;
			}
			if (form.getShowLimitOverride()){
				String limitStr = it5.next();
				Integer limit = null;
				if (limitStr != null && limitStr.trim().length() > 0){
					limit = Integer.valueOf(limitStr);
				}
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
			Long campusOverrideId = it6.next();
			if (campusOverrideId != null && campusOverrideId < 0) {
				campusOverrideId = null;
			}
			BannerCampusOverride newCmp = null;
			if ((campusOverrideId != null && bs.getBannerCampusOverride() != null && !bs.getBannerCampusOverride().getUniqueId().equals(campusOverrideId)) ||
				(campusOverrideId != null && bs.getBannerCampusOverride() == null) ||
				(campusOverrideId == null && bs.getBannerCampusOverride() != null))
			{
				if (campusOverrideId != null) {
					newCmp = BannerCampusOverride.getBannerCampusOverrideById(campusOverrideId);
				}
				bs.setBannerCampusOverride(newCmp);
				changed = true;
			} 
			if (changed){
				hibSession.merge(bs);
			}
		}
    }

	protected PermissionDepartment getPermissionDepartment() {
    	return (PermissionDepartment)SpringApplicationContextHolder.getBean("permissionDepartment");
    }
}
