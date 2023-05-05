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
package org.unitime.colleague.dataexchange;

import java.util.Iterator;

import org.dom4j.Element;
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.timetable.dataexchange.BaseImport;


;

/**
 * @author says
 *
 */
public class ImportColleagueRestrictions extends BaseImport {

	private static String rootName = "COLLEAGUE_RESTRICTIONS";
	private static String colleagueRestrictionName = "RESTRICTION";

	public ImportColleagueRestrictions() {
		super();
	}

	@SuppressWarnings("unchecked")
	@Override
	public void loadXml(Element rootElement) throws Exception {
		if (rootElement.getName().equalsIgnoreCase(rootName)) {
			String termCode = getRequiredStringAttribute(rootElement, "COLLEAGUE_TERM_CODE", rootName);
			beginTransaction();
			for (Iterator<Element> eIt = rootElement.elementIterator(colleagueRestrictionName); eIt.hasNext();) {
				Element colleagueRestriction = (Element) eIt.next();
				String code = getRequiredStringAttribute(colleagueRestriction, "CODE", colleagueRestrictionName);
				ColleagueRestriction restriction = ColleagueRestriction.findColleagueRestrictionTermCode(code, termCode, getHibSession());
				if (restriction == null){
					restriction = new ColleagueRestriction();
					restriction.setCode(code);
					restriction.setTermCode(termCode);
				}
				restriction.setName(getRequiredStringAttribute(colleagueRestriction, "NAME", colleagueRestrictionName));
				restriction.setDescription(getOptionalStringAttribute(colleagueRestriction, "DESCRIPTION"));

				if (restriction.getUniqueId() == null)
					getHibSession().persist(restriction);
				else
					getHibSession().merge(restriction);
			}
			commitTransaction();
		}
	}

}
