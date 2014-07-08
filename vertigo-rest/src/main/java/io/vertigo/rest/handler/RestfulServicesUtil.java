package io.vertigo.rest.handler;

import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.rest.RestfulService.PathParam;
import io.vertigo.rest.RestfulService.QueryParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import spark.Request;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Gestion du passage de paramètres aux Actions.
 * @author npiedeloup
 */
final class RestfulServicesUtil {

	private static final Gson GSON = new GsonBuilder() //
			.setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'") //
			.setPrettyPrinting().create();

	private RestfulServicesUtil() {
		//privé pour une classe utilitaire
	}

	/**
	 * Invocation dynamique d'une méthode sur une instance.
	 * 
	 * @param instance Objet sur lequel est invoqué la méthode
	 * @param methodName Nom de la methode invoquée (la premiere trouvée est appellée)
	 * @param container Container des arguments
	 * @return R Valeur retournée par l'invocation
	 */
	public static Object invoke(final Object instance, final String methodName, final Request request) {
		final Option<Method> actionMethod = findMethodByName(instance.getClass(), methodName);
		if (actionMethod.isEmpty()) {
			throw new VRuntimeException("Method " + methodName + " not found on " + instance.getClass().getName());
		}
		actionMethod.get().setAccessible(true); //la méthode peut être protected
		return invoke(instance, actionMethod.get(), request);
	}

	/**
	 * Invocation dynamique d'une méthode sur une instance.
	 * 
	 * @param instance Objet sur lequel est invoqué la méthode
	 * @param method Methode invoquée
	 * @param container Container des arguments
	 * @return R Valeur retournée par l'invocation
	 */
	public static Object invoke(final Object instance, final Method method, final Request request) {
		Assertion.checkNotNull(instance);
		Assertion.checkNotNull(method);
		//--------------------------------------------------------------------
		final Object[] args = findMethodParameters(request, method);
		return ClassUtil.invoke(instance, method, args);
	}

	/**
	 * Retrouve une méthode par son nom.
	 * Part de la class déclarante et remonte les superclass.
	 * @param declaringClass Class de la méthode
	 * @param methodName Nom de la méthode
	 * @return Option de la première méthode trouvée.
	 */
	public static Option<Method> findMethodByName(final Class<?> declaringClass, final String methodName) {
		for (final Method method : declaringClass.getDeclaredMethods()) {
			if (method.getName().equals(methodName)) {
				return Option.some(method);
			}
		}
		if (declaringClass.getSuperclass() != null) {
			return findMethodByName(declaringClass.getSuperclass(), methodName);
		}
		return Option.none();
	}

	private static Object[] findMethodParameters(final Request request, final Method method) {
		final Object[] parameters = new Object[method.getParameterTypes().length];
		for (int i = 0; i < method.getParameterTypes().length; i++) {
			parameters[i] = getInjected(request, method, i);
		}
		return parameters;
	}

	//On récupère pour le paramètre i du constructeur l'objet à injecter
	private static Object getInjected(final Request request, final Method method, final int i) {
		final String id = getNamedValue(method.getParameterAnnotations()[i]);
		//------------
		final String stringValue;
		if (id != null) {
			stringValue = request.params(":" + id);
		} else { //body content
			stringValue = request.body();
		}

		final Object value = convert(stringValue, method.getParameterTypes()[i]);

		Assertion.checkNotNull(value);
		//------------
		return value;
	}

	private static Object convert(final String value, final Class<?> paramClass) {
		if (value == null) {
			return null;
		} else if (String.class.isAssignableFrom(paramClass)) {
			return value;
		} else if (Integer.class.isAssignableFrom(paramClass)) {
			return Integer.valueOf(value);
		} else if (Long.class.isAssignableFrom(paramClass)) {
			return Long.valueOf(value);
		} else {
			return GSON.fromJson(value, paramClass);
		}
	}

	public static Object toJson(final Object value) {
		return GSON.toJson(value);
	}

	private static String getNamedValue(final Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			if (annotation instanceof PathParam) {
				return PathParam.class.cast(annotation).value();
			} else if (annotation instanceof QueryParam) {
				return QueryParam.class.cast(annotation).value();
			}
		}
		return null;
	}

	public static String toJsonError(final String message) {
		return "{ globalErrors:[\"" + message + "\"]}"; //TODO +stack
	}
}
