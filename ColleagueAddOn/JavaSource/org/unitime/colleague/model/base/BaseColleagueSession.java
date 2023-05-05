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
package org.unitime.colleague.model.base;

import jakarta.persistence.Column;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.commons.hibernate.id.UniqueIdGenerator;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseColleagueSession implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iColleagueCampus;
	private String iColleagueTermCode;
	private Boolean iStoreDataForColleague;
	private Boolean iSendDataToColleague;
	private Boolean iLoadingOfferingsFile;
	private Long iUniqueIdRolledForwardFrom;

	private Session iSession;

	public BaseColleagueSession() {
	}

	public BaseColleagueSession(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@GenericGenerator(name = "colleague_session_id", type = UniqueIdGenerator.class, parameters = {
		@Parameter(name = "sequence", value = "pref_group_seq")
	})
	@GeneratedValue(generator = "colleague_session_id")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "colleague_campus", nullable = false, length = 20)
	public String getColleagueCampus() { return iColleagueCampus; }
	public void setColleagueCampus(String colleagueCampus) { iColleagueCampus = colleagueCampus; }

	@Column(name = "colleague_term_code", nullable = false, length = 20)
	public String getColleagueTermCode() { return iColleagueTermCode; }
	public void setColleagueTermCode(String colleagueTermCode) { iColleagueTermCode = colleagueTermCode; }

	@Column(name = "store_data_for_colleague", nullable = false)
	public Boolean isStoreDataForColleague() { return iStoreDataForColleague; }
	@Transient
	public Boolean getStoreDataForColleague() { return iStoreDataForColleague; }
	public void setStoreDataForColleague(Boolean storeDataForColleague) { iStoreDataForColleague = storeDataForColleague; }

	@Column(name = "send_data_to_colleague", nullable = false)
	public Boolean isSendDataToColleague() { return iSendDataToColleague; }
	@Transient
	public Boolean getSendDataToColleague() { return iSendDataToColleague; }
	public void setSendDataToColleague(Boolean sendDataToColleague) { iSendDataToColleague = sendDataToColleague; }

	@Column(name = "loading_offerings_file", nullable = false)
	public Boolean isLoadingOfferingsFile() { return iLoadingOfferingsFile; }
	@Transient
	public Boolean getLoadingOfferingsFile() { return iLoadingOfferingsFile; }
	public void setLoadingOfferingsFile(Boolean loadingOfferingsFile) { iLoadingOfferingsFile = loadingOfferingsFile; }

	@Column(name = "uid_rolled_fwd_from", nullable = true, length = 20)
	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "session_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof ColleagueSession)) return false;
		if (getUniqueId() == null || ((ColleagueSession)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ColleagueSession)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "ColleagueSession["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "ColleagueSession[" +
			"\n	ColleagueCampus: " + getColleagueCampus() +
			"\n	ColleagueTermCode: " + getColleagueTermCode() +
			"\n	LoadingOfferingsFile: " + getLoadingOfferingsFile() +
			"\n	SendDataToColleague: " + getSendDataToColleague() +
			"\n	Session: " + getSession() +
			"\n	StoreDataForColleague: " + getStoreDataForColleague() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UniqueIdRolledForwardFrom: " + getUniqueIdRolledForwardFrom() +
			"]";
	}
}
