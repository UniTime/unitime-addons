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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.Department;
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
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.WebSolver;
import org.unitime.timetable.util.LookupTables;


/**
 * @author Stephanie Schluttenhofer
 */
@Service("/bannerOfferingModify")
public class BannerOfferingModifyAction extends Action {

	@Autowired SessionContext sessionContext;

	/**
     * Method execute
     * @param mapping
     * @param form
     * @param request
     * @param response
     * @return ActionForward
     */
    public ActionForward execute(
        ActionMapping mapping,
        ActionForm form,
        HttpServletRequest request,
        HttpServletResponse response) throws Exception {
    	
		LookupTables.setupExternalDepts(request, sessionContext.getUser().getCurrentAcademicSessionId());
		TreeSet<Department> ts = new TreeSet<Department>();
		for (@SuppressWarnings("unchecked")
		Iterator<Department> it = ((TreeSet<Department>) request.getAttribute(Department.EXTERNAL_DEPT_ATTR_NAME)).iterator(); it.hasNext();){
			Department d = it.next();
			if (sessionContext.hasPermission(d, Right.MultipleClassSetupDepartment))
				ts.add(d);
		}
		request.setAttribute((Department.EXTERNAL_DEPT_ATTR_NAME + "list"), ts);

        MessageResources rsc = getResources(request);
        BannerOfferingModifyForm frm = (BannerOfferingModifyForm) form;
		LookupTables.setupConsentType(request);
		frm.setBannerCampusOverrides(BannerCampusOverride.getBannerCampusOverrideList());
       // Get operation
        String op = (request.getParameter("op")==null)
						? (frm.getOp()==null || frm.getOp().length()==0)
						        ? (request.getAttribute("op")==null)
						                ? null
						                : request.getAttribute("op").toString()
						        : frm.getOp()
						: request.getParameter("op");

        if(op==null)
            op = request.getParameter("hdnOp");

        if(op==null || op.trim().length()==0)
            throw new Exception ("Operation could not be interpreted: " + op);

        // Instructional Offering Config Id
        String instrOffrConfigId = "";
        String bannerCourseOfferingId = "";

        // Set up Lists
        frm.setOp(op);

        // First access to screen
        if(op.equalsIgnoreCase(rsc.getMessage("button.edit"))) {

        	instrOffrConfigId = (request.getParameter("uid")==null)
								? (request.getAttribute("uid")==null)
								        ? null
								        : request.getAttribute("uid").toString()
								: request.getParameter("uid");
			bannerCourseOfferingId = (request.getParameter("bc")==null)
										? (request.getAttribute("bc")==null)
										        ? null
										        : request.getAttribute("bc").toString()
										: request.getParameter("bc");

            doLoad(request, frm, instrOffrConfigId, bannerCourseOfferingId);
            setupItypeChoices(request, frm);
        }


 
 
        // Update the classes
        if(op.equalsIgnoreCase(rsc.getMessage("button.update"))) {
            // Validate data input
            ActionMessages errors = frm.validate(mapping, request);
            setupItypeChoices(request, frm);
            if(errors.size()==0) {
                doUpdate(request, frm);
                request.setAttribute("io", frm.getInstrOfferingId());
                request.setAttribute("bc", frm.getBannerCourseOfferingId());
                return mapping.findForward("bannerOfferingDetail");
            }
            else {
                saveErrors(request, errors);
            }
        }

        return mapping.findForward("bannerOfferingModify");
    }

    private void setupItypeChoices(HttpServletRequest request, BannerOfferingModifyForm form) {
    	ItypeDescDAO itDao = ItypeDescDAO.getInstance();
    	String qs = "select distinct it from ItypeDesc it, BannerConfig bc, SchedulingSubpart ss where bc.uniqueId = :configId and ss.instrOfferingConfig.uniqueId = bc.instrOfferingConfigId and it.itype = ss.itype.itype";
	    request.setAttribute("availableItypes", itDao.getQuery(qs).setLong("configId", form.getBannerConfigId().longValue()).setCacheable(true).list());
	}

