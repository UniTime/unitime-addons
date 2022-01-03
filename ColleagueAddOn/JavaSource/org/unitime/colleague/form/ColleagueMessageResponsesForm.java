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

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * based on code contributed by Dagmar Murray
 */
public class ColleagueMessageResponsesForm extends ActionForm {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4347509521815310418L;
	private int iN;
    private Long iDepartmentId, iSubjAreaId, iManagerId;
	private String iCourseNumber;
	private String iColleagueId;
	private String iRespType;
	private String iStartDate;
	private String iStopDate;
	private String iMessage;
	private String iOp;
	private Boolean iShowHistory;
	private Boolean iActionAudit;
	private Boolean iActionUpdate;
	private Boolean iActionDelete;
	private Boolean iTypeSuccess;
	private Boolean iTypeError;
	private Boolean iTypeWarning;

	public String getMessage() {
		return iMessage;
	}

	public void setMessage(String message) {
		iMessage = message;
	}

	public String getRespType() {
		return iRespType;
	}

	public void setRespType(String respType) {
		iRespType = respType;
	}

	public String getCourseNumber() {
		return iCourseNumber;
	}

	public void setCourseNumber(String courseNumber) {
		iCourseNumber = courseNumber;
	}
	
	public String getColleagueId() {
		return iColleagueId;
	}
	
	public void setColleagueId(String colleagueId) {
		iColleagueId = colleagueId;
	}

	public String getStartDate() {
		return iStartDate;
	}

	public void setStartDate(String startDate) {
		this.iStartDate = startDate;
	}

	public void setStopDate(String stopDate) {
		this.iStopDate = stopDate;
	}

	public String getStopDate() {
		return iStopDate;
	}

	public String getOp() {
		return iOp;
	}

	public void setOp(String op) {
		this.iOp = op;
	}
	
	public Boolean getShowHistory() {
		return iShowHistory;
	}

	public void setShowHistory(Boolean showHistory) {
		iShowHistory = showHistory;
	}
	
	public Boolean getActionAudit() {
		return iActionAudit;
	}

	public void setActionAudit(Boolean actionAudit) {
		iActionAudit = actionAudit;
	}

	public Boolean getActionUpdate() {
		return iActionUpdate;
	}

	public void setActionUpdate(Boolean actionUpdate) {
		iActionUpdate = actionUpdate;
	}

	public Boolean getActionDelete() {
		return iActionDelete;
	}

	public void setActionDelete(Boolean actionDelete) {
		iActionDelete = actionDelete;
	}

	public Boolean getTypeSuccess() {
		return iTypeSuccess;
	}

	public void setTypeSuccess(Boolean typeSuccess) {
		iTypeSuccess = typeSuccess;
	}

	public Boolean getTypeError() {
		return iTypeError;
	}

	public void setTypeError(Boolean typeError) {
		iTypeError = typeError;
	}

	public Boolean getTypeWarning() {
		return iTypeWarning;
	}

	public void setTypeWarning(Boolean typeWarning) {
		iTypeWarning = typeWarning;
	}

	public ActionErrors validate(ActionMapping mapping,
			HttpServletRequest request) {
		ActionErrors errors = new ActionErrors();

		return errors;
	}

	public void reset(ActionMapping mapping, HttpServletRequest request) {
		iN = 100;
		iDepartmentId = Long.valueOf(-1);
        iSubjAreaId = Long.valueOf(-1);
        iManagerId = Long.valueOf(-1);
		iCourseNumber = null;
		iColleagueId = null;
		iRespType = null;
		iStartDate = null;
		iStopDate = null;
		iMessage = null;
		iShowHistory = null;
		iActionUpdate = null;
		iActionAudit = null;
		iActionDelete = null;
		iTypeSuccess = null;
		iTypeError = null; 
		iTypeWarning = null;
	}

	public int getN() {
		return iN;
	}

	public void setN(int n) {
		iN = n;
	}

	public Long getSubjAreaId() {
		return iSubjAreaId;
	}

	public void setSubjAreaId(Long subjAreaId) {
		iSubjAreaId = subjAreaId;
	}
    public Long getDepartmentId() { return iDepartmentId; }
    public void setDepartmentId(Long departmentId) { iDepartmentId = departmentId; }
    public Long getManagerId() { return iManagerId; }
    public void setManagerId(Long managerId) { iManagerId = managerId; }

