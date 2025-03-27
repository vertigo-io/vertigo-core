/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;

/**
 * The ClassUtil class provides methods to determine the structure of a class or to create instances.
 *
 * @author pchretien
 */
public final class ClassUtil {
	private static final Class<?>[] EMPTY_CLAZZ_ARRAY = new Class[0];

	/**
	 * Constructor
	 */
	private ClassUtil() {
		// private constructor
	}

	/**
	 * Creates a new untyped instance via class name (empty constructor).
	 * Please favor methods returning a typed instance as soon as the type is known.
	 * @param javaClassName Class name
	 * @return New instance
	 */
	public static Object newInstance(final String javaClassName) {
		final Class<?> javaClass = classForName(javaClassName);
		return newInstance(javaClass);
	}

	/**
	 * Creates a new typed instance via class name (empty constructor).
	 *
	 * @param <J> Type of the returned instance
	 * @param javaClassName Class name
	 * @param type Returned type
	 * @return New instance
	 */
	public static <J> J newInstance(final String javaClassName, final Class<J> type) {
		final Class<? extends J> javaClass = classForName(javaClassName, type);
		return newInstance(javaClass);
	}

	/**
	 * Creates a new typed instance via a class (empty constructor).
	 *
	 * @param <J> Type of the returned instance
	 * @param clazz Class
	 * @return New instance
	 */
	public static <J> J newInstance(final Class<J> clazz) {
		final Constructor<? extends J> constructor = findConstructor(clazz);
		return newInstance(constructor, EMPTY_CLAZZ_ARRAY);
	}

	/**
	 * Creates a new typed instance via a constructor and its arguments.
	 *
	 * @param <J> Type of the returned instance
	 * @param constructor Constructor
	 * @param args Construction arguments
	 * @return New instance
	 */
	public static <J> J newInstance(final Constructor<J> constructor, final Object[] args) {
		Assertion.check()
				.isNotNull(constructor)
				.isNotNull(args);
		//---
		try {
			return constructor.newInstance(args);
		} catch (final InvocationTargetException e) {
			throw WrappedException.wrap(e, "An error has occurred while invoking the constructor on class : {0} ", constructor.getDeclaringClass());
		} catch (final java.lang.IllegalAccessException e) {
			throw WrappedException.wrap(e, "The constructor on class {0} is not accessible", constructor.getDeclaringClass());
		} catch (final Exception e) {
			throw WrappedException.wrap(e, "Unable to instantiate the class {0}", constructor.getDeclaringClass());
		}
	}

	/**
	 * Retrieves the no-arg constructor for the given class.
	 * 
	 * @param clazz The class to find the constructor for
	 * @return The no-arg constructor for the class
	 */
	private static <J> Constructor<J> findConstructor(final Class<J> clazz) {
		return findConstructor(clazz, EMPTY_CLAZZ_ARRAY);
	}

	/**
	* Retrieves the constructor corresponding to the given signature.
	* @param <J> Class type
	* @param clazz Class to search for the constructor
	* @param parameterTypes Signature of the searched constructor
	* @return Searched constructor
	*/
	public static <J> Constructor<J> findConstructor(final Class<J> clazz, final Class<?>[] parameterTypes) {
		Assertion.check()
				.isNotNull(clazz)
				.isNotNull(parameterTypes);
		//---
		try {
			return clazz.getConstructor(parameterTypes);
		} catch (final NoSuchMethodException e) {
			if (parameterTypes.length == 0) {
				// For empty constructors (no parameters), we throw a simpler message.
				throw WrappedException.wrap(e, "No empty constructor found on {0}", clazz.getSimpleName());
			}
			throw WrappedException.wrap(e, "No constructor found on {0} with signature {1}", clazz.getSimpleName(), Arrays.toString(parameterTypes));
		}
	}

	/**
	 * Retrieves an untyped class from its name.
	 *
	 * @param javaClassName Class name
	 * @return Java class
	 */
	public static Class<?> classForName(final String javaClassName) {
		Assertion.check()
				.isNotBlank(javaClassName);
		//---
		try {
			return Class.forName(javaClassName);
		} catch (final ClassNotFoundException e) {
			throw WrappedException.wrap(e, "Unable to find the class : {0}", javaClassName);
		}
	}

