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
