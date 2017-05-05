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
package io.vertigo;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.vertigo.app.config.discovery.DiscoveryTest;
import io.vertigo.app.config.xml.AppConfig2Test;
import io.vertigo.app.config.xml2.AppConfigTest;
import io.vertigo.core.component.aop.Aspect2Test;
import io.vertigo.core.component.aop.AspectTest;
import io.vertigo.core.component.di.injector.InjectorTest;
import io.vertigo.core.component.di.reactor.ReactorTest;
import io.vertigo.core.locale.LocaleManagerTest;
import io.vertigo.core.node.SingleNodeManagerTest;
import io.vertigo.core.param.multi.MultiParamManagerTest;
import io.vertigo.core.param.properties.PropertiesParamManagerTest;
import io.vertigo.core.param.xml.XmlParamManagerTest;
import io.vertigo.core.resource.ResourceManagerTest;
import io.vertigo.core.spaces.component.ComponentSpace2Test;
import io.vertigo.core.spaces.component.ComponentSpace3Test;
import io.vertigo.core.spaces.component.ComponentSpace4Test;
import io.vertigo.core.spaces.component.ComponentSpaceTest;
import io.vertigo.core.spaces.definition.DefinitionSpaceTest;
import io.vertigo.lang.AssertionTest;
import io.vertigo.lang.TuplesTest;
import io.vertigo.util.BeanUtilTest;
import io.vertigo.util.ClassUtilTest;
import io.vertigo.util.DateQueryParserUtilTest;
import io.vertigo.util.DateUtilTest;
import io.vertigo.util.MapBuilderTest;
import io.vertigo.util.SelectorTest;
import io.vertigo.util.StringUtilTest;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
@RunWith(Suite.class)
@SuiteClasses({
		//Core
		DefinitionSpaceTest.class,
		ComponentSpaceTest.class,
		ComponentSpace2Test.class,
		ComponentSpace3Test.class,
		ComponentSpace4Test.class,
		InjectorTest.class,
		ReactorTest.class,
		//Boot
		AppConfigTest.class,
		AppConfig2Test.class,
		AspectTest.class,
		Aspect2Test.class,
		DiscoveryTest.class,
		SingleNodeManagerTest.class,
		//Lang
		AssertionTest.class,
		TuplesTest.class,
		//Util
		BeanUtilTest.class,
		ClassUtilTest.class,
		DateQueryParserUtilTest.class,
		DateUtilTest.class,
		SelectorTest.class,
		StringUtilTest.class,
		MapBuilderTest.class,
		//--Params
		MultiParamManagerTest.class,
		PropertiesParamManagerTest.class,
		XmlParamManagerTest.class,
		//--Resources
		ResourceManagerTest.class,
		//Locales
		LocaleManagerTest.class
})

public final class VertigoTestSuite {
	//
}
