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
package io.vertigo.kernel.util;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;

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
import java.util.List;

/**
 * Centralisation des créations d'instances à partir d'une nom de classe. Cette approche étant utilisée pour créer des liens plus souples entre des objets.
 * 
 * @author pchretien
 */
public final class ClassUtil {
	private static final Class<?>[] EMPTY_CLAZZ_ARRAY = new Class[0];

	/**
	 * Constructeur priv� pour classe utilitaire
	 */
	private ClassUtil() {
		// RAS
	}

	/**
	 * Cr�ation d'une nouvelle instance non typ�e via un nom de classe (constructeur vide).
	 * Veuillez privil�gier les m�thodes retournat une instance typ� d�s que le type est connu.
	 * @param javaClassName Nom de la classe
	 * @return Nouvelle instance
	 */
	public static Object newInstance(final String javaClassName) {
		final Class<?> javaClass = classForName(javaClassName);
		return newInstance(javaClass);
	}

	/**
	 * Cr�ation d'une nouvelle instance typ�e via un nom de classe (constructeur vide).
	 * 
	 * @param <J> Type de l'instance retourn�e
	 * @param javaClassName Nom de la classe
	 * @param  type Type retourn�
	 * @return Nouvelle instance
	 */
	public static <J> J newInstance(final String javaClassName, final Class<J> type) {
		final Class<? extends J> javaClass = classForName(javaClassName, type);
		return newInstance(javaClass);
	}

	/**
	 * Cr�ation d'une nouvelle instance typ�e via une classe (constructeur vide).
	 * 
	 * @param <J> Type de l'instance retourn�e
	 * @param clazz Classe
	 * @return Nouvelle instance
	 */
	public static <J> J newInstance(final Class<J> clazz) {
		final Constructor<? extends J> constructor = findConstructor(clazz);
		return newInstance(constructor, EMPTY_CLAZZ_ARRAY);
	}

	/**
	 * Cr�ation d'une nouvelle instance typ�e via un constructeur et ses arguments.
	 * 
	 * @param <J> Type de l'instance retourn�e
	 * @param constructor Constructeur
	 * @param args Arguments de la construction
	 * @return Nouvelle instance
	 */
	public static <J> J newInstance(final Constructor<J> constructor, final Object[] args) {
		Assertion.checkNotNull(constructor);
		Assertion.checkNotNull(args);
		// ----------------------------------------------------------------------
		try {
			return constructor.newInstance(args);
		} catch (final InvocationTargetException e) {
			throw handle(e, "Erreur lors de l'appel au constructeur de la classe: {0} ", constructor.getDeclaringClass());
		} catch (final java.lang.IllegalAccessException e) {
			throw new VRuntimeException("Acc�s final impossible � la classe : {0}", e, constructor.getDeclaringClass().getName());
		} catch (final Exception e) {
			throw new VRuntimeException("Instanciation impossible de la classe : {0}", e, constructor.getDeclaringClass().getName());
		}
	}

	private static RuntimeException handle(final InvocationTargetException e, final String msg, final Object... params) {
		final Throwable t = e.getTargetException();
		if (t instanceof RuntimeException) {
			return (RuntimeException) t;
		}
		//		if (t instanceof Error) {
		//			return (Error) t;
		//		}
		return new VRuntimeException(msg, e, params);

	}

	/**
	 * R�cup�re le constructeur sans param�tres. 
	 * @param clazz Classe sur laquelle on recherche le constructeur
	 * @return Constructeur recherch� 
	 */
	private static <J> Constructor<J> findConstructor(final Class<J> clazz) {
		return findConstructor(clazz, EMPTY_CLAZZ_ARRAY);
	}

	/**
	* R�cup�re le constructeur correspondant � la signature indiqu�e. 
	* @param clazz Classe sur laquelle on recherche le constructeur
	* @param parameterTypes Signature du constructeur recherch�
	* @return Constructeur recherch� 
	*/
	public static <J> Constructor<J> findConstructor(final Class<J> clazz, final Class<?>[] parameterTypes) {
		Assertion.checkNotNull(clazz);
		Assertion.checkNotNull(parameterTypes);
		//---------------------------------------------------------------------	
		try {
			return clazz.getConstructor(parameterTypes);
		} catch (final NoSuchMethodException e) {
			if (parameterTypes.length == 0) {
				//Dans le cas des constructeur vide (sans param�tre), on lance un message plus simple.
				throw new VRuntimeException("Aucun constructeur vide trouv� sur {0} ", e, clazz.getSimpleName());
			}
			throw new VRuntimeException("Aucun constructeur trouv� sur {0} avec la signature {1}", e, clazz.getSimpleName(), parameterTypes);
		}
	}

	/**
	 * R�cup�ration d'une classe non typ�e � partir de son nom.
	 * 
	 * @param javaClassName Nom de la classe
	 * @return Classe java
	 */
	public static Class<?> classForName(final String javaClassName) {
		Assertion.checkArgNotEmpty(javaClassName);
		// ----------------------------------------------------------------------
		try {
			return Class.forName(javaClassName);
		} catch (final ClassNotFoundException e) {
			throw new VRuntimeException("Impossible de trouver la classe : {0}", e, javaClassName);
		}
	}

