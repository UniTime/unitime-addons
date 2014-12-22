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
