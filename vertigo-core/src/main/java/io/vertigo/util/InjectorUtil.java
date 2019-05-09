package io.vertigo.util;

import java.util.Collections;
import java.util.Optional;

import io.vertigo.app.Home;
import io.vertigo.core.component.Activeable;
import io.vertigo.core.component.loader.ComponentSpaceLoader;
import io.vertigo.lang.Assertion;

public final class InjectorUtil {

	private InjectorUtil() {
		// util
	}

	/**
	 * Creates an new object instance of the given class and inject dependencies using the current App ComponentSpace as container.
	 * This created object is not registered in the ComponantSpace.
	 * Therefore the clazz cannot implement the interface Activeable because the lifecycle of this component is not handled by Vertigo.
	 * @param clazz the clazz of the object your want to create with it's member injected.
	 * @return the newly created object.
	 */
	public static <T> T newInstance(final Class<T> clazz) {
		Assertion.checkNotNull(clazz);
		Assertion.checkState(!clazz.isAssignableFrom(Activeable.class), " {0} is an Activeable component and must be registred in the NodeConfig for creation at the application startup", clazz);
		// ---
		return ComponentSpaceLoader.createInstance(clazz, Home.getApp().getComponentSpace(), Optional.empty(), Collections.emptyMap());
	}

	/**
	 * Inject dependencies in the instance using the current App ComponentSpace as container.
	 * @param instance the object your want to get it's member injected.
	 * @return the enhanced object.
	 */
	public static void injectMembers(final Object instance) {
		Assertion.checkNotNull(instance);
		//-----
		ComponentSpaceLoader.injectMembers(instance, Home.getApp().getComponentSpace(), Optional.empty(), Collections.emptyMap());
	}

}
