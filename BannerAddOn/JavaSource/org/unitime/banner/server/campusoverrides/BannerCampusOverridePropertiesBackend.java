package org.unitime.banner.server.campusoverrides;

import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.dao.BannerCampusOverrideDAO;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.BannerCampusOverridePropertiesInterface;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.BannerCampusOverridePropertiesRequest;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(BannerCampusOverridePropertiesRequest.class)
public class BannerCampusOverridePropertiesBackend implements
		GwtRpcImplementation<BannerCampusOverridePropertiesRequest, BannerCampusOverridePropertiesInterface> {

	public BannerCampusOverridePropertiesBackend() {
		// TODO Auto-generated constructor stub
	}

	@Override
	public BannerCampusOverridePropertiesInterface execute(BannerCampusOverridePropertiesRequest request,
			SessionContext context) {
		BannerCampusOverridePropertiesInterface d = new BannerCampusOverridePropertiesInterface();	
		BannerCampusOverride bannerCampusOverride = null;
		if (request.getBannerCampusOverrideId() != null) {
			bannerCampusOverride = BannerCampusOverrideDAO.getInstance().get(request.getBannerCampusOverrideId());
		}
				
		if (bannerCampusOverride != null) {
			d.setCanDelete(context.hasPermission(bannerCampusOverride, Right.AcademicSessionAdd));
			d.setCanEdit(context.hasPermission(bannerCampusOverride, Right.AcademicSessionEdit));
		} else {
			d.setCanDelete(false);
			d.setCanEdit(true);			
		}
		
		return d;
	}

}
