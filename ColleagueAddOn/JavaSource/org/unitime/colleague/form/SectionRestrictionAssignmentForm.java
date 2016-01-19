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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.hibernate.Transaction;
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.comparators.ColleagueRestrictionComparator;
import org.unitime.colleague.model.dao.ColleagueRestrictionDAO;
import org.unitime.colleague.model.dao.ColleagueSectionDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.ColleagueMessages;
import org.unitime.timetable.model.Preference;
import org.unitime.timetable.solver.ClassAssignmentProxy;
import org.unitime.timetable.util.DynamicList;
import org.unitime.timetable.util.DynamicListObjectFactory;


/**
 * @author Stephanie Schluttenhofer
 */
public class SectionRestrictionAssignmentForm extends ActionForm {

	protected final static ColleagueMessages MSG = Localization.create(ColleagueMessages.class);
	
	private String op;
    private Integer subjectAreaId;
	private Long instrOfferingId;
	private Long courseOfferingId;
    private String instrOfferingName;
	private Integer instrOffrConfigLimit;
	private Long instrOffrConfigId;
	private String deletedRestrRowNum;
    private String nextId;
    private String previousId;
	private ClassAssignmentProxy proxy;
	private String addRestrictionId;
	private Boolean displayExternalId;

	private List<String> sectionIds;
	private List<String> sectionLabels;
	private List<String> sectionLabelIndents;
	private List<String> restrictionUids;
	private List times;
	private List<String> rooms;
	private List<Boolean> allowDeletes;
	private List<String> readOnlySections;
	private List<Boolean> sectionHasErrors;
	private List<Boolean> showDisplay;
	private List<String> externalIds;
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -203441190483028649L;
	/**
	 * 
	 */

    // --------------------------------------------------------- Classes

    /** Factory to create dynamic list element for Course Offerings */
    protected DynamicListObjectFactory factoryClasses = new DynamicListObjectFactory() {
        public Object create() {
            return new String(Preference.BLANK_PREF_VALUE);
        }
    };
		
	public void reset(ActionMapping arg0, HttpServletRequest arg1) {
		op = "";
        nextId = previousId = null;
        subjectAreaId = new Integer(0);
    	instrOfferingId = new Long(0);
    	courseOfferingId = new Long(0);
        instrOfferingName = null;
    	instrOffrConfigLimit = new Integer(0);
    	instrOffrConfigId = new Long(0);
    	deletedRestrRowNum = null;
    	displayExternalId = new Boolean(false);
    	proxy = null;	
    	resetLists();
	}

	private void resetLists() {
    	sectionIds = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	sectionLabels = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	sectionLabelIndents = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	restrictionUids = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	times = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	rooms = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	allowDeletes = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	readOnlySections = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	sectionHasErrors = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	showDisplay = DynamicList.getInstance(new ArrayList(), factoryClasses);
    	externalIds = DynamicList.getInstance(new ArrayList(), factoryClasses);
	}

	public ActionErrors validate(ActionMapping arg0, HttpServletRequest arg1) {
		ActionErrors errors = new ActionErrors();

        if(op.equals(MSG.actionUpdateSectionRestrictionAssignment()) || op.equals(MSG.actionNextIO()) || op.equals(MSG.actionPreviousIO())) {	
            // Check Added Restrictions
	        for (int i = 0; i < sectionIds.size(); i++) {
	        	String sectionId = (String) sectionIds.get(i);
	        	String restrictionUid = (String) restrictionUids.get(i);
	        	for (int j = i + 1; j < sectionIds.size(); j++) {
	        		if (((String) restrictionUids.get(j)).length() > 0) {
		        		if(sectionIds.get(j).equals(sectionId) && restrictionUids.get(j).equals(restrictionUid)) {
		        			errors.add("duplicateRestriction", new ActionMessage("errors.generic", MSG.errorDuplicateRestrictionForSection()));
		        		}
	        		}
	        	}
	        }
        }
        return errors;
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
				this.showDisplay.add(new Boolean(true));
				this.times.add(section.buildAssignedTimeHtml(getProxy()));
				this.rooms.add(section.buildAssignedRoomHtml(getProxy()));
				this.externalIds.add(section.getColleagueId() == null?"":section.getColleagueId().toString());
			}
			else {
				this.sectionLabels.add("");
				this.showDisplay.add(new Boolean(false));
				this.times.add("");
				this.rooms.add("");
				this.externalIds.add("");
			}
			this.sectionLabelIndents.add(indent);
			this.sectionIds.add(section.getUniqueId().toString());
			this.readOnlySections.add(isReadOnly.toString());
			this.sectionHasErrors.add(new Boolean(false));
	
			if(restrictions.size() > 0) {
				this.restrictionUids.add(restriction.getUniqueId().toString());
			}
			else {
				this.restrictionUids.add("");
			}
			
