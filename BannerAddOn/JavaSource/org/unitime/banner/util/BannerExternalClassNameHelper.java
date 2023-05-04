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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.hibernate.Session;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.dao.BannerSectionDAO;
import org.unitime.commons.Debug;
import org.unitime.timetable.interfaces.ExternalClassNameHelperInterface;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseCreditUnitConfig;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.FixedCreditUnitConfig;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.timetable.model.comparators.SchedulingSubpartComparator;
import org.unitime.timetable.util.DefaultExternalClassNameHelper;

/**
 * @author says
 *
 */
public class BannerExternalClassNameHelper extends DefaultExternalClassNameHelper implements ExternalClassNameHelperInterface.HasGradableSubpartCache, ExternalClassNameHelperInterface.HasClassNamesCache {

	/**
	 * 
	 */
	public BannerExternalClassNameHelper() {
		// do nothing
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassLabel(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	@Override
	public String getClassLabel(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return super.getClassLabel(clazz, courseOffering);
		} else {
			BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, BannerSectionDAO.getInstance().getSession());
			if (bs != null){
				return courseOffering.getCourseName()+" "+clazz.getItypeDesc().trim()+" "+ bs.getCrn().toString();
			} else {
				return super.getClassLabel(clazz, courseOffering);
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassLabelWithTitle(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	@Override
	public String getClassLabelWithTitle(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return super.getClassLabelWithTitle(clazz, courseOffering);
		} else {
			BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, BannerSectionDAO.getInstance().getSession());
	    	if (bs != null) {
	    		return courseOffering.getCourseNameWithTitle()+" "+clazz.getItypeDesc().trim()+" "+bs.getCrn().toString();
	    	} else {
	    		return super.getClassLabelWithTitle(clazz, courseOffering);
	    	}
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getClassSuffix(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	@Override
	public String getClassSuffix(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return(clazz.getClassSuffix());			
		} else {
			BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, BannerSectionDAO.getInstance().getSession());
			if (bs != null){
				return bs.getCrn().toString() + '-' + bs.getSectionIndex() + (courseOffering.getInstructionalOffering().getCourseOfferings().size() > 1?"*":"");
			} else {
				return(clazz.getClassSuffix());
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalClassNameHelperInterface#getExternalId(org.unitime.timetable.model.Class_, org.unitime.timetable.model.CourseOffering)
	 */
	@Override
	public String getExternalId(Class_ clazz, CourseOffering courseOffering) {
		if (courseOffering.isIsControl().booleanValue()){
			return(clazz.getExternalUniqueId());
		} else {
			BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, BannerSectionDAO.getInstance().getSession());
			if (bs != null) {
				return bs.getCrn().toString();
			} else {
				return(clazz.getExternalUniqueId());
			}
		}
	}
	
	@Override
	public Float getClassCredit(Class_ clazz, CourseOffering courseOffering) {
		CourseCreditUnitConfig credit = courseOffering.getCredit();
		if (credit == null || credit instanceof FixedCreditUnitConfig) return null;
		if (clazz.getParentClass() != null && clazz.getSchedulingSubpart().getItype().equals(clazz.getParentClass().getSchedulingSubpart().getItype())) return null;
		BannerSection bs = BannerSection.findBannerSectionForClassAndCourseOfferingCacheable(clazz, courseOffering, BannerSectionDAO.getInstance().getSession());
		return bs == null ? null : bs.getOverrideCourseCredit();
	}
	
	@Override
	public boolean isGradableSubpart(SchedulingSubpart subpart, CourseOffering courseOffering, org.hibernate.Session hibSession) {
		// child of a subpart with the same itype -> false
		if (subpart.getParentSubpart() != null && subpart.getItype().equals(subpart.getParentSubpart().getItype())) return false;
		// get gradable itype
		Integer itype = hibSession.createQuery(
				"select bc.gradableItype.itype from BannerConfig bc where bc.bannerCourse.courseOfferingId = :courseOfferingId and bc.instrOfferingConfigId = :configId", Integer.class
				).setParameter("configId", subpart.getInstrOfferingConfig().getUniqueId())
				.setParameter("courseOfferingId", courseOffering.getUniqueId())
				.setCacheable(true)
				.setMaxResults(1)
				.uniqueResult();
		// check that the gradable itype matches the subpart's itype
		if (itype != null)
			return subpart.getItype() != null && itype.equals(subpart.getItype().getItype());
		// no gradable itype
		// there is only one subpart -> true
		if (subpart.getInstrOfferingConfig().getSchedulingSubparts().size() == 1) return true;
		// has a parent -> false
		if (subpart.getParentSubpart() != null) return false;
		// otherwise, check that this subpart is the first one
		SchedulingSubpartComparator cmp = new SchedulingSubpartComparator();
		for (SchedulingSubpart s: subpart.getInstrOfferingConfig().getSchedulingSubparts()) {
			if (cmp.compare(s, subpart) < 0) return false;
		}
		return true;
	}

	@Override
	public HasGradableSubpart getGradableSubparts(Long sessionId, Session hibSession) {
		return new GradableSubpartsCache(sessionId, hibSession);
	}
	
	@Override
	public HasGradableSubpart getGradableSubparts(Collection<Long> offeringIds, org.hibernate.Session hibSession) {
		return new GradableSubpartsCache(offeringIds, hibSession);
	}
	
	public static class GradableSubpartsCache implements HasGradableSubpart {
		Map<Long, Map<Long, Integer>> iCache = new HashMap<Long, Map<Long,Integer>>();
		
		public GradableSubpartsCache(Long sessionId, Session hibSession) {
			for (Object[] o: hibSession.createQuery(
					"select bc.bannerCourse.courseOfferingId, bc.instrOfferingConfigId, bc.gradableItype.itype " +
					"from BannerConfig bc, InstrOfferingConfig c where bc.instrOfferingConfigId = c.uniqueId and c.instructionalOffering.session.uniqueId = :sessionId", Object[].class
					).setParameter("sessionId", sessionId).list()) {
				Long courseId = (Long)o[0];
				Long configId = (Long)o[1];
				Integer itype = (Integer)o[2];
				Map<Long, Integer> config2itype = iCache.get(courseId);
				if (config2itype == null) {
					config2itype = new HashMap<Long, Integer>();
					iCache.put(courseId, config2itype);
				}
				config2itype.put(configId, itype);
			}
		}
		
		public GradableSubpartsCache(Collection<Long> offeringIds, Session hibSession) {
			for (Object[] o: hibSession.createQuery(
					"select bc.bannerCourse.courseOfferingId, bc.instrOfferingConfigId, bc.gradableItype.itype " +
					"from BannerConfig bc, InstrOfferingConfig c where bc.instrOfferingConfigId = c.uniqueId and c.instructionalOffering.uniqueId in (:offeringIds)", Object[].class
					).setParameterList("offeringIds", offeringIds, Long.class).list()) {
				Long courseId = (Long)o[0];
				Long configId = (Long)o[1];
				Integer itype = (Integer)o[2];
				Map<Long, Integer> config2itype = iCache.get(courseId);
				if (config2itype == null) {
					config2itype = new HashMap<Long, Integer>();
					iCache.put(courseId, config2itype);
				}
				config2itype.put(configId, itype);
			}
		}

		@Override
		public boolean isGradableSubpart(SchedulingSubpart subpart, CourseOffering courseOffering, org.hibernate.Session hibSession) {
			// child of a subpart with the same itype -> false
			if (subpart.getParentSubpart() != null && subpart.getItype().equals(subpart.getParentSubpart().getItype())) return false;
			// get gradable itype
			Map<Long, Integer> config2itype = iCache.get(courseOffering.getUniqueId());
			Integer itype = (config2itype == null ? null : config2itype.get(subpart.getInstrOfferingConfig().getUniqueId()));
			// check that the gradable itype matches the subpart's itype
			if (itype != null)
				return subpart.getItype() != null && itype.equals(subpart.getItype().getItype());
			// no gradable itype
			// there is only one subpart -> true
			if (subpart.getInstrOfferingConfig().getSchedulingSubparts().size() == 1) return true;
			// has a parent -> false
			if (subpart.getParentSubpart() != null) return false;
			// otherwise, check that this subpart is the first one
			SchedulingSubpartComparator cmp = new SchedulingSubpartComparator();
			for (SchedulingSubpart s: subpart.getInstrOfferingConfig().getSchedulingSubparts()) {
				if (cmp.compare(s, subpart) < 0) return false;
			}
			return true;
		}
	}
	
	public ExternalClassNameHelperInterface getClassNamesCache(Long sessionId, org.hibernate.Session hibSession) {
		return new ClassNamesCache(sessionId, hibSession);
	}
	
	public ExternalClassNameHelperInterface getClassNamesCache(Collection<Long> offeringIds, org.hibernate.Session hibSession) {
		return new ClassNamesCache(offeringIds, hibSession);
	}
	
	public static class ClassNamesCache extends DefaultExternalClassNameHelper implements ExternalClassNameHelperInterface {
		Map<Long, Map<Long, BannerSectionCache>> iCache = new HashMap<Long, Map<Long,BannerSectionCache>>();
		
		public ClassNamesCache(Long sessionId, org.hibernate.Session hibSession) {
			for (Object[] o: hibSession.createQuery(
					"select bs.bannerConfig.bannerCourse.courseOfferingId, bsc.classId, bs " +
					"from BannerSection bs inner join bs.bannerSectionToClasses bsc, CourseOffering co where " +
					"bs.session.uniqueId = :sessionId and " +
					"bs.bannerConfig.bannerCourse.courseOfferingId = co.uniqueId and " +
					"(co.isControl = false or bs.overrideCourseCredit is not null)", Object[].class)
					.setParameter("sessionId", sessionId)
					.setCacheable(true).list()) {
				Long courseId = (Long)o[0];
				Long classId = (Long)o[1];
				BannerSection bs = (BannerSection)o[2];
				Map<Long, BannerSectionCache> class2bsc = iCache.get(courseId);
				if (class2bsc == null) {
					class2bsc = new HashMap<Long, BannerSectionCache>();
					iCache.put(courseId, class2bsc);
				}
				class2bsc.put(classId, new BannerSectionCache(bs));
			}
		}
		
		public ClassNamesCache(Collection<Long> offeringIds, org.hibernate.Session hibSession) {
			for (Object[] o: hibSession.createQuery(
					"select bs.bannerConfig.bannerCourse.courseOfferingId, bsc.classId, bs " +
					"from BannerSection bs inner join bs.bannerSectionToClasses bsc, CourseOffering co where " +
					"co.instructionalOffering.uniqueId in (:offeringIds) and " +
					"bs.bannerConfig.bannerCourse.courseOfferingId = co.uniqueId and " +
					"(co.isControl = false or bs.overrideCourseCredit is not null)", Object[].class)
					.setParameterList("offeringIds", offeringIds, Long.class)
					.setCacheable(true).list()) {
				Long courseId = (Long)o[0];
				Long classId = (Long)o[1];
				BannerSection bs = (BannerSection)o[2];
				Map<Long, BannerSectionCache> class2bsc = iCache.get(courseId);
				if (class2bsc == null) {
					class2bsc = new HashMap<Long, BannerSectionCache>();
					iCache.put(courseId, class2bsc);
				}
				class2bsc.put(classId, new BannerSectionCache(bs));
			}
		}

		@Override
		public String getClassLabel(Class_ clazz, CourseOffering courseOffering) {
			if (courseOffering.isIsControl()) {
				return super.getClassLabel(clazz, courseOffering);
			} else {
				BannerSectionCache bs = getBannerSectionCache(clazz, courseOffering);
				if (bs != null) {
					return courseOffering.getCourseName()+" "+clazz.getItypeDesc().trim()+" "+ bs.getCrn().toString();
				} else {
					return super.getClassLabel(clazz, courseOffering);
				}
			}
		}

		@Override
		public String getClassLabelWithTitle(Class_ clazz, CourseOffering courseOffering) {
			if (courseOffering.isIsControl()) {
				return super.getClassLabelWithTitle(clazz, courseOffering);
			} else {
				BannerSectionCache bs = getBannerSectionCache(clazz, courseOffering);
		    	if (bs != null) {
		    		return courseOffering.getCourseNameWithTitle()+" "+clazz.getItypeDesc().trim()+" "+bs.getCrn().toString();
		    	} else {
		    		return super.getClassLabelWithTitle(clazz, courseOffering);
		    	}
			}
		}

		@Override
		public String getClassSuffix(Class_ clazz, CourseOffering courseOffering) {
			if (courseOffering.isIsControl()) {
				return clazz.getClassSuffix();			
			} else {
				BannerSectionCache bs = getBannerSectionCache(clazz, courseOffering);
				if (bs != null){
					return bs.getCrn().toString() + '-' + bs.getSectionIndex() + (courseOffering.getInstructionalOffering().getCourseOfferings().size() > 1?"*":"");
				} else {
					Debug.warning("No banner section cached for " + courseOffering.getCourseName() + " " + clazz.getItypeDesc().trim() + " " + clazz.getSectionNumberString());
					return clazz.getClassSuffix();
				}
			}
		}

		@Override
		public String getExternalId(Class_ clazz, CourseOffering courseOffering) {
			if (courseOffering.isIsControl()) {
				return clazz.getExternalUniqueId();
			} else {
				BannerSectionCache bs = getBannerSectionCache(clazz, courseOffering);
				if (bs != null) {
					return bs.getCrn().toString();
				} else {
					return clazz.getExternalUniqueId();
				}
			}
		}
		
		@Override
		public Float getClassCredit(Class_ clazz, CourseOffering courseOffering) {
			CourseCreditUnitConfig credit = courseOffering.getCredit();
			if (credit == null || credit instanceof FixedCreditUnitConfig) return null;
			if (clazz.getParentClass() != null && clazz.getSchedulingSubpart().getItype().equals(clazz.getParentClass().getSchedulingSubpart().getItype())) return null;
			BannerSectionCache bs = getBannerSectionCache(clazz, courseOffering);
			return bs == null ? null : bs.getOverrideCourseCredit();
		}
		
		protected BannerSectionCache getBannerSectionCache(Class_ clazz, CourseOffering courseOffering) {
			Map<Long, BannerSectionCache> class2bsc = iCache.get(courseOffering.getUniqueId());
			return (class2bsc == null ? null : class2bsc.get(clazz.getUniqueId()));
		}
	}
	
	public static class BannerSectionCache {
		Integer iCRN;
		String iSectionIndex;
		Float iOverrideCourseCredit;
		
		BannerSectionCache(BannerSection bs) {
			iCRN = bs.getCrn();
			iSectionIndex = bs.getSectionIndex();
			iOverrideCourseCredit = bs.getOverrideCourseCredit();
		}
		
		public Integer getCrn() { return iCRN; }
		public String getSectionIndex() { return iSectionIndex; }
		public Float getOverrideCourseCredit() { return iOverrideCourseCredit; }
	}
}
