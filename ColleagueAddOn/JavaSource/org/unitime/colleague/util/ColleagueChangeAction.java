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
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.hibernate.Session;
import org.unitime.colleague.dataexchange.ColleagueMessage.MessageAction;
import org.unitime.colleague.dataexchange.SendColleagueMessage;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.timetable.interfaces.ExternalClassEditAction;
import org.unitime.timetable.interfaces.ExternalCourseCrosslistAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingRemoveAction;
import org.unitime.timetable.interfaces.ExternalCourseOfferingReservationEditAction;
import org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingAddAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingDeleteAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingInCrosslistAddAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingNotOfferedAction;
import org.unitime.timetable.interfaces.ExternalInstructionalOfferingOfferedAction;
import org.unitime.timetable.interfaces.ExternalSchedulingSubpartEditAction;
import org.unitime.timetable.interfaces.ExternalSolutionCommitAction;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Solution;


/**
 * @author says
 *
 */
public class ColleagueChangeAction implements ExternalClassEditAction,
		ExternalCourseCrosslistAction, ExternalCourseOfferingEditAction,
		ExternalCourseOfferingRemoveAction,
		ExternalCourseOfferingReservationEditAction,
		ExternalInstrOfferingConfigAssignInstructorsAction,
		ExternalInstructionalOfferingAddAction,
		ExternalInstructionalOfferingInCrosslistAddAction,
		ExternalInstructionalOfferingDeleteAction,
		ExternalInstructionalOfferingNotOfferedAction,
		ExternalInstructionalOfferingOfferedAction,
		ExternalSchedulingSubpartEditAction, ExternalSolutionCommitAction {

	/**
	 * 
	 */
	public ColleagueChangeAction() {
		// 
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassEditAction#performExternalExternalClassEditAction(org.unitime.timetable.model.Class_, org.hibernate.Session)
	 */
	public void performExternalClassEditAction(Class_ clazz,
			Session hibSession) {
		SendColleagueMessage.sendColleagueMessage(ColleagueSection.findNotDeletedColleagueSectionsForClass(clazz, hibSession), MessageAction.UPDATE, hibSession);
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalCourseCrosslistAction#performExternalCourseCrosslistAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalCourseCrosslistAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		ColleagueInstrOffrConfigChangeAction ciocca = new ColleagueInstrOffrConfigChangeAction();
		ciocca.performExternalInstrOffrConfigChangeAction(instructionalOffering, hibSession);
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction#performExternalCourseOfferingEditAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalCourseOfferingEditAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
			
		SendColleagueMessage.sendColleagueMessage(ColleagueSection.findColleagueSectionsForInstructionalOffering(instructionalOffering, hibSession), MessageAction.UPDATE, hibSession);
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalCourseOfferingReservationEditAction#performExternalCourseOfferingReservationEditAction(java.lang.Object, org.hibernate.Session)
	 */
	public void performExternalCourseOfferingReservationEditAction(
			Object classOrInstructionalOffering, Session hibSession) {
		InstructionalOffering io = null;
		if (classOrInstructionalOffering instanceof InstructionalOffering) {
			io = (InstructionalOffering) classOrInstructionalOffering;
		}
		if (classOrInstructionalOffering instanceof Class_) {
			Class_ c = (Class_) classOrInstructionalOffering;
			io = c.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering();
		}
		if (io != null){
			ColleagueInstrOffrConfigChangeAction ciocca = new ColleagueInstrOffrConfigChangeAction();
			ciocca.performExternalInstrOffrConfigChangeAction(io, hibSession);
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction#performExternalInstrOfferingConfigAssignInstructorsAction(org.unitime.timetable.model.InstrOfferingConfig, org.hibernate.Session)
	 */
	public void performExternalInstrOfferingConfigAssignInstructorsAction(
			InstrOfferingConfig instrOfferingConfig, Session hibSession) {
		SendColleagueMessage.sendColleagueMessage(ColleagueSection.findColleagueSectionsForInstrOfferingConfig(instrOfferingConfig, hibSession), MessageAction.UPDATE, hibSession);
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstructionalOfferingAddAction#performExternalInstructionalOfferingAddAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalInstructionalOfferingAddAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		// In the Colleague Add On there is no colleague course to add, the coures information is stored 
		// on the section so there is nothing that needs to be done at the time an instructional offering
		// is created.
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstructionalOfferingDeleteAction#performExternalInstructionalOfferingDeleteAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalInstructionalOfferingDeleteAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		// In the Colleague Add On there is nothing to do here.  An instructional offering can only be
		// deleted if it is not offered and has no classes.  This means all colleague sections are
		// already canceled or deleted in Colleague.  There is no "Colleague Course" that needs to be
		// removed.
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstructionalOfferingNotOfferedAction#performExternalInstructionalOfferingNotOfferedAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalInstructionalOfferingNotOfferedAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		ColleagueSection.removeOrphanedColleagueSections(hibSession);
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstructionalOfferingOfferedAction#performExternalInstructionalOfferingOfferedAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalInstructionalOfferingOfferedAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		//Should not have to do anything here.
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalSchedulingSubpartEditAction#performExternalSchedulingSubpartEditAction(org.unitime.timetable.model.SchedulingSubpart, org.hibernate.Session)
	 */
	public void performExternalSchedulingSubpartEditAction(
			SchedulingSubpart schedulingSubpart, Session hibSession) {
		SendColleagueMessage.sendColleagueMessage(ColleagueSection.findNotDeletedColleagueSectionsForSchedulingSubpart(schedulingSubpart, hibSession), MessageAction.UPDATE, hibSession);
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalSolutionCommitAction#performExternalSolutionCommitAction(org.unitime.timetable.model.Solution, org.hibernate.Session)
	 */
	public void performExternalSolutionCommitAction(Set<Solution> solutions,
			Session hibSession) {
		HashSet<ColleagueSection> sections = new HashSet<ColleagueSection>();
		for(Solution s : solutions){
			sections.addAll(ColleagueSection.findColleagueSectionsForSolution(s, hibSession));
		}
		Vector<ColleagueSection> colleagueSections = new Vector<ColleagueSection>();
		colleagueSections.addAll(sections);
		SendColleagueMessage.sendColleagueMessage(colleagueSections, MessageAction.UPDATE, hibSession);
	}

	public void performExternalCourseOfferingRemoveAction(
			CourseOffering courseOffering, Session hibSession) {
		if (ColleagueSession.shouldCreateColleagueDataForSession(courseOffering.getSubjectArea().getSession(), hibSession)){
			List<ColleagueSection> sections = ColleagueSection.findColleagueSectionsForCourseOfferingId(courseOffering.getUniqueId(), hibSession);
			for(ColleagueSection cs : sections){
				SendColleagueMessage.sendColleagueMessage(cs, MessageAction.DELETE, hibSession);
				cs.setDeleted(new Boolean(true));
				hibSession.update(cs);
			}
			SendColleagueMessage.sendColleagueMessage(sections, MessageAction.DELETE, hibSession);
		}
	}

	public void performExternalInstructionalOfferingInCrosslistAddAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
	    // The Colleague Add On does not have to do anything here because the course offering information
		// is stored on the colleague section.

	}

}
