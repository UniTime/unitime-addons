package org.unitime.colleague.server.admin;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.unitime.colleague.model.ColleagueResponse;
import org.unitime.colleague.queueprocessor.exception.LoggableException;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.client.tables.TableInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.CellInterface;
import org.unitime.timetable.gwt.client.tables.TableInterface.LineInterface;
import org.unitime.timetable.gwt.colleague.ColleagueMessageResponsesPage.ColleagueMessageResponsesRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.FilterInterface;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Formats;

@GwtRpcImplements(ColleagueMessageResponsesRequest.class)
public class ColleagueMessageResponsesBackend implements GwtRpcImplementation<ColleagueMessageResponsesRequest, TableInterface>{
	protected static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected static ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);

	@Override
	public TableInterface execute(ColleagueMessageResponsesRequest request, SessionContext context) {
		context.checkPermission(Right.InstructionalOfferings);
	
		if (request.getFilter() instanceof FilterInterface) {
			for (FilterParameterInterface p: ((FilterInterface)request.getFilter()).getParameters()) {
				if (p.getValue() != null)
					context.getUser().setProperty("ColleagueMessageResponses." + p.getName(), p.getValue());
			}
		}

		TableInterface table = new TableInterface();
		table.setSticky(CommonValues.Yes.eq(UserProperty.StickyTables.get(context.getUser())));
		table.setId("ColleagueMessageResponses");
		table.setDefaultSortCookie("!" + MSG.columnDate());
		table.setName(CMSG.sectColleagueResponses());

		LineInterface header = table.addHeader();
		header.addCell(MSG.columnDate());
        header.addCell(MSG.columnSubject());
        header.addCell(MSG.columnCourse());
        header.addCell(CMSG.colSecId());
        header.addCell(CMSG.colColleagueSynonym());
        header.addCell(CMSG.colAction());
    	header.addCell(CMSG.colType());
    	header.addCell(CMSG.colMessage());
        for (CellInterface cell: header.getCells()) {
    		cell.setClassName("WebTableHeader");
    		cell.setText(cell.getText().replace("<br>", "\n"));
    		cell.addStyle("white-space: pre-wrap;");
    		cell.setSortable(true);
    	}
        
        String nbrMessages = request.getFilter().getParameterValue("nbrMessages");
        int n = 1000;
        if (nbrMessages != null && !nbrMessages.isEmpty()) {
        	try {
        		n = Integer.valueOf(nbrMessages);
        	} catch (NumberFormatException e) {}
        }
        Set<SubjectArea> subjects = SubjectArea.getUserSubjectAreas(context.getUser());
        
		try {
        List<ColleagueResponse> responses = ColleagueResponse.find(
        		context.getUser().getCurrentAcademicSessionId(),
        		toDate(request.getFilter().getParameterValue("startDate")),
        		toDate(request.getFilter().getParameterValue("stopDate")),
        		toString(request.getFilter().getParameterValue("subjectCode")),
        		subjects,
        		toLong(request.getFilter().getParameterValue("manager")),
        		toLong(request.getFilter().getParameterValue("department")),
        		toString(request.getFilter().getParameterValue("courseNbr")),
        		toString(request.getFilter().getParameterValue("synonym")),
        		toString(request.getFilter().getParameterValue("message")),
        		n,
        		toBoolean(request.getFilter().getParameterValue("showHistory")),
        		toBoolean(request.getFilter().getParameterValue("actionAudit")),
        		toBoolean(request.getFilter().getParameterValue("actionUpdate")),
        		toBoolean(request.getFilter().getParameterValue("actionDelete")),
        		toBoolean(request.getFilter().getParameterValue("typeSuccess")),
        		toBoolean(request.getFilter().getParameterValue("typeError")),
        		toBoolean(request.getFilter().getParameterValue("typeWarning"))
        		);
			if (responses != null)
				for (ColleagueResponse lastResponse: responses) {
					LineInterface line = table.addLine();
					line.addCell(ColleagueResponse.sDF.format(lastResponse.getActivityDate())).setComparable(lastResponse.dateSortOrder());
					line.addCell(lastResponse.getSubjectCode());
					line.addCell(lastResponse.getCourseNumber());
					line.addCell(lastResponse.getSectionNumber());
					line.addCell(lastResponse.getColleagueId());
					line.addCell(lastResponse.getAction());
					line.addCell(lastResponse.getType());
					line.addCell(lastResponse.getMessage());
				}
		} catch (LoggableException e) {
			throw new GwtRpcException(e.getMessage(), e);
		}
		
		return table;
	}

	protected static String toString(String filter) {
		if (filter == null || filter.isEmpty()) return null;
		return filter;
	}
	
	protected static Long toLong(String filter) {
		if (filter == null || filter.isEmpty()) return null;
		try {
			return Long.valueOf(filter);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	protected static boolean toBoolean(String filter) {
		if (filter == null || filter.isEmpty()) return false;
		return "true".equalsIgnoreCase(filter) || "1".equalsIgnoreCase(filter) || "on".equalsIgnoreCase(filter);
	}
	
	protected static Date toDate(String filter) {
		if (filter == null || filter.isEmpty()) return null;
		try {
			return Formats.getDateFormat(Formats.Pattern.DATE_ENTRY_FORMAT).parse(filter);
		} catch (ParseException e) {
			return null;
		}
	}

}
