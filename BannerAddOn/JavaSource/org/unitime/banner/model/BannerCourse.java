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

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.unitime.banner.model.base.BaseBannerCourse;
import org.unitime.banner.model.comparators.BannerCourseComparator;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.webutil.Navigation;



/**
 * 
 * @author says
 *
 */
public class BannerCourse extends BaseBannerCourse {
	private static final long serialVersionUID = 1L;

/*[CONSTRUCTOR MARKER BEGIN]*/
	public BannerCourse () {
		super();
	}

	/**
	 * Constructor for primary key
	 */
	public BannerCourse (java.lang.Long uniqueId) {
		super(uniqueId);
	}
/*[CONSTRUCTOR MARKER END]*/
	
	private CourseOffering courseOffering;

	public static BannerCourse findBannerCourseForCourseOffering(Long courseOfferingId, Session hibSession){
		return((BannerCourse) hibSession
			.createQuery("from BannerCourse bc where bc.courseOfferingId=:courseOfferingId)")
			.setLong("courseOfferingId", courseOfferingId.longValue())
			.setFlushMode(FlushMode.MANUAL)
			.setCacheable(false)
			.uniqueResult()); 
	}

	@SuppressWarnings("unchecked")
	public static List<BannerCourse> findBannerCoursesForCourseOffering(Long courseOfferingId, Session hibSession){
		return((List<BannerCourse>) hibSession
			.createQuery("from BannerCourse bc where bc.courseOfferingId=:courseOfferingId)")
			.setLong("courseOfferingId", courseOfferingId.longValue())
			.setFlushMode(FlushMode.MANUAL)
			.setCacheable(false)
			.list()); 
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
	}
	
    public BannerCourse getNextBannerCourse(SessionContext context, boolean canEdit, boolean canView) {
    	return getNextBannerCourse(context, new BannerCourseComparator(), canEdit, canView);
    }

    public BannerCourse getPreviousBannerCourse(SessionContext context, boolean canEdit, boolean canView) {
    	return getPreviousBannerCourse(context, new BannerCourseComparator(), canEdit, canView);
    }