			this.allowDeletes.add(new Boolean(restrictions.size() > 1));
		} while (++i < restrictions.size());
	}

	public void deleteRestriction() {
		int index = Integer.parseInt(deletedRestrRowNum);
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
		int pos = new Integer(this.getAddRestrictonId()).intValue();
		this.sectionLabels.add(pos + 1, "");
		this.showDisplay.add(pos + 1, Boolean.FALSE);
		this.times.add(pos + 1, "");
		this.rooms.add(pos + 1, "");
		this.sectionLabelIndents.add(pos + 1, this.sectionLabelIndents.get(pos));
		this.sectionIds.add(pos + 1, this.sectionIds.get(pos));
		this.readOnlySections.add(pos + 1, this.readOnlySections.get(pos));
		this.sectionHasErrors.add(pos + 1, Boolean.FALSE);
		this.restrictionUids.add(pos + 1, "");
		this.allowDeletes.set(pos, Boolean.TRUE);
		this.allowDeletes.add(pos + 1, Boolean.TRUE);
		this.externalIds.add(pos + 1, "");
	}

	public void updateSections() throws Exception {
	    ColleagueSectionDAO cdao = new ColleagueSectionDAO();
	    for (int i = 0; i < sectionIds.size(); ) {
	    	if ("true".equals(getReadOnlySections().get(i))) {
	    		i++;
	    		continue;
	    	}
	    	
			String sectionId = (String) sectionIds.get(i);
		    ColleagueSection c = cdao.get(new Long(sectionId));

		    org.hibernate.Session hibSession = cdao.getSession();
        	Transaction tx = hibSession.beginTransaction();

            // Clear all restrictions
            
        	c.deleteRestrictions(hibSession);
            
            // Save restriction data to section
            for ( ; i < sectionIds.size(); i++) {
            	boolean sameSection = ((String) sectionIds.get(i)).equals(sectionId);
            	if (!sameSection)	{
            		break;
            	}
                String restrictionId = (String) getRestrictionUids().get(i);
                if (restrictionId.length() > 0  && !("-".equals(restrictionId))) {
	                ColleagueRestriction colleagueRestriction =  new ColleagueRestrictionDAO().get(new Long(restrictionId));
	                c.addTocolleagueRestrictions(colleagueRestriction);
	            };
            };

        	try {
                hibSession.saveOrUpdate(c);
	            tx.commit();
        	} catch (Exception e) {
        		tx.rollback(); throw e;
        	}
		}
	}

	public void unassignAllRestrictions() throws Exception {
	    ColleagueSectionDAO cdao = new ColleagueSectionDAO();
	    for (int i = 0; i < sectionIds.size(); i++ ) {
	    	if ("true".equals(getReadOnlySections().get(i))) {
	    		i++;
	    		continue;
	    	}
	    	
			String sectionId = (String) sectionIds.get(i);
		    ColleagueSection c = cdao.get(new Long(sectionId));

		    org.hibernate.Session hibSession = cdao.getSession();
        	Transaction tx = hibSession.beginTransaction();
        	try {
    		    c.deleteRestrictions(hibSession);
                hibSession.saveOrUpdate(c);
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

	public List getAllowDeletes() {
		return allowDeletes;
	}

	public void setAllowDeletes(List allowDeletes) {
		this.allowDeletes = allowDeletes;
	}

	public List getSectionIds() {
		return sectionIds;
	}

	public void setSectionIds(List sectionIds) {
		this.sectionIds = sectionIds;
	}

	public List getSectionLabelIndents() {
		return sectionLabelIndents;
	}

	public void setSectionLabelIndents(List sectionLabelIndents) {
		this.sectionLabelIndents = sectionLabelIndents;
	}

	public List getSectionLabels() {
		return sectionLabels;
	}

	public void setSectionLabels(List sectionLabels) {
		this.sectionLabels = sectionLabels;
	}

	public List getRestrictionUids() {
		return restrictionUids;
	}

	public void setRestrictionUids(List restrictionUids) {
		this.restrictionUids = restrictionUids;
	}

	public List getRooms() {
		return rooms;
	}

	public void setRooms(List rooms) {
		this.rooms = rooms;
	}

	public List getTimes() {
		return times;
	}

	public void setTimes(List times) {
		this.times = times;
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

	public List getReadOnlySections() {
		return readOnlySections;
	}

	public void setReadOnlySections(List readOnlySections) {
		this.readOnlySections = readOnlySections;
	}

	public List getSectionHasErrors() {
		return sectionHasErrors;
	}

	public void setSectionHasErrors(List sectionHasErrors) {
		this.sectionHasErrors = sectionHasErrors;
	}

	public Integer getSubjectAreaId() {
		return subjectAreaId;
	}

	public void setSubjectAreaId(Integer subjectAreaId) {
		this.subjectAreaId = subjectAreaId;
	}

	public ClassAssignmentProxy getProxy() {
		return proxy;
	}

	public void setProxy(ClassAssignmentProxy proxy) {
		this.proxy = proxy;
	}

	public String getNextId() {
		return nextId;
	}

	public void setNextId(String nextId) {
		this.nextId = nextId;
	}

	public String getPreviousId() {
		return previousId;
	}

	public void setPreviousId(String previousId) {
		this.previousId = previousId;
	}

	public String getDeletedRestrRowNum() {
		return deletedRestrRowNum;
	}

	public void setDeletedRestrRowNum(String deletedRestrRowNum) {
		this.deletedRestrRowNum = deletedRestrRowNum;
	}

	public List getShowDisplay() {
		return showDisplay;
	}

	public void setShowDisplay(List showDisplay) {
		this.showDisplay = showDisplay;
	}

	public String getAddRestrictonId() {
		return addRestrictionId;
	}

	public void setAddRestrictionId(String addRestrictionId) {
		this.addRestrictionId = addRestrictionId;
	}

	public List getExternalIds() {
		return externalIds;
	}

	public void setExternalIds(List externalIds) {
		this.externalIds = externalIds;
	}

	public Boolean getDisplayExternalId() {
		return displayExternalId;
	}

	public void setDisplayExternalId(Boolean displayExternalId) {
		this.displayExternalId = displayExternalId;
	}
}
