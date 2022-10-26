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

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.banner.form.RollForwardBannerSessionForm;
import org.unitime.banner.util.BannerSessionRollForward;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.action.RollForwardSessionAction.RollForwardError;
import org.unitime.timetable.action.RollForwardSessionAction.RollForwardErrors;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.queue.QueueItem;



/**
 * 
 * @author says
 *
 */
@Action(value = "rollForwardBannerSession", results = {
		@Result(name = "displayRollForwardBannerSessionForm", type = "tiles", location = "rollForwardBannerSession.tiles")
	})
@TilesDefinition(name = "rollForwardBannerSession.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Roll Forward Banner Session"),
		@TilesPutAttribute(name = "body", value = "/banner/rollForwardBannerSession.jsp")
	})
public class RollForwardBannerSessionAction extends UniTimeAction<RollForwardBannerSessionForm> {
	private static final long serialVersionUID = -8713079460274400691L;
	protected static final BannerMessages BMSG = Localization.create(BannerMessages.class);
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);

	private String remove;
	public String getRemove() { return remove; }
	public void setRemove(String remove) { this.remove = remove; }

	@Override
	public String execute() throws Exception {
		if (form == null) form = new RollForwardBannerSessionForm();

		// Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);

        if (MSG.actionRollForward().equals(op)) {
    		sessionContext.checkPermission(form.getSessionToRollForwardTo(), "Session", Right.SessionRollForward);
    		form.validate(this);
            if (!hasFieldErrors() && (form.getRollForwardBannerSession() || form.getCreateMissingBannerSections())) {
            	getSolverServerService().getQueueProcessor().add(new BannerRollForwardQueueItem(
            			SessionDAO.getInstance().get(form.getSessionToRollForwardTo()), 
            			sessionContext.getUser(),
            			(RollForwardBannerSessionForm)form.clone()));
            }
        }

        if (remove != null && !remove.isEmpty()) {
        	getSolverServerService().getQueueProcessor().remove(request.getParameter("remove"));
	    }
        
        WebTable queueTable = getQueueTable();
		if (queueTable != null && !queueTable.getLines().isEmpty()) {
	    	request.setAttribute("table", queueTable.printTable(WebTable.getOrder(sessionContext,"rollForwardBannerSession.ord")));
	    }
        
		setToFromSessionsInForm();
  		return "displayRollForwardBannerSessionForm";
	}
	
	protected void setToFromSessionsInForm(){
		List<Session> sessionList = new ArrayList<Session>();
		sessionList.addAll(Session.getAllSessions());
		form.setFromSessions(new ArrayList<Session>());
		form.setToSessions(new ArrayList<Session>());
		Session session = null;
		for (int i = (sessionList.size() - 1); i >= 0; i--){
			session = (Session)sessionList.get(i);
			if (session.getStatusType().isAllowRollForward()) {
				form.getToSessions().add(session);
				if (form.getSessionToRollForwardTo() == null){
					form.setSessionToRollForwardTo(session.getUniqueId());
				}
			} else {
				form.getFromSessions().add(session);				
			}
		}
	}
	
	private WebTable getQueueTable() {
        WebTable.setOrder(sessionContext,"rollForwardBannerSession.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		DateFormat df = new SimpleDateFormat("h:mma");
		List<QueueItem> queue = getSolverServerService().getQueueProcessor().getItems(null, null, "Banner Roll Forward");
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, null, "rollForwardBannerSession.action?ord=%%",
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
				delete = "<img src='images/action_delete.png' border='0' onClick=\"if (confirm('Do you really want to remove this roll forward?')) document.location='rollForwardBannerSession.action?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine("onClick=\"document.location='rollForwardBannerSession.action?log=" + item.getId() + "';\"",
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
				((BannerRollForwardQueueItem)item).getForm().copyTo(form);
				saveErrors(((BannerRollForwardQueueItem)item).getErrors());
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
	
	private class BannerRollForwardQueueItem extends QueueItem {
		private static final long serialVersionUID = 1L;
		private RollForwardBannerSessionForm iForm;
		private int iProgress = 0;
		private RollForwardErrors iErrors = new RollForwardErrors();
		
		public BannerRollForwardQueueItem(Session session, UserContext owner, RollForwardBannerSessionForm form) {
			super(session, sessionContext.getUser());
			iForm = form;
		}
		
		public RollForwardErrors getErrors() {
			return iErrors;
		}
		
		public RollForwardBannerSessionForm getForm() {
			return iForm;
		}
		
		@Override
		protected void execute() throws Exception {
		    BannerSessionRollForward sessionRollForward = new BannerSessionRollForward(this);
              
	        Session toAcadSession = Session.getSessionById(iForm.getSessionToRollForwardTo());
			if (toAcadSession == null){
				iErrors.addFieldError("mustSelectSession", MSG.errorRollForwardMissingToSession());
			}
			if (iErrors.isEmpty()){
				iForm.validateSessionToRollForwardTo(iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardBannerSession()) {
				setStatus(BMSG.rollForwardBannerSessionData() + " ...");
				sessionRollForward.rollBannerSessionDataForward(iErrors, iForm);	
	        }
        	if (iErrors.isEmpty() && iForm.getCreateMissingBannerSections()) {
    				setStatus(BMSG.rollForwardCreateMissingBannerSectionData() + " ...");
    				sessionRollForward.createMissingBannerSections(iErrors, iForm);	
    	        }
    	
	        iProgress++;
	        if (!iErrors.isEmpty()) {
	        	setError(new Exception(iErrors.get(0).getMessage()));
	        } else {
	        	log(MSG.logAllDone());
	        }
		}

		@Override
		public String name() {
			List<String> names = new ArrayList<String>();
        	if (iForm.getRollForwardBannerSession()) names.add(BMSG.rollForwardBannerSession());
         	if (iForm.getCreateMissingBannerSections()) names.add(BMSG.rollForwardCreateMissingBannerSectionData());
             	String name = names.toString().replace("[", "").replace("]", "");
        	if (name.length() > 50) name = name.substring(0, 47) + "...";
        	return name;
		}

		@Override
		public double progress() {
			return 100 * iProgress / ((iForm.getRollForwardBannerSession()?1:0) + (iForm.getCreateMissingBannerSections()?1:0));
		}

		@Override
		public String type() {
			return "Banner Roll Forward";
		}
		
	}

}
