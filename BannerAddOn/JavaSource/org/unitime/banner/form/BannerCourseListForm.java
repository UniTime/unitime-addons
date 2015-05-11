/*
 * UniTime 3.2 (University Timetabling Application)
 * Copyright (C) 2010, UniTime LLC, and individual contributors
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
package org.unitime.banner.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.Roles;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.util.Constants;


/**
 * @author Stephanie Schluttenhofer
 */
public class BannerCourseListForm extends ActionForm {

	/**
	 * Comment for <code>serialVersionUID</code>
	 */
	private static final long serialVersionUID = -8198014538320654542L;

	private TreeSet<InstructionalOffering> instructionalOfferings;

	private Collection subjectAreas;

	private String subjectAreaId;

	private String courseNbr;

	private Boolean showNotOffered;

	private String buttonAction;

	private String subjectAreaAbbv;

	private Boolean isControl;

	private String ctrlInstrOfferingId;
	
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
		this.courseNbr = courseNbr.toUpperCase();
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

	// --------------------------------------------------------- Methods
	/**
	 * Method reset
	 * 
	 * @param mapping
	 * @param request
	 */
	public void reset(ActionMapping mapping, HttpServletRequest request) {

		courseNbr = "";
		instructionalOfferings = new TreeSet<InstructionalOffering>();
		subjectAreas = new ArrayList();
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
	public void setSubjectAreas(Collection subjectAreas) {
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.apache.struts.action.ActionForm#validate(org.apache.struts.action.ActionMapping,
	 *      javax.servlet.http.HttpServletRequest)
	 */
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		if (subjectAreaId == null || subjectAreaId.trim().length() == 0 || subjectAreaId.equals(Constants.BLANK_OPTION_VALUE)) {
			errors.add("subjectAreaId", new ActionMessage("errors.required", "Subject Area"));
		}

		return errors;
	}

	public void setCollections(SessionContext sessionContext, TreeSet<InstructionalOffering> instructionalOfferings) throws Exception {
		setSubjectAreas(SubjectArea.getUserSubjectAreas(sessionContext.getUser()));
		setInstructionalOfferings(instructionalOfferings);

	}


    protected void finalize() throws Throwable {
        Debug.debug("!!! Finalizing InstructionalOfferingListForm ... ");
        instructionalOfferings=null;
        subjectAreas=null;
        subjectAreaId=null;
        courseNbr=null;
        showNotOffered=null;
        buttonAction=null;
        subjectAreaAbbv=null;
        isControl=null;
        ctrlInstrOfferingId=null;
        super.finalize();
    }
	
}
