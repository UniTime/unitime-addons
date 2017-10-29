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
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;
import org.apache.struts.util.MessageResources;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.colleague.form.RollForwardColleagueSessionForm;
import org.unitime.colleague.util.ColleagueSessionRollForward;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.timetable.action.RollForwardSessionAction;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.util.queue.QueueItem;



/**
 * 
 * @author says
 *
 */
@Service("/rollForwardColleagueSession")
public class RollForwardColleagueSessionAction extends RollForwardSessionAction {
	/*
	 * Generated Methods
	 */

	@Autowired SessionContext sessionContext;
	
	@Autowired SolverServerService solverServerService;

	/** 
	 * Method execute
	 * @param mapping
	 * @param form
	 * @param request
	 * @param response
	 * @return ActionForward
	 * @throws Exception 
	 */
	public ActionForward execute(ActionMapping mapping, ActionForm form,
		HttpServletRequest request, HttpServletResponse response) throws Exception {
	    
		if (sessionContext == null){
			Debug.info("session context is null");
			throw(new Exception("session context is null"));
		}
	    // Check Access
		sessionContext.checkPermission(Right.AcademicSessionEdit);

		MessageResources rsc = getResources(request);
        
        RollForwardColleagueSessionForm rollForwardColleagueSessionForm = (RollForwardColleagueSessionForm) form;
        // Get operation
        String op = request.getParameter("op");		  
                
        if (op != null && op.equals(rsc.getMessage("button.rollForward"))) {
    		sessionContext.checkPermission(rollForwardColleagueSessionForm.getSessionToRollForwardTo(), "Session", Right.SessionRollForward);
            ActionMessages errors = rollForwardColleagueSessionForm.validate(mapping, request);
            if (errors.size() == 0 && rollForwardColleagueSessionForm.getRollForwardColleagueSession().booleanValue()) {
            	solverServerService.getQueueProcessor().add(new ColleagueRollForwardQueueItem(
            			SessionDAO.getInstance().get(rollForwardColleagueSessionForm.getSessionToRollForwardTo()), 
            			sessionContext.getUser(),
            			(RollForwardColleagueSessionForm)rollForwardColleagueSessionForm.clone()));
            } else {
                saveErrors(request, errors);
            }
        }

		if (request.getParameter("remove") != null) {
			solverServerService.getQueueProcessor().remove(request.getParameter("remove"));
	    }
		WebTable table = getQueueTable(request, rollForwardColleagueSessionForm);
	    if (table != null) {
	    	request.setAttribute("table", table.printTable(WebTable.getOrder(sessionContext,"rollForwardColleagueSession.ord")));
	    }
        
		setToFromSessionsInForm(rollForwardColleagueSessionForm);
  		return mapping.findForward("displayRollForwardColleagueSessionForm");
	}
	
	private WebTable getQueueTable(HttpServletRequest request, RollForwardColleagueSessionForm form) {
        WebTable.setOrder(sessionContext,"rollForwardColleagueSession.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		DateFormat df = new SimpleDateFormat("h:mma");
		List<QueueItem> queue = solverServerService.getQueueProcessor().getItems(null, null, "Colleague Roll Forward");
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, null, "rollForwardColleagueSession.do?ord=%%",
				new String[] { "Name", "Status", "Progress", "Owner", "Session", "Created", "Started", "Finished", "Output"},
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
				delete = "<img src='images/action_delete.png' border='0' onClick=\"if (confirm('Do you really want to remove this roll forward?')) document.location='rollForwardColleagueSession.do?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine("onClick=\"document.location='rollForwardColleagueSession.do?log=" + item.getId() + "';\"",
					new String[] {
						name + (delete == null ? "": " " + delete),
						item.status(),
						(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%"),
						item.getOwnerName(),
						item.getSession().getLabel(),
						df.format(item.created()),
						item.started() == null ? "" : df.format(item.started()),
						item.finished() == null ? "" : df.format(item.finished()),
						item.hasOutput() && item.finished() != null ? "<A href='"+item.getOutputFileLink()+"'>"+item.output().getName().substring(item.output().getName().lastIndexOf('.') + 1).toUpperCase()+"</A>" : ""
					},
					new Comparable[] {
						item.getId(),
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
				((ColleagueRollForwardQueueItem)item).getForm().copyTo(form);
				saveErrors(request, ((ColleagueRollForwardQueueItem)item).getErrors());
				line.setBgColor("rgb(168,187,225)");
			}

		}
		return table;
	}

	
	private class ColleagueRollForwardQueueItem extends QueueItem {
		private static final long serialVersionUID = 1L;
		private RollForwardColleagueSessionForm iForm;
		private int iProgress = 0;
		private ActionErrors iErrors = new ActionErrors();
		
		public ColleagueRollForwardQueueItem(Session session, UserContext owner, RollForwardColleagueSessionForm form) {
			super(session, sessionContext.getUser());
			iForm = form;
		}
		
		public ActionMessages getErrors() {
			return iErrors;
		}
		
		public RollForwardColleagueSessionForm getForm() {
			return iForm;
		}
		
		@Override
		protected void execute() throws Exception {
		    ColleagueSessionRollForward sessionRollForward = new ColleagueSessionRollForward(this);
              
	        Session toAcadSession = Session.getSessionById(iForm.getSessionToRollForwardTo());
			if (toAcadSession == null){
	   			iErrors.add("mustSelectSession", new ActionMessage("errors.rollForward.missingToSession"));
			}
			if (iErrors.isEmpty()){
				iForm.validateSessionToRollForwardTo(iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardColleagueSession()) {
				setStatus("Colleague Session Data ...");
				sessionRollForward.rollColleagueSessionDataForward(iErrors, iForm);	
	        }
	        iProgress++;
	        if (!iErrors.isEmpty()) {
	        	setError(new Exception(((ActionMessage)iErrors.get().next()).getValues()[0].toString()));
	        } else {
	        	log("All done.");
	        }
		}

		@Override
		public String name() {
			List<String> names = new ArrayList<String>();
        	if (iForm.getRollForwardColleagueSession()) names.add("  session");
        	String name = names.toString().replace("[", "").replace("]", "");
        	if (name.length() > 50) name = name.substring(0, 47) + "...";
        	return name;
		}

		@Override
		public double progress() {
			return 100 * iProgress / 1;
		}

		@Override
		public String type() {
			return "Colleague Roll Forward";
		}
		
	}

}
