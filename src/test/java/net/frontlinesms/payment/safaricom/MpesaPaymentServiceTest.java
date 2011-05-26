package net.frontlinesms.payment.safaricom;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.data.events.EntitySavedNotification;
import net.frontlinesms.events.EventObserver;
import net.frontlinesms.junit.BaseTestCase;
import net.frontlinesms.payment.PaymentServiceException;
import net.frontlinesms.ui.events.FrontlineUiUpateJob;

import org.creditsms.plugins.paymentview.data.domain.Account;
import org.creditsms.plugins.paymentview.data.domain.Client;
import org.creditsms.plugins.paymentview.data.domain.IncomingPayment;
import org.creditsms.plugins.paymentview.data.domain.OutgoingPayment;
import org.creditsms.plugins.paymentview.data.repository.AccountDao;
import org.creditsms.plugins.paymentview.data.repository.ClientDao;
import org.creditsms.plugins.paymentview.data.repository.IncomingPaymentDao;
import org.creditsms.plugins.paymentview.data.repository.OutgoingPaymentDao;
import org.mockito.InOrder;
import org.smslib.CService;
import org.smslib.SMSLibDeviceException;
import org.smslib.stk.StkInputRequiremnent;
import org.smslib.stk.StkMenu;
import org.smslib.stk.StkMenuItem;
import org.smslib.stk.StkRequest;

/** Unit tests for {@link MpesaPaymentService} */
public abstract class MpesaPaymentServiceTest<E extends MpesaPaymentService> extends BaseTestCase {
	private static final String PHONENUMBER_0 = "+254723908000";
	protected static final String PHONENUMBER_1 = "+254723908001";
	protected static final String PHONENUMBER_2 = "+254723908002";
	protected static final String ACCOUNTNUMBER_1_1 = "0700000011";
	protected static final String ACCOUNTNUMBER_2_1 = "0700000021";
	protected static final String ACCOUNTNUMBER_2_2 = "0700000022";
	
	private E mpesaPaymentService;
	private CService cService;
	
	private StkMenuItem myAccountMenuItem;
	private StkRequest mpesaMenuItemRequest;
	private StkMenuItem sendMoneyMenuItem;
	private ClientDao clientDao;
	private AccountDao accountDao;
	private IncomingPaymentDao incomingPaymentDao;
	private OutgoingPaymentDao outgoingPaymentDao;
	
	
	@Override
	protected void setUp() throws Exception {
		super.setUp();
		
		this.mpesaPaymentService = createNewTestClass();
		this.cService = mock(CService.class);
		mpesaPaymentService.setCService(cService);
		incomingPaymentDao = mock(IncomingPaymentDao.class);
		outgoingPaymentDao= mock(OutgoingPaymentDao.class);
		
		mpesaPaymentService.setIncomingPaymentDao(incomingPaymentDao);
		mpesaPaymentService.setOutgoingPaymentDao(outgoingPaymentDao);		
		
		setUpDaos();

		StkMenuItem mpesaMenuItem = mockMenuItem("M-PESA", 129, 21);
		when(cService.stkRequest(StkRequest.GET_ROOT_MENU)).thenReturn(
				new StkMenu("Safaricom", "Safaricom+", mpesaMenuItem));
		
		myAccountMenuItem = mockMenuItem("My account");
		mpesaMenuItemRequest = mpesaMenuItem.getRequest();
		sendMoneyMenuItem = mockMenuItem("Send money");
		when(cService.stkRequest(mpesaMenuItemRequest)).thenReturn(new StkMenu("M-PESA",
				sendMoneyMenuItem , "Withdraw cash", "Buy airtime",
				"Pay Bill", "Buy Goods", "ATM Withdrawal", myAccountMenuItem));
	}
	
	protected abstract E createNewTestClass();

	abstract String[] getValidMessagesText();
	abstract String[] getInvalidMessagesText();
	
	public void testValidMessageText() {
		String[] validMessagesText = getValidMessagesText();
		for(int i=0; i<validMessagesText.length; i+=2) {
			String testDescription = validMessagesText[i];
			String messageText = validMessagesText[i+1];
			assertTrue(testDescription, mpesaPaymentService.isMessageTextValid(messageText));
		}
	}

