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
package org.unitime.banner.server.admin;


import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.banner.model.BannerInstrMethodCohortRestriction;
import org.unitime.banner.model.BannerLastSentSectionRestriction;
import org.unitime.banner.model.dao.BannerInstrMethodCohortRestrictionDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;
import org.unitime.timetable.gwt.shared.SimpleEditInterface;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Field;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.FieldType;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Flag;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.ListItem;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.PageName;
import org.unitime.timetable.gwt.shared.SimpleEditInterface.Record;
import org.unitime.timetable.model.ChangeLog;
import org.unitime.timetable.model.ChangeLog.Operation;
import org.unitime.timetable.model.ChangeLog.Source;
import org.unitime.timetable.model.InstructionalMethod;
import org.unitime.timetable.model.StudentGroup;
import org.unitime.timetable.model.dao.InstructionalMethodDAO;
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.dao.StudentGroupDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.admin.AdminTable;

/**
 * @author Stephanie Schluttenhofer
 */
@Service("gwtAdminTable[type=instMthdRstrct]")
public class BannerInstructionalMethodCohortRestrictions implements AdminTable {
	protected static final BannerGwtMessages MESSAGES = Localization.create(BannerGwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageBannerInstrMethodCohortRestriction(), MESSAGES.pageBannerInstrMethodCohortRestrictions());
	}

	@SuppressWarnings("unchecked")
	@Override
	@PreAuthorize("checkPermission('AcademicSessions')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> instrMethods = new ArrayList<ListItem>();
		String query = "from InstructionalMethod im order by im.label";		
		for (InstructionalMethod im: ((List<InstructionalMethod>)hibSession.createQuery(query).list())) {
			instrMethods.add(new ListItem(im.getUniqueId().toString(), im.getLabel()));
		}
		List<ListItem> cohorts = new ArrayList<ListItem>();
//		StudentGroupType sgt = StudentGroupType.findByReference("COHORT", hibSession);
//		if (sgt == null) {
//			throw new RuntimeException(MESSAGES.exceptionNoCohortStudentGroupTypeDefined());
//		}
		String cohortQuery = "from StudentGroup sg where sg.session.uniqueId = :sessId and sg.type.reference = 'COHORT' order by sg.groupName";
		for (StudentGroup sg: ((List<StudentGroup>)hibSession.createQuery(cohortQuery).setLong("sessId", context.getUser().getCurrentAcademicSessionId()).list())) {
			cohorts.add(new ListItem(sg.getUniqueId().toString(), sg.getGroupName()));
		}
		List<ListItem> restrActions = new ArrayList<ListItem>();
		restrActions.add(new ListItem(BannerLastSentSectionRestriction.restrictionActionInclude, BannerLastSentSectionRestriction.restrictionActionLabel(BannerLastSentSectionRestriction.restrictionActionInclude)));
		restrActions.add(new ListItem(BannerLastSentSectionRestriction.restrictionActionExclude, BannerLastSentSectionRestriction.restrictionActionLabel(BannerLastSentSectionRestriction.restrictionActionExclude)));
		
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldInstructionalMethod(), FieldType.list, 300, instrMethods, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldCohort(), FieldType.list, 300, cohorts, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldRestrictionAction(), FieldType.list, 150, restrActions, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldRemoved(), FieldType.toggle, 40)
				);
		data.setSortBy(0,1,2,3);
		Collection<BannerInstrMethodCohortRestriction> restrictions = BannerInstrMethodCohortRestrictionDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId());
		if (restrictions != null){
			for (BannerInstrMethodCohortRestriction restriction: restrictions) {
				Record r = data.addRecord(restriction.getUniqueId());
				r.setField(0, restriction.getInstructionalMethod().getUniqueId().toString());
				r.setField(1, restriction.getCohort().getUniqueId().toString());
				r.setField(2, restriction.getRestrictionAction());
				r.setField(3, restriction.getRemoved().toString());
				r.setDeletable(false);
				boolean used = false;
				int count = Integer.parseInt(hibSession.createQuery("select count(im) from InstrOfferingConfig ioc inner join ioc.instructionalMethod as im where im.uniqueId = :instrMethodId")
						              .setLong("instrMethodId", restriction.getInstructionalMethod().getUniqueId())
						              .uniqueResult().toString());
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
		for (BannerInstrMethodCohortRestriction restriction: BannerInstrMethodCohortRestrictionDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
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
		String action = record.getField(2);
		Boolean removed = new Boolean(record.getField(3));
		BannerInstrMethodCohortRestriction restriction = new BannerInstrMethodCohortRestriction();
		org.unitime.timetable.model.Session sess = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession);
		restriction.setSession(sess);
		InstructionalMethod im = InstructionalMethodDAO.getInstance().get(new Long(record.getField(0)));
		restriction.setInstructionalMethod(im);
		StudentGroup cohort = StudentGroupDAO.getInstance().get(new Long(record.getField(1)));
		restriction.setCohort(cohort);
		validateUniqueSessionInstrMethodCohort(restriction, hibSession);
		validateRestrictionsHaveSameActionForInstrMethod(restriction, action, removed, hibSession);
		restriction.setRestrictionAction(action);
		restriction.setRemoved(removed);
		record.setUniqueId((Long)hibSession.save(restriction));
		ChangeLog.addChange(hibSession,
				context,
				restriction,
				restriction.getInstructionalMethod().getLabel() + " " + restriction.getCohort().getGroupName() + " " + restriction.getRestrictionAction() + (restriction.getRemoved().booleanValue() ? " " + restriction.getRemoved().toString() : ""),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}
	
	protected void validateRestrictionsHaveSameActionForInstrMethod(BannerInstrMethodCohortRestriction restriction, String action, Boolean removed, Session hibSession) {
		ArrayList<BannerInstrMethodCohortRestriction> otherRestrictions = BannerInstrMethodCohortRestriction.findAllWithSameTermAndMethod(restriction, hibSession);
		if (otherRestrictions.size() >= 1) {
			if (restriction.getUniqueId() == null || (!ToolBox.equals(restriction.getRestrictionAction(), action)) && !removed) {	
				for (BannerInstrMethodCohortRestriction r : otherRestrictions) {
					if (!r.getRemoved() && (restriction.getUniqueId() == null || r.getUniqueId() != restriction.getUniqueId()) && !ToolBox.equals(r.getRestrictionAction(), action)) {
						throw new RuntimeException(MESSAGES.exceptionMustHaveSameRestrictionAction());
					}
				}
			}
		}
		
	}
	
	protected void validateUniqueSessionInstrMethodCohort(BannerInstrMethodCohortRestriction restriction, Session hibSession) {
		ArrayList<BannerInstrMethodCohortRestriction> otherRestrictions = BannerInstrMethodCohortRestriction.findAllWithSameTermAndMethod(restriction, hibSession);
		if (otherRestrictions.size() > 1) {
			for (BannerInstrMethodCohortRestriction r : otherRestrictions) {
				if ((restriction.getUniqueId() == null || r.getUniqueId() != restriction.getUniqueId())) {
					if (restriction.getCohort().equals(r.getCohort())) {
						throw new RuntimeException(MESSAGES.exceptionRestrictionMustBeUnique());
					}
				}
			}
		}
		
	}
	
	protected void update(BannerInstrMethodCohortRestriction restriction, Record record, SessionContext context, Session hibSession) {
		if (restriction == null) return;
		String action = record.getField(2);
		Boolean removed = new Boolean(record.getField(3));
		if (restriction.getUniqueId() != null && !ToolBox.equals(restriction.getInstructionalMethod().getUniqueId().toString(), record.getField(0))) {
			throw new RuntimeException(MESSAGES.exceptionRestrictionCannotChangeInstrMethod());	
		}
		if (restriction.getUniqueId() != null && !ToolBox.equals(restriction.getCohort().getUniqueId().toString(), record.getField(1))) {
			throw new RuntimeException(MESSAGES.exceptionRestrictionCannotChangeCohort());	
		}
		validateUniqueSessionInstrMethodCohort(restriction, hibSession);
		validateRestrictionsHaveSameActionForInstrMethod(restriction, action, removed, hibSession);
		if (ToolBox.equals(restriction.getInstructionalMethod().getUniqueId().toString(), record.getField(0)) &&
				ToolBox.equals(restriction.getCohort().getUniqueId().toString(), record.getField(1)) &&
				ToolBox.equals(restriction.getRestrictionAction(), action) &&
				ToolBox.equals(restriction.getRemoved(), record.getField(3))) return;
		restriction.setRestrictionAction(action);
		restriction.setRemoved(removed);
		hibSession.update(restriction);
		ChangeLog.addChange(hibSession,
				context,
				restriction,
				restriction.getInstructionalMethod().getLabel() + " " + restriction.getCohort().getGroupName() + " " + restriction.getRestrictionAction() + (restriction.getRemoved().booleanValue() ? " " + restriction.getRemoved().toString() : ""),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);	
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(BannerInstrMethodCohortRestrictionDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(BannerInstrMethodCohortRestriction restriction, SessionContext context, Session hibSession) {
		if (restriction == null) return;
		ChangeLog.addChange(hibSession,
				context,
				restriction,
				restriction.getInstructionalMethod().getLabel() + " " + restriction.getCohort().getGroupName() + " " + restriction.getRestrictionAction() + (restriction.getRemoved().booleanValue() ? " " + restriction.getRemoved().toString() : ""),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(restriction);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(BannerInstrMethodCohortRestrictionDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
