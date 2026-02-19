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

package org.unitime.colleague.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.List;
import java.util.TreeSet;

import org.hibernate.FlushMode;
import org.unitime.colleague.model.base.BaseColleagueSession;
import org.unitime.colleague.model.dao.ColleagueSessionDAO;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Session;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.ClassComparator;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;



/**
 * 
 * @author says
 *
 */
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "colleague_session")
public class ColleagueSession extends BaseColleagueSession {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public ColleagueSession () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ColleagueSession (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/


	public static ColleagueSession findColleagueSessionForSession(Session acadSession, org.hibernate.Session hibSession){
		if (acadSession == null){
			return(null);
		}
		return(findColleagueSessionForSession(acadSession.getUniqueId(), hibSession));
	}
	
	public static ColleagueSession findColleagueSessionForSession(Long acadSessionId, org.hibernate.Session hibSession){
		if (acadSessionId == null){
			return(null);
		}
		org.hibernate.Session querySession = hibSession;
		if (querySession == null){
			querySession = ColleagueSessionDAO.getInstance().getSession();
		}

		return querySession.createQuery("from ColleagueSession cs where cs.session.uniqueId = :sessionId", ColleagueSession.class)
				.setHibernateFlushMode(FlushMode.MANUAL).setParameter("sessionId", acadSessionId).setCacheable(true).uniqueResult();
	}
	
	public static boolean shouldGenerateColleagueDataFieldsForSession(Session acadSession, org.hibernate.Session hibSession){
		return(shouldGenerateColleagueDataFieldsForSession(acadSession.getUniqueId(), hibSession));
	}
	public static boolean shouldGenerateColleagueDataFieldsForSession(Long acadSessionId, org.hibernate.Session hibSession){
		if (acadSessionId == null){
			return(false);
		}
		ColleagueSession cs = findColleagueSessionForSession(acadSessionId, hibSession);
		if (cs == null){
			return(false);
		}
		return(!cs.isLoadingOfferingsFile());
	}

	public static boolean shouldCreateColleagueDataForSession(Session acadSession, org.hibernate.Session hibSession){
		return(shouldCreateColleagueDataForSession(acadSession.getUniqueId(), hibSession));
	}
	public static boolean shouldCreateColleagueDataForSession(Long acadSessionId, org.hibernate.Session hibSession){
		if (acadSessionId == null){
			return(false);
		}
		org.hibernate.Session querySession = hibSession;
		if (querySession == null){
			querySession = Class_DAO.getInstance().getSession();
		}
		ColleagueSession cs = findColleagueSessionForSession(acadSessionId, hibSession);
		if (cs == null){
			return(false);
		}
		return(cs.isStoreDataForColleague());
	}
	public static boolean shouldSendDataToColleagueForSession(Session acadSession, org.hibernate.Session hibSession){
		return(shouldSendDataToColleagueForSession(acadSession.getUniqueId(), hibSession));
	}
	public static boolean shouldSendDataToColleagueForSession(Long acadSessionId, org.hibernate.Session hibSession){
		if (acadSessionId == null){
			return(false);
		}
		ColleagueSession cs = findColleagueSessionForSession(acadSessionId, hibSession);
		if (cs == null){
			return(false);
		}
		return(cs.isStoreDataForColleague() && cs.isSendDataToColleague() && !cs.isLoadingOfferingsFile());
	}

	@Transient
	public static List<ColleagueSession> getAllSessions() {
		return ColleagueSessionDAO.getInstance().getSession().createQuery("from ColleagueSession", ColleagueSession.class).list();
	}

	public static ColleagueSession getColleagueSessionById(Long id) {
		ColleagueSessionDAO csDao = new ColleagueSessionDAO();
		return(csDao.get(id));
	}
	
	private List<InstructionalOffering> getAllControllingInstructionalOfferingsForSubjectArea(SubjectArea subjectArea) {
		StringBuilder sb = new StringBuilder();
		sb.append("select co.instructionalOffering ")
		  .append("from SubjectArea sa inner join sa.courseOfferings as co ")
		  .append("where sa.uniqueId = :subjectAreaId ")
		  .append("  and co.isControl = true");
		  ;
		String instructionalOfferingQuery = sb.toString();
		
		InstructionalOfferingDAO ioDao = new InstructionalOfferingDAO();
		return ioDao.getSession().createQuery(instructionalOfferingQuery, InstructionalOffering.class)
				.setParameter("subjectAreaId", subjectArea.getUniqueId()).list();
	}

	public void assignSectionNumbersToAllSectionsForSession() {
		
		InstructionalOfferingDAO ioDao = new InstructionalOfferingDAO();
		for(SubjectArea sa : this.getSession().getSubjectAreas()){
			for (InstructionalOffering io : getAllControllingInstructionalOfferingsForSubjectArea(sa)){
				for(InstrOfferingConfig ioc : io.getInstrOfferingConfigs()){
					for (SchedulingSubpart ss : ioc.getSchedulingSubparts()){
						TreeSet<Class_> classes = new TreeSet<Class_>(new ClassComparator(ClassComparator.COMPARE_BY_SUBJ_NBR_ITYP_SEC));
						classes.addAll(ss.getClasses());
						for(Class_ c : classes){
							for(ColleagueSection cs : ColleagueSection.findColleagueSectionsForClass(c, ioDao.getSession())){
								if (cs.getSectionIndex() == null){
									try {
										cs.assignNewSectionIndex(ioDao.getSession());
									} catch (Exception e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
									}
								}
							}
						}
					}
				}
			}
		}
	}
	public void generateColleagueSectionsForSession() {
		SchedulingSubpartComparator ssc = new SchedulingSubpartComparator();
		InstructionalOfferingDAO ioDao = new InstructionalOfferingDAO();
		for(SubjectArea sa : this.getSession().getSubjectAreas()){
			for (InstructionalOffering io : getAllControllingInstructionalOfferingsForSubjectArea(sa)){
				for(InstrOfferingConfig ioc : io.getInstrOfferingConfigs()){
					TreeSet<SchedulingSubpart> subparts = new TreeSet<SchedulingSubpart>(ssc);
					subparts.addAll(ioc.getSchedulingSubparts());
					for (SchedulingSubpart ss : subparts){
						TreeSet<Class_> classes = new TreeSet<Class_>(new ClassComparator(ClassComparator.COMPARE_BY_SUBJ_NBR_ITYP_SEC));
						classes.addAll(ss.getClasses());
						for(Class_ c : classes){
							for(CourseOffering co : io.getCourseOfferings()){
								ColleagueSection cs = ColleagueSection.findColleagueSectionForClassAndCourseOffering(c, co, ioDao.getSession());
								if (cs == null){
									if(c.getParentClass() != null){
										ColleagueSection pcs = ColleagueSection.findColleagueSectionForClassAndCourseOffering(c.getParentClass(), co, ioDao.getSession());
										if (pcs != null){
											pcs.addClass(c, ioDao.getSession());
											ioDao.getSession().merge(pcs);
										} else {
											ColleagueSection.addColleagueSectionFor(co, c, ioDao.getSession());
										}
									} else {
										ColleagueSection.addColleagueSectionFor(co, c, ioDao.getSession());
									}									
								}
							}
						}
					}
				}
			}
		}
		
	}
		
}
