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

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.unitime.banner.model.BannerResponse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerResponseDAO;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.BannerGwtConstants;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.BannerResponseInterface.BannerResponsesFilterRpcRequest;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse;
import org.unitime.timetable.gwt.shared.EventInterface.FilterRpcResponse.Entity;
import org.unitime.timetable.model.Department;
import org.unitime.timetable.model.SolverGroup;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.model.dao.DepartmentDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.model.dao.TimetableManagerDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.FilterBoxBackend;
import org.unitime.timetable.util.DateUtils;
import org.unitime.timetable.util.Formats;

@GwtRpcImplements(BannerResponsesFilterRpcRequest.class)
public class BannerResponsesFilterBackend extends FilterBoxBackend<BannerResponsesFilterRpcRequest> {
	protected static BannerGwtMessages MSG = Localization.create(BannerGwtMessages.class);
	protected static BannerGwtConstants CONSTANTS = Localization.create(BannerGwtConstants.class);

	@Override
	public FilterRpcResponse execute(BannerResponsesFilterRpcRequest request, SessionContext context) {
		context.checkPermission(Right.InstructionalOfferings);
		return super.execute(request, context);
	}

	@Override
	public void load(BannerResponsesFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {
		Set<Department> startingDepts = Department.getUserDepartments(context.getUser());
		Set<Department> userDepts = new TreeSet<Department>();
		Set<SubjectArea> userSubjects = new TreeSet<SubjectArea>();
		Set<TimetableManager> availManagers = new TreeSet<TimetableManager>(); 
		Set<String> managerOptions = null;
		
		
		if (request.hasOption("manager")) {
			managerOptions = request.getOptions().get("manager");
			if (managerOptions.isEmpty()) {
				managerOptions = null;
			}
		}
		Set<String> departmentOptions = null;
		if (request.hasOption("department")) {
			departmentOptions = request.getOptions().get("department");
			if (departmentOptions.isEmpty()) {
				departmentOptions = null;
			}
		}
		depts: for (Department d : startingDepts) {
			if (!d.getSubjectAreas().isEmpty() && d.getSolverGroup() != null) {
				if (managerOptions != null) {
					for (TimetableManager tm : d.getSolverGroup().getTimetableManagers()) {
						Set<String> ttmExternalIds = request.getOptions().get("manager");
						for (String id : ttmExternalIds) {
							if (tm.getExternalUniqueId().equals(id)) {
								userDepts.add(d);
								availManagers.addAll(d.getSolverGroup().getTimetableManagers());
								userSubjects.addAll(d.getSubjectAreas());
								continue depts;
							}
						}
					}
				} else if (departmentOptions != null) {
					if (departmentOptions.contains(d.getDeptCode())) {
						userDepts.add(d);
						availManagers.addAll(d.getSolverGroup().getTimetableManagers());
						userSubjects.addAll(d.getSubjectAreas());
					} 
				} else {
					userDepts.add(d);
					userSubjects.addAll(d.getSubjectAreas());
					availManagers.addAll(d.getTimetableManagers());
				}
			}
		}
		
		Map<Long, Entity> subjects = new HashMap<Long, Entity>();
		for (SubjectArea sa : userSubjects) {
			Entity subj = subjects.get(sa.getUniqueId());
			if (subj == null) {
				subj = new Entity(sa.getUniqueId(), BannerSection.getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(sa, null), sa.getSubjectAreaAbbreviation());
				subjects.put(subj.getUniqueId(), subj);
			}
		}
		response.add("subj",  new TreeSet<Entity>(subjects.values()));
		
		Map<Long, Entity> departments = new HashMap<Long, Entity>();
		for (Department d : userDepts) {
			Entity dept = departments.get(d.getUniqueId());
			if (dept == null && !d.getSubjectAreas().isEmpty()) {
				dept = new Entity(d.getUniqueId(), d.getDeptCode(), d.getName());
				dept.setCount(d.getSubjectAreas().size());
				departments.put(dept.getUniqueId(), dept);
			}
		}
		response.add("department", new TreeSet<Entity>(departments.values()));
		
		Map<Long, Entity> managers = new HashMap<Long, Entity>();
		for (TimetableManager  ttm: availManagers) {
			Entity mgr = managers.get(ttm.getUniqueId());
			if (mgr == null) {
				mgr = new Entity(ttm.getUniqueId(), ttm.getExternalUniqueId(), ttm.getName());
				int subjCount = 0;
				for (SolverGroup sg : ttm.getSolverGroups()) {
					if(sg.getSession().getUniqueId().equals(context.getUser().getCurrentAcademicSessionId())) {
						for(Department d : sg.getDepartments()) {
						subjCount += d.getSubjectAreas().size();
						}
					}
				}
				mgr.setCount(subjCount);
				managers.put(mgr.getUniqueId(), mgr);
			}
		}		
		response.add("manager", new TreeSet<Entity>(managers.values()));
		
		List<Entity> action = new ArrayList<Entity>();
		action.add(new Entity(0l, "Update", CONSTANTS.bannerMessageActionType()[0], "translated-value", CONSTANTS.bannerMessageActionType()[0]));
		action.add(new Entity(1l, "Delete", CONSTANTS.bannerMessageActionType()[1], "translated-value", CONSTANTS.bannerMessageActionType()[1]));
		action.add(new Entity(2l, "Audit", CONSTANTS.bannerMessageActionType()[2], "translated-value", CONSTANTS.bannerMessageActionType()[2]));
		response.add("action", action);

				
		List<Entity> responseType = new ArrayList<Entity>();
		responseType.add(new Entity(0l, "Success", CONSTANTS.bannerMessageResponseType()[0], "translated-value", CONSTANTS.bannerMessageResponseType()[0]));
		responseType.add(new Entity(1l, "Warning", CONSTANTS.bannerMessageResponseType()[1], "translated-value", CONSTANTS.bannerMessageResponseType()[1]));
		responseType.add(new Entity(2l, "Error", CONSTANTS.bannerMessageResponseType()[2], "translated-value", CONSTANTS.bannerMessageResponseType()[2]));
		responseType.add(new Entity(3l, "No Change", CONSTANTS.bannerMessageResponseType()[3], "translated-value", CONSTANTS.bannerMessageResponseType()[3]));
		response.add("rspType", responseType);

	}
		

	@Override
	public void suggestions(BannerResponsesFilterRpcRequest request, FilterRpcResponse response,
			SessionContext context) {
		
	}

	@Override
	public void enumarate(BannerResponsesFilterRpcRequest request, FilterRpcResponse response, SessionContext context) {

		for (BannerResponse bannerResponse: bannerResponses(request.getSessionId(), request.getOptions(), new Query(request.getText()), null, Department.getUserDepartments(context.getUser()),context.getUser().getCurrentAuthority().hasRight(Right.DepartmentIndependent))) {
			response.addResult(new Entity(
					bannerResponse.getUniqueId(),
					bannerResponse.filterLabelShort(),
					bannerResponse.filterLabelLong()
					));
		}

	}
	
	private static ArrayList<HashSet<String>> subjectsForDept(String dept, Long sessionId) {
		HashSet<String> subjects = new HashSet<String>();
		HashSet<String> bannerSubjects = new HashSet<String>();
		ArrayList<HashSet<String>> allSubjects = new ArrayList<HashSet<String>>();
		allSubjects.add(subjects);
		allSubjects.add(bannerSubjects);
		StringBuffer sb = new StringBuffer();
		sb.append("select sa ")
		  .append(" from SubjectArea sa ")
		  .append(" where sa.session.uniqueId = ")
		  .append(sessionId)
		  .append(" and (sa.department.deptCode = '")
		  .append(dept)
		  .append("'  or sa.department.abbreviation = '")
		  .append(dept)
		  .append("' or sa.department.name like '%")
		  .append(dept)
		  .append("%')")
		  ;
		for ( SubjectArea sa : DepartmentDAO.getInstance().getSession().createQuery(sb.toString(), SubjectArea.class).setCacheable(true).list()) {
			String bannerAbbv = BannerSection.getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(sa, null);
			subjects.add(sa.getSubjectAreaAbbreviation());
			if (bannerAbbv != sa.getSubjectAreaAbbreviation()) {
				bannerSubjects.add(bannerAbbv);
			}
		}
		return(allSubjects);
	}

	private static ArrayList<HashSet<String>> subjectsForManager(String managerId, Long sessionId) {
		HashSet<String> subjects = new HashSet<String>();
		HashSet<String> bannerSubjects = new HashSet<String>();
		ArrayList<HashSet<String>> allSubjects = new ArrayList<HashSet<String>>();
		allSubjects.add(subjects);
		allSubjects.add(bannerSubjects);
		StringBuffer sb = new StringBuffer();
		sb.append("select sa ")
		  .append(" from SubjectArea sa, TimetableManager tm inner join tm.departments as d ")
		  .append(" where sa.session.uniqueId = ")
		  .append(sessionId)
		  .append(" and tm.externalUniqueId like '")
		  .append(managerId)
		  .append("%'")
		  .append(" and sa.department = d")
		  .append(" order by sa.subjectAreaAbbreviation")
		  ;
		for ( SubjectArea sa : TimetableManagerDAO.getInstance().getSession().createQuery(sb.toString(), SubjectArea.class).setCacheable(true).list()) {
			String bannerAbbv = BannerSection.getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(sa, null);
			subjects.add(sa.getSubjectAreaAbbreviation());
			if (bannerAbbv != sa.getSubjectAreaAbbreviation()) {
				bannerSubjects.add(bannerAbbv);
			}
		}
		return(allSubjects);
	}
	
	public static class BannerResponsesMatcher implements Query.TermMatcher {
		private Set<String> iManaged;
		private BannerResponse iBannerResponse;
		private Long iSessionId;
		
		public BannerResponsesMatcher(BannerResponse bannerResponse, Long sessionId, Set<Department> managed) {
			iBannerResponse = bannerResponse;
			iSessionId = sessionId;
			iManaged = new HashSet<String>();
			for (Department d : managed) {
				for (SubjectArea sa : d.getSubjectAreas()) {
					iManaged.add(sa.getSubjectAreaAbbreviation());
					String bannerAbbv = BannerSection.getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(sa, null);
					if (!bannerAbbv.equals(sa.getSubjectAreaAbbreviation())) {
						iManaged.add(sa.getSubjectAreaAbbreviation());
					}
				}
			}
		}
		

		@Override
		public boolean match(String attr, String term) {
			if (term.isEmpty()) return true;
			if (attr == null || "dept".equals(attr) || "department".equals(attr)) {
				if (iBannerResponse.getSubjectArea() != null && iManaged.contains(iBannerResponse.getSubjectArea().getSubjectAreaAbbreviation())) {
					if (iBannerResponse.getSubjectArea().getDepartment().getDeptCode().equals(term)) {
						return true;
					}
				} else {
					if (iBannerResponse.getSubjectCode() != null) {
						ArrayList<HashSet<String>> subjects = subjectsForDept(term, iSessionId);
						if (subjects.get(0).contains(iBannerResponse.getSubjectCode())) {
							return true;
						}
						if (subjects.get(1).contains(iBannerResponse.getSubjectCode())) {
							return true;
						}
					}
				}
			}
			if (attr == null || "subj".equals(attr) || "subject".equals(attr)){
				if (iManaged.contains(term)
				&& iBannerResponse.getSubjectArea() != null 
				&& iBannerResponse.getSubjectArea().getSubjectAreaAbbreviation().equals(term)) {
					return true;
				}
			}
			
			if (attr == null || "course".equals(attr)) {
				if (term.contains(" ")) {
					String subj = term.substring(0, term.lastIndexOf(" "));
					String crs = term.substring(term.lastIndexOf(" ") + 1);
					if (iManaged.contains(subj)
							&& iBannerResponse.getSubjectArea() != null 
							&& iBannerResponse.getSubjectArea().getSubjectAreaAbbreviation().equals(subj)
						    && iBannerResponse.getCourseNumber() != null
						    && iBannerResponse.getCourseNumber().substring(0, (crs.length() > 5 ? 5 : crs.length())).equals(crs.substring(0, (crs.length() > 5 ? 5 : crs.length())))) {
						return true;
					} 					
				}
			}
			return false;
		}

	}
	
	private static ArrayList<Set<String>> getSubjectsForStrings(Long sessionId, Set<String> subj){
		Set<String> searchSubjs = new TreeSet<String>();
		Set<String> bannerSearchSubjs = new TreeSet<String>();
		ArrayList<Set<String>> subjects = new ArrayList<Set<String>>();
		subjects.add(searchSubjs);
		subjects.add(bannerSearchSubjs);
		BannerSession bs = BannerSession.findBannerSessionForSession(sessionId, null);
		
		if (bs != null && bs.isUseSubjectAreaPrefixAsCampus() != null &&  bs.isUseSubjectAreaPrefixAsCampus()) {
			String delimiter = (bs.getSubjectAreaPrefixDelimiter() != null && !bs.getSubjectAreaPrefixDelimiter().equals("") ? bs.getSubjectAreaPrefixDelimiter() : " - ");
			for (String s : subj) {
				if (s.indexOf(delimiter) >= 0) {
					searchSubjs.add(s);
					bannerSearchSubjs.add(s.substring(s.indexOf(delimiter) + delimiter.length()));				
				} else {
					List<SubjectArea> matchSubjs = SubjectAreaDAO.getInstance()
							.getSession()
							.createQuery("from SubjectArea sa where sa.session.uniqueId = :sessId and sa.subjectAreaAbbreviation like ':" + delimiter+ "subj'", SubjectArea.class)
							.setParameter("sessId", sessionId)
							.setParameter("subj", s)
							.list();
					for (SubjectArea sa : matchSubjs) {
						searchSubjs.add(sa.getSubjectAreaAbbreviation());
						bannerSearchSubjs.add(BannerSection.getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(sa, null));
					}
				}
			}
			
		} else {
			searchSubjs.addAll(subj);
		}
		

		return subjects;
	}
	
	private static ArrayList<Set<String>> subjectsToSearchFor(Long sessionId, Set<String> subj, Set<String> department, Set<String> manager, boolean userIsDeptIndependent, Set<Department> userDepartments) {
		Set<String> searchSubjs = new TreeSet<String>();
		Set<String> bannerSearchSubjs = new TreeSet<String>();
		ArrayList<Set<String>> subjects = new ArrayList<Set<String>>();
		subjects.add(searchSubjs);
		subjects.add(bannerSearchSubjs);

		Set<String> deptSubjs = new TreeSet<String>(); 
		Set<String> deptBannerSubjs = new TreeSet<String>(); 
		if (department != null) {
			for (String dpt : department) {
				ArrayList<HashSet<String>> deptSubjects = subjectsForDept(dpt, sessionId);
				deptSubjs.addAll(deptSubjects.get(0));
				deptBannerSubjs.addAll(deptSubjects.get(1));
			}
		}
		Set<String> mgrSubjs = new TreeSet<String>();
		Set<String> mgrBannerSubjs = new TreeSet<String>();
		if (manager != null) {
			for (String mgr : manager) {
				ArrayList<HashSet<String>> mgrSubjects = subjectsForManager(mgr, sessionId);
				mgrSubjs.addAll(mgrSubjects.get(0));
				mgrBannerSubjs.addAll(mgrSubjects.get(1));
			}
		}
		Set<String> subjs = new TreeSet<String>();
		Set<String> bannerSubjs = new TreeSet<String>();
		if(subj != null && !subj.isEmpty()) {
			ArrayList<Set<String>> subjsForStrings = getSubjectsForStrings(sessionId, subj);
			subjs.addAll(subjsForStrings.get(0));
			bannerSubjs.addAll(subjsForStrings.get(1));
			searchSubjs.addAll(subjs);
			bannerSearchSubjs.addAll(bannerSubjs);
		}
		if (!deptSubjs.isEmpty()) {
			searchSubjs.addAll(deptSubjs);
		}
		if (!deptBannerSubjs.isEmpty()) {
			bannerSearchSubjs.addAll(deptBannerSubjs);
		}
		if (!mgrSubjs.isEmpty()) {
			searchSubjs.addAll(mgrSubjs);
			searchSubjs.retainAll(mgrSubjs);
		}
		if (!mgrBannerSubjs.isEmpty()) {
			bannerSearchSubjs.addAll(mgrBannerSubjs);
			bannerSearchSubjs.retainAll(mgrBannerSubjs);
		}
		if (!deptSubjs.isEmpty()) {
			searchSubjs.retainAll(deptSubjs);
		}
		if (!deptBannerSubjs.isEmpty()) {
			bannerSearchSubjs.retainAll(deptBannerSubjs);
		}
		if (subj != null && !subj.isEmpty()) {
			if (!subjs.isEmpty()) {
				searchSubjs.retainAll(subj);
			}
			if (!bannerSubjs.isEmpty()) {
				bannerSearchSubjs.retainAll(bannerSubjs);
			}
		}
		if (searchSubjs.isEmpty() && !userIsDeptIndependent) {
			for (Department d: userDepartments) {
				for ( SubjectArea sa : d.getSubjectAreas()) {
					String bannerAbbv = BannerSection.getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(sa, null);
					searchSubjs.add(sa.getSubjectAreaAbbreviation());
					if (bannerAbbv != sa.getSubjectAreaAbbreviation()) {
						bannerSearchSubjs.add(bannerAbbv);
					}
				}
			}

		}
		return subjects;
	}
	
	private static void addOrConstraintsIfNeeded(StringBuffer sb, String objectName, String fieldName, Set<String> values, int length, boolean canUseLike, boolean mustUseLike, boolean useStartingAnd) {
		if (values != null && !values.isEmpty()) {
			boolean first = true;
			if (useStartingAnd) {
				sb.append(" and ");
			} else {
				sb.append(" or ");
			}
			if (values.size() > 1) {
				sb.append(" (");
			}
			for (String value : values) {
				if (first) {
					first = false;
				} else {
					sb.append(" or ");
				}
				sb.append(objectName)
				  .append(".")
				  .append(fieldName);
				if (mustUseLike || (canUseLike && value.length() < length)) {
					sb.append(" like '")
					  .append(value)
					  .append("%'");
				} else {
					sb.append(" = '")
					  .append(value)
					  .append("'");					
				}
			}
			if (values.size() > 1) {
				sb.append(")");
			}
		}
	}
		
	private static String getBannerResponseHqlQuery(Long sessionId, Map<String, Set<String>> options, String ignoreCommand, boolean userIsDeptIndependent, Set<Department> userDepartments) {
		Set<String> subj = (options == null || "subj".equals(ignoreCommand) ? null : options.get("subj"));
		Set<String> crsNbr = (options == null || "crsnbr".equals(ignoreCommand) ? null : options.get("crsnbr"));
		Set<String> crn = (options == null || "crn".equals(ignoreCommand) ? null : options.get("crn"));
		Set<String> msg = (options == null || "msg".equals(ignoreCommand) ? null : options.get("msg"));
		Set<String> xlst = (options == null || "xlst".equals(ignoreCommand) ? null : options.get("xlst"));
		Set<String> department = (options == null || "department".equals(ignoreCommand) ? null : options.get("department"));
		Set<String> manager = (options == null || "manager".equals(ignoreCommand) ? null : options.get("manager"));
		Set<String> to = (options == null || "to".equals(ignoreCommand) ? null : options.get("to"));
		Set<String> from = (options == null || "from".equals(ignoreCommand) ? null : options.get("from"));
		Set<String> action = (options == null || "action".equals(ignoreCommand) ? null : options.get("action"));
		Set<String> rspType = (options == null || "rspType".equals(ignoreCommand) ? null : options.get("rspType"));

		StringBuffer sb = new StringBuffer();
		sb.append("select br from BannerResponse br, BannerSession bs where bs.session.uniqueId = ")
		  .append(sessionId)
		  .append(" and br.termCode = bs.bannerTermCode")
		  .append(" and cast(br.crn as int) >= bs.bannerTermCrnProperties.minCrn")
		  .append(" and cast(br.crn as int) <= bs.bannerTermCrnProperties.maxCrn")
		  ;
		ArrayList<Set<String>> searchSubjects = subjectsToSearchFor(sessionId, subj, department, manager, userIsDeptIndependent, userDepartments);
		if (!searchSubjects.get(0).isEmpty()) {
			sb.append(" and ( 0 < (select count(br2.uniqueId) from BannerResponse br2 where br2.uniqueId = br.uniqueId ");
			addOrConstraintsIfNeeded(sb, "br2", "subjectArea.subjectAreaAbbreviation", searchSubjects.get(0), -1, false, false, true);
			sb.append(" ) or (((select br2.subjectArea from BannerResponse br2 where br2.uniqueId = br.uniqueId ) is null )");
			if (!searchSubjects.get(1).isEmpty()) {
				addOrConstraintsIfNeeded(sb, "br", "subjectCode", searchSubjects.get(1), -1, false, false, true);
			} else {
				addOrConstraintsIfNeeded(sb, "br", "subjectCode", searchSubjects.get(0), -1, false, false, true);				
			}
			sb.append("))");
		}
		addOrConstraintsIfNeeded(sb, "br", "courseNumber", crsNbr, 5, true, false, true);
		addOrConstraintsIfNeeded(sb, "br", "crn", crn, 5, true, false, true);
		addOrConstraintsIfNeeded(sb, "br", "xlstGroup", xlst, 2, true, false, true);
		addOrConstraintsIfNeeded(sb, "br", "message", msg, -1, false, true, true);
		
		Set<String> actionList = new HashSet<String>();
		if (action != null && !action.isEmpty()) {
			for (String a : action) {
				if (a.equalsIgnoreCase("update")){
					actionList.add("UPDATE");
				} else if (a.equalsIgnoreCase("delete")) {
					actionList.add("DELETE");					
				} else if (a.equalsIgnoreCase("audit")) {
					actionList.add("AUDIT");					
				}
			}
		}
		addOrConstraintsIfNeeded(sb, "br", "action", actionList, -1, false, false, true);
		
		Set<String> rspTypeList = new HashSet<String>();
		if (rspType != null && !rspType.isEmpty()) {
			for (String rt : rspType) {
				if (rt.equalsIgnoreCase("success")){
					rspTypeList.add("SUCCESS");
				} else if (rt.equalsIgnoreCase("warning")) {
					rspTypeList.add("WARNING");					
				} else if (rt.equalsIgnoreCase("error")) {
					rspTypeList.add("ERROR");					
				} else if (rt.equalsIgnoreCase("no change")) {
					rspTypeList.add("NO_CHANGE");					
				}
			}
		}		
		addOrConstraintsIfNeeded(sb, "br", "type", rspTypeList, -1, false, false, true);

		if (from != null && from.size() == 1) {
			Date date = null;
			String fromOption = from.iterator().next();
			try {
				int dayOfYear = Integer.parseInt(fromOption);
				date = DateUtils.getDate(SessionDAO.getInstance().get(sessionId).getSessionStartYear(), dayOfYear);
			} catch (NumberFormatException f) {
				try {
					date = Formats.getDateFormat(Formats.Pattern.FILTER_DATE).parse(fromOption);
				} catch (ParseException p) {}
			}
			if (date != null) {
				sb.append(" and br.activityDate >= :fromDate ");
			}
		}
		if (to != null && to.size() == 1) {
			Date last = null;
			String toOption = to.iterator().next();
			try {
				int dayOfYear = Integer.parseInt(toOption);
				last = DateUtils.getDate(SessionDAO.getInstance().get(sessionId).getSessionStartYear(), dayOfYear);
			} catch (NumberFormatException f) {
				try {
					last = Formats.getDateFormat(Formats.Pattern.FILTER_DATE).parse(toOption);
				} catch (ParseException p) {}
				
			}
			if (last != null) {
				sb.append(" and br.activityDate < :toDate ");
			}
		}

		sb.append(" order by br.activityDate desc, br.packetId, br.sequenceNumber");
		Debug.info("BannerResponses Search Query:  " +sb.toString());
		return sb.toString();
	}

	public static List<BannerResponse> bannerResponses(Long sessionId, Map<String, Set<String>> options, Query query, String ignoreCommand, Set<Department> userDepartments, boolean userIsDeptIndependent) {
		org.hibernate.Session hibSession = BannerResponseDAO.getInstance().getSession();
		List<BannerResponse> ret = new ArrayList<BannerResponse>();
		Set<String> to = (options == null || "to".equals(ignoreCommand) ? null : options.get("to"));
		Set<String> from = (options == null || "from".equals(ignoreCommand) ? null : options.get("from"));
		Set<String> limits = null;
		if (options != null) {
			limits = options.get("limit");
		}
		int limit;
		if (limits != null && limits.size() == 1) {
			String limitStr = limits.iterator().next();
			try {
				limit = Integer.parseInt(limitStr);
			} catch (NumberFormatException e) {
				limit = 1000;
			}
		} else {
			limit = 1000;
		}

		
		org.hibernate.query.Query<BannerResponse> hibQuery = hibSession.createQuery(getBannerResponseHqlQuery(sessionId, options, ignoreCommand, userIsDeptIndependent, userDepartments), BannerResponse.class);
		if (from != null && from.size() >= 1) {
			Date date = null;
			String fromOption = from.iterator().next();
			try {
				int dayOfYear = Integer.parseInt(fromOption);
				date = DateUtils.getDate(SessionDAO.getInstance().get(sessionId).getSessionStartYear(), dayOfYear);
			} catch (NumberFormatException f) {
				try {
					date = Formats.getDateFormat(Formats.Pattern.FILTER_DATE).parse(fromOption);
				} catch (ParseException p) {}
			}
			if (date != null) {
				hibQuery.setParameter("fromDate", date);
			}
		}
		if (to != null && to.size() >= 1) {
			Date last = null;
			String toOption = to.iterator().next();
			try {
				int dayOfYear = Integer.parseInt(toOption);
				last = DateUtils.getDate(SessionDAO.getInstance().get(sessionId).getSessionStartYear(), dayOfYear);
			} catch (NumberFormatException f) {
				try {
					last = Formats.getDateFormat(Formats.Pattern.FILTER_DATE).parse(toOption);
				} catch (ParseException p) {}
				
			}
			if (last != null) {
				Calendar cal = Calendar.getInstance();
				cal.setTime(last);
				cal.add(Calendar.DAY_OF_MONTH, 1);
				hibQuery.setParameter("toDate", cal.getTime());
			}
		}


		List<BannerResponse> bannerResponses = hibQuery.setMaxResults(limit).setCacheable(false).list();
		
		for (BannerResponse br: bannerResponses) {
			if (query != null && !query.match(new BannerResponsesMatcher(br, sessionId, userDepartments))) continue;
												
			ret.add(br);
		}
		
		return ret;
	}
	
	public static List<String> stringOption( Map<String, Set<String>> options, String ignoreCommand, String tag, int length, boolean mustBeInteger) {
		List<String> strings = new ArrayList<String>();
		Set<String> strList = (options == null || tag.equals(ignoreCommand) ? null : options.get(tag));
		if (strList == null) return strings;
		for (String str : strList) {
			if (length == -1 || str.length() == length) {
				if (mustBeInteger) {
					boolean isInt = true;
					try {
						Integer.parseInt(str);				
					} catch (NumberFormatException e) {
						isInt = false;
					}
					if (isInt) {
						strings.add(str);
					}
				} else {
					strings.add(str);
				}
			}
		}
		return strings;
	}

	public static List<String> crns( Map<String, Set<String>> options, String ignoreCommand) {
		return stringOption(options, null, "crn", 5, true);
	}
	
	public static List<String> courseNumbers( Map<String, Set<String>> options, String ignoreCommand) {
		return stringOption(options, null, "crsNbr", 5, true);
	}
	
	public static List<String> xlstId( Map<String, Set<String>> options, String ignoreCommand) {
		return stringOption(options, null, "xlst", 2, false);
	}

	public static List<String> message( Map<String, Set<String>> options, String ignoreCommand) {
		return stringOption(options, null, "msg", -1, false);
	}

	public static List<String> limit( Map<String, Set<String>> options, String ignoreCommand) {
		return stringOption(options, null, "limit", -1, true);
	}


}
