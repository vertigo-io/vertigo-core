package io.vertigo.rest;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.rest.EndPointDefinition.Verb;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

public class RestManager implements Manager {

	public RestManager() {
		Home.getDefinitionSpace().register(EndPointDefinition.class);
		//---

		instrospectEndPoints(FamillesRestfulService.class);
		instrospectEndPoints(ContactsRestfulService.class);

		//---

	}

	private <C extends RestfulService> void instrospectEndPoints(final Class<C> restFullServiceClass) {
		for (final Method method : restFullServiceClass.getMethods()) {
			EndPointDefinition endPointDefinition;
			Verb verb = null;
			String path = null;
			for (final Annotation annotation : method.getAnnotations()) {
				if (annotation instanceof GET) {
					verb = Verb.GET;
				} else if (annotation instanceof POST) {
					verb = Verb.POST;
				} else if (annotation instanceof PUT) {
					verb = Verb.PUT;
				} else if (annotation instanceof DELETE) {
					verb = Verb.DELETE;
				} else if (annotation instanceof Path) {
					path = ((Path) annotation).value();
				}
			}
			if (verb != null) {
				Assertion.checkState(verb != null, "Le verb n'a pas été précisé sur {0}", method.getName());
				Assertion.checkArgNotEmpty(path, "Le path n'a pas été précisé sur {0}", method.getName());

				endPointDefinition = new EndPointDefinition("EP_" + StringUtil.camelToConstCase(restFullServiceClass.getSimpleName()) + "_" + StringUtil.camelToConstCase(method.getName()), verb, path, method);
				final Class[] paramType = method.getParameterTypes();
				final Annotation[][] parameterAnnotation = method.getParameterAnnotations();
				for (int i = 0; i < paramType.length; i++) {
					final String paramName = getParamName(parameterAnnotation[i]);
					//Assertion.checkArgNotEmpty(paramName, "Le paramName n'a pas été précisé sur {0}", method.getName());

					endPointDefinition.addParam(paramName != null ? paramName : "BodyContent", paramType[i]);
				}
				Home.getDefinitionSpace().put(endPointDefinition, EndPointDefinition.class);
			}
		}
	}

	private final String getParamName(final Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			if (annotation instanceof PathParam) {
				return ((PathParam) annotation).value();
			}
		}
		return null;//null check by caller
	}
}
