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
package org.unitime.banner.onlinesectioning;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

import org.hibernate.CacheMode;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.server.Query;
import org.unitime.timetable.gwt.shared.OnlineSectioningInterface.WaitListMode;
import org.unitime.timetable.gwt.shared.ReservationInterface.OverrideType;
import org.unitime.timetable.interfaces.ExternalUidTranslation;
import org.unitime.timetable.interfaces.ExternalUidTranslation.Source;
import org.unitime.timetable.model.AcademicArea;
import org.unitime.timetable.model.AcademicClassification;
import org.unitime.timetable.model.Advisor;
import org.unitime.timetable.model.Campus;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseDemand;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.CourseRequest;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideIntent;
import org.unitime.timetable.model.CourseRequest.CourseRequestOverrideStatus;
import org.unitime.timetable.model.CourseRequestOption;
import org.unitime.timetable.model.Degree;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.PosMajor;
import org.unitime.timetable.model.PosMajorConcentration;
import org.unitime.timetable.model.PosMinor;
import org.unitime.timetable.model.Program;
import org.unitime.timetable.model.Reservation;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.Student;
import org.unitime.timetable.model.StudentAccomodation;
import org.unitime.timetable.model.StudentAreaClassificationMajor;
import org.unitime.timetable.model.StudentAreaClassificationMinor;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentEnrollmentMessage;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.StudentGroupType;
import org.unitime.timetable.model.StudentNote;
import org.unitime.timetable.model.StudentSectioningStatus;
import org.unitime.timetable.model.StudentSectioningStatus.NotificationType;
import org.unitime.timetable.model.WaitList.WaitListType;
import org.unitime.timetable.model.dao.AcademicAreaDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningAction;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer.Lock;
import org.unitime.timetable.onlinesectioning.custom.CustomStudentEnrollmentHolder;
import org.unitime.timetable.onlinesectioning.model.XCourseId;
import org.unitime.timetable.onlinesectioning.model.XCourseRequest;
import org.unitime.timetable.onlinesectioning.model.XEnrollment;
import org.unitime.timetable.onlinesectioning.model.XIndividualReservation;
import org.unitime.timetable.onlinesectioning.model.XOffering;
import org.unitime.timetable.onlinesectioning.model.XOverride;
import org.unitime.timetable.onlinesectioning.model.XRequest;
import org.unitime.timetable.onlinesectioning.model.XReservation;
import org.unitime.timetable.onlinesectioning.model.XReservationType;
import org.unitime.timetable.onlinesectioning.model.XSection;
import org.unitime.timetable.onlinesectioning.model.XStudent;
import org.unitime.timetable.onlinesectioning.status.db.DbFindEnrollmentInfoAction.DbStudentMatcher;
import org.unitime.timetable.onlinesectioning.updates.CheckOfferingAction;
import org.unitime.timetable.onlinesectioning.updates.NotifyStudentAction;
import org.unitime.timetable.onlinesectioning.updates.ReloadAllData;
import org.unitime.timetable.util.Constants;

/**
 * @author Tomas Muller
 */
public class BannerUpdateStudentAction implements OnlineSectioningAction<BannerUpdateStudentAction.UpdateResult> {
	private static final long serialVersionUID = 1L;
	private static ValueLock<String> sLock = new ValueLock<String>();

	private String iTermCode, iExternalId, iFName, iMName, iLName, iEmail, iCampus, iStudentCampus;
	private List<String[]> iGroups = new ArrayList<String[]>();
	private List<ACM> iAcadAreaClasfMj = new ArrayList<ACM>();
	private boolean iUpdateAcadAreaClasfMj = false;
	private List<ACM> iAcadAreaClasfMn = new ArrayList<ACM>();
	private boolean iUpdateAcadAreaClasfMn = false;
	private List<String[]> iOverrides = new ArrayList<String[]>();
	private Set<Integer> iCRNs = new TreeSet<Integer>();
	private transient Session iSession;
	private List<String[]> iAdvisors = new ArrayList<String[]>();
	private String iOverrideTypes = null;
	private String iIgnoreGroupRegExp = null;
	private boolean iUpdateClasses = true;
	private boolean iLocking = false;
	private boolean iResetWaitList = false;
	private boolean iDelayOfferingChecks = true;
	private Set<String> iCampusCodes = null;
	
