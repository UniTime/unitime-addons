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
package org.unitime.banner.form;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hibernate.Session;
import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.BannerCourse;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerCourseDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.form.UniTimeForm;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.IdValue;


/**
 * 
 * @author says
 *
 */
public class BannerOfferingModifyForm implements UniTimeForm {
	private static final long serialVersionUID = 5412595518174343486L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static BannerMessages BMSG = Localization.create(BannerMessages.class);
	
    // --------------------------------------------------------- Instance Variables
	private String op;
    private Integer subjectAreaId;
    private Long bannerCourseOfferingId;
    private Long bannerConfigId;
	private Long instrOfferingId;
    private String instrOfferingName;
	private Integer itypeId;
	private Float labHours;
	private Long instrOffrConfigId;
	private String origSubparts;
	private Boolean configIsEditable;
	private Boolean showLimitOverride;
	private Boolean showLabHours;
	
	private List<Long> bannerSectionIds;
	private List<String> itypes;
	private List<String> bannerSectionSectionIds;
	private List<String> bannerSectionOriginalSectionIds;
	private List<Long> consents;
	private List<String> datePatterns;
	private List<String> times;
	private List<Boolean> readOnlyClasses;
	private List<String> bannerSectionLabels;
	private List<String> bannerSectionLabelIndents;
	private List<String> rooms;
	private List<String> instructors;
	private List<Boolean> readOnlySubparts;
	private List<String> limitOverrides;
	private List<String> courseCredits;
	private List<String> courseCreditOverrides;
	private List<Integer> classLimits;
	private List<String> defaultCampus;
	private List<Long> campusOverrides;
	private List<String> campuses;
	private List<Boolean> classHasErrors;
	private List<BannerCampusOverride> bannerCampusOverrides;
    
    public BannerOfferingModifyForm() {
    	reset();
    }
		
    // --------------------------------------------------------- Methods
    /** 
     * Method validate
     * @param mapping
     * @param request
     * @return ActionErrors
     */
    @Override
    public void validate(UniTimeAction action) {
        if (BMSG.actionUpdateBannerConfig().equals(action.getOp())) {
	        // Check Instructional Offering Config
	        if (this.instrOffrConfigId==null || this.instrOffrConfigId < 0) {
	        	action.addFieldError("form.instrOffrConfigId", MSG.errorRequiredField(BMSG.labelInstructionalOfferingConfig()));
	        }
	        // Validate class limits provide space that is >= limit for the instructional offering config
	        initClassHasErrorsToFalse();
	        validateBannerSectionData(action);
        }
    }
    
