package io.vertigo.app.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.util.Selector;
import io.vertigo.util.Selector.ClassConditions;

/**
 * Tool for registering components in an app based on discovery in a package tree.
 * @author mlaroche
 *
 */
public class ComponentDiscovery {

	/**
	 * Register all components of a kind discovered in a package tree.
	 * If component has API we must find one and only one Impl.
	 * If component hasn't API we don't care.
	 * @param componentType the kind of components to discover
	 * @param packagePrefix the package we to look
	 * @param moduleConfigBuilder the module where components will be added.
	 */
	public static void registerComponents(final Class<? extends Component> componentType, final String packagePrefix, final ModuleConfigBuilder moduleConfigBuilder) {
		Assertion.checkNotNull(componentType);
		//
		final Collection<Class> components = new Selector()
				.from(packagePrefix)
				.filterClasses(ClassConditions.subTypeOf(componentType))
				.findClasses();
		registerComponents(components, moduleConfigBuilder);
	}

	private static void registerComponents(final Collection<Class> components, final ModuleConfigBuilder moduleConfigBuilder) {
		Assertion.checkNotNull(components);
		Assertion.checkNotNull(moduleConfigBuilder);
		// ---
		//we control the api just bellow
		moduleConfigBuilder.withNoAPI();
		//API
		final Collection<Class> apiClasses = new Selector()
				.from(components)
				.filterClasses(ClassConditions.interfaces())
				.findClasses();
		//Impl
		final Collection<Class> implClasses = new Selector()
				.from(components)
				.filterClasses(ClassConditions.interfaces().negate())
				.findClasses();

		final Map<Class, Class> apiImplMap = new HashMap<>();
		final Collection<Class> myImplClasses = new ArrayList<>(implClasses);
		//---
		for (final Class apiClazz : apiClasses) {
			final Collection<Class> potentialImpl = new Selector()
					.from(implClasses)
					.filterClasses(ClassConditions.subTypeOf(apiClazz))
					.findClasses();
			// ---
			Assertion.checkState(potentialImpl.size() > 0, "No implentation found for the api {0}", apiClazz);
			Assertion.checkState(potentialImpl.size() == 1, "Multiple implentations found for the api {0}", apiClazz);
			// ---
			final Class implClass = potentialImpl.stream().findFirst().get();
			myImplClasses.remove(implClass);
			apiImplMap.put(apiClazz, implClass);
		}
		//---
		// With API
		apiImplMap.entrySet()
				.stream()
				.forEach((entry) -> moduleConfigBuilder.addComponent(entry.getKey(), entry.getValue()));

		// Without API
		myImplClasses.stream()
				.forEach((clazz) -> moduleConfigBuilder.addComponent(clazz));

	}

}
