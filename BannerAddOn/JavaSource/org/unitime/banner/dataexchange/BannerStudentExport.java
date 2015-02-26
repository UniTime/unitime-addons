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
package org.unitime.banner.dataexchange;

import java.util.List;

import org.dom4j.Element;
import org.unitime.timetable.dataexchange.StudentExport;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.OverrideReservation;
import org.unitime.timetable.model.Student;

/**
 * @author Tomas Muller
 */
public class BannerStudentExport extends StudentExport {
	
	@Override
	protected void exportStudent(Element studentEl, Student student) {
		super.exportStudent(studentEl, student);

		@SuppressWarnings("unchecked")
		List<OverrideReservation> overrides = (List<OverrideReservation>)getHibSession().createQuery(
				"select r from OverrideReservation r inner join r.students s where s.uniqueId = :studentId")
			.setLong("studentId", student.getUniqueId()).list();
		
		for (OverrideReservation reservation: overrides) {
			CourseOffering course = reservation.getInstructionalOffering().getControllingCourseOffering();
			if (reservation.getClasses().isEmpty()) {
				Element overrideEl = studentEl.addElement("override");
				overrideEl.addAttribute("type", reservation.getOverrideType().getReference().toUpperCase());
				overrideEl.addAttribute("subject", course.getSubjectAreaAbbv());
				overrideEl.addAttribute("courseNbr", course.getCourseNbr());
			} else {
				for (Class_ clazz: reservation.getClasses()) {
					Element overrideEl = studentEl.addElement("override");
					overrideEl.addAttribute("type", reservation.getOverrideType().getReference().toUpperCase());
					overrideEl.addAttribute("subject", course.getSubjectAreaAbbv());
					overrideEl.addAttribute("courseNbr", course.getCourseNbr());
					overrideEl.addAttribute("crn", clazz.getExternalId(course));
				}
			}
		}
	}
}