    private void validateBannerSectionData(UniTimeAction action){
    	HashSet<String> idSet = new HashSet<String>();
    	int prevSize = idSet.size();
    	BannerCourseDAO bcDao = BannerCourseDAO.getInstance();
    	Session hibSession = bcDao.getSession();
    	BannerCourse bc = bcDao.get(getBannerCourseOfferingId());
    	CourseOffering co = bc.getCourseOffering(hibSession);
		String sectionIdRegex = "^[0-9A-Z]{1,3}$";
    	Pattern pattern = Pattern.compile(sectionIdRegex);
    	for(int index = 0 ; index < this.getBannerSectionSectionIds().size(); index++){
    		String sectionId = getBannerSectionSectionIds(index);
    		if (sectionId == null || sectionId.trim().isEmpty()){
    			action.addFieldError("form.uniqueSectionId", BMSG.errorSectionIndexMustBeSet(getBannerSectionLabels(index)));
    			this.setClassHasErrors(index, true);    			
    		}
    		if (sectionId != null){
	    		sectionId = sectionId.toUpperCase();
	    		try { 
			    	Matcher matcher = pattern.matcher(sectionId);
			    	if (!matcher.find()) {
		    			action.addFieldError("form.invalid section id", BMSG.errorSectionIndexNumbersAndLetters(getBannerSectionLabels(index)));
		    			this.setClassHasErrors(index, true);    			
			    	}
		    	}
		    	catch (Exception e) {
			        action.addFieldError("form.courseNbr", BMSG.errorSectionIndexDoesNotMatchExpression(sectionIdRegex, getBannerSectionLabels(index), e.getMessage()));
	    			this.setClassHasErrors(index, true);    			
		    	}
    		}
    		idSet.add(sectionId);
    		if (idSet.size() != (prevSize + 1)){
    			action.addFieldError("form.uniqueSectionId", BMSG.errorSectionIndexNotUnique(getBannerSectionLabels(index)));
    			this.setClassHasErrors(index, true); 
    		} else {
    			prevSize++;
    		}
    		if (sectionId.equals("999")) {
    			action.addFieldError("form.maxSectionId", BMSG.errorSectionIndex999(getBannerSectionLabels(index)));
    			this.setClassHasErrors(index, true);     			
    		}
    		if (!this.getBannerSectionOriginalSectionIds().contains(getBannerSectionSectionIds(index))){
    			if (!BannerSection.isSectionIndexUniqueForCourse(co.getInstructionalOffering().getSession(), co, hibSession, getBannerSectionSectionIds(index))){
        			action.addFieldError("form.uniqueSectionId", BMSG.errorSectionNewIndexNotUnique(getBannerSectionLabels(index)));
        			this.setClassHasErrors(index, true);   				
    			}
    		}
    		if (this.showLimitOverride.booleanValue()){
    			String limitStr = getLimitOverrides(index);
				Integer limit = null;
				if (limitStr != null && limitStr.trim().length() > 0){
					try {
						limit = Integer.valueOf(limitStr);
					} catch (Exception e) {
	        			action.addFieldError("form.limitOverride", BMSG.errorLimitOverrideNotANumber(getBannerSectionLabels(index)));
	        			this.setClassHasErrors(index, true);   				
					}
					
				}
				Integer classLimit = this.getClassLimits(index);
    			if (limit != null && limit.intValue() > classLimit.intValue()){
        			action.addFieldError("form.limitOverride", BMSG.errorLimitOverrideOverClassLimit(getBannerSectionLabels(index)));
        			this.setClassHasErrors(index, true);   				
    			}
    			if (limit != null && limit.intValue() < 0){
    				action.addFieldError("form.limitOverride", BMSG.errorLimitOverrideBelowZero(getBannerSectionLabels(index)));
        			this.setClassHasErrors(index, true);   	
    			}
    		}
    		String creditOverrideStr = getCourseCreditOverrides(index);
    		Float creditOverride = null;
    		if (creditOverrideStr != null && creditOverrideStr.trim().length() > 0){
    			try {
					creditOverride = Float.valueOf(creditOverrideStr);
				} catch (Exception e) {
        			action.addFieldError("form.creditOverride", BMSG.errorCourseCreditOverrideNotNumber(getBannerSectionLabels(index)));
        			this.setClassHasErrors(index, true);   				
				}
				if (creditOverride != null && creditOverride < 0){
    				action.addFieldError("form.creditOverride", BMSG.errorCourseCreditOverrideBelowZero(getBannerSectionLabels(index)));
        			this.setClassHasErrors(index, true);   						
				}
    		}
    	}
    }
              
    private void initClassHasErrorsToFalse(){
		this.setClassHasErrors(new ArrayList<Boolean>());
		for(Iterator it = this.getBannerSectionIds().iterator(); it.hasNext();){
			this.getClassHasErrors().add(false);
			it.next();
		}
    }
     
    /** 
     * Method reset
     */
    @Override
    public void reset() {
    	bannerCourseOfferingId = null;
    	bannerConfigId = null;
    	instrOfferingId = null;
    	itypeId = null;
    	labHours = null;
    	instrOffrConfigId = null;
    	instrOfferingName = "";
    	origSubparts = "";
    	configIsEditable = Boolean.valueOf(false);
    	showLimitOverride = Boolean.valueOf(false);
    	showLabHours = Boolean.valueOf(false);
    	resetLists();
    }
    
