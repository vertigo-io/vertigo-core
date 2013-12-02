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
package io.vertigoimpl.commons.config;

import io.vertigo.commons.config.ConfigManager;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.util.ClassUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.inject.Inject;


/**
 * Impl�mentation du gestionnaire de configuration applicative.
 * @author prahmoune
 * @version $Id: ConfigManagerImpl.java,v 1.1 2013/10/09 14:02:59 pchretien Exp $
 */
public final class ConfigManagerImpl implements ConfigManager {
	@Inject
	private List<ConfigPlugin> configPlugins;
	private static final char CONFIG_PATH_SEPARATOR = '.';
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	private static void checkPath(final String configPath) {
		Assertion.checkArgNotEmpty(configPath);
		Assertion.checkArgument(REGEX_PATH.matcher(configPath).matches(), "path '{0}' doit �tre camelCase et commencer par une minuscule", configPath);
	}

	private static void checkProperty(final String property) {
		Assertion.checkArgNotEmpty(property);
		Assertion.checkArgument(REGEX_PROPERTY.matcher(property).matches(), "property '{0}' doit �tre camelCase et commencer par une minuscule", property);
	}

	/** {@inheritDoc} */
	public boolean getBooleanValue(final String configPath, final String property) {
		return (Boolean) doGetPropertyValue(configPath, property, boolean.class);
	}

	/** {@inheritDoc} */
	public int getIntValue(final String configPath, final String property) {
		return (Integer) doGetPropertyValue(configPath, property, int.class);
	}

	/** {@inheritDoc} */
	public String getStringValue(final String configPath, final String property) {
		return (String) doGetPropertyValue(configPath, property, String.class);
	}

	/** {@inheritDoc} */
	public <C> C resolve(final String configPath, final Class<C> config) {
		Assertion.checkNotNull(configPath);
		Assertion.checkNotNull(config);
		//---------------------------------------------------------------------
		if (config.isInterface()) {
			return createProxy(configPath, config);
		}
		return createAndBindConfig(configPath, config);
	}

	private <C> C createAndBindConfig(final String path, final Class<C> config) {
		final C configObject = ClassUtil.newInstance(config);

		final Set<String> properties = new HashSet<>();
		//1. On liste les propri�t�s g�r�es par la configuration
		// Toutes les m�thodes en get et is sont �ligibles.
		for (final Method method : config.getMethods()) {
			if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
				properties.add(ClassUtil.getPropertyName(method));
			}
		}
		//2. On affecte � ces propri�t�s les valeurs en 
		for (final Field field : config.getDeclaredFields()) {
			final String property = field.getName();
			if (properties.contains(property)) {
				final Object value = doGetPropertyValue(path, property, field.getType());
				ClassUtil.set(configObject, field, value);
			}
		}
		return configObject;
	}

	private <C> C createProxy(final String path, final Class<C> config) {
		Assertion.checkNotNull(path);
		Assertion.checkNotNull(config);
		Assertion.checkArgument(config.isInterface(), "la configuration recherch�e doit �tre une interface");
		//---------------------------------------------------------------------
		final Map<Method, String> configs = new HashMap<>();
		for (final Method method : config.getMethods()) {
			configs.put(method, ClassUtil.getPropertyName(method));
		}

		return config.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { config }, new InvocationHandler() {
			public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
				final String property = configs.get(method);
				Assertion.checkNotNull(property, "M�thode {0} inconnue sur la classe {1}", method, config.getSimpleName());
				return doGetPropertyValue(path, property, method.getReturnType());
			}
		}));
	}

	/**
	 * Retourne la config a utiliser pour cette propri�t�.
	 * Le s�parateur par d�faut est le .
	 * @param configPath Chemin de la config de d�part
	 * @return Config � utiliser : une sup�rieur ou �gale. Si property inconnue on retourne celle de d�part 
	 */
	private Object doGetPropertyValue(final String configPath, final String property, final Class<?> propertyClass) {
		checkPath(configPath);
		checkProperty(property);
		//---------------------------------------------------------------------
		String subConfig = configPath;
		while (subConfig != null) {
			for (final ConfigPlugin configPlugin : configPlugins) {
				final Option<String> value = configPlugin.getValue(subConfig, property);
				if (value.isDefined()) {
					return castValue(subConfig, property, propertyClass, value.get());
				}
			}
			subConfig = goUp(subConfig);
		}
		throw new IllegalArgumentException("Propri�t� '" + property + "' non trouv�e dans la configuration '" + configPath + "'.");
	}

	/**
	 * Remonte un niveau dans la hi�rachie implicite des configs.
	 * Le s�parateur par d�faut est le .
	 * @param subConfig config de d�part
	 * @return Config du niveau sup�rieur, null si pas de niveau sup�rieur
	 */
	private static String goUp(final String subConfig) {
		final int pathSeparatorIndex = subConfig.lastIndexOf(CONFIG_PATH_SEPARATOR);
		if (pathSeparatorIndex == -1) {
			return null;
		}
		return subConfig.substring(0, pathSeparatorIndex);
	}

	/**
	 * Cast la valeur fournie sous forme de String dans le type cible : propertyClass.
	 * @param config Chemin d�crivant la configuration
	 * @param property Nom de la propri�t� de la configuration
	 * @param propertyClass Class attendue pour la propri�t�
	 * @return Valeur typ�e de la propri�t�
	 */
	private static Object castValue(final String config, final String property, final Class<?> propertyClass, final String value) {
		if (boolean.class.equals(propertyClass)) {
			return toBoolean(config, property, value);
		} else if (int.class.equals(propertyClass)) {
			return toInteger(config, property, value);
		} else if (String.class.equals(propertyClass)) {
			return value;
		}
		throw new IllegalArgumentException("Type de La propri�t� '" + config + ":" + property + " de type ' " + propertyClass + " non g�r�e");
	}

	private static boolean toBoolean(final String config, final String property, final String value) {
		if (!(TRUE.equalsIgnoreCase(value) || FALSE.equalsIgnoreCase(value))) {
			throw new VRuntimeException("La propri�t� '{0}:{1}' n'est pas convertible en 'boolean' : {2}", null, config, property, value);
		}
		return Boolean.parseBoolean(value);
	}

	private static int toInteger(final String config, final String property, final String value) {
		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			throw new VRuntimeException("La propri�t� '{0}:{1}'  n'est pas convertible en 'int' : {2}", e, config, property, value);
		}
	}
}