	public void testInvalidMessageText() {
		String[] invalidMessagesText = getInvalidMessagesText();
		for(int i=0; i<invalidMessagesText.length; i+=2) {
			String testDescription = invalidMessagesText[i];
			String messageText = invalidMessagesText[i+1];
			assertFalse(testDescription, mpesaPaymentService.isMessageTextValid(messageText));
		}
	}

	@SuppressWarnings("unchecked")
	private void setUpDaos() {
		clientDao = mock(ClientDao.class);
		accountDao = mock(AccountDao.class);

		Set<Account> accounts1 = mockAccounts(ACCOUNTNUMBER_1_1);
		Set<Account> accounts2 = mockAccounts(ACCOUNTNUMBER_2_1, ACCOUNTNUMBER_2_2);
		
		mockClient(0, PHONENUMBER_0, Collections.EMPTY_SET);
		mockClient(1, PHONENUMBER_1, accounts1);
		mockClient(2, PHONENUMBER_2, accounts2);
		
		this.mpesaPaymentService.setClientDao(clientDao);
		this.mpesaPaymentService.setAccountDao(accountDao);
	}
	
	private void mockClient(long id, String phoneNumber, Set<Account> accounts) {
		Client c = mock(Client.class);
		when(c.getId()).thenReturn(id);
		when(clientDao.getClientByPhoneNumber(phoneNumber)).thenReturn(c);
		
		//when(c.getAccounts()).thenReturn(accounts);
		//when(accountDao.getAccountsByClientId(c.getId())).thenReturn((List<Account>) accounts);
		
		when(accountDao.getAccountsByClientId(id)).thenReturn(new ArrayList<Account>(accounts));
	}

	public void testCheckBalance() throws PaymentServiceException, SMSLibDeviceException {
		// setup
		StkRequest myAccountMenuItemRequest = myAccountMenuItem.getRequest();
		StkMenuItem showBalanceMenuItem = mockMenuItem("Show balance");
		when(cService.stkRequest(myAccountMenuItemRequest)).thenReturn(
				new StkMenu("My account", showBalanceMenuItem, "Call support",
						"Change PIN", "Secret word", "Language", "Update menu"));
		StkInputRequiremnent pinRequired = mockInputRequirement("Enter PIN", 0, 0, 4, 4, 0);
		StkRequest pinRequiredRequest = pinRequired.getRequest();
		StkRequest showBalanceMenuItemRequest = showBalanceMenuItem.getRequest();
		when(cService.stkRequest(showBalanceMenuItemRequest)).thenReturn(pinRequired);
		
		// given
		mpesaPaymentService.setPin("1234");
		
		// when
		mpesaPaymentService.checkBalance();
		
		// then
		InOrder inOrder = inOrder(cService);
		inOrder.verify(cService).stkRequest(StkRequest.GET_ROOT_MENU);
		inOrder.verify(cService).stkRequest(mpesaMenuItemRequest);
		inOrder.verify(cService).stkRequest(myAccountMenuItemRequest);
		inOrder.verify(cService).stkRequest(showBalanceMenuItemRequest);
		inOrder.verify(cService).stkRequest(pinRequiredRequest, "1234");
	}
	
