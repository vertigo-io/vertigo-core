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
 *
 */
public final class Selector {
	private final Map<String, Class> scope = new HashMap<>();

	private Predicate<Method> methodPredicate = (m) -> true;
	private Predicate<Class> classPredicate = (c) -> true;

	private boolean scoped;

	public Selector from(final Supplier<Collection<Class>> classesSuplier) {
		Assertion.checkNotNull(classesSuplier);
		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
		// ---
		from(classesSuplier.get());
		return this;
	}

	public Selector from(final Class clazz) {
		Assertion.checkNotNull(clazz);
		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
		// ---
		scope.put(clazz.getName(), clazz);
		return this;
	}

	public Selector from(final Collection<Class> classes) {
		Assertion.checkNotNull(classes);
		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
		// ---
		classes.forEach((clazz) -> from(clazz));
		return this;
	}

	public Selector from(final String packageName) {
		Assertion.checkArgNotEmpty(packageName);
		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
		// ---
		new Reflections(packageName, new SubTypesScanner(false)).getAllTypes().forEach(className -> from(ClassUtil.classForName(className)));
		return this;
	}

	public Selector filter(final MethodConditions methodCondition) {
		scoped = true;
		// ---
		methodPredicate = methodPredicate.and(methodCondition.getPredicate());
		return this;
	}

	public Selector filter(final ClassConditions classCondition) {
		scoped = true;
		// ---
		classPredicate = classPredicate.and(classCondition.getPredicate());
		return this;
	}

	public Collection<Class> findClasses() {
		return scope.values().stream()
				.filter(classPredicate)
				.filter((clazz) -> {
					if (clazz.getDeclaredMethods().length == 0) {
						// no declaring method so we keep it
						return true;
					}
					// methods are declared so we check if a method match the requirements
					return Stream.of(clazz.getDeclaredMethods()).anyMatch(methodPredicate);
				}).collect(Collectors.toList());
	}

	public Collection<Tuple2<Class, Method>> findMethods() {
		return scope.values().stream()
				.filter(classPredicate)
				.flatMap((clazz) -> Stream.of(clazz.getDeclaredMethods()))
				.filter(methodPredicate)
				.map((method) -> new Tuple2<>(Class.class.cast(method.getDeclaringClass()), method))
				.collect(Collectors.toList());

	}

	/**
	 * Condition for selecting a method.
	 * @author mlaroche
	 *
	 */
	public static final class MethodConditions {
		private final Predicate<Method> predicate;

		private MethodConditions(final Predicate<Method> predicate) {
			Assertion.checkNotNull(predicate);
			//---
			this.predicate = predicate;
		}

		Predicate<Method> getPredicate() {
			return predicate;
		}

		public static MethodConditions annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.checkNotNull(annotationClass);
			//---
			return new MethodConditions((method) -> method.getAnnotationsByType(annotationClass).length > 0);
		}

		public MethodConditions or(final MethodConditions methodCondition) {
			Assertion.checkNotNull(methodCondition);
			//---
			return new MethodConditions(methodCondition.getPredicate().or(predicate));
		}

		public static MethodConditions not(final MethodConditions methodCondition) {
			Assertion.checkNotNull(methodCondition);
			//---
			return new MethodConditions(methodCondition.getPredicate().negate());
		}
	}

	/**
	 * Conditions for selecting a class.
	 * @author mlaroche
	 *
	 */
	public static final class ClassConditions {
		private final Predicate<Class> predicate;

		private ClassConditions(final Predicate<Class> predicate) {
			Assertion.checkNotNull(predicate);
			//---
			this.predicate = predicate;
		}

		Predicate<Class> getPredicate() {
			return predicate;
		}

		public static ClassConditions annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.checkNotNull(annotationClass);
			//---
			return new ClassConditions((clazz) -> clazz.getAnnotationsByType(annotationClass).length > 0);
		}

		public static ClassConditions subTypeOf(final Class clazz) {
			Assertion.checkNotNull(clazz);
			//---
			return new ClassConditions((subtype) -> clazz.isAssignableFrom(subtype));
		}

		public static ClassConditions isInterface() {
			return new ClassConditions((clazz) -> clazz.isInterface());
		}

		public ClassConditions or(final ClassConditions classCondition) {
			Assertion.checkNotNull(classCondition);
			//---
			return new ClassConditions(classCondition.getPredicate().or(predicate));
		}

		public static ClassConditions not(final ClassConditions classCondition) {
			Assertion.checkNotNull(classCondition);
			//---
			return new ClassConditions(classCondition.getPredicate().negate());
		}
	}
}
