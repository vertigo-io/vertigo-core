/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.impl.transaction;

import io.vertigo.dynamo.impl.transaction.listener.VTransactionListener;
import io.vertigo.dynamo.transaction.VTransaction;
import io.vertigo.dynamo.transaction.VTransactionResource;
import io.vertigo.dynamo.transaction.VTransactionResourceId;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implémentation standard d'une transaction Dynamo.
 *
 * @author  pchretien
 */
public final class VTransactionImpl implements VTransactionWritable {
	/**
	 * Contient la transaction du Thread courant.
	 */
	private static final ThreadLocal<VTransactionImpl> CURRENT_THREAD_LOCAL_TRANSACTION = new ThreadLocal<>();

	/**
	 * A la création la transaction est démarrée.
	 */
	private boolean transactionClosed;
	private final VTransactionListener transactionListener;
	/**
	 * Map des autres ressources de la transaction.
	 */
	private final Map<VTransactionResourceId<?>, VTransactionResource> resources = new HashMap<>();

	/**
	 * Transaction parente dans le cadre d'une transaction imbriquée.
	 * Nullable.
	 */
	private final VTransactionImpl parentTransaction;

	/**
	 * Transaction imbriquée.
	 * Nullable.
	 */
	private VTransactionImpl innerTransaction;
	/**
	 * Début de la transaction
	 */
	private final long start = System.currentTimeMillis();

	//==========================================================================
	//=========================== CONSTUCTEUR ==================================
	//==========================================================================
	/**
	 * Construit un contexte de transaction.
	 * @param transactionListener Listener des événements produits par
	 */
	VTransactionImpl(final VTransactionListener transactionListener) {
		Assertion.checkNotNull(transactionListener);
		//-----
		parentTransaction = null;
		this.transactionListener = transactionListener;
		//La transaction démarre
		transactionListener.onTransactionStart();
		CURRENT_THREAD_LOCAL_TRANSACTION.set(this);
	}

	/**
	 * Construit un contexte de transaction imbriquée.
	 * @param parentTransaction transaction parente
	 */
	VTransactionImpl(final VTransactionImpl parentTransaction) {
		Assertion.checkNotNull(parentTransaction);
		//-----
		this.parentTransaction = parentTransaction;
		parentTransaction.addInnerTransaction(this);
		transactionListener = parentTransaction.transactionListener;
		//La transaction démarre
		transactionListener.onTransactionStart();
	}

	//==========================================================================
	//=========================== API ==========================================
	//==========================================================================
	/**
	 * Une transaction est
	 * - soit en cours,
	 * - soit terminée.
	 * @return boolean True si la transaction est terminée.
	 */
	boolean isClosed() {
		return transactionClosed;
	}

	/** {@inheritDoc} */
	@Override
	public <R extends VTransactionResource> R getResource(final VTransactionResourceId<R> transactionResourceId) {
		checkStateStarted();
		Assertion.checkNotNull(transactionResourceId);
		//-----
		return (R) resources.get(transactionResourceId);
	}

	/**
	 * Retourne la transaction imbriquée de plus bas étage ou elle même si aucune transaction imbriquée.
	 * @return Transaction de plus bas niveau (elle même si il n'y a pas de transaction imbriquée)
	 */
	VTransactionImpl getDeepestTransaction() {
		return innerTransaction == null ? this : innerTransaction.getDeepestTransaction();
	}

	/**
	 * Ajout d'une transaction imbriquée.
	 * Vérification de l'état de la transaction imbriquée.
	 * - non null
	 * - démarrée
	 * @param newInnerTransaction Transaction imbriquée
	 */
	private void addInnerTransaction(final VTransactionImpl newInnerTransaction) {
		Assertion.checkState(innerTransaction == null, "La transaction possède déjà une transaction imbriquée");
		Assertion.checkNotNull(newInnerTransaction);
		newInnerTransaction.checkStateStarted();
		//-----
		innerTransaction = newInnerTransaction;
	}

