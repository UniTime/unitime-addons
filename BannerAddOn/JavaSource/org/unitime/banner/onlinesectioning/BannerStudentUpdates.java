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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.hibernate.CacheMode;
import org.hibernate.Transaction;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.BannerSession.FutureSessionUpdateMode;
import org.unitime.banner.model.Queue;
import org.unitime.banner.model.QueueIn;
import org.unitime.banner.model.QueueOut;
import org.unitime.banner.model.dao.QueueInDAO;
import org.unitime.banner.onlinesectioning.BannerUpdateStudentAction.Change;
import org.unitime.banner.onlinesectioning.BannerUpdateStudentAction.Status;
import org.unitime.banner.onlinesectioning.BannerUpdateStudentAction.UpdateResult;
import org.unitime.timetable.ApplicationProperties;
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
	
	protected XmlMessage getMessage() throws Exception {
		org.hibernate.Session hibSession = QueueInDAO.getInstance().createNewSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			QueueIn qi = (QueueIn)hibSession.createQuery("from QueueIn where status = :status order by uniqueId")
					.setString("status", QueueIn.STATUS_READY)
					.setMaxResults(1)
					.uniqueResult();
			
			XmlMessage ret = null;
			
			if (qi != null) {
				qi.setStatus(Queue.STATUS_PROCESSING);
				hibSession.update(qi);
				ret = new XmlMessage(qi.getUniqueId(), qi.getPostDate(), qi.getXml());
			}

			tx.commit();
			
			return ret;
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			hibSession.close();
		}
	}
	
	protected void updateMessage(XmlMessage message, String status) throws Exception {
		org.hibernate.Session hibSession = QueueInDAO.getInstance().createNewSession();
		Transaction tx = hibSession.beginTransaction();
		try {
			QueueIn qi = QueueInDAO.getInstance().get(message.getQueueId());
			
			if (qi != null) {
				qi.setStatus(status);
				qi.setProcessDate(new Date());
				hibSession.update(qi);
			}
			
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
			throw e;
		} finally {
			hibSession.close();
		}
	}
	
	public void pollMessage() {
		try {
			while (true) {
				XmlMessage message = getMessage();
				
				if (message == null) break;
				info("Processing message #" + message.getQueueId() + " posted at " + message.getCreated());
				
				try {
					processMessage(message.getContent().getRootElement());
					info("Message #" + message.getQueueId() + " processed.");
					updateMessage(message, Queue.STATUS_PROCESSED);
				} catch (Exception e) {
					updateMessage(message, Queue.STATUS_FAILED);
					error("Failed to process message #" + message.getQueueId() +": " + e.getMessage(), e);
				} finally {
					_RootDAO.closeCurrentThreadSessions();
				}
			}
		} catch (Exception ex) {
			error("Failed to process messages: " + ex.getMessage(), ex);
		}

	}
	
	public void processMessage(Element message) {
		new MessageProcessor(message).process();
	}
	
	protected BannerUpdateStudentAction generateUpdateAction(Element studentElement, String externalId, String bannerSession, boolean trimLeadingZerosFromExternalId) {
		BannerUpdateStudentAction update = new BannerUpdateStudentAction();
		update.forStudent(externalId, bannerSession);
		update.withName(studentElement.attributeValue("firstName"), studentElement.attributeValue("middleName"), studentElement.attributeValue("lastName"));
		update.withEmail(studentElement.attributeValue("email"));
		
		Element studentMajorsElement = studentElement.element("studentMajors");
		Element studentAcadAreaClassElement = studentElement.element("studentAcadAreaClass");
		if (studentMajorsElement != null) {
			// Student XML format
			update.updateAcadAreaClassificationMajors(true);
			String firstClassification = null;
			if (studentAcadAreaClassElement != null) {
				for (Iterator<?> k = studentAcadAreaClassElement.elementIterator("acadAreaClass"); k.hasNext(); ) {
					Element areaClassElement = (Element)k.next();
					firstClassification = areaClassElement.attributeValue("academicClass");
					break;
				}
			}
			for (Iterator<?> j = studentMajorsElement.elementIterator("major"); j.hasNext(); ) {
				Element majorElement = (Element)j.next();
				List<String> concentrations = new ArrayList<String>();
				for (Iterator<?> k = majorElement.elementIterator("concentration"); k.hasNext(); ) {
					Element concentrationElement = (Element)k.next();
					concentrations.add(concentrationElement.getText());
				}
				String classification = majorElement.attributeValue("academicClass");
				if (classification == null && studentAcadAreaClassElement != null) {
					classification = firstClassification;
					for (Iterator<?> k = studentAcadAreaClassElement.elementIterator("acadAreaClass"); k.hasNext(); ) {
						Element areaClassElement = (Element)k.next();
						if (majorElement.attributeValue("academicArea").equals(areaClassElement.attributeValue("academicArea"))) {
							classification = areaClassElement.attributeValue("academicClass");
							break;
						}
					}
				}
				if (concentrations.isEmpty()) {
					update.withAcadAreaClassificationMajor(
							majorElement.attributeValue("academicArea"),
							classification,
							majorElement.attributeValue("code"),
							majorElement.attributeValue("campus"),
							null,
							"Y".equalsIgnoreCase(majorElement.attributeValue("primary")) ? 1.0 : 0.0);
				} else {
					int idx = 0;
					for (String concentration: concentrations) {
						update.withAcadAreaClassificationMajor(
								majorElement.attributeValue("academicArea"),
								classification,
								majorElement.attributeValue("code"),
								majorElement.attributeValue("campus"),
								concentration,
								idx > 0 ? 0.0 : "Y".equalsIgnoreCase(majorElement.attributeValue("primary")) ? 1.0 : 0.0);
						idx++;
					}
				}
			}
		} else {
			// Old banner update message format
			update.withAcadAreaClassificationMajor(
					studentElement.attributeValue("academicArea"),
					studentElement.attributeValue("classification"),
					studentElement.attributeValue("major"),
					studentElement.attributeValue("campus"),
					studentElement.attributeValue("concentration"),
					1.0);
		}
		Element studentMinorsElement = studentElement.element("studentMinors");
		if (studentMinorsElement != null) {
			// Student XML format
			update.updateAcadAreaClassificationMinors(true);
			for (Iterator<?> j = studentMinorsElement.elementIterator("minor"); j.hasNext(); ) {
				Element minorElement = (Element)j.next();
				update.withAcadAreaClassificationMinor(
						minorElement.attributeValue("code"),
						minorElement.attributeValue("campus"));
			}
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
				error("[" + externalId + "] An integer value is required for a crn element (" + crnElement.getTextTrim() + ").");
			}
		}
		// Student enrollment XML format
		for (Iterator<?> j = studentElement.elementIterator("class"); j.hasNext(); ) {
			Element classElement = (Element)j.next();
			try {
				update.withCRN(Integer.valueOf(classElement.attributeValue("externalId")));
			} catch (Exception e) {
				error("[" + externalId + "] An integer value is required as external id of a class element (" + classElement.attributeValue("externalId") + ").");
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
		
		// Student advisors
		for (Iterator<?> j = studentElement.elementIterator("advisor"); j.hasNext(); ) {
			Element advisorElement = (Element)j.next();
			String advisorExternalId = advisorElement.attributeValue("advisorId");
			if (advisorExternalId == null) {
				error("[" + externalId + "] No externalId was given for an advisor.");
				continue;
			}
			while (trimLeadingZerosFromExternalId && advisorExternalId.startsWith("0")) advisorExternalId = advisorExternalId.substring(1);
			update.withAdvisor(advisorExternalId, advisorElement.attributeValue("advisorType"));
		}
		
		// Sports
		for (Iterator<?> j = studentElement.elementIterator("sport"); j.hasNext(); ) {
			Element sportElement = (Element)j.next();
			update.withGroup(
					sportElement.attributeValue("externalId", sportElement.attributeValue("group")),
					sportElement.attributeValue("campus"),
					sportElement.attributeValue("group"),
					sportElement.attributeValue("groupname", sportElement.attributeValue("group")),
					"SPORT");
		}
		
		// Cohorts
		for (Iterator<?> j = studentElement.elementIterator("cohort"); j.hasNext(); ) {
			Element sportElement = (Element)j.next();
			update.withGroup(
					sportElement.attributeValue("externalId", sportElement.attributeValue("group")),
					sportElement.attributeValue("campus"),
					sportElement.attributeValue("group"),
					sportElement.attributeValue("groupname", sportElement.attributeValue("group")),
					"COHORT");
		}
		
		// Campus code(s)
		for (Iterator<?> j = studentElement.elementIterator("campus"); j.hasNext(); ) {
			Element campusElement = (Element)j.next();
			update.withCampus(campusElement.getTextTrim());
		}
		
		return update;
	}
	
	protected class MessageProcessor {
		long iMaxElementTime = 0, iMinElementTime = 0;
		int iElementCount = 0;
		boolean iTrimLeadingZerosFromExternalId = ApplicationProperty.DataExchangeTrimLeadingZerosFromExternalIds.isTrue();
		int iNrThreads;
		Iterator<?> iStudentElementIterator;
		int iStudentElementCount = 0;
		
		Set<String> iFailedStudents = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		Set<String> iProblemStudents = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		Set<String> iUpdatedStudents = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		Set<String> iFailedFutureStudents = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		Set<String> iProblemFutureStudents = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		Set<String> iUpdatedFutureStudents = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());
		
		Set<UpdateRequest> iUpdateRequests = Collections.newSetFromMap(new ConcurrentHashMap<UpdateRequest, Boolean>());
		Map<Long, Set<Long>> iSession2studentIds = new HashMap<Long, Set<Long>>();
		
		MessageProcessor(Element rootElement) {
			for (Iterator<?> i = rootElement.elementIterator("student"); i.hasNext(); ) {
				iStudentElementCount ++;
				i.next();
			}
			iNrThreads = Math.min(
					Integer.parseInt(ApplicationProperties.getProperty("banner.studentUpdates.nrThreads", "1")),
					1 + iStudentElementCount / 10);
			iStudentElementIterator = rootElement.elementIterator("student");
		}
		
		public void process() {
			org.hibernate.Session hibSession = QueueInDAO.getInstance().createNewSession();
			try {
				long start = System.currentTimeMillis();
				if (iNrThreads <= 1 || iStudentElementCount < 10) {
					Worker worker = new Worker();
					while (iStudentElementIterator.hasNext()) {
						Element studentElement = (Element) iStudentElementIterator.next();
						worker.processStudent(studentElement, hibSession, false);
					}
				} else {
					List<Thread> workers = new ArrayList<Thread>();
					for (int i = 0; i < iNrThreads; i++) {
						Thread t = new Thread(new Worker());
						t.setName("BannerStudentUpdates-" + (1 + i));
						workers.add(t);
					}
					for (Thread worker: workers) worker.start();
					for (Thread worker: workers) {
						try {
							worker.join();
						} catch (InterruptedException e) {}
					}
				}
				long end = System.currentTimeMillis();
				info(iUpdatedStudents.size() + " student records updated in " + (end - start)+ " milliseconds (" + iNrThreads + " threads used).");
				info(iFailedStudents.size() + " student records failed to update.");
				info(iProblemStudents.size() + " student records were updated, but had problems.");
				if (iElementCount > 0) {
					info("Minimum milliseconds required to process a record = " + iMinElementTime);
					info("Maximum milliseconds required to process a record = " + iMaxElementTime);
					info("Average milliseconds required to process a record = " + ((end - start)/iElementCount));
				}
				if (!iUpdatedFutureStudents.isEmpty())
					info(iUpdatedFutureStudents.size() + " future student records updated.");
				if (!iFailedFutureStudents.isEmpty())
					info(iFailedFutureStudents.size() + " future student records failed to update.");
				if (!iProblemFutureStudents.isEmpty())
					info(iProblemFutureStudents.size() + " future student records were updated, but had problems.");
				if (!iFailedStudents.isEmpty() || !iFailedFutureStudents.isEmpty()) {
					error("The following student ids were not successfully processed:  ");
					for(String studentId : iFailedStudents) error("\t" + studentId);
					for(String studentId : iFailedFutureStudents) error("\t" + studentId);
				}
				if (!iProblemStudents.isEmpty() || !iProblemFutureStudents.isEmpty()){
					error("The following student ids were successfully processed, but may have had problems finding all classes the student was enrolled in:  ");
					for(String studentId : iProblemStudents) error("\t" + studentId);
					for(String studentId : iProblemFutureStudents) error("\t" + studentId);
				}
				for (Map.Entry<Long, Set<Long>> entry: iSession2studentIds.entrySet()) {
					StudentSectioningQueue.studentChanged(hibSession, null, entry.getKey(), entry.getValue());
				}
				if (!iUpdateRequests.isEmpty()) {
					Document document = DocumentHelper.createDocument();
					Element root = document.addElement("studentUpdateRequest");
					for (UpdateRequest student: iUpdateRequests) {
						Element studentEl = root.addElement("student");
						studentEl.addAttribute("externalId", getBannerId(student));
						studentEl.addAttribute("session", student.getBannerTerm());
					}
					QueueOut out = new QueueOut();
					out.setXml(document);
					out.setStatus(QueueOut.STATUS_READY);
					out.setPostDate(new Date());
					hibSession.save(out);
					info("Future session updates for " + iUpdateRequests.size() + " students requested.");
				}
				hibSession.flush();
			} finally {
				hibSession.close();
			}
		}
		
		protected class Worker implements Runnable {
			Map<String, List<BannerSession>> iSession2ids = new HashMap<String, List<BannerSession>>();
			
			public Worker() {}
			
			@Override
		    public void run() {
				org.hibernate.Session hibSession = QueueInDAO.getInstance().createNewSession();
				try {
					while (true) {
						Element studentElement = null;
						synchronized (iStudentElementIterator) {
							if (!iStudentElementIterator.hasNext()) break;
							studentElement = (Element)iStudentElementIterator.next();
						}
						processStudent(studentElement, hibSession, true);
					}
				} finally {
					hibSession.close();
					_RootDAO.closeCurrentThreadSessions();
				}
			}
			
			protected List<BannerSession> getBannerSessions(String bannerSession, org.hibernate.Session hibSession) {
				List<BannerSession> sessionIds = iSession2ids.get(bannerSession);
				if (sessionIds == null) {
					sessionIds = (List<BannerSession>)hibSession.createQuery(
							"select bs from BannerSession bs where bs.bannerTermCode = :termCode")
							.setString("termCode", bannerSession).list();
					iSession2ids.put(bannerSession, sessionIds);
				}
				return sessionIds;
			}
			
			void processStudent(Element studentElement, org.hibernate.Session hibSession, boolean locking) {
				long t0 = System.currentTimeMillis();
				try {
					String externalId = studentElement.attributeValue("externalId");
					if (externalId == null) {
						error("No externalId was given for a student.");
						return;
					}
					while (iTrimLeadingZerosFromExternalId && externalId.startsWith("0")) externalId = externalId.substring(1);
					
					String bannerSession = studentElement.attributeValue("session");
					if (bannerSession == null) {
						error("[" + externalId + "] No session was given for a student.");
						return;
					}
					List<BannerSession> sessionIds = getBannerSessions(bannerSession, hibSession);
					
					BannerUpdateStudentAction update = generateUpdateAction(studentElement, externalId, bannerSession, iTrimLeadingZerosFromExternalId);
					if (update == null) return;
					if (locking) update.withLocking();
					
					for (BannerSession bs: sessionIds) {
						if (!update.isApplicable(bs)) {
							debug("[" + externalId + "] Skipping campus " + bs.getBannerCampus());
							continue;
						}
						Long sessionId = bs.getSession().getUniqueId();
						UpdateResult result = null;
						try {
							OnlineSectioningServer server = (iContainer == null ? null : iContainer.getSolver(sessionId.toString()));
							if (server != null && server.isReady()) {
								try {
									result = server.execute(update, user());
								} finally {
									_RootDAO.closeCurrentThreadSessions();
								}
							} else {
								OnlineSectioningHelper h = new OnlineSectioningHelper(QueueInDAO.getInstance().createNewSession(), user(), CacheMode.REFRESH);
								try {
									h.addMessageHandler(BannerStudentUpdates.this);
									result = update.execute(sessionId, h);
									notifyStudentChanged(sessionId, result);
								} finally {
									h.getHibSession().close();
								}
							}
							if (result != null) {
								switch (result.getStatus()) {
								case OK:
									iUpdatedStudents.add(externalId);
									break;
								case FAILURE:
									iFailedStudents.add(externalId);
									break;
								case PROBLEM:
									iProblemStudents.add(externalId);
									break;
								case NO_CHANGE:
								}
							}
						} catch (Exception e) {
							error("[" + externalId + "] Failed to update student: " + e.getMessage(), e);
							iFailedStudents.add(externalId);
						}
						// Update future terms as needed
						if (result != null && (result.getStatus() == Status.OK || result.getStatus() == Status.PROBLEM) &&
								result.has(Change.CREATED, Change.DEMOGRAPHICS, Change.GROUPS, Change.ADVISORS)) {
							if (bs.getFutureSessionUpdateMode() == FutureSessionUpdateMode.SEND_REQUEST && bs.getFutureSession() != null) {
								iUpdateRequests.add(new UpdateRequest(externalId, bs.getFutureSession().getBannerTermCode()));
							} else if (bs.getFutureSessionUpdateMode() == FutureSessionUpdateMode.DIRECT_UPDATE) {
								BannerSession future = bs.getFutureSession();
								while (future != null) {
									update.forStudent(externalId, future.getBannerTermCode()).skipClassUpdates();
									sessionId = future.getSession().getUniqueId();
									try {
										OnlineSectioningServer server = (iContainer == null ? null : iContainer.getSolver(sessionId.toString()));
										if (server != null && server.isReady()) {
											try {
												result = server.execute(update, user());
											} finally {
												_RootDAO.closeCurrentThreadSessions();
											}
										} else {
											OnlineSectioningHelper h = new OnlineSectioningHelper(QueueInDAO.getInstance().createNewSession(), user(), CacheMode.REFRESH);
											try {
												h.addMessageHandler(BannerStudentUpdates.this);
												result = update.execute(sessionId, h);
												notifyStudentChanged(sessionId, result);
											} finally {
												h.getHibSession().close();
											}
										}
										switch (result.getStatus()) {
										case OK:
											iUpdatedFutureStudents.add(externalId + " (" + future.getBannerTermCode() + ")");
											break;
										case FAILURE:
											iFailedFutureStudents.add(externalId + " (" + future.getBannerTermCode() + ")");
											break;
										case PROBLEM:
											iProblemFutureStudents.add(externalId + " (" + future.getBannerTermCode() + ")");
											break;
										case NO_CHANGE:
										}
									} catch (Exception e) {
										error("[" + externalId + "] Failed to update future student: " + e.getMessage(), e);
										iFailedFutureStudents.add(externalId + " (" + future.getBannerTermCode() + ")");
										break;
									}
										
									if (result.getStatus() != Status.OK && result.getStatus() != Status.PROBLEM) break;
									if (!result.has(Change.CREATED, Change.DEMOGRAPHICS, Change.GROUPS, Change.ADVISORS)) break;
									if (future.getFutureSessionUpdateMode() != FutureSessionUpdateMode.DIRECT_UPDATE) {
										if (future.getFutureSessionUpdateMode() == FutureSessionUpdateMode.DIRECT_UPDATE)
											iUpdateRequests.add(new UpdateRequest(externalId, future.getFutureSession().getBannerTermCode()));	
										break;
									}
									future = future.getFutureSession();
								}
							}
						}
					}
				} finally {
					updateElementCount(t0);
				}
			}
		}
		
		protected void notifyStudentChanged(Long sessionId, UpdateResult result) {
			if (result != null && result.getStudentId() != null && (result.getStatus() == Status.OK || result.getStatus() == Status.PROBLEM)) {
				synchronized (iSession2studentIds) {
					Set<Long> ids = iSession2studentIds.get(sessionId);
					if (ids == null) {
						ids = new HashSet<Long>();
						iSession2studentIds.put(sessionId, ids);
					}
					ids.add(result.getStudentId());	
				}
			}
		}
		
		protected void updateElementCount(long t0) {
			long time = (System.currentTimeMillis() - t0);
			synchronized (iStudentElementIterator) {
				if (iElementCount == 0 || time < iMinElementTime) {
					iMinElementTime = time;
				}
				if (iElementCount == 0 || time > iMaxElementTime) {
					iMaxElementTime = time;
				}
				iElementCount ++;
			}
		}
	}

	protected String getBannerId(UpdateRequest student) {
		String id = student.getExternalId();
		while (id.length() < 9) id = "0" + id;
		return id;
	}
	
	protected OnlineSectioningLog.Entity user() {
		return OnlineSectioningLog.Entity.newBuilder()
			.setExternalId(StudentClassEnrollment.SystemChange.SYSTEM.name())
			.setName(StudentClassEnrollment.SystemChange.SYSTEM.getName())
			.setType(OnlineSectioningLog.Entity.EntityType.OTHER).build();
	}

	@Override
	public void loadXml(Element rootElement) throws Exception {
		try {
			processMessage(rootElement);
			new MessageProcessor(rootElement).process();
		} finally {
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
	
	protected static class XmlMessage {
		private Long iQueueId;
		private Date iCreated;
		private Document iContent;
		
		XmlMessage(Long queueId, Date created, Document content) {
			iQueueId = queueId;
			iCreated = created;
			iContent = content;
		}
		
		public Long getQueueId() { return iQueueId; }
		public Date getCreated() { return iCreated; }
		public Document getContent() { return iContent; }
	}
	
	protected static class UpdateRequest {
		private String iBannerTerm;
		private String iExternalId;
		
		UpdateRequest(String externalId, String bannerTerm) {
			iExternalId = externalId;
			iBannerTerm = bannerTerm;
		}
		
		public String getExternalId() { return iExternalId; }
		public String getBannerTerm() { return iBannerTerm; }
		@Override
		public String toString() { return getBannerTerm() + ":" + getExternalId(); }
		@Override
		public int hashCode() { return toString().hashCode(); }
		@Override
		public boolean equals(Object o) {
			if (o == null || !(o instanceof UpdateRequest)) return false;
			return getExternalId().equals(((UpdateRequest)o).getExternalId()) && getBannerTerm().equals(((UpdateRequest)o).getBannerTerm());
		}
	}

}
