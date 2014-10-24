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
package io.vertigo.util;

import io.vertigo.lang.Assertion;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Méthodes utilitaires pour manipuler les propriétés (getter/setter) des JavaBeans (ie tous les types d'objets).
 */
public final class BeanUtil {
	private static final Map<Class<?>, BeanInfo> BEAN_INFOS = new ConcurrentHashMap<>();

	/**
	 * Constructeur.
	 */
	private BeanUtil() {
		super();
	}

	/**
	 * Retourne la valeur d'une propriété d'un bean
	 * (ex : "name" -> object.getName() ou "country.name" -> object.getCountry().getName()).
	 * @return java.lang.Object
	 * @param object java.lang.Object
	 * @param propertyName java.lang.String
	 */
	public static Object getValue(final Object object, final String propertyName) {
		Assertion.checkNotNull(object);
		Assertion.checkNotNull(propertyName);
		Assertion.checkArgument(propertyName.indexOf('.') == -1, "La notation par point est interdite.");
		//---------------------------------------------------------------------
		final PropertyDescriptor pd = getPropertyDescriptor(propertyName, object.getClass());
		final Method readMethod = pd.getReadMethod();
		if (readMethod == null) {
			throw new RuntimeException("Getter non trouvé pour l'attribut \"" + propertyName + "\" sur classe \"" + object.getClass().getName() + '\"');
		}
		return ClassUtil.invoke(object, readMethod);
	}

	/**
	 * Définit la valeur d'une propriété d'un bean
	 * (ex : "name" -> object.setName(value) ou "country.name" -> object.getCountry().setName(value)).
	 * @param object java.lang.Object
	 * @param propertyName java.lang.String
	 * @param value java.lang.Object
	 */
	public static void setValue(final Object object, final String propertyName, final Object value) {
		Assertion.checkNotNull(object);
		Assertion.checkNotNull(propertyName);
		Assertion.checkArgument(propertyName.indexOf('.') == -1, "La notation par point est interdite.");
		//---------------------------------------------------------------------
		final PropertyDescriptor pd = getPropertyDescriptor(propertyName, object.getClass());
		final Method writeMethod = pd.getWriteMethod();
		if (writeMethod == null) {
			throw new RuntimeException("Setter non trouvé pour l'attribut \"" + propertyName + "\" sur classe \"" + object.getClass().getName() + '\"');
		}
		ClassUtil.invoke(object, writeMethod, value);
	}

	/**
	 * Retourne le beanInfo d'une classe à partir du cache.
	 * @return java.beans.BeanInfo
	 * @param beanClass java.lang.Class
	 * @throws java.beans.IntrospectionException   Erreur dans l'introspection
	 */
	private static BeanInfo getBeanInfo(final Class<?> beanClass) throws IntrospectionException {
		BeanInfo beanInfo = BEAN_INFOS.get(beanClass);
		if (beanInfo == null) {
			if (BEAN_INFOS.size() > 250) {
				BEAN_INFOS.clear();
				// pour éviter une fuite mémoire potentielle, par ex sur classes proxy
			}
			// On veut tout le BeanInfo sauf Object (pas la propriété de getClass())
			beanInfo = Introspector.getBeanInfo(beanClass, Object.class);
			BEAN_INFOS.put(beanClass, beanInfo);
		}
		return beanInfo;
	}

	/**
	 * Retourne le PropertyDescriptor d'une propriété.
	 * @return java.beans.PropertyDescriptor
	 * @param propertyName java.lang.String
	 * @param beanClass java.lang.Class
	 */
	private static PropertyDescriptor getPropertyDescriptor(final String propertyName, final Class<?> beanClass) {
		// on pourrait faire new PropertyDescriptor(propertyName, beanClass)
		// mais si jamais il a été défini des BeanInfo pour certaines classes,
		//autant les utiliser.
		final PropertyDescriptor[] descriptors = getPropertyDescriptors(beanClass);
		for (final PropertyDescriptor propertyDescriptor : descriptors) {
			if (propertyName.equals(propertyDescriptor.getName())) {
				return propertyDescriptor;
			}
		}
		throw new RuntimeException("Aucune méthode trouvée pour l'attribut \"" + propertyName + "\" sur classe \"" + beanClass.getName() + '\"');
	}

	private static PropertyDescriptor[] getPropertyDescriptors(final Class<?> beanClass) {
		try {
			return getBeanInfo(beanClass).getPropertyDescriptors();
		} catch (final IntrospectionException e) {
			throw new RuntimeException("Erreur d'introspection des propriétés sur la classe " + beanClass, e);
		}
	}

	/**
	 * Vérifie l'égalité de deux valeurs non nulles.
	 *
	 * @param a Valeur 1
	 * @param b Valeur 2
	 * @return Si égales
	 */
	public static boolean equals(final Object a, final Object b) {
		if (a == b) {
			return true;
		}
		if (a == null) {
			//Si les deux objets sont null alors on considère qu'ils sont égaux
			return b == null;
		}
		//A partir de maintenant a est non null
		return a.equals(b);
	}

}
