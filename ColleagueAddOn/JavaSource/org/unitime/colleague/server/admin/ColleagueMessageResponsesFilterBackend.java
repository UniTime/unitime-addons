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
package org.unitime.colleague.server.admin;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.defaults.CommonValues;
import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.defaults.UserProperty;
import org.unitime.timetable.gwt.colleague.ColleagueMessageResponsesPage.ColleagueMessageResponsesFilterRequest;
import org.unitime.timetable.gwt.colleague.ColleagueMessageResponsesPage.ColleagueMessageResponsesFilterResponse;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.FilterInterface.FilterParameterInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(ColleagueMessageResponsesFilterRequest.class)
public class ColleagueMessageResponsesFilterBackend implements GwtRpcImplementation<ColleagueMessageResponsesFilterRequest, ColleagueMessageResponsesFilterResponse>{
	protected static CourseMessages MESSAGES = Localization.create(CourseMessages.class);
	protected static ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);

	@Override
	public ColleagueMessageResponsesFilterResponse execute(ColleagueMessageResponsesFilterRequest request, SessionContext context) {
		context.checkPermission(Right.InstructionalOfferings);
		ColleagueMessageResponsesFilterResponse filter = new ColleagueMessageResponsesFilterResponse();
		
		FilterParameterInterface subjectCode = new FilterParameterInterface();
		subjectCode.setName("subjectCode");
		subjectCode.setType("list");
		subjectCode.setMultiSelect(false);
		subjectCode.setLabel(MESSAGES.filterSubject());
		subjectCode.addOption("", MESSAGES.itemAllSubjects());
		subjectCode.setShowSelectItem(false);
		if (ApplicationProperty.OfferingsFilterSubjectTitle.isTrue())
			for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
				subjectCode.addOption(subject.getSubjectAreaAbbreviation(), subject.getLabel());
		else
			for (SubjectArea subject: SubjectArea.getUserSubjectAreas(context.getUser()))
				subjectCode.addOption(subject.getSubjectAreaAbbreviation(), subject.getSubjectAreaAbbreviation());
		subjectCode.setDefaultValue(context.getUser().getProperty("ColleagueMessageResponses.subjectCode", ""));
		subjectCode.setEnterToSubmit(true);
		filter.addParameter(subjectCode);
		
		FilterParameterInterface courseNbr = new FilterParameterInterface();
		courseNbr.setName("courseNbr");
		courseNbr.setLabel(MESSAGES.filterCourseNumber());
		courseNbr.setType("courseNumber");
		courseNbr.setDefaultValue(context.getUser().getProperty("ColleagueMessageResponses.courseNbr", ""));
		courseNbr.setConfig("subjectAbbv=${subjectCode};notOffered=include");
		filter.addParameter(courseNbr);
		
		FilterParameterInterface synonym = new FilterParameterInterface();
		synonym.setName("synonym");
		synonym.setLabel(CMSG.colColleagueSynonym() + ":");
		synonym.setType("text");
		synonym.setDefaultValue(context.getUser().getProperty("ColleagueMessageResponses.synonym", ""));
		filter.addParameter(synonym);

		filter.addParameter(createToggle(context, CMSG.colAction() + ":", "actionAudit", CMSG.actionAudit(), true));
		filter.addParameter(createToggle(context, CMSG.colAction() + ":", "actionUpdate", CMSG.actionUpdate(), true));
		filter.addParameter(createToggle(context, CMSG.colAction() + ":", "actionDelete", CMSG.actionDelete(), true));
		filter.addParameter(createToggle(context, CMSG.colType() + ":", "typeSuccess", CMSG.typeSuccess(), true));
		filter.addParameter(createToggle(context, CMSG.colType() + ":", "typeError", CMSG.typeError(), true));
		filter.addParameter(createToggle(context, CMSG.colType() + ":", "typeWarning", CMSG.typeWarning(), true));
		
		FilterParameterInterface message = new FilterParameterInterface();
		message.setName("message");
		message.setLabel(CMSG.colMessage() + ":");
		message.setType("text");
		message.setDefaultValue(context.getUser().getProperty("ColleagueMessageResponses.message", ""));
		filter.addParameter(message);
		
		FilterParameterInterface startDate = new FilterParameterInterface();
		startDate.setName("startDate");
		startDate.setLabel(CMSG.colStartDate() + ":");
		startDate.setType("date");
		startDate.setDefaultValue(context.getUser().getProperty("ColleagueMessageResponses.startDate", ""));
		startDate.setComposite(true);
		filter.addParameter(startDate);
		
		FilterParameterInterface endDate = new FilterParameterInterface();
		endDate.setName("stopDate");
		endDate.setLabel(CMSG.colStopDate() + ":");
		endDate.setType("date");
		endDate.setDefaultValue(context.getUser().getProperty("ColleagueMessageResponses.stopDate", ""));
		endDate.setComposite(true);
		filter.addParameter(endDate);
		
		if (context.hasPermission(Right.AcademicSessions)){
			FilterParameterInterface manager = new FilterParameterInterface();
			manager.setName("manager");
			manager.setLabel(MESSAGES.filterManager());
			manager.setType("list");
			manager.setMultiSelect(false);
			manager.addOption("", MESSAGES.itemAllManagers());
			manager.setShowSelectItem(false);
			String nameFormat = UserProperty.NameFormat.get(context.getUser());
			for (TimetableManager m: TimetableManager.getManagerList())
				manager.addOption(m.getUniqueId().toString(), m.getName(nameFormat));
			manager.setDefaultValue(context.getUser().getProperty("ColleagueMessageResponses.manager", ""));
			filter.addParameter(manager);
			
			FilterParameterInterface department = new FilterParameterInterface();
			department.setName("department");
			department.setLabel(MESSAGES.filterDepartment());
			department.setType("list");
			department.setMultiSelect(false);
			department.addOption("", MESSAGES.itemAllDepartments());
			department.setShowSelectItem(false);
			for (Department d: Department.getUserDepartments(context.getUser()))
				department.addOption(d.getUniqueId().toString(), d.getLabel());
			department.setDefaultValue(context.getUser().getProperty("ColleagueMessageResponses.department", ""));
			filter.addParameter(department);
		}
		
		filter.addParameter(createToggle(context, CMSG.filterShowHistory(), "showHistory", null, false));
		
		FilterParameterInterface nbrMessages = new FilterParameterInterface();
		nbrMessages.setName("nbrMessages");
		nbrMessages.setLabel(CMSG.filterNumberOfMessages());
		nbrMessages.setType("integer");
		nbrMessages.setDefaultValue(context.getUser().getProperty("ColleagueMessageResponses.nbrMessages", "1000"));
		filter.addParameter(nbrMessages);
		
		if (subjectCode.getDefaultValue() == null && courseNbr.getDefaultValue() == null) {
			subjectCode.setDefaultValue((String)context.getAttribute(SessionAttribute.ClassesSubjectAreas));
			courseNbr.setDefaultValue((String)context.getAttribute(SessionAttribute.ClassesCourseNumber));
		}
		
		filter.setSticky(CommonValues.Yes.eq(UserProperty.StickyTables.get(context.getUser())));
		filter.setMaxSubjectsToSearchAutomatically(ApplicationProperty.MaxSubjectsToSearchAutomatically.intValue());
		filter.setCanExport(context.hasPermission(Right.InstructionalOfferingsExportPDF));
		filter.setSessionId(context.getUser().getCurrentAcademicSessionId());

		return filter;
	}
	
	protected FilterParameterInterface createToggle(SessionContext context, String section, String name, String label, Boolean defaultValue) {
		FilterParameterInterface toggle = new FilterParameterInterface();
		toggle.setName(name);
		toggle.setLabel(section);
		toggle.setType("boolean");
		toggle.setSuffix(label);
		toggle.setDefaultValue(context.getUser().getProperty("ColleagueMessageResponses." + name, defaultValue == null ? null : defaultValue ? "1" : "0"));
		return toggle;
	}

}
