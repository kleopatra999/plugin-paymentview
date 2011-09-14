package org.creditsms.plugins.paymentview.ui.handler.tabsettings;

import java.io.IOException;

import net.frontlinesms.events.EventBus;
import net.frontlinesms.events.EventObserver;
import net.frontlinesms.events.FrontlineEventNotification;
import net.frontlinesms.messaging.sms.events.SmsModemStatusNotification;
import net.frontlinesms.messaging.sms.modem.SmsModem;
import net.frontlinesms.messaging.sms.modem.SmsModemStatus;
import net.frontlinesms.payment.PaymentService;
import net.frontlinesms.payment.PaymentServiceException;
import net.frontlinesms.payment.event.PaymentServiceStartedNotification;
import net.frontlinesms.payment.event.PaymentServiceStoppedNotification;
import net.frontlinesms.payment.safaricom.MpesaPaymentService;
import net.frontlinesms.payment.safaricom.MpesaPaymentService.BalanceFraudNotification;
import net.frontlinesms.payment.safaricom.MpesaPaymentService.PaymentStatusEventNotification;
import net.frontlinesms.payment.safaricom.MpesaPaymentService.Status;
import net.frontlinesms.ui.UiDestroyEvent;
import net.frontlinesms.ui.UiGeneratorController;
import net.frontlinesms.ui.events.FrontlineUiUpateJob;
import net.frontlinesms.ui.handler.BaseTabHandler;

import org.apache.log4j.Logger;
import org.creditsms.plugins.paymentview.PaymentViewPluginController;
import org.creditsms.plugins.paymentview.data.domain.LogMessage;
import org.creditsms.plugins.paymentview.data.repository.LogMessageDao;
import org.creditsms.plugins.paymentview.ui.handler.tabsettings.dialogs.PaybillSendDialogHandler;
import org.creditsms.plugins.paymentview.ui.handler.tabsettings.dialogs.UpdateAuthorizationCodeDialog;
import org.creditsms.plugins.paymentview.ui.handler.tabsettings.dialogs.steps.createnewsettings.MobilePaymentServiceSettingsInitialisationDialog;
import org.creditsms.plugins.paymentview.userhomepropeties.payment.balance.Balance.BalanceEventNotification;
import org.creditsms.plugins.paymentview.userhomepropeties.payment.service.PaymentServiceProperties;

public class SettingsTabHandler extends BaseTabHandler implements EventObserver{
	private static final String BTN_CREATE_NEW_SERVICE = "btn_createNewService";
	private static final String COMPONENT_SETTINGS_TABLE = "tbl_accounts";
	private static final String CONFIRM_CHECK_BALANCE = "message.confirm.checkbalance";
	private static final String CONFIRM_CHECK_CONFIGURE_MODEM = "message.confirm.configure.modem";
	private String CONFIRM_DELETE_MOBILE_PAYMENT_ACCOUNT = "message.confirm.delete.mobilepaymentaccount";
	private static final String XML_SETTINGS_TAB = "/ui/plugins/paymentview/settings/settingsTab.xml";

	private Object settingsTab;
	private Object settingsTableComponent;
	Object dialogConfirmation;
	Object dialogConfimConfigureModem;
	Object dialogDeleteMobilePaymentAccount;
	private final PaymentViewPluginController pluginController;
	private EventBus eventBus;
	private LogMessageDao logMessageDao;
	private PaymentServiceProperties paymentSettingsProp = PaymentServiceProperties.getInstance();
	
	protected Logger pvLog = Logger.getLogger(this.getClass());

	public SettingsTabHandler(UiGeneratorController ui, PaymentViewPluginController pluginController) {
		super(ui);
		this.pluginController = pluginController;
		this.logMessageDao = pluginController.getLogMessageDao();
		eventBus = ui.getFrontlineController().getEventBus();
		eventBus.registerObserver(this);
		init();
	}

	public void createNew() {
		new MobilePaymentServiceSettingsInitialisationDialog(ui, pluginController).showDialog();
	}
	
