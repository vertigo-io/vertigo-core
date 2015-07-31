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
						final Object component = Home.getComponentSpace().resolve(componentId, Object.class);
						return ClassUtil.invoke(component, method);
					}
				});
			}

		}
	}
}
