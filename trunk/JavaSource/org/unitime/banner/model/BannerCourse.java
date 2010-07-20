/*
 * UniTime 3.1 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
 * as indicated by the @authors tag.
 * 
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
*/

package org.unitime.banner.model;

import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import javax.servlet.http.HttpSession;

import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.unitime.banner.model.base.BaseBannerCourse;
import org.unitime.banner.model.comparators.BannerCourseComparator;
import org.unitime.commons.User;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.comparators.CourseOfferingComparator;
import org.unitime.timetable.model.dao.CourseOfferingDAO;
import org.unitime.timetable.model.dao.InstructionalOfferingDAO;
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

	/**
	 * Constructor for required fields
	 */
	public BannerCourse (
		java.lang.Long uniqueId,
		java.lang.Long courseOfferingId) {

		super (
			uniqueId,
			courseOfferingId);
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
	
    public BannerCourse getNextBannerCourse(HttpSession session, User user, boolean canEdit, boolean canView) {
    	return getNextBannerCourse(session, new BannerCourseComparator(), user, canEdit, canView);
    }

    public BannerCourse getPreviousBannerCourse(HttpSession session, User user, boolean canEdit, boolean canView) {
    	return getPreviousBannerCourse(session, new BannerCourseComparator(), user, canEdit, canView);
    }


    @SuppressWarnings("unchecked")
	public BannerCourse getNextBannerCourse(HttpSession session, Comparator cmp, User user, boolean canEdit, boolean canView) {
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
    	Long nextId = Navigation.getNext(session, Navigation.sInstructionalOfferingLevel, currentCo.getInstructionalOffering().getUniqueId());
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
	public BannerCourse getPreviousBannerCourse(HttpSession session, Comparator cmp, User user, boolean canEdit, boolean canView) {
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
    	Long previousId = Navigation.getPrevious(session, Navigation.sInstructionalOfferingLevel, currentCo.getInstructionalOffering().getUniqueId());
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