	/**
     * Loads the form with the classes that are part of the instructional offering config
     * @param frm Form object
     * @param instrCoffrConfigId Instructional Offering Config Id
     * @param user User object
     */
    private void doLoad(
    		HttpServletRequest request,
    		BannerOfferingModifyForm frm,
            String instrOffrConfigId,
            String bannerCourseOfferingId
            ) throws Exception {

		// Check uniqueid
        if(instrOffrConfigId==null || instrOffrConfigId.trim().length()==0)
            throw new Exception ("Missing Instructional Offering Config.");

		sessionContext.checkPermission(instrOffrConfigId, "InstrOfferingConfig", Right.MultipleClassSetup);

		// Check banner course uniqueid
        if(bannerCourseOfferingId==null || bannerCourseOfferingId.trim().length()==0)
            throw new Exception ("Missing Banner Course Offering.");

        // Load details
        InstrOfferingConfigDAO iocDao = new InstrOfferingConfigDAO();
        InstrOfferingConfig ioc = iocDao.get(Long.valueOf(instrOffrConfigId));
        InstructionalOffering io = ioc.getInstructionalOffering();
        
        BannerCourseDAO bcDao = new BannerCourseDAO();
        BannerCourse bc = bcDao.get(Long.valueOf(bannerCourseOfferingId));
        
        BannerConfig bannerConfig = BannerConfig.findBannerConfigForInstrOffrConfigAndCourseOffering(ioc, bc.getCourseOffering(bcDao.getSession()), bcDao.getSession());
        BannerSession bsess = BannerSession.findBannerSessionForSession(io.getSession(), bcDao.getSession());
        
        // Load form properties
        frm.setInstrOffrConfigId(ioc.getUniqueId());
        frm.setInstrOfferingId(io.getUniqueId());
        frm.setBannerCourseOfferingId(bc.getUniqueId());
        frm.setBannerConfigId(bannerConfig.getUniqueId());
        frm.setItypeId(bannerConfig.getGradableItype() != null?bannerConfig.getGradableItype().getItype():null);
        frm.setConfigIsEditable(sessionContext.hasPermission(ioc, Right.MultipleClassSetup));

        String name = bc.getCourseOffering(bcDao.getSession()).getCourseNameWithTitle();
        if (io.hasMultipleConfigurations()) {
        	name += " [" + ioc.getName() +"]";
        }
        frm.setInstrOfferingName(name);

        if (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().size() == 0)
        	throw new Exception("Instructional Offering Config has not been defined.");

        @SuppressWarnings({ "rawtypes", "unchecked" })
		ArrayList<SchedulingSubpart> subpartList = new ArrayList(ioc.getSchedulingSubparts());
        
        Collections.sort(subpartList, new SchedulingSubpartComparator());
        ClassAssignmentProxy cap = WebSolver.getClassAssignmentProxy(request.getSession());
        for(Iterator<SchedulingSubpart> it = subpartList.iterator(); it.hasNext();){
        	SchedulingSubpart ss = (SchedulingSubpart) it.next();
    		if (ss.getClasses() == null || ss.getClasses().size() == 0)
    			throw new Exception("Initial setup of Instructional Offering Config has not been completed.");
    		if (ss.getParentSubpart() == null){
        		loadClasses(frm, bsess, bc, ss.getClasses(), new Boolean(true), new String(), null, cap);
        	}
        }
      }


    private void loadClasses(BannerOfferingModifyForm frm, BannerSession bsess, BannerCourse bc, Set classes, Boolean isReadOnly, String indent, Integer previousItype, ClassAssignmentProxy classAssignmentProxy){
    	if (classes != null && classes.size() > 0){
    		ArrayList classesList = new ArrayList(classes);
            Collections.sort(classesList, new ClassComparator(ClassComparator.COMPARE_BY_ITYPE) );
	    	Boolean readOnlyClass = new Boolean(false);
	    	Class_ cls = null;
	    	for(Iterator it = classesList.iterator(); it.hasNext();){
	    		cls = (Class_) it.next();
	    		if (previousItype == null || !previousItype.equals(cls.getSchedulingSubpart().getItype().getItype())){
	    			if (!isReadOnly.booleanValue()){
		    			readOnlyClass = new Boolean(isReadOnly.booleanValue());
		    		} else {
		    			readOnlyClass = new Boolean(!sessionContext.hasPermission(cls, Right.MultipleClassSetupClass));
		    		}
		    		BannerSection bs = BannerSection.findBannerSectionForBannerCourseAndClass(bc, cls);
		    		frm.addToBannerSections(bsess, bs, cls, classAssignmentProxy, readOnlyClass, indent);
		    	}
	    		loadClasses(frm, bsess, bc, cls.getChildClasses(), new Boolean(true), indent + ((previousItype == null || !previousItype.equals(cls.getSchedulingSubpart().getItype().getItype()))?"&nbsp;&nbsp;&nbsp;&nbsp;":""), cls.getSchedulingSubpart().getItype().getItype(), classAssignmentProxy);
	    	}
    	}
    }

