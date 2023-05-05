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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.hibernate.Transaction;
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.comparators.ColleagueRestrictionComparator;
import org.unitime.colleague.model.dao.ColleagueRestrictionDAO;
import org.unitime.colleague.model.dao.ColleagueSectionDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.form.UniTimeForm;
import org.unitime.timetable.solver.ClassAssignmentProxy;


/**
 * @author Stephanie Schluttenhofer
 */
public class SectionRestrictionAssignmentForm implements UniTimeForm {
	private static final long serialVersionUID = -203441190483028649L;
	protected static final CourseMessages MSG = Localization.create(CourseMessages.class);
	protected final static ColleagueMessages CMSG = Localization.create(ColleagueMessages.class);
	
	private String op;
    private Long subjectAreaId;
	private Long instrOfferingId;
	private Long courseOfferingId;
    private String instrOfferingName;
	private Integer instrOffrConfigLimit;
	private Long instrOffrConfigId;
	private Integer deletedRestrRowNum;
    private Long nextId;
    private Long previousId;
	private ClassAssignmentProxy proxy;
	private Integer addRestrictionId;
	private Boolean displayExternalId;

	private List<Long> sectionIds;
	private List<String> sectionLabels;
	private List<String> sectionLabelIndents;
	private List<Long> restrictionUids;
	private List<String> times;
	private List<String> rooms;
	private List<Boolean> allowDeletes;
	private List<Boolean> readOnlySections;
	private List<Boolean> sectionHasErrors;
	private List<Boolean> showDisplay;
	private List<String> externalIds;
	
	public SectionRestrictionAssignmentForm() {
		reset();
	}
	
	@Override
	public void reset() {
		op = "";
        nextId = previousId = null;
        subjectAreaId = 0l;
    	instrOfferingId = 0l;
    	courseOfferingId = 0l;
        instrOfferingName = null;
    	instrOffrConfigLimit = 0;
    	instrOffrConfigId = 0l;
    	deletedRestrRowNum = null;
    	displayExternalId = false;
    	proxy = null;	
    	resetLists();
	}

	private void resetLists() {
    	sectionIds = new ArrayList<Long>();
    	sectionLabels = new ArrayList<String>();
    	sectionLabelIndents = new ArrayList<String>();
    	restrictionUids = new ArrayList<Long>();
    	times = new ArrayList<String>();
    	rooms = new ArrayList<String>();
    	allowDeletes = new ArrayList<Boolean>();
    	readOnlySections = new ArrayList<Boolean>();
    	sectionHasErrors = new ArrayList<Boolean>();
    	showDisplay = new ArrayList<Boolean>();
    	externalIds = new ArrayList<String>();
	}

	@Override
	public void validate(UniTimeAction action) {
        if (op.equals(CMSG.actionUpdateSectionRestrictionAssignment()) || op.equals(MSG.actionNextIO()) || op.equals(MSG.actionPreviousIO())) {	
            // Check Added Restrictions
	        for (int i = 0; i < sectionIds.size(); i++) {
	        	Long sectionId = sectionIds.get(i);
	        	Long restrictionUid = restrictionUids.get(i);
	        	for (int j = i + 1; j < sectionIds.size(); j++) {
	        		if (restrictionUids.get(j) != null) {
		        		if(sectionIds.get(j).equals(sectionId) && restrictionUids.get(j).equals(restrictionUid)) {
		        			action.addFieldError("form.duplicateRestriction", CMSG.errorDuplicateRestrictionForSection());
		        		}
	        		}
	        	}
	        }
        }
	}

	public void addToSections(ColleagueSection section, Boolean isReadOnly, String indent){
		ArrayList<ColleagueRestriction> restrictions = new ArrayList(section.getRestrictions());
		Collections.sort(restrictions, new ColleagueRestrictionComparator());
		ColleagueRestriction restriction = null;
		int i = 0;
		do {
			if(restrictions.size() > 0) {
				restriction =  restrictions.get(i);
			}
			// Only display the class name and display flag for the first instructor
			if(i == 0) {
				this.sectionLabels.add(section.colleagueSectionLabel());
				this.showDisplay.add(true);
				this.times.add(section.buildAssignedTimeHtml(getProxy()));
				this.rooms.add(section.buildAssignedRoomHtml(getProxy()));
				this.externalIds.add(section.getColleagueId() == null?"":section.getColleagueId());
			}
			else {
				this.sectionLabels.add("");
				this.showDisplay.add(false);
				this.times.add("");
				this.rooms.add("");
				this.externalIds.add("");
			}
			this.sectionLabelIndents.add(indent);
			this.sectionIds.add(section.getUniqueId());
			this.readOnlySections.add(isReadOnly);
			this.sectionHasErrors.add(false);
	
			if(restrictions.size() > 0) {
				this.restrictionUids.add(restriction.getUniqueId());
			}
			else {
				this.restrictionUids.add(-1l);
			}
			
			this.allowDeletes.add(restrictions.size() > 1);
		} while (++i < restrictions.size());
	}

