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

package org.unitime.banner.util;

import java.util.List;
import java.util.Set;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.unitime.banner.dataexchange.BannerMessage.BannerMessageAction;
import org.unitime.banner.dataexchange.SendBannerMessage;
import org.unitime.banner.model.BannerConfig;
import org.unitime.banner.model.BannerSection;
import org.unitime.banner.model.BannerSession;
import org.unitime.timetable.defaults.ApplicationProperty;
import org.unitime.timetable.interfaces.ExternalInstrOffrConfigChangeAction;
import org.unitime.timetable.model.CourseOffering;
import org.unitime.timetable.model.InstrOfferingConfig;
import org.unitime.timetable.model.InstructionalOffering;
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.SchedulingSubpart;
import org.unitime.commons.Debug;


/**
 * @author says
 *
 */
public class BannerInstrOffrConfigChangeAction implements
		ExternalInstrOffrConfigChangeAction {

	/**
	 * 
	 * 
	 */
	public BannerInstrOffrConfigChangeAction() {
		//
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstrOffrConfigChangeAction#performExternalInstrOffrConfigChangeAction(org.unitime.timetable.model.InstrOfferingConfig, org.hibernate.Session)
	 */
	public void performExternalInstrOffrConfigChangeAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		if (BannerSession.shouldGenerateBannerDataFieldsForSession(instructionalOffering.getSession(), hibSession)){
			updateInstructionalOffering(instructionalOffering, hibSession);
			
			if (BannerSession.shouldSendDataToBannerForSession(instructionalOffering.getSession(), hibSession)){
				SendBannerMessage.sendBannerMessage(BannerSection.findBannerSectionsForInstructionalOffering(instructionalOffering, hibSession), BannerMessageAction.UPDATE, hibSession);
			}
		}
	}
	
	public void updateInstructionalOffering(InstructionalOffering instructionalOffering, Session hibSession){
		if (BannerSession.shouldGenerateBannerDataFieldsForSession(instructionalOffering.getSession(), hibSession)){
			// The banner sections must be correctly crosslisted before the banner linkages are worked with.
			BannerSectionCrosslistHelper bsch = new BannerSectionCrosslistHelper(instructionalOffering, hibSession);
			bsch.updateCrosslists();

			BannerSectionLinkageHelper bslh = new BannerSectionLinkageHelper(instructionalOffering, hibSession);
			bslh.updateLinkages();
			
			assignCrnsToAnyAddedBannerSections(instructionalOffering, hibSession);
			
			defaultGradableItypesIfNull(instructionalOffering, hibSession);
			
		}
		
	}

	@SuppressWarnings("unchecked")
	private void defaultGradableItypesIfNull(InstructionalOffering instructionalOffering, Session hibSession){
		for (InstrOfferingConfig ioc : (Set<InstrOfferingConfig>) instructionalOffering.getInstrOfferingConfigs()){
			int lowestItype = Integer.MAX_VALUE;
			ItypeDesc itype = null;
			List<BannerConfig> configs = BannerConfig.findBannerConfigsForInstrOfferingConfig(ioc, hibSession);
			if (configs.size() > 1){
				for (BannerConfig bc : configs){
					if (bc.getBannerCourse().getCourseOffering(hibSession).isIsControl().booleanValue()){
						if (bc.getGradableItype() != null){
							lowestItype = bc.getGradableItype().getItype().intValue();
							itype = bc.getGradableItype();
						}
					}
				}
			}
			for (BannerConfig bc : configs){
				if (bc.getGradableItype() == null){
					if (lowestItype == Integer.MAX_VALUE){
						for(SchedulingSubpart ss : (Set<SchedulingSubpart>)ioc.getSchedulingSubparts()){
							if (ss.getItype().getItype().intValue() < lowestItype){
								lowestItype = ss.getItype().getItype().intValue();
								itype = ss.getItype();
							}
						}
					}
					if (itype != null){
						bc.setGradableItype(itype);
						Transaction trans = hibSession.beginTransaction();
						hibSession.update(bc);
						trans.commit();
						hibSession.flush();
					}
				}
			}
		}
	}
	private void assignCrnsToAnyAddedBannerSections(
			InstructionalOffering instructionalOffering, Session hibSession) {
		List<BannerSection> sections = BannerSection.findBannerSectionsForInstructionalOffering(instructionalOffering, hibSession);
		for (BannerSection bs : sections){
			Transaction trans = hibSession.beginTransaction();
			if (bs.getSectionIndex() == null){
				bs.assignNewSectionIndex(hibSession);
			}
			if (bs.getCrn() == null){
				bs.assignNewCrn(hibSession);
			}
			bs.updateClassSuffixForClassesIfNecessary(hibSession);
			trans.commit();
			hibSession.flush();
		}
			
	}

	@Override
	public boolean validateConfigChangeCanOccur(
			InstructionalOffering instructionalOffering, Session hibSession) {
		int maxNumClassesAllowed = ApplicationProperty.SubpartMaxNumClasses.intValue();
		boolean canOccur = true;	
		
		if (BannerSession.shouldGenerateBannerDataFieldsForSession(instructionalOffering.getSession(), hibSession)){
			// Validate number of classes for a course offering does not exceed max defined in parameter
			String query = "select count(c) " 
			             + "from BannerSession bsCo, BannerSession bsOrigIo, "
			             + "     Class_ c inner join c.schedulingSubpart.instrOfferingConfig.instructionalOffering.courseOfferings as co "
					     + "where co.subjectArea.subjectAreaAbbreviation = :subject"
			             + "  and co.courseNbr like :crsNbrBase "
					     + "  and bsOrigIo.session.uniqueId = :sessionId"
			             + "  and bsCo.bannerTermCode = bsOrigIo.bannerTermCode"
			             + "  and c.schedulingSubpart.instrOfferingConfig.instructionalOffering.session.uniqueId = bsCo.session.uniqueId "
					     ;
			for (CourseOffering co : instructionalOffering.getCourseOfferings()){
				//count all classes that match this course number minus any suffix in all matching academic terms
				int count = Integer.parseInt(hibSession.createQuery(query)
				          .setString("subject", co.getSubjectAreaAbbv())
				          .setString("crsNbrBase", (co.getCourseNbr().substring(0, 5) + "%"))
				          .setLong("sessionId",	instructionalOffering.getSession().getUniqueId().longValue())
				          .uniqueResult().toString())
				          ;
				if (count > maxNumClassesAllowed) {
					canOccur = false;
					Debug.info(co.getCourseName() + " has more than " + maxNumClassesAllowed + " classes, not generating Banner Sections");
					break;
				}
			}
		}
		
		return(canOccur);
	}


}