    /**
     * Update the instructional offering config
     * @param request
     * @param frm
     */
    @SuppressWarnings("unchecked")
	private void doUpdate(HttpServletRequest request, BannerOfferingModifyForm frm)
    	throws Exception {

        // Get Instructional Offering Config
        InstrOfferingConfigDAO iocdao = new InstrOfferingConfigDAO();
        InstrOfferingConfig ioc = iocdao.get(frm.getInstrOffrConfigId());
        Session hibSession = iocdao.getSession();

        BannerCourse bc = BannerCourseDAO.getInstance().get(frm.getBannerCourseOfferingId());      
        BannerConfig bannerConfig = BannerConfigDAO.getInstance().get(frm.getBannerConfigId());
  
		Transaction tx = null;

        try {
	        tx = hibSession.beginTransaction();

	        // If the banner offering config gradable itype has changed update it.
	        if ((frm.getItypeId() == null && bannerConfig.getGradableItype() != null)  
	        		|| (frm.getItypeId() != null && bannerConfig.getGradableItype() == null)
	        		|| (frm.getItypeId() != null && !frm.getItypeId().equals(bannerConfig.getGradableItype().getItype()))){
	        	ItypeDesc itype = null;
	        	if (frm.getItypeId() != null) {
	        		itype = ItypeDescDAO.getInstance().get(frm.getItypeId());
	        	}
	        	bannerConfig.setGradableItype(itype);
	        	hibSession.update(bannerConfig);
	        }

	        // For all changed classes, update them
	        modifyClasses(frm, ioc, bc, hibSession);

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

  
	private void modifyClasses(BannerOfferingModifyForm frm, InstrOfferingConfig ioc, BannerCourse bc, Session hibSession){
		BannerSectionDAO bsDao = new BannerSectionDAO();
	
		Iterator it1 = frm.getBannerSectionIds().listIterator();
		Iterator it2 = frm.getBannerSectionSectionIds().listIterator();
		Iterator it3 = frm.getConsents().listIterator();
		Iterator it4 = frm.getCourseCreditOverrides().listIterator();
		Iterator it5 = frm.getLimitOverrides().listIterator();
		Iterator it6 = frm.getCampusOverrides().listIterator();
		
		for(;it1.hasNext();){
			boolean changed = false;
			Long sectionId = new Long(it1.next().toString());
			String bannerSectionIndex = (String)it2.next();
			if (bannerSectionIndex != null){
				bannerSectionIndex = bannerSectionIndex.toUpperCase();
			}
			BannerSection bs = bsDao.get(sectionId);
			if ((bannerSectionIndex != null && !bannerSectionIndex.equals(bs.getSectionIndex())) || (bannerSectionIndex == null && bs.getSectionIndex() != null)){
				bs.setSectionIndex(bannerSectionIndex);
				bs.updateClassSuffixForClassesIfNecessary(bsDao.getSession());
				changed = true;
			}
			String consentStr = it3.next().toString();
			Long consentId = new Long((consentStr.equals("-")?"-1":consentStr.toString()));
			if (consentId.equals(-1)){
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
				credit = new Float(creditStr);
			}
			if ((bs.getOverrideCourseCredit() != null && credit == null) ||
				(bs.getOverrideCourseCredit() == null && credit != null) ||
				(bs.getOverrideCourseCredit()!= null && credit != null && !bs.getOverrideCourseCredit().equals(credit))){
				bs.setOverrideCourseCredit(credit);
				changed = true;
			}
			if (frm.getShowLimitOverride().booleanValue()){
				String limitStr = it5.next().toString();
				Integer limit = null;
				if (limitStr != null && limitStr.trim().length() > 0){
					limit = new Integer(limitStr);
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
			String campusOverrideIdStr = it6.next().toString();
			Long campusOverrideId = new Long((campusOverrideIdStr.equals("-")?"-1":campusOverrideIdStr.toString()));
			if (campusOverrideId.equals(-1)){
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
				hibSession.update(bs);
			}
		}
    }

}
