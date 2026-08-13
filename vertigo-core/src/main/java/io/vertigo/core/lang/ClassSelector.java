/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang;

import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Predicate;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import io.vertigo.core.util.ClassUtil;

/**
 * Utility class for selecting and filtering classes through reflection.
 * Provides a fluent API for:
 * - 1. Defining class scope by packages or explicit classes
 * - 2. Filtering classes, methods and fields by predicates
 * - 3. Finding matching elements with type safety
 * - Supporting annotation-based filtering
 * - Enabling component scanning and introspection
 *
 * Usage example:
 * ClassSelector.from("com.example")
 *   .filterClasses(ClassConditions.annotatedWith(MyAnnotation.class))
 *   .filterMethods(MethodConditions.annotatedWith(MyMethodAnnotation.class))
 *   .findMethods();
 *
 * @author mlaroche
 */
public final class ClassSelector {
	private static final Predicate ALWAYS_TRUE = o -> true;
	private final Set<Class> classes;

	private Predicate<Method> methodPredicates = ALWAYS_TRUE;
	private Predicate<Class> classPredicates = ALWAYS_TRUE;
	private Predicate<Field> fieldPredicates = ALWAYS_TRUE;

	private ClassSelector(final Set<Class> classes) {
		Assertion.check().isNotNull(classes);
		//---
		this.classes = classes;
	}

	/**
	 * Adds a set of classes to the scope.
	 *
	 * @param classes a supplier of classes
	 * @return the selector
	 */
	public static ClassSelector from(final Collection<Class> classes) {
		Assertion.check().isNotNull(classes);
		//---
		return new ClassSelector(new HashSet(classes));
	}

	/**
	 * Adds a class to the scope.
	 *
	 * @param clazz the class to add
	 * @return the selector
	 */
	public static ClassSelector from(final Class clazz) {
		Assertion.check().isNotNull(clazz);
		//---
		return from(Set.of(clazz));
	}

	/**
	 * Adds all the classes with a package prefix in the scope.
	 *
	 * @param packageName the root package
	 * @return the selector
	 */
	public static ClassSelector from(final String packageName) {
		Assertion.check().isNotBlank(packageName);
		// ---
		final String packagePath = packageName.replace('.', '/');
		final Set<Class> classes = new HashSet<>();
		try {
			final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
			final Enumeration<URL> resources = classLoader.getResources(packagePath);
			while (resources.hasMoreElements()) {
				final URL resource = resources.nextElement();
				if ("file".equals(resource.getProtocol())) {
					scanDirectory(new File(resource.toURI()), packageName, classes);
				} else if ("jar".equals(resource.getProtocol())) {
					scanJar(resource, packagePath, classes);
				}
			}
		} catch (final Exception e) {
			throw WrappedException.wrap(e);
		}
		return from(classes);
	}

	private static void scanDirectory(final File directory, final String packageName, final Set<Class> classes) {
		if (!directory.exists()) {
			return;
		}
		final File[] files = directory.listFiles();
		if (files == null) {
			return;
		}
		for (final File file : files) {
			if (file.isDirectory()) {
				scanDirectory(file, packageName + "." + file.getName(), classes);
			} else if (file.getName().endsWith(".class") && !file.getName().equals("package-info.class") && !file.getName().equals("module-info.class")) {
				final String className = packageName + "." + file.getName().substring(0, file.getName().length() - 6);
				classes.add(ClassUtil.classForName(className));
			}
		}
	}

	private static void scanJar(final URL resource, final String packagePath, final Set<Class> classes) throws IOException {
		final JarURLConnection jarConnection = (JarURLConnection) resource.openConnection();
		try (final JarFile jar = jarConnection.getJarFile()) {
			final Enumeration<JarEntry> entries = jar.entries();
			while (entries.hasMoreElements()) {
				final JarEntry entry = entries.nextElement();
				final String name = entry.getName();
				if (name.startsWith(packagePath + "/") && name.endsWith(".class")
						&& !name.endsWith("package-info.class") && !name.endsWith("module-info.class")) {
					final String className = name.substring(0, name.length() - 6).replace('/', '.');
					classes.add(ClassUtil.classForName(className));
				}
			}
		}
	}

	/**
	 * Filters field with a predicate.
	 * @param fieldPredicate the predicate
	 * @return the selector
	 */
	public ClassSelector filterFields(final Predicate<Field> fieldPredicate) {
		Assertion.check().isNotNull(fieldPredicate);
		// ---
		fieldPredicates = fieldPredicates.and(fieldPredicate);
		return this;
	}

	/**
	 * Filters method with a predicate.
	 * @param methodPredicate the predicate
	 * @return the selector
	 */
	public ClassSelector filterMethods(final Predicate<Method> methodPredicate) {
		Assertion.check().isNotNull(methodPredicate);
		// ---
		methodPredicates = methodPredicates.and(methodPredicate);
		return this;
	}