    private void resetLists(){
    	bannerSectionIds = new ArrayList<Long>();
    	bannerSectionSectionIds = new ArrayList<String>();
    	bannerSectionOriginalSectionIds = new ArrayList<String>();
    	itypes = new ArrayList<String>();
    	consents = new ArrayList<Long>();
    	times = new ArrayList<String>();
    	readOnlyClasses = new ArrayList<Boolean>();
       	classHasErrors = new ArrayList<Boolean>();
       	bannerSectionLabels = new ArrayList<String>();
       	bannerSectionLabelIndents = new ArrayList<String>();
       	rooms = new ArrayList<String>();
       	instructors = new ArrayList<String>();
    	datePatterns = new ArrayList<String>();
    	readOnlySubparts = new ArrayList<Boolean>();
       	limitOverrides = new ArrayList<String>();
       	classLimits = new ArrayList<Integer>();
       	courseCredits = new ArrayList<String>();
       	courseCreditOverrides = new ArrayList<String>();
       	defaultCampus = new ArrayList<String>();
       	campusOverrides = new ArrayList<Long>();
       	campuses = new ArrayList<String>();
       	bannerCampusOverrides = new ArrayList<BannerCampusOverride>();
    }
     
	public List<Long> getBannerSectionIds() {
		return bannerSectionIds;
	}
	public void setBannerSectionIds(List<Long> bannerSectionIds) {
		this.bannerSectionIds = bannerSectionIds;
	}
	public Long getBannerSectionIds(int idx) {
		return bannerSectionIds.get(idx);
	}
	public void setBannerSectionIds(int idx, Long bannerSectionId) {
		this.bannerSectionIds.set(idx, bannerSectionId);
	}
	
	public List<Boolean> getClassHasErrors() {
		return classHasErrors;
	}
	public void setClassHasErrors(List<Boolean> classHasErrors) {
		this.classHasErrors = classHasErrors;
	}
	public Boolean getClassHasErrors(int idx) {
		return classHasErrors.get(idx);
	}
	public void setClassHasErrors(int idx, Boolean classHasError) {
		this.classHasErrors.set(idx, classHasError);
	}
	
	public List<String> getBannerSectionLabels() {
		return bannerSectionLabels;
	}
	public void setBannerSectionLabels(List bannerSectionLabels) {
		this.bannerSectionLabels = bannerSectionLabels;
	}
	public String getBannerSectionLabels(int idx) {
		return bannerSectionLabels.get(idx);
	}
	public void setBannerSectionLabels(int idx, String bannerSectionLabel) {
		this.bannerSectionLabels.set(idx, bannerSectionLabel);
	}

	public List<String> getInstructors() {
		return instructors;
	}
	public void setInstructors(List<String> instructors) {
		this.instructors = instructors;
	}
	public String getInstructors(int idx) {
		return instructors.get(idx);
	}
	public void setInstructors(int idx, String instructor) {
		this.instructors.set(idx, instructor);
	}

	public List<String> getDatePatterns() {
		return datePatterns;
	}
	public void setDatePatterns(List<String> datePatterns) {
		this.datePatterns = datePatterns;
	}
	public String getDatePatterns(int idx) {
		return datePatterns.get(idx);
	}
	public void setDatePatterns(int idx, String datePattern) {
		this.datePatterns.set(idx, datePattern);
	}
	
	public Long getInstrOffrConfigId() {
		return instrOffrConfigId;
	}
	public void setInstrOffrConfigId(Long instrOffrConfigId) {
		this.instrOffrConfigId = instrOffrConfigId;
	}

	public Integer getItypeId() {
		return itypeId;
	}
	public void setItypeId(Integer itypeId) {
		this.itypeId = itypeId;
	}

	public Float getLabHours() {
		return labHours;
	}
	public void setLabHours(Float labHours) {
		this.labHours = labHours;
	}

	public String getOp() {
		return op;
	}
	public void setOp(String op) {
		this.op = op;
	}

