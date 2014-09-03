/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
*/
package org.unitime.banner.action;

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
import org.unitime.banner.form.RollForwardBannerSessionForm;
import org.unitime.banner.util.BannerSessionRollForward;
import org.unitime.commons.Debug;
import org.unitime.commons.web.WebTable;
import org.unitime.commons.web.WebTable.WebTableLine;
import org.unitime.timetable.action.RollForwardSessionAction;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.UserContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.queue.QueueItem;
import org.unitime.timetable.util.queue.QueueProcessor;



/**
 * 
 * @author says
 *
 */
@Service("/rollForwardBannerSession")
public class RollForwardBannerSessionAction extends RollForwardSessionAction {
	/*
	 * Generated Methods
	 */

	@Autowired SessionContext sessionContext;

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
        
        RollForwardBannerSessionForm rollForwardBannerSessionForm = (RollForwardBannerSessionForm) form;
        // Get operation
        String op = request.getParameter("op");		  
                
        if (op != null && op.equals(rsc.getMessage("button.rollForward"))) {
    		sessionContext.checkPermission(rollForwardBannerSessionForm.getSessionToRollForwardTo(), "Session", Right.SessionRollForward);
            ActionMessages errors = rollForwardBannerSessionForm.validate(mapping, request);
            if (errors.size() == 0 && rollForwardBannerSessionForm.getRollForwardBannerSession().booleanValue()) {
            	QueueProcessor.getInstance().add(new BannerRollForwardQueueItem(
            			SessionDAO.getInstance().get(rollForwardBannerSessionForm.getSessionToRollForwardTo()), 
            			sessionContext.getUser(),
            			(RollForwardBannerSessionForm)rollForwardBannerSessionForm.clone()));
            } else {
                saveErrors(request, errors);
            }
        }

		if (request.getParameter("remove") != null) {
			QueueProcessor.getInstance().remove(Long.valueOf(request.getParameter("remove")));
	    }
		WebTable table = getQueueTable(request, rollForwardBannerSessionForm);
	    if (table != null) {
	    	request.setAttribute("table", table.printTable(WebTable.getOrder(sessionContext,"rollForwardBannerSession.ord")));
	    }
        
		setToFromSessionsInForm(rollForwardBannerSessionForm);
  		return mapping.findForward("displayRollForwardBannerSessionForm");
	}
	
	private WebTable getQueueTable(HttpServletRequest request, RollForwardBannerSessionForm form) {
        WebTable.setOrder(sessionContext,"rollForwardBannerSession.ord",request.getParameter("ord"),1);
		String log = request.getParameter("log");
		DateFormat df = new SimpleDateFormat("h:mma");
		List<QueueItem> queue = QueueProcessor.getInstance().getItems(null, null, "Banner Roll Forward");
		if (queue.isEmpty()) return null;
		WebTable table = new WebTable(9, null, "rollForwardBannerSession.do?ord=%%",
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
				delete = "<img src='images/action_delete.png' border='0' onClick=\"if (confirm('Do you really want to remove this roll forward?')) document.location='rollForwardBannerSession.do?remove="+item.getId()+"'; event.cancelBubble=true;\">";
			}
			WebTableLine line = table.addLine("onClick=\"document.location='rollForwardBannerSession.do?log=" + item.getId() + "';\"",
					new String[] {
						name + (delete == null ? "": " " + delete),
						item.status(),
						(item.progress() <= 0.0 || item.progress() >= 1.0 ? "" : String.valueOf(Math.round(100 * item.progress())) + "%"),
						item.getOwnerName(),
						item.getSession().getLabel(),
						df.format(item.created()),
						item.started() == null ? "" : df.format(item.started()),
						item.finished() == null ? "" : df.format(item.finished()),
						item.output() == null ? "" : "<A href='temp/"+item.output().getName()+"'>"+item.output().getName().substring(item.output().getName().lastIndexOf('.') + 1).toUpperCase()+"</A>"
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
				((BannerRollForwardQueueItem)item).getForm().copyTo(form);
				saveErrors(request, ((BannerRollForwardQueueItem)item).getErrors());
				line.setBgColor("rgb(168,187,225)");
			}

		}
		return table;
	}

	
	private class BannerRollForwardQueueItem extends QueueItem {
		private RollForwardBannerSessionForm iForm;
		private int iProgress = 0;
		private ActionErrors iErrors = new ActionErrors();
		
		public BannerRollForwardQueueItem(Session session, UserContext owner, RollForwardBannerSessionForm form) {
			super(session, sessionContext.getUser());
			iForm = form;
		}
		
		public ActionMessages getErrors() {
			return iErrors;
		}
		
		public RollForwardBannerSessionForm getForm() {
			return iForm;
		}
		
		@Override
		protected void execute() throws Exception {
		    BannerSessionRollForward sessionRollForward = new BannerSessionRollForward();
              
	        Session toAcadSession = Session.getSessionById(iForm.getSessionToRollForwardTo());
			if (toAcadSession == null){
	   			iErrors.add("mustSelectSession", new ActionMessage("errors.rollForward.missingToSession"));
			}
			if (iErrors.isEmpty()){
				iForm.validateSessionToRollForwardTo(iErrors);
			}
        	if (iErrors.isEmpty() && iForm.getRollForwardBannerSession()) {
				setStatus("Banner Session Data ...");
				sessionRollForward.rollBannerSessionDataForward(iErrors, iForm);	
	        }
	        iProgress++;
	        if (!iErrors.isEmpty()) {
	        	setError(new Exception(((ActionMessage)iErrors.get().next()).getValues()[0].toString()));
	        }
		}

		@Override
		public String name() {
			List<String> names = new ArrayList<String>();
        	if (iForm.getRollForwardBannerSession()) names.add("banner session");
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
			return "Banner Roll Forward";
		}
		
	}

}
