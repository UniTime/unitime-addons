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
package org.unitime.banner.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.util.Date;

import org.unitime.banner.model.QueueError;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
@IdClass(QueueErrorId.class)
public abstract class BaseQueueError implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iQueueId;
	private String iErrorType;
	private Date iErrorDate;
	private String iErrorText;


	public BaseQueueError() {
	}


	@Id
	@Column(name="null")
	public Long getQueueId() { return iQueueId; }
	public void setQueueId(Long queueId) { iQueueId = queueId; }

	@Id
	@Column(name="null")
	public String getErrorType() { return iErrorType; }
	public void setErrorType(String errorType) { iErrorType = errorType; }

	@Id
	@Column(name="null")
	public Date getErrorDate() { return iErrorDate; }
	public void setErrorDate(Date errorDate) { iErrorDate = errorDate; }

	@Column(name = "errortext", nullable = true)
	public String getErrorText() { return iErrorText; }
	public void setErrorText(String errorText) { iErrorText = errorText; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof QueueError)) return false;
		QueueError queueError = (QueueError)o;
		if (getQueueId() == null || queueError.getQueueId() == null || !getQueueId().equals(queueError.getQueueId())) return false;
		if (getErrorType() == null || queueError.getErrorType() == null || !getErrorType().equals(queueError.getErrorType())) return false;
		if (getErrorDate() == null || queueError.getErrorDate() == null || !getErrorDate().equals(queueError.getErrorDate())) return false;
		return true;
	}

	@Override
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
