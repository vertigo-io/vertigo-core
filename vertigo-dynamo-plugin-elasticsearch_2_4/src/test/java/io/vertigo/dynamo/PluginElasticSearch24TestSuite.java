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
package io.vertigo.dynamo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.vertigo.dynamo.collections.CollectionsManagerLucene_5_5Test;
import io.vertigo.dynamo.collections.FacetManagerLucene_5_5Test;
import io.vertigo.dynamo.search.dynamic.SearchManagerDynaFieldsElasticSearch2_4Test;
import io.vertigo.dynamo.search.multiindex.SearchManagerMultiIndexElasticSearch2_4Test;
import io.vertigo.dynamo.search.standard.SearchManagerElasticSearch2_4Test;
import io.vertigo.dynamo.search.withstore.SearchManagerStoreElasticSearch2_4Test;

/**
 * This suite contains all the tests for 'dynamo' module.
 *
 * @author pchretien
 */
@RunWith(Suite.class)
@SuiteClasses({
		//--collections
		CollectionsManagerLucene_5_5Test.class,
		FacetManagerLucene_5_5Test.class,

		//--search
		SearchManagerDynaFieldsElasticSearch2_4Test.class,
		SearchManagerMultiIndexElasticSearch2_4Test.class,
		SearchManagerElasticSearch2_4Test.class,
		SearchManagerStoreElasticSearch2_4Test.class,
})
public final class PluginElasticSearch24TestSuite {
	//
}
