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

import io.vertigo.commons.locale.LocaleManagerTest;
import io.vertigo.core.component.cglib.CGLIBAspectContainerTest;
import io.vertigo.core.di.injector.InjectorTest;
import io.vertigo.core.di.reactor.ReactorTest;
import io.vertigo.core.home.componentspace.ComponentSpace2Test;
import io.vertigo.core.home.componentspace.ComponentSpace3Test;
import io.vertigo.core.home.componentspace.ComponentSpaceTest;
import io.vertigo.core.home.definitionspace.DefinitionSpaceTest;
import io.vertigo.lang.AssertionTest;
import io.vertigo.lang.TuplesTest;
import io.vertigo.util.BeanUtilTest;
import io.vertigo.util.ClassUtilTest;
import io.vertigo.util.DateQueryParserUtilTest;
import io.vertigo.util.DateUtilTest;
import io.vertigo.util.MapBuilderTest;
import io.vertigo.util.StringUtilTest;
import io.vertigo.xml.XmlComponentsTest;
import io.vertigo.xml.XmlHomeTest;

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
		XmlHomeTest.class,
		XmlComponentsTest.class,
		InjectorTest.class,
		ReactorTest.class,
		//-----
		AssertionTest.class,
		TuplesTest.class,
		//-----
		BeanUtilTest.class,
		ClassUtilTest.class,
		DateQueryParserUtilTest.class,
		DateUtilTest.class,
		StringUtilTest.class,
		MapBuilderTest.class,
		//---
		CGLIBAspectContainerTest.class,
})
public final class VertigoTestSuite {
	//
}
