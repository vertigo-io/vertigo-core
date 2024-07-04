/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2024, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.lang.WrappedException;

/**
 * Utility methods for manipulating JavaBean properties (getter/setter) for all types of objects.
 */
public final class BeanUtil {
	private static final int BEAN_INFOS_MAX_SIZE = 250;
	private static final Map<Class<?>, BeanInfo> BEAN_INFOS = new ConcurrentHashMap<>();

	/**
	 * Constructor.
	 */
	private BeanUtil() {
		// Private constructor to prevent instantiation
	}

	/**
	* Returns the value of a bean property (e.g., "name" -> object.getName()).
	* Dot notation is not allowed (e.g., "country.name").
	*
	* @param object the object containing the property
	* @param propertyName the name of the property
	* @return the value of the property
	* @throws VSystemException if the getter method is not found
	*/
	public static Object getValue(final Object object, final String propertyName) {
		Assertion.check()
				.isNotNull(object)
				.isNotNull(propertyName)
				.isTrue(propertyName.indexOf('.') == -1, "the dot notation is forbidden");
		//-----
		final PropertyDescriptor pd = getPropertyDescriptor(propertyName, object.getClass());
		final Method readMethod = pd.getReadMethod();
		if (readMethod == null) {
			throw new VSystemException("No getter found for property '{0}' on class '{1}'", propertyName, object.getClass().getName());
		}
		return ClassUtil.invoke(object, readMethod);
	}

	/**
	 * Sets the value of a bean property (e.g., "name" -> object.setName(value)).
	 * Dot notation is not allowed (e.g., "country.name").
	 *
	 * @param object the object containing the property
	 * @param propertyName the name of the property
	 * @param value the new value of the property
	 * @throws VSystemException if the setter method is not found
	 */
	public static void setValue(final Object object, final String propertyName, final Object value) {
		Assertion.check()
				.isNotNull(object)
				.isNotNull(propertyName)
				.isTrue(propertyName.indexOf('.') == -1, "the dot notation is forbidden");
		//-----
		final PropertyDescriptor pd = getPropertyDescriptor(propertyName, object.getClass());
		final Method writeMethod = pd.getWriteMethod();
		if (writeMethod == null) {
			throw new VSystemException("No setter found for property '{0}' on class '{1}'", propertyName, object.getClass().getName());
		}
		ClassUtil.invoke(object, writeMethod, value);
	}

	/**
	* Returns the BeanInfo of a class from the cache.
	* If not present in the cache, it introspects the class and caches the result.
	*
	* @param beanClass the class to introspect
	* @return the BeanInfo of the class
	* @throws IntrospectionException if an error occurs during introspection
	*/
	private static BeanInfo getBeanInfo(final Class<?> beanClass) throws IntrospectionException {
		BeanInfo beanInfo = BEAN_INFOS.get(beanClass);
		if (beanInfo == null) {
			if (BEAN_INFOS.size() > BEAN_INFOS_MAX_SIZE) {
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
	 * Returns the PropertyDescriptor of a property.
	 *
	 * @param propertyName the name of the property
	 * @param beanClass the class containing the property
	 * @return the PropertyDescriptor of the property
	 * @throws VSystemException if no property descriptor is found
	 */
	public static PropertyDescriptor getPropertyDescriptor(final String propertyName, final Class<?> beanClass) {
		final PropertyDescriptor[] descriptors = getPropertyDescriptors(beanClass);
		for (final PropertyDescriptor propertyDescriptor : descriptors) {
			if (propertyName.equals(propertyDescriptor.getName())) {
				return propertyDescriptor;
			}
		}
		throw new VSystemException("No method found for property '{0}' on class '{1}'", propertyName, beanClass.getName());
	}

	/**
	* Returns an array of PropertyDescriptors for a given class.
	*
	* @param beanClass the class to introspect
	* @return an array of PropertyDescriptors
	* @throws WrappedException if an error occurs during introspection
	*/
	private static PropertyDescriptor[] getPropertyDescriptors(final Class<?> beanClass) {
		try {
			return getBeanInfo(beanClass).getPropertyDescriptors();
		} catch (final IntrospectionException e) {
			throw WrappedException.wrap(e, "Error introspecting properties on class {0}", beanClass);
		}
	}
}
