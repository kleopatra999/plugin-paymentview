package org.creditsms.plugins.paymentview.data.repository.hibernate;

import java.sql.Time;
import java.util.List;

import net.frontlinesms.data.DuplicateKeyException;
import net.frontlinesms.data.repository.hibernate.BaseHibernateDao;

import org.creditsms.plugins.paymentview.data.domain.Client;
import org.creditsms.plugins.paymentview.data.domain.Target;
import org.creditsms.plugins.paymentview.data.repository.TargetDao;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;

/**
 * 
 * @author Roy
 * 
 */
public class HibernateTargetDao extends BaseHibernateDao<Target> implements
		TargetDao {

	protected HibernateTargetDao() {
		super(Target.class);
	}

	public void deleteTarget(Target target) {
		super.delete(target);
	}

	public List<Target> getAllTargets() {
		return super.getAll();
	}

	public List<Target> getAllTargets(int startIndex, int limit) {
		return super.getAll(startIndex, limit);
	}

	public List<Target> getTargetsByAccount(String string) {
		DetachedCriteria criteria = super.getCriterion();
		DetachedCriteria accountCriteria = criteria.createCriteria("account");
		accountCriteria.add(Restrictions.eq("accountNumber", string));
		return super.getList(criteria);
	}
	//getAllActiveTargets
	
	public List<Target> getAllActiveTargets() {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.isNull("completedDate"));
		return super.getList(criteria);
	}
	
	public Target getActiveTargetByAccount(String string) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.isNull("completedDate"));
		DetachedCriteria accountCriteria = criteria.createCriteria("account");
		accountCriteria.add(Restrictions.eq("accountNumber", string));
		return super.getUnique(criteria);
	}

	public Target getTargetByEndDateBetweenDates(long targetItemId,
			Time startDate, Time endDate) {
		// TODO Auto-generated method stub
		return null;
	}

	public Target getTargetById(long id) {
		DetachedCriteria criteria = super.getCriterion();
		criteria.add(Restrictions.eq("id", id));
		return super.getUnique(criteria);
	}
	
	public int getTargetCount() {
		return super.countAll();
	}

	public void saveTarget(Target target) {
		super.saveWithoutDuplicateHandling(target);
	}

	public void updateTarget(Target target) throws DuplicateKeyException {
		super.update(target);
	}

	public List<Target> getTargetsByStatus(String status) {
		DetachedCriteria criteria = super.getCriterion().add(
				Restrictions
						.disjunction()
						.add(Restrictions.ilike("status", status,
								MatchMode.ANYWHERE)));
		return super.getList(criteria);
	}
}
