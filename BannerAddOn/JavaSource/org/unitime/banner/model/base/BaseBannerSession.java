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
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.io.Serializable;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.BannerTermCrnProperties;
import org.unitime.commons.annotations.UniqueIdGenerator;
import org.unitime.timetable.model.Session;

/**
 * Do not change this class. It has been automatically generated using ant create-model.
 * @see org.unitime.commons.ant.CreateBaseModelFromXml
 */
@MappedSuperclass
public abstract class BaseBannerSession implements Serializable {
	private static final long serialVersionUID = 1L;

	private Long iUniqueId;
	private String iBannerCampus;
	private String iBannerTermCode;
	private Boolean iStoreDataForBanner;
	private Boolean iSendDataToBanner;
	private Boolean iLoadingOfferingsFile;
	private Integer iFutureSessionUpdateModeInt;
	private String iStudentCampus;
	private Boolean iUseSubjectAreaPrefixAsCampus;
	private String iSubjectAreaPrefixDelimiter;

	private Session iSession;
	private BannerSession iFutureSession;
	private BannerTermCrnProperties iBannerTermCrnProperties;

	public BaseBannerSession() {
	}

	public BaseBannerSession(Long uniqueId) {
		setUniqueId(uniqueId);
	}


	@Id
	@UniqueIdGenerator(sequence = "pref_group_seq")
	@Column(name="uniqueid")
	public Long getUniqueId() { return iUniqueId; }
	public void setUniqueId(Long uniqueId) { iUniqueId = uniqueId; }

	@Column(name = "banner_campus", nullable = false, length = 20)
	public String getBannerCampus() { return iBannerCampus; }
	public void setBannerCampus(String bannerCampus) { iBannerCampus = bannerCampus; }

	@Column(name = "banner_term_code", nullable = false, length = 20)
	public String getBannerTermCode() { return iBannerTermCode; }
	public void setBannerTermCode(String bannerTermCode) { iBannerTermCode = bannerTermCode; }

	@Column(name = "store_data_for_banner", nullable = false)
	public Boolean isStoreDataForBanner() { return iStoreDataForBanner; }
	@Transient
	public Boolean getStoreDataForBanner() { return iStoreDataForBanner; }
	public void setStoreDataForBanner(Boolean storeDataForBanner) { iStoreDataForBanner = storeDataForBanner; }

	@Column(name = "send_data_to_banner", nullable = false)
	public Boolean isSendDataToBanner() { return iSendDataToBanner; }
	@Transient
	public Boolean getSendDataToBanner() { return iSendDataToBanner; }
	public void setSendDataToBanner(Boolean sendDataToBanner) { iSendDataToBanner = sendDataToBanner; }

	@Column(name = "loading_offerings_file", nullable = false)
	public Boolean isLoadingOfferingsFile() { return iLoadingOfferingsFile; }
	@Transient
	public Boolean getLoadingOfferingsFile() { return iLoadingOfferingsFile; }
	public void setLoadingOfferingsFile(Boolean loadingOfferingsFile) { iLoadingOfferingsFile = loadingOfferingsFile; }

	@Column(name = "future_mode", nullable = true)
	public Integer getFutureSessionUpdateModeInt() { return iFutureSessionUpdateModeInt; }
	public void setFutureSessionUpdateModeInt(Integer futureSessionUpdateModeInt) { iFutureSessionUpdateModeInt = futureSessionUpdateModeInt; }

	@Column(name = "student_campus", nullable = true, length = 500)
	public String getStudentCampus() { return iStudentCampus; }
	public void setStudentCampus(String studentCampus) { iStudentCampus = studentCampus; }

	@Column(name = "use_subj_area_prfx_as_campus", nullable = true)
	public Boolean isUseSubjectAreaPrefixAsCampus() { return iUseSubjectAreaPrefixAsCampus; }
	@Transient
	public Boolean getUseSubjectAreaPrefixAsCampus() { return iUseSubjectAreaPrefixAsCampus; }
	public void setUseSubjectAreaPrefixAsCampus(Boolean useSubjectAreaPrefixAsCampus) { iUseSubjectAreaPrefixAsCampus = useSubjectAreaPrefixAsCampus; }

	@Column(name = "subj_area_prfx_delim", nullable = true, length = 5)
	public String getSubjectAreaPrefixDelimiter() { return iSubjectAreaPrefixDelimiter; }
	public void setSubjectAreaPrefixDelimiter(String subjectAreaPrefixDelimiter) { iSubjectAreaPrefixDelimiter = subjectAreaPrefixDelimiter; }

	@ManyToOne(optional = false, fetch = FetchType.EAGER)
	@JoinColumn(name = "session_id", nullable = false)
	@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
	public Session getSession() { return iSession; }
	public void setSession(Session session) { iSession = session; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "future_id", nullable = true)
	public BannerSession getFutureSession() { return iFutureSession; }
	public void setFutureSession(BannerSession futureSession) { iFutureSession = futureSession; }

	@ManyToOne(optional = true)
	@JoinColumn(name = "banner_term_crn_prop_id", nullable = true)
	public BannerTermCrnProperties getBannerTermCrnProperties() { return iBannerTermCrnProperties; }
	public void setBannerTermCrnProperties(BannerTermCrnProperties bannerTermCrnProperties) { iBannerTermCrnProperties = bannerTermCrnProperties; }

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof BannerSession)) return false;
		if (getUniqueId() == null || ((BannerSession)o).getUniqueId() == null) return false;
		return getUniqueId().equals(((BannerSession)o).getUniqueId());
	}

	@Override
	public int hashCode() {
		if (getUniqueId() == null) return super.hashCode();
		return getUniqueId().hashCode();
	}

	@Override
	public String toString() {
		return "BannerSession["+getUniqueId()+"]";
	}

	public String toDebugString() {
		return "BannerSession[" +
			"\n	BannerCampus: " + getBannerCampus() +
			"\n	BannerTermCode: " + getBannerTermCode() +
			"\n	BannerTermCrnProperties: " + getBannerTermCrnProperties() +
			"\n	FutureSession: " + getFutureSession() +
			"\n	FutureSessionUpdateModeInt: " + getFutureSessionUpdateModeInt() +
			"\n	LoadingOfferingsFile: " + getLoadingOfferingsFile() +
			"\n	SendDataToBanner: " + getSendDataToBanner() +
			"\n	Session: " + getSession() +
			"\n	StoreDataForBanner: " + getStoreDataForBanner() +
			"\n	StudentCampus: " + getStudentCampus() +
			"\n	SubjectAreaPrefixDelimiter: " + getSubjectAreaPrefixDelimiter() +
			"\n	UniqueId: " + getUniqueId() +
			"\n	UseSubjectAreaPrefixAsCampus: " + getUseSubjectAreaPrefixAsCampus() +
			"]";
	}
}
