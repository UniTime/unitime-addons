/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010-2012, UniTime LLC, and individual contributors
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

package org.unitime.banner.model;

import java.sql.CallableStatement;
import java.sql.Connection;
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
import org.hibernate.engine.SessionFactoryImplementor;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.interfaces.ExternalBannerCampusCodeElementHelperInterface;
import org.unitime.banner.model.base.BaseBannerSection;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.banner.model.dao.BannerSectionDAO;
import org.unitime.banner.util.BannerCrnValidator;
import org.unitime.banner.util.DefaultExternalBannerCampusCodeElementHelper;
import org.unitime.commons.Debug;
import org.unitime.timetable.ApplicationProperties;
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
import org.unitime.timetable.model.PreferenceLevel;
import org.unitime.timetable.model.RoomPref;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.Solution;
import org.unitime.timetable.model.dao.Class_DAO;
import org.unitime.timetable.model.dao._RootDAO;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.Constants;

/**
 * 
 * @author says
 *
 */
public class BannerSection extends BaseBannerSection {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7327253389631092870L;
	private static ExternalBannerCampusCodeElementHelperInterface externalCampusCodeElementHelper;


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
				for(Iterator it = getBannerSectionToClasses().iterator(); it.hasNext();){
					BannerSectionToClass bsc = (BannerSectionToClass) it.next();
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
		for (Iterator it = getBannerSectionToClasses().iterator(); (it.hasNext() && !isCrossListed);){
			BannerSectionToClass bsc = (BannerSectionToClass) it.next();
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
    	if (getBannerConfig().getGradableItype() != null) {
    		if (schedSubpart != null && schedSubpart.getInstrOfferingConfig().getInstructionalOffering().getCredit() != null && schedSubpart.getInstrOfferingConfig().getInstructionalOffering().getCredit() instanceof FixedCreditUnitConfig) {
    			if (schedSubpart.getItype().getItype().equals(getBannerConfig().getGradableItype().getItype())) {
    				FixedCreditUnitConfig fixed = (FixedCreditUnitConfig) schedSubpart.getInstrOfferingConfig().getInstructionalOffering().getCredit();
    				credit = fixed.getFixedUnits().toString();
    			} else {
    				credit = "0.0";
    			}
    		} else {
    			credit = "";
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
	   	try {
    		String nextCrnSql = ApplicationProperties.getProperty("banner.crn.generator","{?=call timetable.crn_processor.get_crn(?)}");
    		
            SessionFactoryImplementor hibSessionFactory = (SessionFactoryImplementor)new _RootDAO().getSession().getSessionFactory();
            Connection connection = hibSessionFactory.getConnectionProvider().getConnection();
            CallableStatement call = connection.prepareCall(nextCrnSql);
            call.registerOutParameter(1, java.sql.Types.INTEGER);
            call.setLong(2, acadSession.getUniqueId().longValue());
            call.execute();
            nextCrn = call.getInt(1);
            call.close();
            hibSessionFactory.getConnectionProvider().closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return(nextCrn);
	}
	
	
	public static boolean isSectionIndexUniqueForCourse(org.unitime.timetable.model.Session acadSession, CourseOffering courseOffering,
			Session hibSession, String sectionId) {
		int sectionExists = 0;
	   	try {
    		String sectionExistsSql = ApplicationProperties.getProperty("banner.section_id.validator");
    		
            SessionFactoryImplementor hibSessionFactory = (SessionFactoryImplementor)new _RootDAO().getSession().getSessionFactory();
            Connection connection = hibSessionFactory.getConnectionProvider().getConnection();
            CallableStatement call = connection.prepareCall(sectionExistsSql);
            call.registerOutParameter(1, java.sql.Types.INTEGER);
            call.setLong(2, acadSession.getUniqueId().longValue());
            call.setString(3, courseOffering.getSubjectAreaAbbv());
            call.setString(4, courseOffering.getCourseNbr());
            call.setString(5, sectionId);
            call.execute();
            sectionExists = call.getInt(1);
            call.close();
            hibSessionFactory.getConnectionProvider().closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
		} 
		return(sectionExists != 1);
	}
	
	public static String findNextUnusedSectionIndexFor(org.unitime.timetable.model.Session acadSession, CourseOffering courseOffering,
			Session hibSession) {
		String nextSectionId = null;
	   	try {
//    		String nextSectionIdSql = ApplicationProperties.getProperty("banner.section_id.generator","{?= call timetable.section_processor.get_section(?,?,?)}");
    		String nextSectionIdSql = ApplicationProperties.getProperty("banner.section_id.generator");
            SessionFactoryImplementor hibSessionFactory = (SessionFactoryImplementor)new _RootDAO().getSession().getSessionFactory();
            Connection connection = hibSessionFactory.getConnectionProvider().getConnection();
            CallableStatement call = connection.prepareCall(nextSectionIdSql);
            call.registerOutParameter(1, java.sql.Types.VARCHAR);
            call.setLong(2, acadSession.getUniqueId().longValue());
            call.setString(3, courseOffering.getSubjectAreaAbbv());
            call.setString(4, courseOffering.getCourseNbr());
            call.execute();
            nextSectionId = call.getString(1);
            call.close();
            hibSessionFactory.getConnectionProvider().closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
		} 

		return(nextSectionId);
	}

	public static String findNextUnusedLinkIdentifierFor(org.unitime.timetable.model.Session acadSession, CourseOffering courseOffering,
			Session hibSession) {
		String nextLinkId = null;
	   	try {
    		String nextLinkIdSql = ApplicationProperties.getProperty("banner.link_id.generator","{?= call timetable.section_processor.get_link_identifier(?,?,?)}");
    		
            SessionFactoryImplementor hibSessionFactory = (SessionFactoryImplementor)new _RootDAO().getSession().getSessionFactory();
            Connection connection = hibSessionFactory.getConnectionProvider().getConnection();
            CallableStatement call = connection.prepareCall(nextLinkIdSql);
            call.registerOutParameter(1, java.sql.Types.VARCHAR);
            call.setLong(2, acadSession.getUniqueId().longValue());
            call.setString(3, courseOffering.getSubjectAreaAbbv());
            call.setString(4, courseOffering.getCourseNbr());
            call.execute();
            nextLinkId = call.getString(1);
            call.close();
            hibSessionFactory.getConnectionProvider().closeConnection(connection);
		} catch (Exception e) {
			e.printStackTrace();
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
		List sections = hibSession
		.createQuery("select distinct bsc.bannerSection from BannerSectionToClass as bsc, Assignment a where a.solution.uniqueId = :solutionId and bsc.classId = a.clazz.uniqueId")
		.setLong("solutionId", solution.getUniqueId().longValue())
		.setFlushMode(FlushMode.MANUAL)
		.setCacheable(false)
		.list();
		BannerSection bs = null;
		Iterator it = sections.iterator();
		while(it.hasNext()){
			bs = (BannerSection) it.next();
			for(Iterator cIt = bs.getClasses(hibSession).iterator(); cIt.hasNext();){
				Class_ c = (Class_) cIt.next();
				hibSession.refresh(c);
			}
			hibSession.refresh(bs);
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
	
	public List bannerSectionsCrosslistedWithThis(){
		if (!getClasses(null).isEmpty()){
		Class_ c = (Class_)getClasses(null).iterator().next();
		String qs = "select distinct bsc.bannerSection from BannerSectionToClass bsc where bsc.class_id = :classId and bsc.bannerSection.uniqueId != :sectionId";
		return(BannerSectionDAO.getInstance()
				               .getQuery(qs)
				               .setLong("classId", c.getUniqueId().longValue())
				               .setLong("sectionId", getUniqueId().longValue())
				               .list());
		} else {
			return(new Vector());
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

		updateClassSuffixForClassesIfNecessaryRefreshClasses(hibSession, true);

	}
	
	public void updateClassSuffixForClassesIfNecessaryRefreshClasses(Session hibSession, boolean refresh){
		Boolean control = this.getBannerConfig().getBannerCourse().getCourseOffering(hibSession).isIsControl();
		if (control.booleanValue()){
			for (Iterator<?> it = this.getBannerSectionToClasses().iterator(); it.hasNext();){
				BannerSectionToClass bsc = (BannerSectionToClass) it.next();
				Class_ clazz = Class_DAO.getInstance().get(bsc.getClassId(), hibSession);
				if (clazz != null){
					String classSuffix = this.classSuffixFor(clazz, hibSession);
					if(clazz.getClassSuffix() == null || !clazz.getClassSuffix().equals(classSuffix)){
						clazz.setClassSuffix(classSuffix);
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
	
	@SuppressWarnings("unchecked")
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
		if (io != null && io.getCoordinators() != null){
			for (DepartmentalInstructor di : io.getCoordinators()){
				instructorPercents.put(di, new Integer(0));
			}
		}
		boolean sendInstructors = false;
		for(Iterator<Class_> cIt = this.getClasses(hibSession).iterator(); cIt.hasNext();){
			Class_ c = cIt.next();		
			
			if (c.getCommittedAssignment() != null || c.getEffectiveTimePreferences().isEmpty()){
				sendInstructors = true;
				if (c.getClassInstructors() != null && !c.getClassInstructors().isEmpty()){
					int totalPercent = 0;
					int instructorCount = 0;
					Iterator ciIt  = c.getClassInstructors().iterator();
					while(ciIt.hasNext()){
						ClassInstructor ci = (ClassInstructor)ciIt.next();
						totalPercent += (ci.getPercentShare() != null?(ci.getPercentShare().intValue() < 0?-1*ci.getPercentShare().intValue():ci.getPercentShare().intValue()):0);
						instructorCount++;
					}
					if (instructorCount > 0){
						numClassesWithInstructors++;
					} else {
						continue;
					}
					ciIt = c.getClassInstructors().iterator();
					while(ciIt.hasNext()){
						ClassInstructor ci = (ClassInstructor)ciIt.next();
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
		    			for(Iterator rmPrefIt = aClass.getEffectiveRoomPreferences().iterator(); rmPrefIt.hasNext();){
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
	   				sb.append(a.getTimeLocation().getStartTimeHeader());
	   				sb.append("-");
	   				sb.append(a.getTimeLocation().getEndTimeHeader());
	   				if (a.getRoomLocations().size() > 1){
	   					for (int i = 1; i < a.getRoomLocations().size() ; i++){
	   						sb.append("<BR>");
	   					}
	   				}
	     		}  else {
		    		if (aClass.getEffectiveTimePreferences().isEmpty()){
		    			boolean firstReqRoomPref = true;
		    			for(Iterator rmPrefIt = aClass.getEffectiveRoomPreferences().iterator(); rmPrefIt.hasNext();){
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
		    		Iterator it2 = a.getRooms().iterator();
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
		    			for(Iterator rmPrefIt = aClass.getEffectiveRoomPreferences().iterator(); rmPrefIt.hasNext();){
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
		    		Iterator it2 = a.getRooms().iterator();
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
		    			for(Iterator rmPrefIt = aClass.getEffectiveRoomPreferences().iterator(); rmPrefIt.hasNext();){
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

    public static List findAll(Long sessionId) {
    	return (new BannerSectionDAO()).
    		getSession().
    		createQuery("select distinct bs from BannerSection bs where " +
    				"bs.session.uniqueId=:sessionId").
    		setLong("sessionId",sessionId.longValue()).
    		list();
    }

    public static List findAllClassesForCrnAndTermCode(Integer crn, String termCode){
    	return(findAllClassesForCrnAndTermCode((new BannerSectionDAO()).getSession(), crn, termCode));
    }
    
    public static List findAllClassesForCrnAndTermCode(Session hibSession, Integer crn, String termCode){
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

	public String getCampusCode(BannerSession bannerSession,
			Class_ clazz) {
		if (this.getBannerCampusOverride() != null){
			return(this.getBannerCampusOverride().getBannerCampusCode());
		} else {
			return(getExternalCampusCodeElementHelper().getDefaultCampusCode(this, bannerSession, clazz));
		}
	}

	public String getDefualtCampusCode(BannerSession bannerSession,
			Class_ clazz) {
		return(getExternalCampusCodeElementHelper().getDefaultCampusCode(this, bannerSession, clazz));
	}

}