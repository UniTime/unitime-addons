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

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.Vector;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.colleague.dataexchange.ColleagueMessage.MessageAction;
import org.unitime.colleague.dataexchange.SendColleagueMessage;
import org.unitime.colleague.model.base.BaseColleagueSection;
import org.unitime.colleague.model.dao.ColleagueSectionDAO;
import org.unitime.colleague.model.dao.ColleagueSuffixDefDAO;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.Constants;

/**
 * 
 * @author says
 *
 */
public class ColleagueSection extends BaseColleagueSection {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7327253389631092870L;


	/*[CONSTRUCTOR MARKER BEGIN]*/
	public ColleagueSection () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public ColleagueSection (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/

	private HashSet<Class_> classes;
	private CourseOffering courseOffering;
	private SubjectArea subjectArea;
	
	public void addClass(Class_ clazz, Session hibSession){
		if (clazz == null || clazz.getUniqueId() == null){
			return;
		}
		if (getColleagueSectionToClasses() != null && !getColleagueSectionToClasses().isEmpty()){
			initClassesIfNecessary(hibSession, clazz);
		}
		ColleagueSectionToClass csc = new ColleagueSectionToClass();
		csc.setColleagueSection(this);
		csc.setClassId(clazz.getUniqueId());
		addTocolleagueSectionToClasses(csc);
		if (classes == null){
			classes = new HashSet<Class_>();
		}
		classes.add(clazz);
	}
	
	private void initClassesIfNecessary(Session hibSession, Class_ clazz){
		if (classes == null){
			classes = new HashSet<Class_>();
			if (getColleagueSectionToClasses() != null && !getColleagueSectionToClasses().isEmpty()){
				Session querySession;
				if (hibSession == null){
					querySession = Class_DAO.getInstance().getSession();
				} else {
					querySession = hibSession;
				}
				for(ColleagueSectionToClass csc : getColleagueSectionToClasses()){
					if (clazz != null && csc.getClassId().equals(clazz.getUniqueId())){
						classes.add(clazz);
					} else {
						Class_ c = (Class_)querySession.createQuery("from Class_ c where c.uniqueId = :classId").setLong("classId", csc.getClassId().longValue()).setFlushMode(FlushMode.MANUAL).uniqueResult();
						if (c != null){
							classes.add(c);
						}
					}
				}
			}
		}		
	}
	
	public HashSet<Class_> getClasses(Session hibSession, Class_ clazz){
		initClassesIfNecessary(hibSession, clazz);	
		return(classes);
	}
	
	public HashSet<Class_> getClasses(Session hibSession){
		initClassesIfNecessary(hibSession, null);	
		return(classes);
	}


	public static ColleagueSection findColleagueSectionForClassAndCourseExternalId(Class_ clazz, String courseExternalId, Session hibSession, org.unitime.timetable.model.Session acadSession){
		return((ColleagueSection)hibSession
				.createQuery("select cs from ColleagueSection cs inner join cs.colleagueSectionToClasses as csc where cs.courseOfferingId = " +
						" (select co.uniqueId from CourseOffering co where co.externalUniqueId = :courseExternalId and co.instructionalOffering.session.uniqueId = :sessionId)" +
						" and csc.classId = :classId")
				.setLong("classId", clazz.getUniqueId().longValue())
				.setLong("sessionId", acadSession.getUniqueId().longValue())
				.setString("courseExternalId", courseExternalId)
				.setFlushMode(FlushMode.MANUAL)
				.setCacheable(false)
				.uniqueResult());
	}
	
	@SuppressWarnings("unchecked")
	public static List<ColleagueSection> findColleagueSectionsForInstructionalOffering(InstructionalOffering instructionalOffering, Session hibSession){
		return((List<ColleagueSection>) hibSession.createQuery("select cs from ColleagueSection cs, CourseOffering co where co.instructionalOffering = :instrOfferId and cs.courseOfferingId = co.uniqueId")
				           .setLong("instrOfferId", instructionalOffering.getUniqueId().longValue())
				           .setFlushMode(FlushMode.MANUAL)
				           .list());
	}
		
	public boolean isNestedSection(){
		return(getColleagueSectionToClasses() != null && getColleagueSectionToClasses().size() > 1);
	}
	
	private int countColleagueSectionsFor(Long classId, Session hibSession){
		if (classId == null){
			return(0);
		}
		Session querySession = hibSession;
		if (querySession == null){
			querySession = Class_DAO.getInstance().getSession();
		}
		return((Long)querySession.createQuery("select count(csc) from ColleagueSectionToClass csc where csc.classId = :classId").setLong("classId", classId.longValue()).setFlushMode(FlushMode.MANUAL).uniqueResult()).intValue();
		
	}

	public boolean isCrossListedSection(Session hibSession){
		boolean isCrossListed = false;
		for (ColleagueSectionToClass csc : getColleagueSectionToClasses()){
			if (countColleagueSectionsFor(csc.getClassId(), hibSession) > 1){
				isCrossListed = true;
			}			
		}
		return(isCrossListed);
	}
	
	public int calculateMaxEnrl(Session hibSession) {
		return(maxEnrollBasedOnClasses(hibSession));
	}
	
	
	public Class_ getFirstClass() {
		Class_ c = null;
		if (getColleagueSectionToClasses() != null && !getColleagueSectionToClasses().isEmpty()){
			ColleagueSectionToClass csc = (ColleagueSectionToClass)getColleagueSectionToClasses().iterator().next();
			c = Class_DAO.getInstance().get(csc.getClassId());
		}
		return(c);
	}

	public int maxEnrollBasedOnClasses(Session hibSession){
		int maxEnroll = Integer.MAX_VALUE;
		for(Iterator<Class_> it = getClasses(hibSession).iterator(); it.hasNext();){
			Class_ c = it.next();
			if (c.getSchedulingSubpart().getInstrOfferingConfig().isUnlimitedEnrollment().booleanValue()){
				maxEnroll = 9999;
				break;
			}
			if (c.getClassLimit() < maxEnroll){
				maxEnroll = c.getClassLimit();
			}
		}
		return(maxEnroll);	
	}

			
	
	public static boolean isSectionIndexUniqueForCourse(org.unitime.timetable.model.Session acadSession, String subjectAreaAbbreviation, String courseNumber,
			Session hibSession, String sectionId) {
		int sectionExists = 0;
	   	try {
    		String sectionExistsSql = "select count(cs) "
    				+ " from ColleagueSection cs ,"
    				+ " CourseOffering co, ColleagueSession colSess, SubjectArea sa"
    				+ " inner join ColleagueSession as s on cs.session = s.session"
    				+ " where colSess.session.uniqueId = :sessId"
    				+ "   and s.termCode = colSess.termCode"
    				+ "   and cs.sectionIdentifier = :sectionId"
    				+ "   and sa.uniqueId = cs.subjectArea.uniqueId"
    				+ "   and sa.subjectAreaAbbreviation = :subject"
    				+ "   and cs.courseNumber = :crsNbr"
    				;
    		sectionExists =  Integer.parseInt(hibSession.createQuery(sectionExistsSql)
    		      .setLong("sessId", acadSession.getUniqueId().longValue())
    		      .setString("sectionId", sectionId)
    		      .setString("subject", subjectAreaAbbreviation)
    		      .setString("crsNbr", courseNumber)
    		      .uniqueResult().toString());
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return(sectionExists < 1);
	}
	
	public static String findNextUnusedSectionIndexFor(org.unitime.timetable.model.Session acadSession, CourseOffering courseOffering,  ItypeDesc itype,
			Session hibSession) throws Exception {
		String nextSectionId = null;
	   		ColleagueSession colleagueSession = ColleagueSession.findColleagueSessionForSession(acadSession.getUniqueId(), hibSession);
	   		if (!colleagueSession.isSendDataToColleague()){
	   			return(null);
	   		}
	   		ColleagueSuffixDef suffix = ColleagueSuffixDef.findColleagueSuffixDefForTermCode(itype, courseOffering, colleagueSession.getColleagueTermCode(), hibSession);
	   		// find the min and max section number for the suffix
	   		List<String> existingSectionIds = findExistingSectionIds(colleagueSession, suffix, courseOffering, hibSession);
	   		if (existingSectionIds == null || existingSectionIds.isEmpty()){
	   			nextSectionId = suffix.findNextSectionIndex(null);
	   		} else {
		   		String secIdx = null;
		   		String firstIdx = null;
		   		boolean foundIdx = false;
		   		boolean checkedAllPossible = false;
		   		while (!foundIdx && !checkedAllPossible){
			   		secIdx = suffix.findNextSectionIndex(secIdx);
		   			if (firstIdx == null){
		   				firstIdx = secIdx;
		   			} else if (secIdx == firstIdx) {
		   				checkedAllPossible = true;
		   			}
		   			if (!existingSectionIds.contains(secIdx)){
		   				foundIdx = true;
		   				nextSectionId=secIdx;
		   			}
		   		}
	   		}
		return(nextSectionId);
	}

	
	@SuppressWarnings("unchecked")
	private static List<String> findExistingSectionIds(ColleagueSession colleagueSession,
			ColleagueSuffixDef suffix, CourseOffering courseOffering2,
			Session hibSession) {
   		String colleagueCourseNumber = ColleagueSuffixDef.findColleagueCourseNumber(courseOffering2);
   		StringBuilder sb = new StringBuilder();
   		sb.append("select cs.sectionIndex")
   		  .append(" from ColleagueSection cs, ColleagueSession cSes, SubjectArea sa")
   		  .append(" where cSes.colleagueTermCode = :termCode")
   		  .append("  and cs.session.uniqueId = cSes.session.uniqueId")
   		  .append("  and cs.subjectAreaId = sa.uniqueId")
   		  .append("  and sa.subjectAreaAbbreviation = :subject")
   		  .append("  and cs.colleagueCourseNumber = :colleagueCourseNumber");
   		
   		return (List<String>)hibSession.createQuery(sb.toString())
          .setString("termCode", colleagueSession.getColleagueTermCode())
          .setString("subject", courseOffering2.getSubjectArea().getSubjectAreaAbbreviation())
          .setString("colleagueCourseNumber", colleagueCourseNumber).list();
	}


	
	@SuppressWarnings("unchecked")
	public static List<ColleagueSection> findColleagueSectionsForClass(Class_ clazz, Session hibSession) {
		return((List<ColleagueSection>)hibSession
			.createQuery("select distinct csc.colleagueSection from ColleagueSectionToClass as csc where csc.classId = :classId")
			.setLong("classId", clazz.getUniqueId().longValue())
			.setFlushMode(FlushMode.MANUAL)
			.list());
	}

	@SuppressWarnings("unchecked")
	public static List<ColleagueSection> findNotDeletedColleagueSectionsForClass(Class_ clazz, Session hibSession) {
		return((List<ColleagueSection>)hibSession
			.createQuery("select distinct csc.colleagueSection from ColleagueSectionToClass as csc where csc.colleagueSection.deleted = false and csc.classId = :classId")
			.setLong("classId", clazz.getUniqueId().longValue())
			.setFlushMode(FlushMode.MANUAL)
			.list());
	}

	@SuppressWarnings("unchecked")
	public static List<ColleagueSection> findColleagueSectionsForInstrOfferingConfig(InstrOfferingConfig instrOfferingConfig, Session hibSession) {
		return((List<ColleagueSection>)hibSession
			.createQuery("select distinct csc.colleagueSection from ColleagueSectionToClass as csc, Class_ c where c.schedulingSubpart.instrOfferingConfig.uniqueId = :configId and csc.classId = c.uniqueId")
			.setLong("configId", instrOfferingConfig.getUniqueId().longValue())
			.setFlushMode(FlushMode.MANUAL)
			.list());
	}

	@SuppressWarnings("unchecked")
	public static List<ColleagueSection> findNotDeletedColleagueSectionsForInstrOfferingConfig(InstrOfferingConfig instrOfferingConfig, Session hibSession) {
		return((List<ColleagueSection>)hibSession
			.createQuery("select distinct csc.colleagueSection from ColleagueSectionToClass as csc, Class_ c where csc.colleagueSection.deleted = false and c.schedulingSubpart.instrOfferingConfig.uniqueId = :configId and csc.classId = c.uniqueId")
			.setLong("configId", instrOfferingConfig.getUniqueId().longValue())
			.setFlushMode(FlushMode.MANUAL)
			.list());
	}

	@SuppressWarnings("unchecked")
	public static List<ColleagueSection> findColleagueSectionsForSchedulingSubpart(SchedulingSubpart schedulingSubpart, Session hibSession) {
		return((List<ColleagueSection>)hibSession
			.createQuery("select distinct csc.colleagueSection from ColleagueSectionToClass as csc, Class_ c where c.schedulingSubpart.uniqueId = :subpartId and csc.classId = c.uniqueId")
			.setLong("subpartId", schedulingSubpart.getUniqueId().longValue())
			.setFlushMode(FlushMode.MANUAL)
			.list());
	}

	@SuppressWarnings("unchecked")
	public static List<ColleagueSection> findNotDeletedColleagueSectionsForSchedulingSubpart(SchedulingSubpart schedulingSubpart, Session hibSession) {
		return((List<ColleagueSection>)hibSession
			.createQuery("select distinct csc.colleagueSection from ColleagueSectionToClass as csc, Class_ c where c.schedulingSubpart.uniqueId = :subpartId and csc.classId = c.uniqueId and csc.colleagueSection.deleted = false")
			.setLong("subpartId", schedulingSubpart.getUniqueId().longValue())
			.setFlushMode(FlushMode.MANUAL)
			.list());
	}

	@SuppressWarnings("unchecked")
	public static List<ColleagueSection> findNotDeletedColleagueSectionsForSchedulingSubpartAndCourse(SchedulingSubpart schedulingSubpart, CourseOffering courseOffering, Session hibSession) {
		return((List<ColleagueSection>)hibSession
			.createQuery("select distinct csc.colleagueSection from ColleagueSectionToClass as csc, Class_ c where c.schedulingSubpart.uniqueId = :subpartId and csc.classId = c.uniqueId and csc.colleagueSection.deleted = false and csc.colleagueSection.courseOfferingId = :courseId")
			.setLong("subpartId", schedulingSubpart.getUniqueId().longValue())
			.setLong("courseId", courseOffering.getUniqueId().longValue())
			.setFlushMode(FlushMode.MANUAL)
			.list());
	}

	@SuppressWarnings("unchecked")
	public static List<ColleagueSection> findColleagueSectionsForSolution(
			Solution solution, Session hibSession) {
		Vector<ColleagueSection> hs = new Vector<ColleagueSection>();
		List<ColleagueSection> sections = hibSession
		.createQuery("select distinct csc.colleagueSection from ColleagueSectionToClass as csc, Assignment a where a.solution.uniqueId = :solutionId and csc.classId = a.clazz.uniqueId and csc.colleagueSection.deleted = false")
		.setLong("solutionId", solution.getUniqueId().longValue())
		.setFlushMode(FlushMode.MANUAL)
		.setCacheable(false)
		.list();
		for (ColleagueSection cs : sections) {
			hs.add(cs);
		}
		return(hs);
	}

	private static void removeOrphanedColleagueSections(Session hibSession, List<ColleagueSection> orphanedSections){
		for(ColleagueSection cs : orphanedSections){
			Debug.info("removing orphaned colleague section");
			SendColleagueMessage.sendColleagueMessage(cs, MessageAction.DELETE, hibSession);
			if (cs.getParentColleagueSection() != null){
				ColleagueSection pCs = cs.getParentColleagueSection();
				pCs.getColleagueSectionToChildSections().remove(cs);
				hibSession.update(pCs);
			}
			if (cs.getColleagueSectionToChildSections() != null && !cs.getColleagueSectionToChildSections().isEmpty()){
				for(ColleagueSection cCs : (Set<ColleagueSection>)cs.getColleagueSectionToChildSections()){
					cCs.setParentColleagueSection(null);
					hibSession.update(cCs);
				}
			}
			cs.setDeleted(new Boolean(true));
			hibSession.update(cs);
			hibSession.flush();
		}		
	}
	@SuppressWarnings("unchecked")
	public static void removeOrphanedColleagueSections(Session hibSession){
		String orphanedSectionsQuery1 = "select distinct cs from ColleagueSection cs where cs.deleted = false and cs.courseOfferingId not in ( select co.uniqueId from CourseOffering co )";
		String orphanedSectionsQuery2 = "select distinct csc.colleagueSection from ColleagueSectionToClass csc where csc.colleagueSection.deleted = false and csc.classId not in ( select c.uniqueId from Class_ c )";

		Transaction trans = hibSession.beginTransaction();
		List<ColleagueSection> orphanedColleagueSections1 = (List<ColleagueSection>) hibSession.createQuery(orphanedSectionsQuery1)
		.setFlushMode(FlushMode.MANUAL)
		.list();
		removeOrphanedColleagueSections(hibSession, orphanedColleagueSections1);
		
		List<ColleagueSection> orphanedColleagueSections2 = (List<ColleagueSection>) hibSession.createQuery(orphanedSectionsQuery2)
				.setFlushMode(FlushMode.MANUAL)
				.list();
		removeOrphanedColleagueSections(hibSession, orphanedColleagueSections2);

		trans.commit();
	}

	public static ColleagueSection findColleagueSectionForClassAndCourseOffering(
			Class_ clazz, CourseOffering courseOffering, Session hibSession) {
		if (clazz == null || courseOffering == null){
			return(null);
		}
		return((ColleagueSection)hibSession
				.createQuery("select distinct csc.colleagueSection from ColleagueSectionToClass as csc where csc.colleagueSection.courseOfferingId = :courseOfferingId " +
						" and csc.classId = :classId")
				.setLong("classId", clazz.getUniqueId().longValue())
				.setLong("courseOfferingId", courseOffering.getUniqueId().longValue())
				.setFlushMode(FlushMode.MANUAL)
				.setCacheable(false)
				.uniqueResult());

	}
	
	public static ColleagueSection findColleagueSectionForClassAndCourseOfferingCacheable(
			Class_ clazz, CourseOffering courseOffering, Session hibSession) {
		if (clazz == null || courseOffering == null){
			return(null);
		}
		return((ColleagueSection)hibSession
				.createQuery("select distinct csc.colleagueSection from ColleagueSectionToClass as csc where csc.colleagueSection.courseOfferingId = :courseOfferingId " +
						" and csc.classId = :classId")
				.setLong("classId", clazz.getUniqueId().longValue())
				.setLong("courseOfferingId", courseOffering.getUniqueId().longValue())
				.setFlushMode(FlushMode.MANUAL)
				.setCacheable(true)
				.uniqueResult());

	}
	
	public String colleagueCourseLabel() {
		CourseOffering co = this.getCourseOffering(null);
		StringBuilder sb = new StringBuilder();
		sb.append(co.getSubjectAreaAbbv())
		  .append(" ")
		  .append(this.getColleagueCourseNumber());
		return(sb.toString());
	}
	
	public String colleagueSectionLabel(){
		String sectionNum = "";
		StringBuilder sb = new StringBuilder();
		if (getSectionIndex() != null){
			sectionNum = getSectionIndex();
		} else {
			sectionNum = "*" + getFirstClass().getSectionNumberString() + "*";
		}
		sb.append(colleagueCourseLabel())
		  .append(" ")
		  .append(sectionNum);
		  ;
		return(sb.toString());
	}
	
	@SuppressWarnings("unchecked")
	public List<ColleagueSection> colleagueSectionsCrosslistedWithThis(){
		if (!getClasses(null).isEmpty()){
		Class_ c = (Class_)getClasses(null).iterator().next();
		String qs = "select distinct csc.colleagueSection from ColleagueSectionToClass csc where csc.class_id = :classId and csc.colleagueSection.uniqueId != :sectionId";
		return(ColleagueSectionDAO.getInstance()
				               .getQuery(qs)
				               .setLong("classId", c.getUniqueId().longValue())
				               .setLong("sectionId", getUniqueId().longValue())
				               .list());
		} else {
			return(new Vector<ColleagueSection>());
		}
		
	}
		
	public String classSuffixFor(Class_ clazz){
			if (this.getColleagueId() == null  && this.getSectionIndex() == null){
				return(clazz.getClassSuffix());
			} else if (this.getColleagueId() == null && this.getSectionIndex() != null) {
				return(this.getSectionIndex() + (courseOffering.getInstructionalOffering().getCourseOfferings().size() > 1?"*":""));
			} else if (this.getColleagueId() != null && this.getSectionIndex() == null) {
				return(this.getColleagueId() + (courseOffering.getInstructionalOffering().getCourseOfferings().size() > 1?"*":""));
			} else {
				if ((this.getColleagueId().length() + this.getSectionIndex().length() + (courseOffering.getInstructionalOffering().getCourseOfferings().size() > 1?"*":"").length()) >= 10){
					String str = this.getColleagueId() + '-' + this.getSectionIndex() + (courseOffering.getInstructionalOffering().getCourseOfferings().size() > 1?"*":"");
					return (str.substring(str.length() - 10));
				} else {
					return (this.getColleagueId() + '-' + this.getSectionIndex() + (courseOffering.getInstructionalOffering().getCourseOfferings().size() > 1?"*":""));
				}
			}
	}
	
	public String externalUniqueIdFor(Class_ clazz, Session hibSession){
		if (this.getColleagueId() == null){
			if (clazz != null && clazz.getExternalUniqueId() != null){
				return(clazz.getExternalUniqueId());
			} else {
				return("");
			}
		}

		return(this.getColleagueId());
	}

	public void assignNewSectionIndex(Session hibSession) throws Exception{
		ItypeDesc itype = null;
		for (Class_ c : this.getClasses(hibSession)){
			itype = c.getSchedulingSubpart().getItype();
			break;
		}
		ColleagueSession cSess = ColleagueSession.findColleagueSessionForSession(this.getSession().getUniqueId(), hibSession);
		if (cSess.isSendDataToColleague() && this.getSectionIndex() == null) {
			this.setSectionIndex(ColleagueSection.findNextUnusedSectionIndexFor(this.getSession(), this.getCourseOffering(hibSession), itype, hibSession));
			hibSession.update(this);
			updateClassSuffixForClassesIfNecessary(hibSession);
		}
	}
	
	
	public void updateClassSuffixForClassesIfNecessary(Session hibSession){
		
		updateClassSuffixForClassesIfNecessaryRefreshClasses(hibSession, false);

	}
	
	public void updateClassSuffixForClassesIfNecessaryRefreshClasses(Session hibSession, boolean refresh){
		Boolean control = this.getCourseOffering(hibSession).isIsControl();
		if (control.booleanValue()){
			for (Iterator<?> it = this.getColleagueSectionToClasses().iterator(); it.hasNext();){
				ColleagueSectionToClass bsc = (ColleagueSectionToClass) it.next();
				Class_ clazz = Class_DAO.getInstance().get(bsc.getClassId(), hibSession);
				if (clazz != null){
					String classSuffix = this.classSuffixFor(clazz);
					if(clazz.getClassSuffix() == null || !clazz.getClassSuffix().equals(classSuffix)){
						clazz.setClassSuffix(classSuffix);
						clazz.setExternalUniqueId(this.externalUniqueIdFor(clazz, hibSession));
						hibSession.update(clazz);
						hibSession.flush();
						if (refresh){
							hibSession.refresh(clazz);
						}
					}
				}
			}
		}

	}
		
	public TreeMap<DepartmentalInstructor, Integer> findInstructorsWithPercents(Session hibSession){

		int numClassesWithInstructors = 0;
		TreeMap<DepartmentalInstructor, Integer> instructorPercents = new TreeMap<DepartmentalInstructor, Integer>();

		// Include course coordinators in the instructor list with a percentage of 0 unless they are on a class with a higher percentage.
		InstructionalOffering io = null;
		try {
			io = getCourseOffering(hibSession).getInstructionalOffering();			
		} catch (Exception e) {
			Debug.error("No instructional offering found for colleague section uniqueId:  " + getUniqueId() + ".");
			// no instructional offering found no course coordinators to be sent.
		}
		if (io != null && io.getCoordinators() != null){
			for (DepartmentalInstructor di : io.getCoordinators()){
				instructorPercents.put(di, new Integer(0));
			}
		}
		boolean sendInstructors = false;
		for(Iterator<Class_> cIt = this.getClasses(hibSession).iterator(); cIt.hasNext();){
			Class_ c = cIt.next();
			if (c.isCancelled().booleanValue()){
				continue;
			}
			if (c.getCommittedAssignment() != null || c.getEffectiveTimePreferences().isEmpty()){
				sendInstructors = true;
				if (c.getClassInstructors() != null && !c.getClassInstructors().isEmpty()){
					int totalPercent = 0;
					int instructorCount = 0;
					for (ClassInstructor ci : c.getClassInstructors()){
						totalPercent += (ci.getPercentShare() != null?(ci.getPercentShare().intValue() < 0?-1*ci.getPercentShare().intValue():ci.getPercentShare().intValue()):0);
						instructorCount++;
					}
					if (instructorCount > 0){
						numClassesWithInstructors++;
					} else {
						continue;
					}
					for (ClassInstructor ci : c.getClassInstructors()){
						if (ci.getInstructor().getExternalUniqueId() != null){
							if (instructorPercents.containsKey(ci.getInstructor()) && totalPercent > 0){
								int pct = instructorPercents.get(ci.getInstructor()).intValue();
								pct += (ci.getPercentShare() != null?(((ci.getPercentShare().intValue() < 0?-1*ci.getPercentShare().intValue():ci.getPercentShare().intValue())*100/totalPercent)):0);
								instructorPercents.put(ci.getInstructor(),new Integer(pct));
							} else {
								if (totalPercent > 0)
									instructorPercents.put(ci.getInstructor(),new Integer(ci.getPercentShare() != null?(((ci.getPercentShare().intValue() < 0?-1*ci.getPercentShare().intValue():ci.getPercentShare().intValue())*100/totalPercent)):0));
								else {
									instructorPercents.put(ci.getInstructor(),new Integer(0));
								}
							}
						}
					}
				}
			}
		}
		if (!sendInstructors){
			instructorPercents.clear();
		}
		if (!instructorPercents.isEmpty() && numClassesWithInstructors > 0){
			int totalPct = 0;
			for(Iterator<DepartmentalInstructor> instrIdIt = instructorPercents.keySet().iterator(); instrIdIt.hasNext();){
				DepartmentalInstructor instructor = instrIdIt.next();
				int pct = instructorPercents.get(instructor).intValue()/numClassesWithInstructors;
				instructorPercents.put(instructor, new Integer(pct));
				totalPct += pct;
			}
			if (totalPct != 100){
				DepartmentalInstructor instructor = instructorPercents.keySet().iterator().next();
				int pct = (instructorPercents.get(instructor).intValue() == 0?0:instructorPercents.get(instructor).intValue() + (100 - totalPct));
				instructorPercents.put(instructor, new Integer(pct));
			}
		}
		return(instructorPercents);
	
	}

    @SuppressWarnings("unchecked")
	public String buildDatePatternHtml(ClassAssignmentProxy classAssignment){
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Class_ aClass : getClasses(Class_DAO.getInstance().getSession())){
    		if (first){
    			first = false;
    		} else {
    			sb.append("<br>");
    		}
    		if (aClass.effectiveDatePattern() != null){
    			sb.append(aClass.effectiveDatePattern().getName());
    		} else {
    			sb.append("&nbsp;");
    		}
	    	if (classAssignment!=null) {
	    		Assignment a = null;
	    		try {
	    			a = classAssignment.getAssignment(aClass);
	    		} catch (Exception e) {
	    			Debug.error(e);
	    		}
	    		if (a!=null) {
	   				if (a.getRoomLocations().size() > 1){
	   					for (int i = 1; i < a.getRoomLocations().size() ; i++){
	   						sb.append("<BR>");
	   					}
	   				}
	     		}   else {
		    		if (aClass.getEffectiveTimePreferences().isEmpty()){
		    			boolean firstReqRoomPref = true;
		    			for(RoomPref rp : (Set<RoomPref>)aClass.getEffectiveRoomPreferences()){
							if (rp.getPrefLevel().getPrefId().toString().equals(PreferenceLevel.PREF_LEVEL_REQUIRED)){
			    				if (firstReqRoomPref){
			    					firstReqRoomPref = false;
			    				} else {
			    					sb.append("<BR>");
			    				}
							}
						}
		    		}
		    	}   		            
	            
	    	} 
    	} 
    	if (sb.length() == 0){
    		sb.append("&nbsp;");
    	}
    		
        return(sb.toString());
    }

    public String sectionLabelHtml(){
    	StringBuilder sb = new StringBuilder();
    	sb.append(this.getCourseOffering(null).getSubjectArea().getSubjectAreaAbbreviation())
    	  .append(" ")
    	  .append(this.getColleagueCourseNumber())
    	  .append(" ")
    	  .append(this.getFirstClass().getSchedulingSubpart().getItype().getSis_ref())
    	  .append(" ")
    	  .append(this.getSectionIndex());
    	  ;
    	return(sb.toString());
    }
    public String buildClassLabelHtml(ClassAssignmentProxy classAssignment){
		StringBuilder sb = new StringBuilder();
		boolean first = true;
    	for (Class_ aClass : getClasses(Class_DAO.getInstance().getSession())){
    		if (first){
    			first = false;
    		} else {
    			sb.append("<br>");
    		}
    		sb.append(aClass.getClassLabel());
	    	if (classAssignment!=null) {
	    		Assignment a = null;
	    		try {
	    			a = classAssignment.getAssignment(aClass);
	    		} catch (Exception e) {
	    			Debug.error(e);
	    		}
	    		if (a!=null) {
	   				if (a.getRoomLocations().size() > 1){
	   					for (int i = 1; i < a.getRoomLocations().size() ; i++){
	   						sb.append("<BR>");
	   					}
	   				}
	     		} 	            
	    	} 
    	} 
    	if (sb.length() == 0){
    		sb.append("&nbsp;");
    	}
    		
        return(sb.toString());
    }

    public String buildInstructorHtml(){
    	Session hibSession = ColleagueSectionDAO.getInstance().getSession();
		TreeMap<DepartmentalInstructor, Integer> instructors = this.findInstructorsWithPercents(hibSession);
		InstructionalOffering io = null;
		try {
			io = getCourseOffering(hibSession).getInstructionalOffering();			
		} catch (Exception e) {
			Debug.error("No instructional offering found for colleague section uniqueId:  " + getUniqueId() + ".");
			// no instructional offering found no course coordinators to be sent.
		}
		boolean first = true;
     	StringBuilder instructorString = new StringBuilder();
     	if (!instructors.isEmpty()){
     		for (DepartmentalInstructor di : instructors.keySet()){
         		if (first){
         			first = false;
         		} else {
         			instructorString.append("<br>");
         		}
     			if (io != null && io.getCoordinators() != null){
     				if (io.getCoordinators().contains(di)){
     					instructorString.append("*");
     				}
     			}
     			Integer pct = instructors.get(di);
     			instructorString.append(di.getName(DepartmentalInstructor.sNameFormatLastInitial) + " (" + pct.toString() +  "%)");
     		}
     	}
     	if (instructorString.length() == 0){
     		instructorString.append("&nbsp;");
     	}
     	return(instructorString.toString());
    }
    @SuppressWarnings("unchecked")
	public String buildAssignedTimeHtml(ClassAssignmentProxy classAssignment){
		StringBuilder sb = new StringBuilder();
		boolean first = true;
    	for (Class_ aClass : getClasses(Class_DAO.getInstance().getSession())){
    		if (first){
    			first = false;
    		} else {
    			sb.append("<br>");
    		}
	    	if (classAssignment!=null) {
	    		Assignment a = null;
	    		try {
	    			a = classAssignment.getAssignment(aClass);
	    		} catch (Exception e) {
	    			Debug.error(e);
	    		}
	    		if (a!=null) {
	   				Enumeration<Integer> e = a.getTimeLocation().getDays();
	   				while (e.hasMoreElements()){
	   					sb.append(Constants.DAY_NAMES_SHORT[e.nextElement()]);
	   				}
	   				sb.append(" ");
	   				sb.append(a.getTimeLocation().getStartTimeHeader(true));
	   				sb.append("-");
	   				sb.append(a.getTimeLocation().getEndTimeHeader(true));
	   				if (a.getRoomLocations().size() > 1){
	   					for (int i = 1; i < a.getRoomLocations().size() ; i++){
	   						sb.append("<BR>");
	   					}
	   				}
	     		}  else {
		    		if (aClass.getEffectiveTimePreferences().isEmpty()){
		    			boolean firstReqRoomPref = true;
		    			for(RoomPref rp : (Set<RoomPref>)aClass.getEffectiveRoomPreferences()){
							if (rp.getPrefLevel().getPrefId().toString().equals(PreferenceLevel.PREF_LEVEL_REQUIRED)){
			    				if (firstReqRoomPref){
			    					firstReqRoomPref = false;
			    				} else {
			    					sb.append("<BR>");
			    				}
							}
						}
		    		}
		    	}   		            
	    	} 
    	} 
    	if (sb.length() == 0){
    		sb.append("&nbsp;");
    	}
    		
        return(sb.toString());
    }
   
    @SuppressWarnings("unchecked")
	public String  buildAssignedRoomHtml(ClassAssignmentProxy classAssignment) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
    	for (Class_ aClass : getClasses(Class_DAO.getInstance().getSession())){
    		if (first){
    			first = false;
    		} else {
    			sb.append("<br>");
    		}
	    	if (classAssignment!=null){
	    		Assignment a = null;
	    		try {
	    			a= classAssignment.getAssignment(aClass);
	    		} catch (Exception e) {
	    			Debug.error(e);
	    		}
	    		if (a!=null) {
	    			boolean brk = false;
		    		for (Location room  : a.getRooms()){
		    			if (brk){
		        			sb.append("<BR>");		    				
		    			} else {
		    				brk = true;
		    			}
		    			sb.append(room.getLabel());
		    		}	
	    		} else {
		    		if (aClass.getEffectiveTimePreferences().isEmpty()){
		    			boolean firstReqRoomPref = true;
		    			for(RoomPref rp : (Set<RoomPref>)aClass.getEffectiveRoomPreferences()){
							if (rp.getPrefLevel().getPrefId().toString().equals(PreferenceLevel.PREF_LEVEL_REQUIRED)){
			    				if (firstReqRoomPref){
			    					firstReqRoomPref = false;
			    				} else {
			    					sb.append("<BR>");
			    				}
								sb.append(rp.getRoom().getLabel());							
							}
						}
		    		}
		    	}   	
	    	} 
    	}
    	if (sb.length() == 0){
    		sb.append("&nbsp;");
    	}
    		
       return(sb.toString());
    }

