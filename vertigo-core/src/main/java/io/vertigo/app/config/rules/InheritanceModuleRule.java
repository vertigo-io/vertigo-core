package io.vertigo.app.config.rules;

import io.vertigo.app.config.ComponentConfig;
import io.vertigo.app.config.ModuleConfig;
import io.vertigo.app.config.ModuleRule;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.VSystemException;

/**
 * Rule : all components of a module must inherit a class.
 *
 * @author pchretien
 */
public final class InheritanceModuleRule implements ModuleRule {
	private final Class<?> superClass;

	/**
	 * Constructor.
	 * @param superClass the superclass that all components must inherit
	 */
	public InheritanceModuleRule(final Class<?> superClass) {
		Assertion.checkNotNull(superClass);
		//-----
		this.superClass = superClass;
	}

	/** {@inheritDoc} */
	@Override
	public void check(final ModuleConfig moduleConfig) {
		for (final ComponentConfig componentConfig : moduleConfig.getComponentConfigs()) {
			Class<?> clazz;
			if (componentConfig.getApiClass().isDefined()) {
				//if component is defined by an api, then we check that api respects the rule.
				clazz = componentConfig.getApiClass().get();
			} else {
				clazz = componentConfig.getImplClass();
			}
			if (!superClass.isAssignableFrom(clazz)) {
				throw new VSystemException("Inheritance rule : all components of module '{0}' must inherit class : '{2}'. Component '{1}' doesn't respect this rule.", moduleConfig, componentConfig, superClass.getSimpleName());
			}
		}
	}
}
