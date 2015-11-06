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
package io.vertigo.core.param;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.lang.Option;
import io.vertigo.util.StringUtil;

import java.util.List;
import java.util.regex.Pattern;

import javax.inject.Inject;

/**
 * Interface du gestionnaire de la configuration applicative.
 * Une configuration est identifiée par un chemin et possède une liste de propriétés.
 * Une propriété est identifiée par un nom et possède une valeur.
 * Le chemmin est camelCase.camelCase et ne contient que des lettres et chiffres; les séparateurs sont des points.
 * 	- Une propriété est camelCase et ne contient que des lettres et chiffres.
 *  - Une regex précise les chaines autorisées.
 *
 * Les propriétés sont de trois types :
 * -boolean
 * -String
 * -int
 *
 * Le chemin des configuration est hiérachique, il y a un héritage implicite des propriétés.
 * Le séparateur est le caractère point (.)
 *
 * Même si une configuration n'est pas déclarée, la remontée est automatique.
 *
 *
 * Exemple :
 *
 * maconf:{
 *  mapropriete1:toto,
 *  mapropriete2:titi
 * }
 *
 * maconf.subConf1:{
 *  mapropriete2:tata,
 *  mapropriete3:titi
 * }
 *
 * maconf.subConf2:{
 *  mapropriete3:tata
 * }
 *
 * getStringValue(maconf, mapropriete1) => toto
 * getStringValue(maconf.subConf1, mapropriete1) => toto  #La propriété 'mapropriete1' n'étant pas trouvée on remonte au parent.
 *
 * getStringValue(maconf, mapropriete2) => titi
 * getStringValue(maconf.subConf1, mapropriete2) => tata #La propriété 'mapropriete2' est surchargée
 *
 * getStringValue(maconf.subConf2, mapropriete3) => tata
 * getStringValue(maconf, mapropriete3) => erreur #'mapropriete3' n'est pas déclarée dans maConf
 *
 * getStringValue(maconf.unknown, mapropriete2) => titi
 * getStringValue(maconf.subConf1.unknown, mapropriete2) => tata
 *
 * @author pchretien, npiedeloup, prahmoune 
 */
public final class ParamManager implements Component {
	/** Regexp property. */
	private static final Pattern REGEX_PROPERTY = Pattern.compile("([a-z][a-zA-Z0-9]*)(\\.[a-z][a-zA-Z0-9]*)*");
	private final List<ParamPlugin> paramPlugins;
	private static final String TRUE = "true";
	private static final String FALSE = "false";

	@Inject
	public ParamManager(final List<ParamPlugin> paramPlugins) {
		Assertion.checkNotNull(paramPlugins);
		//-----
		this.paramPlugins = paramPlugins;
	}

	private static void checkProperty(final String property) {
		Assertion.checkArgNotEmpty(property);
		Assertion.checkArgument(REGEX_PROPERTY.matcher(property).matches(), "property '{0}' doit être camelCase et commencer par une minuscule", property);
	}

	//	/**
	//	 * Retourne une implémentation à partir d'une interface ou d'un Bean.
	//	 * Celà permet de structurer les développements.
	//	 * @param <C> Type de l'interface de la configuration
	//	 * @param configPath Chemin décrivant la configuration
	//	 * @param configClass Interface ou Class de la configuration
	//	 * @return Instance of configClass
	//	 */
	//	public <C> C resolve(final String configPath, final Class<C> configClass) {
	//		Assertion.checkNotNull(configPath);
	//		Assertion.checkNotNull(configClass);
	//		//-----
	//		if (configClass.isInterface()) {
	//			return createProxy(configPath, configClass);
	//		}
	//		return createAndBindConfig(configPath, configClass);
	//	}

	/**
	 * Retourne une propriété de configuration.
	 * @param configPath Chemin décrivant la configuration
	 * @param property Nom de la propriété de la configuration
	 * @return Valeur de la propriété
	 */
	public String getStringValue(final String property) {
		return doGetPropertyValue(property, String.class);
	}

	/**
	 * Retourne une propriété de configuration.
	 * @param configPath Chemin décrivant la configuration
	 * @param property Propriété de la configuration
	 * @return Valeur de la propriété
	 */
	public int getIntValue(final String property) {
		return doGetPropertyValue(property, int.class);
	}

