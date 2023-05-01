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
*/package org.unitime.banner.dataexchange;

import java.util.List;

import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.dao.BannerSectionDAO;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.commons.Debug;
import org.unitime.timetable.model.Class_;
import org.unitime.timetable.model.SubjectArea;

public class BannerSectionInfoHelper {
	private static BannerMessage.BannerMessageContext context = new BannerMessage.BannerMessageContext();

	Integer iCrn;
	String iSubject;
	String iCourse;
	String iCampus;
	String iTermCode;
	String iXlstCode;
	Long iBannerSectionId;
	BannerSection iBannerSection;
	BannerSession iBannerSession;
	SubjectArea iSubjectArea;
	Boolean iSentSection = Boolean.FALSE;
	Boolean iSentCrosslist = Boolean.FALSE;
	Integer iSentSectionPosition;
	Integer iSentCrosslistPosition;
	Boolean iReceivedSectionResponse = Boolean.FALSE;
	Boolean iReceivedCrosslistResponse = Boolean.FALSE;
	String iSentSectionAction;
	String iSentCrossListAction;

	public BannerSectionInfoHelper(Integer crn, String subject, String course, String xlstId, String campus, String termCode, Long bannerSectionId) {
		super();
		this.iCrn = crn;
		this.iSubject = subject;
		this.iCourse = course;
		this.iXlstCode = xlstId;
		this.iCampus = campus;
		this.iTermCode = termCode;
		this.iBannerSectionId = bannerSectionId;
		setMissingFieldsIfPossible();
	}
	
	public void fillInMissingFieldsIfNeeded(Integer crn, String subject, String course, String xlistId, String campus, String termCode, Long bannerSectionId) {
		if (getCrn() == null && crn != null) {
			setCrn(crn);
		}
		if (getSubject() == null && subject != null) {
			setSubject(subject);
		}
		if (getCourse() == null && course != null) {
			setCourse(course);
		}
		if (getCampus() == null && campus != null) {
			setCampus(campus);
		}
		if (getTermCode() == null && termCode != null) {
			setTermCode(termCode);	
		}
		if (getBannerSectionId() == null && bannerSectionId != null) {
			setBannerSectionId(bannerSectionId);
		}
		if (getXlstCode() == null && xlistId != null) {
			setXlstCode(xlistId);
		}
		setMissingFieldsIfPossible();		
	}
	