	public void deleteRestriction() {
		int index = deletedRestrRowNum;
		int firstIndex = index;
		while (firstIndex>0 && sectionIds.get(firstIndex-1).equals(sectionIds.get(index)))
			firstIndex--;
		int lastIndex = index;
		while (lastIndex+1<sectionIds.size() && sectionIds.get(lastIndex+1).equals(sectionIds.get(index)))
			lastIndex++;
		sectionIds.remove(index);
		sectionLabels.remove(index==firstIndex?index+1:index);
		sectionLabelIndents.remove(index==firstIndex?index+1:index);
		sectionHasErrors.remove(index);
		restrictionUids.remove(index);
		times.remove(index==firstIndex?index+1:index);
		rooms.remove(index==firstIndex?index+1:index);
		allowDeletes.remove(index);
		if (firstIndex+1==lastIndex) {
			allowDeletes.set(firstIndex, Boolean.FALSE);
		}
		showDisplay.remove(index==firstIndex?index+1:index);
		readOnlySections.remove(index);
		externalIds.remove(index==firstIndex?index+1:index);
	}

	public void addRestriction() {
		int pos = getAddRestrictonId();
		this.sectionLabels.add(pos + 1, "");
		this.showDisplay.add(pos + 1, Boolean.FALSE);
		this.times.add(pos + 1, "");
		this.rooms.add(pos + 1, "");
		this.sectionLabelIndents.add(pos + 1, this.sectionLabelIndents.get(pos));
		this.sectionIds.add(pos + 1, this.sectionIds.get(pos));
		this.readOnlySections.add(pos + 1, this.readOnlySections.get(pos));
		this.sectionHasErrors.add(pos + 1, Boolean.FALSE);
		this.restrictionUids.add(pos + 1, -1l);
		this.allowDeletes.set(pos, Boolean.TRUE);
		this.allowDeletes.add(pos + 1, Boolean.TRUE);
		this.externalIds.add(pos + 1, "");
	}

	public void updateSections() throws Exception {
	    ColleagueSectionDAO cdao = new ColleagueSectionDAO();
	    for (int i = 0; i < sectionIds.size(); ) {
	    	if (getReadOnlySections(i)) {
	    		i++;
	    		continue;
	    	}
	    	
			Long sectionId = sectionIds.get(i);
		    ColleagueSection c = cdao.get(sectionId);

		    org.hibernate.Session hibSession = cdao.getSession();
        	Transaction tx = hibSession.beginTransaction();

            // Clear all restrictions
            
        	c.deleteRestrictions(hibSession);
            
            // Save restriction data to section
            for ( ; i < sectionIds.size(); i++) {
            	boolean sameSection = (sectionIds.get(i)).equals(sectionId);
            	if (!sameSection)	{
            		break;
            	}
                Long restrictionId = getRestrictionUids(i);
                if (restrictionId != null && restrictionId >= 0) {
	                ColleagueRestriction colleagueRestriction =  new ColleagueRestrictionDAO().get(restrictionId);
	                c.addToRestrictions(colleagueRestriction);
	            };
            };

        	try {
                hibSession.merge(c);
	            tx.commit();
        	} catch (Exception e) {
        		tx.rollback(); throw e;
        	}
		}
	}

	public void unassignAllRestrictions() throws Exception {
	    ColleagueSectionDAO cdao = new ColleagueSectionDAO();
	    for (int i = 0; i < sectionIds.size(); i++ ) {
	    	if (getReadOnlySections(i)) {
	    		i++;
	    		continue;
	    	}
	    	
			Long sectionId = sectionIds.get(i);
		    ColleagueSection c = cdao.get(sectionId);

		    org.hibernate.Session hibSession = cdao.getSession();
        	Transaction tx = hibSession.beginTransaction();
        	try {
    		    c.deleteRestrictions(hibSession);
                hibSession.merge(c);
	            tx.commit();
        	} catch (Exception e) {
        		tx.rollback(); throw e;
        	}
		}
	    this.getRestrictionUids().clear();
	}

	public String getOp() {
		return op;
	}

	public void setOp(String op) {
		this.op = op;
	}

	public List<Boolean> getAllowDeletes() {
		return allowDeletes;
	}
	public void setAllowDeletes(List<Boolean> allowDeletes) {
		this.allowDeletes = allowDeletes;
	}
	public Boolean getAllowDeletes(int idx) {
		return allowDeletes.get(idx);
	}
	public void setAllowDeletes(int idx, Boolean allowDelete) {
		this.allowDeletes.set(idx, allowDelete);
	}

	public List<Long> getSectionIds() {
		return sectionIds;
	}
	public void setSectionIds(List<Long> sectionIds) {
		this.sectionIds = sectionIds;
	}
	public Long getSectionIds(int idx) {
		return sectionIds.get(idx);
	}
	public void setSectionIds(int idx, Long sectionId) {
		this.sectionIds.set(idx, sectionId);
	}

	public List<String> getSectionLabelIndents() {
		return sectionLabelIndents;
	}
	public void setSectionLabelIndents(List<String> sectionLabelIndents) {
		this.sectionLabelIndents = sectionLabelIndents;
	}
	public String getSectionLabelIndents(int idx) {
		return sectionLabelIndents.get(idx);
	}
	public void setSectionLabelIndents(int idx, String sectionLabelIndent) {
		this.sectionLabelIndents.set(idx, sectionLabelIndent);
	}

