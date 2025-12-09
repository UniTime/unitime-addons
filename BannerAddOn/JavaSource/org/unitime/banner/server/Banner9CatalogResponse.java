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
package org.unitime.banner.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.gwt.banner.BannerCatalogPage.CatalogRequest;
import org.unitime.timetable.gwt.banner.BannerCatalogPage.CatalogResponse;
import org.unitime.timetable.gwt.banner.BannerCatalogPage.CatalogSection;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.resources.StudentSectioningMessages;
import org.unitime.timetable.gwt.shared.SectioningException;
import org.unitime.timetable.onlinesectioning.AcademicSessionInfo;
import org.unitime.timetable.onlinesectioning.custom.CourseDetailsProvider;
import org.unitime.timetable.onlinesectioning.custom.CourseUrlProvider;
import org.unitime.timetable.onlinesectioning.custom.ExternalTermProvider;
import org.unitime.timetable.onlinesectioning.custom.purdue.BannerTermProvider;
import org.unitime.timetable.security.SessionContext;

@GwtRpcImplements(CatalogRequest.class)
public class Banner9CatalogResponse implements GwtRpcImplementation<CatalogRequest, CatalogResponse>, CourseDetailsProvider, CourseUrlProvider {
	private static final long serialVersionUID = 1L;
	private static Log sLog = LogFactory.getLog(Banner9CatalogResponse.class);
	private static StudentSectioningMessages MSG = Localization.create(StudentSectioningMessages.class);
	private transient ExternalTermProvider iExternalTermProvider = null;

	@Override
	public CatalogResponse execute(CatalogRequest request, SessionContext context) {
		try {
			String base = ApplicationProperties.getProperty("banner.catalog.url", "https://apps.university.edu/StudentRegistrationSsb/ssb/courseSearchResults");
			String params = "term=" + URLEncoder.encode(request.getTerm(), "utf-8") +
					"&subjectCode=" + URLEncoder.encode(request.getSubject(), "utf-8") +
					"&courseNumber=" + URLEncoder.encode(request.getCourseNbr(), "utf-8");
			
			CatalogResponse response = new CatalogResponse();
			String pageName = ApplicationProperties.getProperty("banner.catalog.pageName", null);
			if (pageName != null && !pageName.isEmpty())
				response.setPageLabel(pageName.replace("{0}", request.getSubject() + " " + request.getCourseNbr()));
			CatalogSection details = downloadSection("Catalog", base + "/getCourseCatalogDetails?" + params);
			if (details == null || details.getContent().contains("The requested URL was rejected. Please consult with your administrator.")) {
				response.addSection(new CatalogSection(request.getSubject() + " " + request.getCourseNbr(),
						"<span class='error'>" + ApplicationProperties.getProperty("banner.catalog.error", "Failed to load course details: The requested URL was rejected.") +
						"</span>"));
			} else {
				response.addSection(downloadSection(request.getSubject() + " " + request.getCourseNbr(), base + "/getCourseDescription?" + params,
						"No course description is available."));
				response.addSection(details);
				response.addSection(downloadSection("Syllabus", base + "/getSyllabus?" + params,
						"No Syllabus Information Available"));
				response.addSection(downloadSection("Attributes", base + "/getCourseAttributes?" + params,
						"No Attribute information available."));
				response.addSection(downloadSection("Restrictions", base + "/getRestrictions?" + params,
						"No course restriction information is available."));
				response.addSection(downloadSection("Corequisites", base + "/getCorequisites?" + params,
						"No corequisite course information available."));
				response.addSection(downloadSection("Prerequisites", base + "/getPrerequisites?" + params,
						"No prerequisite information available."));
				response.addSection(downloadSection("Mutual Exclusion", base + "/getCourseMutuallyExclusions?" + params,
						"No Mutual Exclusion information available."));
				response.addSection(downloadSection("Fees", base + "/getFees?" + params,
						"No fee information available."));
			}
			
			response.setDisclaimer(ApplicationProperties.getProperty("banner.catalog.disclaimer", null));
			
			return response;
		} catch (Exception e) {
			throw new GwtRpcException(e.getMessage(), e);
		}
	}
	
