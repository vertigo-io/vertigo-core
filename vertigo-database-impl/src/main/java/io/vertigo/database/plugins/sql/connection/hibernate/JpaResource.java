/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.database.plugins.sql.connection.hibernate;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;

import io.vertigo.commons.transaction.VTransactionResource;
import io.vertigo.lang.Assertion;

/**
 * Les "transactions" hibernate sont considérées comme une ressource d'une
 * transaction plus globale pouvant gérer d'autres ressources comme des mails,
 * des fichiers...
 *
 * @author pchretien
 */
public final class JpaResource implements VTransactionResource {
	private final EntityManager em;
	private final EntityTransaction tx;

	/**
	 * Constructeur.
	 *
	 * @param entityManagerFactory EntityManagerFactory
	 */
	public JpaResource(final EntityManagerFactory entityManagerFactory) {
		em = entityManagerFactory.createEntityManager(); //throw a NPE if persistence.xml declare à jta-data-source. Use à non-jta-data-source instead
		tx = em.getTransaction();
		//-----
		Assertion.checkNotNull(tx);
		Assertion.checkNotNull(em);
		//-----
		tx.begin();
	}

	/** {@inheritDoc} */
	@Override
	public void commit() {
		tx.commit();
	}

	/** {@inheritDoc} */
	@Override
	public void rollback() {
		tx.rollback();
	}

	/** {@inheritDoc} */
	@Override
	public void release() {
		em.close();
	}

	/**
	 * @return EntityManager of this resource
	 */
	public EntityManager getEntityManager() {
		return em;
	}
}
