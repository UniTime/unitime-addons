package org.unitime.timetable.gwt.client.campusoverrides;

import org.unitime.timetable.gwt.client.Client;
import org.unitime.timetable.gwt.client.ToolBox;
import org.unitime.timetable.gwt.client.page.UniTimeNotifications;
import org.unitime.timetable.gwt.client.page.UniTimePageLabel;
import org.unitime.timetable.gwt.client.widgets.LoadingWidget;
import org.unitime.timetable.gwt.client.widgets.SimpleForm;
import org.unitime.timetable.gwt.client.widgets.UniTimeHeaderPanel;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.MouseClickListener;
import org.unitime.timetable.gwt.client.widgets.UniTimeTable.TableEvent;
import org.unitime.timetable.gwt.command.client.GwtRpcService;
import org.unitime.timetable.gwt.command.client.GwtRpcServiceAsync;
import org.unitime.timetable.gwt.resources.BannerGwtMessages;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.BannerCampusOverridePropertiesInterface;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.BannerCampusOverridePropertiesRequest;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.BannerCampusOverridesDataResponse;
import org.unitime.timetable.gwt.shared.BannerCampusOverrideInterface.GetBannerCampusOverridesRequest;
import org.unitime.timetable.gwt.shared.DepartmentInterface.DepartmentsDataResponse;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.SimplePanel;

public class BannerCampusOverridesPage extends Composite {
	protected static final  BannerGwtMessages MESSAGES = GWT.create(BannerGwtMessages.class);
	protected static GwtRpcServiceAsync RPC = GWT.create(GwtRpcService.class);

	private SimplePanel iPanel;
	private SimpleForm iListBannerCampusOverridesForm;
	private UniTimeHeaderPanel iListBannerCampusOverridesHeader, iListBannerCampusOverridesFooter;
	private BannerCampusOverridesTable iBannerCampusOverridesTable;
	private BannerCampusOverridesEdit iBannerCampusOverridesEdit;
	private BannerCampusOverridesDataResponse iBannerCampusOverridesDataFullList;


	public BannerCampusOverridesPage() {

		iPanel = new SimplePanel();

		iListBannerCampusOverridesForm = new SimpleForm();
		iListBannerCampusOverridesForm.addStyleName("unitime-Dept");
		// Header
		iListBannerCampusOverridesHeader = new UniTimeHeaderPanel(
				MESSAGES.sectBannerCampusOverrides());
		
		// Banner Campus Override ADD Button
		iListBannerCampusOverridesHeader.addButton("add", MESSAGES.buttonAddBannerCampusOverride(),
				new ClickHandler() {
					@Override
					public void onClick(ClickEvent event) {
						addBannerCampusOverride();
					}
				});

		iListBannerCampusOverridesForm.addHeaderRow(iListBannerCampusOverridesHeader);

		//Body
		iBannerCampusOverridesTable = new BannerCampusOverridesTable();
		iListBannerCampusOverridesForm.addRow(iBannerCampusOverridesTable);
		
		
		//Footer
		iListBannerCampusOverridesFooter = iListBannerCampusOverridesHeader.clonePanel("");
		
		iListBannerCampusOverridesForm.addBottomRow(iListBannerCampusOverridesFooter);
		

		// load
		LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());

		// Get all banner campus overrides
		listBannerCampusOverrides();

