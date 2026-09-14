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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.BannerTermCrnProperties;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.banner.model.dao.BannerTermCrnPropertiesDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
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
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.admin.AdminTable;

@Service("gwtAdminTable[type=bannerTermCrnProps]")
public class BannerTermCRNProperties implements AdminTable {
	private static final BannerGwtMessages BANNER = Localization.create(BannerGwtMessages.class);
	private static final BannerMessages BMSG = Localization.create(BannerMessages.class);

	@Override
	public PageName name() {
		return new PageName(BANNER.pageBannerTermCRNProperties());
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessions')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> bannerTermCodes = new ArrayList<ListItem>();
		List<ListItem> bannerSessions = new ArrayList<ListItem>();
		Set<String> codes = new HashSet<String>();
		boolean canAdd = false;
		for (BannerSession bs: BannerSession.getAllSessions()) {
			bannerSessions.add(new ListItem(bs.getUniqueId().toString(), bs.getLabel()));
			if (codes.add(bs.getBannerTermCode()))
				bannerTermCodes.add(new ListItem(bs.getBannerTermCode(), bs.getBannerTermCode()));
			if (bs.getBannerTermCrnProperties() == null) canAdd = true;
		}
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(BMSG.colBannerTermCode(), FieldType.list, 300, bannerTermCodes, Flag.NOT_EMPTY),
				new Field(BMSG.colBannerSessions(), FieldType.multi, 300, bannerSessions, Flag.NOT_EMPTY, Flag.UNIQUE),
				new Field(BMSG.colLastCRN(), FieldType.number, 50, 5, Flag.NOT_EMPTY),
				new Field(BMSG.colMinimumCRN(), FieldType.number, 50, 5, Flag.NOT_EMPTY),
				new Field(BMSG.colMaximumCRN(), FieldType.number, 50, 5, Flag.NOT_EMPTY),
				new Field(BMSG.colSearchFlag(), FieldType.toggle, 40)
				);
		for (BannerTermCrnProperties prop: BannerTermCrnProperties.getAllBannerTermCrnProperties()) {
			Record r = data.addRecord(prop.getUniqueId());
			r.setField(0, prop.getBannerTermCode(), false);
			r.setField(1, "");
			for (BannerSession bs: prop.getBannerSessions())
				r.addToField(1, bs.getUniqueId().toString());
			r.setField(2, prop.getLastCrn().toString());
			r.setField(3, prop.getMinCrn().toString());
			r.setField(4, prop.getMaxCrn().toString());
			r.setField(5, prop.getSearchFlag() ? "true" : "false");
			r.setDeletable(prop.getBannerSessions().isEmpty());
		}
		data.setEditable(context.hasPermission(Right.AcademicSessionEdit));
		data.setAddable(canAdd);
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (BannerTermCrnProperties prop: BannerTermCrnProperties.getAllBannerTermCrnProperties()) {
			Record r = data.getRecord(prop.getUniqueId());
			if (r == null)
				delete(prop, context, hibSession);
			else
				update(prop, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		BannerTermCrnProperties prop = new BannerTermCrnProperties();
		prop.setBannerTermCode(record.getField(0));
		prop.setBannerSessions(new HashSet<BannerSession>());
		for (String id: record.getValues(1)) {
			if (id == null || id.isEmpty()) continue;
			BannerSession bs = BannerSessionDAO.getInstance().get(Long.valueOf(id));
			if (!bs.getBannerTermCode().equals(prop.getBannerTermCode()))
				throw new GwtRpcException(BMSG.errorBannerTermCodeDoesNotMatch(bs.getBannerTermCode(), bs.getLabel(), prop.getBannerTermCode()));
			prop.addToBannerSessions(BannerSessionDAO.getInstance().get(Long.valueOf(id)));
		}
		prop.setLastCrn(Integer.valueOf(record.getField(2)));
		prop.setMinCrn(Integer.valueOf(record.getField(3)));
		prop.setMaxCrn(Integer.valueOf(record.getField(4)));
		prop.setSearchFlag("true".equalsIgnoreCase(record.getField(5)));
		hibSession.persist(prop);
		for (BannerSession bs: prop.getBannerSessions())
			hibSession.merge(bs);
		record.setUniqueId(prop.getUniqueId());
		for (BannerSession bs: prop.getBannerSessions())
			ChangeLog.addChange(hibSession,
					TimetableManager.findByExternalId(context.getUser().getTrueExternalUserId()),
					bs.getSession(),
					prop,
					prop.getBannerTermCode() + " " + prop.getLastCrn() + "/" + prop.getMinCrn() + " .. " + prop.getMaxCrn(),
					Source.SIMPLE_EDIT, 
					Operation.CREATE,
					null,
					null);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(BannerTermCrnPropertiesDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void update(BannerTermCrnProperties prop, Record record, SessionContext context, Session hibSession) {
		if (prop == null) return;
		boolean change = !ToolBox.equals(prop.getBannerTermCode(), record.getField(0)) ||
				!ToolBox.equals(prop.getLastCrn().toString(), record.getField(2)) ||
				!ToolBox.equals(prop.getMinCrn().toString(), record.getField(3)) ||
				!ToolBox.equals(prop.getMaxCrn().toString(), record.getField(4)) ||
				!ToolBox.equals(prop.isSearchFlag() ? "true" : "false", record.getField(5));
		prop.setBannerTermCode(record.getField(0));
		prop.setLastCrn(Integer.valueOf(record.getField(2)));
		prop.setMinCrn(Integer.valueOf(record.getField(3)));
		prop.setMaxCrn(Integer.valueOf(record.getField(4)));
		prop.setSearchFlag("true".equalsIgnoreCase(record.getField(5)));
		List<BannerSession> sessions = new ArrayList<BannerSession>(prop.getBannerSessions());
		id: for (String id: record.getValues(1)) {
			if (id == null || id.isEmpty()) continue;
			for (Iterator<BannerSession> i = sessions.iterator(); i.hasNext(); ) {
				BannerSession bs = i.next();
				if (bs.getUniqueId().equals(Long.valueOf(id))) {
					i.remove();
					continue id;
				}
			}
			BannerSession bs = BannerSessionDAO.getInstance().get(Long.valueOf(id));
			if (!bs.getBannerTermCode().equals(prop.getBannerTermCode()))
				throw new GwtRpcException(BMSG.errorBannerTermCodeDoesNotMatch(bs.getBannerTermCode(), bs.getLabel(), prop.getBannerTermCode()));
			prop.addToBannerSessions(BannerSessionDAO.getInstance().get(Long.valueOf(id)));
			bs.setBannerTermCrnProperties(prop);
			hibSession.merge(bs);
			change = true;
		}
		for (BannerSession bs: sessions) {
			prop.getBannerSessions().remove(bs);
			bs.setBannerTermCrnProperties(null);
			hibSession.merge(bs);
			change = true;
			ChangeLog.addChange(hibSession,
					TimetableManager.findByExternalId(context.getUser().getTrueExternalUserId()),
					bs.getSession(),
					prop,
					prop.getBannerTermCode() + " " + prop.getLastCrn() + "/" + prop.getMinCrn() + " .. " + prop.getMaxCrn(),
					Source.SIMPLE_EDIT, 
					Operation.DELETE,
					null,
					null);
		}
		hibSession.merge(prop);
		if (change)
			for (BannerSession bs: prop.getBannerSessions())
				ChangeLog.addChange(hibSession,
						TimetableManager.findByExternalId(context.getUser().getTrueExternalUserId()),
						bs.getSession(),
						prop,
						prop.getBannerTermCode() + " " + prop.getLastCrn() + "/" + prop.getMinCrn() + " .. " + prop.getMaxCrn(),
						Source.SIMPLE_EDIT, 
						Operation.UPDATE,
						null,
						null);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(BannerTermCrnPropertiesDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
	
	protected void delete(BannerTermCrnProperties prop, SessionContext context, Session hibSession) {
		if (prop == null) return;
		for (Iterator<BannerSession> i = prop.getBannerSessions().iterator(); i.hasNext(); ) {
			BannerSession bs = i.next();
			bs.setBannerTermCrnProperties(null);
			i.remove();
			ChangeLog.addChange(hibSession,
					TimetableManager.findByExternalId(context.getUser().getTrueExternalUserId()),
					bs.getSession(),
					prop,
					prop.getBannerTermCode() + " " + prop.getLastCrn() + "/" + prop.getMinCrn() + " .. " + prop.getMaxCrn(),
					Source.SIMPLE_EDIT, 
					Operation.DELETE,
					null,
					null);
			hibSession.merge(bs);
		}
		hibSession.remove(prop);		
	}

}
