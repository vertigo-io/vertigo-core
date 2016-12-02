package io.vertigo.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
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
 * @author mlaroche
 *
 */
public class Selector {

	private final Map<String, Class> scope = new HashMap<>();

	private final List<MethodConditions> methodConditions = new ArrayList<>();
	private final List<ClassConditions> classConditions = new ArrayList<>();

	private boolean scoped = false;

	Selector add(final Class clazz) {
		Assertion.checkNotNull(clazz);
		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
		// ---
		scope.put(clazz.getName(), clazz);
		return this;
	}

	Selector addAll(final Collection<Class> classes) {
		Assertion.checkNotNull(classes);
		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
		// ---
		classes.forEach((clazz) -> add(clazz));
		return this;
	}

	Selector includeTree(final String packageName) {
		Assertion.checkArgNotEmpty(packageName);
		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
		// ---
		new Reflections(packageName, new SubTypesScanner(false)).getAllTypes().forEach(className -> add(ClassUtil.classForName(className)));
		return this;
	}

	Selector filter(final MethodConditions methodCondition) {
		scoped = true;
		// ---
		methodConditions.add(methodCondition);
		return this;
	}

	Selector filter(final ClassConditions classCondition) {
		scoped = true;
		// ---
		classConditions.add(classCondition);
		return this;
	}

	Collection<Class> find() {
		return Stream.concat(findClasses().stream(), findMethods().stream()
				.map((final Tuple2<Class, Method> tuple) -> tuple.getVal1()))
				.distinct()
				.collect(Collectors.toList());

	}

	Collection<Class> findClasses() {
		final Predicate<Class> allPredicate = classConditions.stream()
				.map(classCondition -> classCondition.getPredicate())
				.reduce((method) -> true, (predicate1, predicate2) -> predicate1.and(predicate2));

		return scope.values().stream()
				.filter(allPredicate)
				.collect(Collectors.toList());
	}

	Collection<Tuple2<Class, Method>> findMethods() {
		final Predicate<Method> allMethodPredicate = methodConditions.stream()
				.map(classCondition -> classCondition.getPredicate())
				.reduce((method) -> true, (predicate1, predicate2) -> predicate1.and(predicate2));
		return scope.values().stream()
				.flatMap((clazz) -> Stream.of(clazz.getDeclaredMethods()))
				.filter(allMethodPredicate)
				.map((method) -> new Tuple2<>(Class.class.cast(method.getDeclaringClass()), method))
				.collect(Collectors.toList());

	}

	/**
	 * Condition for selecting a method.
	 * @author mlaroche
	 *
	 */
	public static class MethodConditions {

		private final Predicate<Method> predicate;

		private MethodConditions(final Predicate<Method> predicate) {
			this.predicate = predicate;
		}

		Predicate<Method> getPredicate() {
			return predicate;
		}

		static MethodConditions annotatedWith(final Class<? extends Annotation> annotationClass) {
			return new MethodConditions((method) -> method.getAnnotationsByType(annotationClass).length > 0);
		}

		MethodConditions or(final MethodConditions methodCondition) {
			return new MethodConditions(methodCondition.getPredicate().or(predicate));
		}
	}

	/**
	 * Condition for selecting a class.
	 * @author mlaroche
	 *
	 */
	public static class ClassConditions {

		private final Predicate<Class> predicate;

		private ClassConditions(final Predicate<Class> predicate) {
			this.predicate = predicate;
		}

		Predicate<Class> getPredicate() {
			return predicate;
		}

		static ClassConditions annotatedWith(final Class<? extends Annotation> annotationClass) {
			return new ClassConditions((clazz) -> clazz.getAnnotationsByType(annotationClass).length > 0);
		}

		static ClassConditions subTypeOf(final Class clazz) {
			return new ClassConditions((subtype) -> clazz.isAssignableFrom(subtype));
		}

		ClassConditions or(final ClassConditions classCondition) {
			return new ClassConditions(classCondition.getPredicate().or(predicate));
		}
	}

}
