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
import io.vertigo.dynamo.environment.java.JavaParserStereotypesTest;
import io.vertigo.dynamo.environment.oom.OOMAATest;
import io.vertigo.dynamo.environment.oom.OOMEnvironmentManagerTest;
import io.vertigo.dynamo.environment.oom.OOMParserAATest;
import io.vertigo.dynamo.environment.oom.OOMParserIdentifiersTest;
import io.vertigo.dynamo.environment.oom.OOMParserTest;
import io.vertigo.dynamo.environment.oom.OOMTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslDefinitionBodyRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslDefinitionEntryRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslDefinitionRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslPackageRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslPropertyEntryRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslWordListRuleTest;
import io.vertigo.dynamo.environment.splittedmodules.SplittedModulesEnvironmentManagerTest;
import io.vertigo.dynamo.file.FileManagerTest;
import io.vertigo.dynamo.kvstore.berkeley.BerkeleyKVStoreManagerTest;
import io.vertigo.dynamo.kvstore.delayedmemory.DelayedMemoryKVStoreManagerTest;
import io.vertigo.dynamo.search.dynamic.SearchManagerDynaFieldsTest;
import io.vertigo.dynamo.search.multiindex.SearchManagerMultiIndexTest;
import io.vertigo.dynamo.search.standard.SearchManagerTest;
import io.vertigo.dynamo.search.withstore.SearchManagerStoreTest;
import io.vertigo.dynamo.store.cache.CachedStoreManagerTest;
import io.vertigo.dynamo.store.direct.StoreManagerTest;
import io.vertigo.dynamo.store.jpa.JpaStoreManagerTest;
import io.vertigo.dynamo.store.multistore.MultiStoreManagerTest;
import io.vertigo.dynamo.task.TaskManagerTest;
import io.vertigo.dynamo.task.x.TaskEngineSelectDynamicTest;
import io.vertigo.dynamo.transaction.VTransactionBeforeAfterCommitTest;
import io.vertigo.dynamo.transaction.VTransactionManagerTest;
import io.vertigo.dynamox.search.DslListFilterBuilderTest;

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
		JavaParserStereotypesTest.class,
		JavaEnvironmentManagerTest.class,
		//--OOM
		OOMAATest.class, OOMEnvironmentManagerTest.class, OOMTest.class, OOMParserTest.class, OOMParserAATest.class, OOMParserIdentifiersTest.class,
		EAXmiAATest.class, EAXmiEnvironmentManagerTest.class, EAXmiTest.class, EAXmiTestParser.class, EAXmiTestParserAA.class, EAXmiTestParserIdentifiers.class,
		SplittedModulesEnvironmentManagerTest.class,
		//--
		//--collections
		io.vertigo.dynamo.collections.javaconfig.CollectionsManagerTest.class,
		io.vertigo.dynamo.collections.xmlconfig.CollectionsManagerTest.class,
		FacetManagerTest.class,
		//--database
		DataBaseManagerTest.class,
		//--domain
		ConstraintTest.class, FormatterTest.class, DomainManagerTest.class,
		//--file
		FileManagerTest.class,
		//--kvdatastore
		DelayedMemoryKVStoreManagerTest.class,
		BerkeleyKVStoreManagerTest.class,
		//--persistence
		CachedStoreManagerTest.class,
		StoreManagerTest.class,
		JpaStoreManagerTest.class,
		MultiStoreManagerTest.class,

		//--task
		TaskManagerTest.class,
		TaskEngineSelectDynamicTest.class,

		//--transaction
		VTransactionManagerTest.class,
		VTransactionBeforeAfterCommitTest.class,

		//Rule
		DslDefinitionRuleTest.class, DslPackageRuleTest.class, DslDefinitionBodyRuleTest.class,
		DslWordListRuleTest.class, DslPropertyEntryRuleTest.class, DslDefinitionEntryRuleTest.class,

		//Search
		SearchManagerDynaFieldsTest.class,
		SearchManagerMultiIndexTest.class,
		SearchManagerTest.class,
		SearchManagerStoreTest.class,
		DslListFilterBuilderTest.class,
})
public final class DynamoTestSuite {
	//
}
