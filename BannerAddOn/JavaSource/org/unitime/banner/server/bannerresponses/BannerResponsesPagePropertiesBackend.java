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
package org.unitime.banner.server.bannerresponses;

import org.unitime.timetable.defaults.SessionAttribute;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesPagePropertiesRequest;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesPagePropertiesResponse;
import org.unitime.timetable.gwt.shared.InstructorInterface.DepartmentInterface;
import org.unitime.timetable.gwt.shared.InstructorInterface.SubjectAreaInterface;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.util.Constants;

@GwtRpcImplements(BannerResponsesPagePropertiesRequest.class)

public class BannerResponsesPagePropertiesBackend implements GwtRpcImplementation<BannerResponsesPagePropertiesRequest, BannerResponsesPagePropertiesResponse>{

	@Override
	public BannerResponsesPagePropertiesResponse execute(BannerResponsesPagePropertiesRequest request,
			SessionContext context) {
		context.checkPermission(Right.InstructionalOfferings);
		BannerResponsesPagePropertiesResponse ret = new BannerResponsesPagePropertiesResponse();
		for (SubjectArea sa: SubjectArea.getUserSubjectAreas(context.getUser(), true)) {
			SubjectAreaInterface subject = new SubjectAreaInterface();
			subject.setId(sa.getUniqueId());
			subject.setAbbreviation(sa.getSubjectAreaAbbreviation());
			subject.setLabel(sa.getTitle());
			ret.addSubjectArea(subject);
		}
		for (Department d: Department.getUserDepartments(context.getUser())) {
			DepartmentInterface department = new DepartmentInterface();
			department.setId(d.getUniqueId());
			department.setDeptCode(d.getDeptCode());
			department.setLabel(d.getName());
			department.setTitle(d.getLabel());
			department.setAbbreviation(d.getAbbreviation());
			ret.addDepartment(department);
		}
		String sa = (String)context.getAttribute(SessionAttribute.OfferingsSubjectArea);
		if (Constants.ALL_OPTION_VALUE.equals(sa))
			ret.setLastSubjectAreaId(-1l);
		else if (sa != null) {
			if (sa.indexOf(',') >= 0) sa = sa.substring(0, sa.indexOf(','));
			ret.setLastSubjectAreaId(Long.valueOf(sa));
		}
		String deptId = (String)context.getAttribute(SessionAttribute.DepartmentId);
		if (deptId != null) {
			try {
				ret.setLastDepartmentId(Long.valueOf(deptId));
			} catch (NumberFormatException e) {}
		}
		
		ret.setCanSelectDepartment(context.hasPermission(Right.TimetableManagerAdd));
		ret.setCanSelectManager(context.hasPermission(Right.TimetableManagerAdd));
		ret.setShowAuditActions(true);
		ret.setShowDeleteActions(true);
		ret.setShowErrorResults(true);
		ret.setShowNoChangeResults(true);
		ret.setShowSuccessResults(true);
		ret.setShowUpdateActions(true);
		ret.setShowWarningResults(true);

		return ret;
	}

}