	public BannerUpdateStudentAction() {
		iOverrideTypes = ApplicationProperties.getProperty("banner.overrides.regexp");
		iIgnoreGroupRegExp = ApplicationProperties.getProperty("banner.ignoreGroups.regexp");
		iResetWaitList = "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.waitlist.resetWhenEnrolled"));
		iDelayOfferingChecks = "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.waitlist.delayOfferingChecks"));
	}
	
	protected String getNewStatusRules(Long sessionId) {
		return ApplicationProperties.getProperty(sessionId, "banner.newstudent.status");
	}
	
	public BannerUpdateStudentAction forStudent(String externalId, String termCode) {
		iExternalId = externalId;
		iTermCode = termCode;
		return this;
	}
	
	public BannerUpdateStudentAction withName(String firstName, String middleName, String lastName) {
		iFName = firstName;
		iMName = middleName;
		iLName = lastName;
		return this;
	}
	
	public BannerUpdateStudentAction withEmail(String email) {
		iEmail = email;
		return this;
	}
	
	public BannerUpdateStudentAction withAcadAreaClassificationMajor(String academicArea, String classification, String major, String campus, String concentration, String degree, String program, double weight) {
		if (campus != null && !campus.isEmpty()) withCampus(campus);
		if (academicArea == null || academicArea.isEmpty() || classification == null || classification.isEmpty() || major == null || major.isEmpty()) return this;
		iUpdateAcadAreaClasfMj = true;
		ACM acm = new ACM(academicArea, classification, major, campus, concentration, degree, program, weight);
		for (ACM other: iAcadAreaClasfMj) {
			if (other.equals(acm)) {
				other.update(acm);
				return this;
			}
		}
		iAcadAreaClasfMj.add(acm);
		return this;
	}
	
	public BannerUpdateStudentAction updateAcadAreaClassificationMajors(boolean update) {
		iUpdateAcadAreaClasfMj = update;
		return this;
	}
	
	public BannerUpdateStudentAction withAcadAreaClassificationMinor(String minor, String campus) {
		if (minor == null || minor.isEmpty()) return this;
		iUpdateAcadAreaClasfMn = true;
		ACM acm = new ACM(minor, campus);
		for (ACM other: iAcadAreaClasfMn) {
			if (other.equals(acm)) return this;
		}
		iAcadAreaClasfMn.add(acm);
		return this;
	}
	
	public BannerUpdateStudentAction updateAcadAreaClassificationMinors(boolean update) {
		iUpdateAcadAreaClasfMn = update;
		return this;
	}

	public BannerUpdateStudentAction withGroup(String externalId, String campus, String abbreviation, String name, String type) {
		iGroups.add(new String[] {externalId, campus, abbreviation, name, type});
		return this;
	}
	
	public BannerUpdateStudentAction withGroup(String externalId, String campus, String abbreviation, String name) {
		iGroups.add(new String[] {externalId, campus, abbreviation, name, null});
		return this;
	}
	
	public BannerUpdateStudentAction withCRN(Integer crn) {
		iCRNs.add(crn);
		return this;
	}
	
	public Set<Integer> getCRNs() { return iCRNs; }
	
	public BannerUpdateStudentAction withOverride(String type, String subject, String course, String crn) {
		iOverrides.add(new String[] {type, subject, course, crn});
		return this;
	}
	
	public BannerUpdateStudentAction withAdvisor(String externalId, String type) {
		iAdvisors.add(new String[] {externalId, type});
		return this;
	}
	
	public BannerUpdateStudentAction withCampus(String campus) {
		if (campus != null && !campus.isEmpty()) {
			if (iCampusCodes == null) iCampusCodes = new HashSet<String>();
			iCampusCodes.add(campus);
		}
		return this;
	}
	
	public BannerUpdateStudentAction skipClassUpdates() {
		iUpdateClasses = false;
		return this;
	}
	
	public BannerUpdateStudentAction withLocking() {
		iLocking = true;
		return this;
	}
	
	public boolean isApplicable(BannerSession bs) {
		if (bs.getStudentCampus() == null || bs.getStudentCampus().isEmpty()) return true;
		if (iCampusCodes == null || iCampusCodes.isEmpty()) return true;
		for (String code: iCampusCodes)
			if (code.matches(bs.getStudentCampus())) return true;
		return false;
	}

	@Override
	public UpdateResult execute(OnlineSectioningServer server, OnlineSectioningHelper helper) {
		UpdateResult result = new UpdateResult();
		try {
			result.setStudentId(getStudentId(server.getAcademicSession().getUniqueId()));
			if (result.getStudentId() == null) result.add(Change.CREATED);
			
			Lock lock = (result.getStudentId() == null ? null : server.lockStudent(result.getStudentId(), null, name()));
			try {
				helper.getHibSession().setHibernateFlushMode(FlushMode.COMMIT);
				helper.getHibSession().setCacheMode(CacheMode.REFRESH);
				helper.beginTransaction();

				iSession = SessionDAO.getInstance().get(server.getAcademicSession().getUniqueId(), helper.getHibSession());

				BannerSession bs = BannerSession.findBannerSessionForSession(server.getAcademicSession().getUniqueId(), helper.getHibSession());
				iCampus = (bs == null ? iSession.getAcademicInitiative() : bs.getBannerCampus());
				iStudentCampus = (bs == null ? null : bs.getStudentCampus());
				
				OnlineSectioningLog.Action.Builder action = helper.getAction();
				
				action.setStudent(
						OnlineSectioningLog.Entity.newBuilder()
						.setExternalId(iExternalId)
						.setName(iLName + ", " + iFName + (iMName == null ? "" : " " + iMName))
						.setType(OnlineSectioningLog.Entity.EntityType.STUDENT));
				
				helper.getAction().addOptionBuilder().setKey("CRNs").setValue(iCRNs.toString());
				if (iEmail != null)
					helper.getAction().addOptionBuilder().setKey("email").setValue(iEmail);
				if (!iAcadAreaClasfMj.isEmpty()) {
					for (ACM acm: iAcadAreaClasfMj) {
						if (iStudentCampus == null || iStudentCampus.isEmpty()) {
							if (acm.hasCampus() && !acm.getCampus().equals(iCampus)) continue;
						} else {
							if (acm.hasCampus() && !acm.getCampus().matches(iStudentCampus)) continue;
						}
						helper.getAction().addOptionBuilder().setKey("curriculum").setValue(acm.toString());
					}
				}
				if (!iAcadAreaClasfMn.isEmpty()) {
					for (ACM acm: iAcadAreaClasfMn) {
						if (iStudentCampus == null || iStudentCampus.isEmpty()) {
							if (acm.hasCampus() && !acm.getCampus().equals(iCampus)) continue;
						} else {
							if (acm.hasCampus() && !acm.getCampus().matches(iStudentCampus)) continue;
						}
						helper.getAction().addOptionBuilder().setKey("minor").setValue(acm.toString());
					}
				}
				
				Student student = getStudent(helper);

				if (updateStudentDemographics(student, helper, result))
					result.add(Change.DEMOGRAPHICS);
				
				if (updateStudentGroups(student, helper))
					result.add(Change.GROUPS);
				
				if (updateAdvisors(student, helper))
					result.add(Change.ADVISORS);
				
				if (iUpdateClasses) {
					Map<CourseOffering, List<Class_>> enrollments = getEnrollments(helper, result);
					
					OnlineSectioningLog.Enrollment.Builder external = OnlineSectioningLog.Enrollment.newBuilder();
					external.setType(OnlineSectioningLog.Enrollment.EnrollmentType.EXTERNAL);
					for (Map.Entry<CourseOffering, List<Class_>> e: enrollments.entrySet()) {
						CourseOffering course = e.getKey();
						for (Class_ clazz: e.getValue()) {
							external.addSectionBuilder()
									.setClazz(OnlineSectioningLog.Entity.newBuilder()
											.setUniqueId(clazz.getUniqueId())
											.setExternalId(clazz.getExternalId(course))
											.setName(clazz.getClassSuffix(course))
											)
									.setCourse(OnlineSectioningLog.Entity.newBuilder()
											.setUniqueId(course.getUniqueId())
											.setName(course.getCourseName())
											)
									.setSubpart(OnlineSectioningLog.Entity.newBuilder()
											.setUniqueId(clazz.getSchedulingSubpart().getUniqueId())
											.setName(clazz.getSchedulingSubpart().getItypeDesc())
											)
									;
						}
					}
					helper.getAction().addEnrollment(external);
					
					if (updateClassEnrollments(student, enrollments, helper))
						result.add(Change.CLASSES);
				}
				
				String newStudentsStatusRules = getNewStatusRules(server.getAcademicSession().getUniqueId());
				if (newStudentsStatusRules != null && !newStudentsStatusRules.isEmpty() && result.has(Change.CREATED)) {
					DbStudentMatcher matcher = new DbStudentMatcher(student);
					for (String rule: newStudentsStatusRules.split("[\n\r]+")) {
						if (rule != null && rule.indexOf('|') >= 0) {
							String filter = rule.substring(0, rule.indexOf('|'));
							String status = rule.substring(rule.indexOf('|') + 1);
							if (filter.isEmpty() || new Query(filter).match(matcher)) {
								student.setSectioningStatus(StudentSectioningStatus.getStatus(status, server.getAcademicSession().getUniqueId(), helper.getHibSession()));
								result.add(Change.STATUS);
								helper.getAction().addOptionBuilder().setKey("status").setValue(status);
								break;
							}
						}
					}
				}
				
				if (result.hasChanges())
					helper.getHibSession().merge(student);
				
				action.getStudentBuilder().setUniqueId(result.getStudentId());
				
				if (iUpdateClasses && updateStudentOverrides(student, server, helper, result))
					result.add(Change.OVERRIDES);
				
				if (!iUpdateClasses && result.hasChanges()) {
					XStudent newStudent = ReloadAllData.loadStudent(student, null, server, helper);
					if (newStudent != null) {
						server.update(newStudent, true);
					}
				}
				
				if (iUpdateClasses && result.hasChanges()) {
					// Unload student
					XStudent oldStudent = server.getStudent(result.getStudentId());
					if (oldStudent != null) {
						OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
						enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.PREVIOUS);
						for (XRequest oldRequest: oldStudent.getRequests()) {
							if (oldRequest instanceof XCourseRequest && ((XCourseRequest)oldRequest).getEnrollment() != null) {
								XEnrollment enrl = ((XCourseRequest)oldRequest).getEnrollment();
								XOffering offering = server.getOffering(enrl.getOfferingId());
								for (XSection section: offering.getSections(enrl))
									enrollment.addSection(OnlineSectioningHelper.toProto(section, enrl));
							}
						}
						action.addEnrollment(enrollment);
					}
					
					// Load student
					XStudent newStudent = ReloadAllData.loadStudent(student, null, server, helper);
					if (newStudent != null) {
						server.update(newStudent, true);
						OnlineSectioningLog.Enrollment.Builder enrollment = OnlineSectioningLog.Enrollment.newBuilder();
						enrollment.setType(OnlineSectioningLog.Enrollment.EnrollmentType.STORED);
						for (XRequest newRequest: newStudent.getRequests()) {
							action.addRequest(OnlineSectioningHelper.toProto(newRequest));
							if (newRequest instanceof XCourseRequest && ((XCourseRequest)newRequest).getEnrollment() != null) {
								XEnrollment enrl = ((XCourseRequest)newRequest).getEnrollment();
								XOffering offering = server.getOffering(enrl.getOfferingId());
								for (XSection section: offering.getSections(enrl))
									enrollment.addSection(OnlineSectioningHelper.toProto(section, enrl));
							}
						}
						action.addEnrollment(enrollment);
					}
					
					OnlineSectioningServer.ServerCallback<Boolean> offeringChecked = new OnlineSectioningServer.ServerCallback<Boolean>() {
						@Override
						public void onFailure(Throwable exception) {
							helper.error("Offering check failed: " + exception.getMessage(), exception);
						}
						@Override
						public void onSuccess(Boolean result) {
						}
					};

					if (oldStudent != null && server.getAcademicSession().isSectioningEnabled() && CustomStudentEnrollmentHolder.isAllowWaitListing()) {
						for (XRequest oldRequest: oldStudent.getRequests()) {
							XEnrollment oldEnrollment = (oldRequest instanceof XCourseRequest ? ((XCourseRequest)oldRequest).getEnrollment() : null);
							if (oldEnrollment == null) continue; // free time or not assigned
							
							if (CheckOfferingAction.isCheckNeeded(server, helper, oldEnrollment)) {
								if (iDelayOfferingChecks)
									result.addCheckOfferingForOthers(oldEnrollment.getOfferingId());
								else
									server.execute(server.createAction(CheckOfferingAction.class).forOfferings(oldEnrollment.getOfferingId()).skipStudents(oldStudent.getStudentId()), helper.getUser(), offeringChecked);
							}
						}
					}
					
					if (newStudent != null && server.getAcademicSession().isSectioningEnabled() && CustomStudentEnrollmentHolder.isAllowWaitListing() && student.getWaitListMode() == WaitListMode.WaitList) {
						Set<Long> offeringIds = new HashSet<Long>();
						for (XRequest newRequest: newStudent.getRequests()) {
							if (newRequest instanceof XCourseRequest) { // only course requests
								XCourseRequest cr = (XCourseRequest) newRequest;
								if (cr.getEnrollment() == null && cr.isWaitlist() && !cr.isAlternative()) { // wait-listed and not assigned
									for (XCourseId c: cr.getCourseIds())
										if (isOfferingCheckNeeded(cr, c)) {
											if (iDelayOfferingChecks)
												result.addCheckOfferingForMe(c.getOfferingId());
											else
												offeringIds.add(c.getOfferingId());
										}
								}
							}
						}
						if (!offeringIds.isEmpty()) {
							server.execute(server.createAction(CheckOfferingAction.class).forOfferings(offeringIds).forStudents(newStudent.getStudentId()), helper.getUser(), offeringChecked);
						}
					}

					if (result.has(Change.CLASSES))
							server.execute(server.createAction(NotifyStudentAction.class)
									.forStudent(newStudent)
									.fromAction(name())
									.withType(NotificationType.ExternalChangeEnrollment)
									.skipWhenNoChange(true)
									.oldStudent(oldStudent), helper.getUser());
 				} else if (server.getAcademicSession().isSectioningEnabled() && CustomStudentEnrollmentHolder.isAllowWaitListing() && student.getWaitListMode() == WaitListMode.WaitList) {
 					// no change in the enrollments --> still check the wait-listed override changes
					XStudent newStudent = server.getStudent(student.getUniqueId());
					if (newStudent != null) {
						Set<Long> offeringIds = new HashSet<Long>();
						for (XRequest newRequest: newStudent.getRequests()) {
							if (newRequest instanceof XCourseRequest) { // only course requests
								XCourseRequest cr = (XCourseRequest) newRequest;
								if (cr.getEnrollment() == null && cr.isWaitlist() && !cr.isAlternative()) { // wait-listed and not assigned
									for (XCourseId c: cr.getCourseIds())
										if (isOfferingCheckNeeded(cr, c)) {
											if (iDelayOfferingChecks) 
												result.addCheckOfferingForMe(c.getOfferingId());
											else
												offeringIds.add(c.getOfferingId());
										}
								}
							}
						}
						if (!offeringIds.isEmpty()) {
							server.execute(server.createAction(CheckOfferingAction.class).forOfferings(offeringIds).forStudents(newStudent.getStudentId()), helper.getUser(),
							new OnlineSectioningServer.ServerCallback<Boolean>() {
								@Override
								public void onFailure(Throwable exception) {
									helper.error("Offering check failed: " + exception.getMessage(), exception);
								}
								@Override
								public void onSuccess(Boolean result) {
								}
							});
						}
					}
				}
			
				helper.commitTransaction();
			} catch (Exception e) {
				helper.rollbackTransaction();
				throw e;
			} finally {
				if (lock != null) lock.release();
			}
			
		} catch (Exception e) {
			helper.error("Student update failed: " + e.getMessage(), e);
			helper.getAction().setResult(OnlineSectioningLog.Action.ResultType.FAILURE);
			result.setStatus(Status.FAILURE);
			return result;
		} finally {
			for (OnlineSectioningLog.Message m: helper.getLog().getMessageList())
				helper.getAction().addMessage(m);
		}
		helper.getAction().setResult(result.hasChanges() ? OnlineSectioningLog.Action.ResultType.TRUE : OnlineSectioningLog.Action.ResultType.FALSE);
		
		if (result.getStatus() == Status.OK && !result.hasChanges())
			result.setStatus(Status.NO_CHANGE);
		
		return result;
	}
	
	public UpdateResult execute(Long sessionId, OnlineSectioningHelper helper) {
		helper.getHibSession().setHibernateFlushMode(FlushMode.COMMIT);
		helper.getHibSession().setCacheMode(CacheMode.REFRESH);
		helper.beginTransaction();
		UpdateResult result = new UpdateResult();
		try {
			iSession = SessionDAO.getInstance().get(sessionId, helper.getHibSession());
			BannerSession bs = BannerSession.findBannerSessionForSession(sessionId, helper.getHibSession());
			iCampus = (bs == null ? iSession.getAcademicInitiative() : bs.getBannerCampus());
			iStudentCampus = (bs == null ? null : bs.getStudentCampus());
			
			Student student = getStudent(helper);
			result.setStudentId(student.getUniqueId());
			if (result.getStudentId() == null) result.add(Change.CREATED);
			
			if (updateStudentDemographics(student, helper, result))
				result.add(Change.DEMOGRAPHICS);
			
			if (updateStudentGroups(student, helper))
				result.add(Change.GROUPS);
			
			if (updateAdvisors(student, helper))
				result.add(Change.ADVISORS);
			
			String newStudentsStatusRules = getNewStatusRules(sessionId);
			if (newStudentsStatusRules != null && !newStudentsStatusRules.isEmpty() && result.has(Change.CREATED)) {
				DbStudentMatcher matcher = new DbStudentMatcher(student);
				for (String rule: newStudentsStatusRules.split("[\n\r]+")) {
					if (rule != null && rule.indexOf('|') >= 0) {
						String filter = rule.substring(0, rule.indexOf('|'));
						String status = rule.substring(rule.indexOf('|') + 1);
						if (filter.isEmpty() || new Query(filter).match(matcher)) {
							student.setSectioningStatus(StudentSectioningStatus.getStatus(status, sessionId, helper.getHibSession()));
							result.add(Change.STATUS);
							break;
						}
					}
				}
			}

			if (result.hasChanges())
				helper.getHibSession().merge(student);

			if (iUpdateClasses && updateStudentOverrides(student, null, helper, result))
				result.add(Change.OVERRIDES);

			if (iUpdateClasses && updateClassEnrollments(student, getEnrollments(helper, result), helper)) {
				result.add(Change.CLASSES);
			}
			
			helper.commitTransaction();
		} catch (Exception e) {
			helper.rollbackTransaction();
			helper.error("Student update failed: " + e.getMessage(), e);
			result.setStatus(Status.FAILURE);
		}
			
		if (result.getStatus() == Status.OK && !result.hasChanges())
			result.setStatus(Status.NO_CHANGE);
		return result;
	}
	
	protected Long getStudentId(Long sessionId) {
		org.hibernate.Session hibSession = SessionDAO.getInstance().createNewSession();
		try {
			return hibSession.createQuery("select s.uniqueId from Student s where " +
					"s.session.uniqueId = :sessionId and s.externalUniqueId = :externalId", Long.class)
					.setParameter("sessionId", sessionId).setParameter("externalId",iExternalId)
				.setCacheable(true).uniqueResult();
		} finally {
			hibSession.close();
		}
	}
	
	public Student getStudent(OnlineSectioningHelper helper) {
		Student student = Student.findByExternalIdBringBackEnrollments(helper.getHibSession(), iSession.getUniqueId(), iExternalId);
		if (student == null) {
			student = new Student();
			student.setExternalUniqueId(iExternalId);
			student.setSession(SessionDAO.getInstance().get(iSession.getUniqueId(), helper.getHibSession()));
			student.setFreeTimeCategory(0);
			student.setSchedulePreference(0);
			student.setClassEnrollments(new HashSet<StudentClassEnrollment>());
			student.setAreaClasfMajors(new HashSet<StudentAreaClassificationMajor>());
			student.setAreaClasfMinors(new HashSet<StudentAreaClassificationMinor>());
			student.setCourseDemands(new HashSet<CourseDemand>());
			student.setGroups(new HashSet<StudentGroup>());
			student.setAccomodations(new HashSet<StudentAccomodation>());
			student.setNotes(new HashSet<StudentNote>());
			student.setAdvisors(new HashSet<Advisor>());
		}
		return student;
	}
	
	protected Map<String, AcademicArea> iCreatedAreas = new HashMap<String, AcademicArea>();
	protected AcademicArea getAcademicArea(OnlineSectioningHelper helper, String area) {
		if (iCreatedAreas.containsKey(area))
			return iCreatedAreas.get(area);
		if (iLocking) {
			try {
				sLock.lock("Area:" + iSession.getReference() + ":" + area);
				AcademicArea aa = AcademicArea.findByExternalId(helper.getHibSession(), iSession.getUniqueId(), area);
				if (aa != null) return aa;
				aa = AcademicArea.findByAbbv(helper.getHibSession(), iSession.getUniqueId(), area);
				if (aa != null) return aa;
				aa = new AcademicArea();
				aa.setPosMajors(new HashSet<PosMajor>());
				aa.setAcademicAreaAbbreviation(area);
				aa.setSession(iSession);
				aa.setExternalUniqueId(area);
				aa.setTitle(area);
				org.hibernate.Session hibSession = AcademicAreaDAO.getInstance().createNewSession();
				try {
					hibSession.persist(aa);
					hibSession.flush();
				} finally {
					hibSession.close();
				}
				helper.info("Added Academic Area:  " + area);
				helper.getHibSession().merge(aa);
				iCreatedAreas.put(area, aa);
				return aa;
			} finally {
				sLock.unlock("Area:" + iSession.getReference() + ":" + area);
			}
		} else {
			AcademicArea aa = AcademicArea.findByExternalId(helper.getHibSession(), iSession.getUniqueId(), area);
			if (aa == null)
				aa = AcademicArea.findByAbbv(helper.getHibSession(), iSession.getUniqueId(), area);
			if (aa == null){
				aa = new AcademicArea();
				aa.setPosMajors(new HashSet<PosMajor>());
				aa.setAcademicAreaAbbreviation(area);
				aa.setSession(iSession);
				aa.setExternalUniqueId(area);
				aa.setTitle(area);
				helper.getHibSession().persist(aa);
				helper.info("Added Academic Area:  " + area);
				iCreatedAreas.put(area, aa);
			}
			return aa;
		}
	}
	
	protected Map<String, AcademicClassification> iCreatedClassifications = new HashMap<String, AcademicClassification>();
	protected AcademicClassification getAcademicClassification(OnlineSectioningHelper helper, String clasf) {
		if (iCreatedClassifications.containsKey(clasf))
			return iCreatedClassifications.get(clasf);
		if (iLocking) {
			try {
				sLock.lock("Clasf:" + iSession.getReference() + ":" + clasf);
				AcademicClassification ac = AcademicClassification.findByExternalId(helper.getHibSession(), iSession.getUniqueId(), clasf);
				if (ac != null) return ac;
				ac = AcademicClassification.findByCode(helper.getHibSession(), iSession.getUniqueId(), clasf);
				if (ac != null) return ac;
				ac = new AcademicClassification();
				ac.setCode(clasf);
				ac.setExternalUniqueId(clasf);
				ac.setName(clasf);
				ac.setSession(iSession);
				org.hibernate.Session hibSession = AcademicAreaDAO.getInstance().createNewSession();
				try {
					hibSession.persist(ac);
					hibSession.flush();
				} finally {
					hibSession.close();
				}
				helper.info("Added Academic Classification:  " + clasf);
				helper.getHibSession().merge(ac);
				iCreatedClassifications.put(clasf, ac);
				return ac;
			} finally {
				sLock.unlock("Clasf:" + iSession.getReference() + ":" + clasf);
			}
		} else {
			AcademicClassification ac = AcademicClassification.findByExternalId(helper.getHibSession(), iSession.getUniqueId(), clasf);
			if (ac == null)
				ac = AcademicClassification.findByCode(helper.getHibSession(), iSession.getUniqueId(), clasf);
			if (ac == null){
				ac = new AcademicClassification();
				ac.setCode(clasf);
				ac.setExternalUniqueId(clasf);
				ac.setName(clasf);
				ac.setSession(iSession);
				helper.getHibSession().persist(ac);
				helper.info("Added Academic Classification:  " + clasf);
				iCreatedClassifications.put(clasf, ac);
			}
			return ac;
		}
	}
	
	protected PosMajor getPosMajor(OnlineSectioningHelper helper, AcademicArea aa, String area, String major) {
		if (iLocking) {
			try {
				sLock.lock("Major:" + iSession.getReference() + ":" + area + ":" + major);
				PosMajor posMajor = PosMajor.findByExternalIdAcadAreaExternalId(helper.getHibSession(), iSession.getUniqueId(), major, area);
				if (posMajor != null) return posMajor;
				posMajor = PosMajor.findByCodeAcadAreaAbbv(helper.getHibSession(), iSession.getUniqueId(), major, area);
				if (posMajor != null) return posMajor;
				posMajor = new PosMajor();
				posMajor.setCode(major);
				posMajor.setExternalUniqueId(major);
				posMajor.setName(major);
				posMajor.setSession(iSession);
				org.hibernate.Session hibSession = AcademicAreaDAO.getInstance().createNewSession();
				try {
					posMajor.addToAcademicAreas(aa);
					aa.addToPosMajors(posMajor);
					hibSession.persist(posMajor);
					hibSession.flush();
				} finally {
					hibSession.close();
				}
				helper.getHibSession().merge(aa);
				helper.info("Added Major:  " + major + " to Academic Area:  " + area);
				return posMajor;
			} finally {
				sLock.unlock("Major:" + iSession.getReference() + ":" + area + ":" + major);
			}
		} else {
			PosMajor posMajor = PosMajor.findByExternalIdAcadAreaExternalId(helper.getHibSession(), iSession.getUniqueId(), major, area);
			if (posMajor == null)
				posMajor = PosMajor.findByCodeAcadAreaAbbv(helper.getHibSession(), iSession.getUniqueId(), major, area);
			if (posMajor == null) {
				posMajor = new PosMajor();
				posMajor.setCode(major);
				posMajor.setExternalUniqueId(major);
				posMajor.setName(major);
				posMajor.setSession(iSession);
				posMajor.addToAcademicAreas(aa);
				aa.addToPosMajors(posMajor);
				helper.getHibSession().persist(posMajor);
				helper.info("Added Major:  " + major + " to Academic Area:  " + area);
			}
			return posMajor;
		}
	}
	
	protected static PosMinor findPosMinor(org.hibernate.Session hibSession, Long sessionId, String minor) {
		PosMinor conc = hibSession.createQuery(
                "select m from PosMinor m where "+
                "m.session.uniqueId = :sessionId and "+
                "m.externalUniqueId = :minor", PosMinor.class).
         setParameter("sessionId", sessionId).
         setParameter("minor", minor).
         setCacheable(true).
         setMaxResults(1).
         uniqueResult(); 
		if (conc != null) return conc;
		return hibSession.createQuery(
				"select m from PosMinor m where "+
                "m.session.uniqueId = :sessionId and "+
                "m.code = :minor", PosMinor.class).
         setParameter("sessionId", sessionId).
         setParameter("minor", minor).
         setCacheable(true).
         setMaxResults(1).
         uniqueResult();
    }
	
	protected static PosMajorConcentration findPosMajorConcentration(org.hibernate.Session hibSession, Long sessionId, String area, String major, String concentration) {
		PosMajorConcentration conc = hibSession.createQuery(
                "select c from PosMajorConcentration c inner join c.major m inner join m.academicAreas a where "+
                "m.session.uniqueId = :sessionId and "+
                "c.externalUniqueId = :concentration and " +
                "m.externalUniqueId = :major and " +
                "a.externalUniqueId = :area", PosMajorConcentration.class).
         setParameter("sessionId", sessionId).
         setParameter("area", area).
         setParameter("major", major).
         setParameter("concentration", concentration).
         setCacheable(true).
         uniqueResult(); 
		if (conc != null) return conc;
		return hibSession.createQuery(
                "select c from PosMajorConcentration c inner join c.major m inner join m.academicAreas a where "+
                "m.session.uniqueId = :sessionId and "+
                "c.code = :concentration and " +
                "m.code = :major and " +
                "a.academicAreaAbbreviation = :area", PosMajorConcentration.class).
         setParameter("sessionId", sessionId).
         setParameter("area", area).
         setParameter("major", major).
         setParameter("concentration", concentration).
         setCacheable(true).
         uniqueResult();
    }
	
	protected PosMajorConcentration getPosMajorConcentration(OnlineSectioningHelper helper, PosMajor posMajor, String area, String major, String concentration) {
		if (iLocking) {
			try {
				sLock.lock("Concentration:" + iSession.getReference() + ":" + area + ":" + major + ":" + concentration);
				PosMajorConcentration conc = findPosMajorConcentration(helper.getHibSession(), iSession.getUniqueId(), area, major, concentration);
				if (conc != null) return conc;
				conc = new PosMajorConcentration();
				conc.setExternalUniqueId(concentration);
				conc.setCode(concentration);
				conc.setName(concentration);
				conc.setMajor(posMajor);
				org.hibernate.Session hibSession = AcademicAreaDAO.getInstance().createNewSession();
				try {
					hibSession.persist(conc);
					hibSession.flush();
				} finally {
					hibSession.close();
				}
				helper.getHibSession().merge(conc);
				posMajor.addToConcentrations(conc);
				helper.info("Added Concentration:  " + concentration + " to Major:  " + area + "/" + major);
				return conc;
			} finally {
				sLock.unlock("Concentration:" + iSession.getReference() + ":" + area + ":" + major + ":" + concentration);
			}
		} else {
			PosMajorConcentration conc = findPosMajorConcentration(helper.getHibSession(), iSession.getUniqueId(), area, major, concentration);
			if (conc == null) {
				conc = new PosMajorConcentration();
				conc.setExternalUniqueId(concentration);
				conc.setCode(concentration);
				conc.setName(concentration);
				conc.setMajor(posMajor);
				posMajor.addToConcentrations(conc);
				helper.getHibSession().persist(conc);
				helper.info("Added Concentration:  " + concentration + " to Major:  " + area + "/" + major);
			}
			return conc;
		}
	}
	
	protected static Degree findDegree(org.hibernate.Session hibSession, Long sessionId, String degree) {
		Degree deg = hibSession.createQuery(
                "select d from Degree d where "+
                "d.session.uniqueId = :sessionId and "+
                "d.externalUniqueId = :degree", Degree.class).
         setParameter("sessionId", sessionId).
         setParameter("degree", degree).
         setCacheable(true).
         uniqueResult(); 
		if (deg != null) return deg;
		return hibSession.createQuery(
                "select d from Degree d where "+
                "d.session.uniqueId = :sessionId and "+
                "d.reference = :degree", Degree.class).
         setParameter("sessionId", sessionId).
         setParameter("degree", degree).
         setCacheable(true).
         uniqueResult();
    }
	
	protected Map<String, Degree> iCreatedDegrees = new HashMap<String, Degree>();
	protected Degree getDegree(OnlineSectioningHelper helper, String degree) {
		if (iCreatedDegrees.containsKey(degree))
			return iCreatedDegrees.get(degree);
		if (iLocking) {
			try {
				sLock.lock("Degree:" + iSession.getReference() + ":" + degree);
				Degree deg = findDegree(helper.getHibSession(), iSession.getUniqueId(), degree);
				if (deg != null) return deg;
				deg = new Degree();
				deg.setExternalUniqueId(degree);
				deg.setReference(degree);
				deg.setLabel(degree);
				deg.setSession(iSession);
				org.hibernate.Session hibSession = AcademicAreaDAO.getInstance().createNewSession();
				try {
					hibSession.persist(deg);
					hibSession.flush();
				} finally {
					hibSession.close();
				}
				helper.getHibSession().merge(deg);
				helper.info("Added Degree:  " + degree);
				iCreatedDegrees.put(degree, deg);
				return deg;
			} finally {
				sLock.unlock("Degree:" + iSession.getReference() + ":" + degree);
			}
		} else {
			Degree deg = findDegree(helper.getHibSession(), iSession.getUniqueId(), degree);
			if (deg == null) {
				deg = new Degree();
				deg.setExternalUniqueId(degree);
				deg.setReference(degree);
				deg.setLabel(degree);
				deg.setSession(iSession);
				helper.getHibSession().persist(deg);
				helper.info("Added Degree:  " + degree);
				iCreatedDegrees.put(degree, deg);
			}
			return deg;
		}
	}
	
	protected static Program findProgram(org.hibernate.Session hibSession, Long sessionId, String program) {
		Program prog = hibSession.createQuery(
                "select d from Program d where "+
                "d.session.uniqueId = :sessionId and "+
                "d.externalUniqueId = :program", Program.class).
         setParameter("sessionId", sessionId).
         setParameter("program", program).
         setCacheable(true).
         uniqueResult(); 
		if (prog != null) return prog;
		return hibSession.createQuery(
                "select d from Program d where "+
                "d.session.uniqueId = :sessionId and "+
                "d.reference = :program", Program.class).
         setParameter("sessionId", sessionId).
         setParameter("program", program).
         setCacheable(true).
         uniqueResult();
    }
	
	protected Map<String, Program> iCreatedPrograms = new HashMap<String, Program>();
	protected Program getProgram(OnlineSectioningHelper helper, String program) {
		if (iCreatedPrograms.containsKey(program))
			return iCreatedPrograms.get(program);
		if (iLocking) {
			try {
				sLock.lock("Program:" + iSession.getReference() + ":" + program);
				Program prog = findProgram(helper.getHibSession(), iSession.getUniqueId(), program);
				if (prog != null) return prog;
				prog = new Program();
				prog.setExternalUniqueId(program);
				prog.setReference(program);
				prog.setLabel(program);
				prog.setSession(iSession);
				org.hibernate.Session hibSession = AcademicAreaDAO.getInstance().createNewSession();
				try {
					hibSession.persist(prog);
					hibSession.flush();
				} finally {
					hibSession.close();
				}
				helper.getHibSession().merge(prog);
				helper.info("Added Program:  " + program);
				iCreatedPrograms.put(program, prog);
				return prog;
			} finally {
				sLock.unlock("Program:" + iSession.getReference() + ":" + program);
			}
		} else {
			Program prog = findProgram(helper.getHibSession(), iSession.getUniqueId(), program);
			if (prog == null) {
				prog = new Program();
				prog.setExternalUniqueId(program);
				prog.setReference(program);
				prog.setLabel(program);
				prog.setSession(iSession);
				helper.getHibSession().persist(prog);
				helper.info("Added Program:  " + program);
				iCreatedPrograms.put(program, prog);
			}
			return prog;
		}
	}
	
	protected static Campus findCampus(org.hibernate.Session hibSession, Long sessionId, String campus) {
		Campus camp = hibSession.createQuery(
                "select d from Campus d where "+
                "d.session.uniqueId = :sessionId and "+
                "d.externalUniqueId = :campus", Campus.class).
         setParameter("sessionId", sessionId).
         setParameter("campus", campus).
         setCacheable(true).
         uniqueResult(); 
		if (camp != null) return camp;
		return hibSession.createQuery(
                "select d from Campus d where "+
                "d.session.uniqueId = :sessionId and "+
                "d.reference = :campus", Campus.class).
         setParameter("sessionId", sessionId).
         setParameter("campus", campus).
         setCacheable(true).
         uniqueResult();
    }
	
	protected Map<String, Campus> iCreatedCampuses = new HashMap<String, Campus>();
	protected Campus getCampus(OnlineSectioningHelper helper, String campus) {
		if (iCreatedCampuses.containsKey(campus))
			return iCreatedCampuses.get(campus);
		if (iLocking) {
			try {
				sLock.lock("Campus:" + iSession.getReference() + ":" + campus);
				Campus camp = findCampus(helper.getHibSession(), iSession.getUniqueId(), campus);
				if (camp != null) return camp;
				camp = new Campus();
				camp.setExternalUniqueId(campus);
				camp.setReference(campus);
				camp.setLabel(campus);
				camp.setSession(iSession);
				org.hibernate.Session hibSession = AcademicAreaDAO.getInstance().createNewSession();
				try {
					hibSession.persist(camp);
					hibSession.flush();
				} finally {
					hibSession.close();
				}
				helper.getHibSession().merge(camp);
				helper.info("Added Campus:  " + campus);
				iCreatedCampuses.put(campus, camp);
				return camp;
			} finally {
				sLock.unlock("Campus:" + iSession.getReference() + ":" + campus);
			}
		} else {
			Campus camp = findCampus(helper.getHibSession(), iSession.getUniqueId(), campus);
			if (camp == null) {
				camp = new Campus();
				camp.setExternalUniqueId(campus);
				camp.setReference(campus);
				camp.setLabel(campus);
				camp.setSession(iSession);
				helper.getHibSession().persist(camp);
				helper.info("Added Campus:  " + campus);
				iCreatedCampuses.put(campus, camp);
			}
			return camp;
		}
	}
	
	protected boolean updateStudentDemographics(Student student, OnlineSectioningHelper helper, UpdateResult result) {
		boolean changed = false;
		if (!eq(iFName, student.getFirstName())) {
			student.setFirstName(iFName);
			changed = true;
		}
		if (!eq(iMName, student.getMiddleName())) {
			student.setMiddleName(iMName);
			changed = true;
		}
		if (!eq(iLName, student.getLastName())) {
			student.setLastName(iLName);
			changed = true;
		}
		if (!eq(iEmail, student.getEmail())) {
			student.setEmail(iEmail);
			changed = true;
		}
		
		if (result.getStudentId() == null) {
			helper.getHibSession().persist(student);
			result.setStudentId(student.getUniqueId());
		}
		
		if (iUpdateAcadAreaClasfMj) {
			if (!iAcadAreaClasfMj.isEmpty()) {
				double total = 0.0;
				for (ACM acm: iAcadAreaClasfMj) {
					total += acm.getWeight();
				}
				if (total == 0.0) {
					for (ACM acm: iAcadAreaClasfMj)
						acm.setWeight(1.0 / iAcadAreaClasfMj.size());
				} else {
					double factor = 1.0 / total;
					for (ACM acm: iAcadAreaClasfMj)
						acm.setWeight(factor * acm.getWeight());
				}
			}
			
			List<StudentAreaClassificationMajor> remaining = new ArrayList<StudentAreaClassificationMajor>(student.getAreaClasfMajors());
			aac: for (ACM acm: iAcadAreaClasfMj) {
				if (iStudentCampus == null || iStudentCampus.isEmpty()) {
					if (acm.hasCampus() && !acm.getCampus().equals(iCampus)) continue;
				} else {
					if (acm.hasCampus() && !acm.getCampus().matches(iStudentCampus)) continue;
				}
				for (Iterator<StudentAreaClassificationMajor> i = remaining.iterator(); i.hasNext(); ) {
					StudentAreaClassificationMajor aac = i.next();
					if (acm.matches(aac)) {
						boolean needUpdate = false;
						if (!acm.sameDegree(aac.getDegree())) {
							aac.setDegree(acm.hasDegree() ? getDegree(helper, acm.getDegree()) : null);
							needUpdate = true;
						}
						if (!acm.sameProgram(aac.getProgram())) {
							aac.setProgram(acm.hasProgram() ? getProgram(helper, acm.getProgram()) : null);
							needUpdate = true;
						}
						if (!acm.sameCampus(aac.getCampus())) {
							aac.setCampus(acm.hasCampus() ? getCampus(helper, acm.getCampus()) : null);
							needUpdate = true;
						}
						if (!acm.sameWeight(aac.getWeight())) {
							aac.setWeight(acm.getWeight());
							needUpdate = true;
						}
						if (needUpdate) {
							helper.getHibSession().merge(aac);
							changed = true;
						}
						i.remove(); continue aac;
					}
				}
				
				AcademicArea aa = getAcademicArea(helper, acm.getArea());
				
				AcademicClassification ac = getAcademicClassification(helper, acm.getClassification());
				
				PosMajor posMajor = getPosMajor(helper, aa, acm.getArea(), acm.getMajor());
				
				PosMajorConcentration conc = (acm.hasConcentration() ? getPosMajorConcentration(helper, posMajor, acm.getArea(), acm.getMajor(), acm.getConcentration()) : null);
				
				Degree degree = (acm.hasDegree() ? getDegree(helper, acm.getDegree()) : null);
				Program program = (acm.hasProgram() ? getProgram(helper, acm.getProgram()) : null);
				Campus campus = (acm.hasCampus() ? getCampus(helper, acm.getCampus()) : null);

				StudentAreaClassificationMajor aac = new StudentAreaClassificationMajor();
				aac.setAcademicArea(aa);
				aac.setAcademicClassification(ac);
				aac.setMajor(posMajor);
				aac.setStudent(student);
				aac.setConcentration(conc);
				aac.setDegree(degree);
				aac.setProgram(program);
				aac.setCampus(campus);
				aac.setWeight(acm.getWeight());
				student.addToAreaClasfMajors(aac);
				changed = true;
			}
			
			for (StudentAreaClassificationMajor aac: remaining) {
				student.getAreaClasfMajors().remove(aac);
				helper.getHibSession().remove(aac);
				changed = true;
			}
		}
		
		if (iUpdateAcadAreaClasfMn) {
			List<StudentAreaClassificationMinor> remaining = new ArrayList<StudentAreaClassificationMinor>(student.getAreaClasfMinors());
			aac: for (ACM acm: iAcadAreaClasfMn) {
				if (iStudentCampus == null || iStudentCampus.isEmpty()) {
					if (acm.hasCampus() && !acm.getCampus().equals(iCampus)) continue;
				} else {
					if (acm.hasCampus() && !acm.getCampus().matches(iStudentCampus)) continue;
				}
				for (Iterator<StudentAreaClassificationMinor> i = remaining.iterator(); i.hasNext(); ) {
					StudentAreaClassificationMinor aac = i.next();
					if (acm.matches(aac)) {
						i.remove(); continue aac;
					}
				}
				
				PosMinor posMinor = findPosMinor(helper.getHibSession(), iSession.getUniqueId(), acm.getMinor());
				if (posMinor == null) {
					helper.warn("Minor " + acm.getMinor() + " does not exist.");
					continue aac;
				}

				StudentAreaClassificationMinor aac = new StudentAreaClassificationMinor();
				for (AcademicArea a: posMinor.getAcademicAreas()) {
					aac.setAcademicArea(a);
					break;
				}
				if (aac.getAcademicArea() == null) {
					helper.warn("Minor " + acm.getMinor() + " does not have an academic area.");
					continue aac;
				}
				for (StudentAreaClassificationMajor m: student.getAreaClasfMajors())
					if (m.getWeight() != null && m.getWeight() > 0.0) {
						aac.setAcademicClassification(m.getAcademicClassification());
						break;
					}
				if (aac.getAcademicClassification() == null)
					aac.setAcademicClassification(getAcademicClassification(helper, "00"));
				aac.setMinor(posMinor);
				aac.setStudent(student);
				student.addToAreaClasfMinors(aac);
				changed = true;
			}
			
			for (StudentAreaClassificationMinor aac: remaining) {
				student.getAreaClasfMinors().remove(aac);
				helper.getHibSession().remove(aac);
				changed = true;
			}
		}
		
		return changed;
	}
	
	protected StudentGroupType getStudentGroupType(OnlineSectioningHelper helper, String name) {
		if (iLocking) {
			try {
				sLock.lock("GroupType:" + name);
				StudentGroupType type = StudentGroupType.findByReference(name, helper.getHibSession());
				if (type != null) return type;
				if (type == null && "SPORT".equals(name)) {
					type = new StudentGroupType();
					type.setAdvisorsCanSet(false);
					type.setAllowDisabledSection(StudentGroupType.AllowDisabledSection.NotAllowed);
					type.setKeepTogether(false);
					type.setReference("SPORT");
					type.setLabel("Student Athletes");
				}
				if (type == null && "COHORT".equals(name)) {
					type = new StudentGroupType();
					type.setAdvisorsCanSet(false);
					type.setAllowDisabledSection(StudentGroupType.AllowDisabledSection.NotAllowed);
					type.setKeepTogether(false);
					type.setReference("COHORT");
					type.setLabel("Student Cohorts");
				}
				if (type != null) {
					org.hibernate.Session hibSession = AcademicAreaDAO.getInstance().createNewSession();
					try {
						hibSession.persist(type);
						hibSession.flush();
					} finally {
						hibSession.close();
					}
					helper.getHibSession().merge(type);
				}
				return type;
			} finally {
				sLock.unlock("GroupType:" + name);
			}
		} else {
			StudentGroupType type = StudentGroupType.findByReference(name, helper.getHibSession());
			if (type == null && "SPORT".equals(name)) {
				type = new StudentGroupType();
				type.setAdvisorsCanSet(false);
				type.setAllowDisabledSection(StudentGroupType.AllowDisabledSection.NotAllowed);
				type.setKeepTogether(false);
				type.setReference("SPORT");
				type.setLabel("Student Athletes");
				helper.getHibSession().persist(type);
			}
			if (type == null && "COHORT".equals(name)) {
				type = new StudentGroupType();
				type.setAdvisorsCanSet(false);
				type.setAllowDisabledSection(StudentGroupType.AllowDisabledSection.NotAllowed);
				type.setKeepTogether(false);
				type.setReference("COHORT");
				type.setLabel("Student Cohorts");
				helper.getHibSession().persist(type);
			}
			return type;
		}
	}
	
	protected StudentGroup getStudentGroup(OnlineSectioningHelper helper, StudentGroupType type, String[] g) {
		if (iLocking) {
			try {
				sLock.lock("Group:" + g[0]);
				StudentGroup sg = StudentGroup.findByExternalId(helper.getHibSession(), g[0], iSession.getUniqueId());
				if (sg == null) {
					sg = new StudentGroup();
					sg.setExternalUniqueId(g[0]);
					sg.setSession(iSession);
					sg.setGroupAbbreviation(g[2] == null ? g[0] : g[2]);
					sg.setGroupName(g[3] == null ? g[0] : g[3]);
					org.hibernate.Session hibSession = AcademicAreaDAO.getInstance().createNewSession();
					try {
						sg.setType(g[4] == null ? null : StudentGroupType.findByReference(g[4], hibSession));
						hibSession.persist(sg);
						hibSession.flush();
					} finally {
						hibSession.close();
					}
					helper.info("Added "+(type == null ? "Student" : type.getLabel()) + " Group:  " + sg.getExternalUniqueId() + " -  " + sg.getGroupAbbreviation() + " - " + sg.getGroupName() + " to session " + sg.getSession().academicInitiativeDisplayString());
					helper.getHibSession().merge(sg);
				} else {
					boolean changed = false;
					if (g[2] != null &&  !g[2].equals(sg.getGroupAbbreviation())){
						helper.info("Changed "+(type == null ? "Student" : type.getLabel()) + " Group:  " + sg.getExternalUniqueId() + " - old abbreviation:  " + sg.getGroupAbbreviation() + ", new abbreviation:  " + g[2] + " in session " + sg.getSession().academicInitiativeDisplayString());
						sg.setGroupAbbreviation(g[2]);
						changed = true;
					} 
					if (g[3] != null && !g[3].equals(sg.getGroupName())){
						helper.info("Changed "+(type == null ? "Student" : type.getLabel()) + " Group:  " + sg.getExternalUniqueId() + " - old name:  " + sg.getGroupName() + ", new name:  " + g[3] + " in session " + sg.getSession().academicInitiativeDisplayString());
						sg.setGroupName(g[3]);
						changed = true;
					}
					if (!(type == null ? ""  :type.getReference()).equals(sg.getType() == null ? "" : sg.getType().getReference())) {
						helper.info("Changed "+(type == null ? "Student" : type.getLabel()) + " Group:  " + sg.getExternalUniqueId() + " - old type:  " + (sg.getType() == null ? "null" : sg.getType().getReference()) + ", new type:  " + g[4] + " in session " + sg.getSession().academicInitiativeDisplayString());
						sg.setType(type);
						changed = true;
					}
					if (changed) {
						helper.getHibSession().merge(sg);
					}
				}
				return sg;
			} finally {
				sLock.unlock("Group:" + g[0]);
			}
		} else {
			StudentGroup sg = StudentGroup.findByExternalId(helper.getHibSession(), g[0], iSession.getUniqueId());
			if (sg == null) {
				sg = new StudentGroup();
				sg.setExternalUniqueId(g[0]);
				sg.setSession(iSession);
				sg.setGroupAbbreviation(g[2] == null ? g[0] : g[2]);
				sg.setGroupName(g[3] == null ? g[0] : g[3]);
				sg.setType(type);
				helper.getHibSession().persist(sg);
				helper.info("Added "+(type == null ? "Student" : type.getLabel()) + " Group:  " + sg.getExternalUniqueId() + " -  " + sg.getGroupAbbreviation() + " - " + sg.getGroupName() + " to session " + sg.getSession().academicInitiativeDisplayString());
			} else {
				boolean changed = false;
				if (g[2] != null &&  !g[2].equals(sg.getGroupAbbreviation())){
					helper.info("Changed "+(type == null ? "Student" : type.getLabel()) + " Group:  " + sg.getExternalUniqueId() + " - old abbreviation:  " + sg.getGroupAbbreviation() + ", new abbreviation:  " + g[2] + " in session " + sg.getSession().academicInitiativeDisplayString());
					sg.setGroupAbbreviation(g[2]);
					changed = true;
				} 
				if (g[3] != null && !g[3].equals(sg.getGroupName())){
					helper.info("Changed "+(type == null ? "Student" : type.getLabel()) + " Group:  " + sg.getExternalUniqueId() + " - old name:  " + sg.getGroupName() + ", new name:  " + g[3] + " in session " + sg.getSession().academicInitiativeDisplayString());
					sg.setGroupName(g[3]);
					changed = true;
				}
				if (!(type == null ? ""  :type.getReference()).equals(sg.getType() == null ? "" : sg.getType().getReference())) {
					helper.info("Changed "+(type == null ? "Student" : type.getLabel()) + " Group:  " + sg.getExternalUniqueId() + " - old type:  " + (sg.getType() == null ? "null" : sg.getType().getReference()) + ", new type:  " + g[4] + " in session " + sg.getSession().academicInitiativeDisplayString());
					sg.setType(type);
					changed = true;
				}
				if (changed) {
					helper.getHibSession().merge(sg);
				}
			}
			return sg;
		}
	}
	
	protected boolean updateStudentGroups(Student student, OnlineSectioningHelper helper) {
		Set<StudentGroup> groups = new HashSet<StudentGroup>();
		for (String[] g: iGroups) {
			if (g[1] != null) {
				if (iStudentCampus == null || iStudentCampus.isEmpty()) {
					if (!g[1].equals(iCampus)) continue;
				} else {
					if (!g[1].matches(iStudentCampus)) continue;
				}
			}
			if (iIgnoreGroupRegExp != null && g[0].matches(iIgnoreGroupRegExp)) continue;
			StudentGroup sg = null;
			for (StudentGroup x: student.getGroups()) {
				if (g[0].equals(x.getExternalUniqueId())) {
					sg = x; break;
				}
			}
			StudentGroupType type = getStudentGroupType(helper, g[4]);
			if (g[4] != null) {
				if (sg != null && sg.getType() != null && sg.getType().getReference().equals(g[4])) {
					type = sg.getType();
				} else {
					type = getStudentGroupType(helper, g[4]);
				}
			}
			if (sg == null)
				sg = getStudentGroup(helper, type, g);
			groups.add(sg);
		}
		boolean changed = false;
		for (Iterator<StudentGroup> i = student.getGroups().iterator(); i.hasNext(); ) {
			StudentGroup g = i.next();
			if (groups.remove(g)) continue;
			if (g.getExternalUniqueId() != null) {
				g.getStudents().remove(student);
				i.remove();
				changed = true;
				helper.info("Student " + student.getExternalUniqueId() + " dropped from " + g.getGroupName() + (g.getType() == null ? "" : " (" + g.getType().getReference() + ")"));
			}
		}
		for (StudentGroup g: groups) {
			g.addToStudents(student);
			student.addToGroups(g);
			changed = true;
			helper.info("Student " + student.getExternalUniqueId() + " added to " + g.getGroupName() + (g.getType() == null ? "" : " (" + g.getType().getReference() + ")"));
		}
    	return changed;
	}
	
	protected boolean isParentOf(Class_ c1, Class_ c2) {
		if (c2 == null) return false;
		if (c1.equals(c2.getParentClass())) return true;
		return isParentOf(c1, c2.getParentClass());
	}
	
	protected boolean hasChild(Class_ c1, Collection<Class_> classes) {
		for (Class_ c2: classes) {
			if (isParentOf(c1, c2)) return true;
		}
		return false;
	}
	
	protected Map<InstructionalOffering, Map<String, Set<Class_>>> getOverrides(OnlineSectioningHelper helper, UpdateResult result) {
		Map<InstructionalOffering, Map<String, Set<Class_>>> restrictions = new HashMap<InstructionalOffering, Map<String, Set<Class_>>>();
		for (String[] override: iOverrides) {
			String type = override[0];
			if (type == null || type.isEmpty()) continue;
			if (iOverrideTypes != null && !type.matches(iOverrideTypes)) {
				helper.info("Ignoring override type " + type);
				continue;
			}
			
			String subject = override[1], course = override[2];

			Integer crn = null;
			if (override[3] != null && !override[3].isEmpty()) {
				try {
					crn = Integer.valueOf(override[3]);
				} catch (NumberFormatException e) {
					helper.warn("Failed to parse CRN " + override[3]);
				}
			}
			
			if (crn == null) {
				List<CourseOffering> courses = helper.getHibSession().createQuery(
						"from CourseOffering co where " +
						"co.instructionalOffering.session.uniqueId = :sessionId and " +
						"co.instructionalOffering.notOffered = false and " + 
						"co.subjectArea.subjectAreaAbbreviation = :subject and co.courseNbr like :course", CourseOffering.class)
						.setParameter("subject", subject).setParameter("course", course + "%").setParameter("sessionId", iSession.getUniqueId()).list();
				if (course.isEmpty()) {
					helper.error("No course offering found for subject " + subject + ", course number " + course + " and banner session " + iTermCode);
					result.setStatus(Status.PROBLEM);
				}
				// all matching courses
				for (CourseOffering co: courses) {
					Map<String, Set<Class_>> type2classes = restrictions.get(co.getInstructionalOffering());
					if (type2classes == null) {
						type2classes = new HashMap<String, Set<Class_>>();
						restrictions.put(co.getInstructionalOffering(), type2classes);
					}
					Set<Class_> classes = type2classes.get(type);
					if (classes == null) {
						classes = new HashSet<Class_>();
						type2classes.put(type, classes);
					}
				}
			} else {
				CourseOffering co = BannerSection.findCourseOfferingForCrnAndTermCode(helper.getHibSession(), crn, iTermCode);
				if (co == null) {
					helper.error("No course offering found for CRN " + crn + " and banner session " + iTermCode);
					result.setStatus(Status.PROBLEM);
					continue;
				}
				if (!iSession.equals(co.getInstructionalOffering().getSession())) continue;
				
				Map<String, Set<Class_>> type2classes = restrictions.get(co.getInstructionalOffering());
				if (type2classes == null) {
					type2classes = new HashMap<String, Set<Class_>>();
					restrictions.put(co.getInstructionalOffering(), type2classes);
				}
				Set<Class_> classes = type2classes.get(type);
				if (classes == null) {
					classes = new HashSet<Class_>();
					type2classes.put(type, classes);
				}

				boolean foundClass = false;
				for(Iterator<?> it = BannerSection.findAllClassesForCrnAndTermCode(helper.getHibSession(), crn, iTermCode).iterator(); it.hasNext();) {
					Class_ c = (Class_) it.next();
					if (!iSession.equals(c.getSession())) continue;
					while (c!=null) {
						classes.add(c);
						c = c.getParentClass();
					}
					foundClass = true;
				}
				if (!foundClass) {
					helper.error("No classes found for CRN " + crn + " and banner session " + iTermCode);
					result.setStatus(Status.PROBLEM);
					continue;
				}
			}
		}
		return restrictions;
	}
	
	protected OverrideType getType(Map<String, Set<Class_>> restrictions) {
		boolean time = false, space = false, link = false;
		OverrideType other = null;
		Set<Class_> otherRestrictions = null;
		for (Map.Entry<String, Set<Class_>> e: restrictions.entrySet()) {
			String type = e.getKey();
			if (OverrideType.AllowTimeConflict.getReference().equalsIgnoreCase(type)) time = true;
			else if (OverrideType.AllowOverLimit.getReference().equalsIgnoreCase(type)) space = true;
			else if (OverrideType.CoReqOverride.getReference().equalsIgnoreCase(type)) link = true;
			else {
				for (OverrideType t: OverrideType.values())
					if (t.getReference().equalsIgnoreCase(type)) {
						if (other == null || e.getValue().size() > otherRestrictions.size() || (e.getValue().size() == otherRestrictions.size() && t.ordinal() < other.ordinal())) {
							other = t;
							otherRestrictions = e.getValue();
						}
					}
			}
		}
		if (time && space && link) return OverrideType.AllowOverLimitTimeConflictLink;
		if (time && link) return OverrideType.AllowTimeConflictLink;
		if (space && link) return OverrideType.AllowOverLimitLink;
		if (link) return OverrideType.CoReqOverride;
		if (time && space) return OverrideType.AllowOverLimitTimeConflict;
		if (time) return OverrideType.AllowTimeConflict;
		if (space) return OverrideType.AllowOverLimit;
		if (other != null) return other;
		return OverrideType.Other;
	}
	
	protected Set<Class_> getClasses(Map<String, Set<Class_>> restrictions) {
		Set<Class_> union = new HashSet<Class_>();
		for (Set<Class_> c: restrictions.values()) {
			union.addAll(c);
		}
		return union;
	}
	
	protected boolean updateStudentOverrides(Student student, OnlineSectioningServer server, OnlineSectioningHelper helper, UpdateResult result) {
		boolean changed = false;

		Map<InstructionalOffering, Map<String, Set<Class_>>> restrictions = getOverrides(helper, result);
		
		Set<OverrideReservation> overrides = new HashSet<OverrideReservation>(helper.getHibSession().createQuery(
				"select r from OverrideReservation r inner join r.students s where s.uniqueId = :studentId", OverrideReservation.class)
			.setParameter("studentId", student.getUniqueId()).list());
		
		overrides: for (Map.Entry<InstructionalOffering, Map<String, Set<Class_>>> e: restrictions.entrySet()) {
			InstructionalOffering io = e.getKey();
			
			OverrideType type = getType(e.getValue());
			Set<Class_> classes = getClasses(e.getValue());
			
			// do not create an override reservation for course-level override that is not a time and/or a space conflict
			if (classes.isEmpty() && !type.isAllowOverLimit() && !type.isAllowTimeConflict()) continue;

			OverrideReservation override = null;
			
			// lookup a matching reservation
			for (Reservation r: io.getReservations()) {
				if (!(r instanceof OverrideReservation) || !((OverrideReservation)r).getOverrideType().equals(type)) continue; // skip other reservations
				int cnt = 0;
				boolean match = true;
				for (Class_ c: classes) {
					if (hasChild(c, classes)) continue;
					if (r.getClasses().contains(c)) {
						cnt ++;
					} else {
						match = false; break;
					}
				}
				if (match && r.getClasses().size() == cnt && r.getConfigurations().isEmpty()) {
					// match found
					if (overrides.remove(r)) {
						// student already in --> no changes are needed
						continue overrides;
					} else {
						// student not present --> add this student
						override = (OverrideReservation)r;
						override.addToStudents(student);
						helper.info("Updated " + type.getReference() + " override for " + io.getCourseName() + " [" + student.getExternalUniqueId()  + " added]");
						helper.getHibSession().merge(override);
						break;
					}
				}
			}

			if (override == null) {
				// no match --> create a new override reservation
				override = new OverrideReservation();
				override.setOverrideType(type);
				override.setStudents(new HashSet<Student>());
				override.setConfigurations(new HashSet<InstrOfferingConfig>());
				override.setClasses(new HashSet<Class_>());
				override.setInstructionalOffering(io);
				io.addToReservations(override);
				override.addToStudents(student);
				for (Class_ c: classes)
					if (!hasChild(c, classes))
						override.addToClasses(c);

				helper.info("Created " + type.getReference() + " override for " + io.getCourseName() + " [" + student.getExternalUniqueId()  + " added]");
				helper.getHibSession().persist(override);
			}

			if (server != null) {
				Lock w = server.writeLock();
				try {
					XOffering offering = server.getOffering(io.getUniqueId());
					if (offering != null) {
						// remove the previous one
						for (Iterator<XReservation> i = offering.getReservations().iterator(); i.hasNext(); ) {
							XReservation r = i.next();
							if (r.getType() == XReservationType.IndividualOverride && r.getReservationId().equals(override.getUniqueId())) {
								i.remove(); break;
							}
						}
						// load the new one
						offering.getReservations().add(new XIndividualReservation(offering, override));
						server.update(offering);
					}
				} finally {
					w.release();
				}
			}
			changed = true;
		}
		
		for (OverrideReservation override: overrides) {
			if (override.getStudents().size() > 1) {
				helper.info("Updated " + override.getOverrideType().getReference() + " override for " + override.getInstructionalOffering().getCourseName() + " [" + student.getExternalUniqueId()  + " removed]");
				override.getStudents().remove(student);
				helper.getHibSession().merge(override);
			} else {
				helper.info("Removed " + override.getOverrideType().getReference() + " override for " + override.getInstructionalOffering().getCourseName() + " [" + student.getExternalUniqueId()  + " removed]");
				override.getInstructionalOffering().getReservations().remove(override);
				helper.getHibSession().remove(override);
			}
			if (server != null) {
				Lock w = server.writeLock();
				try {
					XOffering offering = server.getOffering(override.getInstructionalOffering().getUniqueId());
					if (offering != null) {
						for (Iterator<XReservation> i = offering.getReservations().iterator(); i.hasNext(); ) {
							XReservation r = i.next();
							if (r.getType() == XReservationType.IndividualOverride && r.getReservationId().equals(override.getUniqueId())) {
								XIndividualReservation ir = (XIndividualReservation)r;
								if (ir.getStudentIds().size() > 1) {
									ir.getStudentIds().remove(result.getStudentId());
								} else {
									i.remove();
								}
								break;
							}
						}
						server.update(offering);
					}
				} finally {
					w.release();
				}
			}
			changed = true;
		}
		
		return changed;
	}
	
	protected boolean updateClassEnrollments(Student student, Map<CourseOffering, List<Class_>> courseToClassEnrollments, OnlineSectioningHelper helper) {
		boolean changed = false;
		Date ts = new Date();

		Hashtable<Pair, StudentClassEnrollment> enrollments = new Hashtable<Pair, StudentClassEnrollment>();
		if (student.getClassEnrollments() != null) {
			List<StudentClassEnrollment> duplicates = new ArrayList<StudentClassEnrollment>();
        	for (StudentClassEnrollment enrollment: student.getClassEnrollments()) {
        		StudentClassEnrollment previous = enrollments.put(new Pair(enrollment.getCourseOffering().getUniqueId(), enrollment.getClazz().getUniqueId()), enrollment);
        		// check for duplicate enrollments
        		if (previous != null) duplicates.add(previous);
        	}
        	// remove duplicate enrollments
        	for (StudentClassEnrollment enrollment: duplicates) {
    			student.getClassEnrollments().remove(enrollment);
    			helper.getHibSession().remove(enrollment);
    			changed = true;
        	}
    	}
    	int nextPriority = 0;
    	for (CourseDemand cd: student.getCourseDemands())
    		if (!cd.isAlternative() && cd.getPriority() >= nextPriority)
    			nextPriority = cd.getPriority() + 1;
    	Set<CourseDemand> remaining = new HashSet<CourseDemand>(student.getCourseDemands());
    	boolean fixCourseDemands = false;
    	
    	// populate course2request, check for course request duplicates
    	Map<CourseOffering, CourseRequest> course2request = new Hashtable<CourseOffering, CourseRequest>();
    	for (CourseDemand cd: student.getCourseDemands())
    		for (CourseRequest cr: cd.getCourseRequests()) {
    			CourseRequest previous = course2request.put(cr.getCourseOffering(), cr);
    			if (previous != null) fixCourseDemands = true;
    		}

    	for (Map.Entry<CourseOffering, List<Class_>> entry: courseToClassEnrollments.entrySet()) {
    		CourseOffering co = entry.getKey();
    		
    		CourseRequest cr = course2request.get(co);
    		if (cr == null) {
    			CourseDemand cd = new CourseDemand();
    			cd.setTimestamp(ts);
    			cd.setCourseRequests(new HashSet<CourseRequest>());
    			cd.setEnrollmentMessages(new HashSet<StudentEnrollmentMessage>());
    			cd.setStudent(student);
    			student.getCourseDemands().add(cd);
    			cd.setAlternative(false);
    			cd.setPriority(nextPriority++);
    			cd.setWaitlist(false);
    			cr = new CourseRequest();
    			cd.getCourseRequests().add(cr);
    			cr.setCourseDemand(cd);
    			cr.setCourseRequestOptions(new HashSet<CourseRequestOption>());
    			cr.setAllowOverlap(false);
    			cr.setCredit(0);
    			cr.setOrder(0);
    			cr.setCourseOffering(co);
    			fixCourseDemands = true;
        		changed = true;
    		} else {
    			if (!remaining.remove(cr.getCourseDemand()) && cr.getCourseDemand().getCourseRequests().size() > 1) {
    				// course demand has been already removed -> need to split the course demand
    				cr.getCourseDemand().getCourseRequests().remove(cr);
    				CourseDemand cd = new CourseDemand();
        			cd.setTimestamp(ts);
        			cd.setCourseRequests(new HashSet<CourseRequest>());
        			cd.setEnrollmentMessages(new HashSet<StudentEnrollmentMessage>());
        			cd.setStudent(student);
        			student.getCourseDemands().add(cd);
        			cd.setAlternative(false);
        			cd.setPriority(nextPriority++);
        			cd.setWaitlist(false);
        			cr.setCourseDemand(cd);
        			cd.getCourseRequests().add(cr);
        			fixCourseDemands = true;
        			changed = true;
    			}
    			for (Iterator<StudentEnrollmentMessage> i = cr.getCourseDemand().getEnrollmentMessages().iterator(); i.hasNext(); ) {
					StudentEnrollmentMessage message = i.next();
					helper.getHibSession().remove(message);
					i.remove();
				}
    		}
    		
    		for (Class_ clazz: entry.getValue()) {
    			StudentClassEnrollment enrollment = enrollments.remove(new Pair(co.getUniqueId(), clazz.getUniqueId()));
        		if (enrollment == null) {
            		enrollment = new StudentClassEnrollment();
            		enrollment.setStudent(student);
            		enrollment.setClazz(clazz);
            		enrollment.setCourseOffering(co);
            		enrollment.setTimestamp(ts);
            		student.getClassEnrollments().add(enrollment);    
            		changed = true;
        		}

        		if (enrollment.getCourseRequest() == null || !cr.equals(enrollment.getCourseRequest())) {
        			enrollment.setCourseRequest(cr);
        			changed = true;
        		}
    		}
    		
    		if (iResetWaitList && cr.getCourseDemand().isWaitlist() && !co.equals(cr.getCourseDemand().getWaitListSwapWithCourseOffering())) {
    			cr.getCourseDemand().setWaitlist(false);
    			changed = true;
    			helper.getHibSession().merge(cr.getCourseDemand());
    			if (student.getWaitListMode() == WaitListMode.WaitList)
    				student.addWaitList(cr.getCourseOffering(), WaitListType.EXTERNAL_UPDATE, false, "BANNER", ts, helper.getHibSession()); 
    		}
    	}
    	
    	
    	Set<CourseDemand> exDropDeletes = new HashSet<CourseDemand>();
    	if (!enrollments.isEmpty()) {
    		for (StudentClassEnrollment enrollment: enrollments.values()) {
    			CourseRequest cr = course2request.get(enrollment.getCourseOffering());
    			if (cr != null && remaining.contains(cr.getCourseDemand())) {
    				if (cr.getCourseRequestOverrideIntent() == CourseRequestOverrideIntent.EX_DROP)
    					exDropDeletes.add(cr.getCourseDemand());
    				else if (iResetWaitList && cr.getCourseDemand().isWaitlist()) {
    					cr.getCourseDemand().setWaitlist(false);
    	    			helper.getHibSession().merge(cr.getCourseDemand());
    					if (student.getWaitListMode() == WaitListMode.WaitList)
    	    				student.addWaitList(cr.getCourseOffering(), WaitListType.EXTERNAL_UPDATE, false, "BANNER", ts, helper.getHibSession());
    				}
    			}
    			student.getClassEnrollments().remove(enrollment);
    			helper.getHibSession().remove(enrollment);
    		}
    		changed = true;
    	}

    	if ((fixCourseDemands || !exDropDeletes.isEmpty()) && student.getUniqueId() != null) {
    		// removed intended extended course drops
    		if (!exDropDeletes.isEmpty()) {
    			for (CourseDemand cd: exDropDeletes) {
        			if (cd.getFreeTime() != null)
        				helper.getHibSession().remove(cd.getFreeTime());
        			for (CourseRequest cr: cd.getCourseRequests())
        				helper.getHibSession().remove(cr);
        			student.getCourseDemands().remove(cd);
        			helper.getHibSession().remove(cd);
        		}
    		}
    		// fix priorities
    		int priority = 0;
    		for (CourseDemand cd: new TreeSet<CourseDemand>(student.getCourseDemands())) {
    			cd.setPriority(priority++);
    			if (cd.getUniqueId() == null)
        			helper.getHibSession().persist(cd);
    			else
    				helper.getHibSession().merge(cd);
    		}
    	}
    	
    	return changed;
	}
	
	public Map<CourseOffering, List<Class_>> getEnrollments(OnlineSectioningHelper helper, UpdateResult result) {
		Map<CourseOffering, List<Class_>> enrollments = new HashMap<CourseOffering, List<Class_>>();
		for (Integer crn: iCRNs) {
			CourseOffering co = BannerSection.findCourseOfferingForCrnAndTermCode(helper.getHibSession(), crn, iTermCode);
			if (co == null) {
				helper.error("No course offering found for CRN " + crn + " and banner session " + iTermCode);
				result.setStatus(Status.PROBLEM);
				continue;
			}
			if (!iSession.equals(co.getInstructionalOffering().getSession()))
				continue;
			
			boolean foundClasses = false;
			for(Iterator<?> it = BannerSection.findAllClassesForCrnAndTermCode(helper.getHibSession(), crn, iTermCode).iterator(); it.hasNext();) {
				Class_ c = (Class_) it.next();
				if (!iSession.equals(c.getSession())) continue;
				
				foundClasses = true;
				List<Class_> classes = enrollments.get(co);
				if (classes == null) {
					classes = new ArrayList<Class_>(); enrollments.put(co, classes);
				}
				classes.add(c);
			}
			if (!foundClasses) {
				helper.error("No classes found for CRN " + crn + " and banner session " + iTermCode);
				result.setStatus(Status.PROBLEM);
			}
		}
		return enrollments;
	}
	
	protected Advisor getAdvisor(OnlineSectioningHelper helper, String externalId, String type) {
		Roles role = null;
		if (type != null && !type.isEmpty())
			role = Roles.getRole(type + " Advisor", helper.getHibSession());
		if (role == null)
			role = Roles.getRole("Advisor", helper.getHibSession());
		if (role == null) {
			helper.warn("No advisor role found for " + type);
			return null;
		}
		if (iLocking) {
			try {
				sLock.lock("Advisor:" + externalId);
				Advisor advisor = helper.getHibSession().createQuery(
						"from Advisor where externalUniqueId = :externalId and role.roleId = :roleId and session.uniqueId = :sessionId", Advisor.class)
						.setParameter("externalId", externalId).setParameter("roleId", role.getRoleId()).setParameter("sessionId", iSession.getUniqueId())
						.setCacheable(true).setMaxResults(1).uniqueResult();
				if (advisor != null) return advisor;
				advisor = new Advisor();
				advisor.setExternalUniqueId(externalId);
				advisor.setRole(role);
				advisor.setSession(iSession);
				advisor.setStudents(new HashSet<Student>());
				try {
					if ("true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.advisorLookup.ldapFirst", "true"))) {
						if (!updateDetailsFromLdap(advisor))
							updateDetailsFromBanner(advisor, helper);
					} else {
						if (!updateDetailsFromBanner(advisor, helper))
							updateDetailsFromLdap(advisor);
					}
				} catch (Throwable t) {
					helper.info("Failed to lookup advisor details: " + t.getMessage(), t);
				}
				org.hibernate.Session hibSession = AcademicAreaDAO.getInstance().createNewSession();
				try {
					hibSession.persist(advisor);
					hibSession.flush();
				} finally {
					hibSession.close();
				}
				helper.info("Added Advisor:  " + advisor.getExternalUniqueId() + " - " + advisor.getRole().getReference() + " to session " + iSession.academicInitiativeDisplayString());
				helper.getHibSession().merge(advisor);
				return advisor;
			} finally {
				sLock.unlock("Advisor:" + externalId);
			}
		} else {
			Advisor advisor = helper.getHibSession().createQuery(
					"from Advisor where externalUniqueId = :externalId and role.roleId = :roleId and session.uniqueId = :sessionId", Advisor.class)
					.setParameter("externalId", externalId).setParameter("roleId", role.getRoleId()).setParameter("sessionId", iSession.getUniqueId())
					.setCacheable(true).setMaxResults(1).uniqueResult();
			if (advisor == null) {
				advisor = new Advisor();
				advisor.setExternalUniqueId(externalId);
				advisor.setRole(role);
				advisor.setSession(iSession);
				advisor.setStudents(new HashSet<Student>());
				helper.getHibSession().persist(advisor);
				try {
					if ("true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.advisorLookup.ldapFirst", "true"))) {
						if (!updateDetailsFromLdap(advisor))
							updateDetailsFromBanner(advisor, helper);
					} else {
						if (!updateDetailsFromBanner(advisor, helper))
							updateDetailsFromLdap(advisor);
					}
				} catch (Throwable t) {
					helper.info("Failed to lookup advisor details: " + t.getMessage(), t);
				}
				helper.info("Added Advisor:  " + advisor.getExternalUniqueId() + " - " + advisor.getRole().getReference() + " to session " + advisor.getSession().academicInitiativeDisplayString());
			}
			return advisor;
		}
	}
	
	protected boolean updateAdvisors(Student student, OnlineSectioningHelper helper) {
		Set<Advisor> advisors = new HashSet<Advisor>();
		for (String[] a: iAdvisors) {
			String externalId = a[0], type = a[1];
			Advisor advisor = getAdvisor(helper, externalId, type);
			if (advisor == null) continue;
			advisors.add(advisor);
		}
		boolean changed = false;
		if (student.getAdvisors() != null)
			for (Iterator<Advisor> i = student.getAdvisors().iterator(); i.hasNext(); ) {
				Advisor a = i.next();
				if (advisors.remove(a)) continue;
				a.getStudents().remove(student);
				i.remove();
				changed = true;
				helper.info("Student " + student.getExternalUniqueId() + " dropped from advisor " + a.getExternalUniqueId());
			}
		for (Advisor a: advisors) {
			a.addToStudents(student);
			student.addToAdvisors(a);
			changed = true;
			helper.info("Student " + student.getExternalUniqueId() + " added to advisor " + a.getExternalUniqueId());
		}
    	return changed;
	}
	
	protected boolean eq(Object o1, Object o2) {
		if (o1 == null)
			return o2 == null;
		else
			return o1.equals(o2);
	}

	@Override
	public String name() {
		return "banner-update";
	}
	
	public static enum Status {
		NO_CHANGE,
		OK,
		PROBLEM,
		FAILURE,
	}
	
	public static enum Change {
		CREATED,
		DEMOGRAPHICS,
		GROUPS,
		ADVISORS,
		CLASSES,
		OVERRIDES,
		STATUS,
		;
		public int flag() { return 1 << ordinal(); }
	}
	
	public static class OfferingCheck implements Serializable {
		private static final long serialVersionUID = 1L;
		private Long iOfferingId;
		private Set<Long> iExcludeStudents;
		private Set<Long> iIncludeStudents;
		private boolean iAllStudents = false;
		
		public OfferingCheck(Long offeringId) {
			iOfferingId = offeringId;
		}
		
		public Long getOfferingId() { return iOfferingId; }
		@Override
		public int hashCode() { return iOfferingId.hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof OfferingCheck)) return false;
			return getOfferingId().equals(((OfferingCheck)o).getOfferingId());
		}
		
		public boolean hasExcludeStudents() { return iExcludeStudents != null && !iExcludeStudents.isEmpty(); }
		public void addExcludeStudent(Long studentId) {
			if (iExcludeStudents == null) iExcludeStudents = new HashSet<Long>();
			iExcludeStudents.add(studentId);
		}
		public Set<Long> getExcludeStudents() { return iExcludeStudents; }
		
		public boolean hasIncludeStudents() { return iIncludeStudents != null && !iIncludeStudents.isEmpty(); }
		public void addIncludeStudent(Long studentId) {
			if (iIncludeStudents == null) iIncludeStudents = new HashSet<Long>();
			iIncludeStudents.add(studentId);
		}
		public Set<Long> getIncludeStudents() { return iIncludeStudents; }
		
		public boolean isAllStudents() { return iAllStudents; }
		public void setAllStudents(boolean allStudents) { iAllStudents = allStudents; }
		
		public boolean merge(OfferingCheck check) {
			if (!check.equals(this)) return false;
			setAllStudents(isAllStudents() || check.isAllStudents());
			if (check.hasIncludeStudents()) {
				if (iIncludeStudents == null) iIncludeStudents = new HashSet<Long>();
				iIncludeStudents.addAll(check.getIncludeStudents());
			}
			if (check.hasExcludeStudents()) {
				if (iExcludeStudents == null) iExcludeStudents = new HashSet<Long>();
				iExcludeStudents.addAll(check.getExcludeStudents());
			}
			return true;
		}
		
		public CheckOfferingAction createCheckOfferingAction(OnlineSectioningServer server) {
			CheckOfferingAction action = server.createAction(CheckOfferingAction.class);
			action.forOfferings(getOfferingId());
			if (isAllStudents()) {
				if (hasExcludeStudents())
					action.skipStudents(getExcludeStudents());
			} else if (hasIncludeStudents()) {
				action.forStudents(getIncludeStudents());
			}
			return action;
		}
		
		@Override
		public String toString() {
			if (isAllStudents()) {
				if (hasExcludeStudents())
					return getOfferingId() + "(all but " + getExcludeStudents().size() + " students)";
				else
					return getOfferingId() + "(all students)";
			} else if (hasIncludeStudents()) {
				return getOfferingId() + "(only " + getIncludeStudents().size() + " students)";
			} else {
				return getOfferingId().toString();
			}
		}
	}
	
	public static class UpdateResult implements Serializable {
		private static final long serialVersionUID = 1L;
		private Status iStatus = Status.OK;
		private int iChanges = 0;
		private Long iStudentId = null;
		private List<OfferingCheck> iOfferingChecks = null;
		
		public Status getStatus() { return iStatus; }
		public void setStatus(Status status) { iStatus = status; }
		
		public void add(Change change) {
			iChanges = (iChanges | change.flag());
		}
		public boolean hasChanges() {
			return iChanges != 0;
		}
		public boolean has(Change... changes) {
			for (Change ch: changes)
				if ((iChanges & ch.flag()) != 0) return true;
			return false;
		}
		public void setStudentId(Long studentId) { iStudentId = studentId; }
		public Long getStudentId() { return iStudentId; }
		
		public boolean hasOfferingChecks() { return iOfferingChecks != null && !iOfferingChecks.isEmpty(); }
		public void addCheckOfferingForOthers(Long offeringId) {
			if (iOfferingChecks == null) iOfferingChecks = new ArrayList<OfferingCheck>();
			OfferingCheck check = new OfferingCheck(offeringId);
			check.setAllStudents(true); check.addExcludeStudent(iStudentId);
			iOfferingChecks.add(check);
		}
		public void addCheckOfferingForMe(Long offeringId) {
			if (iOfferingChecks == null) iOfferingChecks = new ArrayList<OfferingCheck>();
			OfferingCheck check = new OfferingCheck(offeringId);
			check.setAllStudents(false); check.addIncludeStudent(iStudentId);
			iOfferingChecks.add(check);
		}
		public Collection<OfferingCheck> getOfferingChecks() { return iOfferingChecks; }
	}

	public static class Pair {
		private Long iCourseId, iClassId;
		public Pair(Long courseId, Long classId) {
			iCourseId = courseId; iClassId = classId;
		}
		public Long getCourseId() { return iCourseId; }
		public Long getClassId() { return iClassId; }
		public boolean equals(Object o) {
			if (o == null || !(o instanceof Pair)) return false;
			Pair p = (Pair)o;
			return getCourseId().equals(p.getCourseId()) && getClassId().equals(p.getClassId());
		}
		public int hashCode() {
			return getCourseId().hashCode() ^ getClassId().hashCode();
		}
	}
	
    private static String getAttribute(Attributes attrs, String name) {
        if (attrs == null) return null;
        if (name == null || name.isEmpty()) return null;
        for (StringTokenizer stk = new StringTokenizer(name, ","); stk.hasMoreTokens(); ) {
            Attribute a = attrs.get(stk.nextToken());
            try {
                if (a!=null && a.get()!=null) return a.get().toString();
            } catch (NamingException e) {
            }
        }
        return null;
    }
    
    public static boolean updateDetailsFromLdap(Advisor advisor) throws NamingException {
    	String url = ApplicationProperty.PeopleLookupLdapUrl.value();
    	if (url == null) return false;
    	
    	ExternalUidTranslation translation = null;
        if (ApplicationProperty.ExternalUserIdTranslation.value()!=null) {
            try {
                translation = (ExternalUidTranslation)Class.forName(ApplicationProperty.ExternalUserIdTranslation.value()).getConstructor().newInstance();
            } catch (Exception e) {}
        }
        String uid = advisor.getExternalUniqueId();
        if (translation != null) uid = translation.translate(advisor.getExternalUniqueId(), Source.Staff, Source.LDAP);
        if (!advisor.getExternalUniqueId().equals(uid))
        	advisor.setLastName(uid);
        
        InitialDirContext ctx = null;
        try {
            Hashtable<String,String> env = new Hashtable<String, String>();
            env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
            env.put(Context.PROVIDER_URL, url);
            if (ApplicationProperty.PeopleLookupLdapUser.value() != null) {
                env.put(Context.SECURITY_AUTHENTICATION, "simple");
            	env.put(Context.SECURITY_PRINCIPAL, ApplicationProperty.PeopleLookupLdapUser.value());
            	env.put(Context.SECURITY_CREDENTIALS, ApplicationProperty.PeopleLookupLdapPassword.value());
            } else {
                env.put(Context.SECURITY_AUTHENTICATION, "none");
            }
            String referral = ApplicationProperty.PeopleLookupLdapReferral.value();
			if (referral != null)
				env.put(Context.REFERRAL, referral);
            
            ctx = new InitialDirContext(env);
            SearchControls ctls = new SearchControls();
            ctls.setCountLimit(ApplicationProperty.PeopleLookupLdapLimit.intValue());
			if (ApplicationProperty.PeopleLookupLdapSearchSubtree.isTrue())
				ctls.setSearchScope(SearchControls.SUBTREE_SCOPE);
            String filter = "(" + ApplicationProperty.PeopleLookupLdapUidAttribute.value() + "=" + uid + ")";
            for (NamingEnumeration<SearchResult> e = ctx.search(ApplicationProperty.PeopleLookupLdapBase.value(), filter, ctls); e.hasMore(); ) {
            	Attributes a = e.next().getAttributes();
            	advisor.setFirstName(Constants.toInitialCase(getAttribute(a, ApplicationProperty.PeopleLookupLdapGivenNameAttribute.value())));
            	advisor.setMiddleName(Constants.toInitialCase(getAttribute(a, ApplicationProperty.PeopleLookupLdapCnAttribute.value())));
            	advisor.setLastName(Constants.toInitialCase(getAttribute(a, ApplicationProperty.PeopleLookupLdapSnAttribute.value())));
                if (advisor.getMiddleName()!=null && advisor.getFirstName()!=null && advisor.getMiddleName().indexOf(advisor.getFirstName())>=0)
                	advisor.setMiddleName(advisor.getMiddleName().replaceAll(advisor.getFirstName()+" ?", ""));
                if (advisor.getMiddleName()!=null && advisor.getLastName()!=null && advisor.getMiddleName().indexOf(advisor.getLastName())>=0)
                	advisor.setMiddleName(advisor.getMiddleName().replaceAll(" ?"+advisor.getLastName(), ""));
            	advisor.setAcademicTitle(getAttribute(a, ApplicationProperty.PeopleLookupLdapAcademicTitleAttribute.value()));
            	advisor.setEmail(getAttribute(a, ApplicationProperty.PeopleLookupLdapEmailAttribute.value()));
            	return advisor.getEmail() != null && !advisor.getEmail().isEmpty();
            }
        } finally {
            try {
                if (ctx != null) ctx.close();
            } catch (Exception e) {}
        }
        return false;
    }
    
    public static boolean updateDetailsFromBanner(Advisor advisor, OnlineSectioningHelper helper) {
    	String lookupQuery = ApplicationProperties.getProperty("banner.advisorLookup.nativeQuery",
    			"select first_name, last_name, email from timetable.szv_utm_advisors where puid = lpad(?,9,'0')");
    	if (lookupQuery == null || lookupQuery.isEmpty()) return false;
    	try {
    		Object[] details = helper.getHibSession().createNativeQuery(
    				lookupQuery, Object[].class
        			).setParameter(1, advisor.getExternalUniqueId()).setMaxResults(1).uniqueResult();
        	if (details != null) {
        		advisor.setFirstName((String)details[0]);
        		advisor.setLastName((String)details[1]);
        		advisor.setEmail((String)details[2]);
        		return true;
        	}
    	} catch (HibernateException e) {
    		helper.info("Failed to lookup advisor details from Banner: " + e.getMessage(), e);
    	}
    	return false;
    }
    
    private static class ACM implements Serializable {
    	private static final long serialVersionUID = 1L;
    	private String iArea, iMajor, iClassification, iConcentration, iCampus, iMinor, iDegree, iProgram;
    	private double iWeight = 1.0;
    	
    	public ACM(String minor, String campus) {
    		iMinor = minor; iCampus = campus;
    	}
		public ACM(String area, String classification, String major, String campus, String concentration, String degree, String program, double weight) {
    		iArea = area; iMajor = major; iClassification = classification; iCampus = campus;
    		iConcentration = concentration; iDegree = degree; iProgram = program; iWeight = weight;
    	}
    	
    	public String getArea() { return iArea; }
    	public String getMajor() { return iMajor; }
    	public String getMinor() { return iMinor; }
    	public String getCampus() { return iCampus; }
    	public boolean hasCampus() { return iCampus != null && !iCampus.isEmpty(); }
    	public String getClassification() { return iClassification; }
    	public String getConcentration() { return iConcentration; }
    	public boolean hasConcentration() { return iConcentration != null && !iConcentration.isEmpty(); }
    	public String getDegree() { return iDegree; }
    	public boolean hasDegree() { return iDegree != null && !iDegree.isEmpty(); }
    	public String getProgram() { return iProgram; }
    	public boolean hasProgram() { return iProgram != null && !iProgram.isEmpty(); }
    	public double getWeight() { return iWeight; }
    	public void setWeight(double weight) { iWeight = weight; }
    	
    	public void update(ACM acm) {
    		if (!hasConcentration() && acm.hasConcentration()) iConcentration = acm.getConcentration();
    		iWeight = Math.min(1.0, iWeight + acm.getWeight());
    	}
    	
    	public boolean sameArea(AcademicArea area) {
    		return getArea().equalsIgnoreCase(area.getExternalUniqueId()) || getArea().equalsIgnoreCase(area.getAcademicAreaAbbreviation());
    	}
    	
    	public boolean sameClassification(AcademicClassification clasf) {
    		return getClassification().equalsIgnoreCase(clasf.getExternalUniqueId()) || getClassification().equalsIgnoreCase(clasf.getCode());
    	}
    	
    	public boolean sameMajor(PosMajor major) {
    		return getMajor().equalsIgnoreCase(major.getExternalUniqueId()) || getMajor().equalsIgnoreCase(major.getCode());
    	}
    	
    	public boolean sameMinor(PosMinor minor) {
    		return getMinor().equalsIgnoreCase(minor.getExternalUniqueId()) || getMinor().equalsIgnoreCase(minor.getCode());
    	}
    	
    	public boolean sameConcentration(PosMajorConcentration conc) {
    		if (hasConcentration())
    			return conc != null && (getConcentration().equalsIgnoreCase(conc.getExternalUniqueId()) || getConcentration().equalsIgnoreCase(conc.getCode()));
    		else
    			return conc == null;
    	}
    	
    	public boolean sameDegree(Degree deg) {
    		if (hasDegree())
    			return deg != null && (getDegree().equalsIgnoreCase(deg.getExternalUniqueId()) || getDegree().equalsIgnoreCase(deg.getReference()));
    		else
    			return deg == null;
    	}
    	
    	public boolean sameProgram(Program prog) {
    		if (hasProgram())
    			return prog != null && (getProgram().equalsIgnoreCase(prog.getExternalUniqueId()) || getProgram().equalsIgnoreCase(prog.getReference()));
    		else
    			return prog == null;
    	}
    	
    	public boolean sameCampus(Campus camp) {
    		if (hasCampus())
    			return camp != null && (getCampus().equalsIgnoreCase(camp.getExternalUniqueId()) || getCampus().equalsIgnoreCase(camp.getReference()));
    		else
    			return camp == null;
    	}
    	
    	public boolean sameWeight(Double weight) {
    		if (weight == null)
    			return getWeight() == 1.0;
    		else
    			return getWeight() == weight;
    	}
    	
    	public boolean matches(StudentAreaClassificationMajor aac) {
    		return sameArea(aac.getAcademicArea()) && sameClassification(aac.getAcademicClassification()) && sameMajor(aac.getMajor()) && sameConcentration(aac.getConcentration());
    	}
    	
    	public boolean matches(StudentAreaClassificationMinor aac) {
    		return sameMinor(aac.getMinor());
    	}
    	
    	@Override
    	public String toString() {
    		if (getMinor() != null) return getMinor();
    		return getArea() + "/" + getMajor() + (hasConcentration() ? "-" + getConcentration() : "") + " " + getClassification();
    	}
    	
    	@Override
    	public int hashCode() {
    		return toString().hashCode();
    	}
    	
    	@Override
    	public boolean equals(Object o) {
    		if (o == null || !(o instanceof ACM)) return false;
    		ACM acm = (ACM)o;
    		return equals(getArea(), acm.getArea())
    				&& equals(getMajor(), acm.getMajor())
    				&& equals(getMinor(), acm.getMinor())
    				&& equals(getClassification(), acm.getClassification())
    				&& equals(getConcentration(), acm.getConcentration());
    	}
    	
    	public static boolean equals(Object o1, Object o2) {
    		return (o1 == null ? o2 == null : o1.equals(o2));
    	}
    }
    
    protected boolean isOfferingCheckNeeded(XCourseRequest cr, XCourseId c) {
    	if ("pending-only".equals(ApplicationProperties.getProperty("banner.checkOffering.condition"))) {
    		for (String[] override: iOverrides) {
    			String subject = override[1], course = override[2];
    			if (c.getCourseName().startsWith(subject + " " + course)) {
					XOverride o = cr.getOverride(c);
		    		if (o != null && o.getStatus() != null && o.getStatus() == CourseRequestOverrideStatus.PENDING.ordinal())
		    			return true;
				}
        	}
    	} else {
			XOverride o = cr.getOverride(c);
			if (o == null || o.getStatus() == null) {
				// no override needed -> go ahead and check
				return true;
			} else if (o.getStatus() == CourseRequestOverrideStatus.APPROVED.ordinal()) {
				// override approved -> go ahead and check
				return true;
			} else if (o.getStatus() == CourseRequestOverrideStatus.PENDING.ordinal()) {
				// override pending -> check that an override was granted
				for (String[] override: iOverrides) {
	    			String subject = override[1], course = override[2];
	    			if (c.getCourseName().startsWith(subject + " " + course)) {
	    				// has a matching override and a pending override status
	    				return true;
	    			}
				}
			}
		}
    	return false;
    }
    
    public static class ValueLock<T> {
    	private ReentrantLock lock = new ReentrantLock();
    	private Map<T, Condition> conditions  = new HashMap<T, Condition>();

        public void lock(T t){
            lock.lock();
            try {
                while (conditions.containsKey(t)){
                    conditions.get(t).awaitUninterruptibly();
                }
                conditions.put(t, lock.newCondition());
            } finally {
                lock.unlock();
            }
        }

        public void unlock(T t){
            lock.lock();
            try {
                Condition condition = conditions.get(t);
                if (condition == null)
                    throw new IllegalStateException();// possibly an attempt to release what wasn't acquired
                conditions.remove(t);
                condition.signalAll();
            } finally {
                lock.unlock();
            }
        }
    }
}