    @SuppressWarnings("unchecked")
	public String  buildAssignedRoomCapacityHtml(ClassAssignmentProxy classAssignment) {
		StringBuilder sb = new StringBuilder();
		boolean first = true;
		for (Class_ aClass : getClasses(Class_DAO.getInstance().getSession())){
    		if (first){
    			first = false;
    		} else {
    			sb.append("<br>");
    		}
	    	if (classAssignment!=null){
	    		Assignment a = null;
	    		try {
	    			a= classAssignment.getAssignment(aClass);
	    		} catch (Exception e) {
	    			Debug.error(e);
	    		}
	    		if (a!=null) {
		    		boolean brk = false;
		    		for (Location room : a.getRooms()){
		    			if (brk){
		        			sb.append("<BR>");		    				
		    			} else {
		    				brk = true;
		    			}
		    			sb.append(room.getCapacity());
		    		}	
	    		} else {
		    		if (aClass.getEffectiveTimePreferences().isEmpty()){
		    			boolean firstReqRoomPref = true;
		    			for(RoomPref rp : (Set<RoomPref>) aClass.getEffectiveRoomPreferences()){
		    				if (firstReqRoomPref){
		    					firstReqRoomPref = false;
		    				} else {
		    					sb.append("<BR>");
		    				}
							if (rp.getPrefLevel().getPrefId().toString().equals(PreferenceLevel.PREF_LEVEL_REQUIRED)){
								sb.append(rp.getRoom().getCapacity());	
							}
						}
		    		}
		    	}
	    	}
  	
	    	}     	if (sb.length() == 0){
    		sb.append("&nbsp;");
    	}
    		
       return(sb.toString());
    }

