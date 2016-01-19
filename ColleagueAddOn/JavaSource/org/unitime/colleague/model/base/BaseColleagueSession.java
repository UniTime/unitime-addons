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

import java.io.Serializable;

import org.unitime.colleague.model.ColleagueSession;
import org.unitime.timetable.model.Session;

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

	public static String PROP_UNIQUEID = "uniqueId";
	public static String PROP_COLLEAGUE_CAMPUS = "colleagueCampus";
	public static String PROP_COLLEAGUE_TERM_CODE = "colleagueTermCode";
	public static String PROP_STORE_DATA_FOR_COLLEAGUE = "storeDataForColleague";
	public static String PROP_SEND_DATA_TO_COLLEAGUE = "sendDataToColleague";
	public static String PROP_LOADING_OFFERINGS_FILE = "loadingOfferingsFile";
	public static String PROP_UID_ROLLED_FWD_FROM = "uniqueIdRolledForwardFrom";

	public BaseColleagueSession() {
		initialize();
	}

	public BaseColleagueSession(Long uniqueId) {
		setUniqueId(uniqueId);
		initialize();
	}

	protected void initialize() {}

	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getColleagueCampus() { return iColleagueCampus; }
	public void setColleagueCampus(String colleagueCampus) { iColleagueCampus = colleagueCampus; }

	public String getColleagueTermCode() { return iColleagueTermCode; }
	public void setColleagueTermCode(String colleagueTermCode) { iColleagueTermCode = colleagueTermCode; }

	public Boolean isStoreDataForColleague() { return iStoreDataForColleague; }
	public Boolean getStoreDataForColleague() { return iStoreDataForColleague; }
	public void setStoreDataForColleague(Boolean storeDataForColleague) { iStoreDataForColleague = storeDataForColleague; }

	public Boolean isSendDataToColleague() { return iSendDataToColleague; }
	public Boolean getSendDataToColleague() { return iSendDataToColleague; }
	public void setSendDataToColleague(Boolean sendDataToColleague) { iSendDataToColleague = sendDataToColleague; }

	public Boolean isLoadingOfferingsFile() { return iLoadingOfferingsFile; }
	public Boolean getLoadingOfferingsFile() { return iLoadingOfferingsFile; }
	public void setLoadingOfferingsFile(Boolean loadingOfferingsFile) { iLoadingOfferingsFile = loadingOfferingsFile; }

	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	public Long getUniqueIdRolledForwardFrom() { return iUniqueIdRolledForwardFrom; }
	public void setUniqueIdRolledForwardFrom(Long uniqueIdRolledForwardFrom) { iUniqueIdRolledForwardFrom = uniqueIdRolledForwardFrom; }

	public boolean equals(Object o) {
		if (o == null || !(o instanceof ColleagueSession)) return false;
		if (getUniqueId() == null || ((ColleagueSession)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((ColleagueSession)o).getUniqueId());
	}

	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

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
			"]";
	}
}
