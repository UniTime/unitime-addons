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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.colleague.dataexchange.ColleagueMessage.MessageAction;
import org.unitime.colleague.dataexchange.SendColleagueMessage;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;



public class ColleagueSectionCrosslistHelper {
	private InstructionalOffering instructionalOffering;
	private Session hibSession;
	/**
	 * 
	 */
	public ColleagueSectionCrosslistHelper(InstructionalOffering instructionalOffering, Session hibSession) {
		this.instructionalOffering = instructionalOffering;
		this.hibSession = hibSession;
	}
	
    
	@SuppressWarnings("unchecked")
	private void ensureAllSubpartClassesHaveColleagueSection(SchedulingSubpart schedSubpart) throws Exception{
		
		for(Class_ c : schedSubpart.getClasses()){
			List<ColleagueSection> colleagueSections = ColleagueSection.findNotDeletedColleagueSectionsForClass(c, hibSession);
			if (colleagueSections.isEmpty()){
				if (c.getParentClass() != null && c.getParentClass().getSchedulingSubpart().getItype().getItype().equals(c.getSchedulingSubpart().getItype().getItype())){
					List<ColleagueSection> parentColleagueSections = ColleagueSection.findNotDeletedColleagueSectionsForClass(c.getParentClass(), hibSession);
					Transaction trans = hibSession.beginTransaction();
					for(ColleagueSection cs : parentColleagueSections){
						cs.addClass(c, hibSession);
						hibSession.update(cs);
					}
					trans.commit();
				} else {					
					for (CourseOffering courseOffering : instructionalOffering.getCourseOfferings()){
						addColleagueSectionFor(courseOffering, c);
					}
				}
			
			} else {
				if (c.getParentClass() != null && c.getParentClass().getSchedulingSubpart().getItype().getItype().equals(c.getSchedulingSubpart().getItype().getItype())){
					HashSet<ColleagueSection> sections = new HashSet<ColleagueSection>();
					sections.addAll(ColleagueSection.findNotDeletedColleagueSectionsForClass(c, hibSession));
					
					HashSet<ColleagueSection> parentSections = new HashSet<ColleagueSection>();
					parentSections.addAll(ColleagueSection.findNotDeletedColleagueSectionsForClass(c.getParentClass(), hibSession));
					
					HashSet<ColleagueSection> removeSet = new HashSet<ColleagueSection>();
					removeSet.addAll(sections);
					removeSet.removeAll(parentSections);
					
					
					
					HashSet<ColleagueSection> addSet = new HashSet<ColleagueSection>();
					addSet.addAll(parentSections);
					addSet.removeAll(sections);
					
					if (!removeSet.isEmpty()){
						for(ColleagueSection cs : removeSet){
							if (!cs.isDeleted()){
								Transaction trans = hibSession.beginTransaction();
								SendColleagueMessage.sendColleagueMessage(cs, MessageAction.DELETE, hibSession);
								cs.setDeleted(new Boolean(true));
								hibSession.update(cs);
								trans.commit();
							}
						}
					}
					if (!addSet.isEmpty()){
						for(ColleagueSection cs : addSet){
							Transaction trans = hibSession.beginTransaction();
							cs.addClass(c, hibSession);
							hibSession.update(cs);
							trans.commit();
						}
					}

				
				} else {
					HashSet<CourseOffering> hs = new HashSet<CourseOffering>();
					HashSet<ColleagueSection> removeSet = new HashSet<ColleagueSection>();
					hs.addAll(instructionalOffering.getCourseOfferings());
					CourseOffering co = null;
					for(ColleagueSection cs : colleagueSections){
						co = cs.getCourseOffering(hibSession);
						if (!hs.remove(co)){
							removeSet.add(cs);
						}
					}
					if (!hs.isEmpty()){
						for (CourseOffering courseOffering : hs){
							addColleagueSectionFor(courseOffering, c);
						}
					}
					if (!removeSet.isEmpty()){
						for(ColleagueSection cs : removeSet){
							Transaction trans = hibSession.beginTransaction();
							SendColleagueMessage.sendColleagueMessage(cs, MessageAction.DELETE, hibSession);
							cs.setDeleted(new Boolean(true));
							hibSession.update(cs);
							trans.commit();
						}
					}
				}
			}
			Transaction trans = hibSession.beginTransaction();
			adjustColleagueSectionParentageIfNecessary(c);
			trans.commit();
			hibSession.flush();
		}
		for(SchedulingSubpart ss :schedSubpart.getChildSubparts()){
			ensureAllSubpartClassesHaveColleagueSection(ss);
		}
	}
	
