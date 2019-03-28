package com.framework.core.dal.datasource.custom;

import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionStatus;

import com.framework.core.dal.datasource.DataSourceManager;

/**
 * 
 * @author zhangjun
 *
 */
public class CustomDataSourceTransactionManager extends DataSourceTransactionManager {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2397686295573806880L;


	@Override
	protected void doRollback(DefaultTransactionStatus status) {
		super.doRollback(status);
	}

	@Override
	protected void doCommit(DefaultTransactionStatus status) {
		super.doCommit(status);
	}

	@Override
	protected void doResume(Object transaction, Object suspendedResources) {
		super.doResume(transaction, suspendedResources);

	}

	@Override
	protected void doBegin(Object transaction, TransactionDefinition definition) {

		DataSourceManager.setTransactionFlag(true);

		super.doBegin(transaction, definition);
	}

	@Override
	protected void doCleanupAfterCompletion(Object transaction) {

		DataSourceManager.removeTransactionFlag();

		super.doCleanupAfterCompletion(transaction);

	}


}