	public List<String> getSectionLabels() {
		return sectionLabels;
	}
	public void setSectionLabels(List<String> sectionLabels) {
		this.sectionLabels = sectionLabels;
	}
	public String getSectionLabels(int idx) {
		return sectionLabels.get(idx);
	}
	public void setSectionLabels(int idx, String sectionLabel) {
		this.sectionLabels.set(idx, sectionLabel);
	}

	public List<Long> getRestrictionUids() {
		return restrictionUids;
	}
	public void setRestrictionUids(List<Long> restrictionUids) {
		this.restrictionUids = restrictionUids;
	}
	public Long getRestrictionUids(int idx) {
		return restrictionUids.get(idx);
	}
	public void setRestrictionUids(int idx, Long restrictionUid) {
		this.restrictionUids.set(idx, restrictionUid);
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
	public void setRooms(int idx, String room) {
		this.rooms.set(idx, room);
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

	public Long getInstrOfferingId() {
		return instrOfferingId;
	}
	public void setInstrOfferingId(Long instrOfferingId) {
		this.instrOfferingId = instrOfferingId;
	}

	public Long getCourseOfferingId() {
		return courseOfferingId;
	}
	public void setCourseOfferingId(Long courseOfferingId) {
		this.courseOfferingId = courseOfferingId;
	}

	public String getInstrOfferingName() {
		return instrOfferingName;
	}
	public void setInstrOfferingName(String instrOfferingName) {
		this.instrOfferingName = instrOfferingName;
	}

	public Long getInstrOffrConfigId() {
		return instrOffrConfigId;
	}
	public void setInstrOffrConfigId(Long instrOffrConfigId) {
		this.instrOffrConfigId = instrOffrConfigId;
	}

	public Integer getInstrOffrConfigLimit() {
		return instrOffrConfigLimit;
	}
	public void setInstrOffrConfigLimit(Integer instrOffrConfigLimit) {
		this.instrOffrConfigLimit = instrOffrConfigLimit;
	}

	public List<Boolean> getReadOnlySections() {
		return readOnlySections;
	}
	public void setReadOnlySections(List<Boolean> readOnlySections) {
		this.readOnlySections = readOnlySections;
	}
	public Boolean getReadOnlySections(int idx) {
		return readOnlySections.get(idx);
	}
	public void setReadOnlySections(int idx, Boolean readOnlySection) {
		this.readOnlySections.set(idx, readOnlySection);
	}

	public List<Boolean> getSectionHasErrors() {
		return sectionHasErrors;
	}
	public void setSectionHasErrors(List<Boolean> sectionHasErrors) {
		this.sectionHasErrors = sectionHasErrors;
	}
	public Boolean getSectionHasErrors(int idx) {
		return sectionHasErrors.get(idx);
	}
	public void setSectionHasErrors(int idx, Boolean sectionHasError) {
		this.sectionHasErrors.set(idx, sectionHasError);
	}

	public Long getSubjectAreaId() {
		return subjectAreaId;
	}
	public void setSubjectAreaId(Long subjectAreaId) {
		this.subjectAreaId = subjectAreaId;
	}

	public ClassAssignmentProxy getProxy() {
		return proxy;
	}
	public void setProxy(ClassAssignmentProxy proxy) {
		this.proxy = proxy;
	}

	public Long getNextId() {
		return nextId;
	}
	public void setNextId(Long nextId) {
		this.nextId = nextId;
	}

	public Long getPreviousId() {
		return previousId;
	}
	public void setPreviousId(Long previousId) {
		this.previousId = previousId;
	}

	public Integer getDeletedRestrRowNum() {
		return deletedRestrRowNum;
	}
	public void setDeletedRestrRowNum(Integer deletedRestrRowNum) {
		this.deletedRestrRowNum = deletedRestrRowNum;
	}

	public List<Boolean> getShowDisplay() {
		return showDisplay;
	}
	public void setShowDisplay(List<Boolean> showDisplay) {
		this.showDisplay = showDisplay;
	}
	public Boolean getShowDisplay(int idx) {
		return showDisplay.get(idx);
	}
	public void setShowDisplay(int idx, Boolean showDisplay) {
		this.showDisplay.set(idx, showDisplay);
	}

	public Integer getAddRestrictonId() {
		return addRestrictionId;
	}
	public void setAddRestrictionId(Integer addRestrictionId) {
		this.addRestrictionId = addRestrictionId;
	}

	public List<String> getExternalIds() {
		return externalIds;
	}
	public void setExternalIds(List<String> externalIds) {
		this.externalIds = externalIds;
	}
	public String getExternalIds(int idx) {
		return externalIds.get(idx);
	}
	public void setExternalIds(int idx, String externalId) {
		this.externalIds.set(idx, externalId);
	}

	public Boolean getDisplayExternalId() {
		return displayExternalId;
	}
	public void setDisplayExternalId(Boolean displayExternalId) {
		this.displayExternalId = displayExternalId;
	}
}