	private void adjustColleagueSectionParentageIfNecessary(Class_ c) {
		List<ColleagueSection> sections = ColleagueSection.findNotDeletedColleagueSectionsForClass(c, hibSession);
		for(ColleagueSection cs : sections){
			if (c.getParentClass() == null){
				if (cs.getParentColleagueSection() != null){
					ColleagueSection parentSection = cs.getParentColleagueSection();
					cs.setParentColleagueSection(null);
					parentSection.getColleagueSectionToChildSections().remove(cs);
					hibSession.update(cs);
					hibSession.update(parentSection);
				}
			} else {
				ColleagueSection parentSection = ColleagueSection.findColleagueSectionForClassAndCourseOffering(c.getParentClass(), cs.getCourseOffering(hibSession), hibSession);
				Class_ parentClass = c.getParentClass();
				boolean foundParentClass = false;
				while (!foundParentClass && parentSection != null){
					if (!parentClass.getSchedulingSubpart().getItype().getItype().equals(c.getSchedulingSubpart().getItype().getItype())){
						foundParentClass = true;
					} else {
						parentSection = ColleagueSection.findColleagueSectionForClassAndCourseOffering(parentClass.getParentClass(), cs.getCourseOffering(hibSession), hibSession);
						parentClass = parentClass.getParentClass();
					}
				}
				if (parentSection != null && (cs.getParentColleagueSection() == null || !cs.getParentColleagueSection().getUniqueId().equals(parentSection.getUniqueId()))){
					cs.setParentColleagueSection(parentSection);
					parentSection.addTocolleagueSectionToChildSections(cs);
					hibSession.update(parentSection);
					hibSession.update(cs);
				} else if (parentSection == null && cs.getParentColleagueSection() != null){
					cs.setParentColleagueSection(null);
					hibSession.update(cs);
				}
			}
		}
	}

	private void addColleagueSectionFor(CourseOffering courseOffering, Class_ cls){
		ColleagueSection.addColleagueSectionFor(courseOffering, cls, hibSession);		
	}
	

	@SuppressWarnings("unchecked")
	private void ensureAllClassesHaveColleagueSection() throws Exception{
		// Remove any orphaned Colleague sections
		ColleagueSection.removeOrphanedColleagueSections(hibSession);
		
		for(Iterator iocIt = instructionalOffering.getInstrOfferingConfigs().iterator(); iocIt.hasNext();){
			InstrOfferingConfig ioc = (InstrOfferingConfig) iocIt.next();
			for(Iterator ssIt = ioc.getSchedulingSubparts().iterator(); ssIt.hasNext();){
				SchedulingSubpart ss = (SchedulingSubpart) ssIt.next();
				if (ss.getParentSubpart() == null){
					ensureAllSubpartClassesHaveColleagueSection(ss);
				}
			}
		}
	}

	public void updateCrosslists(){
		Debug.info("Manage crosslisting for an offering");

		// ensure all associated classes have at least one colleague section if not create them
		// by ensuring all classes have a colleague section crosslists that are removed have a message sent
		// the remaining classes have a message sent with the appropriate crosslist information
		try {
			ensureAllClassesHaveColleagueSection();
		} catch (Exception e) {
			// TODO decide how to handle a failure to ensure all classes have at least one colleague section
			e.printStackTrace();
		}

	}
	
}
