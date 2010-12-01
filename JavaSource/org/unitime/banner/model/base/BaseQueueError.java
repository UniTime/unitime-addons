/*
 * UniTime 3.2 (University Timetabling Application)
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
import java.util.Date;

import org.unitime.banner.model.QueueError;

public abstract class BaseQueueError implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iQueueId;
	private String iErrorType;
	private Date iErrorDate;
	private String iErrorText;


	public static String PROP_ERRORTEXT = "errorText";

	public BaseQueueError() {
		initialize();
	}

	protected void initialize() {}

	public Long getQueueId() { return iQueueId; }
	public void setQueueId(Long queueId) { iQueueId = queueId; }

	public String getErrorType() { return iErrorType; }
	public void setErrorType(String errorType) { iErrorType = errorType; }

	public Date getErrorDate() { return iErrorDate; }
	public void setErrorDate(Date errorDate) { iErrorDate = errorDate; }

	public String getErrorText() { return iErrorText; }
	public void setErrorText(String errorText) { iErrorText = errorText; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof QueueError)) return false;
		QueueError queueError = (QueueError)o;
		if (getQueueId() == null || queueError.getQueueId() == null || !getQueueId().equals(queueError.getQueueId())) return false;
		if (getErrorType() == null || queueError.getErrorType() == null || !getErrorType().equals(queueError.getErrorType())) return false;
		if (getErrorDate() == null || queueError.getErrorDate() == null || !getErrorDate().equals(queueError.getErrorDate())) return false;
		return true;
	}

	public int hashCode() {
		if (getQueueId() == null || getErrorType() == null || getErrorDate() == null) return super.hashCode();
		return getQueueId().hashCode() ^ getErrorType().hashCode() ^ getErrorDate().hashCode();
	}

	public String toString() {
		return "QueueError[" + getQueueId() + ", " + getErrorType() + ", " + getErrorDate() + "]";
	}

	public String toDebugString() {
		return "QueueError[" +
			"\n	ErrorDate: " + getErrorDate() +
			"\n	ErrorText: " + getErrorText() +
			"\n	ErrorType: " + getErrorType() +
			"\n	QueueId: " + getQueueId() +
			"]";
	}
}
