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
package io.vertigo.dynamox.search;

import io.vertigo.core.Home;
import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionUtil;
import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.metamodel.SearchChunk;
import io.vertigo.dynamo.search.metamodel.SearchLoader;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.task.model.TaskResult;
import io.vertigo.dynamox.task.TaskEngineSelect;
import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import javax.inject.Inject;

public abstract class DefaultSearchLoader<P extends Serializable, S extends KeyConcept, I extends DtObject> implements
		SearchLoader<S, I> {

	private static final String DOMAIN_PREFIX = DefinitionUtil.getPrefix(Domain.class);
	private static final char SEPARATOR = Definition.SEPARATOR;
	private static final int SEARCH_CHUNK_SIZE = 500;
	private final TaskManager taskManager;

	@Inject
	public DefaultSearchLoader(final TaskManager taskManager) {
		Assertion.checkNotNull(taskManager);
		Assertion.checkNotNull(taskManager);
		// -----
		this.taskManager = taskManager;
	}

	/** {@inheritDoc} */
	@Override
	public Iterable<SearchChunk<S>> chunk(final Class<S> keyConceptClass) {
		return new Iterable<SearchChunk<S>>() {

			private final Iterator<SearchChunk<S>> iterator = new Iterator<SearchChunk<S>>() {

				private SearchChunk<S> current = null;
				private SearchChunk<S> next = null;

				/** {@inheritDoc} */
				@Override
				public boolean hasNext() {
					return hasNextChunk(keyConceptClass, next);
				}

				/** {@inheritDoc} */
				@Override
				public SearchChunk<S> next() {
					if (next == null) {
						next = nextChunk(keyConceptClass, null);
					}
					current = next;
					next = nextChunk(keyConceptClass, current);
					return current;
				}

				/** {@inheritDoc} */
				@Override
				public void remove() {
					throw new UnsupportedOperationException("This list is unmodifiable");
				}
			};

			/** {@inheritDoc} */
			@Override
			public Iterator<SearchChunk<S>> iterator() {
				return iterator;
			}
		};
	}

	private SearchChunk<S> nextChunk(final Class<S> keyConceptClass, final SearchChunk<S> previousChunck) {
		final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(keyConceptClass);
		P lastId = getLowestIdValue(dtDefinition);
		if (previousChunck != null) {
			final List<URI<S>> previousUris = previousChunck.getAllURIs();
			Assertion
					.checkState(
							!previousUris.isEmpty(),
							"No more SearchChunk for KeyConcept {0}, ensure you use Iterable pattern or call hasNext before next",
							keyConceptClass.getSimpleName());
			lastId = (P) previousUris.get(previousUris.size() - 1).getId();
		}
		// call loader service
		final List<URI<S>> uris = loadNextURI(lastId, dtDefinition);
		return new SearchChunkImpl<>(uris);
	}

	private List<URI<S>> loadNextURI(final P lastId, final DtDefinition dtDefinition) {
		final String tableName = getTableName(dtDefinition);
		final String taskName = "TK_SELECT_" + tableName + "_NEXT_SEARCH_CHUNK";
		final DtField pk = dtDefinition.getIdField().get();
		final String pkFieldName = pk.getName();
		final String request = getNextIdsSqlQuery(tableName, pkFieldName);

		final TaskDefinition taskDefinition = new TaskDefinitionBuilder(taskName)
				.withEngine(TaskEngineSelect.class)
				.withRequest(request)
				.addInAttribute(pkFieldName, pk.getDomain(), true)
				//IN, obligatoire
				.addOutAttribute("dtc", Home.getDefinitionSpace().resolve(DOMAIN_PREFIX + SEPARATOR + dtDefinition.getName() + "_DTC", Domain.class), true)//obligatoire
				.build();

		final Task task = new TaskBuilder(taskDefinition)
				.addValue(pkFieldName, lastId)
				.build();
		final TaskResult taskResult = process(task);
		final DtList<S> resultDtc = getDtList(taskResult);
		final List<URI<S>> uris = new ArrayList<>(SEARCH_CHUNK_SIZE);
		for (final S dto : resultDtc) {
			uris.add(new URI(dtDefinition, DtObjectUtil.getId(dto)));
		}
		return uris;
	}

	protected String getNextIdsSqlQuery(final String tableName, final String pkFieldName) {
		final StringBuilder request = new StringBuilder()
				.append(" select " + pkFieldName + " from ")
				.append(tableName)
				.append(" where ").append(pkFieldName).append(" > #").append(pkFieldName).append('#');
		final String sqlQueryFilter = getSqlQueryFilter();
		Assertion.checkNotNull(sqlQueryFilter, "getSqlQueryFilter can't be null");
		if (!sqlQueryFilter.isEmpty()) {
			request.append("and (").append(sqlQueryFilter).append(")");
		}
		request.append(" order by " + pkFieldName + " ASC")
				.append(" limit " + SEARCH_CHUNK_SIZE); //Attention : non compatible avec toutes les bases
		return request.toString();
	}

	protected String getSqlQueryFilter() {
		//nothing, but overrideable
		return "";
	}

	/**
	 * Exécution d'une tache de façon synchrone.
	 *
	 * @param task Tache à executer.
	 * @return TaskResult de la tache
	 */
	protected final TaskResult process(final Task task) {
		return taskManager.execute(task);
	}

	private static <D extends DtObject> DtList<D> getDtList(final TaskResult taskResult) {
		return taskResult.getValue("dtc");
	}

	private P getLowestIdValue(final DtDefinition dtDefinition) {
		final DtField pkField = dtDefinition.getIdField().get();
		final DataType pkDataType = pkField.getDomain().getDataType();
		P pkValue;
		switch (pkDataType) {
			case Integer:
				pkValue = (P) Integer.valueOf(-1);
				break;
			case Long:
				pkValue = (P) Long.valueOf(-1);
				break;
			case String:
				pkValue = (P) "";
				break;
			case BigDecimal:
			case DataStream:
			case Boolean:
			case Double:
			case Date:
			case DtList:
			case DtObject:
			default:
				throw new IllegalArgumentException("Type's PK " + pkDataType.name() + " of "
						+ dtDefinition.getClassSimpleName() + " is not supported, prefer int, long or String PK.");
		}
		return pkValue;
	}

	private boolean hasNextChunk(final Class<S> keyConceptClass, final SearchChunk<S> currentChunck) {
		// il y a une suite, si on a pas commencé, ou s'il y avait des résultats la dernière fois.
		return currentChunck == null || !currentChunck.getAllURIs().isEmpty();
	}

	/**
	 * Nom de la table en fonction de la définition du DT mappé.
	 *
	 * @param dtDefinition Définition du DT mappé
	 * @return Nom de la table
	 */
	protected static final String getTableName(final DtDefinition dtDefinition) {
		return dtDefinition.getLocalName();
	}

	public static class SearchChunkImpl<S extends KeyConcept> implements SearchChunk<S> {

		private final List<URI<S>> uris;

		/**
		 * @param uris Liste des uris du chunk
		 */
		public SearchChunkImpl(final List<URI<S>> uris) {
			Assertion.checkNotNull(uris);
			// ----
			this.uris = Collections.unmodifiableList(uris); // pas de clone pour l'instant
		}

		/** {@inheritDoc} */
		@Override
		public List<URI<S>> getAllURIs() {
			return uris;
		}
	}
}
