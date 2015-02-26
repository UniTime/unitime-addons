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

import java.util.Collection;
import java.util.Date;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.unitime.banner.model.QueueOut;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.OnlineSectioningHelper;
import org.unitime.timetable.onlinesectioning.OnlineSectioningServer;
import org.unitime.timetable.onlinesectioning.custom.purdue.XEInterface;
import org.unitime.timetable.onlinesectioning.custom.purdue.XEStudentEnrollment;
import org.unitime.timetable.onlinesectioning.model.XStudent;

/**
 * @author Tomas Muller
 */
public class BannerXEStudentEnrollment extends XEStudentEnrollment {
	
	@Override
	public boolean isCanRequestUpdates() {
		return "true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.studentUpdateRequests.enabled", "false"));
	}

	@Override
	public boolean requestUpdate(OnlineSectioningServer server, OnlineSectioningHelper helper, Collection<XStudent> students) throws SectioningException {
		if (students == null || students.isEmpty()) return false;
		if (!isCanRequestUpdates()) return false;
		Document document = DocumentHelper.createDocument();
		Element root = document.addElement("studentUpdateRequest");
		String term = getBannerTerm(server.getAcademicSession());
		for (XStudent student: students) {
			Element studentEl = root.addElement("student");
			studentEl.addAttribute("externalId", getBannerId(student));
			studentEl.addAttribute("session", term);
		}
		QueueOut out = new QueueOut();
		out.setXml(document);
		out.setStatus(QueueOut.STATUS_READY);
		out.setPostDate(new Date());
		helper.getHibSession().save(out);
		helper.getHibSession().flush();
		return true;
	}
	
	@Override
	protected boolean eligibilityIgnoreBannerRegistration(OnlineSectioningServer server, OnlineSectioningHelper helper, XStudent student, XEInterface.Registration reg) {
		// ignore sections that do not exist in UniTime (and of matching campus)
		// this is to fix synchronization issues when a class is cancelled
		// (it does not exist in UniTime, but still contains enrolled students in Banner)
		Number count = (Number)helper.getHibSession().createQuery(
				"select count(bs) from BannerSession s, BannerSection bs where " +
				"bs.session = s.session and s.bannerTermCode = :term and bs.crn = :crn")
				.setString("term", reg.term)
				.setString("crn", reg.courseReferenceNumber)
				.uniqueResult();
		return count.intValue() == 0 && getBannerCampus(server.getAcademicSession()).equals(reg.campus);
	}

}