	/**
	 * Retrieves a typed class from its name.
	 *
	 * @param <J> Type of the returned instance
	 * @param javaClassName Class name
	 * @param type Type.
	 * @return Java class
	 */
	public static <J> Class<? extends J> classForName(final String javaClassName, final Class<J> type) {
		Assertion.check()
				.isNotNull(javaClassName)
				.isNotNull(type);
		//---
		try {
			return Class.forName(javaClassName).asSubclass(type);
		} catch (final ClassNotFoundException e) {
			throw WrappedException.wrap(e, "Unable to find the class : '{0}'", javaClassName);
		} catch (final NoClassDefFoundError e) {
			throw WrappedException.wrap(e, "Unable to load one of the dependent classes of : '{0}'", javaClassName);
		} catch (final ClassCastException e) {
			throw WrappedException.wrap(e, "Class {0} must be a subclass of : {1}", javaClassName, type.getSimpleName());
		}
	}

	/**
	 * Dynamic invocation of a method on a specific instance.
	 *
	 * @param instance Object
	 * @param method method which is invoked
	 * @param args Args
	 * @return value provided as the result by the method
	 */
	public static Object invoke(final Object instance, final Method method, final Object... args) {
		Assertion.check()
				.isNotNull(instance)
				.isNotNull(method);
		//---
		try {
			return method.invoke(instance, args);
		} catch (final IllegalAccessException e) {
			throw WrappedException.wrap(e, "Impossible access to method : {0} of {1}", method.getName(), method.getDeclaringClass().getName());
		} catch (final InvocationTargetException e) {
			throw WrappedException.wrap(e, "Error while invoking method : {0} of {1}", method.getName(), method.getDeclaringClass().getName());
		}
	}

	/**
	 * Dynamic assignment of a field's value (even private).
	 *
	 * @param instance Object on which the method is invoked
	 * @param field Field concerned
	 * @param value New value
	 */
	public static void set(final Object instance, final Field field, final Object value) {
		Assertion.check()
				.isNotNull(instance)
				.isNotNull(field);
		//---
		try {
			field.setAccessible(true);
			field.set(instance, value);
		} catch (final IllegalAccessException e) {
			throw WrappedException.wrap(e, "Impossible access to field : {0} of {1}", field.getName(), field.getDeclaringClass().getName());
		}
	}

	/**
	 * Dynamic retrieval of a field's value.
	 *
	 * @param instance Object on which the method is invoked
	 * @param field Concerned field
	 * @return Value
	 */
	public static Object get(final Object instance, final Field field) {
		Assertion.check()
				.isNotNull(instance)
				.isNotNull(field);
		//---
		try {
			field.setAccessible(true);
			return field.get(instance);
		} catch (final IllegalAccessException e) {
			throw WrappedException.wrap(e, "Access to field '{0}' of '{1}' is not possible", field.getName(), field.getDeclaringClass().getName());
		}
	}

