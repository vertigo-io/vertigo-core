package io.vertigo.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuples.Tuple2;

/**
 * Selector of classes and methods.
 *
 * 1. Define  a scope/set of classes
 *  - by addind them
 *  - by their packages
 *
 * 2. filter
 *  - classes
 *  - methods
 *
 * 3 - find your classes or methods.
 *
 * @author mlaroche
 */
public final class Selector {
	private static final Predicate ALWAYS_TRUE = (o) -> true;
	private final Map<String, Class> scope = new HashMap<>();

	private Predicate<Method> methodPredicates = ALWAYS_TRUE;
	private Predicate<Class> classPredicates = ALWAYS_TRUE;

	private boolean scoped;

	private void checkScope() {

		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
	}

	public Selector from(final Class clazz) {
		Assertion.checkNotNull(clazz);
		checkScope();
		// ---
		scope.put(clazz.getName(), clazz);
		return this;
	}

	public Selector from(final Supplier<Collection<Class>> classesSupplier) {
		Assertion.checkNotNull(classesSupplier);
		checkScope();
		// ---
		from(classesSupplier.get());
		return this;
	}

	public Selector from(final Collection<Class> classes) {
		Assertion.checkNotNull(classes);
		checkScope();
		// ---
		classes.forEach((clazz) -> from(clazz));
		return this;
	}

	public Selector from(final String packageName) {
		Assertion.checkArgNotEmpty(packageName);
		checkScope();
		// ---
		new Reflections(packageName, new SubTypesScanner(false)).getAllTypes().forEach(className -> from(ClassUtil.classForName(className)));
		return this;
	}

	public Selector filterMethods(final Predicate<Method> methodPredicate) {
		Assertion.checkNotNull(methodPredicate);
		scoped = true;
		// ---
		methodPredicates = methodPredicates.and(methodPredicate);
		return this;
	}

	public Selector filterClasses(final Predicate<Class> classPredicate) {
		Assertion.checkNotNull(classPredicate);
		scoped = true;
		// ---
		classPredicates = classPredicates.and(classPredicate);
		return this;
	}

	public Collection<Class> findClasses() {
		return scope.values()
				.stream()
				.filter(classPredicates)
				.filter((clazz) -> {
					//We don't want to load all declared methods if we don't care
					if (methodPredicates == ALWAYS_TRUE || clazz.getDeclaredMethods().length == 0) {
						// no methodPredicate
						// or no declaring method
						// so we keep it
						return true;
					}
					// methods are declared so we check if a method match the requirements
					return Stream.of(clazz.getDeclaredMethods()).anyMatch(methodPredicates);
				})
				.collect(Collectors.toList());
	}

	public Collection<Tuple2<Class, Method>> findMethods() {
		return scope.values()
				.stream()
				.filter(classPredicates)
				.flatMap((clazz) -> Stream.of(clazz.getDeclaredMethods()))
				.filter(methodPredicates)
				.map((method) -> new Tuple2<>(Class.class.cast(method.getDeclaringClass()), method))
				.collect(Collectors.toList());
	}

	/**
	 * Condition for selecting a method.
	 * @author mlaroche
	 *
	 */
	public static final class MethodConditions {
		public static Predicate<Method> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.checkNotNull(annotationClass);
			//---
			return (method) -> method.getAnnotationsByType(annotationClass).length > 0;
		}
	}

	/**
	 * Conditions for selecting a class.
	 * @author mlaroche
	 *
	 */
	public static final class ClassConditions {
		public static Predicate<Class> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.checkNotNull(annotationClass);
			//---
			return (clazz) -> clazz.getAnnotationsByType(annotationClass).length > 0;
		}

		public static Predicate<Class> subTypeOf(final Class clazz) {
			Assertion.checkNotNull(clazz);
			//---
			return (subtype) -> clazz.isAssignableFrom(subtype);
		}

		public static Predicate<Class> interfaces() {
			return (clazz) -> clazz.isInterface();
		}
	}
}