	/**
	 * Suppression d'une transaction imbriquée.
	 * Vérification de l'état de la transaction imbriquée.
	 * - non null
	 * - terminée
	 */
	private void removeInnerTransaction() {
		Assertion.checkNotNull(innerTransaction, "La transaction ne possède pas de transaction imbriquée");
		innerTransaction.checkStateEnded();
		//-----
		innerTransaction = null;
	}

	/** {@inheritDoc} */
	@Override
	public <R extends VTransactionResource> void addResource(final VTransactionResourceId<R> id, final R resource) {
		checkStateStarted();
		Assertion.checkNotNull(resource);
		Assertion.checkNotNull(id);
		//-----
		final Object o = resources.put(id, resource);
		Assertion.checkState(o == null, "Ressource déjà enregistrée");
	}

	/** {@inheritDoc} */
	@Override
	public void commit() {
		checkStateStarted();
		//Il ne doit plus exister de transaction imbriquée
		if (innerTransaction != null) {
			throw new IllegalStateException("La transaction imbriquée doit être terminée(Commit ou Rollback) avant la transaction parente");
		}
		//-----
		final Throwable throwable = this.doEnd(false);
		if (throwable != null) {
			throw WrappedException.wrapIfNeeded(throwable, "Transaction");
		}
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() {
		final Throwable throwable = doRollback();
		if (throwable != null) {
			throw WrappedException.wrapIfNeeded(throwable, "Transaction");
		}
	}

	/**
	 * Rollback et retourne l'erreur sans la lancer (throw)
	 * @return Erreur de rollback (null la plupart du temps)
	 */
	private Throwable doRollback() {
		if (isClosed()) {
			//Si la transaction est (déjà) fermée on ne fait rien
			return null;
		}
		//Si la transaction n'est pas déjà terminée
		//alors on la rollback réellement.
		//-----
		Throwable throwable = this.doEnd(true);

		if (innerTransaction != null) {
			//Si il existe une transaction imbriquée non terminée
			//alors on note qu'il existe une erreur et on la rollback
			innerTransaction.doRollback();
			throwable = new IllegalStateException("La transaction imbriquée doit être terminée(Commit ou Rollback) avant la transaction parente");
		}
		return throwable;
	}

	//==========================================================================
	//=========================PRIVATE==========================================
	//==========================================================================
	/**
	 * Vérifie que la transaction est démarrée
	 */
	private void checkStateStarted() {
		if (isClosed()) {
			throw new IllegalStateException("La transaction n'est plus dans l'état démarré");
		}
	}

	/**
	 * Vérifie que la transaction est terminée
	 */
	private void checkStateEnded() {
		if (!isClosed()) {
			throw new IllegalStateException("La transaction n'est plus dans l'état terminé");
		}
	}

	/**
	 * Termine la transaction.
	 * L'exception la plus grave (la première survenue dans la finalisation des ressources)
	 * est retournée.
	 * @param rollback Si Rollback, sinon Commit.
	 * @return L'exception à lancer.
	 */
	private Throwable doEnd(final boolean rollback) {
		//Changement d'état
		transactionClosed = true;

		Throwable firstThrowable = null;
		if (!resources.isEmpty()) {
			//Il existe des ressources
			firstThrowable = doEndResources(rollback);
		}

		if (parentTransaction != null) {
			//Lors de la clôture d'une transaction imbriquée,
			//on la supprime de la transaction parente.
			parentTransaction.removeInnerTransaction();
		}
		//Fin de la transaction, si firstThrowable!=null alors on a rollbacké tout ou partie des resources
		transactionListener.onTransactionFinish(rollback || firstThrowable != null, System.currentTimeMillis() - start);
		return firstThrowable;
	}

	private Throwable doEndResources(final boolean rollback) {
		Throwable firstThrowable = null;
		boolean shouldRollback = rollback;
		//On traite les ressources par ordre de priorité
		for (final VTransactionResourceId<?> id : getOrderedListByPriority()) {
			final VTransactionResource ktr = resources.remove(id);
			//On termine toutes les resources utilisées en les otant de la map.
			Assertion.checkNotNull(ktr);
			final Throwable throwable = doEnd(ktr, shouldRollback);
			if (throwable != null) {
				shouldRollback = true;
				if (firstThrowable == null) {
					firstThrowable = throwable;
				} else {
					firstThrowable.addSuppressed(throwable);
				}
			}
		}
		return firstThrowable;
	}

	//=========================================================================
	//=============TRI de la liste des id de ressouces par priorité============
	//=========================================================================
	private List<VTransactionResourceId<?>> getOrderedListByPriority() {
		//On termine les ressources dans l'ordre DEFAULT, A, B...F
		final List<VTransactionResourceId<?>> list = new ArrayList<>(resources.size());

		populate(list, VTransactionResourceId.Priority.TOP);
		populate(list, VTransactionResourceId.Priority.NORMAL);
		populate(list, VTransactionResourceId.Priority.LOW);
		return list;
	}

	private void populate(final List<VTransactionResourceId<?>> list, final VTransactionResourceId.Priority priority) {
		// Ajout des ressources ayant une ceraine priorité à la liste.
		for (final VTransactionResourceId<?> id : resources.keySet()) {
			if (id.getPriority().equals(priority)) {
				list.add(id);
			}
		}
	}

	//=========================================================================

	/**
	 * Termine la transaction pour une ressource.
	 * La première exception (considérée comme la plus grave est retournée).
	 * @param resource Ressource transactionnelle.
	 * @param rollback Si vrai annule, sinon valide.
	 * @return Exception à lancer.
	 */
	private static Throwable doEnd(final VTransactionResource resource, final boolean rollback) {
		Assertion.checkNotNull(resource);
		//-----
		Throwable throwable = null;
		//autoCloseableResource is use to call release() in a finally/suppressedException block
		try (AutoCloseableResource autoCloseableResource = new AutoCloseableResource(resource)) {
			if (rollback) {
				autoCloseableResource.rollback();
			} else {
				if (resource instanceof VTransaction) {
					//Si la ressource est elle même une transaction, elle ne doit pas etre commitée de cette facon implicite
					autoCloseableResource.rollback();
					throw new IllegalStateException("La transaction incluse dans la transaction courante n'a pas été commité correctement");
				}
				autoCloseableResource.commit();
			}
		} catch (final Throwable t) {
			//we catch Throwable in order to handle all ressources even if one of them throw an error
			throwable = t;
		}
		return throwable;
	}

	/** {@inheritDoc} */
	@Override
	public void close() {
		try {
			rollback();
		} finally {
			final boolean isAutonomous = parentTransaction != null;
			if (!isAutonomous) {
				//C'est uniquement lors de la clôture de la transaction racine qu'on la supprime du threadLocal.
				CURRENT_THREAD_LOCAL_TRANSACTION.remove();
			}
		}
	}

	//==========================================================================
	//=========================PRIVATE==========================================
	//==========================================================================

	private static class AutoCloseableResource implements AutoCloseable {
		private final VTransactionResource innerResource;

		AutoCloseableResource(final VTransactionResource innerResource) {
			this.innerResource = innerResource;
		}

		void commit() throws Exception {
			innerResource.commit();
		}

		void rollback() throws Exception {
			innerResource.rollback();
		}

		@Override
		public void close() throws Exception {
			innerResource.release();
		}
	}

	/**
	 * Retourne la transaction courante de plus haut niveau.
	 * - jamais closed
	 * - peut être null
	 * @return VTransaction
	 */
	static VTransactionImpl getLocalCurrentTransaction() {
		VTransactionImpl transaction = CURRENT_THREAD_LOCAL_TRANSACTION.get();
		//Si la transaction courante est finie on ne la retourne pas.
		if (transaction != null && transaction.isClosed()) {
			transaction = null;
		}
		return transaction;
	}
}
