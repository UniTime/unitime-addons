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
import java.util.List;
import java.util.Vector;

import org.springframework.beans.factory.annotation.Autowired;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.banner.BannerOfferingDetailPage.BannerOfferingDetailRequest;
import org.unitime.timetable.gwt.banner.BannerOfferingDetailPage.BannerOfferingDetailResponse;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.comparators.OfferingCoordinatorComparator;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.courses.OfferingDetailBackend;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.solver.service.AssignmentService;
import org.unitime.timetable.webutil.BackTracker;
import org.unitime.timetable.webutil.Navigation;

@GwtRpcImplements(BannerOfferingDetailRequest.class)
public class BannerOfferingDetailBackend implements GwtRpcImplementation<BannerOfferingDetailRequest, BannerOfferingDetailResponse>{
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
	@Autowired AssignmentService<ClassAssignmentProxy> classAssignmentService;
	
	@Override
	public BannerOfferingDetailResponse execute(BannerOfferingDetailRequest request, SessionContext context) {
		org.hibernate.Session hibSession = BannerCourseDAO.getInstance().getSession();
		BannerCourse bc = BannerCourseDAO.getInstance().get(request.getBannerCourseId());
		if (bc == null)
			throw new GwtRpcException(BMSG.missingBannerCourseOfferingId(request.getBannerCourseId()));
		CourseOffering co = bc.getCourseOffering(hibSession);
		InstructionalOffering io = (co == null ? null : co.getInstructionalOffering());
		if (io == null)
			throw new GwtRpcException(BMSG.missingBannerCourseOfferingId(request.getBannerCourseId()));
		context.checkPermission(io, Right.InstructionalOfferingDetail);
		
		BannerOfferingDetailResponse response = new BannerOfferingDetailResponse();
		response.setBannerCourseId(bc.getUniqueId());
		response.setOfferingId(io.getUniqueId());
		
		if (request.getAction() == null) {
			BackTracker.markForBack(
					context,
					"bannerOffering?bc="+request.getBannerCourseId(),
					BMSG.sectBannerOffering() + " ("+co.getCourseName()+")",
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
			case ResendToBanner:
				Vector<BannerSection> sections = new Vector<BannerSection>();
	        	for (BannerConfig bannerConfig: bc.getBannerConfigs()) {
	        		sections.addAll(bannerConfig.getBannerSections());
	        	}
	        	SendBannerMessage.sendBannerMessage(sections, BannerMessageAction.UPDATE, hibSession);
				break;
			}
		}
		
		response.setCourseId(co.getUniqueId());
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
            			).setUrl("instructorDetail.action?instructorId=" + coordinator.getInstructor().getUniqueId()).setClassName("noFancyLinks");
            }
        }
        
        BannerCourse next = getNext(bc.getUniqueId(), context);
        response.setNextId(next==null ? null : next.getUniqueId());
        BannerCourse previous = getPrevious(bc.getUniqueId(), context);
        response.setPreviousId(previous == null ? null : previous.getUniqueId());

    	BannerConfigTableBuilder builder = new BannerConfigTableBuilder(context, request.getBackType(), request.getBackId());
    	builder.generateConfigTablesForBannerOffering(
    			classAssignmentService.getAssignment(), io, bc, response);

        if (CommonValues.Yes.eq(context.getUser().getProperty(UserProperty.DisplayLastChanges)))
        	response.setLastChanges(OfferingDetailBackend.getLastChanges(io));

    	if (context.hasPermission(io, Right.OfferingCanLock))
    		response.addOperation("lock");
    	if (context.hasPermission(io, Right.OfferingCanUnlock))
    		response.addOperation("unlock");
    	response.addOperation("resend");
		
		return response;
	}
	
	protected BannerCourse getNext(Long bannerCourseId, SessionContext context) {
		Long nextId = Navigation.getNext(context, Navigation.sInstructionalOfferingLevel, bannerCourseId);
    	if (nextId != null && nextId >= 0)
    		return BannerCourseDAO.getInstance().get(nextId);
    	return null;
	}
	
	protected BannerCourse getPrevious(Long bannerCourseId, SessionContext context) {
		Long previousId = Navigation.getPrevious(context, Navigation.sInstructionalOfferingLevel, bannerCourseId);
    	if (previousId != null && previousId >= 0)
    		return BannerCourseDAO.getInstance().get(previousId);
    	return null;
	}

}
