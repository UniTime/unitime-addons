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
package org.unitime.banner.util;

import org.hibernate.metadata.ClassMetadata;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSectionToClass;
import org.unitime.timetable.backup.SessionRestore;
import org.unitime.timetable.backup.TableData.Record;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;

/** 
 * @author Tomas Muller
 */
public class BannerSessionRestore extends SessionRestore {
	
	@Override
	protected void add(Entity entity) {
		if (entity.getObject().getClass().getSimpleName().startsWith("Banner"))
			entity = new BannerEntity(entity.getMetaData(), entity.getRecord(), entity.getObject(), entity.getId());
		super.add(entity);
	}
	
	class BannerEntity extends SessionRestore.Entity {
		
		BannerEntity(ClassMetadata metadata, Record record, Object object, String id) {
			super(metadata, record, object, id);
		}

		@Override
		public void fixRelations() {
			super.fixRelations();
			// Set links for the UniTime Banner Add-On, if it is in use
			if (getObject() instanceof BannerSectionToClass) {
				BannerSectionToClass s2c = (BannerSectionToClass)getObject();
				Class_ clazz = (Class_)get(Class_.class, s2c.getClassId().toString());
				if (clazz != null)
					s2c.setClassId(clazz.getUniqueId());
			}
			if (getObject() instanceof BannerConfig) {
				BannerConfig bc = (BannerConfig)getObject();
				InstrOfferingConfig config = (InstrOfferingConfig)get(InstrOfferingConfig.class, bc.getInstrOfferingConfigId().toString());
				if (config != null)
					bc.setInstrOfferingConfigId(config.getUniqueId());
			}
			if (getObject() instanceof BannerCourse) {
				BannerCourse bc = (BannerCourse)getObject();
				CourseOffering course = (CourseOffering)get(CourseOffering.class, bc.getCourseOfferingId().toString());
				if (course != null)
					bc.setCourseOfferingId(course.getUniqueId());
			}
		}
	}

}
