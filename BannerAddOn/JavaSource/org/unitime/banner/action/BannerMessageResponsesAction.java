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

import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.struts2.convention.annotation.Action;
import org.apache.struts2.convention.annotation.Result;
import org.apache.struts2.tiles.annotation.TilesDefinition;
import org.apache.struts2.tiles.annotation.TilesPutAttribute;
import org.unitime.commons.web.WebTable;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.ExportUtils;
import org.unitime.timetable.webutil.PdfWebTable;

import org.unitime.banner.form.BannerMessageResponsesForm;
import org.unitime.banner.model.BannerResponse;
import org.unitime.banner.model.dao.BannerResponseDAO;



/** 
 * based on code contributed by Dagmar Murray
 */
@Action(value = "bannerMessageResponses", results = {
		@Result(name = "displayBannerMessageResponsesForm", type = "tiles", location = "bannerMessageResponses.tiles")
	})
@TilesDefinition(name = "bannerMessageResponses.tiles", extend = "baseLayout", putAttributes =  {
		@TilesPutAttribute(name = "title", value = "Banner Message Responses"),
		@TilesPutAttribute(name = "body", value = "/banner/bannerMessageResponses.jsp")
	})
public class BannerMessageResponsesAction extends UniTimeAction<BannerMessageResponsesForm> {
	private static final long serialVersionUID = 629602594484511388L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);

	@Override
	public String execute() throws Exception {
		if (form == null) form = new BannerMessageResponsesForm();
		
        sessionContext.checkPermission(Right.InstructionalOfferings);
        
        // Read operation to be performed
        if (op == null) op = form.getOp();
        
		// allow a max of 1000 messages
		if (form.getN()>1000){
			form.setN(1000);
		} else if (form.getN()<1){
			form.setN(100);
		}
		
        if (MSG.actionFilterApply().equals(op) || MSG.actionRefreshLog().equals(op) || MSG.actionExportCsv().equals(op)|| MSG.actionExportPdf().equals(op)) {
        	form.save(request);
        } else {
            form.load(request);
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
        
        WebTable.setOrder(sessionContext,"bannerMessageResponses.ord",request.getParameter("ord"),1);
        
        WebTable webTable = new WebTable( 9, BMSG.sectBannerResponses(),
                "bannerMessageResponses.action?ord=%%",
                new String[] {
                		MSG.columnDate(),
                		MSG.columnSubject(),
                		MSG.columnCourse(),
                		BMSG.colSecId(),
                		BMSG.colCRN(),
                		BMSG.colXlst(),
                		BMSG.colAction(),
                		BMSG.colType(),
                		BMSG.colMessage()},
                new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left"},
                new boolean[] { false, true, true, true, true, true, true, true, true} );
     
        List responses = BannerResponseDAO.getInstance().find(
        		sessionContext.getUser().getCurrentAcademicSessionId(),
        		(form.getStartDate() == null ? null : form.getStartDate()),
        		(form.getStopDate() == null ? null : form.getStopDate()),
                (form.getSubjAreaId() == null || form.getSubjAreaId().longValue() < 0 ? null : form.getSubjAreaId()),
                subjects,
               (form.getManagerId()==null || form.getManagerId().longValue()<0?null:form.getManagerId()), 
                (form.getDepartmentId()==null || form.getDepartmentId().longValue()<0?null:form.getDepartmentId()),
                (form.getCourseNumber() == null ? null : form.getCourseNumber()),
                (form.getCrn() == null ? null : form.getCrn()),
                (form.getXlst() == null ? null : form.getXlst()),
                (form.getMessage() == null ? null : form.getMessage()),
                form.getN(),
                form.getShowHistory() == null ? false : form.getShowHistory().booleanValue(),
                form.getActionAudit() == null ? false : form.getActionAudit().booleanValue(),
                form.getActionUpdate() == null ? false : form.getActionUpdate().booleanValue(),
                form.getActionDelete() == null ? false : form.getActionDelete().booleanValue(),
                form.getTypeSuccess() == null ? false : form.getTypeSuccess().booleanValue(),
                form.getTypeError() == null ? false : form.getTypeError().booleanValue(),
                form.getTypeWarning() == null ? false : form.getTypeWarning().booleanValue());
		
        if (responses!=null) {
            for (Iterator i=responses.iterator();i.hasNext();)
                printLastResponseTableRow(webTable, (BannerResponse)i.next(), true);
        }
        
        request.setAttribute("table", webTable.printTable(WebTable.getOrder(sessionContext,"bannerMessageResponses.ord")));
        
        if (MSG.actionExportPdf().equals(op) && responses!=null) {
            PdfWebTable pdfTable = new PdfWebTable( 9, BMSG.sectBannerResponses(),
            		"bannerMessageResponses.action?ord=%%",
                    new String[] {
                    		MSG.columnDate(),
                    		MSG.columnSubject(),
                    		MSG.columnCourse(),
                    		BMSG.colSecId(),
                    		BMSG.colCRN(),
                    		BMSG.colXlst(),
                    		BMSG.colAction(),
                    		BMSG.colType(),
                    		BMSG.colMessage()
                    },
                    new String[] {"left", "left", "left", "left", "left", "left", "left", "left", "left"},
                    new boolean[] { false, true, true, true, true, true, true, true, true} );
            for (Iterator i=responses.iterator();i.hasNext();)
                printLastResponseTableRow(pdfTable, (BannerResponse)i.next(), false);
            ExportUtils.exportPDF(pdfTable, WebTable.getOrder(sessionContext,"bannerMessageResponses.ord"), response, "bannerResponses");
            return null;
        }
        
        if (MSG.actionExportCsv().equals(op) && responses!=null) {
        	ExportUtils.exportCSV(webTable, WebTable.getOrder(sessionContext,"bannerMessageResponses.ord"), response, "bannerResponses");
        	return null;
        }
        
  		return "displayBannerMessageResponsesForm";
	}
    
    private int printLastResponseTableRow(WebTable webTable, BannerResponse lastResponse, boolean html) {
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

