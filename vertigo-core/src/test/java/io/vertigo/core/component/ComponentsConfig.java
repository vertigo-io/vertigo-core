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
package io.vertigo.core.component;

import io.vertigo.core.component.mock.A;
import io.vertigo.core.component.mock.B;
import io.vertigo.core.component.mock.BImpl;
import io.vertigo.core.component.mock.C;
import io.vertigo.core.component.mock.Computer;
import io.vertigo.core.component.mock.ComputerImpl;
import io.vertigo.core.component.mock.F;
import io.vertigo.core.component.mock.aop.OneMore;
import io.vertigo.core.component.mock.aop.OneMoreInterceptor;
import io.vertigo.core.component.mock.aop.TenMore;
import io.vertigo.core.component.mock.aop.TenMoreInterceptor;
import io.vertigo.core.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.core.engines.AopEngine;
import io.vertigo.core.lang.Assertion;

public final class ComponentsConfig {
	private final AopEngine aopEngine;

	public ComponentsConfig(AopEngine aopEngine) {
		Assertion.checkNotNull(aopEngine);
		//---------------------------------------------------------------------
		this.aopEngine = aopEngine;
	}

	public void config(final ComponentSpaceConfigBuilder componentSpaceConfiguilder) {
		// @formatter:off
		componentSpaceConfiguilder
		.withAopEngine(aopEngine)
		.beginModule("vertigo")
			.withNoAPI()
			.withInheritance(Object.class)
			.beginComponent(Computer.class, ComputerImpl.class).endComponent()
			.beginComponent(A.class).endComponent()
			.beginComponent(B.class, BImpl.class).endComponent()
			.beginComponent(C.class).endComponent()
			.beginComponent(F.class).endComponent()
			.withAspect(OneMore.class, OneMoreInterceptor.class)
			.withAspect(TenMore.class, TenMoreInterceptor.class)
		.endModule();
		// @formatter:on
	}
}
