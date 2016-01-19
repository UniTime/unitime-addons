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


import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.colleague.model.ColleagueSession;
import org.unitime.colleague.model.dao.ColleagueSessionDAO;
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
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.admin.AdminTable;

/**
 * @author Stephanie Schluttenhofer
 */
@Service("gwtAdminTable[type=collSess]")
public class ColleagueSessions implements AdminTable {
	protected static final ColleagueGwtMessages MESSAGES = Localization.create(ColleagueGwtMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(MESSAGES.pageColleagueSession(), MESSAGES.pageColleagueSessions());
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessions')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MESSAGES.fieldLocationCode(), FieldType.text, 30, 20, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldTermCode(), FieldType.text, 40, 20, Flag.NOT_EMPTY),
				new Field(MESSAGES.fieldStoreDataForColleague(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldSendDataToColleague(), FieldType.toggle, 40),
				new Field(MESSAGES.fieldLoadingOfferingsFile(), FieldType.toggle, 40)
				);
		data.setSortBy(1,0);
		for (ColleagueSession collSession: ColleagueSessionDAO.getInstance().findAll(hibSession)) {
			Record r = data.addRecord(collSession.getUniqueId());
			r.setField(0, collSession.getColleagueCampus());
			r.setField(1, collSession.getColleagueTermCode());
			r.setField(2, collSession.getStoreDataForColleague().toString());
			r.setField(3, collSession.getSendDataToColleague().toString());
			r.setField(4, collSession.getLoadingOfferingsFile().toString());
			r.setDeletable(false);
		}
		data.setEditable(context.hasPermission(Right.AcademicSessionEdit));
		return data;
	}
	
	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(SimpleEditInterface data, SessionContext context, org.hibernate.Session hibSession) {
		for (ColleagueSession collSession: ColleagueSessionDAO.getInstance().findAll(hibSession)) {
			Record r = data.getRecord(collSession.getUniqueId());
			if (r == null)
				delete(collSession, context, hibSession);
			else
				update(collSession, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}
	
	
	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		ColleagueSession colleagueSession = new ColleagueSession();
		colleagueSession.setColleagueCampus(record.getField(0));
		colleagueSession.setColleagueTermCode(record.getField(1));
		colleagueSession.setStoreDataForColleague(new Boolean(record.getField(2)));
		colleagueSession.setSendDataToColleague(new Boolean(record.getField(3)));
		colleagueSession.setLoadingOfferingsFile(new Boolean(record.getField(4)));
		org.unitime.timetable.model.Session session = SessionDAO.getInstance().get(context.getUser().getCurrentAcademicSessionId(), hibSession);
		colleagueSession.setSession(session);
		record.setUniqueId((Long)hibSession.save(colleagueSession));
		ChangeLog.addChange(hibSession,
				context,
				colleagueSession,
				colleagueSession.getColleagueTermCode() + " " + colleagueSession.getColleagueCampus(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
		if (colleagueSession.isStoreDataForColleague()){
			colleagueSession.generateColleagueSectionsForSession();			
		}
	}
	
	protected void update(ColleagueSession colleagueSession, Record record, SessionContext context, Session hibSession) {
		if (colleagueSession == null) return;
		if (ToolBox.equals(colleagueSession.getColleagueCampus(), record.getField(0)) &&
				ToolBox.equals(colleagueSession.getColleagueTermCode(), record.getField(1)) &&
				ToolBox.equals(colleagueSession.getStoreDataForColleague(), record.getField(2)) &&
				ToolBox.equals(colleagueSession.getSendDataToColleague(), record.getField(3)) &&
				ToolBox.equals(colleagueSession.getLoadingOfferingsFile(), record.getField(4))
				) return;
		colleagueSession.setColleagueCampus(record.getField(0));
		colleagueSession.setColleagueTermCode(record.getField(1));
		
		boolean doStoreDataForColleague = false;
		if (!colleagueSession.isStoreDataForColleague() && new Boolean(record.getField(2))){
			doStoreDataForColleague = true;
		}
		colleagueSession.setStoreDataForColleague(new Boolean(record.getField(2)));
		
		boolean doSendDataToColleague = false;
		if (!colleagueSession.isSendDataToColleague() && new Boolean(record.getField(3))){
			doSendDataToColleague = true;
		}
		colleagueSession.setSendDataToColleague(new Boolean(record.getField(3)));
		
		colleagueSession.setLoadingOfferingsFile(new Boolean(record.getField(4)));
		hibSession.saveOrUpdate(colleagueSession);
		ChangeLog.addChange(hibSession,
				context,
				colleagueSession,
				colleagueSession.getColleagueTermCode() + " " + colleagueSession.getColleagueCampus(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);			
		
		if (doStoreDataForColleague){
			colleagueSession.generateColleagueSectionsForSession();
		}
		if (!doStoreDataForColleague && doSendDataToColleague){
			colleagueSession.assignSectionNumbersToAllSectionsForSession();
		}
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		try {
			update(ColleagueSessionDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	protected void delete(ColleagueSession collSession, SessionContext context, Session hibSession) {
		if (collSession == null) return;
		ChangeLog.addChange(hibSession,
				context,
				collSession,
				collSession.getColleagueTermCode() + " " + collSession.getColleagueCampus(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.delete(collSession);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(ColleagueSessionDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
}
