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
import io.vertigo.kernel.component.cglib.CGLIBAspectContainerTest;
import io.vertigo.kernel.di.injector.InjectorTest;
import io.vertigo.kernel.di.reactor.ReactorTest;
import io.vertigo.kernel.home.componentspace.ComponentSpace2Test;
import io.vertigo.kernel.home.componentspace.ComponentSpaceTest;
import io.vertigo.kernel.home.definitionspace.DefinitionSpaceTest;
import io.vertigo.kernel.lang.AssertionTest;
import io.vertigo.kernel.util.ClassUtilTest;
import io.vertigo.kernel.util.DateUtilTest;
import io.vertigo.kernel.util.StringUtilTest;
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
//@formatter:off
	//Kernel
	DefinitionSpaceTest.class,
	ComponentSpaceTest.class,
	ComponentSpace2Test.class,
	XmlHomeTest.class,
	XmlComponentsTest.class,
	InjectorTest.class,
	ReactorTest.class,
	AssertionTest.class,
	ClassUtilTest.class,
	DateUtilTest.class,
	StringUtilTest.class,
	//---
	ConfigManagerTest.class,
	LocaleManagerTest.class,
	ResourceManagerTest.class,
	MultiConfigManagerTest.class,
	PropertiesConfigManagerTest.class,
	XmlConfigManagerTest.class,
	CGLIBAspectContainerTest.class, 
//@formatter:on
})
public final class VertigoTestSuite {
	//
}