    @SuppressWarnings("unchecked")
	public static List<ColleagueSection> findAll(Long sessionId) {
    	return (new ColleagueSectionDAO()).
    		getSession().
    		createQuery("select distinct cs from ColleagueSection cs where " +
    				"cs.session.uniqueId=:sessionId").
    		setLong("sessionId",sessionId.longValue()).
    		list();
    }

    public static List<Class_> findAllClassesForColleagueIdAndTermCode(Integer collegueId, String termCode){
    	return(findAllClassesForColleagueIdAndTermCode((new ColleagueSectionDAO()).getSession(), collegueId, termCode));
    }
    
    @SuppressWarnings("unchecked")
	public static List<Class_> findAllClassesForColleagueIdAndTermCode(Session hibSession, Integer colleagueId, String termCode){
    	return (hibSession.
			createQuery("select distinct c from ColleagueSession csess, ColleagueSection cs inner join cs.colleagueSectionToClasses as cstc, Class_ c where " +
					"cs.session.uniqueId=csess.session.uniqueId and csess.colleagueTermCode = :termCode and cs.colleagueId = :colleagueId and cstc.classId = c.uniqueId").
			setString("termCode",termCode).
			setInteger("colleagueId", colleagueId).
			list());
    }

    public static CourseOffering findCourseOfferingForColleagueIdAndTermCode(Integer colleagueId, String termCode){
    	return(findCourseOfferingForColleagueIdAndTermCode((new ColleagueSectionDAO()).getSession(), colleagueId, termCode));
    }
  
