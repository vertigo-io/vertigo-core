package io.vertigo.dynamo.impl.transaction;

import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.kernel.aop.Interceptor;
import io.vertigo.kernel.aop.MethodInvocation;
import io.vertigo.kernel.lang.Assertion;

import javax.inject.Inject;

/**
 * Intercepteur pour la gestion des transactions au niveau
 * de la couche service.
 * @author prahmoune
 */
public class KTransactionInterceptor implements Interceptor {
	private final KTransactionManager transactionManager;

	@Inject
	public KTransactionInterceptor(final KTransactionManager transactionManager) {
		Assertion.checkNotNull(transactionManager);
		//---------------------------------------------------------------------
		this.transactionManager = transactionManager;
	}

	public Object invoke(final Object[] args, final MethodInvocation methodInvocation) throws Throwable {
		//La transaction est REQUIRED : si elle existe on l'utilise, sinon on la crée.
		if (transactionManager.hasCurrentTransaction()) {
			return methodInvocation.proceed(args);
		}
		//Dans le cas ou il n'existe pas de transaction on en crée une.
		try (final KTransactionWritable transaction = transactionManager.createCurrentTransaction()) {
			final Object o = methodInvocation.proceed(args);
			transaction.commit();
			return o;
		}
	}
}
