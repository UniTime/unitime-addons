package org.unitime.timetable.gwt.client.campusoverrides;

import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeConfirmationDialog;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeWidget;
import org.unitime.timetable.gwt.command.client.GwtRpcRequest;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface;
import org.unitime.timetable.gwt.shared.DepartmentInterface;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.BannerCampusOverridePropertiesInterface;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.UpdateBannerCampusOverrideAction;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

public class BannerCampusOverridesEdit extends Composite implements TakesValue<BannerCampusOverrideInterface> {

	protected static final BannerGwtMessages MESSAGES = GWT.create(BannerGwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);
	
	private SimpleForm iForm;
	private UniTimeHeaderPanel iHeader, iFooter;
	private UniTimeWidget<TextBox> iBannerCampusCode;
	private UniTimeWidget<TextBox> iBannerCampusName;
	private UniTimeWidget<TextBox> iFirstBannerTerm;
	private UniTimeWidget<TextBox> iLastBannerTerm;
	private UniTimeWidget<TextBox>  iAcademicInitiativeRegex ;
	private UniTimeWidget<TextBox>   iManagingDeptCodeRegex ;
	private UniTimeWidget<TextBox> iCampusCodeRegex;
	private UniTimeWidget<CheckBox> iVisible;
	private UniTimeWidget<CheckBox> iUsedDefaultCalc;
	private UniTimeWidget<CheckBox> iReplaceCampusCode;
	private UniTimeHeaderPanel controlDeptHeaderPanel;
	private VerticalPanel iControlDeptMainPanel ;
	private FlexTable iControlDeptFlexTable;	  
	private BannerCampusOverrideInterface iBannerCampusOverride = null;
	private BannerCampusOverridePropertiesInterface iBannerCampusOverrideProperties;

