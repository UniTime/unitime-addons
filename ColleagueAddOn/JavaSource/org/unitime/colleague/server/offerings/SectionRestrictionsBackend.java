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
package org.unitime.colleague.server.offerings;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.comparators.ColleagueRestrictionComparator;
import org.unitime.colleague.model.comparators.ColleagueSectionComparator;
import org.unitime.colleague.model.dao.ColleagueRestrictionDAO;
import org.unitime.colleague.model.dao.ColleagueSectionDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.colleague.SectionRestrictionsPage.SectionInterface;
import org.unitime.timetable.gwt.colleague.SectionRestrictionsPage.SectionRestrictionsInterface;
import org.unitime.timetable.gwt.colleague.SectionRestrictionsPage.SectionRestrictionsRequest;
import org.unitime.timetable.gwt.colleague.SectionRestrictionsPage.SectionRestrictionsResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.service.AssignmentService;

@GwtRpcImplements(SectionRestrictionsRequest.class)
public class SectionRestrictionsBackend implements GwtRpcImplementation<SectionRestrictionsRequest, SectionRestrictionsResponse>{
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;

	@Override
	public SectionRestrictionsResponse execute(SectionRestrictionsRequest request, SessionContext context) {
		if (request.getConfigId() == null)
			throw new GwtRpcException(MSG.exceptionMissingIOConfig());
		InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(request.getConfigId());
		if (ioc == null)
			throw new GwtRpcException(MSG.exceptionMissingIOConfig());
		context.checkPermission(ioc, Right.InstrOfferingConfigEdit);
		InstructionalOffering io = ioc.getInstructionalOffering();
		CourseOffering co = (request.getCourseId() == null ? null : CourseOfferingDAO.getInstance().get(request.getCourseId()));
		if (co == null)
			co = io.getControllingCourseOffering();
		SectionRestrictionsResponse response = new SectionRestrictionsResponse();
		SectionRestrictionsInterface sections = new SectionRestrictionsInterface();
		response.setSections(sections);
		sections.setConfigId(ioc.getUniqueId());
		sections.setCourseId(co.getUniqueId());
		switch (request.getOperation()) {
		case LOAD:
			String name = io.getCourseNameWithTitle();
	        if (io.hasMultipleConfigurations()) name += " [" + ioc.getName() +"]";
	        sections.setName(name);
			populateRestrictions(response, io.getSessionId());
			
			if (ioc.getSchedulingSubparts() == null || ioc.getSchedulingSubparts().size() == 0)
	        	throw new GwtRpcException(MSG.exceptionIOConfigUndefined());
			
	        ArrayList<SchedulingSubpart> subpartList = new ArrayList<SchedulingSubpart>(ioc.getSchedulingSubparts());
	        Collections.sort(subpartList, new SchedulingSubpartComparator());

	        for(SchedulingSubpart ss : subpartList){
	    		if (ss.getClasses() == null || ss.getClasses().size() == 0)
	    			throw new GwtRpcException(MSG.exceptionInitialIOSetupIncomplete());
	    		if (ss.getParentSubpart() == null) {
	        		loadSections(sections, ColleagueSection.findNotDeletedColleagueSectionsForSchedulingSubpartAndCourse(ss, co, ColleagueSectionDAO.getInstance().getSession()), 0, context);
	        	}
	        }
			break;
		case UPDATE:
			update(request.getRestrictions(), context);
			break;
		case UNASSIGN_ALL:
			unassignAllRestrictions(request.getRestrictions(), context);
			break;
		}

		return response;
	}
	

	protected void populateRestrictions(SectionRestrictionsResponse response, Long sessionId) {
		TreeSet<ColleagueRestriction> restrictions = new TreeSet<ColleagueRestriction>(new ColleagueRestrictionComparator());
		restrictions.addAll(ColleagueRestriction.getAllColleagueRestrictionsForSession(ColleagueRestrictionDAO.getInstance().getSession(), sessionId));
		for (ColleagueRestriction r: restrictions)
			response.addRestriction(r.getUniqueId(), r.getOptionLabel());
	}
	
	protected void loadSections(SectionRestrictionsInterface form, Collection<ColleagueSection> sections, int indent, SessionContext context) {
		if (sections != null && sections.size() > 0){
    		ArrayList<ColleagueSection> sectionList = new ArrayList<ColleagueSection>(sections);
    		Collections.sort(sectionList, new ColleagueSectionComparator());
	    	for(ColleagueSection cs : sectionList){
	    		if (!cs.isDeleted() && !cs.isCanceled(ColleagueSectionDAO.getInstance().getSession())){
		    		addToSections(form, cs, context.hasPermission(cs.getFirstClass(), Right.ClassEdit), indent);
		    		loadSections(form, cs.getColleagueSectionToChildSections(), indent + 1, context);
	    		}
	    	}
		}
	}

	protected void addToSections(SectionRestrictionsInterface form, ColleagueSection section, boolean canEdit, int indent) {
		SectionInterface s = new SectionInterface();
		s.setId(section.getUniqueId());
		s.setIndent(indent);
		s.setCanEdit(canEdit);
		s.setName(section.colleagueSectionLabel());
		s.setTime(section.buildAssignedTimeCell(classAssignmentService.getAssignment()));
		s.setRoom(section.buildAssignedRoomCell(classAssignmentService.getAssignment()));
		s.setSynonym(section.getColleagueId());
		for (ColleagueRestriction r: section.getRestrictions())
			s.addRestrictionId(r.getUniqueId());
		form.addSection(s);
	}
	
	protected void update(SectionRestrictionsInterface sections, SessionContext context) {
		if (sections == null || !sections.hasSections()) return;
        Transaction tx = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        try {
            tx = hibSession.beginTransaction();
            for (SectionInterface s: sections.getSections()) {
				if (!s.isCanEdit()) continue;
				ColleagueSection c = ColleagueSectionDAO.getInstance().get(s.getId(), hibSession);
				context.checkPermission(c.getFirstClass(), Right.ClassEdit);
				
				Set<ColleagueRestriction> remaining = new HashSet<ColleagueRestriction>(c.getRestrictions());
	        	if (s.hasRestrictionIds())
	        		for (Long id: s.getRestrictionIds()) {
	        			ColleagueRestriction rest = ColleagueRestrictionDAO.getInstance().get(id, hibSession);
	        			if (!remaining.remove(rest))
	        				c.addToRestrictions(rest);
	        		}
	        	c.getRestrictions().removeAll(remaining);
	        	hibSession.merge(c);
			}
            tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw new GwtRpcException(e.getMessage(), e);
        }
	}
	
	protected void unassignAllRestrictions(SectionRestrictionsInterface sections, SessionContext context) {
		if (sections == null || !sections.hasSections()) return;
        Transaction tx = null;
        org.hibernate.Session hibSession = SessionDAO.getInstance().getSession();
        try {
            tx = hibSession.beginTransaction();
            for (SectionInterface s: sections.getSections()) {
				if (!s.isCanEdit()) continue;
				ColleagueSection c = ColleagueSectionDAO.getInstance().get(s.getId(), hibSession);
				context.checkPermission(c.getFirstClass(), Right.ClassEdit);
				c.getRestrictions().clear();
				hibSession.merge(c);
			}
            tx.commit();
        } catch (Exception e) {
            if (tx!=null) tx.rollback();
            throw new GwtRpcException(e.getMessage(), e);
        }
	}
}
