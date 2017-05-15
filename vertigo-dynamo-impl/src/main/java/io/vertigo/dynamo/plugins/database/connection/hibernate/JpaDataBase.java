/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.database.connection.hibernate;

import javax.persistence.EntityManagerFactory;

import io.vertigo.dynamo.database.vendor.SqlDataBase;
import io.vertigo.dynamo.database.vendor.SqlDialect;
import io.vertigo.dynamo.database.vendor.SqlExceptionHandler;
import io.vertigo.dynamo.database.vendor.SqlMapping;
import io.vertigo.dynamo.transaction.VTransaction;
import io.vertigo.dynamo.transaction.VTransactionResourceId;
import io.vertigo.lang.Assertion;

/**
 * Gestion de la base de données Hibernate.
 *
 * @author npiedeloup
 */
public final class JpaDataBase implements SqlDataBase {
	//This Resource must be commited AFTER the KConnection ones. The release of EntityManager close the DB Connection and KConnection can't be commited anymore
	private static final VTransactionResourceId JPA_RESOURCE_ID = new VTransactionResourceId(VTransactionResourceId.Priority.NORMAL, "Jpa");

	private final SqlDataBase innerDataBase;
	private final EntityManagerFactory entityManagerFactory;

	/**
	 * Constructor.
	 * @param innerDataBase Base sous jacente
	 * @param entityManagerFactory entityManagerFactory
	 */
	JpaDataBase(final SqlDataBase innerDataBase, final EntityManagerFactory entityManagerFactory) {
		Assertion.checkNotNull(innerDataBase);
		Assertion.checkNotNull(entityManagerFactory);
		//-----
		this.innerDataBase = innerDataBase;
		this.entityManagerFactory = entityManagerFactory;
	}

	/** {@inheritDoc} */
	@Override
	public SqlExceptionHandler getSqlExceptionHandler() {
		return innerDataBase.getSqlExceptionHandler();
	}

	/** {@inheritDoc} */
	@Override
	public SqlMapping getSqlMapping() {
		return innerDataBase.getSqlMapping();
	}

	@Override
	public SqlDialect getSqlDialect() {
		return innerDataBase.getSqlDialect();
	}

	/**
	 * récupère la ressource JPA de la transaction et la créé si nécessaire.
	 * @param transaction Transaction courante
	 * @return ResourceJpa de la transaction, elle est crée si nécessaire.
	 * */
	public JpaResource obtainJpaResource(final VTransaction transaction) {
		JpaResource resource = JpaResource.class.cast(transaction.getResource(JPA_RESOURCE_ID));

		if (resource == null) {
			// Si aucune ressource de type JPA existe sur la transaction, on la créé
			resource = new JpaResource(entityManagerFactory);
			transaction.addResource(JPA_RESOURCE_ID, resource);
		}
		return resource;
	}

}