    public static CourseOffering findCourseOfferingForColleagueIdAndTermCode(Session hibSession, Integer colleagueId, String termCode){
    	return ((CourseOffering)hibSession.
			createQuery("select distinct co from ColleagueSession csess, ColleagueSection cs, CourseOffering co where " +
					"cs.session.uniqueId=csess.session.uniqueId and csess.colleagueTermCode = :termCode and cs.colleagueId = :colleagueId and co.uniqueId = cs.courseOfferingId").
			setString("termCode",termCode).
			setInteger("colleagueId", colleagueId).
			uniqueResult());
    }
    
	public String getCampusCode(ColleagueSession colleagueSession,
			Class_ clazz) {
		String campusCode = null;
		ColleagueSuffixDef colleagueSuffixDef = ColleagueSuffixDef
				.findColleagueSuffixDefForTermCode(
						 clazz.getSchedulingSubpart().getItype(), 
						 this.getCourseOffering(ColleagueSectionDAO.getInstance().getSession()), 
						 colleagueSession.getColleagueTermCode(), 
						 ColleagueSuffixDefDAO.getInstance().getCurrentThreadSession());
		if (colleagueSuffixDef.getCampusCode() != null) {
			campusCode = colleagueSuffixDef.getCampusCode();
		} else {
			campusCode = colleagueSession.getColleagueCampus();
		}
		return(campusCode);
		}

