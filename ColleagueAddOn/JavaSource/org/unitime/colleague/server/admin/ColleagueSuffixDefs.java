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

import java.util.ArrayList;
import java.util.List;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.ColleagueSuffixDef;
import org.unitime.colleague.model.dao.ColleagueSuffixDefDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.ColleagueGwtMessages;
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
import org.unitime.timetable.model.ItypeDesc;
import org.unitime.timetable.model.SubjectArea;
import org.unitime.timetable.model.dao.ItypeDescDAO;
import org.unitime.timetable.model.dao.SubjectAreaDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.admin.AdminTable;

/**
 * @author Stephanie Schluttenhofer
 */
@Service("gwtAdminTable[type=suffixDefs]")
public class ColleagueSuffixDefs implements AdminTable {
	protected static final ColleagueGwtMessages MESSAGES = Localization.create(ColleagueGwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageColleagueSuffixDef(), MESSAGES.pageColleagueSuffixDefs());
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessions')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> itypes = new ArrayList<ListItem>();
		for (ItypeDesc itype: ItypeDescDAO.getInstance().findAll(hibSession)) {
			itypes.add(new ListItem(itype.getItype().toString(), itype.getAbbv() + " - " + itype.getDesc()));
		}
		List<ListItem> subjectAreas = new ArrayList<ListItem>();
		for (SubjectArea sa: SubjectAreaDAO.getInstance().findBySession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			subjectAreas.add(new ListItem(sa.getUniqueId().toString(), sa.getSubjectAreaAbbreviation()));
		}
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldSubjectArea(), FieldType.list, 300, subjectAreas),
				new Field(MESSAGES.fieldInstructionalMethod(), FieldType.list, 300, itypes),
				new Field(MESSAGES.fieldCourseSuffix(), FieldType.text, 80, 5),
				new Field(MESSAGES.fieldMethodPrefix(), FieldType.text, 20, 1),
				new Field(MESSAGES.fieldPrefix(), FieldType.text, 20, 1),
				new Field(MESSAGES.fieldSuffix(), FieldType.text, 20, 1),
				new Field(MESSAGES.fieldMinSectionNumber(), FieldType.number, 30, 3, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldMaxSectionNumber(), FieldType.number, 30, 3, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldLocationCode(), FieldType.text, 60, 20),
				new Field(MESSAGES.fieldNote(), FieldType.text, 300, 500));
		data.setSortBy(1,2);
		for (ColleagueSuffixDef suffixDef: ColleagueSuffixDef.getAllColleagueSuffixDefsForSession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.addRecord(suffixDef.getUniqueId());
			r.setField(0, (suffixDef.getSubjectAreaId() == null?null:suffixDef.getSubjectAreaId().toString()));
			r.setField(1, (suffixDef.getItypeId() == null?null:suffixDef.getItypeId().toString()));
			r.setField(2, suffixDef.getCourseSuffix());
			r.setField(3, suffixDef.getItypePrefix());
			r.setField(4, suffixDef.getPrefix());
			r.setField(5, suffixDef.getSuffix());
			r.setField(6, suffixDef.getMinSectionNum().toString());
			r.setField(7, suffixDef.getMaxSectionNum().toString());
			r.setField(8, suffixDef.getCampusCode());
			r.setField(9, suffixDef.getNote());
			r.setDeletable(true);
		}
		data.setEditable(context.hasPermission(Right.AcademicSessionEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (ColleagueSuffixDef suffixes: ColleagueSuffixDef.getAllColleagueSuffixDefsForSession(hibSession, context.getUser().getCurrentAcademicSessionId())) {
			Record r = data.getRecord(suffixes.getUniqueId());
			if (r == null)
				delete(suffixes, context, hibSession);
			else
				update(suffixes, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		ColleagueSuffixDef suffix = new ColleagueSuffixDef();
		suffix.setSubjectAreaId(record.getField(0)== null?null:new Long(record.getField(0)));
		suffix.setItypeId(record.getField(1)== null?null:new Integer(record.getField(1)));
		suffix.setCourseSuffix(record.getField(2));
		suffix.setItypePrefix(record.getField(3));
		suffix.setPrefix(record.getField(4));
		suffix.setSuffix(record.getField(5));
		suffix.setMinSectionNum(new Integer(record.getField(6)));
		suffix.setMaxSectionNum(new Integer(record.getField(7)));
		suffix.setCampusCode(record.getField(8));
		suffix.setNote(record.getField(9));
		ColleagueSession collSession = ColleagueSession.findColleagueSessionForSession(context.getUser().getCurrentAcademicSessionId(), hibSession);
		suffix.setTermCode(collSession.getColleagueTermCode());
		record.setUniqueId((Long)hibSession.save(suffix));
		ChangeLog.addChange(hibSession,
				context,
				suffix,
				(suffix.getSubjectAreaId() == null?"":suffix.getSubjectAreaId().toString() + " ")+(suffix.getItypeId() == null?"":(suffix.getItypeId().toString() + " ")) + suffix.getCourseSuffix(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}

	protected void update(ColleagueSuffixDef suffix, Record record, SessionContext context, Session hibSession) {
		if (suffix == null) return;
		boolean changed =
				!ToolBox.equals(suffix.getSubjectAreaId(), record.getField(0)) ||
				!ToolBox.equals(suffix.getItypeId(), record.getField(1)) ||
				!ToolBox.equals(suffix.getCourseSuffix(), record.getField(2)) ||
				!ToolBox.equals(suffix.getItypePrefix(), record.getField(3)) ||
				!ToolBox.equals(suffix.getPrefix(), record.getField(4)) ||
				!ToolBox.equals(suffix.getSuffix(), record.getField(5)) ||
				!ToolBox.equals(suffix.getMinSectionNum(), record.getField(6)) ||
				!ToolBox.equals(suffix.getMaxSectionNum(), record.getField(7)) ||
				!ToolBox.equals(suffix.getCampusCode(), record.getField(8)) ||
				!ToolBox.equals(suffix.getNote(), record.getField(9))
				;
			suffix.setSubjectAreaId(record.getField(0) == null?null:new Long(record.getField(0)));
			suffix.setItypeId(record.getField(1) == null?null:new Integer(record.getField(1)));
			suffix.setCourseSuffix(record.getField(2));
			suffix.setItypePrefix(record.getField(3));
			suffix.setPrefix(record.getField(4));
			suffix.setSuffix(record.getField(5));
			suffix.setMinSectionNum(new Integer(record.getField(6)));
			suffix.setMaxSectionNum(new Integer(record.getField(7)));
			suffix.setCampusCode(record.getField(8));
			suffix.setNote(record.getField(9));
			hibSession.saveOrUpdate(suffix);
			if (changed)
				ChangeLog.addChange(hibSession,
						context,
						suffix,
						(suffix.getSubjectAreaId() == null?"":suffix.getSubjectAreaId().toString() + " ")+(suffix.getItypeId() == null?"":(suffix.getItypeId().toString() + " ")) + suffix.getCourseSuffix(),
						Source.SIMPLE_EDIT, 
						Operation.UPDATE,
						null,
						null);
	}
	
	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(ColleagueSuffixDefDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void delete(ColleagueSuffixDef suffix, SessionContext context, Session hibSession) {
		if (suffix == null) return;
		ChangeLog.addChange(hibSession,
				context,
				suffix,
				(suffix.getSubjectAreaId() == null?"":suffix.getSubjectAreaId().toString() + " ")+(suffix.getItypeId() == null?"":(suffix.getItypeId().toString() + " ")) + suffix.getCourseSuffix(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(suffix);		
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(ColleagueSuffixDefDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
	
	

}
