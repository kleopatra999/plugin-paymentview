package org.creditsms.plugins.paymentview.data.repository.hibernate;

import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;

import org.creditsms.plugins.paymentview.data.domain.Client;
import org.creditsms.plugins.paymentview.data.domain.ResponseRecipient;
import org.creditsms.plugins.paymentview.data.repository.ResponseRecipientDao;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;

/**
 * 
 * @author Roy
 * 
 */

public class HibernateResponseRecipientDao extends BaseHibernateDao<ResponseRecipient> implements
ResponseRecipientDao{

	protected HibernateResponseRecipientDao() {
		super(ResponseRecipient.class);
	}

	public List<ResponseRecipient> getResponseRecipientByThirdPartyResponseId(
			long thirdPartyResponseId) {
		DetachedCriteria criteria = super.getCriterion();
		DetachedCriteria thirdPartyResponseCriteria = criteria.createCriteria("thirdPartyResponse");
		thirdPartyResponseCriteria.add(Restrictions.eq("id", thirdPartyResponseId));
		return super.getList(criteria);
	}
	
	public ResponseRecipient getResponseRecipientByTpResponseAndRecipient(
			long thirdPartyResponseId, Client recipientId) {
		DetachedCriteria criteria = super.getCriterion();
		DetachedCriteria thirdPartyResponseCriteria = criteria.createCriteria("thirdPartyResponse");
		thirdPartyResponseCriteria.add(Restrictions.eq("id", thirdPartyResponseId));
		criteria.add(Restrictions.eq("client", recipientId));
		return super.getUnique(criteria);
	}

	public void deleteResponseRecipient(ResponseRecipient responseRecipient) {
		super.delete(responseRecipient);
	}

	public void updateResponseRecipient(ResponseRecipient responseRecipient) throws DuplicateKeyException {
		super.update(responseRecipient);
	}

	public void saveResponseRecipient(ResponseRecipient responseRecipient)
			throws DuplicateKeyException {
		super.save(responseRecipient);
	}

	public List<ResponseRecipient> getAllResponseRecipients() {
		return super.getAll();
	}
}
