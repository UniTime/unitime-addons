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
