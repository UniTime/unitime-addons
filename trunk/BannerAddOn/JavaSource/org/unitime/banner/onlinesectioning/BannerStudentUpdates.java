/*
 * UniTime 3.5 (University Timetabling Application)
 * Copyright (C) 2014, UniTime LLC, and individual contributors
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
package org.unitime.banner.onlinesectioning;

import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.dom4j.Element;
import org.hibernate.CacheMode;
import org.unitime.banner.model.Queue;
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.onlinesectioning.BannerUpdateStudentAction.UpdateResult;
import org.unitime.timetable.dataexchange.BaseImport;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.model.StudentClassEnrollment;
import org.unitime.timetable.model.StudentSectioningQueue;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper.Message;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper.MessageHandler;
import org.unitime.timetable.onlinesectioning.OnlineSectioningLog;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.solver.jgroups.SolverContainer;
import org.unitime.timetable.solver.service.SolverServerService;
import org.unitime.timetable.spring.SpringApplicationContextHolder;

/**
 * @author Tomas Muller
 */
public class BannerStudentUpdates extends BaseImport implements MessageHandler {
	private SolverContainer<OnlineSectioningServer> iContainer;

	public BannerStudentUpdates(SolverContainer<OnlineSectioningServer> container) {
		super();
		iContainer = container;
	}
	
	public BannerStudentUpdates() {
		super();
		if (SpringApplicationContextHolder.isInitialized())
			iContainer = ((SolverServerService)SpringApplicationContextHolder.getBean("solverServerService")).getOnlineStudentSchedulingContainer();
	}
	
	public void pollMessage() {
		try {
			while (true) {
				org.hibernate.Session hibSession = QueueInDAO.getInstance().createNewSession();
				try {
					QueueIn qi = (QueueIn)hibSession.createQuery("from QueueIn where status = :status order by uniqueId")
							.setString("status", QueueIn.STATUS_READY)
							.setMaxResults(1)
							.uniqueResult();
					if (qi == null) break;
					
					info("Processing message posted at " + qi.getPostDate());
					try {
						processMessage(hibSession, qi.getXml().getRootElement());
						info("Message processed.");
						qi.setStatus(Queue.STATUS_PROCESSED);
					} catch (Exception e) {
						qi.setStatus(Queue.STATUS_FAILED);
						error("Failed to process a message: " + e.getMessage(), e);
					}
					qi.setProcessDate(new Date());
					hibSession.update(qi);
					hibSession.flush();
				} finally {
					hibSession.close();
					_RootDAO.closeCurrentThreadSessions();
				}
			}
		} catch (Exception ex) {
			error("Failed to process messages: " + ex.getMessage(), ex);
		}

	}
	
