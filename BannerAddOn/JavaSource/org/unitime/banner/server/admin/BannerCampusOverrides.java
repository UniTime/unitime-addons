package org.unitime.banner.server.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.dao.BannerCampusOverrideDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;
import org.unitime.timetable.gwt.resources.GwtMessages;
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

@Service("gwtAdminTable[type=bannerCampusOverride]")
public class BannerCampusOverrides implements AdminTable, AdminTable.HasUpDown {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static final BannerGwtMessages BANNER = Localization.create(BannerGwtMessages.class);

	@Override
	public PageName name() {
		return new PageName(BANNER.pageBannerCampusOverride(), BANNER.pageBannerCampusOverrides());
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessions')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(BANNER.colBannerCampusCode(), FieldType.text, 100, 20, Flag.NOT_EMPTY, Flag.UNIQUE),
				new Field(BANNER.colBannerCampusName(), FieldType.text, 320, 60, Flag.NOT_EMPTY),
				new Field(BANNER.colFirstBannerTermCode(), FieldType.number, 62, 8)
					.setDescription(BANNER.descFirstBannerTermCode()),
				new Field(BANNER.colLastBannerTermCode(), FieldType.number, 62, 8)
					.setDescription(BANNER.descLastBannerTermCode()),
				new Field(BANNER.colBannerCampusVisible(), FieldType.toggle, 40)
					.setDescription(BANNER.descBannerCampusVisible()),
				new Field(BANNER.colUsedCampusCodeCalc(), FieldType.toggle, 40)
					.setDescription(BANNER.descUsedCampusCodeCalc()),
				new Field(BANNER.colRegexAcademicInitiative(), FieldType.text, 320, 60)
					.setDescription(BANNER.descRegexAcademicInitiative()),
				new Field(BANNER.colRegexManagingDeptCode(), FieldType.text, 320, 60)
					.setDescription(BANNER.descRegexManagingDeptCode()),
				new Field(BANNER.colRegexCampusCodeToOverride(), FieldType.text, 320, 60)
					.setDescription(BANNER.descRegexCampusCodeToOverride())
				);
		data.setSaveOrder(false);
		data.setCanMoveUpAndDown(true);
		data.setAllowSort(false);
		for (BannerCampusOverride bannerCampusOverride: BannerCampusOverrideDAO.getInstance().getSession().createQuery(
				"from BannerCampusOverride order by order", BannerCampusOverride.class).list()) {
			Record r = data.addRecord(bannerCampusOverride.getUniqueId());
			r.setField(0, bannerCampusOverride.getBannerCampusCode());
			r.setField(1, bannerCampusOverride.getBannerCampusName());
			r.setField(2, bannerCampusOverride.getFirstBannerTerm());
			r.setField(3, bannerCampusOverride.getLastBannerTerm());
			r.setField(4, bannerCampusOverride.getVisible() != null && bannerCampusOverride.getVisible() ? "true" : "false");
			r.setField(5, bannerCampusOverride.getUsedDefaultCalc() != null && bannerCampusOverride.getUsedDefaultCalc() ? "true" : "false");
			r.setField(6, bannerCampusOverride.getAcademicInitiativeRegex());
			r.setField(7, bannerCampusOverride.getManagingDeptCodeRegex());
			r.setField(8, bannerCampusOverride.getCampusCodeRegex());
		}
		data.setEditable(context.hasPermission(Right.AcademicSessionEdit));
		return data;
	}
	
	protected int nextOrd(Set<Integer> ords) {
		for (int i = 0; i < ords.size() + 1; i++) {
			if (!ords.contains(i)) {
				ords.add(i);
				return i;
			}
		}
		return ords.size();
	}
	
	protected int nextOrd() {
		List<BannerCampusOverride> overrides = BannerCampusOverrideDAO.getInstance().findAll();
		int idx = 0;
		t: while (true) {
			for (BannerCampusOverride t: overrides) {
				if (idx == t.getOrder()) { idx++; continue t; }
			}
			return idx;
		}
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
		Set<Integer> ords = new HashSet<Integer>();
		for (Record r: data.getRecords()) {
			if (r.isEmpty(data)) continue;
			r.setOrder(nextOrd(ords));
		}
		for (BannerCampusOverride bannerCampusOverride: BannerCampusOverride.getAllBannerCampusOverrides()) {
			Record r = data.getRecord(bannerCampusOverride.getUniqueId());
			if (r == null)
				delete(bannerCampusOverride, context, hibSession);
			else
				update(bannerCampusOverride, r, context, hibSession);
		}
		for (Record r: data.getNewRecords())
			save(r, context, hibSession);
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
		if (record.getOrder() == null) record.setOrder(nextOrd());
		BannerCampusOverride bannerCampusOverride = new BannerCampusOverride();
		bannerCampusOverride.setBannerCampusCode(record.getField(0));
		bannerCampusOverride.setBannerCampusName(record.getField(1));
		bannerCampusOverride.setFirstBannerTerm(record.getField(2));
		bannerCampusOverride.setLastBannerTerm(record.getField(3));
		bannerCampusOverride.setVisible("true".equals(record.getField(4)));
		bannerCampusOverride.setUsedDefaultCalc("true".equals(record.getField(5)));
		bannerCampusOverride.setAcademicInitiativeRegex(record.getField(6));
		bannerCampusOverride.setManagingDeptCodeRegex(record.getField(7));
		bannerCampusOverride.setCampusCodeRegex(record.getField(8));
		bannerCampusOverride.setOrder(record.getOrder());
		hibSession.persist(bannerCampusOverride);
		record.setUniqueId(bannerCampusOverride.getUniqueId());
		ChangeLog.addChange(hibSession,
				context,
				bannerCampusOverride,
				bannerCampusOverride.getBannerCampusCode() + " " + bannerCampusOverride.getBannerCampusName(),
				Source.SIMPLE_EDIT,
				Operation.CREATE,
				null,
				null);
	}
	
	protected void update(BannerCampusOverride bannerCampusOverride, Record record, SessionContext context, Session hibSession) {
		if (bannerCampusOverride == null) return;
		if (!ToolBox.equals(bannerCampusOverride.getBannerCampusCode(), record.getField(0)) ||
				!ToolBox.equals(bannerCampusOverride.getBannerCampusName(), record.getField(1)) ||
				!ToolBox.equals(bannerCampusOverride.getFirstBannerTerm(), record.getField(2)) ||
				!ToolBox.equals(bannerCampusOverride.getLastBannerTerm(), record.getField(3)) ||
				!ToolBox.equals(bannerCampusOverride.getVisible(), record.getField(4)) ||
				!ToolBox.equals(bannerCampusOverride.getUsedDefaultCalc(), record.getField(5)) ||
				!ToolBox.equals(bannerCampusOverride.getAcademicInitiativeRegex(), record.getField(6)) ||
				!ToolBox.equals(bannerCampusOverride.getManagingDeptCodeRegex(), record.getField(7)) ||
				!ToolBox.equals(bannerCampusOverride.getCampusCodeRegex(), record.getField(8)) ||
				(record.getOrder() != null && !ToolBox.equals(bannerCampusOverride.getOrder(), record.getOrder()))
				) {
			bannerCampusOverride.setBannerCampusCode(record.getField(0));
			bannerCampusOverride.setBannerCampusName(record.getField(1));
			bannerCampusOverride.setFirstBannerTerm(record.getField(2));
			bannerCampusOverride.setLastBannerTerm(record.getField(3));
			bannerCampusOverride.setVisible("true".equals(record.getField(4)));
			bannerCampusOverride.setUsedDefaultCalc("true".equals(record.getField(5)));
			bannerCampusOverride.setAcademicInitiativeRegex(record.getField(6));
			bannerCampusOverride.setManagingDeptCodeRegex(record.getField(7));
			bannerCampusOverride.setCampusCodeRegex(record.getField(8));
			if (record.getOrder() != null)
				bannerCampusOverride.setOrder(record.getOrder());
			hibSession.merge(bannerCampusOverride);
			ChangeLog.addChange(hibSession,
					context,
					bannerCampusOverride,
					bannerCampusOverride.getBannerCampusCode() + " " + bannerCampusOverride.getBannerCampusName(),
					Source.SIMPLE_EDIT, 
					Operation.UPDATE,
					null,
					null);
		}
	}

	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void update(Record record, SessionContext context, Session hibSession) {
		update(BannerCampusOverrideDAO.getInstance().get(record.getUniqueId(), hibSession), record, context, hibSession);
	}

	protected void delete(BannerCampusOverride bannerCampusOverride, SessionContext context, Session hibSession) {
		if (bannerCampusOverride == null) return;
		ChangeLog.addChange(hibSession,
				context,
				bannerCampusOverride,
				bannerCampusOverride.getBannerCampusCode() + " " + bannerCampusOverride.getBannerCampusName(),
				Source.SIMPLE_EDIT, 
				Operation.DELETE,
				null,
				null);
		hibSession.remove(bannerCampusOverride);		
	}

	
	@Override
	@PreAuthorize("checkPermission('AcademicSessionEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		delete(BannerCampusOverrideDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}
	
	@Override
	public void move(Record record, boolean up, SessionContext context, Session hibSession) {
		BannerCampusOverride type = BannerCampusOverrideDAO.getInstance().get(record.getUniqueId(), hibSession);
		if (type != null) {
			boolean found = false;
			int add = (up ? 1 : -1);
			for (BannerCampusOverride r: BannerCampusOverrideDAO.getInstance().findAll()) {
				if (r.getOrder() + add == type.getOrder()) {
					r.setOrder(r.getOrder() + add); 
                    hibSession.merge(r);
                    found = true;
                }
			}
			if (found) {
                type.setOrder(type.getOrder() - add);
                record.setOrder(type.getOrder());
                hibSession.merge(type);
            }
		}
	}

}