    @SuppressWarnings("unchecked")
	public BannerCourse getNextBannerCourse(SessionContext context, Comparator cmp, boolean canEdit, boolean canView) {
    	InstructionalOfferingDAO ioDao = new InstructionalOfferingDAO();
    	CourseOffering currentCo = getCourseOffering(ioDao.getSession());
    	BannerCourse next = null;
    	if (currentCo.getInstructionalOffering().getCourseOfferings().size() > 1){
     		Vector courses = new Vector();
    		courses.addAll(currentCo.getInstructionalOffering().getCourseOfferings());
    		Collections.sort(courses, new CourseOfferingComparator());
    		SubjectArea area = getCourseOffering(ioDao.getSession()).getSubjectArea();
    		for (Iterator it = courses.iterator(); it.hasNext(); ){
    			CourseOffering co = (CourseOffering) it.next();
    			if (co.getUniqueId().equals(getCourseOffering(ioDao.getSession()).getUniqueId())){
    				continue;
    			} else if (co.getSubjectArea().getUniqueId().equals(area.getUniqueId()) && co.getCourseNbr().compareTo(getCourseOffering(ioDao.getSession()).getCourseNbr()) > 0){
    				next = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), ioDao.getSession());
    				break;
    			} else if (co.getSubjectArea().getUniqueId().equals(area.getUniqueId()) && co.getCourseNbr().equals(getCourseOffering(ioDao.getSession()).getCourseNbr())  && co.getTitle().compareTo(getCourseOffering(ioDao.getSession()).getTitle()) > 0){
    				next = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), ioDao.getSession());
    				break;
    			}
    		}
    	}
    	if (next != null){
    		return(next);
    	}
    	InstructionalOffering nextIo = null;
    	Long nextId = Navigation.getNext(context, Navigation.sInstructionalOfferingLevel, currentCo.getInstructionalOffering().getUniqueId());
    	if (nextId!=null) {
    		if (nextId.longValue()<0) return null;
    		nextIo = (InstructionalOffering)ioDao.get(nextId);
    	}
    	if (nextIo == null){
    		return(null);
    	}
     	if (nextIo.getCourseOfferings() == null  || nextIo.getCourseOfferings().isEmpty()) {
    		return(null);
    	} else if (nextIo.getCourseOfferings().size() == 1){
    		next = BannerCourse.findBannerCourseForCourseOffering(nextIo.getControllingCourseOffering().getUniqueId(), ioDao.getSession());
    		return(next);
    	} else {
    		SubjectArea area = getCourseOffering(ioDao.getSession()).getSubjectArea();
    		Vector courses = new Vector();
    		courses.addAll(nextIo.getCourseOfferings());
    		Collections.sort(courses, new CourseOfferingComparator());
    		for (Iterator it = courses.iterator(); it.hasNext(); ){
    			CourseOffering co = (CourseOffering) it.next();
    			if (co.getUniqueId().equals(getCourseOffering(ioDao.getSession()).getUniqueId())){
    				continue;
    			} else if (co.getSubjectArea().getUniqueId().equals(area.getUniqueId()) && co.getCourseNbr().compareTo(getCourseOffering(ioDao.getSession()).getCourseNbr()) > 0){
    				next = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), ioDao.getSession());
    				break;
    			} else if (co.getSubjectArea().getUniqueId().equals(area.getUniqueId()) && co.getCourseNbr().equals(getCourseOffering(ioDao.getSession()).getCourseNbr())  && co.getTitle().compareTo(getCourseOffering(ioDao.getSession()).getTitle()) > 0){
    				next = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), ioDao.getSession());
    				break;
    			}
    		}
    	}
    	return(next);
    }

    @SuppressWarnings("unchecked")
	public BannerCourse getPreviousBannerCourse(SessionContext context, Comparator cmp, boolean canEdit, boolean canView) {
    	InstructionalOfferingDAO ioDao = new InstructionalOfferingDAO();
    	CourseOffering currentCo = getCourseOffering(ioDao.getSession());
    	BannerCourse previous = null;
    	if (currentCo.getInstructionalOffering().getCourseOfferings().size() > 1){
     		Vector courses = new Vector();
    		courses.addAll(currentCo.getInstructionalOffering().getCourseOfferings());
    		Collections.sort(courses, new CourseOfferingComparator());
    		Collections.reverse(courses);
    		SubjectArea area = getCourseOffering(ioDao.getSession()).getSubjectArea();
    		for (Iterator it = courses.iterator(); it.hasNext(); ){
    			CourseOffering co = (CourseOffering) it.next();
    			if (co.getUniqueId().equals(getCourseOffering(ioDao.getSession()).getUniqueId())){
    				continue;
    			} else if (co.getSubjectArea().getUniqueId().equals(area.getUniqueId()) && co.getCourseNbr().compareTo(getCourseOffering(ioDao.getSession()).getCourseNbr()) < 0){
    				previous = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), ioDao.getSession());
    				break;
    			} else if (co.getSubjectArea().getUniqueId().equals(area.getUniqueId()) && co.getCourseNbr().equals(getCourseOffering(ioDao.getSession()).getCourseNbr())  && co.getTitle().compareTo(getCourseOffering(ioDao.getSession()).getTitle()) < 0){
    				previous = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), ioDao.getSession());
    				break;
    			}
    		}
    	}
    	if (previous != null){
    		return(previous);
    	}
    	InstructionalOffering previousIo = null;
    	Long previousId = Navigation.getPrevious(context, Navigation.sInstructionalOfferingLevel, currentCo.getInstructionalOffering().getUniqueId());
    	if (previousId!=null) {
    		if (previousId.longValue()<0) return null;
    		previousIo = (InstructionalOffering)ioDao.get(previousId);
    	}
    	if (previousIo == null){
    		return(null);
    	}
     	if (previousIo.getCourseOfferings() == null  || previousIo.getCourseOfferings().isEmpty()) {
    		return(null);
    	} else if (previousIo.getCourseOfferings().size() == 1){
    		previous = BannerCourse.findBannerCourseForCourseOffering(previousIo.getControllingCourseOffering().getUniqueId(), ioDao.getSession());
    		return(previous);
    	} else {
    		SubjectArea area = getCourseOffering(ioDao.getSession()).getSubjectArea();
    		Vector courses = new Vector();
    		courses.addAll(previousIo.getCourseOfferings());
    		Collections.sort(courses, new CourseOfferingComparator());
    		Collections.reverse(courses);
    		for (Iterator it = courses.iterator(); it.hasNext(); ){
    			CourseOffering co = (CourseOffering) it.next();
    			if (co.getUniqueId().equals(getCourseOffering(ioDao.getSession()).getUniqueId())){
    				continue;
    			} else if (co.getSubjectArea().getUniqueId().equals(area.getUniqueId()) && co.getCourseNbr().compareTo(getCourseOffering(ioDao.getSession()).getCourseNbr()) < 0){
    				previous = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), ioDao.getSession());
    				break;
    			} else if (co.getSubjectArea().getUniqueId().equals(area.getUniqueId()) && co.getCourseNbr().equals(getCourseOffering(ioDao.getSession()).getCourseNbr())  && co.getTitle().compareTo(getCourseOffering(ioDao.getSession()).getTitle()) < 0){
    				previous = BannerCourse.findBannerCourseForCourseOffering(co.getUniqueId(), ioDao.getSession());
    				break;
    			}
    		}
    	}
    	return(previous);
    }

	@SuppressWarnings("unchecked")
	public static List<BannerCourse> findBannerCoursesForInstrOffrConfig(
			InstrOfferingConfig ioc, Session hibSession) {
		String qs = "select distinct bc.bannerCourse from BannerConfig bc where bc.instrOfferingConfigId = :configId";
		return((List<BannerCourse>)hibSession.createQuery(qs)
				      .setLong("configId", ioc.getUniqueId().longValue())
				      .list());
	}
	
}