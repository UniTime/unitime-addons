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

package org.unitime.colleague.util;

import java.util.List;

import org.hibernate.Session;
import org.unitime.colleague.dataexchange.ColleagueMessage.MessageAction;
import org.unitime.colleague.dataexchange.SendColleagueMessage;
import org.unitime.colleague.model.ColleagueSection;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.timetable.interfaces.ExternalInstrOffrConfigChangeAction;
import org.unitime.timetable.model.InstructionalOffering;


/**
 * @author says
 *
 */
public class ColleagueInstrOffrConfigChangeAction implements
		ExternalInstrOffrConfigChangeAction {

	/**
	 * 
	 * 
	 */
	public ColleagueInstrOffrConfigChangeAction() {
		//
	}

	/* (non-Javadoc)
	 * @see org.unitime.timetable.interfaces.ExternalInstrOffrConfigChangeAction#performExternalInstrOffrConfigChangeAction(org.unitime.timetable.model.InstrOfferingConfig, org.hibernate.Session)
	 */
	public void performExternalInstrOffrConfigChangeAction(
			InstructionalOffering instructionalOffering, Session hibSession) {
		if (ColleagueSession.shouldGenerateColleagueDataFieldsForSession(instructionalOffering.getSession(), hibSession)){
			updateInstructionalOffering(instructionalOffering, hibSession);
			
			if (ColleagueSession.shouldSendDataToColleagueForSession(instructionalOffering.getSession(), hibSession)){
				SendColleagueMessage.sendColleagueMessage(ColleagueSection.findColleagueSectionsForInstructionalOffering(instructionalOffering, hibSession), MessageAction.UPDATE, hibSession);
			}
		}
	}
	
	public void updateInstructionalOffering(InstructionalOffering instructionalOffering, Session hibSession){
		if (ColleagueSession.shouldGenerateColleagueDataFieldsForSession(instructionalOffering.getSession(), hibSession)){
			// The colleague sections must be correctly crosslisted before the colleague linkages are worked with.
			ColleagueSectionCrosslistHelper bsch = new ColleagueSectionCrosslistHelper(instructionalOffering, hibSession);
			bsch.updateCrosslists();
			
			assignSectionIndexesToAnyAddedColleagueSections(instructionalOffering, hibSession);
			
		}
		
	}

	private void assignSectionIndexesToAnyAddedColleagueSections(
			InstructionalOffering instructionalOffering, Session hibSession)  {
		List<ColleagueSection> sections = ColleagueSection.findColleagueSectionsForInstructionalOffering(instructionalOffering, hibSession);
		for (ColleagueSection cs : sections){
//			Transaction trans = hibSession.beginTransaction();
			if (cs.getSectionIndex() == null){
				try {
					cs.assignNewSectionIndex(hibSession);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			cs.updateClassSuffixForClassesIfNecessary(hibSession);
//			trans.commit();
			hibSession.flush();
		}
			
	}

	@Override
	public boolean validateConfigChangeCanOccur(
			InstructionalOffering instructionalOffering, Session hibSession) {
		return true;
	}

}