	/**
	 * Retrieves the method corresponding to the name and signature among the passed methods.
	 * @param clazz Class on which we are searching for methods
	 * @param methodName Name of the searched method
	 * @param parameterTypes Signature of the searched method
	 * @return Searched method
	 */
	public static Method findMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
		Assertion.check()
				.isNotNull(clazz)
				.isNotNull(methodName)
				.isNotNull(parameterTypes);
		//---
		try {
			return clazz.getMethod(methodName, parameterTypes);
		} catch (final NoSuchMethodException e) {
			throw WrappedException.wrap(e, "Method '{0}' not found on '{1}'", methodName, clazz.getName());
		}
	}

	/**
	 * Returns all declared fields (including parent fields) annotated for a given class.
	 * @param clazz Class
	 * @param annotation Expected annotation
	 * @return All declared fields (including parent fields)
	 */
	public static Collection<Field> getAllFields(final Class<?> clazz, final Class<? extends Annotation> annotation) {
		Assertion.check()
				.isNotNull(clazz)
				.isNotNull(annotation);
		//---
		return ClassUtil.getAllFields(clazz)
				.stream()
				.filter(field -> field.isAnnotationPresent(annotation))
				.toList();
	}

	/**
	 * Returns all declared methods annotated by the given annotation.
	 * @param clazz Class
	 * @param annotation Expected annotation
	 * @return All declared fields (including parent fields)
	 */
	public static Collection<Method> getAllMethods(final Class<?> clazz, final Class<? extends Annotation> annotation) {
		Assertion.check()
				.isNotNull(clazz)
				.isNotNull(annotation);
		//---
		return ClassUtil.getAllMethods(clazz)
				.stream()
				.filter(method -> method.isAnnotationPresent(annotation))
				.toList();
	}

	/**
	 * Returns all declared fields (including parent fields) for a given class.
	 * @param clazz Class
	 * @return All declared fields (including parent fields)
	 */
	public static Collection<Field> getAllFields(final Class<?> clazz) {
		Assertion.check()
				.isNotNull(clazz);
		//---
		final List<Field> fields = new ArrayList<>();
		final Field[] declaredFields = clazz.getDeclaredFields();
		fields.addAll(Arrays.asList(declaredFields));
		final Class<?> parent = clazz.getSuperclass();
		if (parent != null) {
			fields.addAll(getAllFields(parent));
		}
		return Collections.unmodifiableCollection(fields);
	}

	/**
	 * Returns all declared methods for a given class (including parent methods).
	 * @param clazz Class
	 * @return All declared methods (including parent methods)
	 */
	public static Collection<Method> getAllMethods(final Class<?> clazz) {
		Assertion.check()
				.isNotNull(clazz);
		//---
		final List<Method> methods = new ArrayList<>();
		final Method[] declaredMethods = clazz.getDeclaredMethods();
		methods.addAll(Arrays.asList(declaredMethods));
		final Class<?> parent = clazz.getSuperclass();
		if (parent != null) {
			methods.addAll(getAllMethods(parent));
		}
		return Collections.unmodifiableCollection(methods);
	}

	/**
	 * Returns all interfaces (including those of the parents) for a given class.
	 * @param clazz Class
	 * @return All implemented interfaces
	 */
	public static Set<Class<?>> getAllInterfaces(final Class<?> clazz) {
		Assertion.check()
				.isNotNull(clazz);
		//---
		Class<?> root = clazz;
		final Set<Class<?>> allInterfaces = new HashSet<>();
		while (root != null) {
			for (final Class<?> intf : root.getInterfaces()) {
				allInterfaces.add(intf);
				for (final Class<?> iIntf : getAllInterfaces(intf)) {
					allInterfaces.add(iIntf);
				}
			}
			root = root.getSuperclass();
		}
		return Collections.unmodifiableSet(allInterfaces);
	}

	/**
	 * Retrieves the generic type of a parameterized field.
	 * It is expected that there is ONE and only ONE declared generic.
	 * Example:
	 * List<Car> => Car
	 * Option<Car> => Car
	 * @param constructor Constructor
	 * @param i Index of the parameter in the component
	 * @return Class of the generic type
	 */
	public static Class<?> getGeneric(final Constructor<?> constructor, final int i) {
		Assertion.check()
				.isNotNull(constructor);
		//---
		return getGeneric(
				constructor.getGenericParameterTypes()[i],
				() -> new UnsupportedOperationException("Generic type detection could not be performed on constructor " + constructor));
	}

	/**
	 * Retrieves the generic type of a parameterized method.
	 * It is expected that there is ONE and only ONE declared generic.
	 * Example:
	 * List<Car> => Car
	 * Option<Car> => Car
	 * @param method Method
	 * @param i Index of the parameter in the component
	 * @return Class of the generic type
	 */
	public static Class<?> getGeneric(final Method method, final int i) {
		Assertion.check()
				.isNotNull(method);
		//---
		return getGeneric(
				method.getGenericParameterTypes()[i],
				() -> new UnsupportedOperationException("Generic type detection could not be performed on method " + method.getDeclaringClass() + "." + method.getName()));
	}

	/**
	 * Retrieves the generic type of a parameterized field.
	 * It is expected that there is ONE and only ONE declared generic.
	 * Example:
	 * List<Car> => Car
	 * Option<Car> => Car
	 * @param field Field
	 * @return Class of the generic type
	 */
	public static Class<?> getGeneric(final Field field) {
		Assertion.check()
				.isNotNull(field);
		//---
		return getGeneric(field.getGenericType(),
				() -> new UnsupportedOperationException("Generic type detection could not be performed on field " + field.getName()));
	}

	/**
	 * Finds the generic type.
	 * Ex: List<Car> ==> Car
	 * @param type Type
	 * @param exceptionSupplier Supplier for the exception
	 * @return The first generic of this class
	 */
	private static Class<?> getGeneric(
			final Type type,
			final Supplier<RuntimeException> exceptionSupplier) {
		Assertion.check()
				.isNotNull(type)
				.isNotNull(exceptionSupplier);
		//---
		if (type instanceof final ParameterizedType parameterizedType) {
			Assertion.check()
					.isTrue(parameterizedType.getActualTypeArguments().length == 1, "There must be exactly one declared generic");
			final Type optionType = parameterizedType.getActualTypeArguments()[0];
			if (optionType instanceof Class) {
				return (Class<?>) optionType;
			} else if (optionType instanceof ParameterizedType) {
				// Case where the parameterized type is itself parameterized
				return (Class<?>) ((ParameterizedType) optionType).getRawType();
			}
		}
		throw exceptionSupplier.get();
	}
}