	public void testMakePayment() throws PaymentServiceException, SMSLibDeviceException {
		// setup
		StkRequest sendMoneyMenuItemRequest = sendMoneyMenuItem.getRequest();
		StkInputRequiremnent phoneNumberRequired = mockInputRequirement("Enter phone no.");
		when(cService.stkRequest(sendMoneyMenuItemRequest)).thenReturn(phoneNumberRequired);
		StkRequest phoneNumberRequest = phoneNumberRequired.getRequest();
		StkInputRequiremnent amountRequired = mockInputRequirement("Enter amount");
		when(cService.stkRequest(phoneNumberRequest, "0712345678")).thenReturn(amountRequired);
		StkRequest amountRequest = amountRequired.getRequest();
		StkInputRequiremnent pinRequired = mockInputRequirement("Enter PIN");
		when(cService.stkRequest(amountRequest, "500")).thenReturn(pinRequired);
		StkRequest pinRequiredRequest = pinRequired.getRequest();
		
		// given
		mpesaPaymentService.setPin("1234");
		
		// when
		mpesaPaymentService.makePayment(mockAccount(), new BigDecimal("500"));
		
		// then
		InOrder inOrder = inOrder(cService);
		inOrder.verify(cService).stkRequest(StkRequest.GET_ROOT_MENU);
		inOrder.verify(cService).stkRequest(mpesaMenuItemRequest);
		inOrder.verify(cService).stkRequest(sendMoneyMenuItemRequest);
		inOrder.verify(cService).stkRequest(phoneNumberRequest , "0712345678");
		inOrder.verify(cService).stkRequest(amountRequest , "500");
		inOrder.verify(cService).stkRequest(pinRequiredRequest , "1234");
	}
	
	protected void testIncomingPaymentProcessing(String messageText,
			final String phoneNo, final String accountNumber, final String amount,
			final String confirmationCode, final String payedBy, final String datetime) {
		// then
		assertTrue(mpesaPaymentService instanceof EventObserver);
		
		// when
		mpesaPaymentService.notify(mockMessageNotification("MPESA", messageText));
		
		// then
		WaitingJob.waitForEvent();
		verify(incomingPaymentDao).saveIncomingPayment(new IncomingPayment() {
			@Override
			public boolean equals(Object that) {
				
				if(!(that instanceof IncomingPayment)) return false;
				IncomingPayment other = (IncomingPayment) that;
				return other.getPhoneNumber().equals(phoneNo) &&
						other.getAmountPaid().equals(new BigDecimal(amount)) &&
						other.getAccount().getAccountNumber().equals(accountNumber) &&
						other.getConfirmationCode().equals(confirmationCode) &&
						other.getTimePaid().equals(getTimestamp(datetime)) &&
						other.getPaymentBy().equals(payedBy);
						
			}
		});
	}
	
	protected void testOutgoingPaymentProcessing(String messageText,
			final String phoneNo, final String accountNumber, final String amount,
			final String confirmationCode, final String payedBy, final String datetime) {
		// then
		assertTrue(mpesaPaymentService instanceof EventObserver);
		
		// when
		mpesaPaymentService.notify(mockMessageNotification("MPESA", messageText));
		
		// then
		WaitingJob.waitForEvent();
		verify(outgoingPaymentDao).saveOutgoingPayment(new OutgoingPayment() {
			@Override
			public boolean equals(Object that) {
				
				if(!(that instanceof OutgoingPayment)) return false;
				OutgoingPayment other = (OutgoingPayment) that;
				return other.getPhoneNumber().equals(phoneNo) &&
						other.getAmountPaid().equals(new BigDecimal(amount)) &&
						other.getAccount().getAccountNumber().equals(accountNumber) &&
						other.getConfirmationCode().equals(confirmationCode) &&
						other.getTimePaid().equals(getTimestamp(datetime)) &&
						other.getPaymentTo().equals(payedBy);
						
			}
		});
	}
	
	private long getTimestamp(String dateString) {
		try {
			return new SimpleDateFormat("HH:mm dd MMM yyyy").parse(dateString).getTime();
		} catch (ParseException e) {
			throw new IllegalArgumentException("Test date supplied in incorrect format: " + dateString);
		}
	}

	public void testIncomingPaymentProcessorIgnoresIrrelevantMessages() {
		// when
		testIncomingPaymentProcessorIgnoresMessage("MPESA", "Some random text...");
		testIncomingPaymentProcessorIgnoresMessage("0798765432", "... and some more random text.");
	}
	
	protected void testIncomingPaymentProcessorIgnoresMessage(String fromNumber, String messageText) {
		mpesaPaymentService.notify(mockMessageNotification(fromNumber, messageText));
		
		// then
		WaitingJob.waitForEvent();
		verify(incomingPaymentDao, never()).saveIncomingPayment(any(IncomingPayment.class));
	}
	
