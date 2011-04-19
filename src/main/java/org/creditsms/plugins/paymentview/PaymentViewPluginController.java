/*
 * FrontlineSMS:Credit - http://www.creditsms.org
 * Copyright (C) - 2009, 2010
 * 
 * This file is part of FrontlineSMS:Credit
 * 
 */
package org.creditsms.plugins.paymentview;

import net.frontlinesms.FrontlineSMS;
import net.frontlinesms.data.domain.FrontlineMessage;
import net.frontlinesms.listener.IncomingMessageListener;
import net.frontlinesms.plugins.BasePluginController;
import net.frontlinesms.plugins.PluginControllerProperties;
import net.frontlinesms.plugins.PluginInitialisationException;
import net.frontlinesms.ui.UiGeneratorController;

import org.creditsms.plugins.paymentview.ui.PaymentViewThinletTabController;
import org.creditsms.plugins.paymentview.ui.handler.IncomingPaymentsTabHandler;
import org.creditsms.plugins.paymentview.ui.handler.analytics.AnalyticsTabHandler;
import org.creditsms.plugins.paymentview.ui.handler.client.ClientsTabHandler;
import org.creditsms.plugins.paymentview.ui.handler.export.ExportTabHandler;
import org.creditsms.plugins.paymentview.ui.handler.outgoingpayments.OutgoingPaymentsTabHandler;
import org.springframework.context.ApplicationContext;
/**
 * This is the base class for the FrontlineSMS:Credit PaymentView plugin. The PaymentView
 * plugin is used to process payments transacted via the connected mobile phone. Processing
 * of the payments includes mining the incoming message for specific information and pushing
 * the same to an online/external database or system such as Mifos - http://www.mifos.org
 * @author Emmanuel Kala
 */
@PluginControllerProperties(
	name="Payment View", 
	iconPath="/icons/creditsms.png", 
	i18nKey="plugins.paymentview",
    springConfigLocation="classpath:org/creditsms/plugins/paymentview/paymentview-spring-hibernate.xml",
	hibernateConfigPath="classpath:org/creditsms/plugins/paymentview/paymentview.hibernate.cfg.xml"
)
public class PaymentViewPluginController extends BasePluginController implements IncomingMessageListener {

//>	CONSTANTS
	/** Filename and path of the XML for the PaymentView tab */
	private static final String XML_PAYMENT_VIEW_TAB = "/ui/plugins/paymentview/paymentViewTab.xml";
	private static final String TABP_MAIN_PANE = "tabP_mainPane";

	private FrontlineSMS frontlineController;
	private PaymentViewThinletTabController tabController;
	Object paymentViewTab;

	private Object mainPane;

//> THE OFFICIAL TABS FOR PAYMENTVIEW
	private ClientsTabHandler clientsTab;
	private ExportTabHandler exportTab;
	private OutgoingPaymentsTabHandler outgoingPayTab;
	private IncomingPaymentsTabHandler incomingPayTab;
	private AnalyticsTabHandler analyticsTab;  

//> CONFIG METHODS
	/** @see net.frontlinesms.plugins.PluginController#init(FrontlineSMS, ApplicationContext) */
	public void init(FrontlineSMS frontlineController,	ApplicationContext applicationContext) throws PluginInitialisationException {
		this.frontlineController = frontlineController;
		this.frontlineController.addIncomingMessageListener(this);
		//Initialize the DAO for the domain objects
	}

	/** @see net.frontlinesms.plugins.BasePluginController#initThinletTab(UiGeneratorController) */
	@Override
	public Object initThinletTab(UiGeneratorController uiController) {
		tabController = new PaymentViewThinletTabController(this, uiController);

		paymentViewTab = uiController.loadComponentFromFile(XML_PAYMENT_VIEW_TAB, tabController);
		tabController.setTabComponent(paymentViewTab);
		tabController.refresh();
		mainPane = uiController.find(paymentViewTab, TABP_MAIN_PANE);
		
		

		clientsTab = new ClientsTabHandler(uiController);
		clientsTab.refresh();
		uiController.add(mainPane, clientsTab.getTab());		
		
		incomingPayTab = new IncomingPaymentsTabHandler(uiController);
		incomingPayTab.refresh();
		uiController.add(mainPane, incomingPayTab.getTab());
		
		outgoingPayTab = new OutgoingPaymentsTabHandler(uiController);
		outgoingPayTab.refresh();
		uiController.add(mainPane, outgoingPayTab.getTab());

		exportTab = new ExportTabHandler(uiController);
		exportTab.refresh();
		uiController.add(mainPane, exportTab.getTab());
		
		analyticsTab = new AnalyticsTabHandler(uiController);
		analyticsTab.refresh();
		uiController.add(mainPane, analyticsTab.getTab());

		return paymentViewTab;
	}

	/**
	 * @see net.frontlinesms.plugins.PluginController#deinit()
	 */
	public void deinit() {
		//this.frontlineController.removeIncomingMessageListener(this);
	}

	/**
	 * Ensures that the incoming message is trapped and the necessary information
	 * is extracted i.e. transaction type, amount, sender and transaction code (if any)
	 * The above parameters may vary amongst service providers
	 */
	public void incomingMessageEvent(FrontlineMessage message) { 
	}
}
