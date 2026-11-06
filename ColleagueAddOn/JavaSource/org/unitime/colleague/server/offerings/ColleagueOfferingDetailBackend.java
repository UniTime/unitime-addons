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
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.colleague.dataexchange.ColleagueMessage.MessageAction;
import org.unitime.colleague.dataexchange.SendColleagueMessage;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.colleague.ColleagueOfferingDetailPage.ColleagueOfferingDetailRequest;
import org.unitime.timetable.gwt.colleague.ColleagueOfferingDetailPage.ColleagueOfferingDetailResponse;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.comparators.OfferingCoordinatorComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.OfferingDetailBackend;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.BackTracker.BackItem;
import org.unitime.timetable.webutil.Navigation;

@GwtRpcImplements(ColleagueOfferingDetailRequest.class)
public class ColleagueOfferingDetailBackend implements GwtRpcImplementation<ColleagueOfferingDetailRequest, ColleagueOfferingDetailResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static ColleagueMessages BMSG = Localization.create(ColleagueMessages.class);
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	
	@Override
	public ColleagueOfferingDetailResponse execute(ColleagueOfferingDetailRequest request, SessionContext context) {
		org.hibernate.Session hibSession = CourseOfferingDAO.getInstance().getSession();
		CourseOffering co = CourseOfferingDAO.getInstance().get(request.getCourseId());
		if (co == null)
			throw new GwtRpcException(BMSG.missingCourseOfferingId(request.getCourseId()));
		InstructionalOffering io = (co == null ? null : co.getInstructionalOffering());
		if (io == null)
			throw new GwtRpcException(BMSG.missingCourseOfferingId(request.getCourseId()));
		context.checkPermission(io, Right.InstructionalOfferingDetail);
		
		ColleagueOfferingDetailResponse response = new ColleagueOfferingDetailResponse();
		response.setCourseId(co.getUniqueId());
		response.setOfferingId(io.getUniqueId());
		
		if (request.getAction() == null) {
			BackTracker.markForBack(
					context,
					"colleagueOffering?co="+request.getCourseId(),
					BMSG.sectColleagueOffering() + " ("+co.getCourseName()+")",
					true, false);
		    // Set Session Variables
	    	OfferingDetailBackend.setLastInstructionalOffering(context, io);
		} else {
			switch (request.getAction()) {
			case Lock:
				context.checkPermission(io, Right.OfferingCanLock);
				io.getSession().lockOffering(io.getUniqueId());
				break;
			case Unlock:
				context.checkPermission(io, Right.OfferingCanUnlock);
				io.getSession().unlockOffering(io, context.getUser());
				break;
			case ResendToColleague:
				List<ColleagueSection> colleagueSections = ColleagueSection.findColleagueSectionsForCourseOfferingId(co.getUniqueId(), hibSession);
	        	SendColleagueMessage.sendColleagueMessage(colleagueSections, MessageAction.UPDATE, hibSession);
				break;
			}
		}
		
		response.setCourseNumber(co.getCourseNbr());
		response.setSubjectAreaId(co.getSubjectArea().getUniqueId());
		response.setName(co.getCourseNameWithTitle());
        response.setOffered(!io.isNotOffered());
        response.setCourses(OfferingDetailBackend.createCoursesTable(context, io, false));
        
        if (!io.getOfferingCoordinators().isEmpty()) {
        	CellInterface c = response.addProperty(MSG.propertyCoordinators());
        	c.setInline(false);
        	String instructorNameFormat = context.getUser().getProperty(UserProperty.NameFormat);
            List<OfferingCoordinator> coordinatorList = new ArrayList<OfferingCoordinator>(io.getOfferingCoordinators());
            Collections.sort(coordinatorList, new OfferingCoordinatorComparator(context));
            for (OfferingCoordinator coordinator: coordinatorList) {
            	c.add(coordinator.getInstructor().getName(instructorNameFormat) +
            			(coordinator.getResponsibility() == null ?  (coordinator.getPercentShare() != 0 ? " (" + coordinator.getPercentShare() + "%)" : "") :
            				" (" + coordinator.getResponsibility().getLabel() + (coordinator.getPercentShare() > 0 ? ", " + coordinator.getPercentShare() + "%" : "") + ")")
            			).setUrl("instructor?instructorId=" + coordinator.getInstructor().getUniqueId()).setClassName("noFancyLinks");
            }
        }
        
        CourseOffering next = getNext(co, context);
        if (next != null && context.hasPermission(next.getInstructionalOffering(), "InstructionalOffering", Right.InstructionalOfferingDetail)) {
        	response.setNextId(next.getUniqueId());
        	response.addOperation("next");
        }
        CourseOffering previous = getPrevious(co, context);
        if (previous != null && context.hasPermission(previous.getInstructionalOffering(), "InstructionalOffering", Right.InstructionalOfferingDetail)) {
        	response.setPreviousId(previous.getUniqueId());
        	response.addOperation("previous");
        }
        BackItem back = BackTracker.getBackItem(context, 2);
    	if (back != null) {
    		response.addOperation("back");
    		response.setBackTitle(back.getTitle());
    		response.setBackUrl(back.getUrl() +
    				(back.getUrl().indexOf('?') >= 0 ? "&" : "?") +
    				"backId=" + io.getUniqueId() + "&backType=InstructionalOffering");
    	}

    	ColleagueConfigTableBuilder builder = new ColleagueConfigTableBuilder(context, request.getBackType(), request.getBackId());
    	builder.generateConfigTablesForColleagueOffering(
    			classAssignmentService.getAssignment(), io, co, response);

        if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.DisplayLastChanges)))
        	response.setLastChanges(OfferingDetailBackend.getLastChanges(io));

    	if (context.hasPermission(io, Right.OfferingCanLock))
    		response.addOperation("lock");
    	if (context.hasPermission(io, Right.OfferingCanUnlock))
    		response.addOperation("unlock");
    	response.addOperation("resend");
		
		return response;
	}
	
	protected CourseOffering getNext(CourseOffering course, SessionContext context) {
		Long nextId = Navigation.getNext(context, Navigation.sInstructionalOfferingLevel, course.getUniqueId());
    	if (nextId != null && nextId >= 0)
    		return CourseOfferingDAO.getInstance().get(nextId);
    	return null;
	}
	
	protected CourseOffering getPrevious(CourseOffering course, SessionContext context) {
		Long previousId = Navigation.getPrevious(context, Navigation.sInstructionalOfferingLevel, course.getUniqueId());
    	if (previousId != null && previousId >= 0)
    		return CourseOfferingDAO.getInstance().get(previousId);
    	return null;
	}

}