	/**
	 * R�cup�ration d'une classe typ�e � partir de son nom.
	 * 
	 * @param <J> Type de l'instance retourn�e
	 * @param javaClassName Nom de la classe
	 * @param type Type.
	 * @return Classe java
	 */
	public static <J> Class<? extends J> classForName(final String javaClassName, final Class<J> type) {
		Assertion.checkNotNull(javaClassName);
		Assertion.checkNotNull(type);
		// ----------------------------------------------------------------------
		try {
			return Class.forName(javaClassName).asSubclass(type);
		} catch (final ClassNotFoundException e) {
			throw new VRuntimeException("Impossible de trouver la classe : {0}", e, javaClassName);
		}
	}

	/**
	 * Invocation dynamique d'une m�thode sur une instance.
	 * 
	 * @param instance Objet sur lequel est invoqu� la m�thode
	 * @param method Methode invoqu�e
	 * @param args Arguments
	 * @return R Valeur retourn�e par l'invocation
	 */
	public static Object invoke(final Object instance, final Method method, final Object... args) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(method);
		//--------------------------------------------------------------------
		try {
			return method.invoke(instance, args);
		} catch (final IllegalAccessException e) {
			throw new VRuntimeException("Acc�s impossible � la m�thode : {0} de {1}", e, method.getName(), method.getDeclaringClass().getName());
		} catch (final InvocationTargetException e) {
			throw handle(e, "Erreur lors de l'appel de la m�thode : {0} de {1}", method.getName(), method.getDeclaringClass().getName());
		}
	}

	/**
	 * Affectation dynamique de la valeur d'un champ (m�me priv�).
	 * 
	 * @param instance Objet sur lequel est invoqu� la m�thode
	 * @param field Champ concern�
	 * @param value Nouvelle valeur
	 */
	public static void set(final Object instance, final Field field, final Object value) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(field);
		//--------------------------------------------------------------------
		try {
			field.setAccessible(true);
			field.set(instance, value);
		} catch (final IllegalAccessException e) {
			throw new VRuntimeException("Acc�s impossible au champ : {0} de {1}", e, field.getName(), field.getDeclaringClass().getName());
		}
	}

	/**
	 * R�cup�ration dynamique de la valeur d'un champ.
	 * 
	 * @param instance Objet sur lequel est invoqu� la m�thode
	 * @param field Champ concern�
	 * @return Valeur
	 */
	public static Object get(final Object instance, final Field field) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(field);
		//--------------------------------------------------------------------
		try {
			field.setAccessible(true);
			return field.get(instance);
		} catch (final IllegalAccessException e) {
			throw new VRuntimeException("Acc�s impossible au champ : {0} de {1}", e, field.getName(), field.getDeclaringClass().getName());
		}
	}

	/**
	 * R�cup�re la m�thode correspondant au nom et � la signature indiqu�e parmi les m�thodes pass�es. 
	 * @param clazz Classe sur laquelle on recherche les m�thodes
	 * @param methodName Nom de la m�thode recherch�e
	 * @param parameterTypes Signature de la m�thode recherch�e
	 * @return M�thode recherch�e 
	 */
	public static Method findMethod(final Class<?> clazz, final String methodName, final Class<?>... parameterTypes) {
		Assertion.checkNotNull(clazz);
		Assertion.checkNotNull(methodName);
		Assertion.checkNotNull(parameterTypes);
		//---------------------------------------------------------------------	
		try {
			return clazz.getMethod(methodName, parameterTypes);
		} catch (final NoSuchMethodException e) {
			throw new VRuntimeException("M�thode {0} non trouv�e sur {1}", e, methodName, clazz.getName());
		}
	}

	/**
	 * Retourne tous les champs d�clar�s (incluant les champs parents) et annot�s pour une classe donn�e.
	 * @param clazz Class
	 * @param annotation Annotation attendue 
	 * @return Tous les champs d�clar�s (incluant les champs parents)
	 */
	public static Collection<Field> getAllFields(final Class<?> clazz, final Class<? extends Annotation> annotation) {
		Assertion.checkNotNull(clazz);
		Assertion.checkNotNull(annotation);
		//---------------------------------------------------------------------	
		final List<Field> fields = new ArrayList<>();
		for (final Field field : ClassUtil.getAllFields(clazz)) {
			if (field.isAnnotationPresent(annotation)) {
				fields.add(field);
			}
		}
		return fields;
	}

	/**
	 * Retourne tous les champs d�clar�s (incluant les champs parents) pour une classe donn�e.
	 * @param clazz Class
	 * @return Tous les champs d�clar�s (incluant les champs parents)
	 */
	public static Collection<Field> getAllFields(final Class<?> clazz) {
		Assertion.checkNotNull(clazz);
		//---------------------------------------------------------------------	
		final List<Field> fields = new ArrayList<>();
		final Field[] declaredFields = clazz.getDeclaredFields();
		fields.addAll(Arrays.asList(declaredFields));
		final Class<?> parent = clazz.getSuperclass();
		if (parent != null) {
			fields.addAll(getAllFields(parent));
		}
		return Collections.unmodifiableCollection(fields);
	}

	public static Class<?>[] getAllInterfaces(final Class<?> clazz) {
		Assertion.checkNotNull(clazz);
		//---------------------------------------------------------------------	
		Class<?> root = clazz;
		final List<Class<?>> allInterfaces = new ArrayList<>();
		while (root != null) {
			for (final Class<?> intf : root.getInterfaces()) {
				if (!allInterfaces.contains(intf)) {
					allInterfaces.add(intf);
				}
				for (final Class<?> iIntf : getAllInterfaces(intf)) {
					if (!allInterfaces.contains(iIntf)) {
						allInterfaces.add(iIntf);
					}
				}
			}
			root = root.getSuperclass();
		}
		final Class<?>[] ia = new Class[allInterfaces.size()];
		return allInterfaces.toArray(ia);
	}

	/**
	 * R�cup�ration du type g�n�rique d'un champ param�tr�.
	 * Il convient qu'il y ait UN et un seul g�n�rique d�clar�.
	 * exemple  : 
	 * List<Voiture> => Voiture
	 * Option<Voiture> => Voiture
	 * @param constructor constructeur 
	 * @param i Index du param�tre dans le composant 
	 * @return Classe du type g�n�rique
	 */
	public static Class<?> getGeneric(final Constructor<?> constructor, final int i) {
		Assertion.checkNotNull(constructor);
		//---------------------------------------------------------------------	
		final Class<?> generic = getGeneric(constructor.getGenericParameterTypes()[i]);
		if (generic == null) {
			throw new UnsupportedOperationException("La d�tection du g�n�rique n'a pas pu �tre effectu�e sur le constructeur " + constructor);
		}
		return generic;
	}

	/**
	 * R�cup�ration du type g�n�rique d'un champ param�tr�.
	 * Il convient qu'il y ait UN et un seul g�n�rique d�clar�.
	 * exemple  : 
	 * List<Voiture> => Voiture
	 * Option<Voiture> => Voiture
	 * @param method method 
	 * @param i Index du param�tre dans le composant 
	 * @return Classe du type g�n�rique
	 */
	public static Class<?> getGeneric(final Method method, final int i) {
		Assertion.checkNotNull(method);
		//---------------------------------------------------------------------	
		final Class<?> generic = getGeneric(method.getGenericParameterTypes()[i]);
		if (generic == null) {
			throw new UnsupportedOperationException("La d�tection du g�n�rique n'a pas pu �tre effectu�e sur la methode " + method.getDeclaringClass() + "." + method.getName());
		}
		return generic;
	}

	/**
	 * R�cup�ration du type g�n�rique d'un champ param�tr�.
	 * Il convient qu'il y ait UN et un seul g�n�rique d�clar�.
	 * exemple  : 
	 * List<Voiture> => Voiture
	 * Option<Voiture> => Voiture
	 * @param field Champ
	 * @return Classe du type g�n�rique
	 */
	public static Class<?> getGeneric(final Field field) {
		Assertion.checkNotNull(field);
		//---------------------------------------------------------------------	
		final Class<?> generic = getGeneric(field.getGenericType());
		if (generic == null) {
			throw new UnsupportedOperationException("La d�tection du g�n�rique n'a pas pu �tre effectu�e sur le champ " + field.getName());
		}
		return generic;
	}

	private static Class<?> getGeneric(final Type type) {
		if (type instanceof ParameterizedType) {
			final ParameterizedType parameterizedType = ParameterizedType.class.cast(type);
			Assertion.checkArgument(parameterizedType.getActualTypeArguments().length == 1, "Il doit y avoir 1 et 1 seul g�n�rique d�clar�");
			final Type optionType = parameterizedType.getActualTypeArguments()[0];
			if (optionType instanceof Class) {
				return (Class<?>) optionType;
			} else if (optionType instanceof ParameterizedType) {
				//Cas ou le type param�tr� est lui m�me param�tr�
				return (Class<?>) ((ParameterizedType) optionType).getRawType();
			}

		}
		return null;
	}

	/**
	 * D�termine le nom de la propri�t� associ�e � un getteur.
	 * @param method M�thode du getteur
	 * @return Nom de la propri�t� associ�e
	 */
	public static String getPropertyName(final Method method) {
		Assertion.checkNotNull(method);
		//---------------------------------------------------------------------	
		final String property;
		if (method.getName().startsWith("get")) {
			property = method.getName().substring("get".length());
		} else if (method.getName().startsWith("is")) {
			Assertion.checkArgument(Boolean.class.equals(method.getReturnType()) || boolean.class.equals(method.getReturnType()), "une m�thode is concerne un boolean : {0}", method);
			property = method.getName().substring("is".length());
		} else {
			throw new IllegalArgumentException("Type de M�thode " + method + " non g�r�e en tant que propri�t�");
		}
		//On abaisse la premi�re lettre
		return StringUtil.first2LowerCase(property);
	}
}