	public String getDefaultCampusCode(ColleagueSession colleagueSession,
			Class_ clazz) {
		return(getCampusCode(colleagueSession, clazz));
	}
	
	public CourseOffering getCourseOffering(Session hibSession) {
		if (courseOffering == null && getCourseOfferingId() != null){
			Session querySession = hibSession;
			if (querySession == null){
				querySession = CourseOfferingDAO.getInstance().getSession();
			}
			courseOffering = CourseOfferingDAO.getInstance().get(getCourseOfferingId(), querySession);
		}
		return courseOffering;
	}


	public void setCourseOffering(CourseOffering courseOffering) {
		this.courseOffering = courseOffering;
		setCourseOfferingId(courseOffering.getUniqueId());
		setSubjectArea(courseOffering.getSubjectArea());
	}

	public SubjectArea getSubjectArea(Session hibSession) {
		if (subjectArea == null && getSubjectAreaId() != null){
			Session querySession = hibSession;
			if (querySession == null){
				querySession = SubjectAreaDAO.getInstance().getSession();
			}
			subjectArea = SubjectAreaDAO.getInstance().get(getSubjectAreaId(), querySession);
		}
		return subjectArea;
	}

	public void setSubjectArea(SubjectArea subjectArea) {
		this.subjectArea = subjectArea;
		setSubjectAreaId(subjectArea.getUniqueId());
	}

