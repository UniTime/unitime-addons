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
import java.util.Collection;
import java.util.TreeSet;

import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.form.UniTimeForm;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;


/**
 * @author Stephanie Schluttenhofer
 */
public class BannerCourseListForm implements UniTimeForm {
	private static final long serialVersionUID = -8198014538320654542L;
	protected final static CourseMessages MSG = Localization.create(CourseMessages.class);

	private TreeSet<InstructionalOffering> instructionalOfferings;
	private Collection<SubjectArea> subjectAreas;
	private String subjectAreaId;
	private String courseNbr;
	private Boolean showNotOffered;
	private String buttonAction;
	private String subjectAreaAbbv;
	private Boolean isControl;
	private String ctrlInstrOfferingId;
	
	public BannerCourseListForm() {
		reset();
	}
	
	@Override
	public void validate(UniTimeAction action) {
		if (subjectAreaId == null || subjectAreaId.trim().isEmpty())
			action.addFieldError("subjectAreaIds", MSG.errorSubjectRequired());
	}
	
	@Override
	public void reset() {
		courseNbr = "";
		instructionalOfferings = new TreeSet<InstructionalOffering>();
		subjectAreas = new ArrayList<SubjectArea>();
	}

	
	/**
	 * @return Returns the ctrlInstrOfferingId.
	 */
	public String getCtrlInstrOfferingId() {
		return ctrlInstrOfferingId;
	}

	/**
	 * @param ctrlInstrOfferingId
	 *            The ctrlInstrOfferingId to set.
	 */
	public void setCtrlInstrOfferingId(String ctrlInstrOfferingId) {
		this.ctrlInstrOfferingId = ctrlInstrOfferingId;
	}

	/**
	 * @return Returns the isControl.
	 */
	public Boolean getIsControl() {
		return isControl;
	}

	/**
	 * @param isControl
	 *            The isControl to set.
	 */
	public void setIsControl(Boolean isControl) {
		this.isControl = isControl;
	}

	/**
	 * @return Returns the subjectAreaAbbv.
	 */
	public String getSubjectAreaAbbv() {
		return subjectAreaAbbv;
	}

	/**
	 * @param subjectAreaAbbv
	 *            The subjectAreaAbbv to set.
	 */
	public void setSubjectAreaAbbv(String subjectAreaAbbv) {
		this.subjectAreaAbbv = subjectAreaAbbv;
	}

	/**
	 * @return Returns the buttonAction.
	 */
	public String getButtonAction() {
		return buttonAction;
	}

	/**
	 * @param buttonAction
	 *            The buttonAction to set.
	 */
	public void setButtonAction(String buttonAction) {
		this.buttonAction = buttonAction;
	}

	/**
	 * @return Returns the courseNbr.
	 */
	public String getCourseNbr() {
		return courseNbr;
	}

	/**
	 * @param courseNbr
	 *            The courseNbr to set.
	 */
	public void setCourseNbr(String courseNbr) {
		this.courseNbr = courseNbr;
	}

	/**
	 * @return Returns the subjectAreaId.
	 */
	public String getSubjectAreaId() {
		return subjectAreaId;
	}

	/**
	 * @param subjectAreaId
	 *            The subjectAreaId to set.
	 */
	public void setSubjectAreaId(String subjectAreaId) {
		this.subjectAreaId = subjectAreaId;
	}

	/**
	 * @return Returns the bannerCoursesWithOfferings.
	 */
	public TreeSet<InstructionalOffering> getInstructionalOfferings() {
		return instructionalOfferings;
	}

	/**
	 * @param bannerCoursesWithOfferings
	 *            The bannerCoursesWithOfferings to set.
	 */
	public void setInstructionalOfferings(TreeSet<InstructionalOffering> instructionalOfferings) {
		this.instructionalOfferings = instructionalOfferings;
	}

	/**
	 * @return Returns the subjectAreas.
	 */
	public Collection getSubjectAreas() {
		return subjectAreas;
	}

	/**
	 * @param subjectAreas
	 *            The subjectAreas to set.
	 */
	public void setSubjectAreas(Collection<SubjectArea> subjectAreas) {
		this.subjectAreas = subjectAreas;
	}

	/**
	 * @return Returns the showNotOffered.
	 */
	public Boolean getShowNotOffered() {
		return showNotOffered;
	}

	/**
	 * @param showNotOffered
	 *            The showNotOffered to set.
	 */
	public void setShowNotOffered(Boolean showNotOffered) {
		this.showNotOffered = showNotOffered;
	}


	public void setCollections(SessionContext sessionContext, TreeSet<InstructionalOffering> instructionalOfferings) throws Exception {
		setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
		setInstructionalOfferings(instructionalOfferings);
	}
}