	@Override
	protected Object initialiseTab() {
		settingsTab = ui.loadComponentFromFile(XML_SETTINGS_TAB, this);
		settingsTableComponent = ui.find(settingsTab, COMPONENT_SETTINGS_TABLE);
		return settingsTab;
	}
	
	public Object getRow(MpesaPaymentService paymentService) {
		Object row = ui.createTableRow(paymentService);
		ui.add(row, ui.createTableCell(paymentService.toString()));
		try {
			ui.add(row, ui.createTableCell(paymentService.getCService().getMsisdn()));
		} catch (IOException e) {
			ui.add(row, ui.createTableCell("Not configured"));
		}
		ui.add(row, ui.createTableCell(paymentService.getBalance().getBalanceAmount().toString()));
		return row;
	}

	@Override
	public void refresh() {
		ui.removeAll(settingsTableComponent);
		if (this.pluginController.getPaymentService() != null){
			ui.add(settingsTableComponent, getRow((MpesaPaymentService)this.pluginController.getPaymentService()));
		}
	}
	
	public void configureNewModem(){
		ui.remove(dialogConfimConfigureModem);
		Object selectedItem = this.ui.getSelectedItem(settingsTableComponent);
		if (selectedItem != null){
			PaymentService paymentService = ui.getAttachedObject(selectedItem, PaymentService.class);
			try {
				paymentService.configureModem();
				logMessageDao.saveLogMessage(new LogMessage(LogMessage.LogLevel.INFO, "Configure Modem Request Performed", ""));
				ui.alert("The modem is now configured. Please wait for a few seconds while it restarts.");
			} catch (PaymentServiceException e) {
				logMessageDao.saveLogMessage(new LogMessage(LogMessage.LogLevel.ERROR, "Configure Modem Request Failed", ""));
				pvLog.warn("Check Balance failed." + e);
			}
		}else{
			ui.alert("Please select an account to configure the modem");
		}	
	}

	public void updateAccountBalance(){
		ui.remove(dialogConfirmation);
		Object selectedItem = this.ui.getSelectedItem(settingsTableComponent);
		if (selectedItem != null){
			PaymentService paymentService = ui.getAttachedObject(selectedItem, PaymentService.class);
			try {
				paymentService.checkBalance();
				logMessageDao.saveLogMessage(new LogMessage(LogMessage.LogLevel.INFO, "Check Balance Request Performed", ""));
				ui.alert("Request has been sent. The Account balance will be updated shortly.");
			} catch (PaymentServiceException e) {
				logMessageDao.saveLogMessage(new LogMessage(LogMessage.LogLevel.ERROR, "Check Balance Request Failed", ""));
				pvLog.warn("Check Balance failed." + e);
			}
		}else{
			ui.alert("Please select an account to update balance.");
		}
	}
	
	public void updateAuthCode() {
		new UpdateAuthorizationCodeDialog(ui, pluginController).showDialog();
	}
	
	public void sendToPaybillAccount() {
		Object selectedItem = this.ui.getSelectedItem(settingsTableComponent);
		if (selectedItem != null) {
			PaymentService paymentService = ui.getAttachedObject(selectedItem, PaymentService.class);
			if (paymentService instanceof MpesaPaymentService){
				new PaybillSendDialogHandler(ui, pluginController, (MpesaPaymentService)paymentService).showDialog();
			}else{
				ui.alert("This functionality is only open to Safaricom Service.");
			}
		}else{
			ui.alert("Please select an account to use.");
		}
	}
	
	public void deleteAccount() {
		ui.remove(dialogDeleteMobilePaymentAccount);
		Object selectedItem = this.ui.getSelectedItem(settingsTableComponent);
		if (selectedItem != null) {
			PaymentService __paymentService = ui.getAttachedObject(selectedItem, PaymentService.class);
			//just before
			__paymentService.stop();
			//then notify listeners 
			eventBus.notifyObservers(new PaymentServiceStoppedNotification(__paymentService));
			//memory leaks?
			pluginController.setPaymentService(null);
			
			paymentSettingsProp.setPaymentServiceClass(null);
			paymentSettingsProp.setPin("");
			paymentSettingsProp.setSmsModem("");
			paymentSettingsProp.saveToDisk();
		}else{
			ui.alert("Please select an account to delete.");
		}
	}

