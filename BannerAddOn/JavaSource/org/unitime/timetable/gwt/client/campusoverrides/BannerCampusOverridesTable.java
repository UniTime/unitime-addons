package org.unitime.timetable.gwt.client.campusoverrides;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.unitime.timetable.gwt.client.page.UniTimePageHeader;
import org.unitime.timetable.gwt.client.widgets.P;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.HasColumnName;
import org.unitime.timetable.gwt.client.widgets.UniTimeTableHeader.Operation;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.BannerCampusOverridesColumn;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class BannerCampusOverridesTable extends UniTimeTable<BannerCampusOverrideInterface> {

	protected static final BannerGwtMessages MESSAGES = GWT.create(BannerGwtMessages.class);
	private BannerCampusOverridesColumn iSortBy = null;
	private boolean iAsc = true;	
	private boolean iSelectable = true;

	public BannerCampusOverridesTable() {
		setHeaderData();
		addStyleName("unitime-Departments");
	}
	
	public void selectDept(int row, boolean value) {
		Widget w = getWidget(row, 0);
		if (w != null && w instanceof CheckBox) {
			((CheckBox)w).setValue(value);
		}
	}
	
	
	public String getColumnName(BannerCampusOverridesColumn column) {
		switch (column) {
		case BANNER_CAMPUS_CODE: return MESSAGES.colBannerCampusCode();
		case BANNER_CAMPUS_NAME: return MESSAGES.colBannerCampusName();
		case VISIBLE: return MESSAGES.colBannerCampusVisibleOnBannerOfferingPage();
		case FIRST_BANNER_TERM: return MESSAGES.colFirstBannerTermCode();
		case LAST_BANNER_TERM: return MESSAGES.colLastBannerTermCode();
		case USED_DEFAULT_CALC: return MESSAGES.colUsedCampusCodeCalc();
		case REPLACE_CAMPUS_CODE: return MESSAGES.colOverrideCalcCampusCode();
		case ACAD_INIT_REGEX: return MESSAGES.colRegexAcademicInitiative();
		case MNG_DEPT_CODE_REGEX: return MESSAGES.colRegexManagingDeptCode();
		case CAMPUS_CODE_REGEX: return MESSAGES.colRegexCampusCodeToOverride();
		
		default: return column.name();
		}
	}
	
	public String getUser() { return UniTimePageHeader.getInstance().getMiddle().getText(); }
	
	public Widget getColumnWidget(BannerCampusOverridesColumn column, BannerCampusOverrideInterface bannerCampusOverride) {;
		switch (column) {
		case BANNER_CAMPUS_CODE:
			return new Label(bannerCampusOverride.getBannerCampusCode() == null ? "" : bannerCampusOverride.getBannerCampusCode());
		case BANNER_CAMPUS_NAME:
			return new Label(bannerCampusOverride.getBannerCampusName() == null ? "" : bannerCampusOverride.getBannerCampusName());
		case VISIBLE:
			P visibleWidget = new P("visible");
			if(bannerCampusOverride.isVisible()){
				visibleWidget.addStyleName("department-accept");
			}
			return visibleWidget;
		case FIRST_BANNER_TERM:
			return new Label(bannerCampusOverride.getFirstBannerTerm() == null ? "" : bannerCampusOverride.getFirstBannerTerm());
		case LAST_BANNER_TERM:
			return new Label(bannerCampusOverride.getLastBannerTerm() == null ? "" : bannerCampusOverride.getLastBannerTerm());
		case USED_DEFAULT_CALC:
			P usedDefaultCalcWidget = new P("usedDefaultCalc");
			if(bannerCampusOverride.isUsedDefaultCalc()){
				usedDefaultCalcWidget.addStyleName("department-accept");
			}
			return usedDefaultCalcWidget;
		case REPLACE_CAMPUS_CODE: 
			P replaceCampusCodeWidget = new P("replaceCampusCode");
			if(bannerCampusOverride.isUsedDefaultCalc()){
				replaceCampusCodeWidget.addStyleName("department-accept");
			}
			return replaceCampusCodeWidget;
		case ACAD_INIT_REGEX:
			return new Label(bannerCampusOverride.getAcademicInitiativeRegex() == null ? "" : bannerCampusOverride.getAcademicInitiativeRegex());
		case MNG_DEPT_CODE_REGEX:
			return new Label(bannerCampusOverride.getManagingDeptCodeRegex() == null ? "" : bannerCampusOverride.getManagingDeptCodeRegex());
		case CAMPUS_CODE_REGEX: 
			return new Label(bannerCampusOverride.getCampusCodeRegex() == null ? "" : bannerCampusOverride.getCampusCodeRegex());

		default:
			return null;
		}
	}
	
	protected void addRow(BannerCampusOverrideInterface bannerCampusOverride) {
		List<Widget> line = new ArrayList<Widget>();
		for (BannerCampusOverridesColumn col: BannerCampusOverridesColumn.values()){
			if(getColumnWidget(col, bannerCampusOverride) != null){
				line.add(getColumnWidget(col, bannerCampusOverride));
			}
			
		}
		addRow(bannerCampusOverride, line);
	}

	public void setHeaderData () {
		clearTable();
		List<UniTimeTableHeader> header = new ArrayList<UniTimeTableHeader>();
		for (final BannerCampusOverridesColumn col: BannerCampusOverridesColumn.values()) {
						
			if (BannerCampusOverrideComparator.isApplicable(col)){
				final UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(col));
//				Operation op = new SortOperation() {
//					@Override
//					public void execute() { doSort(col); }
//					@Override
//					public boolean isApplicable() { return getRowCount() > 1 && h.isVisible(); }
//					@Override
//					public boolean hasSeparator() { return false; }
//					@Override
//					public String getName() { return MESSAGES.opSortBy(getColumnName()); }
//					@Override
//					public String getColumnName() { return h.getHTML().replace("<br>", " "); }
//				};
			
//				h.addOperation(op);
				header.add(h);
			} else {
				final UniTimeTableHeader h = new UniTimeTableHeader(getColumnName(col));				
				header.add(h);
			}
			
		}
		addRow(null, header);
		
		for (int i = 0; i < getCellCount(0); i++)
			getCellFormatter().setStyleName(0, i, "unitime-ClickableTableHeader");
		
		
		if (iSelectable) {
			setAllowSelection(true);
			addMouseClickListener(new MouseClickListener<BannerCampusOverrideInterface>() {
				@Override
				public void onMouseClick(TableEvent<BannerCampusOverrideInterface> event) {
					selectDept(event.getRow(), isSelected(event.getRow()));
				}
			});
		}
		
//		setSortBy(AdminCookie.getInstance().getSortDepartmentsBy());
	}
	
	public void setData(List<BannerCampusOverrideInterface> bannerCampusOverrides) {
		clearTable(1);
		if (bannerCampusOverrides != null)
			for (BannerCampusOverrideInterface bannerCampusOverride: bannerCampusOverrides)
	            addRow(bannerCampusOverride);
		sort();
	}
	protected void doSort(BannerCampusOverridesColumn column) {
		if (column == iSortBy) {
			iAsc = !iAsc;
		} else {
			iSortBy = column;
			iAsc = true;
		}
//		AdminCookie.getInstance().setSortDepartmentsBy(getSortBy());		
		sort();
	}
	
	public boolean hasSortBy() { return iSortBy != null; }
	public int getSortBy() { return iSortBy == null ? 0 : iAsc ? 1 + iSortBy.ordinal() : -1 - iSortBy.ordinal(); }
	public void setSortBy(int sortBy) {
		if (sortBy == 0) {
			iSortBy = null;
			iAsc = true;
		} else if (sortBy > 0) {
			iSortBy = BannerCampusOverridesColumn.values()[sortBy - 1];
			iAsc = true;
		} else {
			iSortBy = BannerCampusOverridesColumn.values()[-1 - sortBy];
			iAsc = false;
		}
		sort();
	}
	
	public void sort() {
		if (iSortBy == null) return;
		UniTimeTableHeader header = getHeader(iSortBy.ordinal());
		sort(header, new BannerCampusOverrideComparator(iSortBy, true), iAsc);
	}
	public static interface SortOperation extends Operation, HasColumnName {}
	
	public static class BannerCampusOverrideComparator implements Comparator<BannerCampusOverrideInterface>{
		private BannerCampusOverridesColumn iColumn;
		private boolean iAsc;
		
		public BannerCampusOverrideComparator(BannerCampusOverridesColumn column, boolean asc) {
			iColumn = column;
			iAsc = asc;
		}
		
		public int compareById(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.getId(), r2.getId());
		}
			
		public int compareByBannerCampusCode(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.getBannerCampusCode(), r2.getBannerCampusCode());
		}
		public int compareByBannerCampusName(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.getBannerCampusName(), r2.getBannerCampusName());
		}
		public int compareByVisible(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.isVisible(), r2.isVisible());
		}
		public int compareByFirstBannerTerm(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.getFirstBannerTerm(), r2.getFirstBannerTerm());
		}
		public int compareByLastBannerTerm(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.getLastBannerTerm(), r2.getLastBannerTerm());
		}
		public int compareByUsedDefaultCalc(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.isUsedDefaultCalc(), r2.isUsedDefaultCalc());
		}	
		public int compareByReplaceCampusCode(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.isReplaceCampusCode(), r2.isReplaceCampusCode());
		}	
		public int compareByAcademicInitiativeRegex(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.getAcademicInitiativeRegex(), r2.getAcademicInitiativeRegex());
		}	
		public int compareByManagingDeptCodeRegex(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.getManagingDeptCodeRegex(), r2.getManagingDeptCodeRegex());
		}
		public int compareByCampusCodeRegex(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			return compare(r1.getCampusCodeRegex(), r2.getCampusCodeRegex());
		}		
		protected int compareByColumn(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			switch (iColumn) {	
			case BANNER_CAMPUS_CODE: return compareByBannerCampusCode(r1, r2);
			case BANNER_CAMPUS_NAME: return compareByBannerCampusName(r1, r2);
			case VISIBLE: return compareByVisible(r1, r2);
			case FIRST_BANNER_TERM: return compareByFirstBannerTerm(r1, r2);
			case LAST_BANNER_TERM: return compareByLastBannerTerm(r1, r2);
			case USED_DEFAULT_CALC: return compareByUsedDefaultCalc(r1, r2);
			case REPLACE_CAMPUS_CODE: return compareByReplaceCampusCode(r1, r2);
			case ACAD_INIT_REGEX: return compareByAcademicInitiativeRegex(r1, r2);
			case MNG_DEPT_CODE_REGEX: return compareByManagingDeptCodeRegex(r1, r2);
			case CAMPUS_CODE_REGEX: return compareByCampusCodeRegex(r1, r2);
						
			default: return compareByBannerCampusCode(r1, r2);
			}
		}
		
		public static boolean isApplicable(BannerCampusOverridesColumn column) {
			switch (column) {
			case BANNER_CAMPUS_CODE:
			case BANNER_CAMPUS_NAME:
			case VISIBLE:
			case FIRST_BANNER_TERM:
			case LAST_BANNER_TERM:
			case USED_DEFAULT_CALC:
			case REPLACE_CAMPUS_CODE: 
			case ACAD_INIT_REGEX:
			case MNG_DEPT_CODE_REGEX:
			case CAMPUS_CODE_REGEX: 
				return true;
			default:
				return false;
			}
		}
		
		@Override
		public int compare(BannerCampusOverrideInterface r1, BannerCampusOverrideInterface r2) {
			int cmp = compareByColumn(r1, r2);
			if (cmp == 0) cmp = compareByBannerCampusCode(r1, r2);
			if (cmp == 0) cmp = compareById(r1, r2);
			return (iAsc ? cmp : -cmp);
		}
		
		protected int compare(String s1, String s2) {
			if (s1 == null || s1.isEmpty()) {
				return (s2 == null || s2.isEmpty() ? 0 : 1);
			} else {
				return (s2 == null || s2.isEmpty() ? -1 : s1.compareToIgnoreCase(s2));
			}
		}
		
		protected int compare(Number n1, Number n2) {
			return (n1 == null ? n2 == null ? 0 : -1 : n2 == null ? 1 : Double.compare(n1.doubleValue(), n2.doubleValue())); 
		}
		
		protected int compare(Boolean b1, Boolean b2) {
			return - Boolean.compare(b1 != null && b1.booleanValue(), b2 != null && b2.booleanValue()); 
		}
	}


}