	@SuppressWarnings("unchecked")
	public void processMessage(org.hibernate.Session hibSession, Element rootElement) {
		if (!"studentUpdates".equalsIgnoreCase(rootElement.getName())) return;
		long maxElementTime = 0, minElementTime = 0;
		int elementCount = 0;
		boolean trimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();
		Map<String, List<Long>> session2ids = new HashMap<String, List<Long>>();
		Set<String> failedStudents = new HashSet<String>();
		Set<String> problemStudents = new HashSet<String>();
		Set<String> updatedStudents = new HashSet<String>();
		Map<Long, Set<Long>> session2studentIds = new HashMap<Long, Set<Long>>();
		long start = System.currentTimeMillis();
		for (Iterator<?> i = rootElement.elementIterator("student"); i.hasNext();) {
			Element studentElement = (Element) i.next();
			long t0 = System.currentTimeMillis();
			try {
				String externalId = studentElement.attributeValue("externalId");
				if (externalId == null) {
					error("No externalId was given for a student.");
					continue;
				}
				while (trimLeadingZerosFromExternalId && externalId.startsWith("0")) externalId = externalId.substring(1);
				
				String bannerSession = studentElement.attributeValue("session");
				if (bannerSession == null) {
					error("No session was given for a student.");
					continue;
				}
				List<Long> sessionIds = session2ids.get(bannerSession);
				if (sessionIds == null) {
					sessionIds = (List<Long>)hibSession.createQuery(
							"select bs.session.uniqueId from BannerSession bs where bs.bannerTermCode = :termCode")
							.setString("termCode", bannerSession).list();
					session2ids.put(bannerSession, sessionIds);
				}
				
				BannerUpdateStudentAction update = new BannerUpdateStudentAction();
				update.forStudent(externalId, bannerSession);
				update.withName(studentElement.attributeValue("firstName"), studentElement.attributeValue("middleName"), studentElement.attributeValue("lastName"));
				update.withEmail(studentElement.attributeValue("email"));
				
				Element studentAcadAreaClassElement = studentElement.element("studentAcadAreaClass");
				if (studentAcadAreaClassElement != null) {
					// Student XML format
					update.updateAcadAreaClassifications(true);
					for (Iterator<?> j = studentAcadAreaClassElement.elementIterator("acadAreaClass"); j.hasNext(); ) {
						Element areaClassElement = (Element)j.next();
						update.withAcadAreaClassification(areaClassElement.attributeValue("academicArea"), areaClassElement.attributeValue("academicClass"));
					}					
				} else {
					// Old banner update message format
					update.withAcadAreaClassification(studentElement.attributeValue("academicArea"), studentElement.attributeValue("classification"));
				}
				
				Element studentMajorsElement = studentElement.element("studentMajors");
				if (studentMajorsElement != null) {
					// Student XML format
					update.updateAcadAreaMajors(true);
					for (Iterator<?> j = studentMajorsElement.elementIterator("major"); j.hasNext(); ) {
						Element majorElement = (Element)j.next();
						update.withAcadAreaMajor(majorElement.attributeValue("academicArea"), majorElement.attributeValue("code"));
					}					
				} else {
					// Old banner update message format
					update.withAcadAreaMajor(studentElement.attributeValue("academicArea"), studentElement.attributeValue("major"));
				}
				
				Element studentGroupsElement = studentElement.element("studentGroups");
				if (studentGroupsElement != null) {
					// Student XML format
					for (Iterator<?> j = studentGroupsElement.elementIterator("studentGroup"); j.hasNext(); ) {
						Element studentGroupElement = (Element)j.next();
						update.withGroup(
								studentGroupElement.attributeValue("externalId", studentGroupElement.attributeValue("group")),
								studentGroupElement.attributeValue("campus"),
								studentGroupElement.attributeValue("abbreviation", studentGroupElement.attributeValue("group")),
								studentGroupElement.attributeValue("name")); 
					}
				} else {
					// Old banner upadte message format
					for (Iterator<?> j = studentElement.elementIterator("studentGroup"); j.hasNext(); ) {
						Element studentGroupElement = (Element)j.next();
						update.withGroup(studentGroupElement.attributeValue("externalId"), studentGroupElement.attributeValue("campus"), studentGroupElement.attributeValue("abbreviation"), studentGroupElement.attributeValue("name")); 
					}
				}
				
				// Old banner update message format
				for (Iterator<?> j = studentElement.elementIterator("crn"); j.hasNext(); ) {
					Element crnElement = (Element)j.next();
					try {
						update.withCRN(Integer.valueOf(crnElement.getTextTrim()));
					} catch (Exception e) {
						error("An integer value is required for a crn element (student " + externalId + ").");
						problemStudents.add(externalId);
					}
				}
				// Student enrollment XML format
				for (Iterator<?> j = studentElement.elementIterator("class"); j.hasNext(); ) {
					Element classElement = (Element)j.next();
					try {
						update.withCRN(Integer.valueOf(classElement.attributeValue("externalId")));
					} catch (Exception e) {
						error("An integer value is required as external id of a class element (student " + externalId + ").");
						problemStudents.add(externalId);
					}
				}
				
				// Overrides
				for (Iterator<?> j = studentElement.elementIterator("override"); j.hasNext(); ) {
					Element overrideElement = (Element)j.next();
					update.withOverride(
							overrideElement.attributeValue("type"),
							overrideElement.attributeValue("subject"),
							overrideElement.attributeValue("course", overrideElement.attributeValue("courseNbr")),
							overrideElement.attributeValue("crn"));
				}
				
				for (Long sessionId: sessionIds) {
					try {
						OnlineSectioningServer server = (iContainer == null ? null : iContainer.getSolver(sessionId.toString()));
						if (server != null && server.isReady()) {
							switch (server.execute(update, user())) {
							case OK:
								updatedStudents.add(externalId);
								break;
							case FAILURE:
								failedStudents.add(externalId);
								break;
							case PROBLEM:
								problemStudents.add(externalId);
								break;
							case NO_CHANGE:
							}
						} else {
							OnlineSectioningHelper h = new OnlineSectioningHelper(QueueInDAO.getInstance().createNewSession(), user(), CacheMode.REFRESH);
							try {
								h.addMessageHandler(this);
								UpdateResult result = update.execute(sessionId, h);
								switch (result) {
								case OK:
									updatedStudents.add(externalId);
									break;
								case FAILURE:
									failedStudents.add(externalId);
									break;
								case PROBLEM:
									problemStudents.add(externalId);
									break;
								case NO_CHANGE:
								}
								if (update.getStudentId() != null && (result == UpdateResult.OK || result == UpdateResult.PROBLEM)) {
									Set<Long> ids = session2studentIds.get(sessionId);
									if (ids == null) {
										ids = new HashSet<Long>();
										session2studentIds.put(sessionId, ids);
									}
									ids.add(update.getStudentId());
								}
							} finally {
								h.getHibSession().close();
							}
						}
					} catch (Exception e) {
						error("Failed to update student " +externalId + ": " + e.getMessage());
						failedStudents.add(externalId);
					}
				}
				
			} finally {
				long time = (System.currentTimeMillis() - t0);
				if (elementCount == 0 || time < minElementTime) {
					minElementTime = time;
				}
				if (elementCount == 0 || time > maxElementTime) {
					maxElementTime = time;
				}
				elementCount ++;
			}
		}
		long end = System.currentTimeMillis();
		info(updatedStudents.size() + " student records updated in " + (end - start)+ " milliseconds.");
		info(failedStudents.size() + " student records failed to update.");
		info(problemStudents.size() + " student records were updated, but had problems.");
		if (elementCount > 0) {
			info("Minimum milliseconds required to process a record = " + minElementTime);
			info("Maximum milliseconds required to process a record = " + maxElementTime);
			info("Average milliseconds required to process a record = " + ((end - start)/elementCount));
		}
		if (!failedStudents.isEmpty()) {
			error("The following student ids were not successfully processed:  ");
			for(String studentId : failedStudents) error("\t" + studentId);
		}
		if (!problemStudents.isEmpty()){
			error("The following student ids were successfully processed, but may have had problems finding all classes the student was enrolled in:  ");
			for(String studentId : problemStudents) error("\t" + studentId);
		}
		for (Map.Entry<Long, Set<Long>> entry: session2studentIds.entrySet()) {
			StudentSectioningQueue.studentChanged(hibSession, null, entry.getKey(), entry.getValue());
		}
		hibSession.flush();
	}
	
