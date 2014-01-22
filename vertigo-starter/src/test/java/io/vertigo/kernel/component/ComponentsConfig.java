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
package io.vertigo.kernel.component;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.kernel.component.mock.A;
import io.vertigo.kernel.component.mock.B;
import io.vertigo.kernel.component.mock.BImpl;
import io.vertigo.kernel.component.mock.C;
import io.vertigo.kernel.component.mock.Computer;
import io.vertigo.kernel.component.mock.ComputerImpl;
import io.vertigo.kernel.component.mock.F;
import io.vertigo.kernel.component.mock.aop.OneMore;
import io.vertigo.kernel.component.mock.aop.OneMoreInterceptor;
import io.vertigo.kernel.component.mock.aop.TenMore;
import io.vertigo.kernel.component.mock.aop.TenMoreInterceptor;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.engines.AopEngine;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.plugins.commons.resource.java.ClassPathResourceResolverPlugin;
import io.vertigoimpl.commons.resource.ResourceManagerImpl;

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
		.beginModule("io.vertigo")
			.withNoAPI()
			.withInheritance(Object.class)
			.beginComponent(Computer.class, ComputerImpl.class).endComponent()
			.beginComponent(A.class).endComponent()
			.beginComponent(B.class, BImpl.class).endComponent()
			.beginComponent(C.class).endComponent()
			.beginComponent(F.class).endComponent()
			.withAspect(OneMore.class, OneMoreInterceptor.class)
			.withAspect(TenMore.class, TenMoreInterceptor.class)
		.endModule()	
		.beginModule("spaces")
			.beginComponent(ResourceManager.class, ResourceManagerImpl.class)
				.beginPlugin( ClassPathResourceResolverPlugin.class).endPlugin()
			.endComponent()
		.endModule();
		// @formatter:on
	}
}