	public void load(HttpServletRequest request) {

		Integer n = (Integer) request.getSession().getAttribute(
				"ColleagueMessageResponses.N");

		setN(n == null ? 100 : n.intValue());

        setDepartmentId((Long)request.getSession().getAttribute("ColleagueMessageResponses.DepartmentId"));

		setManagerId((Long)request.getSession().getAttribute("ColleagueMessageResponses.ManagerId"));
		setManagerId(Long.valueOf(-1));
		
		setSubjAreaId((Long) request.getSession().getAttribute(
				"ColleagueMessageResponses.SubjAreaId"));
		setCourseNumber((String) request.getSession().getAttribute(
				"ColleagueMessageResponses.CourseNumber"));
		setColleagueId((String) request.getSession().getAttribute(
				"ColleagueMessageResponses.ColleagueId"));
		setStartDate((String) request.getSession().getAttribute(
				"ColleagueMessageResponses.StartDate"));
		setStopDate((String) request.getSession().getAttribute(
				"ColleagueMessageResponses.StopDate"));
		setMessage((String) request.getSession().getAttribute(
				"ColleagueMessageResponses.Message"));
		setOp((String) request.getSession().getAttribute(
				"ColleagueMessageResponses.Op"));
		setShowHistory((Boolean) request.getSession().getAttribute(
				"ColleagueMessageResponses.ShowHistory"));
		if (request.getSession().getAttribute("ColleagueMessageResponses.ActionAudit") == null ) {
			setActionAudit(Boolean.valueOf(true));
		} else {
			setActionAudit((Boolean) request.getSession().getAttribute(
				"ColleagueMessageResponses.ActionAudit"));
		}
		if (request.getSession().getAttribute("ColleagueMessageResponses.ActionUpdate") == null ) {
			setActionUpdate(Boolean.valueOf(true));
		} else {
			setActionUpdate((Boolean) request.getSession().getAttribute(
				"ColleagueMessageResponses.ActionUpdate"));
		}
		if (request.getSession().getAttribute("ColleagueMessageResponses.ActionDelete") == null ) {
			setActionDelete(Boolean.valueOf(true));
		} else {
			setActionDelete((Boolean) request.getSession().getAttribute(
				"ColleagueMessageResponses.ActionDelete"));
		}
		if (request.getSession().getAttribute("ColleagueMessageResponses.TypeSuccess") == null ) {
			setTypeSuccess(Boolean.valueOf(true));
		} else {
			setTypeSuccess((Boolean) request.getSession().getAttribute(
				"ColleagueMessageResponses.TypeSuccess"));
		}
		if (request.getSession().getAttribute("ColleagueMessageResponses.TypeError") == null ) {
			setTypeError(Boolean.valueOf(true));
		} else {
			setTypeError((Boolean) request.getSession().getAttribute(
				"ColleagueMessageResponses.TypeError"));
		}
		if (request.getSession().getAttribute("ColleagueMessageResponses.TypeWarning") == null ) {
			setTypeWarning(Boolean.valueOf(true));
		} else {
			setTypeWarning((Boolean) request.getSession().getAttribute(
				"ColleagueMessageResponses.TypeWarning"));
		}

	}

	public void save(HttpServletRequest request) {
		request.getSession().setAttribute("ColleagueMessageResponses.N",
				Integer.valueOf(getN()));

		if (getSubjAreaId() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.SubjAreaId");
		} else {
			request.getSession().setAttribute(
					"ColleagueMessageResponses.SubjAreaId", getSubjAreaId());
		}
	       if (getDepartmentId()==null)
	            request.getSession().removeAttribute("ColleagueMessageResponses.DepartmentId");
	        else
	            request.getSession().setAttribute("ColleagueMessageResponses.DepartmentId", getDepartmentId());
	        if (getManagerId()==null)
	            request.getSession().removeAttribute("ColleagueMessageResponses.ManagerId");
	        else
	            request.getSession().setAttribute("ColleagueMessageResponses.ManagerId", getManagerId());
		
		if (getCourseNumber() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.CourseNumber");
		} else {
			request.getSession().setAttribute(
					"ColleagueMessageResponses.CourseNumber", getCourseNumber());
		}
		if (getColleagueId() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.ColleagueId");
		} else {
			request.getSession().setAttribute(
					"ColleagueMessageResponses.ColleagueId", getColleagueId());
		}
		if (getStartDate() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.StartDate");
		} else {
			request.getSession().setAttribute(
					"ColleagueMessageResponses.StartDate", getStartDate());
		}
		if (getStopDate() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.StopDate");
		} else {
			request.getSession().setAttribute(
					"ColleagueMessageResponses.StopDate", getStopDate());
		}
		if (getMessage() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.Message");
		} else {
			request.getSession().setAttribute("ColleagueMessageResponses.Message",
					getMessage());
		}
		
		if (getShowHistory() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.ShowHistory");
		} else {
			request.getSession().setAttribute("ColleagueMessageResponses.ShowHistory",
					getShowHistory());
		}
		
		
		if (getActionAudit() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.ActionAudit");
		} else {
			request.getSession().setAttribute("ColleagueMessageResponses.ActionAudit",
					getActionAudit());
		}
		if (getActionUpdate() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.ActionUpdate");
		} else {
			request.getSession().setAttribute("ColleagueMessageResponses.ActionUpdate",
					getActionUpdate());
		}
		if (getActionDelete() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.ActionDelete");
		} else {
			request.getSession().setAttribute("ColleagueMessageResponses.ActionDelete",
					getActionDelete());
		}
		if (getTypeSuccess() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.TypeSuccess");
		} else {
			request.getSession().setAttribute("ColleagueMessageResponses.TypeSuccess",
					getTypeSuccess());
		}
		if (getTypeError() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.TypeError");
		} else {
			request.getSession().setAttribute("ColleagueMessageResponses.TypeError",
					getTypeError());
		}
		if (getTypeWarning() == null) {
			request.getSession().removeAttribute(
					"ColleagueMessageResponses.TypeWarning");
		} else {
			request.getSession().setAttribute("ColleagueMessageResponses.TypeWarning",
					getTypeWarning());
		}
		
		if (getOp() == null) {
			request.getSession().removeAttribute("ColleagueMessageResponses.Op");
		} else {
			request.getSession().setAttribute("ColleagueMessageResponses.Op",
					getOp());
		}
	}
}