	protected OnlineSectioningLog.Entity user() {
		return OnlineSectioningLog.Entity.newBuilder()
			.setExternalId(StudentClassEnrollment.SystemChange.SYSTEM.name())
			.setName(StudentClassEnrollment.SystemChange.SYSTEM.getName())
			.setType(OnlineSectioningLog.Entity.EntityType.OTHER).build();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		org.hibernate.Session hibSession = QueueInDAO.getInstance().createNewSession();
		try {
			processMessage(hibSession, rootElement);
		} finally {
			hibSession.close();
			_RootDAO.closeCurrentThreadSessions();
		}
	}

	@Override
	public void onMessage(Message message) {
		switch (message.getLevel()) {
		case DEBUG:
			if (isDebugEnabled())
				debug(message.getMessage(), message.getThrowable());
			break;
		case INFO:
			info(message.getMessage(), message.getThrowable());
			break;
		case WARN:
			warn(message.getMessage(), message.getThrowable());
			break;
		case ERROR:
			error(message.getMessage(), message.getThrowable());
			break;
		case FATAL:
			fatal(message.getMessage(), message.getThrowable());
			break;
		default:
			info(message.getMessage(), message.getThrowable());
		}
	}

	@Override
	public boolean isDebugEnabled() {
		return false;
	}

}