		iBannerCampusOverridesEdit = new BannerCampusOverridesEdit() {
			
			//Override on back event from BannerCampusOverrideEdit page
			@Override
			protected void onBack(boolean refresh, final Long bannerCampusOverrideId) {
				iPanel.setWidget(iListBannerCampusOverridesForm);
				UniTimePageLabel.getInstance().setPageName(MESSAGES.pageBannerCampusOverrides());
				Client.fireGwtPageChanged(new Client.GwtPageChangeEvent());
				if (refresh) {
					LoadingWidget.getInstance().show(MESSAGES.waitLoadingData());
					RPC.execute(new GetBannerCampusOverridesRequest(),new AsyncCallback<BannerCampusOverridesDataResponse>() {
								@Override
								public void onSuccess(BannerCampusOverridesDataResponse result) {
									iListBannerCampusOverridesHeader.setEnabled("add",result.isCanAdd());
									//banner campus overrides list
									iBannerCampusOverridesTable.setData(result.getBannerCampusOverrides());
																		
									LoadingWidget.getInstance().hide();
									if (bannerCampusOverrideId != null)
										for (int i = 0; i < iBannerCampusOverridesTable.getRowCount(); i++) {
											BannerCampusOverrideInterface b = iBannerCampusOverridesTable.getData(i);
											if (b != null && b.getId().equals(bannerCampusOverrideId)) {
												iBannerCampusOverridesTable.getRowFormatter().getElement(i).scrollIntoView();
												iBannerCampusOverridesTable.setSelected(i, true);
												break;
											}
										}
								}

								@Override
								public void onFailure(Throwable caught) {
									LoadingWidget.getInstance().hide();
									UniTimeNotifications.error(MESSAGES.failedLoadData(caught.getMessage()), caught);
									iListBannerCampusOverridesHeader.setErrorMessage(MESSAGES.failedLoadData(caught.getMessage()));
									ToolBox.checkAccess(caught);
								}

							});
				} else {
					if (iBannerCampusOverridesTable.getSelectedRow() >= 0)
						iBannerCampusOverridesTable.setSelected(iBannerCampusOverridesTable.getSelectedRow(), false);
					if (bannerCampusOverrideId != null)
						for (int i = 0; i < iBannerCampusOverridesTable.getRowCount(); i++) {
							BannerCampusOverrideInterface b = iBannerCampusOverridesTable.getData(i);
							if (b != null && b.getId().equals(bannerCampusOverrideId)) {
								iBannerCampusOverridesTable.getRowFormatter().getElement(i).scrollIntoView();
								iBannerCampusOverridesTable.setSelected(i, true);
								break;
							}
						}
				}
			}
						
		};
		iPanel.setWidget(iListBannerCampusOverridesForm);
		initWidget(iPanel);
			
		
		
	}

	protected void editBannerCampusOverride(BannerCampusOverrideInterface bannerCampusOverride) {
		RPC.execute(new BannerCampusOverridePropertiesRequest(bannerCampusOverride.getId()),new AsyncCallback<BannerCampusOverridePropertiesInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListBannerCampusOverridesHeader.setErrorMessage(MESSAGES.failedCreate(MESSAGES.objectBannerCampusOverride(), caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedUpdate(MESSAGES.objectBannerCampusOverride(), caught.getMessage()), caught);
			}
			@Override
			public void onSuccess(BannerCampusOverridePropertiesInterface result) {
				if (result.getCanEdit()) {
					iBannerCampusOverridesEdit.setProperties(result);
					iBannerCampusOverridesEdit.setValue(bannerCampusOverride);
					iPanel.setWidget(iBannerCampusOverridesEdit);
					iBannerCampusOverridesEdit.show();
					UniTimePageLabel.getInstance().setPageName(MESSAGES.pageBannerCampusOverride());
					iBannerCampusOverridesTable.clearHover();
					Client.fireGwtPageChanged(new Client.GwtPageChangeEvent());
				}
			}
		});
	}
	private void listBannerCampusOverrides() {
		// Get all banner campus overrides
		RPC.execute(new GetBannerCampusOverridesRequest(),
				new AsyncCallback<BannerCampusOverridesDataResponse>() {
					@Override
					public void onSuccess(BannerCampusOverridesDataResponse result) {
						iBannerCampusOverridesDataFullList = result;
						iListBannerCampusOverridesHeader.setEnabled("add",result.isCanAdd());
						iBannerCampusOverridesTable.removeAllRows();
						iBannerCampusOverridesTable.setAllowSelection(true);
						iBannerCampusOverridesTable.setAllowMultiSelect(false);
						
						// list banner campus overrides
						iBannerCampusOverridesTable.setData(result.getBannerCampusOverrides());
						
						LoadingWidget.getInstance().hide();

						// Click to edit
						iBannerCampusOverridesTable
								.addMouseClickListener(new MouseClickListener<BannerCampusOverrideInterface>() {
									@Override
									public void onMouseClick(
											TableEvent<BannerCampusOverrideInterface> event) {
										if (event.getData() != null) {
											iBannerCampusOverridesTable.setSelected(
													event.getRow(), true);
											editBannerCampusOverride(event.getData());
										}
									}
								});
					}

					@Override
					public void onFailure(Throwable caught) {
						LoadingWidget.getInstance().hide();
						UniTimeNotifications.error(
								MESSAGES.failedLoadData(caught.getMessage()),
								caught);
						iListBannerCampusOverridesHeader.setErrorMessage(MESSAGES
								.failedLoadData(caught.getMessage()));
						ToolBox.checkAccess(caught);
					}

				});
	}

	protected void addBannerCampusOverride() {
		RPC.execute(new BannerCampusOverridePropertiesRequest(null),new AsyncCallback<BannerCampusOverridePropertiesInterface>() {
			@Override
			public void onFailure(Throwable caught) {
				LoadingWidget.getInstance().hide();
				iListBannerCampusOverridesHeader.setErrorMessage(MESSAGES.failedCreate(MESSAGES.objectBannerCampusOverride(), caught.getMessage()));
				UniTimeNotifications.error(MESSAGES.failedCreate(MESSAGES.objectBannerCampusOverride(), caught.getMessage()), caught);
			}
			@Override
			public void onSuccess(BannerCampusOverridePropertiesInterface result) {
				if (result.getCanEdit()) {
					iBannerCampusOverridesEdit.setProperties(result);
					iBannerCampusOverridesEdit.setValue(null);
					iPanel.setWidget(iBannerCampusOverridesEdit);
					iBannerCampusOverridesEdit.show();
					UniTimePageLabel.getInstance().setPageName(MESSAGES.pageAddBannerCampusOverride());
					iBannerCampusOverridesTable.clearHover();
					Client.fireGwtPageChanged(new Client.GwtPageChangeEvent());
				}						
			}
		});

	}
	
	
}