	private BannerSection getControllingBannerSectionForCrosslist(String termCode, String crossListIdentifier) {
		List<BannerSection> controllingBannerSections = BannerSectionDAO
				.getInstance()
				.getSession()
				.createQuery("select bs from BannerSession bsess, BannerSection bs, CourseOffering co where bsess.bannerTermCode = :termCode and bs.session = bsess.session and bs.crossListIdentifier = :xlst and co.uniqueId = bs.bannerConfig.bannerCourse.courseOfferingId and co.isControl = true", BannerSection.class)
				.setParameter("termCode", termCode)
				.setParameter("xlst", crossListIdentifier).list();
		if (controllingBannerSections.size() != 1) {
			return null;
		} else {
			return controllingBannerSections.get(0);
		}
	}
	private void setMissingFieldsIfPossible() {
		if (getBannerSectionId() != null) {
			if (getBannerSection() == null) {
				setBannerSection(BannerSectionDAO.getInstance().get(getBannerSectionId()));
				if (getBannerSection() == null) {
					Debug.info("Banner Section with uniqueId:  " + getBannerSectionId().toString() + " no longer exists.");
				}
			}
			if (getBannerSession() == null) {
				setBannerSession(tryToLookupBannerSession());
			}
		}
		
		if (getTermCode() == null && getBannerSession() != null) {
			setTermCode(getBannerSession().getBannerTermCode());
		}
		
		if (getTermCode() == null) {
			return;
		}
		
		if (getXlstCode() != null && getCrn() == null && getBannerSection() == null) {
			BannerSection bannerSection = getControllingBannerSectionForCrosslist(getTermCode(), getXlstCode());
			if (bannerSection != null) {
				setBannerSectionId(getBannerSectionId());
				setBannerSection(getBannerSection());
				setCrn(getCrn());
				setBannerSession(tryToLookupBannerSession());
			}
		}

		if (getCrn() == null && getBannerSection() != null) {
			setCrn(getBannerSection().getCrn());
		}
		
		if (getCrn() == null) {
			return;
		}
				
		if (getBannerSectionId() == null) {
			if (getBannerSection() != null) {
				setBannerSectionId(getBannerSection().getUniqueId());
			} else {
				setBannerSection(BannerSection.findBannerSectionForCrnAndTermCode(getCrn(), getTermCode()));
				if (getBannerSection() == null) {
					Debug.info("Banner Section with CRN:  " + getCrn().toString() + " does not exist for Banner term code: " + getTermCode() + ".");		
				} else {
					setBannerSectionId(getBannerSection().getUniqueId());
				}
			}
			if (getBannerSession() == null && getBannerSection() != null) {
				setBannerSession(tryToLookupBannerSession());
			}
		}
		if (getCampus() == null) {
			if (getBannerSection() != null) {
				Class_ clazz = getBannerSection().getFirstClass();
				if (clazz != null) {
					setCampus(getBannerSection().getCampusCode(getBannerSession(), clazz));
				}
			} else if (getBannerSession() != null) {
				setCampus(getBannerSession().getBannerCampus());
			} else {
				Debug.info("Unable to determine banner campus.");							
			}
		}
		if (getSubject() == null) {
			if (getBannerSection() != null && getBannerSession() != null) {
				Class_ clazz = getBannerSection().getFirstClass();
				setSubjectArea(getBannerSection().getBannerConfig().getBannerCourse().getCourseOffering(null).getSubjectArea());
				if (clazz != null) {
					setSubject(getBannerSection().getBannerSubjectArea(getBannerSession(), clazz));
				} else {
					if (getBannerSection().getBannerConfig() != null
							&& getBannerSection().getBannerConfig().getBannerCourse() != null
							&& getBannerSection().getBannerConfig().getBannerCourse().getCourseOffering(null) != null) {
						setSubject(BannerSection.getExternalSubjectAreaElementHelper().getBannerSubjectAreaAbbreviation(getSubjectArea(), getBannerSession()));
					} else {
						Debug.info("Unable to determine subject area.");														
					}
				}
			}
		}
		if (getCourse() == null) {
			if (getBannerSection() != null) {
				if (getBannerSection().getBannerConfig() != null
						&& getBannerSection().getBannerConfig().getBannerCourse() != null
						&& getBannerSection().getBannerConfig().getBannerCourse().getCourseOffering(null) != null) {
					setCourse(getBannerSection().getBannerConfig().getBannerCourse().getCourseOffering(null).getCourseNbr().substring(0, context.getCourseNumberLength()));
				} else {
					Debug.info("Unable to determine course number.");														
				}
			}
		}
		
		if (getBannerSession() == null) {
			setBannerSession(tryToLookupBannerSession());
		}
		
		if (getSubjectArea() == null && getSubject() != null) {
			if (getBannerSection() != null 
					&& getBannerSection().getBannerConfig() != null 
					&& getBannerSection().getBannerConfig().getBannerCourse() != null
					&& getBannerSection().getBannerConfig().getBannerCourse().getCourseOffering(null) != null) {
				setSubjectArea(getBannerSection().getBannerConfig().getBannerCourse().getCourseOffering(null).getSubjectArea());
			} else if (getBannerSession() != null) {
				setSubjectArea(findSubjectAreaInBannerSession(getBannerSession(), getSubject(), getCampus()));
			}
			
		}
		if (getXlstCode() == null && getBannerSection() != null) {
			setXlstCode(getBannerSection().getCrossListIdentifier());
		}
	}
	
	private BannerSession tryToLookupBannerSession() {
		if (getBannerSection() != null) {
			BannerSession bsess = BannerSession.findBannerSessionForSession(getBannerSection().getSession(), null);
			if (bsess == null) {
				Debug.info("Banner Session matching UniTime Session:  " + getBannerSection().getSession().getLabel() + " does not exist.");					
			} else {
				return bsess;
			}
		}
		if (getTermCode() == null) {
			return null;
		}
		
		List<BannerSession> possibleSessions = BannerSessionDAO.getInstance()
				.getSession()
				.createQuery("from BannerSession bs where bs.bannerTermCode = :termCode", BannerSession.class)
				.setParameter("termCode", getTermCode()).list();
		if (possibleSessions.size() == 1) {
			return possibleSessions.get(0);
		}
		
		for (BannerSession bs : possibleSessions) {
			if (getCampus() != null && bs.getBannerCampus().equals(getCampus())) {
				return bs;
			} else {
				if (bs.getUseSubjectAreaPrefixAsCampus() != null && bs.getUseSubjectAreaPrefixAsCampus() && getCampus() != null && getSubject() != null) {
					if (findSubjectAreaInBannerSession(bs, getSubject(), getCampus()) != null) {				
						return(bs);
					}
				}
			}
		}
		return null;
	}
	
