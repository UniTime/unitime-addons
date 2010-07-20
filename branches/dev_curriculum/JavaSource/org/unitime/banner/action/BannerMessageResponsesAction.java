/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/



package org.unitime.banner.action;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.cpsolver.ifs.util.CSVFile;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.unitime.banner.form.BannerMessageResponsesForm;
import org.unitime.banner.model.BannerResponse;
import org.unitime.banner.model.dao.BannerResponseDAO;
import org.unitime.commons.User;
import org.unitime.commons.web.Web;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.webutil.PdfWebTable;



/** 
 * based on code contributed by Dagmar Murray
 */
public class BannerMessageResponsesAction extends Action {

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		BannerMessageResponsesForm myForm = (BannerMessageResponsesForm) form;
		
        // Check Access
        if (!Web.isLoggedIn( request.getSession() )) {
            throw new Exception ("Access Denied.");
        }
        
        // Read operation to be performed
        String op = (myForm.getOp()!=null?myForm.getOp():request.getParameter("op"));
        
		// allow a max of 1000 messages
		if (myForm.getN()>1000){
			myForm.setN(1000);
		} else if (myForm.getN()<1){
			myForm.setN(100);
		}
		
        if ("Apply".equals(op) || "Refresh".equals(op)||"Export CSV".equals(op)|| "Export PDF".equals(op)) {
        	myForm.save(request);
        } else {
            myForm.load(request);
        }
        
        Session session = Session.getCurrentAcadSession(Web.getUser(request.getSession()));
 
        User user = Web.getUser(request.getSession());
        request.setAttribute("subjAreas",new TreeSet(TimetableManager.getSubjectAreas(user)));
        
        String [] respTypes = new String[] {"AUDIT", "ERROR", "SUCCESS", "WARNING"};
        request.setAttribute("respTypes", respTypes);
 
        Set subjects;
        
		if (user.isAdmin()){
			request.setAttribute("managers",TimetableManager.getManagerList());
	        request.setAttribute("departments",Department.findAll(session.getUniqueId()));
	        subjects = new TreeSet();
		} else {
	        subjects = TimetableManager.getSubjectAreas(user);
		}
        
        WebTable.setOrder(request.getSession(),"lastChanges.ord2",request.getParameter("ord"),1);
        
        WebTable webTable = new WebTable( 9, "Banner Responses",
                "bannerMessageResponses.do?ord=%%",
                new String[] {"Date", "Subject", "Course", "Sec ID", "CRN", "XLst", "Action", "Type","Message"},
                new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left"},
                new boolean[] { false, true, true, true, true, true, true, true, true} );
     
        List responses = BannerResponseDAO.getInstance().find(
                session.getUniqueId(), 
        		(myForm.getStartDate() == null ? null : myForm.getStartDate()),
        		(myForm.getStopDate() == null ? null : myForm.getStopDate()),
                (myForm.getSubjAreaId() == null || myForm.getSubjAreaId().longValue() < 0 ? null : myForm.getSubjAreaId()),
                subjects,
               (myForm.getManagerId()==null || myForm.getManagerId().longValue()<0?null:myForm.getManagerId()), 
                (myForm.getDepartmentId()==null || myForm.getDepartmentId().longValue()<0?null:myForm.getDepartmentId()),
                (myForm.getCourseNumber() == null ? null : myForm.getCourseNumber()),
                (myForm.getCrn() == null ? null : myForm.getCrn()),
                (myForm.getXlst() == null ? null : myForm.getXlst()),
                (myForm.getMessage() == null ? null : myForm.getMessage()),
                myForm.getN(),
                myForm.getShowHistory() == null ? false : myForm.getShowHistory().booleanValue(),
                myForm.getActionAudit() == null ? false : myForm.getActionAudit().booleanValue(),
                myForm.getActionUpdate() == null ? false : myForm.getActionUpdate().booleanValue(),
                myForm.getActionDelete() == null ? false : myForm.getActionDelete().booleanValue(),
                myForm.getTypeSuccess() == null ? false : myForm.getTypeSuccess().booleanValue(),
                myForm.getTypeError() == null ? false : myForm.getTypeError().booleanValue(),
                myForm.getTypeWarning() == null ? false : myForm.getTypeWarning().booleanValue());
		
        if (responses!=null) {
            for (Iterator i=responses.iterator();i.hasNext();)
                printLastResponseTableRow(request, webTable, (BannerResponse)i.next(), true);
        }
        
        request.setAttribute("table", webTable.printTable(WebTable.getOrder(request.getSession(),"lastChanges.ord2")));
        
        if ("Export PDF".equals(op) && responses!=null) {
            PdfWebTable pdfTable = new PdfWebTable( 9, "Banner Responses",
                    "lastChanges.do?ord=%%",
                    new String[] {"Date", "Subject", "Course", "Sec ID", "CRN", "XLst", "Action", "Type","Message"},
                    new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left"},
                    new boolean[] { false, true, true, true, true, true, true, true, true} );
            for (Iterator i=responses.iterator();i.hasNext();)
                printLastResponseTableRow(request, pdfTable, (BannerResponse)i.next(), false);
            File file = ApplicationProperties.getTempFile("bannerResponses", "pdf");
            pdfTable.exportPdf(file, WebTable.getOrder(request.getSession(),"lastChanges.ord2"));
            if (file!=null) request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
        
        if ("Export CSV".equals(op) && responses!=null) {
        	String messageIds = "";
        	CSVFile csvFile = webTable.toCSVFile(0);

        	File file = ApplicationProperties.getTempFile("bannerResponses", "csv");
        	csvFile.save(file);
        	request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
        
  		return mapping.findForward("displayBannerMessageResponsesForm");
	}
    
    private int printLastResponseTableRow(HttpServletRequest request, WebTable webTable, BannerResponse lastResponse, boolean html) {
        if (lastResponse==null) return 0;
        webTable.addLine(null,
                new String[] {
                    BannerResponse.sDF.format(lastResponse.getActivityDate()),
                    lastResponse.getSubjectCode(),
                    lastResponse.getCourseNumber(),
                    lastResponse.getSectionNumber(),
                    lastResponse.getCrn(),
                    lastResponse.getXlstGroup(),
                    lastResponse.getAction(),
                    lastResponse.getType(),
                    lastResponse.getMessage()
                     },
                new Comparable[] {
        		lastResponse.dateSortOrder(),
                lastResponse.getSubjectCode(),
                lastResponse.getCourseNumber(),
                lastResponse.getSectionNumber(),
                lastResponse.getCrn(),
                lastResponse.getXlstGroup(),
                lastResponse.getAction(),
                lastResponse.getType(),
                lastResponse.getMessage()
                    });
        return 1;
    }
    
}