	/**
	 * Retourne une propriété de configuration.
	 * @param configPath Chemin décrivant la configuration
	 * @param property Propriété de la configuration
	 * @return Valeur de la propriété
	 */
	public boolean getBooleanValue(final String property) {
		return doGetPropertyValue(property, boolean.class);
	}

	//	private <C> C createAndBindConfig(final String path, final Class<C> config) {
	//		final C configObject = ClassUtil.newInstance(config);
	//
	//		final Set<String> properties = new HashSet<>();
	//		//1. On liste les propriétés gérées par la configuration
	//		// Toutes les méthodes en get et is sont éligibles.
	//		for (final Method method : config.getMethods()) {
	//			if (method.getName().startsWith("get") || method.getName().startsWith("is")) {
	//				properties.add(ClassUtil.getPropertyName(method));
	//			}
	//		}
	//		//2. On affecte à ces propriétés les valeurs en
	//		for (final Field field : config.getDeclaredFields()) {
	//			final String property = field.getName();
	//			if (properties.contains(property)) {
	//				final Object value = doGetPropertyValue(path, property, field.getType());
	//				ClassUtil.set(configObject, field, value);
	//			}
	//		}
	//		return configObject;
	//	}

	//	private <C> C createProxy(final String path, final Class<C> config) {
	//		Assertion.checkNotNull(path);
	//		Assertion.checkNotNull(config);
	//		Assertion.checkArgument(config.isInterface(), "la configuration recherchée doit être une interface");
	//		//-----
	//		final Map<Method, String> configs = new HashMap<>();
	//		for (final Method method : config.getMethods()) {
	//			configs.put(method, ClassUtil.getPropertyName(method));
	//		}
	//
	//		return config.cast(Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(), new Class[] { config }, new InvocationHandler() {
	//			@Override
	//			public Object invoke(final Object proxy, final Method method, final Object[] args) throws Throwable {
	//				final String property = configs.get(method);
	//				Assertion.checkNotNull(property, "Méthode {0} inconnue sur la classe {1}", method, config.getSimpleName());
	//				return doGetPropertyValue(path, property, method.getReturnType());
	//			}
	//		}));
	//	}

	/**
	 * Retourne la config a utiliser pour cette propriété.
	 * @param property property's name
	 * @param propertyClass property's class
	 * @return Config à utiliser : une supérieur ou égale. Si property inconnue on retourne celle de départ
	 */
	private <C> C doGetPropertyValue(final String property, final Class<C> propertyClass) {
		checkProperty(property);
		//-----
		for (final ParamPlugin paramPlugin : paramPlugins) {
			final Option<String> value = paramPlugin.getValue(property);
			if (value.isDefined()) {
				return (C) castValue(property, propertyClass, value.get());
			}
		}
		throw new IllegalArgumentException("propriété '" + property + "' non trouvée");
	}

	/**
	 * Cast la valeur fournie sous forme de String dans le type cible : propertyClass.
	 * @param property Nom de la propriété de la configuration
	 * @param propertyClass Class attendue pour la propriété
	 * @return Valeur typée de la propriété
	 */
	private static Object castValue(final String property, final Class<?> propertyClass, final String value) {
		if (boolean.class.equals(propertyClass)) {
			return toBoolean(property, value);
		} else if (int.class.equals(propertyClass)) {
			return toInteger(property, value);
		} else if (String.class.equals(propertyClass)) {
			return value;
		}
		throw new IllegalArgumentException("Type de La propriété :" + property + " de type ' " + propertyClass + " non gérée");
	}

	private static boolean toBoolean(final String property, final String value) {
		if (!(TRUE.equalsIgnoreCase(value) || FALSE.equalsIgnoreCase(value))) {
			throw new RuntimeException(StringUtil.format("La propriété '{0}' n'est pas convertible en 'boolean' : {1}", property, value));
		}
		return Boolean.parseBoolean(value);
	}

	private static int toInteger(final String property, final String value) {
		try {
			return Integer.parseInt(value);
		} catch (final NumberFormatException e) {
			throw new RuntimeException(StringUtil.format("La propriété '{0}'  n'est pas convertible en 'int' : {1}", property, value), e);
		}
	}
}