	public List<String> getTimes() {
		return times;
	}
	public void setTimes(List<String> times) {
		this.times = times;
	}
	public String getTimes(int idx) {
		return times.get(idx);
	}
	public void setTimes(int idx, String time) {
		this.times.set(idx, time);
	}
	
	
	public List<String> getBannerSectionSectionIds() {
		return bannerSectionSectionIds;
	}
	public void setBannerSectionSectionIds(List<String> bannerSectionSectionIds) {
		this.bannerSectionSectionIds = bannerSectionSectionIds;
	}
	public String getBannerSectionSectionIds(int idx) {
		return bannerSectionSectionIds.get(idx);
	}
	public void setBannerSectionSectionIds(int idx, String bannerSectionSectionId) {
		this.bannerSectionSectionIds.set(idx, bannerSectionSectionId);
	}
	
	
	public List<Boolean> getReadOnlyClasses() {
		return readOnlyClasses;
	}
	public void setReadOnlyClasses(List<Boolean> readOnlyClasses) {
		this.readOnlyClasses = readOnlyClasses;
	}
	public Boolean getReadOnlyClasses(int idx) {
		return readOnlyClasses.get(idx);
	}
	public void setReadOnlyClasses(int idx, Boolean readOnlyClass) {
		this.readOnlyClasses.set(idx, readOnlyClass);
	}
	
	public Long getInstrOfferingId() {
		return instrOfferingId;
	}
	public void setInstrOfferingId(Long instrOfferingId) {
		this.instrOfferingId = instrOfferingId;
	}

	    
	public void addToBannerSections(BannerSession bsess, BannerSection bs, Class_ cls, ClassAssignmentProxy classAssignmentProxy, Boolean isReadOnly, String indent, Boolean canShowLimitOverridesIfNeeded){
		this.showLimitOverride = Boolean.valueOf(canShowLimitOverridesIfNeeded?bs.isCrossListedSection(null):false);
		this.bannerSectionLabels.add(bs.bannerSectionLabel());
		this.bannerSectionLabelIndents.add(indent);
		this.bannerSectionIds.add(bs.getUniqueId());
		this.bannerSectionSectionIds.add(bs.getSectionIndex());
		this.bannerSectionOriginalSectionIds.add(bs.getSectionIndex());
		this.itypes.add(cls.getSchedulingSubpart().getItype().getSis_ref());
		this.consents.add(bs.effectiveConsentType()==null?null:bs.effectiveConsentType().getUniqueId());
		this.readOnlyClasses.add(isReadOnly);
		this.classHasErrors.add(false);	
		this.datePatterns.add(bs.buildDatePatternHtml(classAssignmentProxy));
		this.times.add(bs.buildAssignedTimeHtml(classAssignmentProxy));
		this.rooms.add(bs.buildAssignedRoomHtml(classAssignmentProxy));
		this.instructors.add(bs.buildInstructorHtml());
		if (showLimitOverride.booleanValue()){
			this.limitOverrides.add(bs.getOverrideLimit()==null?"":bs.getOverrideLimit().toString());
			this.classLimits.add(bs.maxEnrollBasedOnClasses(null));
		}
		this.courseCreditOverrides.add(bs.getOverrideCourseCredit()==null?"":bs.getOverrideCourseCredit().toString());
		this.courseCredits.add(bs.courseCreditStringBasedOnClass(cls));
		this.defaultCampus.add(bs.getDefaultCampusCode(bsess, cls));
		this.campusOverrides.add(bs.getBannerCampusOverride()==null?null:bs.getBannerCampusOverride().getUniqueId());
		this.campuses.add(bs.getCampusCode(bsess, cls));
	}
	
	public Integer getSubjectAreaId() {
		return subjectAreaId;
	}

	public void setSubjectAreaId(Integer subjectAreaId) {
		this.subjectAreaId = subjectAreaId;
	}

	public String getInstrOfferingName() {
		return instrOfferingName;
	}

