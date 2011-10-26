package org.creditsms.plugins.paymentview.ui.handler.tabincomingpayments.dialogs;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.List;

import net.frontlinesms.ui.UiGeneratorController;

import org.creditsms.plugins.paymentview.PaymentViewPluginController;
import org.creditsms.plugins.paymentview.data.domain.Account;
import org.creditsms.plugins.paymentview.data.domain.Client;
import org.creditsms.plugins.paymentview.data.domain.IncomingPayment;
import org.creditsms.plugins.paymentview.data.repository.AccountDao;
import org.creditsms.plugins.paymentview.data.repository.ClientDao;
import org.creditsms.plugins.paymentview.data.repository.IncomingPaymentDao;
import org.creditsms.plugins.paymentview.data.repository.TargetDao;
import org.creditsms.plugins.paymentview.ui.handler.base.BaseDialog;
import org.creditsms.plugins.paymentview.ui.handler.tabincomingpayments.IncomingPaymentsTabHandler;
import org.creditsms.plugins.paymentview.ui.handler.tabincomingpayments.dialogs.DistributeIncomingPaymentDialogHandler.Child;

public class DistributeConfirmationDialogHandler extends BaseDialog{
//> CONSTANTS
	private static final String COMPONENT_TEXT_PARENT_NAME = "fldName";
	private static final String COMPONENT_TEXT_PARENT_PHONE_NUMBER = "fldPhoneNumber";
	private static final String COMPONENT_TEXT_PARENT_AMOUNT = "fldAmount";
	private static final String TBL_CHILDREN = "tbl_children";
	private static String XML_DISTRIBUTE_CONFIRMATION = "/ui/plugins/paymentview/incomingpayments/dialogs/dlgDistributeConfirmation.xml";
	private static String CONFIRM_ACCEPT_DISTRIBUTE_IP = "";
	
//>DAOs
	private IncomingPaymentDao incomingPaymentDao;
	
//> UI FIELDS
	private Object fieldName;
	private Object fieldPhoneNumber;
	private Object fieldAmount;

//UI HELPERS	
	private IncomingPayment parentIncomingPayment;
	private List<Child> children;
	private ClientDao clientDao;
	private Object tblChildrenComponent;
	private PaymentViewPluginController pluginController;
	private BigDecimal totalAmount;
	private AccountDao accountDao;
	private TargetDao targetDao;
	private String methodToBeCalled;
	private Object dialogConfimDistributeIp;
	


	
	public DistributeConfirmationDialogHandler(UiGeneratorController ui, PaymentViewPluginController pluginController, 
			IncomingPayment parentIncomingPayment, List<Child> children,DistributeIncomingPaymentDialogHandler distributeIncomingPaymentDialogHandler) {
		super(ui);
		this.pluginController = pluginController;
		this.incomingPaymentDao = pluginController.getIncomingPaymentDao();
		this.accountDao = pluginController.getAccountDao();
		this.clientDao = pluginController.getClientDao();
		this.targetDao = pluginController.getTargetDao();
		this.parentIncomingPayment = parentIncomingPayment;
		this.children = children;
		init();
		refresh();
	}

	public void init() {
		dialogComponent = ui.loadComponentFromFile(XML_DISTRIBUTE_CONFIRMATION, this);
		tblChildrenComponent = ui.find(dialogComponent, TBL_CHILDREN);	
		for(Child child: children) {
			ui.add(this.tblChildrenComponent, getRow(child));
		}
	}
	
	protected Object getRow(Child child) {
		Object row = ui.createTableRow(child);
		ui.add(row, ui.createTableCell(child.getClient().getPhoneNumber()));
		ui.add(row, ui.createTableCell(child.getClient().getFullName()));
		ui.add(row, ui.createTableCell(child.getAmount().toPlainString()));
		return row;
	}
	
	Account getAccount(String phoneNumber) {
		Client client = clientDao.getClientByPhoneNumber(phoneNumber);
		if (client != null) {
			List<Account> activeNonGenericAccountsByClientId = accountDao.getActiveNonGenericAccountsByClientId(client.getId());
			if(!activeNonGenericAccountsByClientId.isEmpty()){
				return activeNonGenericAccountsByClientId.get(0);
			} else {
				return accountDao.getGenericAccountsByClientId(client.getId());
			}
		}
		return null;
	}

	public void create(){
		CONFIRM_ACCEPT_DISTRIBUTE_IP = "Are you wure you want to distribute this group payment?\n After creating a group payment, this action cannot be undone.";
		dialogConfimDistributeIp = ((UiGeneratorController) ui).showConfirmationDialogPlainText("createInvidualIncomingPayments", this, CONFIRM_ACCEPT_DISTRIBUTE_IP);
	}
	
	public void createInvidualIncomingPayments(){
		ui.removeDialog(dialogConfimDistributeIp);
		
		//update parent incoming payment;
		parentIncomingPayment.setActive(false);
		incomingPaymentDao.updateIncomingPayment(parentIncomingPayment);
		
		//create individual incoming payments
		IncomingPayment individualIncomingPayment = new IncomingPayment();
		for (Child child: children){
			Account acc = getAccount(child.getClient().getPhoneNumber());
			
			individualIncomingPayment.setAccount(acc);
			individualIncomingPayment.setActive(true);
			individualIncomingPayment.setAmountPaid(child.getAmount());
			individualIncomingPayment.setChild(true);
			individualIncomingPayment.setConfirmationCode(parentIncomingPayment.getConfirmationCode());
			individualIncomingPayment.setPaymentBy(child.getClient().getFullName());
			individualIncomingPayment.setPhoneNumber(child.getClient().getPhoneNumber());
			individualIncomingPayment.setTarget(targetDao.getActiveTargetByAccount(acc.getAccountNumber()));
			individualIncomingPayment.setTimePaid(new Date(parentIncomingPayment.getTimePaid()));
			
			incomingPaymentDao.saveIncomingPayment(individualIncomingPayment);
		}
		this.removeDialog();
		ui.infoMessage("You have successfully distribute the incoming payment " + parentIncomingPayment.getConfirmationCode());

	}
	
	public void previous(){
		new DistributeIncomingPaymentDialogHandler(children, ui, pluginController, parentIncomingPayment).showDialog();
		this.removeDialog();

	}
	
	
	@Override
	public void refresh() {
	}

	
	@Override
	public void showDialog() {
		ui.add(this.dialogComponent);
	}
	



}
