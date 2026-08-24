package org.unitime.banner.server.campusoverrides;

import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.BannerCampusOverridesDataResponse;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.GetBannerCampusOverridesRequest;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(GetBannerCampusOverridesRequest.class)
public class GetBannerCampusOverridesDataBackend
		implements GwtRpcImplementation<GetBannerCampusOverridesRequest, BannerCampusOverridesDataResponse> {

	public GetBannerCampusOverridesDataBackend() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public BannerCampusOverridesDataResponse execute(GetBannerCampusOverridesRequest request, SessionContext context) {
		context.checkPermission(Right.Departments);		
		BannerCampusOverridesDataResponse response = new BannerCampusOverridesDataResponse();
		/*permission */
		response.setCanAdd(context.hasPermission(Right.AcademicSessionAdd));
 		
		/*department list */
		for (BannerCampusOverride bannerCampusOverride: BannerCampusOverride.getAllBannerCampusOverrides()) {
			BannerCampusOverrideInterface bco = new BannerCampusOverrideInterface();
			bco.setId(bannerCampusOverride.getUniqueId());
			bco.setBannerCampusCode(bannerCampusOverride.getBannerCampusCode() == null? "":bannerCampusOverride.getBannerCampusCode());
			bco.setBannerCampusName(bannerCampusOverride.getBannerCampusName() == null? "":bannerCampusOverride.getBannerCampusName());
			bco.setFirstBannerTerm(bannerCampusOverride.getFirstBannerTerm() == null? "":bannerCampusOverride.getFirstBannerTerm());
			bco.setLastBannerTerm(bannerCampusOverride.getLastBannerTerm() == null? "":bannerCampusOverride.getLastBannerTerm());
			bco.setVisible(bannerCampusOverride.isVisible() == null? false:bannerCampusOverride.isVisible().booleanValue());
			bco.setUsedDefaultCalc(bannerCampusOverride.isUsedDefaultCalc() == null? false:bannerCampusOverride.isUsedDefaultCalc().booleanValue());
			bco.setReplaceCampusCode(bannerCampusOverride.isReplaceCampusCode() == null? false:bannerCampusOverride.isReplaceCampusCode().booleanValue());
			bco.setAcademicInitiativeRegex(bannerCampusOverride.getAcademicInitiativeRegex() == null? "":bannerCampusOverride.getAcademicInitiativeRegex());
			bco.setCampusCodeRegex(bannerCampusOverride.getCampusCodeRegex() == null? "":bannerCampusOverride.getCampusCodeRegex());
			bco.setManagingDeptCodeRegex(bannerCampusOverride.getManagingDeptCodeRegex() == null? "":bannerCampusOverride.getManagingDeptCodeRegex());
			response.addBannerCampusOverrides(bco);
		}

		return response;
	}

}