	@SuppressWarnings("unchecked")
	public static List<ColleagueSection> findColleagueSectionsForCourseOfferingId(
			Long courseOfferingId, Session hibSession) {
		return((List<ColleagueSection>)hibSession.createQuery("from ColleagueSection cs where cs.courseOfferingId = :crsId").setLong("crsId", courseOfferingId).list());
	}

	/**
	 * Delete all restrictions
	 * @param hibSession
	 */
	public void deleteRestrictions(org.hibernate.Session hibSession) {
		Set<ColleagueRestriction> s = new HashSet<ColleagueRestriction>();
		s.addAll(this.getRestrictions());
		//deleteObjectsFromCollection(hibSession, s);
		if (s==null || s.size()==0) return;

		for (ColleagueRestriction cr : s) {
			this.getRestrictions().remove(cr);
			hibSession.saveOrUpdate(this);
		}
	}

	private static String calculateColleagueCourseNumber(CourseOffering courseOffering, Class_ cls) {
		if (courseOffering.getCourseNbr().contains("-")){
			return(courseOffering.getCourseNbr().substring(0, courseOffering.getCourseNbr().indexOf("-")));			
		}
		return(courseOffering.getCourseNbr());
		
	}
	public static void addColleagueSectionFor(CourseOffering courseOffering, Class_ cls, Session hibSession){
		
		ColleagueSection cs = new ColleagueSection();
		cs.setSubjectAreaId(courseOffering.getSubjectArea().getUniqueId());
		cs.setColleagueCourseNumber(calculateColleagueCourseNumber(courseOffering, cls));;
		cs.setCourseOffering(courseOffering);
		cs.setCourseOfferingId(courseOffering.getUniqueId());
		try {
			cs.setSectionIndex(ColleagueSection.findNextUnusedSectionIndexFor(courseOffering.getInstructionalOffering().getSession(), 
					courseOffering, 
					cls.getSchedulingSubpart().getItype(), 
					hibSession));
		} catch (Exception e) {
			e.printStackTrace();
		}
		cs.setDeleted(new Boolean(false));
		cs.setSession(courseOffering.getInstructionalOffering().getSession());
		cs.addClass(cls, hibSession);

		
		Transaction trans = null;
		boolean alreadyInTransaction = false;
		if (hibSession.getTransaction() != null) {
			alreadyInTransaction = true;
			trans = hibSession.getTransaction();
		} else {
			trans = hibSession.beginTransaction();
		}
		hibSession.saveOrUpdate(cs);
		if (!alreadyInTransaction) {
			trans.commit();
		}
		hibSession.flush();
	}

	public boolean isCanceled(Session hibSession){
		boolean allClassesCanceled = true;
		for(Class_ c : this.getClasses(hibSession)){
			if (!c.isCancelled().booleanValue()){
				allClassesCanceled = false;
				break;
			}
		}
		return(allClassesCanceled);
	}


}