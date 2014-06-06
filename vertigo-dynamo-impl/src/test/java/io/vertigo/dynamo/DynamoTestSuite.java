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
package io.vertigo.dynamo;

import io.vertigo.dynamo.collections.CollectionsManagerTest;
import io.vertigo.dynamo.collections.facet.FacetManagerTest;
import io.vertigo.dynamo.database.DataBaseManagerTest;
import io.vertigo.dynamo.domain.DomainManagerTest;
import io.vertigo.dynamo.domain.constraint.ConstraintTest;
import io.vertigo.dynamo.domain.formatter.FormatterTest;
import io.vertigo.dynamo.environment.eaxmi.EAXmiAATest;
import io.vertigo.dynamo.environment.eaxmi.EAXmiEnvironmentManagerTest;
import io.vertigo.dynamo.environment.eaxmi.EAXmiTest;
import io.vertigo.dynamo.environment.eaxmi.EAXmiTestParser;
import io.vertigo.dynamo.environment.eaxmi.EAXmiTestParserAA;
import io.vertigo.dynamo.environment.eaxmi.EAXmiTestParserIdentifiers;
import io.vertigo.dynamo.environment.java.JavaEnvironmentManagerTest;
import io.vertigo.dynamo.environment.oom.OOMAATest;
import io.vertigo.dynamo.environment.oom.OOMEnvironmentManagerTest;
import io.vertigo.dynamo.environment.oom.OOMTest;
import io.vertigo.dynamo.environment.oom.TestParserOOM;
import io.vertigo.dynamo.environment.oom.TestParserOOMAA;
import io.vertigo.dynamo.environment.oom.TestParserOOMIdentifiers;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DefinitionBodyRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DefinitionRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.PackageRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.WordListRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.XDefinitionEntryRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.XPropertyEntryRuleTest;
import io.vertigo.dynamo.file.FileManagerTest;
import io.vertigo.dynamo.kvdatastore.KVDataStoreManagerTest;
import io.vertigo.dynamo.persistence.direct.PersistenceManagerTest;
import io.vertigo.dynamo.persistence.jpa.JpaPersistenceManagerTest;
import io.vertigo.dynamo.search.dynamic.SearchManagerDynaFieldsTest;
import io.vertigo.dynamo.search.multiindex.SearchManagerMultiIndexTest;
import io.vertigo.dynamo.search.standard.SearchManagerTest;
import io.vertigo.dynamo.task.TaskManagerTest;
import io.vertigo.dynamo.task.x.TaskEngineSelectDynamicTest;
import io.vertigo.dynamo.transaction.KTransactionManagerTest;
import io.vertigo.dynamo.work.local.WorkManagerTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
@RunWith(Suite.class)
@SuiteClasses({
//@formatter:off
		//--	
		//--
		//--
		JavaEnvironmentManagerTest.class,
		//--OOM
		OOMAATest.class, OOMEnvironmentManagerTest.class, OOMTest.class, TestParserOOM.class, TestParserOOMAA.class, TestParserOOMIdentifiers.class,
		EAXmiAATest.class, EAXmiEnvironmentManagerTest.class, EAXmiTest.class, EAXmiTestParser.class, EAXmiTestParserAA.class, EAXmiTestParserIdentifiers.class,

		//--

		//@formatter:on

		//@formatter:off
		//--collections
		CollectionsManagerTest.class, FacetManagerTest.class,
		//--database
		DataBaseManagerTest.class,
		//--domain
		ConstraintTest.class, FormatterTest.class, DomainManagerTest.class,
		//--file
		FileManagerTest.class,
		//--kvdatastore
		KVDataStoreManagerTest.class,
		//--persistence
		PersistenceManagerTest.class, JpaPersistenceManagerTest.class,
		//--task
		TaskManagerTest.class, TaskEngineSelectDynamicTest.class,

		//--transaction
		KTransactionManagerTest.class,
		
		//--work
		WorkManagerTest.class,
		//RedisWorkManagerTest.class, DistributedWorkManagerTest.class, //REST
		
		//Rule
		DefinitionRuleTest.class, PackageRuleTest.class, DefinitionBodyRuleTest.class, WordListRuleTest.class, XPropertyEntryRuleTest.class, XDefinitionEntryRuleTest.class,
		
		//Search
		SearchManagerDynaFieldsTest.class, 
		SearchManagerMultiIndexTest.class,
		SearchManagerTest.class,
//@formatter:on
})
public final class DynamoTestSuite {
	//
}
