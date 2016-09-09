/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.plugins.store.datastore.jpa;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.LockModeType;
import javax.persistence.PersistenceException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import org.hibernate.exception.ConstraintViolationException;

import io.vertigo.commons.analytics.AnalyticsManager;
import io.vertigo.commons.analytics.AnalyticsTracker;
import io.vertigo.dynamo.database.SqlDataBaseManager;
import io.vertigo.dynamo.database.vendor.SqlDataBase;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtDefinitionBuilder;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNNDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForNNAssociation;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForSimpleAssociation;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtListURIForCriteria;
import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.AssociationUtil;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.impl.store.datastore.DataStorePlugin;
import io.vertigo.dynamo.plugins.database.connection.hibernate.JpaDataBase;
import io.vertigo.dynamo.plugins.database.connection.hibernate.JpaResource;
import io.vertigo.dynamo.store.criteria.Criteria;
import io.vertigo.dynamo.store.criteria.FilterCriteria;
import io.vertigo.dynamo.store.criteria.FilterCriteriaBuilder;
import io.vertigo.dynamo.transaction.VTransaction;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.StringUtil;

/**
 * Implémentation d'un Store Jpa.
 *
 * @author  pchretien, npiedeloup
 */
public final class JpaDataStorePlugin implements DataStorePlugin {
	private static final int MAX_TASK_SPECIFIC_NAME_LENGTH = 40;
	private static final String DEFAULT_CONNECTION_NAME = "main";
	/**
	 * Identifiant de ressource FileSystem par défaut.
	 */
	private static final FilterCriteria<?> EMPTY_FILTER_CRITERIA = new FilterCriteriaBuilder<>().build();

	private final String dataSpace;
	private final String connectionName;
	private final VTransactionManager transactionManager;
	private final SqlDataBaseManager dataBaseManager;
	private final AnalyticsManager analyticsManager;

	/**
	 * Constructor.
	 * @param nameOption the name of the dataSpace (optional)
	 * @param connectionName Connection name
	 * @param transactionManager Transaction manager
	 * @param dataBaseManager  Database manager
	 * @param analyticsManager  Analytics manager
	 */
	@Inject
	public JpaDataStorePlugin(@Named("name") final Optional<String> nameOption, @Named("connectionName") final Optional<String> connectionName, final VTransactionManager transactionManager, final SqlDataBaseManager dataBaseManager, final AnalyticsManager analyticsManager) {
		Assertion.checkNotNull(nameOption);
		Assertion.checkNotNull(connectionName);
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(dataBaseManager);
		//-----
		dataSpace = nameOption.orElse(DtDefinitionBuilder.DEFAULT_DATA_SPACE);
		this.connectionName = connectionName.orElse(DEFAULT_CONNECTION_NAME);
		this.transactionManager = transactionManager;
		this.dataBaseManager = dataBaseManager;
		this.analyticsManager = analyticsManager;
	}

	/** {@inheritDoc} */
	@Override
	public String getDataSpace() {
		return dataSpace;
	}

	/** {@inheritDoc} */
	@Override
	public String getConnectionName() {
		return connectionName;
	}

	private EntityManager getEntityManager() {
		return obtainJpaResource().getEntityManager();
	}

