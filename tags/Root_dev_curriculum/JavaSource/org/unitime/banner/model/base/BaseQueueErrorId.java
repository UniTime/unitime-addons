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

package org.unitime.banner.model.base;

import java.io.Serializable;


public abstract class BaseQueueErrorId implements Serializable {

	protected int hashCode = Integer.MIN_VALUE;

	private java.lang.Long queueId;
	private java.lang.String errorType;
	private java.util.Date errorDate;


	public BaseQueueErrorId () {}
	
	public BaseQueueErrorId (
		java.lang.Long queueId,
		java.lang.String errorType,
		java.util.Date errorDate) {

		this.setQueueId(queueId);
		this.setErrorType(errorType);
		this.setErrorDate(errorDate);
	}


	/**
	 * Return the value associated with the column: queueid
	 */
	public java.lang.Long getQueueId () {
		return queueId;
	}

	/**
	 * Set the value related to the column: queueid
	 * @param queueId the queueid value
	 */
	public void setQueueId (java.lang.Long queueId) {
		this.queueId = queueId;
	}



	/**
	 * Return the value associated with the column: ERRORTYPE
	 */
	public java.lang.String getErrorType () {
		return errorType;
	}

	/**
	 * Set the value related to the column: ERRORTYPE
	 * @param errorType the ERRORTYPE value
	 */
	public void setErrorType (java.lang.String errorType) {
		this.errorType = errorType;
	}



	/**
	 * Return the value associated with the column: errordate
	 */
	public java.util.Date getErrorDate () {
		return errorDate;
	}

	/**
	 * Set the value related to the column: errordate
	 * @param errorDate the errordate value
	 */
	public void setErrorDate (java.util.Date errorDate) {
		this.errorDate = errorDate;
	}




	public boolean equals (Object obj) {
		if (null == obj) return false;
		if (!(obj instanceof org.unitime.banner.model.QueueErrorId)) return false;
		else {
			org.unitime.banner.model.QueueErrorId mObj = (org.unitime.banner.model.QueueErrorId) obj;
			if (null != this.getQueueId() && null != mObj.getQueueId()) {
				if (!this.getQueueId().equals(mObj.getQueueId())) {
					return false;
				}
			}
			else {
				return false;
			}
			if (null != this.getErrorType() && null != mObj.getErrorType()) {
				if (!this.getErrorType().equals(mObj.getErrorType())) {
					return false;
				}
			}
			else {
				return false;
			}
			if (null != this.getErrorDate() && null != mObj.getErrorDate()) {
				if (!this.getErrorDate().equals(mObj.getErrorDate())) {
					return false;
				}
			}
			else {
				return false;
			}
			return true;
		}
	}

	public int hashCode () {
		if (Integer.MIN_VALUE == this.hashCode) {
			StringBuffer sb = new StringBuffer();
			if (null != this.getQueueId()) {
				sb.append(this.getQueueId().hashCode());
				sb.append(":");
			}
			else {
				return super.hashCode();
			}
			if (null != this.getErrorType()) {
				sb.append(this.getErrorType().hashCode());
				sb.append(":");
			}
			else {
				return super.hashCode();
			}
			if (null != this.getErrorDate()) {
				sb.append(this.getErrorDate().hashCode());
				sb.append(":");
			}
			else {
				return super.hashCode();
			}
			this.hashCode = sb.toString().hashCode();
		}
		return this.hashCode;
	}


}