	public void setInstrOfferingName(String instrOfferingName) {
		this.instrOfferingName = instrOfferingName;
	}

	public List<String> getBannerSectionLabelIndents() {
		return bannerSectionLabelIndents;
	}
	public void setBannerSectionLabelIndents(List<String> bannerSectionLabelIndents) {
		this.bannerSectionLabelIndents = bannerSectionLabelIndents;
	}
	public String getBannerSectionLabelIndents(int idx) {
		return bannerSectionLabelIndents.get(idx);
	}
	public void setBannerSectionLabelIndents(int idx, String bannerSectionLabelIndent) {
		this.bannerSectionLabelIndents.set(idx, bannerSectionLabelIndent);
	}

	public List<String> getItypes() {
		return itypes;
	}
	public void setItypes(List<String> itypes) {
		this.itypes = itypes;
	}
	public String getItypes(int idx) {
		return itypes.get(idx);
	}
	public void setItypes(int idx, String itype) {
		this.itypes.set(idx, itype);
	}

	public List<Long> getConsents() {
		return consents;
	}
	public void setConsents(List<Long> consents) {
		this.consents = consents;
	}
	public Long getConsents(int idx) {
		return consents.get(idx);
	}
	public void setConsents(int idx, Long consent) {
		this.consents.set(idx, consent);
	}

	public String getOrigSubparts() {
		return origSubparts;
	}
	public void setOrigSubparts(String origSubparts) {
		this.origSubparts = origSubparts;
	}


	public List<Boolean> getReadOnlySubparts() {
		return readOnlySubparts;
	}
	public void setReadOnlySubparts(List<Boolean> readOnlySubparts) {
		this.readOnlySubparts = readOnlySubparts;
	}
	public Boolean getReadOnlySubparts(int idx) {
		return readOnlySubparts.get(idx);
	}
	public void setReadOnlySubparts(int idx, Boolean readOnlySubpart) {
		this.readOnlySubparts.set(idx, readOnlySubpart);
	}

	public List<String> getRooms() {
		return rooms;
	}
	public void setRooms(List<String> rooms) {
		this.rooms = rooms;
	}
	public String getRooms(int idx) {
		return rooms.get(idx);
	}
	public void setRooms(int idx, String rooms) {
		this.rooms.set(idx, rooms);
	}

	public Long getBannerCourseOfferingId() {
		return bannerCourseOfferingId;
	}
	public void setBannerCourseOfferingId(Long bannerCourseOfferingId) {
		this.bannerCourseOfferingId = bannerCourseOfferingId;
	}

	public List<String> getBannerSectionOriginalSectionIds() {
		return bannerSectionOriginalSectionIds;
	}
	public void setBannerSectionOriginalSectionIds(List<String> bannerSectionOriginalSectionIds) {
		this.bannerSectionOriginalSectionIds = bannerSectionOriginalSectionIds;
	}
	public String getBannerSectionOriginalSectionIds(int idx) {
		return bannerSectionOriginalSectionIds.get(idx);
	}
	public void setBannerSectionOriginalSectionIds(int idx, String bannerSectionOriginalSectionId) {
		this.bannerSectionOriginalSectionIds.set(idx, bannerSectionOriginalSectionId);
	}

	public Long getBannerConfigId() {
		return bannerConfigId;
	}
	public void setBannerConfigId(Long bannerConfigId) {
		this.bannerConfigId = bannerConfigId;
	}

	public Boolean getConfigIsEditable() {
		return configIsEditable;
	}
	public void setConfigIsEditable(Boolean configIsEditable) {
		this.configIsEditable = configIsEditable;
	}

	public Boolean getShowLabHours() {
		return showLabHours;
	}
	public void setShowLabHours(Boolean showLabHours) {
		this.showLabHours = showLabHours;
	}

