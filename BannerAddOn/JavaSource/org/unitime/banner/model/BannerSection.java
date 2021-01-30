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

package org.unitime.banner.model;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.hibernate.engine.spi.SessionImplementor;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.interfaces.ExternalBannerCampusCodeElementHelperInterface;
import org.unitime.banner.interfaces.ExternalBannerSubjectAreaElementHelperInterface;
import org.unitime.banner.model.base.BaseBannerSection;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.banner.model.dao.BannerSectionDAO;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.banner.util.BannerCrnValidator;
import org.unitime.banner.util.DefaultExternalBannerCampusCodeElementHelper;
import org.unitime.banner.util.DefaultExternalBannerSubjectAreaElementHelper;
import org.unitime.commons.Debug;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.gwt.resources.GwtConstants;
import org.unitime.timetable.model.Assignment;
import org.unitime.timetable.model.ClassInstructor;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.DepartmentalInstructor;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Location;
import org.unitime.timetable.model.OfferingConsentType;
import org.unitime.timetable.model.OfferingCoordinator;
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.TeachingResponsibility;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao.InstrOfferingConfigDAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.Constants;

/**
 * 
 * @author says
 *
 */
public class BannerSection extends BaseBannerSection {
	protected static GwtConstants CONSTANTS = Localization.create(GwtConstants.class);
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -7327253389631092870L;
	private static ExternalBannerCampusCodeElementHelperInterface externalCampusCodeElementHelper;
	private static ExternalBannerSubjectAreaElementHelperInterface externalSubjectAreaElementHelper;


