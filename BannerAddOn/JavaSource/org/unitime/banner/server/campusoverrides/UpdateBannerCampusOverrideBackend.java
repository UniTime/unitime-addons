package org.unitime.banner.server.campusoverrides;


import org.hibernate.Transaction;
import org.unitime.banner.model.BannerCampusOverride;
import org.unitime.banner.model.dao.BannerCampusOverrideDAO;
import org.unitime.commons.hibernate.util.HibernateUtil;
import org.unitime.timetable.gwt.client.campusoverrides.BannerCampusOverridesEdit.UpdateBannerCampusOverrideRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcException;
import org.unitime.timetable.gwt.command.server.GwtRpcImplementation;
import org.unitime.timetable.gwt.command.server.GwtRpcImplements;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface;
import org.unitime.timetable.gwt.shared.PageAccessException;
import org.unitime.timetable.security.SessionContext;
import org.unitime.timetable.security.rights.Right;

@GwtRpcImplements(UpdateBannerCampusOverrideRequest.class)
public class UpdateBannerCampusOverrideBackend
		implements GwtRpcImplementation<UpdateBannerCampusOverrideRequest, BannerCampusOverrideInterface> {

	public UpdateBannerCampusOverrideBackend() {	}

	@Override
	public BannerCampusOverrideInterface execute(UpdateBannerCampusOverrideRequest request, SessionContext context) {
		org.hibernate.Session hibSession = BannerCampusOverrideDAO.getInstance().getSession();
		BannerCampusOverride bannerCampusOverride = null;
		switch (request.getAction()) {
		case CREATE:			
			context.checkPermission(Right.AcademicSessionAdd);
			bannerCampusOverride = saveOrUpdate(request.getBannerCampusOverride(), context);               
//            ChangeLog.addChange(
//                    hibSession, 
//                    context, 
//                    department, 
//                    ChangeLog.Source.DEPARTMENT_EDIT, 
//                    ChangeLog.Operation.CREATE , 
//                    null, 
//                    bannerCampusOverride);        	
			break;
		case UPDATE:
			context.checkPermission(request.getBannerCampusOverride().getId(), "Department", Right.AcademicSessionEdit);
			bannerCampusOverride = saveOrUpdate(request.getBannerCampusOverride(), context);
//			TODO: set up change log for this
//            ChangeLog.addChange(
//                    hibSession, 
//                    context, 
//                    department, 
//                    ChangeLog.Source.DEPARTMENT_EDIT, 
//                    ChangeLog.Operation.UPDATE, 
//                    null, 
//                    department);  
			break;
		case DELETE:
			context.checkPermission(request.getBannerCampusOverride().getId(), "Department", Right.AcademicSessionDelete);
			bannerCampusOverride = BannerCampusOverrideDAO.getInstance().get( request.getBannerCampusOverride().getId(), hibSession);
//			TODO: set up change log for this
//            ChangeLog.addChange(
//                    hibSession, 
//                    context, 
//                    department, 
//                    ChangeLog.Source.DEPARTMENT_EDIT, 
//                    ChangeLog.Operation.DELETE, 
//                    null, 
//                    null);
			delete(request.getBannerCampusOverride(), context);
			break;
		}
		hibSession.flush();
		return request.getBannerCampusOverride();
	}

	private void delete(BannerCampusOverrideInterface bannerCampusOverrideInterface, SessionContext context) {

		context.checkPermission(bannerCampusOverrideInterface.getId(), "BannerCampusOverride", Right.AcademicSessionDelete);
		org.hibernate.Session hibSession = BannerCampusOverrideDAO.getInstance().getSession();
		Transaction tx = null;
        try { 
        	tx = hibSession.beginTransaction();
        	BannerCampusOverride bannerCampusOverride = BannerCampusOverrideDAO.getInstance().get(bannerCampusOverrideInterface.getId(), hibSession);
        	
            hibSession.remove(bannerCampusOverride);
			tx.commit();
			HibernateUtil.clearCache();     	
	    } catch (Exception e) {
	    	if (tx!=null && tx.isActive()) tx.rollback();
	    	throw new GwtRpcException(e.getMessage(), e);
	    }
	
		
	}

	private BannerCampusOverride saveOrUpdate(BannerCampusOverrideInterface bannerCampusOverrideInterface,
			SessionContext context) throws GwtRpcException {

		BannerCampusOverride bannerCampusOverride = null;
		try {
		org.hibernate.Session hibSession = BannerCampusOverrideDAO.getInstance().getSession();
		
        try {       	
        	        
            if (bannerCampusOverrideInterface.getId() != null) {
            	bannerCampusOverride = BannerCampusOverrideDAO.getInstance().get(bannerCampusOverrideInterface.getId(), hibSession);
            }
            if (bannerCampusOverride==null) {
            	bannerCampusOverride = new BannerCampusOverride();
            }
                       
            bannerCampusOverride.setBannerCampusCode(bannerCampusOverrideInterface.getBannerCampusCode());
            bannerCampusOverride.setBannerCampusName(bannerCampusOverrideInterface.getBannerCampusName());
            bannerCampusOverride.setFirstBannerTerm(bannerCampusOverrideInterface.getFirstBannerTerm());
            bannerCampusOverride.setLastBannerTerm(bannerCampusOverrideInterface.getLastBannerTerm()); 
            bannerCampusOverride.setVisible(bannerCampusOverrideInterface.getVisible());
            bannerCampusOverride.setUsedDefaultCalc(bannerCampusOverrideInterface.getUsedDefaultCalc());
            bannerCampusOverride.setReplaceCampusCode(bannerCampusOverrideInterface.getReplaceCampusCode());
            bannerCampusOverride.setAcademicInitiativeRegex(bannerCampusOverrideInterface.getAcademicInitiativeRegex());          
            bannerCampusOverride.setManagingDeptCodeRegex(bannerCampusOverrideInterface.getManagingDeptCodeRegex());  
            bannerCampusOverride.setCampusCodeRegex(bannerCampusOverrideInterface.getCampusCodeRegex());            
         
           
            if (bannerCampusOverride.getUniqueId() == null) {
            	hibSession.persist(bannerCampusOverride);
            } else {
            	hibSession.merge(bannerCampusOverride);
            }
		
	    } finally {
	    	hibSession.flush();
		}
        bannerCampusOverrideInterface.setId(bannerCampusOverride.getUniqueId());
	} catch (PageAccessException e) {
		throw e;
	} catch (Exception e) {
		throw new GwtRpcException(e.getMessage(), e);
	}
        return bannerCampusOverride;
		}

}