	public void testFakedIncomingPayment() {
		// Message text which looks reasonable, but is wrong format
		testFakedIncomingPayment("0798765432", "0712345678 sent payment of 500 KES");
		
		// Genuine MPESA message text, with bad number
		testFakedIncomingPayment("0798765432", "BI94HR849 Confirmed.\n" +
				"You have received Ksh1,235 JOHN KIU 254723908653 on 3/5/11 at 10:35 PM\n" +
				"New M-PESA balance Ksh1,236");
		
		// Genuine PayBill message text, with bad number
		testFakedIncomingPayment("0798765432", "BH45UU225 Confirmed.\n" +
				"on 5/4/11 at 2:45 PM\n" +
				"Ksh950 received from ELLY ASAKHULU 254713698227.\n" +
				"Account Number 0713698227\n" +
				"New Utility balance is Ksh50,802\n" +
				"Time: 05/04/2011 14:45:34");
		
		// Genuinie PayBill message from correct number, but with bad date (29 Undecimber)
		testFakedIncomingPayment("MPESA", "BHT57U225XXX Confirmed.\n"
				+ "on 29/13/11 at 1:45 PM\n"
				+ "Ksh123 received from ELLY 254723908002.\n"
				+ "Account Number 0700000022\n"
				+ "New Utility balance is Ksh50,802\n"
				+ "Time: 29/13/2011 16:45:34");
	}
	
	private void testFakedIncomingPayment(String from, String messageText) {
		// setup
		MpesaPaymentService s = this.mpesaPaymentService;
		
		// when
		s.notify(mockMessageNotification(from, messageText));
		
		// then
		WaitingJob.waitForEvent();
		verify(incomingPaymentDao, never()).saveIncomingPayment(any(IncomingPayment.class));
	}
	
	private StkMenuItem mockMenuItem(String title, int... numbers) {
		StkMenuItem i = mock(StkMenuItem.class);
		when(i.getText()).thenReturn(title);
		StkRequest mockRequest = mock(StkRequest.class);
		when(i.getRequest()).thenReturn(mockRequest);
		return i;
	}
	
	private StkInputRequiremnent mockInputRequirement(String title, int... nums) {
		StkInputRequiremnent ir = mock(StkInputRequiremnent.class);
		when(ir.getText()).thenReturn(title);
		
		StkRequest mockRequest = mock(StkRequest.class);
		when(ir.getRequest()).thenReturn(mockRequest);
		return ir;
	}
	
	private Account mockAccount() {
		Account a = mock(Account.class);
		when(a.getAccountId()).thenReturn(Long.valueOf(123456789));
		
		Client c = mock(Client.class);
		when(c.getPhoneNumber()).thenReturn("0712345678");
		
		when(a.getClient()).thenReturn(c);
		
		return a;
	}
	
	private EntitySavedNotification<FrontlineMessage> mockMessageNotification(String from, String text) {
		FrontlineMessage m = mock(FrontlineMessage.class);
		when(m.getSenderMsisdn()).thenReturn(from);
		when(m.getTextContent()).thenReturn(text);
		return new EntitySavedNotification<FrontlineMessage>(m);
	}
	
	private Set<Account> mockAccounts(String... accountNumbers) {
		ArrayList<Account> accounts = new ArrayList<Account>();
		for(String accountNumber : accountNumbers) {
			Account account = mock(Account.class);
			when(account.getAccountNumber()).thenReturn(accountNumber);
			accounts.add(account);
			when(accountDao.getAccountByAccountNumber(accountNumber)).thenReturn(account);
		}
		return new HashSet<Account>(accounts);
	}
}

class WaitingJob extends FrontlineUiUpateJob {
	private boolean running;
	private void block() {
		running = true;
		execute();
		while(running) {
			try {
				Thread.sleep(200);
			} catch (InterruptedException e) {
				running = false;
			}
		}
	}
	
	public void run() {
		running = false;
	}
	
	/** Put a job on the UI event queue, and block until it has been run. */
	public static void waitForEvent() {
		new WaitingJob().block();
	}
}