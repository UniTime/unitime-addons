/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
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
	@SuppressWarnings("unchecked")
	public void performExternalInstructionalOfferingAddAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		if (BannerSession.shouldCreateBannerDataForSession(instructionalOffering.getSession(), hibSession)){
			Transaction trans = null;
			if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive())
				trans = hibSession.beginTransaction();
			for(Iterator it = instructionalOffering.getCourseOfferings().iterator(); it.hasNext();){
				CourseOffering co = (CourseOffering) it.next();
				if (BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), hibSession) == null) {
					BannerCourse bc = new BannerCourse();
					bc.setCourseOffering(co);
					bc.setCourseOfferingId(co.getUniqueId());
					hibSession.save(bc);
				}
			}
			if (trans != null)
				trans.commit();
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstructionalOfferingDeleteAction#performExternalInstructionalOfferingDeleteAction(org.unitime.timetable.model.InstructionalOffering, org.hibernate.Session)
	 */
	@SuppressWarnings("unchecked")
	public void performExternalInstructionalOfferingDeleteAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		if (BannerSession.shouldCreateBannerDataForSession(instructionalOffering.getSession(), hibSession)){
			for(Iterator it = instructionalOffering.getCourseOfferings().iterator(); it.hasNext();){
				CourseOffering co = (CourseOffering) it.next();
				BannerCourse bc = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), hibSession);		
				if (bc != null){
					hibSession.delete(bc);
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

	@SuppressWarnings("unchecked")
	public void performExternalCourseOfferingRemoveAction(
			CourseOffering courseOffering, Session hibSession) {
		if (BannerSession.shouldCreateBannerDataForSession(courseOffering.getSubjectArea().getSession(), hibSession)){
			List<BannerCourse> bannerCourses = BannerCourse.findBannerCoursesForCourseOffering(courseOffering.getUniqueId(), hibSession);
			if (!bannerCourses.isEmpty()){
				for (BannerCourse bc : bannerCourses){
					for(Iterator bcfgIt = bc.getBannerConfigs().iterator(); bcfgIt.hasNext();){
						BannerConfig bcfg = (BannerConfig) bcfgIt.next();
						for(Iterator bsIt = bcfg.getBannerSections().iterator(); bsIt.hasNext();){
							BannerSection bs = (BannerSection) bsIt.next();
							SendBannerMessage.sendBannerMessage(bs, BannerMessageAction.DELETE, hibSession);
						}
					}
					hibSession.delete(bc);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void performExternalInstructionalOfferingInCrosslistAddAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		if (BannerSession.shouldCreateBannerDataForSession(instructionalOffering.getSession(), hibSession)){
			for(Iterator it = instructionalOffering.getCourseOfferings().iterator(); it.hasNext();){
				CourseOffering co = (CourseOffering) it.next();
				if (BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), hibSession) == null) {
					BannerCourse bc = new BannerCourse();
					bc.setCourseOffering(co);
					bc.setCourseOfferingId(co.getUniqueId());
					hibSession.save(bc);
				}
			}
		}

	}

}
