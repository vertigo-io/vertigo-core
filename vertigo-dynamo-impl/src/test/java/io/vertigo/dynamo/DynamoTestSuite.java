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
package io.vertigo.dynamo;

import org.junit.platform.runner.JUnitPlatform;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.runner.RunWith;

import io.vertigo.dynamo.collections.CollectionsManagerTest;
import io.vertigo.dynamo.collections.FacetManagerTest;
import io.vertigo.dynamo.criteria.predicate.PredicateCriteriaTest;
import io.vertigo.dynamo.criteria.sql.SqlCriteriaTest;
import io.vertigo.dynamo.domain.DataTypeTest;
import io.vertigo.dynamo.domain.constraint.ConstraintTest;
import io.vertigo.dynamo.domain.formatter.BooleanFormatterTest;
import io.vertigo.dynamo.domain.formatter.DateFormatterTest;
import io.vertigo.dynamo.domain.formatter.NumberFormatterTest;
import io.vertigo.dynamo.domain.formatter.StringFormatterTest;
import io.vertigo.dynamo.environment.eaxmi.EAXmiEnvironmentManagerTest;
import io.vertigo.dynamo.environment.eaxmi.EAXmiTestParser;
import io.vertigo.dynamo.environment.eaxmi.EAXmiTestParserAA;
import io.vertigo.dynamo.environment.eaxmi.EAXmiTestParserIdentifiers;
import io.vertigo.dynamo.environment.java.JavaEnvironmentManagerTest;
import io.vertigo.dynamo.environment.java.JavaParserStereotypesTest;
import io.vertigo.dynamo.environment.ksp.KspEnvironmentManagerTest;
import io.vertigo.dynamo.environment.loader.EnvironmentManagerTest;
import io.vertigo.dynamo.environment.multi.MultiResourcesEnvironmentManagerTest;
import io.vertigo.dynamo.environment.oom.OOMEnvironmentManagerTest;
import io.vertigo.dynamo.environment.oom.OOMParserAATest;
import io.vertigo.dynamo.environment.oom.OOMParserIdentifiersTest;
import io.vertigo.dynamo.environment.oom.OOMParserStereotypesTest;
import io.vertigo.dynamo.environment.oom.OOMParserTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslDefinitionBodyRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslDefinitionEntryRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslDefinitionRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslPackageDeclarationRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslPropertyDeclarationRuleTest;
import io.vertigo.dynamo.environment.plugins.loaders.kpr.definition.DslWordListRuleTest;
import io.vertigo.dynamo.file.FileManagerTest;
import io.vertigo.dynamo.kvstore.berkeley.BerkeleyKVStoreManagerTest;
import io.vertigo.dynamo.kvstore.delayedmemory.DelayedMemoryKVStoreManagerTest;
import io.vertigo.dynamo.search.dynamic.SearchManagerDynaFieldsTest;
import io.vertigo.dynamo.search.multiindex.SearchManagerMultiIndexTest;
import io.vertigo.dynamo.search.standard.SearchManagerTest;
import io.vertigo.dynamo.search.withstore.SearchManagerStoreTest;
import io.vertigo.dynamo.store.cache.CachedStoreManagerTest;
import io.vertigo.dynamo.store.datastore.jpa.JpaStoreManagerTest;
import io.vertigo.dynamo.store.datastore.multistore.MultiStoreManagerTest;
import io.vertigo.dynamo.store.datastore.sql.vendor.h2.H2SqlStoreManagerTest;
import io.vertigo.dynamo.store.datastore.sql.vendor.oracle.OracleSqlStoreManagerTest;
import io.vertigo.dynamo.task.TaskManagerTest;
import io.vertigo.dynamo.task.x.TaskEngineProcBatchTest;
import io.vertigo.dynamo.task.x.TaskEngineSelectDynamicTest;
import io.vertigo.dynamo.task.x.TaskProxyTest;
import io.vertigo.dynamox.search.DslListFilterBuilderTest;

/**
 * This suite contains all the tests for 'dynamo' module.
 *
 * @author pchretien
 */
@RunWith(JUnitPlatform.class)
@SelectClasses({
		//--collections
		CollectionsManagerTest.class,
		FacetManagerTest.class,
		//--domain
		DataTypeTest.class,
		ConstraintTest.class,
		BooleanFormatterTest.class,
		DateFormatterTest.class,
		NumberFormatterTest.class,
		StringFormatterTest.class,
		//--Environment
		//----EAXMI
		EAXmiEnvironmentManagerTest.class,
		EAXmiTestParser.class,
		EAXmiTestParserAA.class,
		EAXmiTestParserIdentifiers.class,
		//----Java
		JavaParserStereotypesTest.class,
		JavaEnvironmentManagerTest.class,
		//----KSP
		KspEnvironmentManagerTest.class,
		//----Multi
		MultiResourcesEnvironmentManagerTest.class,
		//----OOM
		OOMEnvironmentManagerTest.class,
		OOMParserAATest.class,
		OOMParserIdentifiersTest.class,
		OOMParserStereotypesTest.class,
		OOMParserTest.class,
		//----Rule
		DslDefinitionBodyRuleTest.class,
		DslDefinitionEntryRuleTest.class,
		DslDefinitionRuleTest.class,
		DslPackageDeclarationRuleTest.class,
		DslWordListRuleTest.class,
		DslPropertyDeclarationRuleTest.class,
		//--file
		FileManagerTest.class,
		//--kvstore
		BerkeleyKVStoreManagerTest.class,
		DelayedMemoryKVStoreManagerTest.class,
		//--search
		SearchManagerDynaFieldsTest.class,
		SearchManagerMultiIndexTest.class,
		SearchManagerTest.class,
		SearchManagerStoreTest.class,
		//--store
		//----------cache
		CachedStoreManagerTest.class,
		//----------sql
		H2SqlStoreManagerTest.class,
		OracleSqlStoreManagerTest.class,
		//----------multistore
		MultiStoreManagerTest.class,
		//----------jpa
		JpaStoreManagerTest.class,
		//--task
		TaskManagerTest.class,
		TaskEngineSelectDynamicTest.class,
		TaskEngineProcBatchTest.class,
		TaskProxyTest.class,
		//x
		DslListFilterBuilderTest.class,

		//Criteria
		SqlCriteriaTest.class,
		PredicateCriteriaTest.class,
		//---
		EnvironmentManagerTest.class

})

public final class DynamoTestSuite {
	//
}