	/*[CONSTRUCTOR MARKER BEGIN]*/
	public BannerSection () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public BannerSection (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/

	private HashSet<Class_> classes;
	
	public void addClass(Class_ clazz, Session hibSession){
		if (clazz == null || clazz.getUniqueId() == null){
			return;
		}
		if (getBannerSectionToClasses() != null && !getBannerSectionToClasses().isEmpty()){
			initClassesIfNecessary(hibSession, clazz);
		}
		BannerSectionToClass bsc = new BannerSectionToClass();
		bsc.setBannerSection(this);
		bsc.setClassId(clazz.getUniqueId());
		addTobannerSectionToClasses(bsc);
		if (classes == null){
			classes = new HashSet<Class_>();
		}
		classes.add(clazz);
	}
	
	public void removeClass(Class_ clazz, Session hibSession){
		if (clazz == null || clazz.getUniqueId() == null){
			return;
		}
		removeClassId(clazz.getUniqueId(), hibSession);
	}

	public void removeClassId(Long classId, Session hibSession){
		if (classId == null){
			return;
		}
		if (getBannerSectionToClasses() != null && !getBannerSectionToClasses().isEmpty()){
			initClassesIfNecessary(hibSession, null);
		}
		BannerSectionToClass bscToRemove = null;
		for (BannerSectionToClass bsc : getBannerSectionToClasses()) {
			if (bsc.getClassId() != null && classId != null && bsc.getClassId().equals(classId)) {
				bscToRemove = bsc;
				break;
			}
		}
		if (bscToRemove != null) {
			getBannerSectionToClasses().remove(bscToRemove);
		}
	}

	
	private void initClassesIfNecessary(Session hibSession, Class_ clazz){
		if (classes == null){
			classes = new HashSet<Class_>();
			if (getBannerSectionToClasses() != null && !getBannerSectionToClasses().isEmpty()){
				Session querySession;
				if (hibSession == null){
					querySession = Class_DAO.getInstance().getSession();
				} else {
					querySession = hibSession;
				}
				for(BannerSectionToClass bsc : getBannerSectionToClasses()){
					if (clazz != null && bsc.getClassId().equals(clazz.getUniqueId())){
						classes.add(clazz);
					} else {
						Class_ c = (Class_)querySession.createQuery("from Class_ c where c.uniqueId = :classId").setLong("classId", bsc.getClassId().longValue()).setFlushMode(FlushMode.MANUAL).uniqueResult();
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


	public static BannerSection findBannerSectionForClassAndCourseExternalId(Class_ clazz, String courseExternalId, Session hibSession, org.unitime.timetable.model.Session acadSession){
		return((BannerSection)hibSession
				.createQuery("select bs from BannerSection bs inner join bs.bannerSectionToClasses as bsc where bs.bannerConfig.bannerCourse.courseOfferingId = " +
						" (select co.uniqueId from CourseOffering co where co.externalUniqueId = :courseExternalId and co.instructionalOffering.session.uniqueId = :sessionId)" +
						" and bsc.classId = :classId")
				.setLong("classId", clazz.getUniqueId().longValue())
				.setLong("sessionId", acadSession.getUniqueId().longValue())
				.setString("courseExternalId", courseExternalId)
				.setFlushMode(FlushMode.MANUAL)
				.setCacheable(false)
				.uniqueResult());
	}
	
	@SuppressWarnings("unchecked")
	public static List<BannerSection> findBannerSectionsForInstructionalOffering(InstructionalOffering instructionalOffering, Session hibSession){
		return((List<BannerSection>) hibSession.createQuery("select bs from BannerSection bs, CourseOffering co where co.instructionalOffering = :instrOfferId and bs.bannerConfig.bannerCourse.courseOfferingId = co.uniqueId")
				           .setLong("instrOfferId", instructionalOffering.getUniqueId().longValue())
				           .setFlushMode(FlushMode.MANUAL)
				           .list());
	}
		
	public boolean isNestedSection(){
		return(getBannerSectionToClasses() != null && getBannerSectionToClasses().size() > 1);
	}
	
	private int countBannerSectionsFor(Long classId, Session hibSession){
		if (classId == null){
			return(0);
		}
		Session querySession = hibSession;
		if (querySession == null){
			querySession = Class_DAO.getInstance().getSession();
		}
		return((Long)querySession.createQuery("select count(bsc) from BannerSectionToClass bsc where bsc.classId = :classId").setLong("classId", classId.longValue()).setFlushMode(FlushMode.MANUAL).uniqueResult()).intValue();
		
	}

	public boolean isCrossListedSection(Session hibSession){
		boolean isCrossListed = false;
		for (BannerSectionToClass bsc : getBannerSectionToClasses()){
			if (countBannerSectionsFor(bsc.getClassId(), hibSession) > 1){
				isCrossListed = true;
			}			
		}
		return(isCrossListed);
	}
	
	public int calculateMaxEnrl(Session hibSession) {
		if (isCrossListedSection(hibSession) && getOverrideLimit() != null){
			return(getOverrideLimit().intValue());
		} else {
			return(maxEnrollBasedOnClasses(hibSession));
		}
	}
	
	public String courseCreditStringBasedOnClass(Class_ clazz){
    	Class_ c = clazz;
    	if (c == null){
    		c = getFirstClass();
    	}
    	if (c == null){
    		return("");
    	}
    	return(courseCreditStringBasedOnSchedulingSubpart(c.getSchedulingSubpart()));
	}
	
	public String courseCreditStringBasedOnClasses(){
		return(courseCreditStringBasedOnClass(getFirstClass()));
	}

	public String bannerCourseCreditStr(){
		return(bannerCourseCreditStr(getFirstClass()));
	}
	
	public String bannerCourseCreditStr(Class_ clazz){
		Class_ c = clazz;
		if (c == null) {
			c = getFirstClass();
		}
		return(bannerCourseCreditStr(c.getSchedulingSubpart()));
	}
	
	public String bannerCourseCreditStr(SchedulingSubpart schedSubpart){
		String credit = "";
		
     	if (getBannerConfig().getGradableItype() != null){
	     	if (schedSubpart != null && schedSubpart.getItype().getItype().equals(getBannerConfig().getGradableItype().getItype())){
	     		if (getOverrideCourseCredit() != null){
	     			credit = getOverrideCourseCredit().toString();
	     		} else {
					credit = courseCreditStringBasedOnSchedulingSubpart(schedSubpart);
	     		}
			} else {
				credit = "0.0";
			}
     	}
     	return(credit);
	}

	
	private String courseCreditStringBasedOnSchedulingSubpart(
			SchedulingSubpart schedSubpart) {

    	String credit = "";
    	if (getBannerConfig().getGradableItype() != null && schedSubpart != null) {
    		// Get course offering for this banner section
    		CourseOffering course = null;
    		for (CourseOffering co: schedSubpart.getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings()) {
    			if (co.getUniqueId().equals(getBannerConfig().getBannerCourse().getCourseOfferingId())) {
    				course = co; break;
    			}
    		}
    		if (course == null)
    			course = schedSubpart.getControllingCourseOffering();
    		// Get course credit
    		if (course.getCredit() != null && course.getCredit() instanceof FixedCreditUnitConfig) {
    			if (schedSubpart.getItype().getItype().equals(getBannerConfig().getGradableItype().getItype())) {
    				FixedCreditUnitConfig fixed = (FixedCreditUnitConfig) course.getCredit();
    				credit = fixed.getFixedUnits().toString();
    			} else {
    				credit = "0.0";
    			}
    		}
    	}
		return(credit);
	
	}

	public Class_ getFirstClass() {
		Class_ c = null;
		if (getBannerSectionToClasses() != null && !getBannerSectionToClasses().isEmpty()){
			BannerSectionToClass bsc = (BannerSectionToClass)getBannerSectionToClasses().iterator().next();
			c = Class_DAO.getInstance().get(bsc.getClassId());
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

	public static Integer findNextUnusedCrnFor(org.unitime.timetable.model.Session acadSession, Session hibSession) {
		boolean crnIsUsed = true;
		String crnIsUsedStr = null;
		Integer crn = null;
		BannerCrnValidator bannerCrnValidator = new BannerCrnValidator();
		BannerSession bs = BannerSession.findBannerSessionForSession(acadSession, hibSession);
		do {
			crn = findNextUnusedCrnInUniTimeFor(acadSession, hibSession);
			if (crn == null){
				break;
			}
			try {
				crnIsUsedStr = bannerCrnValidator.isCrnUsedInBannerForTerm(crn, bs.getBannerTermCode());
				if (crnIsUsedStr == null){
					Debug.error("Failed to validate whether new CRN: " + crn.toString() + " already exists in Banner");					
					crn = null;
					break;
				}
				crnIsUsed = !"N".equals(crnIsUsedStr);
			} catch (Exception e) {
				Debug.error("Failed to validate whether new CRN: " + crn.toString() + " already exists in Banner");
				e.printStackTrace();
				crn = null;
				break;
			}
		} while (crnIsUsed);
        return(crn);
	}
		
	
	public static Integer findNextUnusedCrnInUniTimeFor(org.unitime.timetable.model.Session acadSession, Session hibSession) {
		Integer nextCrn = null;
		@SuppressWarnings("rawtypes")
		SessionImplementor session = (SessionImplementor)new _RootDAO().getSession();
		Connection connection = null;
	   	try {
    		String nextCrnSql = ApplicationProperties.getProperty("banner.crn.generator","{?=call timetable.crn_processor.get_crn(?)}");
            connection = session.getJdbcConnectionAccess().obtainConnection();
            CallableStatement call = connection.prepareCall(nextCrnSql);
            call.registerOutParameter(1, java.sql.Types.INTEGER);
            call.setLong(2, acadSession.getUniqueId().longValue());
            call.execute();
            nextCrn = call.getInt(1);
            call.close();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					session.getJdbcConnectionAccess().releaseConnection(connection);
			} catch (SQLException e) {}
		}
		return(nextCrn);
	}
	
	
	public static boolean isSectionIndexUniqueForCourse(org.unitime.timetable.model.Session acadSession, CourseOffering courseOffering,
			Session hibSession, String sectionId) {
		int sectionExists = 0;
		@SuppressWarnings("rawtypes")
		SessionImplementor session = (SessionImplementor)new _RootDAO().getSession();
        Connection connection = null;
	   	try {
    		String sectionExistsSql = ApplicationProperties.getProperty("banner.section_id.validator");
    		BannerSession bs = BannerSession.findBannerSessionForSession(acadSession, hibSession);	
    		String subject = getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(courseOffering.getSubjectArea(), bs);
            connection = session.getJdbcConnectionAccess().obtainConnection();
            CallableStatement call = connection.prepareCall(sectionExistsSql);
            call.registerOutParameter(1, java.sql.Types.INTEGER);
            call.setLong(2, acadSession.getUniqueId().longValue());
            call.setString(3, subject);
            call.setString(4, courseOffering.getCourseNbr());
            call.setString(5, sectionId);
            call.execute();
            sectionExists = call.getInt(1);
            call.close();
            session.getJdbcConnectionAccess().releaseConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					session.getJdbcConnectionAccess().releaseConnection(connection);
			} catch (SQLException e) {}
		}
		return(sectionExists != 1);
	}
	
	public static String findNextUnusedSectionIndexFor(org.unitime.timetable.model.Session acadSession, CourseOffering courseOffering,
			Session hibSession) {
		String nextSectionId = null;
		@SuppressWarnings("rawtypes")
		SessionImplementor session = (SessionImplementor)new _RootDAO().getSession();
		Connection connection = null;
		try {
//    		String nextSectionIdSql = ApplicationProperties.getProperty("banner.section_id.generator","{?= call timetable.section_processor.get_section(?,?,?)}");
		BannerSession bs = BannerSession.findBannerSessionForSession(acadSession, hibSession);	
		String subject = getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(courseOffering.getSubjectArea(), bs);
    		String nextSectionIdSql = ApplicationProperties.getProperty("banner.section_id.generator");
            connection = session.getJdbcConnectionAccess().obtainConnection();
            CallableStatement call = connection.prepareCall(nextSectionIdSql);
            call.registerOutParameter(1, java.sql.Types.VARCHAR);
            call.setLong(2, acadSession.getUniqueId().longValue());
            call.setString(3, subject);
            call.setString(4, courseOffering.getCourseNbr());
            call.execute();
            nextSectionId = call.getString(1);
            call.close();
            session.getJdbcConnectionAccess().releaseConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					session.getJdbcConnectionAccess().releaseConnection(connection);
			} catch (SQLException e) {}
		}

		return(nextSectionId);
	}

	public static String findNextUnusedLinkIdentifierFor(org.unitime.timetable.model.Session acadSession, CourseOffering courseOffering, Session hibSession) {
		String nextLinkId = null;
		@SuppressWarnings("rawtypes")
		SessionImplementor session = (SessionImplementor)new _RootDAO().getSession();
		Connection connection = null;
	   	try {
			BannerSession bs = BannerSession.findBannerSessionForSession(acadSession, hibSession);	
			String subject = getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(courseOffering.getSubjectArea(), bs);
    		String nextLinkIdSql = ApplicationProperties.getProperty("banner.link_id.generator","{?= call timetable.section_processor.get_link_identifier(?,?,?)}");
            connection = session.getJdbcConnectionAccess().obtainConnection();
            CallableStatement call = connection.prepareCall(nextLinkIdSql);
            call.registerOutParameter(1, java.sql.Types.VARCHAR);
            call.setLong(2, acadSession.getUniqueId().longValue());
            call.setString(3, subject);
            call.setString(4, courseOffering.getCourseNbr());
            call.execute();
            nextLinkId = call.getString(1);
            call.close();
            session.getJdbcConnectionAccess().releaseConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				if (connection != null)
					session.getJdbcConnectionAccess().releaseConnection(connection);
			} catch (SQLException e) {}
		}

		return(nextLinkId);
	}
	
	@SuppressWarnings("unchecked")
	public static List<BannerSection> findBannerSectionsForClass(Class_ clazz, Session hibSession) {
		return((List<BannerSection>)hibSession
			.createQuery("select distinct bsc.bannerSection from BannerSectionToClass as bsc where bsc.classId = :classId")
			.setLong("classId", clazz.getUniqueId().longValue())
			.setFlushMode(FlushMode.MANUAL)
			.list());
	}

	@SuppressWarnings("unchecked")
	public static List<BannerSection> findBannerSectionsForInstrOfferingConfig(InstrOfferingConfig instrOfferingConfig, Session hibSession) {
		return((List<BannerSection>)hibSession
			.createQuery("select distinct bsc.bannerSection from BannerSectionToClass as bsc, Class_ c where c.schedulingSubpart.instrOfferingConfig.uniqueId = :configId and bsc.classId = c.uniqueId")
			.setLong("configId", instrOfferingConfig.getUniqueId().longValue())
			.setFlushMode(FlushMode.MANUAL)
			.list());
	}

	@SuppressWarnings("unchecked")
	public static List<BannerSection> findBannerSectionsForSchedulingSubpart(SchedulingSubpart schedulingSubpart, Session hibSession) {
		return((List<BannerSection>)hibSession
			.createQuery("select distinct bsc.bannerSection from BannerSectionToClass as bsc, Class_ c where c.schedulingSubpart.uniqueId = :subpartId and bsc.classId = c.uniqueId")
			.setLong("subpartId", schedulingSubpart.getUniqueId().longValue())
			.setFlushMode(FlushMode.MANUAL)
			.list());
	}

	@SuppressWarnings("unchecked")
	public static List<BannerSection> findBannerSectionsForSolution(
			Solution solution, Session hibSession) {
		Vector<BannerSection> hs = new Vector<BannerSection>();
		List<BannerSection> sections = hibSession
		.createQuery("select distinct bsc.bannerSection from BannerSectionToClass as bsc, Assignment a where a.solution.uniqueId = :solutionId and bsc.classId = a.clazz.uniqueId")
		.setLong("solutionId", solution.getUniqueId().longValue())
		.setFlushMode(FlushMode.MANUAL)
		.setCacheable(false)
		.list();
		for(BannerSection bs : sections){
			hs.add(bs);
		}
		return(hs);
	}

	@SuppressWarnings("unchecked")
	public static void removeOrphanedBannerSections(Session hibSession){
		String orphanedBannerCoursesQuery = "select distinct bs from BannerSection bs where bs.bannerConfig.bannerCourse.courseOfferingId not in ( select co.uniqueId from CourseOffering co )";
		HashSet<BannerConfig> parentList = new HashSet<BannerConfig>();
		String orphanedBannerSectionsQuery = "select distinct bsc.bannerSection from BannerSectionToClass bsc where bsc.classId not in ( select c.uniqueId from Class_ c )";
		HashSet<BannerCourse> orphanedCourses = new HashSet<BannerCourse>();

		Transaction trans = hibSession.beginTransaction();
		List<BannerSection> orphanedBannerCourses = (List<BannerSection>) hibSession.createQuery(orphanedBannerCoursesQuery)
		.setFlushMode(FlushMode.MANUAL)
		.list();
		for (BannerSection bs : orphanedBannerCourses){
			SendBannerMessage.sendBannerMessage(bs, BannerMessageAction.DELETE, hibSession);
			orphanedCourses.add(bs.getBannerConfig().getBannerCourse());
		}
		for(BannerCourse bc : orphanedCourses){
			hibSession.delete(bc);
			hibSession.flush();
		}
		List<BannerSection> orphanedBannerSections = (List<BannerSection>) hibSession.createQuery(orphanedBannerSectionsQuery)
				.setFlushMode(FlushMode.MANUAL)
				.list();
		for(BannerSection bs : orphanedBannerSections){
			if (bs.getClasses(hibSession).size() > 0) {
				Debug.info("removing deleted classes from banner section");
				HashSet<BannerSectionToClass> l = new HashSet<BannerSectionToClass>();
				l.addAll(bs.getBannerSectionToClasses());
				for (BannerSectionToClass bstc : l) {
					if (bstc.getClassId() == null) {
						bs.getBannerSectionToClasses().remove(bstc);
						hibSession.update(bs);
					} else {
						Class_ c = Class_DAO.getInstance().get(bstc.getClassId(), hibSession);
						if (c == null) {
							bs.getBannerSectionToClasses().remove(bstc);
							hibSession.update(bs);
						}
					}
				}
			} else {
				Debug.info("removing orphaned banner section");
				SendBannerMessage.sendBannerMessage(bs, BannerMessageAction.DELETE, hibSession);
				parentList.add(bs.getBannerConfig());
				if (bs.getParentBannerSection() != null){
					bs.getParentBannerSection().getBannerSectionToChildSections().remove(bs);
				}
				if (bs.getBannerSectionToChildSections() != null && !bs.getBannerSectionToChildSections().isEmpty()){
					for(BannerSection cBs : (Set<BannerSection>)bs.getBannerSectionToChildSections()){
						cBs.setParentBannerSection(null);
					}
				}
				bs.getBannerConfig().getBannerSections().remove(bs);
			}
		}
		for(BannerConfig bc : parentList){		
				hibSession.update(bc);
		}

		trans.commit();
	}

	public static BannerSection findBannerSectionForClassAndCourseOffering(
			Class_ clazz, CourseOffering courseOffering, Session hibSession) {
		if (clazz == null || courseOffering == null){
			return(null);
		}
		return((BannerSection)hibSession
				.createQuery("select distinct bsc.bannerSection from BannerSectionToClass as bsc where bsc.bannerSection.bannerConfig.bannerCourse.courseOfferingId = :courseOfferingId " +
						" and bsc.classId = :classId")
				.setLong("classId", clazz.getUniqueId().longValue())
				.setLong("courseOfferingId", courseOffering.getUniqueId().longValue())
				.setFlushMode(FlushMode.MANUAL)
				.setCacheable(false)
				.uniqueResult());

	}
	
	public static BannerSection findBannerSectionForClassAndCourseOfferingCacheable(
			Class_ clazz, CourseOffering courseOffering, Session hibSession) {
		if (clazz == null || courseOffering == null){
			return(null);
		}
		return((BannerSection)hibSession
				.createQuery("select distinct bsc.bannerSection from BannerSectionToClass as bsc where bsc.bannerSection.bannerConfig.bannerCourse.courseOfferingId = :courseOfferingId " +
						" and bsc.classId = :classId")
				.setLong("classId", clazz.getUniqueId().longValue())
				.setLong("courseOfferingId", courseOffering.getUniqueId().longValue())
				.setFlushMode(FlushMode.MANUAL)
				.setCacheable(true)
				.uniqueResult());

	}
	
	public static boolean displayLabHours(){

		return("true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.lab_hours.display", "false")));

	}
	
	public String bannerSectionLabel(){
		CourseOffering co = getBannerConfig().getBannerCourse().getCourseOffering(null);
		StringBuilder sb = new StringBuilder();
		sb.append(co.getSubjectAreaAbbv())
		  .append(" ")
		  .append(co.getCourseNbr())
		  .append(" ")
		  .append(getCrn() == null?"<not set>":getCrn().toString());
		return(sb.toString());
	}
	
	@SuppressWarnings("unchecked")
	public List<BannerSection> bannerSectionsCrosslistedWithThis(){
		if (!getClasses(null).isEmpty()){
		Class_ c = (Class_)getClasses(null).iterator().next();
		String qs = "select distinct bsc.bannerSection from BannerSectionToClass bsc where bsc.class_id = :classId and bsc.bannerSection.uniqueId != :sectionId";
		return(BannerSectionDAO.getInstance()
				               .getQuery(qs)
				               .setLong("classId", c.getUniqueId().longValue())
				               .setLong("sectionId", getUniqueId().longValue())
				               .list());
		} else {
			return(new Vector<BannerSection>());
		}
		
	}
	
	public String consentLabel(){
		OfferingConsentType oct = effectiveConsentType();
		if (oct != null){
			return(oct.getReference());
		} else {
			return(" ");
		}
		
	}
	
	public String classSuffixFor(Class_ clazz, Session hibSession){
		if (this.getCrn() == null || this.getSectionIndex() == null){
			if (clazz != null && clazz.getClassSuffix() != null){
				return(clazz.getClassSuffix());
			} else {
				return("");
			}
		}
		StringBuilder sb = new StringBuilder();
		sb.append(this.getCrn().toString())
		  .append("-")
		  .append(this.getSectionIndex().trim());
		
		if (clazz != null && clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings() != null && clazz.getSchedulingSubpart().getInstrOfferingConfig().getInstructionalOffering().getCourseOfferings().size() > 1){
			sb.append("*");
		}
		return(sb.toString());
	}
	
	public String externalUniqueIdFor(Class_ clazz, Session hibSession){
		if (this.getCrn() == null){
			if (clazz != null && clazz.getExternalUniqueId() != null){
				return(clazz.getExternalUniqueId());
			} else {
				return("");
			}
		}

		return(this.getCrn().toString());
	}

	public void assignNewSectionIndex(Session hibSession){
		this.setSectionIndex(BannerSection.findNextUnusedSectionIndexFor(this.getSession(), this.getBannerConfig().getBannerCourse().getCourseOffering(hibSession), hibSession));
		hibSession.update(this);
		updateClassSuffixForClassesIfNecessary(hibSession);
	}
	
	public void assignNewCrn(Session hibSession){	
		this.setCrn(BannerSection.findNextUnusedCrnFor(this.getSession(), hibSession));
		hibSession.update(this);
		updateClassSuffixForClassesIfNecessary(hibSession);
	}
	
	public void updateClassSuffixForClassesIfNecessary(Session hibSession){
		
		updateClassSuffixForClassesIfNecessaryRefreshClasses(hibSession, false);

	}
	
	public void updateClassSuffixForClassesIfNecessaryRefreshClasses(Session hibSession, boolean refresh){
		Boolean control = this.getBannerConfig().getBannerCourse().getCourseOffering(hibSession).isIsControl();
		if (control.booleanValue()){
			for (BannerSectionToClass bsc : this.getBannerSectionToClasses()){
				Class_ clazz = Class_DAO.getInstance().get(bsc.getClassId(), hibSession);
				if (clazz != null){
					String classSuffix = this.classSuffixFor(clazz, hibSession);
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

	public static BannerSection findBannerSectionForBannerCourseAndClass(
			BannerCourse bannerCourse, Class_ clazz) {
		BannerSectionDAO bsDao = new BannerSectionDAO();
		if (clazz == null || bannerCourse == null){
			return(null);
		}
		String qs = "select distinct bsc.bannerSection from BannerSectionToClass bsc where bsc.classId = :classId and bsc.bannerSection.bannerConfig.bannerCourse.uniqueId = :bannerCourseId";
		return((BannerSection)bsDao.getQuery(qs)
		     .setLong("classId", clazz.getUniqueId().longValue())
		     .setLong("bannerCourseId", bannerCourse.getUniqueId().longValue())
		     .setFlushMode(FlushMode.MANUAL)
		     .uniqueResult());
	}
	
	public OfferingConsentType effectiveConsentType(){
		if (getConsentType() != null){
			return(getConsentType());
		}
		return(getBannerConfig().getBannerCourse().getCourseOffering(BannerCourseDAO.getInstance().getSession()).getConsentType());
	}
	
	public TreeMap<DepartmentalInstructor, Integer> findInstructorsWithPercents(Session hibSession){

		int numClassesWithInstructors = 0;
		TreeMap<DepartmentalInstructor, Integer> instructorPercents = new TreeMap<DepartmentalInstructor, Integer>();

		// Include course coordinators in the instructor list with a percentage of 0 unless they are on a class with a higher percentage.
		InstructionalOffering io = null;
		try {
			io = getBannerConfig().getBannerCourse().getCourseOffering(hibSession).getInstructionalOffering();			
		} catch (Exception e) {
			Debug.error("No instructional offering found for banner section uniqueId:  " + getUniqueId() + ".");
			// no instructional offering found no course coordinators to be sent.
		}
		if (io != null && io.getOfferingCoordinators() != null){
			for (OfferingCoordinator oc : io.getOfferingCoordinators()){
				if (oc.getResponsibility() == null || !oc.getResponsibility().hasOption(TeachingResponsibility.Option.noexport)) {
						instructorPercents.put(oc.getInstructor(), new Integer(0));
				}
			}
		}
		boolean sendInstructors = false;
		for(Class_ c : this.getClasses(hibSession)){
			if (c.isCancelled().booleanValue()){
				continue;
			}
			if (c.getCommittedAssignment() != null || c.getEffectiveTimePreferences().isEmpty()){
				sendInstructors = true;
				if (c.getClassInstructors() != null && !c.getClassInstructors().isEmpty()){
					int totalPercent = 0;
					int instructorCount = 0;
					for(ClassInstructor ci : c.getClassInstructors()) {
						if (ci.getResponsibility() == null || !ci.getResponsibility().hasOption(TeachingResponsibility.Option.noexport)) {
							if (ci.getResponsibility() == null || !ci.getResponsibility().hasOption(TeachingResponsibility.Option.auxiliary)){
								totalPercent += (ci.getPercentShare() != null?(ci.getPercentShare().intValue() < 0?-1*ci.getPercentShare().intValue():ci.getPercentShare().intValue()):0);
								instructorCount++;
							}
						}
					}
					if (instructorCount > 0){
						numClassesWithInstructors++;
					} else {
						continue;
					}
					for(ClassInstructor ci : c.getClassInstructors()) {
						if (ci.getResponsibility() == null || !ci.getResponsibility().hasOption(TeachingResponsibility.Option.noexport)) {
							if (ci.getInstructor().getExternalUniqueId() != null){
								if ((ci.getResponsibility() == null || !ci.getResponsibility().hasOption(TeachingResponsibility.Option.auxiliary)) && instructorPercents.containsKey(ci.getInstructor()) && totalPercent > 0){
									int pct = instructorPercents.get(ci.getInstructor()).intValue();
									pct += (ci.getPercentShare() != null?(((ci.getPercentShare().intValue() < 0?-1*ci.getPercentShare().intValue():ci.getPercentShare().intValue())*100/totalPercent)):0);
									instructorPercents.put(ci.getInstructor(),new Integer(pct));
								} else {
									if ((ci.getResponsibility() == null || !ci.getResponsibility().hasOption(TeachingResponsibility.Option.auxiliary)) && totalPercent > 0)
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
		}
		if (!sendInstructors){
			instructorPercents.clear();
		}
		if (!instructorPercents.isEmpty() && numClassesWithInstructors > 0){
			int totalPct = 0;
			DepartmentalInstructor firstInstructorWithNonZeroPercent = null;
			for(DepartmentalInstructor instructor : instructorPercents.keySet()){
				int pct = instructorPercents.get(instructor).intValue()/numClassesWithInstructors;
				instructorPercents.put(instructor, new Integer(pct));
				if (pct > 0 && firstInstructorWithNonZeroPercent == null){
					firstInstructorWithNonZeroPercent = instructor;
				}
				totalPct += pct;
			}
			if (totalPct != 100 && firstInstructorWithNonZeroPercent != null){
				int pct = instructorPercents.get(firstInstructorWithNonZeroPercent).intValue() + (100 - totalPct);
				instructorPercents.put(firstInstructorWithNonZeroPercent, new Integer(pct));
			}
		}
		return(instructorPercents);
	
	}

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
		    			for(@SuppressWarnings("rawtypes")
						Iterator rmPrefIt = aClass.getEffectiveRoomPreferences().iterator(); rmPrefIt.hasNext();){
							RoomPref rp = (RoomPref) rmPrefIt.next();
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
    
    public String buildRestrictionHtml() {
    		StringBuilder restrictionString = new StringBuilder();
    		ArrayList<BannerCohortRestriction> cohortRestrictions = getAllBannerCohortRestrictions(BannerSessionDAO.getInstance().getSession());
    		if (!cohortRestrictions.isEmpty()) {
	    		ArrayList<BannerCohortRestriction> activeCohortRestrictions = new ArrayList<BannerCohortRestriction>();
	    		ArrayList<BannerCohortRestriction> removedCohortRestrictions = new ArrayList<BannerCohortRestriction>();
	    		
	    		for (BannerCohortRestriction bcr : cohortRestrictions) {
	    			if (bcr.getRemoved()) {
	    				removedCohortRestrictions.add(bcr);
	    			} else {
	    				activeCohortRestrictions.add(bcr);
	    			}
	    		}
	    		boolean first = true;
	    		for ( BannerCohortRestriction bcr : activeCohortRestrictions) {
	    			if (first) {
	    				first = false;
	    			} else {
	    				restrictionString.append("<BR>");
	    			}
		    		restrictionString.append(bcr.restrictionText());
	    		}
	    		if ("true".equals(ApplicationProperties.getProperty("banner.menu.display_inst_method_restrictions_show_removed","false"))) {
	    			for ( BannerCohortRestriction bcr : removedCohortRestrictions) {
		    			if (first) {
		    				first = false;
		    			} else {
		    				restrictionString.append("<BR>");
		    			}
			    		restrictionString.append(bcr.restrictionText());
		    		}
	    		}
    		}
    		return(restrictionString.toString());
    }

    public String buildInstructorHtml(){
    	Session hibSession = BannerSectionDAO.getInstance().getSession();
		TreeMap<DepartmentalInstructor, Integer> instructors = this.findInstructorsWithPercents(hibSession);
		InstructionalOffering io = null;
		try {
			io = getBannerConfig().getBannerCourse().getCourseOffering(hibSession).getInstructionalOffering();			
		} catch (Exception e) {
			Debug.error("No instructional offering found for banner section uniqueId:  " + getUniqueId() + ".");
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
     			if (io != null && io.getOfferingCoordinators() != null){
     				for (OfferingCoordinator oc: io.getOfferingCoordinators()) {
     					if (oc.getInstructor().equals(di)) {
     						instructorString.append("*");
     						break;
     					}
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
	   				sb.append(a.getTimeLocation().getStartTimeHeader(CONSTANTS.useAmPm()));
	   				sb.append("-");
	   				sb.append(a.getTimeLocation().getEndTimeHeader(CONSTANTS.useAmPm()));
	   				if (a.getRoomLocations().size() > 1){
	   					for (int i = 1; i < a.getRoomLocations().size() ; i++){
	   						sb.append("<BR>");
	   					}
	   				}
	     		}  else {
		    		if (aClass.getEffectiveTimePreferences().isEmpty()){
		    			boolean firstReqRoomPref = true;
		    			for(@SuppressWarnings("rawtypes")
						Iterator rmPrefIt = aClass.getEffectiveRoomPreferences().iterator(); rmPrefIt.hasNext();){
							RoomPref rp = (RoomPref) rmPrefIt.next();
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
					Iterator<Location> it2 = a.getRooms().iterator();
		    		while (it2.hasNext()){
		    			Location room = (Location)it2.next();
		    			sb.append(room.getLabel());
		    			if (it2.hasNext()){
		        			sb.append("<BR>");
		        		} 
		    		}	
	    		} else {
		    		if (aClass.getEffectiveTimePreferences().isEmpty()){
		    			boolean firstReqRoomPref = true;
		    			for(@SuppressWarnings("rawtypes")
						Iterator rmPrefIt = aClass.getEffectiveRoomPreferences().iterator(); rmPrefIt.hasNext();){
							RoomPref rp = (RoomPref) rmPrefIt.next();
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
		    		Iterator<Location> it2 = a.getRooms().iterator();
		    		while (it2.hasNext()){
		    			Location room = (Location)it2.next();
		    			sb.append(room.getCapacity());
		    			if (it2.hasNext()){
		        			sb.append("<BR>");
		        		} 
		    		}	
	    		} else {
		    		if (aClass.getEffectiveTimePreferences().isEmpty()){
		    			boolean firstReqRoomPref = true;
		    			for(@SuppressWarnings("rawtypes")
						Iterator rmPrefIt = aClass.getEffectiveRoomPreferences().iterator(); rmPrefIt.hasNext();){
		    				if (firstReqRoomPref){
		    					firstReqRoomPref = false;
		    				} else {
		    					sb.append("<BR>");
		    				}
							RoomPref rp = (RoomPref) rmPrefIt.next();
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
	public static List<BannerSection> findAll(Long sessionId) {
    	return (new BannerSectionDAO()).
    		getSession().
    		createQuery("select distinct bs from BannerSection bs where " +
    				"bs.session.uniqueId=:sessionId").
    		setLong("sessionId",sessionId.longValue()).
    		list();
    }

    public static List<Class_> findAllClassesForCrnAndTermCode(Integer crn, String termCode){
    	return(findAllClassesForCrnAndTermCode((new BannerSectionDAO()).getSession(), crn, termCode));
    }
    
    @SuppressWarnings("unchecked")
	public static List<Class_> findAllClassesForCrnAndTermCode(Session hibSession, Integer crn, String termCode){
    	return (hibSession.
			createQuery("select distinct c from BannerSession bsess, BannerSection bs inner join bs.bannerSectionToClasses as bstc, Class_ c where " +
					"bs.session.uniqueId=bsess.session.uniqueId and bsess.bannerTermCode = :termCode and bs.crn = :crn and bstc.classId = c.uniqueId").
			setString("termCode",termCode).
			setInteger("crn", crn).
			list());
    }

    public static CourseOffering findCourseOfferingForCrnAndTermCode(Integer crn, String termCode){
    	return(findCourseOfferingForCrnAndTermCode((new BannerSectionDAO()).getSession(), crn, termCode));
    }
  
    public static CourseOffering findCourseOfferingForCrnAndTermCode(Session hibSession, Integer crn, String termCode){
    	return ((CourseOffering)hibSession.
			createQuery("select distinct co from BannerSession bsess, BannerSection bs, CourseOffering co where " +
					"bs.session.uniqueId=bsess.session.uniqueId and bsess.bannerTermCode = :termCode and bs.crn = :crn and co.uniqueId = bs.bannerConfig.bannerCourse.courseOfferingId").
			setString("termCode",termCode).
			setInteger("crn", crn).
			uniqueResult());
    }
    
	private ExternalBannerCampusCodeElementHelperInterface getExternalCampusCodeElementHelper(){
		if (externalCampusCodeElementHelper == null){
            String className = ApplicationProperties.getProperty("tmtbl.banner.campus.element.helper");
        	if (className != null && className.trim().length() > 0){
        		try {
        			externalCampusCodeElementHelper = (ExternalBannerCampusCodeElementHelperInterface) (Class.forName(className).newInstance());
				} catch (InstantiationException e) {
					Debug.error("Failed to instantiate instance of: " + className + " using the default campus code element helper.");
					e.printStackTrace();
					externalCampusCodeElementHelper = new DefaultExternalBannerCampusCodeElementHelper();
				} catch (IllegalAccessException e) {
					Debug.error("Illegal Access Exception on: " + className + " using the default campus code element helper.");
					e.printStackTrace();
					externalCampusCodeElementHelper = new DefaultExternalBannerCampusCodeElementHelper();
				} catch (ClassNotFoundException e) {
					Debug.error("Failed to find class: " + className + " using the default campus code element helper.");
					e.printStackTrace();
					externalCampusCodeElementHelper = new DefaultExternalBannerCampusCodeElementHelper();
				}
        	} else {
        		externalCampusCodeElementHelper = new DefaultExternalBannerCampusCodeElementHelper();
        	}
		}
		return externalCampusCodeElementHelper;

	}

	private static ExternalBannerSubjectAreaElementHelperInterface getExternalSubjectAreaElementHelper(){
		if (externalSubjectAreaElementHelper == null){
            String className = ApplicationProperties.getProperty("tmtbl.banner.subjectArea.element.helper");
        	if (className != null && className.trim().length() > 0){
        		try {
        			externalSubjectAreaElementHelper = (ExternalBannerSubjectAreaElementHelperInterface) (Class.forName(className).newInstance());
				} catch (InstantiationException e) {
					Debug.error("Failed to instantiate instance of: " + className + " using the default subject area element helper.");
					e.printStackTrace();
					externalSubjectAreaElementHelper = new DefaultExternalBannerSubjectAreaElementHelper();
				} catch (IllegalAccessException e) {
					Debug.error("Illegal Access Exception on: " + className + " using the default subject area element helper.");
					e.printStackTrace();
					externalSubjectAreaElementHelper = new DefaultExternalBannerSubjectAreaElementHelper();
				} catch (ClassNotFoundException e) {
					Debug.error("Failed to find class: " + className + " using the default subject area element helper.");
					e.printStackTrace();
					externalSubjectAreaElementHelper = new DefaultExternalBannerSubjectAreaElementHelper();
				}
        	} else {
        		externalSubjectAreaElementHelper = new DefaultExternalBannerSubjectAreaElementHelper();
        	}
		}
		return externalSubjectAreaElementHelper;

	}

	
	public String getCampusCode(BannerSession bannerSession,
			Class_ clazz) {
		if (this.getBannerCampusOverride() != null){
			return(this.getBannerCampusOverride().getBannerCampusCode());
		} else {
			return(getExternalCampusCodeElementHelper().getDefaultCampusCode(this, bannerSession, clazz));
		}
	}

	public String getDefaultCampusCode(BannerSession bannerSession,
			Class_ clazz) {
		return(getExternalCampusCodeElementHelper().getDefaultCampusCode(this, bannerSession, clazz));
	}
	
	public String getBannerSubjectArea(BannerSession bannerSession, Class_ clazz) {
		return(getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(this, bannerSession, clazz));
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
	
	public ArrayList<BannerInstrMethodCohortRestriction> getAllBannerInstrMethodCohortRestriction(Session hibSession){
		ArrayList<BannerInstrMethodCohortRestriction> imRestrictions = new ArrayList<BannerInstrMethodCohortRestriction>();
		
		InstrOfferingConfig ioc = InstrOfferingConfigDAO.getInstance().get(this.getBannerConfig().getInstrOfferingConfigId(), hibSession);
		if (ioc.getEffectiveInstructionalMethod() != null) {
    	    		imRestrictions.addAll(BannerInstrMethodCohortRestriction.findAllWithTermAndMethod(getSession(), ioc.getEffectiveInstructionalMethod(), hibSession));
		}
		return(imRestrictions);
		
	}

	private ArrayList<BannerCohortRestriction> mergeBannerCohortRestrictions(ArrayList<BannerCohortRestriction> bannerCohortRestrictions, ArrayList<BannerInstrMethodCohortRestriction> instructionalMethodBannerCohortRestrictions) {
		ArrayList<BannerCohortRestriction> mergedRestrictions = new ArrayList<BannerCohortRestriction>();
		ArrayList<BannerCohortRestriction> notMatchedRestrictions = new ArrayList<BannerCohortRestriction>();
		notMatchedRestrictions.addAll(bannerCohortRestrictions);
		for (BannerInstrMethodCohortRestriction bimcr : instructionalMethodBannerCohortRestrictions) {
			boolean merged = false;
			for (BannerCohortRestriction bcr : bannerCohortRestrictions) {
				if (notMatchedRestrictions.contains(bcr) && bcr.matches(bimcr)) {
					merged = true;
					notMatchedRestrictions.remove(bcr);
					BannerCohortRestriction newBcr = BannerCohortRestriction.createFromInstructionalMethodCohortRestriction(bimcr);
					newBcr.setBannerSection(this);
					mergedRestrictions.add(newBcr);
					break;
				}
			}
			if (!merged) {
				BannerCohortRestriction newBcr = BannerCohortRestriction.createFromInstructionalMethodCohortRestriction(bimcr);
				newBcr.setBannerSection(this);
				mergedRestrictions.add(newBcr);
			}
		}
		for (BannerCohortRestriction bcr : notMatchedRestrictions) {
			BannerCohortRestriction newBcr = bcr.clone();
			newBcr.setRemoved(new Boolean(true));
			mergedRestrictions.add(newBcr);
		}
        return(mergedRestrictions);
	}
	
	public void replaceLastSentCohortRestrictions(ArrayList<BannerCohortRestriction> newLastSentCohortRestrictions, Session hibSession) {
		ArrayList<BannerCohortRestriction> deleteList = new ArrayList<BannerCohortRestriction>();
		deleteList.addAll(getAllLastSentCohortRestrictions());
		
		Transaction trans = null;
		try {
			if (hibSession.getTransaction()==null || !hibSession.getTransaction().isActive()) {
				trans = hibSession.beginTransaction();
			}
			for (BannerCohortRestriction bcr : deleteList) {
				bcr.setBannerSection(null);
				this.getBannerLastSentBannerRestrictions().remove(bcr);
				hibSession.delete(bcr);
			}
			hibSession.update(this);
			if (trans == null) {
				hibSession.flush();
			} else {
				trans.commit();
				trans = hibSession.beginTransaction();
			}
			for (BannerCohortRestriction bcr : newLastSentCohortRestrictions) {
				bcr.setBannerSection(this);
				bcr.setUniqueId((Long) hibSession.save(bcr));
				this.addTobannerLastSentBannerRestrictions(bcr);
			}
			hibSession.update(this);
			if (trans == null) {
				hibSession.flush();
			} else {
				trans.commit();
			}
		} catch (Exception e) {
			Debug.info("Failed to save Last Sent Banner Restrictions for " + this.getCrn().toString());
		}
		
	}
	
	public ArrayList<BannerCohortRestriction> getAllBannerCohortRestrictions(Session hibSession) {
		ArrayList<BannerInstrMethodCohortRestriction> imRestrictions = getAllBannerInstrMethodCohortRestriction(hibSession);
		ArrayList<BannerCohortRestriction> lastSentCohortRestrictions = getAllLastSentCohortRestrictions();
		return(mergeBannerCohortRestrictions(lastSentCohortRestrictions, imRestrictions));
	}

	private ArrayList<BannerCohortRestriction> getAllLastSentCohortRestrictions() {
		ArrayList<BannerCohortRestriction> lastSentCohortRestrictions = new ArrayList<BannerCohortRestriction>();
		if (getBannerLastSentBannerRestrictions() != null) {
			for (BannerLastSentSectionRestriction blssr : getBannerLastSentBannerRestrictions()) {
				if (blssr instanceof BannerCohortRestriction) {
					lastSentCohortRestrictions.add((BannerCohortRestriction) blssr);
				}
			}
		}
		return lastSentCohortRestrictions;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<InstructionalOffering> findOfferingsMissingBannerSections(
			org.unitime.timetable.model.Session academicSession, Session hibSession) {
		ArrayList<InstructionalOffering> offeringList = new ArrayList<InstructionalOffering>();
		String query = " select distinct c.schedulingSubpart.instrOfferingConfig.instructionalOffering"
				+ " from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co"
				+ " where c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session = :sessionId"
				+ "  and 0 = (select count(bs)"
				+ "               from BannerSection bs inner join bs.bannerSectionToClasses as bstc"
				+ "               where bstc.classId = c.uniqueId)";
		offeringList.addAll(hibSession.createQuery(query).setLong("sessionId", academicSession.getUniqueId().longValue()).list());
		
		return offeringList;
	}

	@SuppressWarnings("unchecked")
	public static ArrayList<InstructionalOffering> findOfferingsMissingBannerSectionsForSubjectArea(
			SubjectArea subjectArea, Session hibSession) {
		ArrayList<InstructionalOffering> offeringList = new ArrayList<InstructionalOffering>();
		String query = " select distinct c.schedulingSubpart.instrOfferingConfig.instructionalOffering"
				+ " from Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co"
				+ " where co.subjectArea.uniqueId = :subjId"
				+ "  and 0 = (select count(bs)"
				+ "               from BannerSection bs inner join bs.bannerSectionToClasses as bstc"
				+ "               where bstc.classId = c.uniqueId)";
		offeringList.addAll(hibSession.createQuery(query).setLong("subjId", subjectArea.getUniqueId().longValue()).list());
		
		return offeringList;
	}

}