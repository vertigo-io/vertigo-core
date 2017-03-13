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
package io.vertigo.dynamo.search.withstore;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.inject.Inject;

import io.vertigo.app.Home;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.search.SearchManager;
import io.vertigo.dynamo.search.data.domain.Car;
import io.vertigo.dynamo.search.metamodel.SearchChunk;
import io.vertigo.dynamo.search.metamodel.SearchIndexDefinition;
import io.vertigo.dynamo.search.model.SearchIndex;
import io.vertigo.dynamo.task.TaskManager;
import io.vertigo.dynamo.task.metamodel.TaskDefinition;
import io.vertigo.dynamo.task.metamodel.TaskDefinitionBuilder;
import io.vertigo.dynamo.task.model.Task;
import io.vertigo.dynamo.task.model.TaskBuilder;
import io.vertigo.dynamo.transaction.VTransactionManager;
import io.vertigo.dynamo.transaction.VTransactionWritable;
import io.vertigo.dynamox.search.AbstractSqlSearchLoader;
import io.vertigo.dynamox.task.TaskEngineSelect;

/**
 * SearchLoader of Car keyconcept, load uses StoreManager.
 * @author npiedeloup
 */
public final class CarSearchLoader extends AbstractSqlSearchLoader<Long, Car, Car> {
	private final SearchIndexDefinition indexDefinition;
	private final DefinitionSpace definitionSpace;

	/**
	 * Constructor.
	 * @param taskManager Task manager
	 * @param searchManager Search manager
	 */
	@Inject
	public CarSearchLoader(final TaskManager taskManager, final SearchManager searchManager, final VTransactionManager transactionManager) {
		super(taskManager, transactionManager);
		indexDefinition = searchManager.findIndexDefinitionByKeyConcept(Car.class);
		definitionSpace = Home.getApp().getDefinitionSpace();
	}

	/** {@inheritDoc} */
	@Override
	public List<SearchIndex<Car, Car>> loadData(final SearchChunk<Car> searchChunk) {
		try (final VTransactionWritable tx = getTransactionManager().createCurrentTransaction()) {
			final List<SearchIndex<Car, Car>> result = new ArrayList<>();
			final DtDefinition dtDefinition = DtObjectUtil.findDtDefinition(Car.class);
			for (final Car car : loadCarList(searchChunk)) {
				final URI<Car> uri = new URI<>(dtDefinition, car.getId());
				result.add(SearchIndex.createIndex(indexDefinition, uri, car));
			}
			return result;
		}
	}

	private DtList<Car> loadCarList(final SearchChunk<Car> searchChunk) {
		final TaskDefinition taskLoadCars = getTaskLoadCarList(searchChunk);

		final Task task = new TaskBuilder(taskLoadCars)
				.build();

		return getTaskManager()
				.execute(task)
				.getResult();
	}

	private TaskDefinition getTaskLoadCarList(final SearchChunk<Car> searchChunk) {
		final Domain doCarList = definitionSpace.resolve("DO_DT_CAR_DTC", Domain.class);
		final String sql = searchChunk.getAllURIs()
				.stream()
				.map(uri -> uri.getId().toString())
				.collect(Collectors.joining(", ", "select * from CAR where ID in (", ")"));

		return new TaskDefinitionBuilder("TK_LOAD_ALL_CARS")
				.withEngine(TaskEngineSelect.class)
				.withRequest(sql)
				.withPackageName(TaskEngineSelect.class.getPackage().getName())
				.withOutRequired("dtc", doCarList)
				.build();
	}
}
