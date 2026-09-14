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
import java.util.List;

import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.banner.model.BannerSession;
import org.unitime.banner.model.BannerSession.FutureSessionUpdateMode;
import org.unitime.banner.model.dao.BannerSessionDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
import org.unitime.localization.messages.CourseMessages;
import org.unitime.timetable.ApplicationProperties;
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
import org.unitime.timetable.model.dao.SessionDAO;
import org.unitime.timetable.model.TimetableManager;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;
import org.unitime.timetable.server.admin.AdminTable;

@Service("gwtAdminTable[type=bannerSession]")
public class BannerSessions implements AdminTable {
	private static final BannerGwtMessages BANNER = Localization.create(BannerGwtMessages.class);
	private static final BannerMessages BMSG = Localization.create(BannerMessages.class);
	private static final CourseMessages MSG = Localization.create(CourseMessages.class);
	
	@Override
	public PageName name() {
		return new PageName(BANNER.pageBannerSession(), BANNER.pageBannerSessions());
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessions')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		List<ListItem> acadSessions = new ArrayList<ListItem>();
		for (org.unitime.timetable.model.Session session: org.unitime.timetable.model.Session.getAllSessions())
			acadSessions.add(new ListItem(session.getUniqueId().toString(), session.getLabel()));
		List<ListItem> futureTerms = new ArrayList<ListItem>();
		futureTerms.add(new ListItem("", "-"));
		for (BannerSession bs: BannerSession.getAllSessions())
			futureTerms.add(new ListItem(bs.getUniqueId().toString(), bs.getLabel()));
		List<ListItem> updateMode = new ArrayList<ListItem>();
		updateMode.add(new ListItem(FutureSessionUpdateMode.NO_UPDATE.name(), BMSG.nameUpdateModeDisabled(), BMSG.descUpdateModeDisabled()));
		updateMode.add(new ListItem(FutureSessionUpdateMode.DIRECT_UPDATE.name(), BMSG.nameUpdateModeDirect(), BMSG.descUpdateModeDirect()));
		updateMode.add(new ListItem(FutureSessionUpdateMode.SEND_REQUEST.name(), BMSG.nameUpdateModeRequest(), BMSG.descUpdateModeRequest()));
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(MSG.columnAcademicSession(), FieldType.list, 200, acadSessions, Flag.NOT_EMPTY, Flag.UNIQUE, Flag.LAZY),
				new Field(BMSG.colBannerTermCode(), FieldType.number, 60, 6, Flag.NOT_EMPTY),
				new Field(BMSG.colBannerCampus(), FieldType.text, 40, 3, Flag.NOT_EMPTY),
				new Field(BMSG.colStoreDataForBanner(), FieldType.toggle, 40),
				new Field(BMSG.colSendDataToBanner(), FieldType.toggle, 40),
				new Field(BMSG.colLoadingOfferings(), FieldType.toggle, 40,
						"true".equalsIgnoreCase(ApplicationProperties.getProperty("banner.sessions.showLoadOfferings")) ? null : Flag.HIDDEN)
					.setDescription(BMSG.noteLoadingOfferings()),
				new Field(BMSG.colFutureTerm(), FieldType.list, 200, futureTerms),
				new Field(BMSG.colUpdateMode(), FieldType.list, 200, updateMode),
				new Field(BMSG.colStudentCampus(), FieldType.text, 300, 500)
					.setDescription(BMSG.noteStudentCampus()),
				new Field(BMSG.colUseStudentAreaPrefix(), FieldType.toggle, 40),
				new Field(BMSG.colSubjectAreaPrefixDelim(), FieldType.text, 50, 5)
					.setDescription(BMSG.noteSubjectAreaPrefixDelim()));
		for (BannerSession bs: BannerSession.getAllSessions()) {
			Record r = data.addRecord(bs.getUniqueId());
			r.setField(0, bs.getSession().getUniqueId().toString(), false);
			r.setField(1, bs.getBannerTermCode());
			r.setField(2, bs.getBannerCampus());
			r.setField(3, bs.isStoreDataForBanner() ? "true" : "false");
			r.setField(4, bs.isSendDataToBanner() ? "true" : "false");
			r.setField(5, bs.isLoadingOfferingsFile() ? "true" : "false", bs.isLoadingOfferingsFile());
			r.setField(6, bs.getFutureSession() == null ? "" : bs.getFutureSession().getUniqueId().toString());
			r.setField(7, bs.getFutureSessionUpdateMode().name());
			r.setField(8, bs.getStudentCampus());
			r.setField(9, Boolean.TRUE.equals(bs.isUseSubjectAreaPrefixAsCampus()) ? "true" : "false");
			r.setField(10, bs.getSubjectAreaPrefixDelimiter());
			r.setDeletable(false);
		}
		data.setEditable(context.hasPermission(Right.AcademicSessionEdit));
		data.setAddable(acadSessions.size() > futureTerms.size() - 1);
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		for (BannerSession bs: BannerSession.getAllSessions()) {
			Record r = data.getRecord(bs.getUniqueId());
			if (r == null)
				delete(bs, context, hibSession);
			else
				update(bs, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		BannerSession bs = new BannerSession();
		bs.setSession(SessionDAO.getInstance().get(Long.valueOf(record.getField(0))));
		bs.setBannerTermCode(record.getField(1));
		bs.setBannerCampus(record.getField(2));
		bs.setStoreDataForBanner("true".equals(record.getField(3)));
		bs.setSendDataToBanner("true".equals(record.getField(4)));
		bs.setLoadingOfferingsFile("true".equals(record.getField(5)));
		bs.setFutureSession(record.getField(6) == null || record.getField(6).isEmpty() ? null : BannerSessionDAO.getInstance().get(Long.valueOf(record.getField(6))));
		bs.setFutureSessionUpdateMode(record.getField(7) == null || record.getField(7).isEmpty() ? FutureSessionUpdateMode.NO_UPDATE : FutureSessionUpdateMode.valueOf(record.getField(7)));
		bs.setStudentCampus(record.getField(8));
		bs.setUseSubjectAreaPrefixAsCampus("true".equals(record.getField(9)));
		bs.setSubjectAreaPrefixDelimiter(record.getField(10));
		hibSession.persist(bs);
		record.setUniqueId(bs.getUniqueId());
		ChangeLog.addChange(hibSession,
				TimetableManager.findByExternalId(context.getUser().getTrueExternalUserId()),
				bs.getSession(),
				bs,
				bs.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.CREATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(BannerSessionDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}
	
	protected void update(BannerSession bs, Record record, SessionContext context, Session hibSession) {
		if (bs == null) return;
		bs.setSession(SessionDAO.getInstance().get(Long.valueOf(record.getField(0))));
		bs.setBannerTermCode(record.getField(1));
		bs.setBannerCampus(record.getField(2));
		bs.setStoreDataForBanner("true".equals(record.getField(3)));
		bs.setSendDataToBanner("true".equals(record.getField(4)));
		bs.setLoadingOfferingsFile("true".equals(record.getField(5)));
		bs.setFutureSession(record.getField(6) == null || record.getField(6).isEmpty() ? null : BannerSessionDAO.getInstance().get(Long.valueOf(record.getField(6))));
		bs.setFutureSessionUpdateMode(record.getField(7) == null || record.getField(7).isEmpty() ? FutureSessionUpdateMode.NO_UPDATE : FutureSessionUpdateMode.valueOf(record.getField(7)));
		bs.setStudentCampus(record.getField(8));
		bs.setUseSubjectAreaPrefixAsCampus("true".equals(record.getField(9)));
		bs.setSubjectAreaPrefixDelimiter(record.getField(10));
		hibSession.merge(bs);
		ChangeLog.addChange(hibSession,
				TimetableManager.findByExternalId(context.getUser().getTrueExternalUserId()),
				bs.getSession(),
				bs,
				bs.getLabel(),
				Source.SIMPLE_EDIT, 
				Operation.UPDATE,
				null,
				null);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		throw new GwtRpcException("Banner session cannot be deleted.");
	}
	
	protected void delete(BannerSession bs, SessionContext context, Session hibSession) {
		throw new GwtRpcException("Banner session cannot be deleted.");
	}
}