	private JpaResource obtainJpaResource() {
		final SqlDataBase dataBase = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).getDataBase();
		Assertion.checkState(dataBase instanceof JpaDataBase, "DataBase must be a JpaDataBase (current:{0}).", dataBase.getClass());
		return ((JpaDataBase) dataBase).obtainJpaResource(getCurrentTransaction());
	}

	/** récupère la transaction courante. */
	private VTransaction getCurrentTransaction() {
		return transactionManager.getCurrentTransaction();
	}

	private <E extends Entity> E loadWithoutClear(final URI<E> uri) {
		final String serviceName = "Jpa:find " + uri.getDefinition().getName();
		try (AnalyticsTracker tracker = analyticsManager.startLogTracker("Jpa", serviceName)) {
			final Class<E> objectClass = (Class<E>) ClassUtil.classForName(uri.<DtDefinition> getDefinition().getClassCanonicalName());
			final E result = getEntityManager().find(objectClass, uri.getId());
			tracker.setMeasure("nbSelectedRow", result != null ? 1 : 0)
					.markAsSucceeded();
			return result;
			//Objet null géré par le dataStore
		}
	}

	/** {@inheritDoc} */
	@Override
	public int count(final DtDefinition dtDefinition) {
		//Adds the where condition to the end of the query
		final String tableName = getTableName(dtDefinition);
		final Query query = getEntityManager().createQuery("select count(*) from " + tableName + " t");
		final Long count = (Long) query.getSingleResult();
		return count.intValue();
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForCriteria<E> uri) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(uri);
		//-----
		final Criteria<E> criteria = uri.getCriteria();
		final Integer maxRows = uri.getMaxRows();
		Assertion.checkArgument(criteria == null || criteria instanceof FilterCriteria<?>, "Ce store ne gére que les FilterCriteria");
		//-----
		final FilterCriteria<E> filterCriteria = (FilterCriteria<E>) (criteria == null ? EMPTY_FILTER_CRITERIA : criteria);
		return doLoadList(dtDefinition, filterCriteria, maxRows);
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E read(final DtDefinition dtDefinition, final URI<E> uri) {
		final E entity = this.<E> loadWithoutClear(uri);
		//On détache le DTO du contexte jpa
		//De cette façon on interdit à jpa d'utiliser son cache
		getEntityManager().clear();
		return entity;
	}

	private <E extends Entity> DtList<E> doLoadList(final DtDefinition dtDefinition, final FilterCriteria<E> filterCriteria, final Integer maxRows) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(filterCriteria);
		//-----
		//Il faudrait vérifier que les filtres portent tous sur des champs du DT.
		//-----
		final String serviceName = "Jpa:find " + getListTaskName(getTableName(dtDefinition), filterCriteria);
		try (AnalyticsTracker tracker = analyticsManager.startLogTracker("Jpa", serviceName)) {
			final Class<E> resultClass = (Class<E>) ClassUtil.classForName(dtDefinition.getClassCanonicalName());
			final String tableName = getTableName(dtDefinition);
			final String request = createLoadAllLikeQuery(tableName, filterCriteria);

			final TypedQuery<E> q = getEntityManager().createQuery(request, resultClass);
			//IN, obligatoire
			for (final Map.Entry<String, Object> filterEntry : filterCriteria.getFilterMap().entrySet()) {
				q.setParameter(filterEntry.getKey(), filterEntry.getValue());
			}
			for (final Map.Entry<String, String> prefixEntry : filterCriteria.getPrefixMap().entrySet()) {
				q.setParameter(prefixEntry.getKey(), prefixEntry.getValue());
			}
			if (maxRows != null) {
				q.setMaxResults(maxRows);
			}

			final List<E> results = q.getResultList();
			final DtList<E> dtc = new DtList<>(dtDefinition);
			dtc.addAll(results);
			tracker.setMeasure("nbSelectedRow", dtc.size())
					.markAsSucceeded();
			return dtc;
		}
	}

	private static String getTableName(final DtDefinition dtDefinition) {
		// Warning jSQL is "almost case-insensitive"; that's why we have to keep the case of java objects
		return dtDefinition.getFragment().orElse(dtDefinition).getClassSimpleName();
	}

	private static <E extends Entity> String getListTaskName(final String tableName, final FilterCriteria<E> filter) {
		final StringBuilder sb = new StringBuilder()
				.append("LIST_")
				.append(tableName);
		//s'il y a plus d'un champs : on nomme _BY_CRITERIA, sinon le nom sera trop long
		if (filter.getFilterMap().size() + filter.getPrefixMap().size() <= 1) {
			String sep = "_BY_";
			for (final String filterName : filter.getFilterMap().keySet()) {
				sb.append(sep);
				sb.append(filterName);
				sep = "_AND_";
			}
			sep = "_PREFIXED_ON_";
			for (final String filterName : filter.getPrefixMap().keySet()) {
				sb.append(sep);
				sb.append(filterName);
				sep = "_AND_";
			}
		} else {
			sb.append("_BY_CRITERIA");
		}
		String result = sb.toString();
		if (result.length() > MAX_TASK_SPECIFIC_NAME_LENGTH) {
			result = result.substring(result.length() - MAX_TASK_SPECIFIC_NAME_LENGTH);
		}

		return result;
	}

	private static <E extends Entity> String createLoadAllLikeQuery(final String tableName, final FilterCriteria<E> filterCriteria) {
		final StringBuilder request = new StringBuilder("select t from ").append(tableName).append(" t");
		String sep = " where ";
		for (final String fieldName : filterCriteria.getFilterMap().keySet()) {
			final String camelFieldName = StringUtil.constToLowerCamelCase(fieldName);
			request.append(sep);
			request.append("t.").append(camelFieldName);
			if (filterCriteria.getFilterMap().get(fieldName) != null) {
				request.append(" = :").append(fieldName);
			} else {
				request.append(" is null");
			}
			sep = " and ";
		}
		for (final String fieldName : filterCriteria.getPrefixMap().keySet()) {
			final String camelFieldName = StringUtil.constToLowerCamelCase(fieldName);
			request.append(sep)
					.append("t.")
					.append(camelFieldName)
					.append(" like concat(:")
					.append(fieldName)
					.append(",'%')");
			sep = " and ";
		}
		return request.toString();
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForSimpleAssociation dtcUri) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(dtcUri);
		//-----
		final DtField fkField = dtcUri.getAssociationDefinition().getFKField();
		final Object value = dtcUri.getSource().getId();

		final FilterCriteria<E> filterCriteria = new FilterCriteriaBuilder<E>()
				.addFilter(fkField.getName(), value)
				.build();
		return doLoadList(dtDefinition, filterCriteria, null);
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> DtList<E> findAll(final DtDefinition dtDefinition, final DtListURIForNNAssociation dtcUri) {
		Assertion.checkNotNull(dtDefinition);
		Assertion.checkNotNull(dtcUri);
		//-----
		final String tableName = getTableName(dtDefinition);

		final String taskName = "N_N_LIST_" + tableName + "_BY_URI";
		final String serviceName = "Jpa:find " + taskName;
		try (AnalyticsTracker tracker = analyticsManager.startLogTracker("Jpa", serviceName)) {
			final Class<E> resultClass = (Class<E>) ClassUtil.classForName(dtDefinition.getClassCanonicalName());
			//PK de la DtList recherchée
			final String idFieldName = dtDefinition.getIdField().get().getName();
			//FK dans la table nn correspondant à la collection recherchée. (clé de jointure ).
			final AssociationNNDefinition associationNNDefinition = dtcUri.getAssociationDefinition();
			final String joinTableName = associationNNDefinition.getTableName();
			final DtDefinition joinDtDefinition = AssociationUtil.getAssociationNode(associationNNDefinition, dtcUri.getRoleName()).getDtDefinition();
			final DtField joinDtField = joinDtDefinition.getIdField().get();

			//La condition s'applique sur l'autre noeud de la relation (par rapport à la collection attendue)
			final AssociationNode associationNode = AssociationUtil.getAssociationNodeTarget(associationNNDefinition, dtcUri.getRoleName());
			final DtField fkField = associationNode.getDtDefinition().getIdField().get();
			final String fkFieldName = fkField.getName();

			final StringBuilder request = new StringBuilder(" select t.* from ")
					.append(dtDefinition.getLocalName())
					.append(" t")
					//On établit une jointure fermée entre la pk et la fk de la collection recherchée.
					.append(" join ").append(joinTableName).append(" j on j.").append(joinDtField.getName()).append(" = t.").append(idFieldName)
					//Condition de la recherche
					.append(" where j.").append(fkFieldName).append(" = :").append(fkFieldName);

			final URI uri = dtcUri.getSource();

			final Query q = getEntityManager().createNativeQuery(request.toString(), resultClass);
			q.setParameter(fkFieldName, uri.getId());

			final List<E> results = q.getResultList();
			final DtList<E> dtc = new DtList<>(dtDefinition);
			dtc.addAll(results);
			tracker.setMeasure("nbSelectedRow", dtc.size())
					.markAsSucceeded();
			return dtc;
		}
	}

	@Override
	public void create(final DtDefinition dtDefinition, final Entity entity) {
		put("Jpa:create", entity, true);
	}

	@Override
	public void update(final DtDefinition dtDefinition, final Entity entity) {
		put("Jpa:update", entity, false);
	}

	private void put(final String prefixServiceName, final Entity entity, final boolean persist) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(entity);
		final String serviceName = prefixServiceName + dtDefinition.getName();

		try (AnalyticsTracker tracker = analyticsManager.startLogTracker("Jpa", serviceName)) {
			final EntityManager entityManager = getEntityManager();
			if (persist) { //si pas de PK exception
				//Si l'objet est en cours de création (pk null)
				//(l'objet n'est pas géré par jpa car les objets sont toujours en mode détaché :
				//sinon on ferait persist aussi si em.contains(dto)).
				entityManager.persist(entity);
			} else {
				entityManager.merge(entity);
			}
			entityManager.flush();
			entityManager.clear();
			tracker.setMeasure("nbModifiedRow", 1)
					.markAsSucceeded();
		} catch (final PersistenceException pse) {
			//Gère les erreurs d'exécution JDBC.
			handlePersistenceException(pse);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void delete(final DtDefinition dtDefinition, final URI uri) {
		final String serviceName = "Jpa:remove " + uri.getDefinition().getName();

		try (AnalyticsTracker tracker = analyticsManager.startLogTracker("Jpa", serviceName)) {
			final Object dto = loadWithoutClear(uri);
			if (dto == null) {
				throw new VSystemException("Aucune ligne supprimée");
			}
			getEntityManager().remove(dto);
			getEntityManager().flush();
			getEntityManager().clear();
			tracker.setMeasure("nbModifiedRow", 1)
					.markAsSucceeded();
		} catch (final PersistenceException pse) {
			//Gère les erreurs d'exécution JDBC.
			handlePersistenceException(pse);
		}
	}

	/** {@inheritDoc} */
	@Override
	public <E extends Entity> E readForUpdate(final DtDefinition dtDefinition, final URI<?> uri) {
		final String serviceName = "Jpa:lock " + uri.getDefinition().getName();

		try (AnalyticsTracker tracker = analyticsManager.startLogTracker("Jpa", serviceName)) {
			final Class<Entity> objectClass = (Class<Entity>) ClassUtil.classForName(uri.<DtDefinition> getDefinition().getClassCanonicalName());
			final E result = (E) getEntityManager().find(objectClass, uri.getId(), LockModeType.PESSIMISTIC_WRITE);
			tracker.setMeasure("nbSelectedRow", result != null ? 1 : 0)
					.markAsSucceeded();
			return result;
		}
	}

	/**
	 * Gestion centralisée des exceptions SQL.
	 * @param pse Exception SQL
	 */
	private void handlePersistenceException(final PersistenceException pse) {
		Throwable t = pse.getCause();
		// On ne traite que les violations de contraintes
		if (!(t instanceof ConstraintViolationException)) {
			throw pse;
		}
		final ConstraintViolationException cve = (ConstraintViolationException) t;
		// On récupère l'erreur SQL associé
		t = cve.getCause();
		if (!(t instanceof SQLException)) {
			throw pse;
		}
		final SQLException sqle = (SQLException) t;
		final SqlDataBase dataBase = dataBaseManager.getConnectionProvider(SqlDataBaseManager.MAIN_CONNECTION_PROVIDER_NAME).getDataBase();
		dataBase.getSqlExceptionHandler().handleSQLException(sqle, null);
	}

}
