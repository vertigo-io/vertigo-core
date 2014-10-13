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
package io.vertigo.dynamo.search.dynamic;

import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.dynamo.search.AbstractSearchManagerTest;
import io.vertigo.dynamo.search.IndexFieldNameResolver;

import java.util.HashMap;
import java.util.Map;

/**
 * @author  npiedeloup
 */
public final class SearchManagerDynaFieldsTest extends AbstractSearchManagerTest {
	//Index
	private static final String IDX_DYNA_CAR = "IDX_DYNA_CAR";

	/**{@inheritDoc}*/
	@Override
	protected void doSetUp() {

		//attention : la première utilisation de l'index fige la définition des types
		init(IDX_DYNA_CAR);
		//---------------------------------------------------------------------
		final Map<String, String> indexFieldsMap = new HashMap<>();
		for (final DtField dtField : carIndexDefinition.getIndexDtDefinition().getFields()) {
			String indexType = dtField.getDomain().getProperties().getValue(DtProperty.INDEX_TYPE);
			if (indexType == null) {
				indexType = dtField.getDomain().getDataType().name().toLowerCase();
			}
			indexFieldsMap.put(dtField.getName(), dtField.getName() + "_DYN_" + indexType);
		}
		searchManager.getSearchServices().registerIndexFieldNameResolver(carIndexDefinition, new IndexFieldNameResolver(indexFieldsMap));

	}
}
