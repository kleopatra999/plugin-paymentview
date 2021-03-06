package org.creditsms.plugins.paymentview.data.repository.hibernate;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.Order;
import net.frontlinesms.data.domain.PersistableSettings;
import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;

import org.creditsms.plugins.paymentview.data.domain.IncomingPayment;
import org.creditsms.plugins.paymentview.data.domain.OutgoingPayment;
import org.creditsms.plugins.paymentview.data.domain.OutgoingPayment.Status;
import org.creditsms.plugins.paymentview.data.repository.OutgoingPaymentDao;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

public class HibernateOutgoingPaymentDao extends
		BaseHibernateDao<OutgoingPayment> implements OutgoingPaymentDao {

	protected HibernateOutgoingPaymentDao() {
		super(OutgoingPayment.class);
	}

	public void deleteOutgoingPayment(OutgoingPayment outgoingPayment) {
		super.delete(outgoingPayment);
	}

	public List<OutgoingPayment> getAllOutgoingPayments() {
		return super.getAll();
	}
	
	public List<OutgoingPayment> getAllOutgoingPayments(int startIndex,
			int limit) {
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		List<OutgoingPayment> payments = getList(criteria);
		return payments.subList(startIndex, Math.min(startIndex+limit, payments.size()));
	}
	
	public int getOutgoingPaymentsCount() {
		return super.countAll();
	}
	
	public List<OutgoingPayment> getOutgoingPaymentsByClientId(long clientId) {
		DetachedCriteria criteria = super.getCriterion();
		DetachedCriteria clientCriteria = criteria.createCriteria("client");
		clientCriteria.add(Restrictions.eq("id", clientId));
		return super.getList(criteria);
	}

	public OutgoingPayment getOutgoingPaymentById(long id) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq("id", id));
		return super.getUnique(criteria);
	}

	public List<OutgoingPayment> getOutgoingPaymentsByAccountNumber(
			String accNumber) {
		DetachedCriteria criteria = super.getCriterion();
		DetachedCriteria accountCriteria = criteria.createCriteria("account");
		accountCriteria.add(Restrictions.eq("accountNumber", accNumber));
		return super.getList(criteria);
	}

	public List<OutgoingPayment> getOutgoingPaymentsByAccountNumberByDateRange(
			long accountId, Calendar startDate, Calendar endDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<OutgoingPayment> getOutgoingPaymentsByAccountNumberByTimeRange(
			long accountId, Date startDate, Date endDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<OutgoingPayment> getOutgoingPaymentsByClientIdByDateRange(
			long clientId, Calendar startDate, Calendar endDate) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<OutgoingPayment> getOutgoingPaymentsByDateRange(Date startDate,	Date endDate,int startIndex,int limit) {
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		criteria.add(Restrictions.between("timePaid", startDate.getTime(), endDate.getTime()));
		List<OutgoingPayment> payments = getList(criteria);
		return payments.subList(startIndex, Math.min(startIndex+limit, payments.size()));
	}
	
	public List<OutgoingPayment> getOutgoingPaymentsByDateRange(Date startDate,	Date endDate) {
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		criteria.add(Restrictions.between("timePaid", startDate.getTime(), endDate.getTime()));
		return super.getList(criteria);
	}
	
	public List<OutgoingPayment> getOutgoingPaymentsByStartDate(Date startDate, int startingIndex, int limit) {
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		criteria.add(Restrictions.ge("timePaid", startDate.getTime()));
		List<OutgoingPayment> payments = getList(criteria);
		return payments.subList(startingIndex, Math.min(startingIndex+limit, payments.size()));
	}
	
	public List<OutgoingPayment> getOutgoingPaymentsByStartDate(Date startDate) {
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		criteria.add(Restrictions.ge("timePaid", startDate.getTime()));
		return super.getList(criteria);
	}
	
	public List<OutgoingPayment> getOutgoingPaymentsByEndDate(Date endDate, int startingIndex, int limit) {
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		criteria.add(Restrictions.le("timePaid", endDate.getTime()));
		List<OutgoingPayment> payments = getList(criteria);
		return payments.subList(startingIndex, Math.min(startingIndex+limit, payments.size()));
	}
	
	public List<OutgoingPayment> getOutgoingPaymentsByEndDate(Date endDate) {
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		criteria.add(Restrictions.le("timePaid", endDate.getTime()));
		return super.getList(criteria);
	}

	public List<OutgoingPayment> getOutgoingPaymentsByPhoneNo(String phoneNo) {
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		DetachedCriteria clientCriteria = criteria.createCriteria("client");
		clientCriteria.add(Restrictions.eq("phoneNumber", phoneNo));
		return super.getList(criteria);
	}

	public List<OutgoingPayment> getOutgoingPaymentsByTimeRange(Date startTime,
			Date endtime) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<OutgoingPayment> getByPhoneNumberAndAmountPaid(String phoneNo, BigDecimal amountPaid, OutgoingPayment.Status status){
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		criteria.add(Restrictions.eq("amountPaid", amountPaid));
		criteria.add(Restrictions.eq("status", status));
		DetachedCriteria clientCriteria = criteria.createCriteria("client");
		clientCriteria.add(Restrictions.eq("phoneNumber", phoneNo));
		return super.getList(criteria);
	}
	

	public List<OutgoingPayment> getByAmountPaidAndStatus(
			BigDecimal amountPaid, Status status) {
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		criteria.add(Restrictions.eq("amountPaid", amountPaid));
		criteria.add(Restrictions.eq("status", status));
		return super.getList(criteria);
	}
	
	public List<OutgoingPayment> getOutgoingPaymentByFirstNameAndAmountAndSpecialAndStatus(
			String firstName,
			BigDecimal amountPaid,
			String special, 
			OutgoingPayment.Status status) {
		DetachedCriteria criteria = super.getSortCriterion(OutgoingPayment.Field.TIME_PAID, Order.DESCENDING);
		criteria.add(Restrictions.eq("amountPaid", amountPaid));
		criteria.add(Restrictions.eq("status", status));
		criteria.add(Restrictions.eq("special", special));
		DetachedCriteria clientCriteria = criteria.createCriteria("client");
		clientCriteria.add(Restrictions.eq("firstName", firstName));
		return super.getList(criteria);
	}


	public void saveOutgoingPayment(OutgoingPayment outgoingPayment){
		super.saveWithoutDuplicateHandling(outgoingPayment);
	}

	public void updateOutgoingPayment(OutgoingPayment outgoingPayment)
			throws DuplicateKeyException {
		super.update(outgoingPayment);

	}	
	
	public List<OutgoingPayment> getByPaymentServiceSettings(PersistableSettings paymentServiceSettings){
		DetachedCriteria criteria = super.getCriterion();
		DetachedCriteria paymentServiceSettingsCriteria = criteria.createCriteria("paymentServiceSettings");
		paymentServiceSettingsCriteria.add(Restrictions.eq("id", paymentServiceSettings.getId()));
		return super.getList(criteria);
	}

}