	protected CatalogSection downloadSection(String title, String url) {
		return downloadSection(title, url, null);
	}
	
	protected CatalogSection downloadSection(String title, String url, String emptyCheck) {
		try {
			BufferedReader in = null;
			try {
				in = new BufferedReader(new InputStreamReader(new URL(url).openStream(), "utf-8"));
				StringBuffer buffer = new StringBuffer();
				String line;
				while ((line = in.readLine()) != null) {
					if (!buffer.isEmpty()) buffer.append("\n");
					buffer.append(line);
				}
				in.close();
				String content = buffer.toString();
				if (emptyCheck != null && content.contains(emptyCheck))
					return null;
				if ("Syllabus".equals(title))
					content = content.replace(" <br/><br/>\n"
							+ "        \n"
							+ "        \n"
							+ "        <span class=\"status-bold\">URL:</span> <br/> <span><a\n"
							+ "                href=\"\" target=\"_blank\"></a>\n"
							+ "        </span> <br/><br/>\n"
							+ "    \n"
							+ "    ", "");
				return new CatalogSection(title, content);
			} finally {
				if (in != null) in.close();
			}
		} catch (IOException e) {
			sLog.error(e.getMessage(), e);
			return new CatalogSection(title, "Failed to read <a href='"+url+"'>" + title + "</a>: " + e.getMessage());
		}
	}
	
	public ExternalTermProvider getTermProvider() {
		if (iExternalTermProvider == null) {
			try {
				String clazz = ApplicationProperty.CustomizationExternalTerm.value();
				if (clazz == null || clazz.isEmpty())
					iExternalTermProvider = new BannerTermProvider();
				else
					iExternalTermProvider = (ExternalTermProvider)Class.forName(clazz).getConstructor().newInstance();
			} catch (Exception e) {
				sLog.error("Failed to create external term provider, using the default one instead.", e);
				iExternalTermProvider = new BannerTermProvider();
			}
		}
		return iExternalTermProvider;
	}

	@Override
	public String getDetails(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		try {
			CatalogRequest request = new CatalogRequest(
					getTermProvider().getExternalTerm(session),
					getTermProvider().getExternalSubject(session, subject, courseNbr),
					getTermProvider().getExternalCourseNumber(session, subject, courseNbr));
			CatalogResponse response = execute(request, null);
			StringBuffer ret = new StringBuffer("<table class='unitime-MainTable unitime-BannerCatalogPage' cellpadding='2' cellspacing='0'>");
			if (response.hasSections()) {
				for (CatalogSection section: response.getSections()) {
					ret.append("<tr class='unitime-MainTableHeaderRow'><td colspan='2' class='unitime-MainTableHeader'>");
					ret.append(section.getTitle());
					ret.append("</td></tr>");
					ret.append("<tr><td colspan='2'><div class='content'>");
					ret.append(section.getContent());
					ret.append("</div></td></tr>");
				}
			}
			ret.append("</table>");
			return ret.toString();
		} catch (Exception e) {
			throw new SectioningException(MSG.failedLoadCourseDetails(e.getMessage()), e);
		}			
	}

	@Override
	public URL getCourseUrl(AcademicSessionInfo session, String subject, String courseNbr) throws SectioningException {
		try {
			return new URL(ApplicationProperty.UniTimeUrl.value() + "/bannerCatalog?term=" + URLEncoder.encode(getTermProvider().getExternalTerm(session), "utf-8")
				+ "&subjectCode=" + URLEncoder.encode(getTermProvider().getExternalSubject(session, subject, courseNbr), "utf-8")
				+ "&courseNumber=" + URLEncoder.encode(getTermProvider().getExternalCourseNumber(session, subject, courseNbr), "utf-8"));
		} catch (Exception e) {
			throw new SectioningException("Failed to get course URL: " + e.getMessage(), e);
		}
	}
}