	public void notify(final FrontlineEventNotification notification) {
		new FrontlineUiUpateJob() {
			public void run() {
				if(notification instanceof SmsModemStatusNotification &&
						((SmsModemStatusNotification) notification).getStatus() == SmsModemStatus.CONNECTED) {
					final SmsModem connectedModem = ((SmsModemStatusNotification) notification).getService();
					PaymentServiceProperties props = PaymentServiceProperties.getInstance();
					if(props.getSmsModemSerial()!=null){
						if(props.getSmsModemSerial().equals(connectedModem.getSerial())) {
							// We've just connected the configured device, so start up the payment service...
							//...if it's not already running!
							MpesaPaymentService mpesaPaymentService = (MpesaPaymentService) props.initPaymentService();
							if(mpesaPaymentService != null) {
								if(props.getPin() != null) {
									mpesaPaymentService.setPin(props.getPin());
									mpesaPaymentService.setCService(connectedModem.getCService());
									mpesaPaymentService.initDaosAndServices(pluginController);
									EventBus eventBus = ui.getFrontlineController().getEventBus();
									eventBus.registerObserver(mpesaPaymentService);
									pluginController.setPaymentService(mpesaPaymentService);
									eventBus.notifyObservers(new PaymentServiceStartedNotification(mpesaPaymentService));
									mpesaPaymentService.updateStatus(Status.PAYMENTSERVICE_ON);
								}
								// TODO configure the payment service from the properties file
								// TODO set the payment service in the plugin controller
								// TODO start the payment service
							} else {
								ui.alert("Please setup payment service");
							}
						}	
					} else {
						ui.alert("Please setup payment service");
					}
				} else if (notification instanceof BalanceEventNotification) {
					ui.alert(((BalanceEventNotification)notification).getMessage());
					SettingsTabHandler.this.refresh();
				} else if (notification instanceof PaymentServiceStartedNotification) {
					SettingsTabHandler.this.refresh();
					ui.setEnabled(ui.find(settingsTab, BTN_CREATE_NEW_SERVICE), false);
				} else if (notification instanceof PaymentServiceStoppedNotification) {
					SettingsTabHandler.this.refresh();
					ui.setEnabled(ui.find(settingsTab, BTN_CREATE_NEW_SERVICE), true);
				} else if (notification instanceof BalanceFraudNotification){
					ui.alert(((BalanceFraudNotification)notification).getMessage());
					SettingsTabHandler.this.refresh();
				} else if (notification instanceof PaymentStatusEventNotification){
					pluginController.updateStatusBar(((PaymentStatusEventNotification)notification).getPaymentStatus().toString());
				} else if (notification instanceof UiDestroyEvent) {
					if(((UiDestroyEvent) notification).isFor(ui)) {
						ui.getFrontlineController().getEventBus().unregisterObserver(SettingsTabHandler.this);
					}
				}
			}
		}.execute();
	}
	
	public final void checkBalance(String methodToBeCalled){
		dialogConfirmation = this.ui.showConfirmationDialog(methodToBeCalled, this, CONFIRM_CHECK_BALANCE);
	}
	
	public final void configureModem(String methodToBeCalled){
		dialogConfimConfigureModem = this.ui.showConfirmationDialog(methodToBeCalled, this, CONFIRM_CHECK_CONFIGURE_MODEM);
	}
	
	public final void deleteMobilePaymentAccount(String methodToBeCalled){
		dialogDeleteMobilePaymentAccount = this.ui.showConfirmationDialog(methodToBeCalled, this, CONFIRM_DELETE_MOBILE_PAYMENT_ACCOUNT);
	}
}
