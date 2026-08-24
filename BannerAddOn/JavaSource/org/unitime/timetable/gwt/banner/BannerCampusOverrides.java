package org.unitime.timetable.gwt.banner;

import org.cpsolver.ifs.util.ToolBox;
import org.hibernate.Session;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.dao.BannerCampusOverrideDAO;
import org.unitime.localization.impl.Localization;
import org.unitime.localization.messages.BannerMessages;
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
public class BannerCampusOverrides implements AdminTable {
	protected static final GwtMessages MESSAGES = Localization.create(GwtMessages.class);
	private static final BannerMessages BANNER = Localization.create(BannerMessages.class);

	public BannerCampusOverrides() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public PageName name() {
		return new PageName(BANNER.pageBannerCampusOverride(), BANNER.pageBannerCampusOverrides());
	}

	@Override
	@PreAuthorize("checkPermission('Campuses')")
	public SimpleEditInterface load(SessionContext context, Session hibSession) {
		// TODO Auto-generated method stub
		SimpleEditInterface data = new SimpleEditInterface(
				new Field(BANNER.colBannerCampusCode(), FieldType.text, 120, 20, Flag.NOT_EMPTY, Flag.UNIQUE),
				new Field(BANNER.colBannerCampusName(), FieldType.text, 360, 60, Flag.NOT_EMPTY),
				new Field(BANNER.colFirstBannerTermCode(), FieldType.text, 100, 8),
				new Field(BANNER.colLastBannerTermCode(), FieldType.text, 100, 8),
				new Field(BANNER.colBannerCampusVisibleOnBannerOfferingPage(), FieldType.toggle, 40),
				new Field(BANNER.colUsedCampusCodeCalc(), FieldType.toggle, 40),
				new Field(BANNER.colOverrideCalcCampusCode(), FieldType.toggle, 40),
				new Field(BANNER.colRegexAcademicInitiative(), FieldType.text, 360, 60),
				new Field(BANNER.colRegexManagingDeptCode(), FieldType.text, 360, 60),
				new Field(BANNER.colRegexCampusCodeToOverride(), FieldType.text, 360, 60)
				
				);
		data.setSortBy(1,2);
		for (BannerCampusOverride bannerCampusOverride: BannerCampusOverride.getAllBannerCampusOverrides()) {
			Record r = data.addRecord(bannerCampusOverride.getUniqueId());
			r.setField(0, bannerCampusOverride.getBannerCampusCode());
			r.setField(1, bannerCampusOverride.getBannerCampusName());
			r.setField(2, bannerCampusOverride.getFirstBannerTerm());
			r.setField(3, bannerCampusOverride.getLastBannerTerm());
			r.setField(4, bannerCampusOverride.getVisible() != null && bannerCampusOverride.getVisible() ? "true" : "false");
			r.setField(5, bannerCampusOverride.getUsedDefaultCalc() != null && bannerCampusOverride.getUsedDefaultCalc() ? "true" : "false");
			r.setField(6, bannerCampusOverride.isReplaceCampusCode() != null && bannerCampusOverride.isReplaceCampusCode() ? "true" : "false");
			r.setField(7, bannerCampusOverride.getAcademicInitiativeRegex());
			r.setField(8, bannerCampusOverride.getManagingDeptCodeRegex());
			r.setField(9, bannerCampusOverride.getCampusCodeRegex());
			r.setDeletable(Boolean.FALSE);
		}
		data.setEditable(context.hasPermission(Right.CampusEdit));
		return data;
	}

	@Override
	@PreAuthorize("checkPermission('CampusEdit')")
	public void save(SimpleEditInterface data, SessionContext context, Session hibSession) {
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
	@PreAuthorize("checkPermission('CampusEdit')")
	public void save(Record record, SessionContext context, Session hibSession) {
				
		BannerCampusOverride bannerCampusOverride = new BannerCampusOverride();
		bannerCampusOverride.setBannerCampusCode(record.getField(0));
		bannerCampusOverride.setBannerCampusName(record.getField(1));
		bannerCampusOverride.setFirstBannerTerm(record.getField(2));
		bannerCampusOverride.setLastBannerTerm(record.getField(3));
		bannerCampusOverride.setVisible("true".equals(record.getField(4)));
		bannerCampusOverride.setUsedDefaultCalc("true".equals(record.getField(5)));
		bannerCampusOverride.setReplaceCampusCode("true".equals(record.getField(6)));
		bannerCampusOverride.setAcademicInitiativeRegex(record.getField(7));
		bannerCampusOverride.setManagingDeptCodeRegex(record.getField(8));
		bannerCampusOverride.setCampusCodeRegex(record.getField(9));

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
				!ToolBox.equals(bannerCampusOverride.getReplaceCampusCode(), record.getField(6)) ||
				!ToolBox.equals(bannerCampusOverride.getAcademicInitiativeRegex(), record.getField(7)) ||
				!ToolBox.equals(bannerCampusOverride.getManagingDeptCodeRegex(), record.getField(8)) ||
				!ToolBox.equals(bannerCampusOverride.getCampusCodeRegex(), record.getField(9))
				) {
			bannerCampusOverride.setBannerCampusCode(record.getField(0));
			bannerCampusOverride.setBannerCampusName(record.getField(1));
			bannerCampusOverride.setFirstBannerTerm(record.getField(2));
			bannerCampusOverride.setLastBannerTerm(record.getField(3));
			bannerCampusOverride.setVisible("true".equals(record.getField(4)));
			bannerCampusOverride.setUsedDefaultCalc("true".equals(record.getField(5)));
			bannerCampusOverride.setReplaceCampusCode("true".equals(record.getField(6)));
			bannerCampusOverride.setAcademicInitiativeRegex(record.getField(7));
			bannerCampusOverride.setManagingDeptCodeRegex(record.getField(8));
			bannerCampusOverride.setCampusCodeRegex(record.getField(9));
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
	@PreAuthorize("checkPermission('CampusEdit')")
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
	@PreAuthorize("checkPermission('CampusEdit')")
	public void delete(Record record, SessionContext context, Session hibSession) {
		// TODO Auto-generated method stub
		delete(BannerCampusOverrideDAO.getInstance().get(record.getUniqueId(), hibSession), context, hibSession);
	}

}
