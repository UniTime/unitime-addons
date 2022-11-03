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
package org.unitime.colleague.form;

import java.util.Iterator;
import java.util.List;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.form.UniTimeForm;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.dao.OverrideTypeDAO;

/**
 * 
 * @author says
 *
 */
public class ColleagueOfferingDetailForm implements UniTimeForm {
	private static final long serialVersionUID = -5161466018324037153L;
	protected final static ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);


    private String op;   
    private Long subjectAreaId;
    private Long crsOfferingId;
    private Long instrOfferingId;
    private Long courseOfferingId;
    private Long ctrlCrsOfferingId;
    private Integer projectedDemand;
    private Integer enrollment;
    private Integer demand;
    private Integer limit;
    private Boolean unlimited;
    private Boolean notOffered;
    private Boolean isEditable;
    private Boolean isFullyEditable;
    private Boolean isManager;
    private String instrOfferingName;
    private String instrOfferingNameNoTitle;
    private List<CourseOffering> courseOfferings;
    private String subjectAreaAbbr;
    private String courseNbr;
    private String consentType;
    private String creditText;
    private Long nextId;
    private Long previousId;
    private String catalogLinkLabel;
    private String catalogLinkLocation;

    public ColleagueOfferingDetailForm() {
        reset();
    }

    /** 
     * Method validate
     */
    @Override
    public void validate(UniTimeAction action) {}
    
    /** 
     * Method reset
     */
    @Override
    public void reset() {
        op = "view";    
        subjectAreaId = null;
        subjectAreaAbbr = null;
        courseNbr = null;
        crsOfferingId = null;
        instrOfferingId = null;
        courseOfferingId = null;
        ctrlCrsOfferingId = null;
        enrollment = null;
        demand = null;
        projectedDemand = null;
        limit = null;
        unlimited = Boolean.valueOf(false);
        notOffered = null;
        instrOfferingName = "";
        instrOfferingNameNoTitle = "";
        isEditable = null;
        isFullyEditable = null; 
        isManager = null;
        courseOfferings = null;
        nextId = previousId = null;
        creditText = "";
        catalogLinkLabel = null;
        catalogLinkLocation = null;
    }
    
    public List<CourseOffering> getCourseOfferings() {
        return courseOfferings;
    }
    public void setCourseOfferings(List<CourseOffering> courseOfferings) {
        this.courseOfferings = courseOfferings;
    }
    
    public Long getSubjectAreaId() {
        return subjectAreaId;
    }
    public void setSubjectAreaId(Long subjectAreaId) {
        this.subjectAreaId = subjectAreaId;
    }
    
    public Long getCrsOfferingId() {
        return crsOfferingId;
    }
    public void setCrsOfferingId(Long crsOfferingId) {
        this.crsOfferingId = crsOfferingId;
    }
    
    public Long getCtrlCrsOfferingId() {
        return ctrlCrsOfferingId;
    }
    public void setCtrlCrsOfferingId(Long ctrlCrsOfferingId) {
        this.ctrlCrsOfferingId = ctrlCrsOfferingId;
    }
    
    public Integer getDemand() {
        return demand;
    }
    public void setDemand(Integer demand) {
        this.demand = demand;
    }
    
    public Integer getProjectedDemand() {
        return projectedDemand;
    }
    public void setProjectedDemand(Integer projectedDemand) {
        this.projectedDemand = projectedDemand;
    }

    public Long getInstrOfferingId() {
        return instrOfferingId;
    }
    public void setInstrOfferingId(Long instrOfferingId) {
        this.instrOfferingId = instrOfferingId;
    }
    
    public Integer getLimit() {
        return limit;
    }    
    public void setLimit(Integer limit) {
        this.limit = limit;
    }
    
    public Boolean getUnlimited() {
        return unlimited;
    }    
    public void setUnlimited(Boolean unlimited) {
        this.unlimited = unlimited;
    }

    public Boolean getNotOffered() {
        return notOffered;
    }
    public void setNotOffered(Boolean notOffered) {
        this.notOffered = notOffered;
    }
    
    public Boolean getIsEditable() {
        return isEditable;
    }
    public void setIsEditable(Boolean isEditable) {
        this.isEditable = isEditable;
    }
    
    public Boolean getIsFullyEditable() {
        return isFullyEditable;
    }
    public void setIsFullyEditable(Boolean isFullyEditable) {
        this.isFullyEditable = isFullyEditable;
    }

    public String getOp() {
        return op;
    }
    public void setOp(String op) {
        this.op = op;
    }
        
    public String getSubjectAreaAbbr() {
        return subjectAreaAbbr;
    }
    public void setSubjectAreaAbbr(String subjectAreaAbbr) {
        this.subjectAreaAbbr = subjectAreaAbbr;
    }
    
    public String getCourseNbr() {
        return courseNbr;
    }
    public void setCourseNbr(String courseNbr) {
        this.courseNbr = courseNbr;
    }
    
    public String getInstrOfferingName() {
        return instrOfferingName;
    }
    public void setInstrOfferingName(String instrOfferingName) {
        this.instrOfferingName = instrOfferingName;
    }    
    public String getInstrOfferingNameNoTitle() {
        return instrOfferingNameNoTitle;
    }
    public void setInstrOfferingNameNoTitle(String instrOfferingNameNoTitle) {
        this.instrOfferingNameNoTitle = instrOfferingNameNoTitle;
    }    
    
    public Boolean getIsManager() {
        return isManager;
    }
    public void setIsManager(Boolean isManager) {
        this.isManager = isManager;
    }
        
    public String getConsentType() {
        return consentType;
    }
    public void setConsentType(String consentType) {
        this.consentType = consentType;
    }
        
    public String getCatalogLinkLabel() {
		return catalogLinkLabel;
	}

	public void setCatalogLinkLabel(String catalogLinkLabel) {
		this.catalogLinkLabel = catalogLinkLabel;
	}

	public String getCatalogLinkLocation() {
		return catalogLinkLocation;
	}

	public void setCatalogLinkLocation(String catalogLinkLocation) {
		this.catalogLinkLocation = catalogLinkLocation;
	}

	/**
     * Add a course offering to the existing list
     * @param co Course Offering
     */
    public void addToCourseOfferings(CourseOffering co) {
        this.courseOfferings.add(co);
    }

    /**
     * @return No. of course offerings in the instr offering
     */
    public Integer getCourseOfferingCount() {
        return Integer.valueOf(this.courseOfferings.size());
    }
    
    public Long getNextId() { return nextId; }
    public void setNextId(Long nextId) { this.nextId = nextId; }
    public Long getPreviousId() { return previousId; }
    public void setPreviousId(Long previousId) { this.previousId = previousId; }

	public String getCreditText() {
		return creditText;
	}

	public void setCreditText(String creditText) {
		this.creditText = creditText;
	}

	public Integer getEnrollment() {
		return enrollment;
	}

	public void setEnrollment(Integer enrollment) {
		this.enrollment = enrollment;
	}

	public Long getCourseOfferingId() {
		return courseOfferingId;
	}

	public void setCourseOfferingId(Long courseOfferingId) {
		this.courseOfferingId = courseOfferingId;
	}
	
	public boolean getHasCourseTypes() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getCourseType()!=null) return true;
    	return false;
    }
    
    public boolean getHasConsent() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getConsentType()!=null) return true;
    	return false;
    }
    
    public boolean getHasCredit() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getCredit() != null) return true;
    	return false;
    }
    
    public boolean getHasScheduleBookNote() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();) {
    		CourseOffering course = (CourseOffering)i.next();
    		if (course.getScheduleBookNote() != null && !course.getScheduleBookNote().isEmpty()) return true;
    	}
    	return false;
    }

    public boolean getHasCourseReservation() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getReservation() != null) return true;
    	return false;
    }
    
    public boolean getHasCourseExternalId() {
    	if (!ApplicationProperty.CourseOfferingShowExternalIds.isTrue()) return false;
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();) {
    		CourseOffering co = (CourseOffering)i.next();
    		if (co.getExternalUniqueId() != null && !co.getExternalUniqueId().isEmpty()) return true;
    	}
    	return false;
    }
    
    public boolean getHasDisabledOverrides() {
    	for (Iterator i=courseOfferings.iterator();i.hasNext();) {
    		CourseOffering co = (CourseOffering)i.next();
    		if (!co.getDisabledOverrides().isEmpty()) return true;
    	}
    	return false; 
    }
    
    public boolean getHasOverrides() {
    	return !OverrideTypeDAO.getInstance().findAll().isEmpty();
    }
    
    public boolean getHasDemandOfferings() {
    	if (courseOfferings==null || courseOfferings.isEmpty()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getDemandOffering()!=null) return true;
    	return false;
    }
    public boolean getHasAlternativeCourse() {
    	if (courseOfferings==null || courseOfferings.isEmpty() || ApplicationProperty.StudentSchedulingAlternativeCourse.isFalse()) return false;
    	for (Iterator i=courseOfferings.iterator();i.hasNext();)
    		if (((CourseOffering)i.next()).getAlternativeOffering()!=null) return true;
    	return false;
    }
}
