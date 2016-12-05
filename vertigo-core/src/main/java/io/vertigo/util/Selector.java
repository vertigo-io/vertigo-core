package io.vertigo.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
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

	private  Predicate<Method> methodPredicate= (m) -> true ;
	private Predicate<Class>  classPredicate = (c) -> true;

	private boolean scoped;

	public Selector add(final Class clazz) {
		Assertion.checkNotNull(clazz);
		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
		// ---
		scope.put(clazz.getName(), clazz);
		return this;
	}

	public Selector addAll(final Collection<Class> classes) {
		Assertion.checkNotNull(classes);
		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
		// ---
		classes.forEach((clazz) -> add(clazz));
		return this;
	}

	public Selector includeTree(final String packageName) {
		Assertion.checkArgNotEmpty(packageName);
		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
		// ---
		new Reflections(packageName, new SubTypesScanner(false)).getAllTypes().forEach(className -> add(ClassUtil.classForName(className)));
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

	public Collection<Class> find() {
		return Stream.concat(findClasses().stream(), findMethods().stream()
				.map((final Tuple2<Class, Method> tuple) -> tuple.getVal1()))
				.distinct()
				.collect(Collectors.toList());
	}

	public Collection<Class> findClasses() {
		return scope.values().stream()
				.filter(classPredicate)
				.collect(Collectors.toList());
	}

	public Collection<Tuple2<Class, Method>> findMethods() {
		return scope.values().stream()
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

		public ClassConditions or(final ClassConditions classCondition) {
			Assertion.checkNotNull(classCondition);
			//---
			return new ClassConditions(classCondition.getPredicate().or(predicate));
		}
	}
}
