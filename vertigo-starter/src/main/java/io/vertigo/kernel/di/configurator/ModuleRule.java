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
package io.vertigo.kernel.di.configurator;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

/**
 * Rule : all components of a module must respect this rule.
 * 
 * @author pchretien
 * @version $Id: ModuleRule.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
interface ModuleRule {
	void chek(final ModuleConfig moduleConfig);
}

/**
 * Rule : all components of a module must have an API.
 * 
 * @author pchretien
 * @version $Id: ModuleRule.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
final class APIModuleRule implements ModuleRule {
	/** {@inheritDoc} */
	public void chek(final ModuleConfig moduleConfig) {
		for (final ComponentConfig componentConfig : moduleConfig.getComponentConfigs()) {
			if (componentConfig.getApiClass().isEmpty()) {
				throw new VRuntimeException("api rule : all components of module '{0}' must have an api. Component '{1}' doesn't respect this rule.", null, moduleConfig, componentConfig);
			}
		}
	}
}

/**
 * Rule : all components of a module must inherit a class.
 * 
 * @author pchretien
 * @version $Id: ModuleRule.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
final class InheritanceModuleRule implements ModuleRule {
	private final Class<?> superClass;

	InheritanceModuleRule(final Class<?> superClass) {
		Assertion.checkNotNull(superClass);
		//---------------------------------------------------------------------
		this.superClass = superClass;
	}

	/** {@inheritDoc} */
	public void chek(final ModuleConfig moduleConfig) {
		for (final ComponentConfig componentConfig : moduleConfig.getComponentConfigs()) {
			Class<?> clazz;
			if (componentConfig.getApiClass().isDefined()) {
				//if component is defined by an api, then we check that api respects the rule.
				clazz = componentConfig.getApiClass().get();
			} else {
				clazz = componentConfig.getImplClass();
			}
			if (!superClass.isAssignableFrom(clazz)) {
				throw new VRuntimeException("Inheritance rule : all components of module '{0}' must inherit class : '{2}'. Component '{1}' doesn't respect this rule.", null, moduleConfig, componentConfig, superClass.getSimpleName());
			}
		}
	}
}
