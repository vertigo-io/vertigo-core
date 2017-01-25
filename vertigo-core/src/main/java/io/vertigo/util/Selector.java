package io.vertigo.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.reflections.Reflections;
import org.reflections.scanners.TypeElementsScanner;

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
	private static final Predicate ALWAYS_TRUE = o -> true;
	private final Map<String, Class> scope = new HashMap<>();

	private Predicate<Method> methodPredicates = ALWAYS_TRUE;
	private Predicate<Class> classPredicates = ALWAYS_TRUE;
	private Predicate<Field> fieldPredicates = ALWAYS_TRUE;

	private boolean scoped;

	private void checkScope() {

		Assertion.checkState(!scoped, "Classes cannot be added to scope after filtering");
	}

	/**
	 * Add a class to the scope.
	 * @param clazz the class to add
	 * @return the selector
	 */
	public Selector from(final Class clazz) {
		Assertion.checkNotNull(clazz);
		checkScope();
		// ---
		scope.put(clazz.getName(), clazz);
		return this;
	}

	/**
	 * Adds a collection of class to the scope provided by the given supplier.
	 * @param classesSupplier a supplier of classes
	 * @return the selector
	 */
	public Selector from(final Supplier<Collection<Class>> classesSupplier) {
		Assertion.checkNotNull(classesSupplier);
		checkScope();
		// ---
		from(classesSupplier.get());
		return this;
	}

	/**
	 * Adds a collection of class to the scope.
	 * @param classes a supplier of classes
	 * @return the selector
	 */
	public Selector from(final Iterable<Class> classes) {
		Assertion.checkNotNull(classes);
		checkScope();
		// ---
		classes.forEach(clazz -> from(clazz));
		return this;
	}

	/**
	 * Add all the classes with a package prefix in the scope.
	 * @param packageName the root package
	 * @return the selector
	 */
	public Selector from(final String packageName) {
		Assertion.checkArgNotEmpty(packageName);
		checkScope();
		// ---
		new Reflections(packageName,
				new TypeElementsScanner().includeAnnotations(false).includeFields(false).includeMethods(false))
						.getStore()
						.get(TypeElementsScanner.class.getSimpleName())
						.keys()
						.forEach(className -> from(ClassUtil.classForName(className)));
		return this;
	}

	/**
	 * Filter field with a predicate.
	 * @param fieldPredicate the predicate
	 * @return the selector
	 */
	public Selector filterFields(final Predicate<Field> fieldPredicate) {
		Assertion.checkNotNull(fieldPredicate);
		scoped = true;
		// ---
		fieldPredicates = fieldPredicates.and(fieldPredicate);
		return this;
	}

	/**
	 * Filter method with a predicate.
	 * @param methodPredicate the predicate
	 * @return the selector
	 */
	public Selector filterMethods(final Predicate<Method> methodPredicate) {
		Assertion.checkNotNull(methodPredicate);
		scoped = true;
		// ---
		methodPredicates = methodPredicates.and(methodPredicate);
		return this;
	}

	/**
	 * Filter classes with a predicate.
	 * @param classPredicate the predicate
	 * @return the selector
	 */
	public Selector filterClasses(final Predicate<Class> classPredicate) {
		Assertion.checkNotNull(classPredicate);
		scoped = true;
		// ---
		classPredicates = classPredicates.and(classPredicate);
		return this;
	}

	/**
	 * Find the classes matching the requirements and with method matching the requirements.
	 * @return the classes matching the selector
	 */
	public Collection<Class> findClasses() {
		return scope.values()
				.stream()
				.filter(classPredicates)
				.filter(filterClassesBasedOnMethods())
				.filter(filterClassesBasedOnFields())
				.collect(Collectors.toList());
	}

	/**
	 * Find the methods matching the requirements and with method matching the requirements.
	 * @return the classes matching the selector
	 */
	public Collection<Tuple2<Class, Method>> findMethods() {

		return scope.values()
				.stream()
				.filter(classPredicates)
				.filter(filterClassesBasedOnFields())
				.flatMap((clazz) -> Stream.<Method> of(clazz.getDeclaredMethods()))
				.filter(methodPredicates)
				.map(method -> new Tuple2<>(Class.class.cast(method.getDeclaringClass()), method))
				.collect(Collectors.toList());
	}

	/**
	 * Find the fields matching the requirements with the associatedClass.
	 * @return the classes matching the selector
	 */
	public Collection<Tuple2<Class, Field>> findFields() {
		return scope.values()
				.stream()
				.filter(classPredicates)
				.filter(filterClassesBasedOnMethods())
				.flatMap((clazz) -> Stream.<Field> of(clazz.getDeclaredFields()))
				.filter(fieldPredicates)
				.map((field) -> new Tuple2<>(Class.class.cast(field.getDeclaringClass()), field))
				.collect(Collectors.toList());
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
	 * Condition for selecting a method.
	 * @author mlaroche
	 *
	 */
	public static final class MethodConditions {
		private MethodConditions() {
			//stateless
		}

		/**
		 * Build a predicate to check if the method is Annotated.
		 * @param annotationClass the annotation
		 * @return the predicate
		 */
		public static Predicate<Method> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.checkNotNull(annotationClass);
			//---
			return method -> method.getAnnotationsByType(annotationClass).length > 0;
		}
	}

	/**
	 * Condition for selecting a method.
	 * @author mlaroche
	 *
	 */
	public static final class FieldConditions {
		private FieldConditions() {
			//stateless
		}

		/**
		 * Build a predicate to check if the field is Annotated.
		 * @param annotationClass the annotation
		 * @return the predicate
		 */
		public static Predicate<Field> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.checkNotNull(annotationClass);
			//---
			return field -> field.getAnnotationsByType(annotationClass).length > 0;
		}
	}

	/**
	 * Conditions for selecting a class.
	 * @author mlaroche
	 *
	 */
	public static final class ClassConditions {
		private ClassConditions() {
			//stateless
		}

		/**
		 * Build a predicate to check if the classe is Annotated.
		 * @param annotationClass the annotation
		 * @return the predicate
		 */
		public static Predicate<Class> annotatedWith(final Class<? extends Annotation> annotationClass) {
			Assertion.checkNotNull(annotationClass);
			//---
			return clazz -> clazz.getAnnotationsByType(annotationClass).length > 0;
		}

		/**
		 * Build a predicate to check if the classe is a subtype of the given class.
		 * @param clazz the annotation
		 * @return the predicate
		 */
		public static Predicate<Class> subTypeOf(final Class clazz) {
			Assertion.checkNotNull(clazz);
			//---
			return subtype -> clazz.isAssignableFrom(subtype);
		}

		/**
		 * Build a predicate to check if the classe is an interface.
		 * @return the predicate
		 */
		public static Predicate<Class> interfaces() {
			return Class::isInterface;
		}
	}
}
