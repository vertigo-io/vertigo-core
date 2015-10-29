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
package io.vertigo.engines.command;

import io.vertigo.core.Home;
import io.vertigo.core.command.VCommand;
import io.vertigo.core.command.VCommandExecutor;
import io.vertigo.engines.command.samples.VDescribableCommandExecutor;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.COMMAND;
import io.vertigo.lang.Describable;
import io.vertigo.util.ClassUtil;
import io.vertigo.util.MapBuilder;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

/**
 * @author pchretien
 */
final class CommandScannerUtil {

	private CommandScannerUtil() {
		//private
	}

	static void scan(final MapBuilder<String, VCommandExecutor> mapBuilder, final String componentId, final Object component) {
		Assertion.checkNotNull(mapBuilder);
		Assertion.checkNotNull(componentId);
		Assertion.checkNotNull(component);
		//----
		scanByDescribable(mapBuilder, componentId, component);
		scanByAnnnotation(mapBuilder, componentId, component);
	}

	private static void scanByAnnnotation(final MapBuilder<String, VCommandExecutor> mapBuilder, final String componentId, final Object component) {
		for (final Method method : component.getClass().getMethods()) {
			scanByAnnotation(mapBuilder, componentId, method);
		}
	}

	private static void scanByDescribable(final MapBuilder<String, VCommandExecutor> mapBuilder, final String componentId, final Object component) {
		if (component instanceof Describable) {
			mapBuilder.put(componentId, new VDescribableCommandExecutor());
		}
	}

	private static void scanByAnnotation(final MapBuilder<String, VCommandExecutor> mapBuilder, final String componentId, final Method method) {
		//		final PathPrefix pathPrefix = method.getDeclaringClass().getAnnotation(PathPrefix.class);
		//		if (pathPrefix != null) {
		//			builder.withPathPrefix(pathPrefix.value());
		//		}
		for (final Annotation annotation : method.getAnnotations()) {
			if (annotation instanceof COMMAND) {
				Assertion.checkArgument(method.getParameterTypes().length == 0, " Only methods without params are accepted, check component {0}", method.getDeclaringClass());
				//-----
				mapBuilder.put(((COMMAND) annotation).value(), new VCommandExecutor() {
					@Override
					public Object exec(final VCommand command) {
						final Object component = Home.getApp().getComponentSpace().resolve(componentId, Object.class);
						return ClassUtil.invoke(component, method);
					}
				});
			}

		}
	}
}
