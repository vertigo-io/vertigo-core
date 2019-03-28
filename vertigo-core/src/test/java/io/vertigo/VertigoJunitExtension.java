/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import io.vertigo.app.App;
import io.vertigo.app.AutoCloseableApp;
import io.vertigo.core.component.di.injector.DIInjector;

/**
 * Classe parente de tous les TNR associés à vertigo.
 *
 * @author jmforhan
 */
public class VertigoJunitExtension implements TestInstancePostProcessor {

	@Override
	public void postProcessTestInstance(final Object testInstance, final ExtensionContext context) throws Exception {
		final App app = new AutoCloseableApp(((AbstractTestCaseJU5) testInstance).buildNodeConfig());
		DIInjector.injectMembers(testInstance, app.getComponentSpace());

	}

}
