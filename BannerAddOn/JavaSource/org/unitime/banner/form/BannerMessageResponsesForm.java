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

import jakarta.servlet.http.HttpServletRequest;

import org.unitime.timetable.action.UniTimeAction;
import org.unitime.timetable.form.UniTimeForm;

/**
 * based on code contributed by Dagmar Murray
 */
public class BannerMessageResponsesForm implements UniTimeForm {
	private static final long serialVersionUID = 4347509521815310418L;

	/**
	 * 
	 */
	private int iN;
    private Long iDepartmentId, iSubjAreaId, iManagerId;
	private String iCourseNumber;
	private String iCrn;
	private String iXlst;
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
	
	public BannerMessageResponsesForm() {
		reset();
	}

	public String getMessage() {
		return iMessage;
	}

	public void setMessage(String message) {
		iMessage = message;
	}

	public String getXlst() {
		return iXlst;
	}

	public void setXlst(String xlst) {
		iXlst = xlst;
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
	
	public String getCrn() {
		return iCrn;
	}
	
	public void setCrn(String crn) {
		iCrn = crn;
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

	@Override
	public void validate(UniTimeAction action) {
	}

	@Override
	public void reset() {
		iN = 100;
		iDepartmentId = Long.valueOf(-1);
        iSubjAreaId = Long.valueOf(-1);
        iManagerId = Long.valueOf(-1);
		iCourseNumber = null;
		iCrn = null;
		iXlst = null;
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
				"BannerMessageResponses.N");

		setN(n == null ? 100 : n.intValue());

        setDepartmentId((Long)request.getSession().getAttribute("BannerMessageResponses.DepartmentId"));

		setManagerId((Long)request.getSession().getAttribute("BannerMessageResponses.ManagerId"));
		setManagerId(Long.valueOf(-1));
		
		setSubjAreaId((Long) request.getSession().getAttribute(
				"BannerMessageResponses.SubjAreaId"));
		setCourseNumber((String) request.getSession().getAttribute(
				"BannerMessageResponses.CourseNumber"));
		setCrn((String) request.getSession().getAttribute(
				"BannerMessageResponses.Crn"));
		setXlst((String) request.getSession().getAttribute(
				"BannerMessageResponses.XlstGroup"));
		setStartDate((String) request.getSession().getAttribute(
				"BannerMessageResponses.StartDate"));
		setStopDate((String) request.getSession().getAttribute(
				"BannerMessageResponses.StopDate"));
		setMessage((String) request.getSession().getAttribute(
				"BannerMessageResponses.Message"));
		setOp((String) request.getSession().getAttribute(
				"BannerMessageResponses.Op"));
		setShowHistory((Boolean) request.getSession().getAttribute(
				"BannerMessageResponses.ShowHistory"));
		if (request.getSession().getAttribute("BannerMessageResponses.ActionAudit") == null ) {
			setActionAudit(Boolean.valueOf(true));
		} else {
			setActionAudit((Boolean) request.getSession().getAttribute(
				"BannerMessageResponses.ActionAudit"));
		}
		if (request.getSession().getAttribute("BannerMessageResponses.ActionUpdate") == null ) {
			setActionUpdate(Boolean.valueOf(true));
		} else {
			setActionUpdate((Boolean) request.getSession().getAttribute(
				"BannerMessageResponses.ActionUpdate"));
		}
		if (request.getSession().getAttribute("BannerMessageResponses.ActionDelete") == null ) {
			setActionDelete(Boolean.valueOf(true));
		} else {
			setActionDelete((Boolean) request.getSession().getAttribute(
				"BannerMessageResponses.ActionDelete"));
		}
		if (request.getSession().getAttribute("BannerMessageResponses.TypeSuccess") == null ) {
			setTypeSuccess(Boolean.valueOf(true));
		} else {
			setTypeSuccess((Boolean) request.getSession().getAttribute(
				"BannerMessageResponses.TypeSuccess"));
		}
		if (request.getSession().getAttribute("BannerMessageResponses.TypeError") == null ) {
			setTypeError(Boolean.valueOf(true));
		} else {
			setTypeError((Boolean) request.getSession().getAttribute(
				"BannerMessageResponses.TypeError"));
		}
		if (request.getSession().getAttribute("BannerMessageResponses.TypeWarning") == null ) {
			setTypeWarning(Boolean.valueOf(true));
		} else {
			setTypeWarning((Boolean) request.getSession().getAttribute(
				"BannerMessageResponses.TypeWarning"));
		}

	}

	public void save(HttpServletRequest request) {
		request.getSession().setAttribute("BannerMessageResponses.N",
				Integer.valueOf(getN()));

		if (getSubjAreaId() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.SubjAreaId");
		} else {
			request.getSession().setAttribute(
					"BannerMessageResponses.SubjAreaId", getSubjAreaId());
		}
	       if (getDepartmentId()==null)
	            request.getSession().removeAttribute("BannerMessageResponses.DepartmentId");
	        else
	            request.getSession().setAttribute("BannerMessageResponses.DepartmentId", getDepartmentId());
	        if (getManagerId()==null)
	            request.getSession().removeAttribute("BannerMessageResponses.ManagerId");
	        else
	            request.getSession().setAttribute("BannerMessageResponses.ManagerId", getManagerId());
		
		if (getCourseNumber() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.CourseNumber");
		} else {
			request.getSession().setAttribute(
					"BannerMessageResponses.CourseNumber", getCourseNumber());
		}
		if (getCrn() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.Crn");
		} else {
			request.getSession().setAttribute(
					"BannerMessageResponses.Crn", getCrn());
		}
		if (getXlst() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.XlstGroup");
		} else {
			request.getSession().setAttribute(
					"BannerMessageResponses.XlstGroup", getXlst());
		}
		if (getStartDate() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.StartDate");
		} else {
			request.getSession().setAttribute(
					"BannerMessageResponses.StartDate", getStartDate());
		}
		if (getStopDate() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.StopDate");
		} else {
			request.getSession().setAttribute(
					"BannerMessageResponses.StopDate", getStopDate());
		}
		if (getMessage() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.Message");
		} else {
			request.getSession().setAttribute("BannerMessageResponses.Message",
					getMessage());
		}
		
		if (getShowHistory() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.ShowHistory");
		} else {
			request.getSession().setAttribute("BannerMessageResponses.ShowHistory",
					getShowHistory());
		}
		
		
		if (getActionAudit() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.ActionAudit");
		} else {
			request.getSession().setAttribute("BannerMessageResponses.ActionAudit",
					getActionAudit());
		}
		if (getActionUpdate() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.ActionUpdate");
		} else {
			request.getSession().setAttribute("BannerMessageResponses.ActionUpdate",
					getActionUpdate());
		}
		if (getActionDelete() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.ActionDelete");
		} else {
			request.getSession().setAttribute("BannerMessageResponses.ActionDelete",
					getActionDelete());
		}
		if (getTypeSuccess() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.TypeSuccess");
		} else {
			request.getSession().setAttribute("BannerMessageResponses.TypeSuccess",
					getTypeSuccess());
		}
		if (getTypeError() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.TypeError");
		} else {
			request.getSession().setAttribute("BannerMessageResponses.TypeError",
					getTypeError());
		}
		if (getTypeWarning() == null) {
			request.getSession().removeAttribute(
					"BannerMessageResponses.TypeWarning");
		} else {
			request.getSession().setAttribute("BannerMessageResponses.TypeWarning",
					getTypeWarning());
		}
		
		if (getOp() == null) {
			request.getSession().removeAttribute("BannerMessageResponses.Op");
		} else {
			request.getSession().setAttribute("BannerMessageResponses.Op",
					getOp());
		}
	}
}
