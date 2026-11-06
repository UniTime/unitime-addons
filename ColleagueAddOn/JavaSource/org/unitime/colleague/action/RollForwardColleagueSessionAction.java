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
package org.unitime.colleague.action;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.colleague.form.RollForwardColleagueSessionForm;
import org.unitime.colleague.server.rollforward.ColleagueRollForwardQueueItem;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.action.RollForwardSessionAction.SessionComparator;
import org.unitime.timetable.gwt.shared.RollForwardSessionInterface.RollForwardError;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.queue.QueueItem;


/**
 * 
 * @author says
 *
 */
@Action(value = "rollForwardColleagueSession", results = {
		@Result(name = "displayRollForwardColleagueSessionForm", type = "tiles", location = "rollForwardColleagueSession.tiles")
	})
@TilesDefinition(name = "rollForwardColleagueSession.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Roll Forward Colleague Session"),
		@TilesPutAttribute(name = "body", value = "/colleague/rollForwardColleagueSession.jsp")
	})
public class RollForwardColleagueSessionAction extends UniTimeAction<RollForwardColleagueSessionForm> {
	private static final long serialVersionUID = 8306854159590606098L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static final ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);

	private String remove;
	public String getRemove() { return remove; }
	public void setRemove(String remove) { this.remove = remove; }

	public String execute() throws Exception {
		if (form == null) form = new RollForwardColleagueSessionForm();
	    
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);

		if (MSG.actionRollForward().equals(op)) {
    		sessionContext.checkPermission(form.getSessionToRollForwardTo(), "Session", Right.SessionRollForward);
    		form.validate(this);
            if (!hasFieldErrors() && form.getRollForwardColleagueSession().booleanValue()) {
            	getSolverServerService().getQueueProcessor().add(new ColleagueRollForwardQueueItem(
            			SessionDAO.getInstance().get(form.getSessionToRollForwardTo()), 
            			sessionContext.getUser(),
            			form.toColleagueRollForwardSessionInterface()));
            }
        }

		if (remove != null && !remove.isEmpty()) {
			getSolverServerService().getQueueProcessor().remove(remove);
	    }
		
		WebTable queueTable = getQueueTable();
		if (queueTable != null && !queueTable.getLines().isEmpty()) {
	    	request.setAttribute("table", queueTable.printTable(WebTable.getOrder(sessionContext,"rollForwardColleagueSession.ord")));
	    }
        
		setToFromSessionsInForm();
  		return "displayRollForwardColleagueSessionForm";
	}
	
	protected void setToFromSessionsInForm(){
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.addAll(Session.getAllSessions());
		List<Session> fromSessions = new ArrayList<Session>(); form.setFromSessions(fromSessions);
		List<Session> toSessions = new ArrayList<Session>(); form.setToSessions(toSessions);
		Session session = null;
		for (int i = (sessionList.size() - 1); i >= 0; i--){
			session = (Session)sessionList.get(i);
			if (session.getStatusType().isAllowRollForward()) {
				toSessions.add(session);
				if (form.getSessionToRollForwardTo() == null){
					form.setSessionToRollForwardTo(session.getUniqueId());
				}
			} else {
				fromSessions.add(session);				
			}
		}
		Long currentSessionId = form.getSessionToRollForwardTo();
		if (currentSessionId == null || currentSessionId <= 0l)
			currentSessionId = sessionContext.getUser().getCurrentAcademicSessionId();
		Session currentSession = (currentSessionId == null ? null : SessionDAO.getInstance().get(currentSessionId));
		if (currentSession != null) {
			Collections.sort(fromSessions, new SessionComparator(currentSession.getAcademicInitiative()));
			Collections.sort(toSessions, new SessionComparator(currentSession.getAcademicInitiative()));
		}
	}
	
	private WebTable getQueueTable() {
        WebTable.setOrder(sessionContext,"rollForwardColleagueSession.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		DateFormat df = new SimpleDateFormat("h:mma");
		List<QueueItem> queue = getSolverServerService().getQueueProcessor().getItems(null, null, "Roll Forward");
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, null, "rollForwardColleagueSession.action?ord=%%",
				new String[] {
						MSG.fieldQueueName(),
						MSG.fieldQueueStatus(),
						MSG.fieldQueueProgress(),
						MSG.fieldQueueOwner(),
						MSG.fieldQueueSession(),
						MSG.fieldQueueCreated(),
						MSG.fieldQueueStarted(),
						MSG.fieldQueueFinished(),
						MSG.fieldQueueOutput()},
				new String[] { "left", "left", "right", "left", "left", "left", "left", "left", "center"},
				new boolean[] { true, true, true, true, true, true, true, true, true});
		Date now = new Date();
		long timeToShow = 1000 * 60 * 60;
		for (QueueItem item: queue) {
			if (item.finished() != null && now.getTime() - item.finished().getTime() > timeToShow) continue;
			String name = item.name();
			if (name.length() > 60) name = name.substring(0, 57) + "...";
			String delete = null;
			if (sessionContext.getUser().getExternalUserId().equals(item.getOwnerId()) && (item.started() == null || item.finished() != null)) {
				delete = "<img src='images/action_delete.png' border='0' onClick=\"if (confirm('Do you really want to remove this roll forward?')) document.location='rollForwardColleagueSession.action?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine("onClick=\"document.location='rollForwardColleagueSession.action?log=" + item.getId() + "';\"",
					new String[] {
						name + (delete == null ? "": " " + delete),
						item.status(),
						(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%"),
						item.getOwnerName(),
						item.getSession().getLabel(),
						df.format(item.created()),
						item.started() == null ? "" : df.format(item.started()),
						item.finished() == null ? "" : df.format(item.finished()),
						item.hasOutput() ? "<A href='"+item.getOutputLink()+"'>"+item.getOutputName().substring(item.getOutputName().lastIndexOf('.') + 1).toUpperCase()+"</A>" : ""
					},
					new Comparable[] {
						item.created().getTime(),
						item.status(),
						item.progress(),
						item.getOwnerName(),
						item.getSession(),
						item.created().getTime(),
						item.started() == null ? Long.MAX_VALUE : item.started().getTime(),
						item.finished() == null ? Long.MAX_VALUE : item.finished().getTime(),
						null
					});
			if (log != null && log.equals(item.getId().toString())) {
				request.setAttribute("logname", name);
				request.setAttribute("logid", item.getId().toString());
				request.setAttribute("log", item.log());
				form.copyFromRollForwardSessionInterface(((ColleagueRollForwardQueueItem)item).getForm());
				saveErrors(((ColleagueRollForwardQueueItem)item).getErrors());
				line.setBgColor("rgb(168,187,225)");
			}

		}
		return table;
	}
	
	protected void saveErrors(List<RollForwardError> errors) {
		if (errors != null)
			for (RollForwardError e: errors)
				addFieldError(e.getType(), e.getMessage());
	}
}
