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

import java.io.File;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.cpsolver.ifs.util.CSVFile;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.unitime.commons.web.WebTable;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;

import org.unitime.banner.form.BannerMessageResponsesForm;
import org.unitime.banner.model.BannerResponse;
import org.unitime.banner.model.dao.BannerResponseDAO;



/** 
 * based on code contributed by Dagmar Murray
 */
@Service("/bannerMessageResponses")
public class BannerMessageResponsesAction extends Action {

	@Autowired SessionContext sessionContext;

	public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
		BannerMessageResponsesForm myForm = (BannerMessageResponsesForm) form;
		
        sessionContext.checkPermission(Right.InstructionalOfferings);
        
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
         
        request.setAttribute("subjAreas",new TreeSet(SubjectArea.getUserSubjectAreas(sessionContext.getUser())));
        
        String [] respTypes = new String[] {"AUDIT", "ERROR", "SUCCESS", "WARNING"};
        request.setAttribute("respTypes", respTypes);
 
        Set subjects;
        
		if (sessionContext.hasPermission(Right.AcademicSessions)){
			request.setAttribute("managers",TimetableManager.getManagerList());
	        request.setAttribute("departments",Department.getUserDepartments(sessionContext.getUser()));
	        subjects = new TreeSet();
		} else {
	        subjects = SubjectArea.getUserSubjectAreas(sessionContext.getUser());
		}
        
        WebTable.setOrder(sessionContext,"lastChanges.ord2",request.getParameter("ord"),1);
        
        WebTable webTable = new WebTable( 9, "Banner Responses",
                "bannerMessageResponses.do?ord=%%",
                new String[] {"Date", "Subject", "Course", "Sec ID", "CRN", "XLst", "Action", "Type","Message"},
                new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left"},
                new boolean[] { false, true, true, true, true, true, true, true, true} );
     
        List responses = BannerResponseDAO.getInstance().find(
        		sessionContext.getUser().getCurrentAcademicSessionId(),
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
        
        request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext,"lastChanges.ord2")));
        
        if ("Export PDF".equals(op) && responses!=null) {
            PdfWebTable pdfTable = new PdfWebTable( 9, "Banner Responses",
                    "lastChanges.do?ord=%%",
                    new String[] {"Date", "Subject", "Course", "Sec ID", "CRN", "XLst", "Action", "Type","Message"},
                    new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left"},
                    new boolean[] { false, true, true, true, true, true, true, true, true} );
            for (Iterator i=responses.iterator();i.hasNext();)
                printLastResponseTableRow(request, pdfTable, (BannerResponse)i.next(), false);
            File file = ApplicationProperties.getTempFile("bannerResponses", "pdf");
            OutputStream out = ExportUtils.getPdfOutputStream(response, "bannerResponses");
            pdfTable.exportPdf(out, WebTable.getOrder(sessionContext,"lastChanges.ord2"));
            if (file!=null) request.setAttribute(Constants.REQUEST_OPEN_URL, "temp/"+file.getName());
        }
        
        if ("Export CSV".equals(op) && responses!=null) {
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