	/**
	 * Filters classes with a predicate.
	 * @param classPredicate the predicate
	 * @return the selector
	 */
	public ClassSelector filterClasses(final Predicate<Class> classPredicate) {
		Assertion.check().isNotNull(classPredicate);
		// ---
		classPredicates = classPredicates.and(classPredicate);
		return this;
	}

	/**
	 * Find the classes matching the requirements and with method matching the requirements.
	 * @return the classes matching the selector
	 */
	public Collection<Class> findClasses() {
		return classes
				.stream()
				.filter(classPredicates)
				.filter(filterClassesBasedOnMethods())
				.filter(filterClassesBasedOnFields())
				.toList();
	}

	/**
	 * Find the methods matching the requirements and with method matching the requirements.
	 * @return the classes matching the selector
	 */
	public Collection<Tuple<Class, Method>> findMethods() {
		return classes
				.stream()
				.filter(classPredicates)
				.filter(filterClassesBasedOnFields())
				.flatMap(clazz -> Stream.of(clazz.getDeclaredMethods()))
				.filter(methodPredicates)
				.map(method -> Tuple.of(Class.class.cast(method.getDeclaringClass()), method))
				.toList();
	}

	/**
	 * Finds the fields matching the requirements with the associatedClass.
	 * @return the classes matching the selector
	 */
	public Collection<Tuple<Class, Field>> findFields() {
		return classes
				.stream()
				.filter(classPredicates)
				.filter(filterClassesBasedOnMethods())
				.flatMap(clazz -> Stream.of(clazz.getDeclaredFields()))
				.filter(fieldPredicates)
				.map(field -> Tuple.of(Class.class.cast(field.getDeclaringClass()), field))
				.toList();
	}

	private Predicate<Class> filterClassesBasedOnMethods() {
		return clazz -> {
			//We don't want to load all declared methods if we don't care
			if (ALWAYS_TRUE.equals(methodPredicates) || clazz.getDeclaredMethods().length == 0) {
				// no methodPredicate
				// or no declaring method
				// so we keep it
				return true;
			}
			// methods are declared so we check if a method match the requirements
			return Stream.of(clazz.getDeclaredMethods()).anyMatch(methodPredicates);
		};
	}

	private Predicate<Class> filterClassesBasedOnFields() {
		return clazz -> {
			//We don't want to load all field if we don't care
			if (ALWAYS_TRUE.equals(fieldPredicates) || clazz.getDeclaredFields().length == 0) {
				// no fieldPredicates
				// or no declaring field
				// so we keep it
				return true;
			}
			// fields are declared so we check if a field match the requirements
			return Stream.of(clazz.getDeclaredFields()).anyMatch(fieldPredicates);
		};
	}

	/**
	 * Utility class for method selection predicates.
	 */
	public static final class MethodConditions {
		private MethodConditions() {
			//stateless
		}

		/**
		 * Creates predicate for methods with specific annotation.
		 * @param annotationClass Target annotation
		 * @return Predicate for method selection
		 */
		public static Predicate<Method> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.check().isNotNull(annotationClass);
			//---
			return method -> method.getAnnotationsByType(annotationClass).length > 0;
		}
	}

	/**
	 * Utility class for field selection predicates.
	 */
	public static final class FieldConditions {
		private FieldConditions() {
			//stateless
		}

		/**
		 * Creates predicate for fields with specific annotation.
		 * @param annotationClass Target annotation
		 * @return Predicate for field selection
		 */
		public static Predicate<Field> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.check().isNotNull(annotationClass);
			//---
			return field -> field.getAnnotationsByType(annotationClass).length > 0;
		}
	}

	/**
	 * Utility class for class selection predicates.
	 */
	public static final class ClassConditions {
		private ClassConditions() {
			//stateless
		}

		/**
		 * Creates predicate for classes with specific annotation.
		 * @param annotationClass Target annotation
		 * @return Predicate for class selection
		 */
		public static Predicate<Class> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.check().isNotNull(annotationClass);
			//---
			return clazz -> clazz.getAnnotationsByType(annotationClass).length > 0;
		}

		/**
		 * Creates predicate for classes extending or implementing a type.
		 * @param clazz Base type to check
		 * @return Predicate for class selection
		 */
		public static Predicate<Class> subTypeOf(final Class clazz) {
			Assertion.check().isNotNull(clazz);
			//---
			return clazz::isAssignableFrom;
		}

		/**
		 * Creates predicate for abstract classes (excluding interfaces).
		 * @return Predicate for class selection
		 */
		public static Predicate<Class> isAbstract() {
			return clazz -> !clazz.isInterface() && Modifier.isAbstract(clazz.getModifiers());
		}

		/**
		 * Creates predicate for interface types.
		 * @return Predicate for class selection
		 */
		public static Predicate<Class> interfaces() {
			return Class::isInterface;
		}
	}
}
