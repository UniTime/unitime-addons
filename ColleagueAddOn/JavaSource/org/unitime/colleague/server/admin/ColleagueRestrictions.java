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
package org.unitime.colleague.server.admin;


import java.util.Collection;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.colleague.model.ColleagueRestriction;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.dao.ColleagueRestrictionDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.ColleagueGwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.admin.AdminTable;

/**
 * @author Stephanie Schluttenhofer
 */
@Service("gwtAdminTable[type=restrictions]")
public class ColleagueRestrictions implements AdminTable {
	protected static final ColleagueGwtMessages MESSAGES = Localization.create(ColleagueGwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageColleagueRestriction(), MESSAGES.pageColleagueRestrictions());
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessions')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldCode(), FieldType.text, 60, 8, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldName(), FieldType.text, 150, 30, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldNote(), FieldType.text, 300, 500, Flag.NOT_EMPTY)
				);
		data.setSortBy(1,2,3);
		Collection<ColleagueRestriction> restrictions = ColleagueRestriction.getAllColleagueRestrictionsForSession(hibSession, context.getUser().getCurrentAcademicSessionId());
		if (restrictions != null){
			for (ColleagueRestriction restriction: restrictions) {
				Record r = data.addRecord(restriction.getUniqueId());
				r.setField(0, restriction.getCode());
				r.setField(1, restriction.getName());
				r.setField(2, restriction.getDescription());
				boolean used = false;
				
				int count = hibSession.createQuery("select count(cr) from ColleagueSection cs inner join cs.restrictions as cr where cr.uniqueId = :restrictionId", Number.class)
						              .setParameter("restrictionId", restriction.getUniqueId())
						              .uniqueResult().intValue();
				used = count > 0;
				r.setDeletable(!used);
			}
		}
		data.setEditable(context.hasPermission(Right.AcademicSessionEdit));
		return data;
	}
	
	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(SimpleEditInterface data, SessionContext context, org.hibernate.Session hibSession) {
		for (ColleagueRestriction restriction: ColleagueRestriction.getAllColleagueRestrictionsForSession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(restriction.getUniqueId());
			if (r == null)
				delete(restriction, context, hibSession);
			else
				update(restriction, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}
	
	
	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		ColleagueRestriction restriction = new ColleagueRestriction();
		restriction.setCode(record.getField(0));
		restriction.setName(record.getField(1));
		restriction.setDescription(record.getField(2));
		ColleagueSession collSession = ColleagueSession.findColleagueSessionForSession(context.getUser().getCurrentAcademicSessionId(), hibSession);
		restriction.setTermCode(collSession.getColleagueTermCode());
		hibSession.persist(restriction);
		record.setUniqueId(restriction.getUniqueId());
		ChangeLog.addChange(hibSession,
				context,
				restriction,
				restriction.getCode() + " " + restriction.getName(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(ColleagueRestriction restriction, Record record, SessionContext context, Session hibSession) {
		if (restriction == null) return;
		if (ToolBox.equals(restriction.getCode(), record.getField(0)) &&
				ToolBox.equals(restriction.getName(), record.getField(1)) &&
				ToolBox.equals(restriction.getDescription(), record.getField(2))) return;
		restriction.setCode(record.getField(0));
		restriction.setName(record.getField(1));
		restriction.setDescription(record.getField(2));
		hibSession.merge(restriction);
		ChangeLog.addChange(hibSession,
				context,
				restriction,
				restriction.getCode() + " " + restriction.getName(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);	
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(ColleagueRestrictionDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(ColleagueRestriction restriction, SessionContext context, Session hibSession) {
		if (restriction == null) return;
		ChangeLog.addChange(hibSession,
				context,
				restriction,
				restriction.getCode() + " " + restriction.getName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.remove(restriction);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(ColleagueRestrictionDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
