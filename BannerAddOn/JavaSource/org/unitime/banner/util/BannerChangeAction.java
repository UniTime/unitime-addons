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

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
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
public class BannerChangeAction implements ExternalClassEditAction,
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
	public BannerChangeAction() {
		// 
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassEditAction#performExternalExternalClassEditAction(org.unitime.timetable.model.Class_, org.hibernate.Session)
	 */
	public void performExternalClassEditAction(Class_ clazz,
			Session hibSession) {
		SendBannerMessage.sendBannerMessage(BannerSection.findBannerSectionsForClass(clazz, hibSession), BannerMessageAction.UPDATE, hibSession);
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalCourseCrosslistAction#performExternalCourseCrosslistAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalCourseCrosslistAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		BannerInstrOffrConfigChangeAction biocca = new BannerInstrOffrConfigChangeAction();
		biocca.performExternalInstrOffrConfigChangeAction(instructionalOffering, hibSession);
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalCourseOfferingEditAction#performExternalCourseOfferingEditAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalCourseOfferingEditAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		SendBannerMessage.sendBannerMessage(BannerSection.findBannerSectionsForInstructionalOffering(instructionalOffering, hibSession), BannerMessageAction.UPDATE, hibSession);
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
			BannerInstrOffrConfigChangeAction biocca = new BannerInstrOffrConfigChangeAction();
			biocca.performExternalInstrOffrConfigChangeAction(io, hibSession);
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstrOfferingConfigAssignInstructorsAction#performExternalInstrOfferingConfigAssignInstructorsAction(org.unitime.timetable.model.InstrOfferingConfig, org.hibernate.Session)
	 */
	public void performExternalInstrOfferingConfigAssignInstructorsAction(
			InstrOfferingConfig instrOfferingConfig, Session hibSession) {
		SendBannerMessage.sendBannerMessage(BannerSection.findBannerSectionsForInstrOfferingConfig(instrOfferingConfig, hibSession), BannerMessageAction.UPDATE, hibSession);
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstructionalOfferingAddAction#performExternalInstructionalOfferingAddAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalInstructionalOfferingAddAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		if (BannerSession.shouldCreateBannerDataForSession(instructionalOffering.getSession(), hibSession)){
			Transaction trans = null;
			if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
				trans = hibSession.beginTransaction();
			for(CourseOffering co : instructionalOffering.getCourseOfferings()){
				if (BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), hibSession) == null) {
					BannerCourse bc = new BannerCourse();
					bc.setCourseOffering(co);
					bc.setCourseOfferingId(co.getUniqueId());
					hibSession.persist(bc);
				}
			}
			if (trans != null)
				trans.commit();
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstructionalOfferingDeleteAction#performExternalInstructionalOfferingDeleteAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalInstructionalOfferingDeleteAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		if (BannerSession.shouldCreateBannerDataForSession(instructionalOffering.getSession(), hibSession)){
			for(CourseOffering co : instructionalOffering.getCourseOfferings()){
				BannerCourse bc = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), hibSession);		
				if (bc != null){
					hibSession.remove(bc);
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstructionalOfferingNotOfferedAction#performExternalInstructionalOfferingNotOfferedAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	public void performExternalInstructionalOfferingNotOfferedAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		BannerSection.removeOrphanedBannerSections(hibSession);
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
		SendBannerMessage.sendBannerMessage(BannerSection.findBannerSectionsForSchedulingSubpart(schedulingSubpart, hibSession), BannerMessageAction.UPDATE, hibSession);
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalSolutionCommitAction#performExternalSolutionCommitAction(org.unitime.timetable.model.Solution, org.hibernate.Session)
	 */
	public void performExternalSolutionCommitAction(Set<Solution> solutions,
			Session hibSession) {
		HashSet<BannerSection> sections = new HashSet<BannerSection>();
		for(Solution s : solutions){
			sections.addAll(BannerSection.findBannerSectionsForSolution(s, hibSession));
		}
		Vector<BannerSection> bannerSections = new Vector<BannerSection>();
		bannerSections.addAll(sections);
		SendBannerMessage.sendBannerMessage(bannerSections, BannerMessageAction.UPDATE, hibSession);
	}

	public void performExternalCourseOfferingRemoveAction(
			CourseOffering courseOffering, Session hibSession) {
		if (BannerSession.shouldCreateBannerDataForSession(courseOffering.getSubjectArea().getSession(), hibSession)){
			for (BannerCourse bc : BannerCourse.findBannerCoursesForCourseOffering(courseOffering.getUniqueId(), hibSession)){
				for(BannerConfig bcfg : bc.getBannerConfigs()){
					for(BannerSection bs : bcfg.getBannerSections()){
						SendBannerMessage.sendBannerMessage(bs, BannerMessageAction.DELETE, hibSession);
					}
				}
				hibSession.remove(bc);
			}
		}
	}

	public void performExternalInstructionalOfferingInCrosslistAddAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		if (BannerSession.shouldCreateBannerDataForSession(instructionalOffering.getSession(), hibSession)){
			for(CourseOffering co : instructionalOffering.getCourseOfferings()){
				if (BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), hibSession) == null) {
					BannerCourse bc = new BannerCourse();
					bc.setCourseOffering(co);
					bc.setCourseOfferingId(co.getUniqueId());
					hibSession.persist(bc);
				}
			}
		}

	}

}