	public SubjectArea findSubjectAreaInBannerSession(BannerSession bannerSession, String subject, String campus) {
		String lookupSubject = null;
		if (bannerSession.isUseSubjectAreaPrefixAsCampus() != null && bannerSession.isUseSubjectAreaPrefixAsCampus()) {
			lookupSubject = campus + ((bannerSession.getSubjectAreaPrefixDelimiter() != null && !bannerSession.getSubjectAreaPrefixDelimiter().equals("")) ? bannerSession.getSubjectAreaPrefixDelimiter() : " - ") + getSubject();
		} else {
			lookupSubject = subject;
		}
		for (SubjectArea sa : bannerSession.getSession().getSubjectAreas()) {
			if (sa.getSubjectAreaAbbreviation().equals(lookupSubject)) {
				return(sa);
			}
		}
		
		return null;
	}
	
	public boolean needsNoChangeSectionMessage(boolean isAudit, int lastMatch, int currentMatch) {
		if (isAudit) {
			return false;
		}
		if (getSentSection() != null 
				&& getSentSection() 
				&& getSentSectionPosition() != null 
				&& lastMatch < getSentSectionPosition().intValue() 
				&& getSentSectionPosition().intValue() < currentMatch) {
			if (getReceivedSectionResponse() != null && getReceivedSectionResponse()) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}
	
	public boolean needsNoChangeCrosslistMessage(boolean isAudit, int lastMatch, int currentMatch) {
		if (isAudit) {
			return false;
		}
		if (getSentCrosslist() != null 
				&& getSentCrosslist() 
				&& getSentCrosslistPosition() != null 
				&& lastMatch < getSentCrosslistPosition().intValue() 
				&& getSentCrosslistPosition().intValue() < currentMatch) {
			if (getReceivedCrosslistResponse()) {
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	public Integer getCrn() {
		return iCrn;
	}
	public void setCrn(Integer crn) {
		this.iCrn = crn;
	}
	public String getSubject() {
		return iSubject;
	}
	public void setSubject(String subject) {
		this.iSubject = subject;
	}
	public String getCourse() {
		return iCourse;
	}
	public void setCourse(String course) {
		this.iCourse = course;
	}
	public String getCampus() {
		return iCampus;
	}
	public void setCampus(String campus) {
		this.iCampus = campus;
	}
	public String getTermCode() {
		return iTermCode;
	}

	public void setTermCode(String termCode) {
		this.iTermCode = termCode;
	}

	public Long getBannerSectionId() {
		return iBannerSectionId;
	}
	public void setBannerSectionId(Long bannerSectionId) {
		this.iBannerSectionId = bannerSectionId;
	}

	public BannerSection getBannerSection() {
		return iBannerSection;
	}

	public void setBannerSection(BannerSection bannerSection) {
		this.iBannerSection = bannerSection;
	}

	public BannerSession getBannerSession() {
		return iBannerSession;
	}

	public void setBannerSession(BannerSession bannerSession) {
		this.iBannerSession = bannerSession;
	}
	
	public SubjectArea getSubjectArea() {
		return iSubjectArea;
	}

	public void setSubjectArea(SubjectArea subjectArea) {
		this.iSubjectArea = subjectArea;
	}

	public String getXlstCode() {
		return iXlstCode;
	}

	public void setXlstCode(String xlstCode) {
		this.iXlstCode = xlstCode;
	}

	public Boolean getSentSection() {
		return iSentSection;
	}

	public void setSentSection(Boolean sentSection) {
		this.iSentSection = sentSection;
	}

	public Boolean getSentCrosslist() {
		return iSentCrosslist;
	}

	public void setSentCrosslist(Boolean sentCrosslist) {
		this.iSentCrosslist = sentCrosslist;
	}

	public Integer getSentSectionPosition() {
		return iSentSectionPosition;
	}

	public void setSentSectionPosition(Integer sentSectionPosition) {
		this.iSentSectionPosition = sentSectionPosition;
	}

	public Boolean getReceivedSectionResponse() {
		return iReceivedSectionResponse;
	}

	public void setReceivedSectionResponse(Boolean receivedSectionResponse) {
		this.iReceivedSectionResponse = receivedSectionResponse;
	}

	public Boolean getReceivedCrosslistResponse() {
		return iReceivedCrosslistResponse;
	}

	public void setReceivedCrosslistResponse(Boolean receivedCrosslistResponse) {
		this.iReceivedCrosslistResponse = receivedCrosslistResponse;
	}

	public Integer getSentCrosslistPosition() {
		return iSentCrosslistPosition;
	}

	public void setSentCrosslistPosition(Integer sentCrosslistPosition) {
		this.iSentCrosslistPosition = sentCrosslistPosition;
	}

	public String getSentSectionAction() {
		return iSentSectionAction;
	}

	public void setSentSectionAction(String sentSectionAction) {
		this.iSentSectionAction = sentSectionAction;
	}

	public String getSentCrossListAction() {
		return iSentCrossListAction;
	}

	public void setSentCrossListAction(String sentCrossListAction) {
		this.iSentCrossListAction = sentCrossListAction;
	}

}
