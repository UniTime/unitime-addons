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
package org.unitime.colleague.util;

import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSectionToClass;
import org.unitime.timetable.backup.SessionRestore;
import org.unitime.timetable.backup.TableData.Record;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.SubjectArea;

import jakarta.persistence.metamodel.EntityType;

/** 
 * @author Tomas Muller, Stephanie Schluttenhofer
 */
public class ColleagueSessionRestore extends SessionRestore {
	
	@Override
	protected void add(Entity entity) {
		if (entity.getObject().getClass().getSimpleName().startsWith("Colleague"))
			entity = new ColleagueEntity(entity.getMeta(), entity.getRecord(), entity.getObject(), entity.getId());
		super.add(entity);
	}
	
	class ColleagueEntity extends SessionRestore.Entity {
		
		ColleagueEntity(EntityType metadata, Record record, Object object, String id) {
			super(metadata, record, object, id);
		}

		@Override
		public void fixRelations() {
			super.fixRelations();
			// Set links for the UniTime Colleague Add-On, if it is in use
			if (getObject() instanceof ColleagueSectionToClass) {
				ColleagueSectionToClass s2c = (ColleagueSectionToClass)getObject();
				Class_ clazz = (Class_)get(Class_.class, s2c.getClassId().toString());
				if (clazz != null)
					s2c.setClassId(clazz.getUniqueId());
			}
			if (getObject() instanceof ColleagueSection) {
				ColleagueSection cs = (ColleagueSection)getObject();
				SubjectArea subjectArea = (SubjectArea)get(SubjectArea.class, cs.getSubjectAreaId().toString());
				if (subjectArea != null)
					cs.setSubjectAreaId(subjectArea.getUniqueId());
				CourseOffering course = (CourseOffering)get(CourseOffering.class, cs.getCourseOfferingId().toString());
				if (course != null)
					cs.setCourseOfferingId(course.getUniqueId());
			}
		}
	}

}
