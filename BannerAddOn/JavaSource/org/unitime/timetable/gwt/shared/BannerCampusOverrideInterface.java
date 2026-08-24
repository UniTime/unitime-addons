package org.unitime.timetable.gwt.shared;

import java.util.ArrayList;
import java.util.List;

import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcResponse;

import com.google.gwt.user.client.rpc.IsSerializable;

public class BannerCampusOverrideInterface
		implements IsSerializable, Comparable<BannerCampusOverrideInterface>, GwtRpcResponse {

	private  Long iUniqueId = null;
	private String iBannerCampusCode = null;
	private String iBannerCampusName = null;
	private String iFirstBannerTerm = null;
	private String iLastBannerTerm = null;
	private String iAcademicInitiativeRegex = null;
	private String iManagingDeptCodeRegex = null;
	private String iCampusCodeRegex = null;
	private Boolean iVisible = false;
	private Boolean iUsedDefaultCalc = false;
	private Boolean iReplaceCampusCode = false;

	public BannerCampusOverrideInterface() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(BannerCampusOverrideInterface o) {
		// TODO Auto-generated method stub
		return 0;
	}

	/*
	 * List all existing banner campus overrides
	 */
	public static class GetBannerCampusOverridesRequest implements GwtRpcRequest<BannerCampusOverridesDataResponse> {		
	}
	
	public static class BannerCampusOverridesDataResponse implements GwtRpcResponse {
		private List<BannerCampusOverrideInterface> iBannerCampusOverrides;
		private boolean iCanAdd;

		public boolean isCanAdd() { return iCanAdd; }
		public void setCanAdd(boolean canAdd) { iCanAdd = canAdd; }
		
		public void addBannerCampusOverrides(BannerCampusOverrideInterface bannerCampusOverrides) {
			if (iBannerCampusOverrides == null) iBannerCampusOverrides = new ArrayList<BannerCampusOverrideInterface>();
			iBannerCampusOverrides.add(bannerCampusOverrides);
		}
	
		public BannerCampusOverridesDataResponse() {}						
		
		public List<BannerCampusOverrideInterface> getBannerCampusOverrides() { return iBannerCampusOverrides; }
		
		public boolean hasBannerCampusOverrides() { return iBannerCampusOverrides != null && !iBannerCampusOverrides.isEmpty(); }
		
	}
	
	public static class BannerCampusOverridePropertiesInterface  implements GwtRpcResponse {
		
		private Boolean iCanEdit = false;
		private Boolean iCanDelete = false;
			
		public BannerCampusOverridePropertiesInterface() {}
				
		public boolean getCanEdit() { return iCanEdit; }
		public void setCanEdit (boolean canEdit) { iCanEdit = canEdit; }

		public boolean getCanDelete() { return iCanDelete; }
		public void setCanDelete(boolean canDelete) { iCanDelete = canDelete; }


	} //end BannerCampusOverridesPropertiesInterface
	
	public static enum BannerCampusOverridesColumn {
		BANNER_CAMPUS_CODE, BANNER_CAMPUS_NAME, VISIBLE, FIRST_BANNER_TERM, LAST_BANNER_TERM, USED_DEFAULT_CALC, REPLACE_CAMPUS_CODE, ACAD_INIT_REGEX, MNG_DEPT_CODE_REGEX, CAMPUS_CODE_REGEX,

	}	
	
	public static enum UpdateBannerCampusOverrideAction {
		CREATE, UPDATE, DELETE,
	}
	/*
	 * Look Up properties for Department
	 */
	public static class BannerCampusOverridePropertiesRequest implements GwtRpcRequest<BannerCampusOverridePropertiesInterface> {
		private Long iBannerCampusOverrideId;

		protected BannerCampusOverridePropertiesRequest() {
		}

		public BannerCampusOverridePropertiesRequest(Long bannerCampusOverrideId) {
			setBannerCampusOverrideId(bannerCampusOverrideId);
		}

		public Long getBannerCampusOverrideId() { return iBannerCampusOverrideId; }
		public void setBannerCampusOverrideId(Long bannerCampusOverrideId) { this.iBannerCampusOverrideId = bannerCampusOverrideId; }
		
		
	}//end DepartmentPropertiesRequest

	public Long getId() { return iUniqueId; }
	public void setId(Long uniqueId) { iUniqueId = uniqueId; }

	public String getBannerCampusCode() {
		return iBannerCampusCode;
	}

	public void setBannerCampusCode(String iBannerCampusCode) {
		this.iBannerCampusCode = iBannerCampusCode;
	}

	public String getBannerCampusName() {
		return iBannerCampusName;
	}

	public void setBannerCampusName(String iBannerCampusName) {
		this.iBannerCampusName = iBannerCampusName;
	}

	public String getFirstBannerTerm() {
		return iFirstBannerTerm;
	}

	public void setFirstBannerTerm(String iFirstBannerTerm) {
		this.iFirstBannerTerm = iFirstBannerTerm;
	}

	public String getLastBannerTerm() {
		return iLastBannerTerm;
	}

	public void setLastBannerTerm(String iLastBannerTerm) {
		this.iLastBannerTerm = iLastBannerTerm;
	}

	public String getAcademicInitiativeRegex() {
		return iAcademicInitiativeRegex;
	}

	public void setAcademicInitiativeRegex(String iAcademicInitiativeRegex) {
		this.iAcademicInitiativeRegex = iAcademicInitiativeRegex;
	}

	public String getManagingDeptCodeRegex() {
		return iManagingDeptCodeRegex;
	}

	public void setManagingDeptCodeRegex(String iManagingDeptCodeRegex) {
		this.iManagingDeptCodeRegex = iManagingDeptCodeRegex;
	}

	public String getCampusCodeRegex() {
		return iCampusCodeRegex;
	}

	public void setCampusCodeRegex(String iCampusCodeRegex) {
		this.iCampusCodeRegex = iCampusCodeRegex;
	}

	public Boolean isVisible() {
		return iVisible;
	}

	public Boolean getVisible() {
		return iVisible;
	}

	public void setVisible(Boolean iVisible) {
		this.iVisible = iVisible;
	}

	public Boolean isUsedDefaultCalc() {
		return iUsedDefaultCalc;
	}

	public Boolean getUsedDefaultCalc() {
		return iUsedDefaultCalc;
	}

	public void setUsedDefaultCalc(Boolean iUsedDefaultCalc) {
		this.iUsedDefaultCalc = iUsedDefaultCalc;
	}

	public Boolean getReplaceCampusCode() {
		return iReplaceCampusCode;
	}

	public Boolean isReplaceCampusCode() {
		return iReplaceCampusCode;
	}

	public void setReplaceCampusCode(Boolean iReplaceCampusCode) {
		this.iReplaceCampusCode = iReplaceCampusCode;
	}

	
}
