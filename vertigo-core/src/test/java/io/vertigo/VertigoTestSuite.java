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
package io.vertigo;

import io.vertigo.commons.config.hierarchy.ConfigManagerTest;
import io.vertigo.commons.config.multi.MultiConfigManagerTest;
import io.vertigo.commons.config.properties.PropertiesConfigManagerTest;
import io.vertigo.commons.config.xml.XmlConfigManagerTest;
import io.vertigo.commons.locale.LocaleManagerTest;
import io.vertigo.commons.resource.ResourceManagerTest;
import io.vertigo.core.aop.AspectTest;
import io.vertigo.core.config.AppConfigTest;
import io.vertigo.core.di.injector.InjectorTest;
import io.vertigo.core.di.reactor.ReactorTest;
import io.vertigo.core.spaces.component.ComponentSpace2Test;
import io.vertigo.core.spaces.component.ComponentSpace3Test;
import io.vertigo.core.spaces.component.ComponentSpaceTest;
import io.vertigo.core.spaces.definition.DefinitionSpaceTest;
import io.vertigo.lang.AssertionTest;
import io.vertigo.lang.OptionTest;
import io.vertigo.lang.TuplesTest;
import io.vertigo.util.BeanUtilTest;
import io.vertigo.util.ClassUtilTest;
import io.vertigo.util.DateQueryParserUtilTest;
import io.vertigo.util.DateUtilTest;
import io.vertigo.util.MapBuilderTest;
import io.vertigo.util.StringUtilTest;

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
		//LocaleManager
		LocaleManagerTest.class,
		//core
		DefinitionSpaceTest.class,
		ComponentSpaceTest.class,
		ComponentSpace2Test.class,
		ComponentSpace3Test.class,
		InjectorTest.class,
		ReactorTest.class,
		//boot
		AppConfigTest.class,
		AspectTest.class,
		//lang
		AssertionTest.class,
		OptionTest.class,
		TuplesTest.class,
		//util
		BeanUtilTest.class,
		ClassUtilTest.class,
		DateQueryParserUtilTest.class,
		DateUtilTest.class,
		StringUtilTest.class,
		MapBuilderTest.class,

		//--config
		ConfigManagerTest.class,
		MultiConfigManagerTest.class,
		PropertiesConfigManagerTest.class,
		XmlConfigManagerTest.class,
		//--resource
		ResourceManagerTest.class,

})
public final class VertigoTestSuite {
	//
}