	public Boolean getShowLimitOverride() {
		return showLimitOverride;
	}
	public void setShowLimitOverride(Boolean showLimitOverride) {
		if (ApplicationProperties.getProperty("tmtbl.banner.section.limit.overrides_allowed", "true").equalsIgnoreCase("true")) {
			this.showLimitOverride = showLimitOverride;
		} else {
			this.showLimitOverride = Boolean.valueOf(false);	
		}
	}

	public List<String> getLimitOverrides() {
		return limitOverrides;
	}
	public void setLimitOverrides(List<String> limitOverrides) {
		this.limitOverrides = limitOverrides;
	}
	public String getLimitOverrides(int idx) {
		return limitOverrides.get(idx);
	}
	public void setLimitOverrides(int idx, String limitOverride) {
		this.limitOverrides.set(idx, limitOverride);
	}

	public List<String> getCourseCreditOverrides() {
		return courseCreditOverrides;
	}
	public void setCourseCreditOverrides(List<String> courseCreditOverrides) {
		this.courseCreditOverrides = courseCreditOverrides;
	}
	public String getCourseCreditOverrides(int idx) {
		return courseCreditOverrides.get(idx);
	}
	public void setCourseCreditOverrides(int idx, String courseCreditOverride) {
		this.courseCreditOverrides.set(idx, courseCreditOverride);
	}

	public List<Integer> getClassLimits() {
		return classLimits;
	}
	public void setClassLimits(List<Integer> classLimits) {
		this.classLimits = classLimits;
	}
	public Integer getClassLimits(int idx) {
		return classLimits.get(idx);
	}
	public void setClassLimits(int idx, Integer classLimit) {
		this.classLimits.set(idx, classLimit);
	}

	public List<String> getCourseCredits() {
		return courseCredits;
	}
	public void setCourseCredits(List<String> courseCredits) {
		this.courseCredits = courseCredits;
	}
	public String getCourseCredits(int idx) {
		return courseCredits.get(idx);
	}
	public void setCourseCredits(int idx, String courseCredit) {
		this.courseCredits.set(idx, courseCredit);
	}

	public List<String> getDefaultCampus() {
		return defaultCampus;
	}
	public void setDefaultCampus(List<String> defaultCampus) {
		this.defaultCampus = defaultCampus;
	}
	public String getDefaultCampus(int idx) {
		return defaultCampus.get(idx);
	}
	public void setDefaultCampus(int idx, String defaultCampus) {
		this.defaultCampus.set(idx, defaultCampus);
	}

	public List<Long> getCampusOverrides() {
		return campusOverrides;
	}
	public void setCampusOverrides(List<Long> campusOverrides) {
		this.campusOverrides = campusOverrides;
	}
	public Long getCampusOverrides(int idx) {
		return campusOverrides.get(idx);
	}
	public void setCampusOverrides(int idx, Long campusOverride) {
		this.campusOverrides.set(idx, campusOverride);
	}

	public List<String> getCampuses() {
		return campuses;
	}
	public void setCampuses(List campuses) {
		this.campuses = campuses;
	}
	public String getCampuses(int idx) {
		return campuses.get(idx);
	}
	public void setCampuses(int idx, String campus) {
		this.campuses.set(idx, campus);
	}

	public List<BannerCampusOverride> getBannerCampusOverrides() {
		return bannerCampusOverrides;
	}
	public void setBannerCampusOverrides(List<BannerCampusOverride> bannerCampusOverrides) {
		this.bannerCampusOverrides = bannerCampusOverrides;
	}
	public List<IdValue> getBannerCampusOverrideOptions(int index) {
		List<IdValue> ret = new ArrayList<IdValue>();
		ret.add(new IdValue(-1l, BMSG.defaultCampusOverride(getDefaultCampus(index))));
		for (BannerCampusOverride bco: getBannerCampusOverrides()) {
			if (bco.isVisible() || bco.getUniqueId().equals(getCampusOverrides(index)))
				ret.add(new IdValue(bco.getUniqueId(), bco.getBannerCampusCode() + " - " + bco.getBannerCampusName()));
		}
		return ret;
	}
}
