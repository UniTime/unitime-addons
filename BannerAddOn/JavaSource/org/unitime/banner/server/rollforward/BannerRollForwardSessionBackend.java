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
package org.unitime.banner.server.rollforward;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.BannerRollForwardSessionInterface;
import org.unitime.timetable.gwt.shared.BannerRollForwardSessionInterface.BannerRollForwardSessionRequest;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardSessionResponse;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.administration.session.RollForwardSessionBackend.SessionComparator;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.queue.QueueItem;
import org.unitime.timetable.util.queue.RollForwardQueueItem;

@GwtRpcImplements(BannerRollForwardSessionRequest.class)
public class BannerRollForwardSessionBackend implements GwtRpcImplementation<BannerRollForwardSessionRequest, RollForwardSessionResponse>{
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Autowired
	ApplicationContext applicationContext;
	
	@Autowired
	SolverServerService solverServerService;

	@Override
	public RollForwardSessionResponse execute(BannerRollForwardSessionRequest request, SessionContext context) {
		context.checkPermission(Right.SessionRollForward);
		switch (request.getOperation()) {
		case LOAD:
			Long sessionId = request.getData().getSessionToRollForwardTo();
			List<Session> allSessions = new ArrayList<Session>(Session.getAllSessions());
			if (sessionId == null)
				for (Session session: allSessions) {
					if (session.getStatusType().isAllowRollForward())
						sessionId = session.getUniqueId();
				}
			Session currentSession = SessionDAO.getInstance().get(sessionId == null ? context.getUser().getCurrentAcademicSessionId() : sessionId);
			Collections.sort(allSessions, new SessionComparator(currentSession.getAcademicInitiative()));
			
			RollForwardSessionResponse response = new RollForwardSessionResponse();
			for (Session session: allSessions) {
				if (session.getStatusType().isAllowRollForward()) {
					response.addToSession(session.getUniqueId(), session.getLabel());
					if (sessionId == null)
						sessionId = session.getUniqueId();
				} else {
					response.addFromSession(session.getUniqueId(), session.getLabel());
				}
			}
			response.setToSessionId(sessionId);
			return response;
		case EXECUTE:
			BannerRollForwardSessionInterface form = (BannerRollForwardSessionInterface)request.getData();
			Session session = SessionDAO.getInstance().get(form.getSessionToRollForwardTo());
			QueueItem queue = solverServerService.getQueueProcessor().add(new BannerRollForwardQueueItem(session, context.getUser(), form));
			response = new RollForwardSessionResponse();
			response.setQueueId(queue.getId());
			return response;
		case POPULATE:
			QueueItem item = solverServerService.getQueueProcessor().get(request.getQueueId());
			response = new RollForwardSessionResponse();
			if (item != null && item instanceof RollForwardQueueItem) {
				RollForwardQueueItem q = (RollForwardQueueItem)item;
				response.setData(q.getForm());
				response.setErrors(q.getErrors());
			}
			return response;
		}
		return null;
	}

}