	public BannerCampusOverrideInterface getiBannerCampusOverride() { return iBannerCampusOverride; }
	public void setBannerCampusOverride(BannerCampusOverrideInterface bannerCampusOverride) { iBannerCampusOverride = bannerCampusOverride; }

	
	public void setProperties(BannerCampusOverridePropertiesInterface bannerCampusOverrideProperties) {
		iBannerCampusOverrideProperties = bannerCampusOverrideProperties;
	}

	
	public BannerCampusOverridesEdit() {

		/*create the UI */
		iForm = new SimpleForm();
		iForm.addStyleName("unitime-Dept");
		
		iHeader = new UniTimeHeaderPanel();
	
		iHeader.addButton("save", MESSAGES.buttonSave(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!validate()) return;
				UpdateBannerCampusOverrideRequest request = new UpdateBannerCampusOverrideRequest();
				request.setAction(UpdateBannerCampusOverrideAction.CREATE);
				request.setBannerCampusOverride(getValue());
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				RPC.execute(request, new AsyncCallback<BannerCampusOverrideInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.failedCreate(MESSAGES.objectBannerCampusOverride(), caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedCreate(MESSAGES.objectBannerCampusOverride(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(BannerCampusOverrideInterface result) {
						LoadingWidget.getInstance().hide();
						onBack(true, result.getId());
					}
				});
			}
		});
		iHeader.addButton("update", MESSAGES.buttonUpdate(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				if (!validate()) return;
				UpdateBannerCampusOverrideRequest request = new UpdateBannerCampusOverrideRequest();
				request.setAction(UpdateBannerCampusOverrideAction.UPDATE);
				request.setBannerCampusOverride(getValue());
				LoadingWidget.getInstance().show(MESSAGES.waitPlease());
				RPC.execute(request, new AsyncCallback<BannerCampusOverrideInterface>() {
					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						iHeader.setErrorMessage(MESSAGES.failedUpdate(MESSAGES.objectBannerCampusOverride(), caught.getMessage()));
						UniTimeNotifications.error(MESSAGES.failedUpdate(MESSAGES.objectBannerCampusOverride(), caught.getMessage()), caught);
					}
					@Override
					public void onSuccess(BannerCampusOverrideInterface result) {
						LoadingWidget.getInstance().hide();
						onBack(true, result.getId());
					}
				});
			}
		});
		iHeader.addButton("delete", MESSAGES.buttonDelete(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				UniTimeConfirmationDialog.confirm(MESSAGES.confirmDepartmentDelete(), new Command() {
					@Override
					public void execute() {
						UpdateBannerCampusOverrideRequest request = new UpdateBannerCampusOverrideRequest();
						request.setAction(UpdateBannerCampusOverrideAction.DELETE);
						request.setBannerCampusOverride(getValue());
						LoadingWidget.getInstance().show(MESSAGES.waitPlease());
						RPC.execute(request, new AsyncCallback<BannerCampusOverrideInterface>() {
							@Override
							public void onFailure(Throwable caught) {
								LoadingWidget.getInstance().hide();
								iHeader.setErrorMessage(MESSAGES.failedDelete(MESSAGES.objectBannerCampusOverride(), caught.getMessage()));
								UniTimeNotifications.error(MESSAGES.failedDelete(MESSAGES.objectBannerCampusOverride(), caught.getMessage()), caught);
							}
							@Override
							public void onSuccess(BannerCampusOverrideInterface result) {
								LoadingWidget.getInstance().hide();
								onBack(true, null);
							}
						});
					}
				});
			}
		});
		iHeader.addButton("back", MESSAGES.buttonBack(), new ClickHandler() {
			@Override
			public void onClick(ClickEvent event) {
				onBack(false, iBannerCampusOverride.getId());
			}
		});
		iForm.addHeaderRow(iHeader);
				
		//BannerCampusCode
		iBannerCampusCode = new UniTimeWidget<TextBox>(new TextBox());
		iBannerCampusCode.getWidget().setStyleName("unitime-TextBox");
		iBannerCampusCode.getWidget().setMaxLength(20);
		iBannerCampusCode.getWidget().setWidth("100px");
		iBannerCampusCode.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iBannerCampusCode.clearHint();
				iHeader.clearMessage();
			}
		});		
		iForm.addRow(MESSAGES.colBannerCampusCode(), iBannerCampusCode);
		
		
		//BannerCampusName		
		iBannerCampusName = new UniTimeWidget<TextBox>(new TextBox());
		iBannerCampusName.getWidget().setStyleName("unitime-TextBox");
		iBannerCampusName.getWidget().setMaxLength(100);
		iBannerCampusName.getWidget().setWidth("300px");
		iBannerCampusName.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iBannerCampusName.clearHint();
				iHeader.clearMessage();
			}
		});		
		iForm.addRow(MESSAGES.colBannerCampusName(), iBannerCampusName);
		
		//Visible
		iVisible = new UniTimeWidget<CheckBox>(new CheckBox());
		iVisible.getWidget().setValue(false);		
		iForm.addRow(MESSAGES.colBannerCampusVisibleOnBannerOfferingPage(), iVisible);

		//FirstBannerTerm		
		iFirstBannerTerm	 = new UniTimeWidget<TextBox>(new TextBox());
		iFirstBannerTerm.getWidget().setStyleName("unitime-TextBox");
		iFirstBannerTerm.getWidget().setMaxLength(6);
		iFirstBannerTerm.getWidget().setWidth("50px");
		iFirstBannerTerm.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iFirstBannerTerm.clearHint();
				iHeader.clearMessage();
			}
		});	
		iForm.addRow(MESSAGES.fieldFirstBannerTermCode(), iFirstBannerTerm);

		//LastBannerTerm		
		iLastBannerTerm  = new UniTimeWidget<TextBox>(new TextBox());
		iLastBannerTerm.getWidget().setStyleName("unitime-TextBox");
		iLastBannerTerm.getWidget().setMaxLength(6);
		iLastBannerTerm.getWidget().setWidth("50px");
		iLastBannerTerm.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iLastBannerTerm.clearHint();
				iHeader.clearMessage();
			}
		});	
		iForm.addRow(MESSAGES.fieldLastBannerTermCode(), iLastBannerTerm);

		//UsedDefaultCalc
		iUsedDefaultCalc = new UniTimeWidget<CheckBox>(new CheckBox());
		iUsedDefaultCalc.getWidget().setValue(false);	
		iForm.addRow(MESSAGES.fieldUsedCampusCodeCalc(), iUsedDefaultCalc);

		//ReplaceCampusCode
		iReplaceCampusCode = new UniTimeWidget<CheckBox>(new CheckBox());
		iReplaceCampusCode.getWidget().setValue(false);
		iForm.addRow(MESSAGES.fieldOverrideCalcCampusCode(), iReplaceCampusCode);
						
		//ManagingDeptCodeRegex		
		iManagingDeptCodeRegex = new UniTimeWidget<TextBox>(new TextBox());
		iManagingDeptCodeRegex.getWidget().setStyleName("unitime-TextBox");
		iManagingDeptCodeRegex.getWidget().setMaxLength(100);
		iManagingDeptCodeRegex.getWidget().setWidth("200px");
		iManagingDeptCodeRegex.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iManagingDeptCodeRegex.clearHint();
				iHeader.clearMessage();
			}
		});		
		iForm.addRow(MESSAGES.fieldRegexManagingDeptCode(), iManagingDeptCodeRegex);
		
		//AcademicInitiativeRegex		
		iAcademicInitiativeRegex = new UniTimeWidget<TextBox>(new TextBox());
		iAcademicInitiativeRegex.getWidget().setStyleName("unitime-TextBox");
		iAcademicInitiativeRegex.getWidget().setMaxLength(100);
		iAcademicInitiativeRegex.getWidget().setWidth("200px");
		iAcademicInitiativeRegex.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iAcademicInitiativeRegex.clearHint();
				iHeader.clearMessage();
			}
		});			
		iForm.addRow(MESSAGES.fieldRegexAcademicInitiative(), iAcademicInitiativeRegex);
		
		//AcademicInitiativeRegex		
		iCampusCodeRegex = new UniTimeWidget<TextBox>(new TextBox());
		iCampusCodeRegex.getWidget().setStyleName("unitime-TextBox");
		iCampusCodeRegex.getWidget().setMaxLength(100);
		iCampusCodeRegex.getWidget().setWidth("200px");
		iCampusCodeRegex.getWidget().addChangeHandler(new ChangeHandler() {
			@Override
			public void onChange(ChangeEvent event) {
				iCampusCodeRegex.clearHint();
				iHeader.clearMessage();
			}
		});			
		iForm.addRow(MESSAGES.fieldRegexCampusCodeToOverride(), iCampusCodeRegex);
		

		iForm.addHeaderRow(controlDeptHeaderPanel);		

		iControlDeptMainPanel = new VerticalPanel();
		iControlDeptFlexTable = new FlexTable();
		iControlDeptMainPanel.add(iControlDeptFlexTable); 
		
	    iForm.addRow(iControlDeptMainPanel);  
	    
	    iControlDeptFlexTable.getElement().getStyle().setPaddingBottom(20, Unit.PX);
		
		iFooter = iHeader.clonePanel();
		iForm.addBottomRow(iFooter);
	  	initWidget(iForm);
	   		
		
		// TODO Auto-generated constructor stub
	}

	@Override
	public BannerCampusOverrideInterface getValue() {
		iBannerCampusOverride.setBannerCampusCode(iBannerCampusCode.getWidget().getText());
		iBannerCampusOverride.setBannerCampusName(iBannerCampusName.getWidget().getText());
		iBannerCampusOverride.setFirstBannerTerm(iFirstBannerTerm.getWidget().getText());
		iBannerCampusOverride.setLastBannerTerm(iLastBannerTerm.getWidget().getText());
		iBannerCampusOverride.setAcademicInitiativeRegex(iAcademicInitiativeRegex.getWidget().getText());
		iBannerCampusOverride.setCampusCodeRegex(iCampusCodeRegex.getWidget().getText());
		iBannerCampusOverride.setManagingDeptCodeRegex(iManagingDeptCodeRegex.getWidget().getText());
		iBannerCampusOverride.setVisible(iVisible.getWidget().getValue());
		iBannerCampusOverride.setReplaceCampusCode(iReplaceCampusCode.getWidget().getValue());
		iBannerCampusOverride.setUsedDefaultCalc(iUsedDefaultCalc.getWidget().getValue());
		
		
		// TODO Auto-generated method stub
		return iBannerCampusOverride;
	}

	@Override
	public void setValue(BannerCampusOverrideInterface bannerCampusOverrideInterface) {
		// TODO Auto-generated method stub
		iBannerCampusCode.clearHint();
		iBannerCampusName.clearHint();
		iFirstBannerTerm.clearHint();
		iLastBannerTerm.clearHint();
		iAcademicInitiativeRegex.clearHint();
		iManagingDeptCodeRegex.clearHint();
		iCampusCodeRegex.clearHint();
		iVisible.clearHint();
		iUsedDefaultCalc.clearHint();
		iReplaceCampusCode.clearHint();
		iHeader.clearMessage();
		
		iBannerCampusCode.getWidget().setText("");
		iBannerCampusName.getWidget().setText("");
		iFirstBannerTerm.getWidget().setText("");
		iLastBannerTerm.getWidget().setText("");
		iAcademicInitiativeRegex.getWidget().setText("");
		iManagingDeptCodeRegex.getWidget().setText("");
		iCampusCodeRegex.getWidget().setText("");
		iVisible.getWidget().setValue(false);
		iUsedDefaultCalc.getWidget().setValue(false);
		iReplaceCampusCode.getWidget().setValue(false);

		if (bannerCampusOverrideInterface == null) {
			iHeader.setHeaderTitle(MESSAGES.sectAddBannerCampusOverrides());
			iHeader.setEnabled("save", true);
			iHeader.setEnabled("update", false);
			iHeader.setEnabled("delete", false);
			iHeader.setEnabled("back", true);
			iBannerCampusOverride = new BannerCampusOverrideInterface();		
		} else {
			iHeader.setHeaderTitle(MESSAGES.sectEditBannerCampusOverride());
			iHeader.setEnabled("save", false);
			iHeader.setEnabled("update", iBannerCampusOverrideProperties.getCanEdit());
			iHeader.setEnabled("delete", iBannerCampusOverrideProperties.getCanDelete());
			iHeader.setEnabled("back", true); 

			iBannerCampusCode.getWidget().setText(bannerCampusOverrideInterface.getBannerCampusCode());
			iBannerCampusName.getWidget().setText(bannerCampusOverrideInterface.getBannerCampusName());
			iFirstBannerTerm.getWidget().setText(bannerCampusOverrideInterface.getFirstBannerTerm());
			iLastBannerTerm.getWidget().setText(bannerCampusOverrideInterface.getLastBannerTerm());
			iAcademicInitiativeRegex.getWidget().setText(bannerCampusOverrideInterface.getAcademicInitiativeRegex());
			iManagingDeptCodeRegex.getWidget().setText(bannerCampusOverrideInterface.getManagingDeptCodeRegex());
			iCampusCodeRegex.getWidget().setText(bannerCampusOverrideInterface.getCampusCodeRegex());
			iVisible.getWidget().setValue(bannerCampusOverrideInterface.isVisible());
			iUsedDefaultCalc.getWidget().setValue(bannerCampusOverrideInterface.isUsedDefaultCalc());
			iReplaceCampusCode.getWidget().setValue(bannerCampusOverrideInterface.isReplaceCampusCode());
			iBannerCampusOverride = bannerCampusOverrideInterface;
		}
	}
	/*
	 * validate UI
	 */
	protected boolean validate() {
		//TODO need to set up error messages.
		boolean ok = true;
		if (iBannerCampusCode.getWidget().getText().trim().isEmpty() ) {
			iBannerCampusCode.setErrorHint(MESSAGES.errorBannerCampusCodeIsEmpty());
			if (ok) iHeader.setErrorMessage(MESSAGES.errorBannerCampusCodeIsEmpty());
			ok = false;
		}

		if (iBannerCampusName.getWidget().getText().trim().isEmpty()) {
			iBannerCampusName.setErrorHint(MESSAGES.errorBannerCampusNameIsEmpty());
			if (ok) iHeader.setErrorMessage(MESSAGES.errorBannerCampusNameIsEmpty());
			ok = false;
		} 		 

//        if (iLastBannerTerm.getWidget().getText().isEmpty()|| iLastBannerTerm.getWidget().getText().length() ==0) {
//        	iLastBannerTerm.setErrorHint(MESSAGES.errorRequired(MESSAGES.colLastBannerTermCode()));
//			if (ok) iHeader.setErrorMessage(MESSAGES.errorRequired(MESSAGES.colLastBannerTermCode()));
//			ok = false;
//        }
// 
//        if (!iLastBannerTerm.getWidget().getText().isEmpty()|| iLastBannerTerm.getWidget().getText().trim().length() >0) {
//        	iLastBannerTerm.setErrorHint(MESSAGES.errorGeneric(MESSAGES.errorExternalManagerNameUse()));
//			if (ok) iHeader.setErrorMessage(MESSAGES.errorGeneric(MESSAGES.errorExternalManagerNameUse()));
//			ok = false;
//        }
// 
//        if (iFirstBannerTerm.getWidget().getText().isEmpty()|| iFirstBannerTerm.getWidget().getText().length() ==0) {
//        	iFirstBannerTerm.setErrorHint(MESSAGES.errorRequired(MESSAGES.fieldExternalManagerAbbreviation()));
//			if (ok) iHeader.setErrorMessage(MESSAGES.errorRequired(MESSAGES.fieldExternalManagerAbbreviation()));
//			ok = false;
//        }
//        if (!iFirstBannerTerm.getWidget().getText().isEmpty() || iFirstBannerTerm.getWidget().getText().trim().length() >0) {
//        	iFirstBannerTerm.setErrorHint(MESSAGES.errorGeneric(MESSAGES.errorExternalManagerAbbreviationUse()));
//			if (ok) iHeader.setErrorMessage(MESSAGES.errorGeneric(MESSAGES.errorExternalManagerAbbreviationUse()));
//			ok = false;
//        }
//
//        if (iManagingDeptCodeRegex.getWidget().getText().isEmpty()) {
//			iManagingDeptCodeRegex.setErrorHint(MESSAGES.errorDeptCodeIsEmpty());
//			if (ok) iHeader.setErrorMessage(MESSAGES.errorDeptCodeIsEmpty());
//			ok = false;
//		} 
		

        // Need to validate campus code, first, last term, ... combinations are unique and do not overlap
         
		return ok;
	}
	
	public boolean isEntryUnique(BannerCampusOverrideInterface bannerCampusOverrideInterface) {
		return true;
	}
	
	public boolean isNonOverlapping(BannerCampusOverrideInterface bannerCampusOverrideInterface) {
		return true;
	}
	
	/*
	 * execute add, update, delete
	 */
	public static class UpdateBannerCampusOverrideRequest implements GwtRpcRequest<BannerCampusOverrideInterface> {
		private UpdateBannerCampusOverrideAction iAction;
		private BannerCampusOverrideInterface iBannerCampusOverride;
			
		public UpdateBannerCampusOverrideAction getAction() { return iAction; }
		public void setAction(UpdateBannerCampusOverrideAction action) { iAction = action; }
		public BannerCampusOverrideInterface getBannerCampusOverride() { return iBannerCampusOverride; }
		public void setBannerCampusOverride(BannerCampusOverrideInterface bannerCampusOverride) { iBannerCampusOverride = bannerCampusOverride; }

	}	

	public void show() {
		Window.scrollTo(0, 0);
	}
	
	/*onBack is redefined in BannerCampusOverridePage*/	
	protected void onBack(boolean refresh, Long bannerCampusOverrideId) {
		
		
	}

	/*
	 * Add  row in banner campus override 
	 */
	public void addNewRow(BannerCampusOverrideInterface bannerCampusOverride ) {
		// Add a button to delete
		Button deleteStatusButton = new Button("Delete");
		int row = iControlDeptFlexTable.getRowCount();
		
		//delete a row
		deleteStatusButton.addClickHandler(new ClickHandler() {
			public void onClick(ClickEvent event) {
				int receiverRowIndex = iControlDeptFlexTable.getCellForEvent(event).getRowIndex();
				iControlDeptFlexTable.removeRow(receiverRowIndex);
			}
		});
		
		//delete btn
		iControlDeptFlexTable.setWidget(row, 0, deleteStatusButton);

//		//delete btn
//		iControlDeptFlexTable.setWidget(row, 2, deleteStatusButton);
//
//		//status
//		iControlDeptFlexTable.setWidget(row, 1, statusOptions(bannerCampusOverride));
//
//		//department drop down
//		iControlDeptFlexTable.setWidget(row, 0, departmentOptions(bannerCampusOverride));
	}



}
