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
package io.vertigo.rest;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.component.Manager;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;
import io.vertigo.rest.EndPointDefinition.Verb;
import io.vertigo.rest.RestfulService.AnonymousAccessAllowed;
import io.vertigo.rest.RestfulService.DELETE;
import io.vertigo.rest.RestfulService.GET;
import io.vertigo.rest.RestfulService.POST;
import io.vertigo.rest.RestfulService.PUT;
import io.vertigo.rest.RestfulService.PathParam;
import io.vertigo.rest.RestfulService.QueryParam;
import io.vertigo.rest.RestfulService.SessionLess;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Restfull webservice manager.
 * @author npiedeloup
 */
public final class RestManager implements Manager, Activeable {

	public RestManager() {
		Home.getDefinitionSpace().register(EndPointDefinition.class);
	}

	public void start() {
		for (final String componentId : Home.getComponentSpace().keySet()) {
			final Object component = Home.getComponentSpace().resolve(componentId, Object.class);
			if (component instanceof RestfulService) {
				instrospectEndPoints(((RestfulService) component).getClass());
			}
		}
	}

	public void stop() {
		//nothing
	}

	private static <C extends RestfulService> void instrospectEndPoints(final Class<C> restFullServiceClass) {
		for (final Method method : restFullServiceClass.getMethods()) {
			Verb verb = null;
			String path = null;
			boolean needSession = true;
			boolean needAuthentication = true;
			for (final Annotation annotation : method.getAnnotations()) {
				if (annotation instanceof GET) {
					Assertion.checkState(verb == null, "A verb is already specified on {0}", method.getName());
					verb = Verb.GET;
					path = ((GET) annotation).value();
				} else if (annotation instanceof POST) {
					Assertion.checkState(verb == null, "A verb is already specified on {0}", method.getName());
					verb = Verb.POST;
					path = ((POST) annotation).value();
				} else if (annotation instanceof PUT) {
					Assertion.checkState(verb == null, "A verb is already specified on {0}", method.getName());
					verb = Verb.PUT;
					path = ((PUT) annotation).value();
				} else if (annotation instanceof DELETE) {
					Assertion.checkState(verb == null, "A verb is already specified on {0}", method.getName());
					verb = Verb.DELETE;
					path = ((DELETE) annotation).value();
				} else if (annotation instanceof AnonymousAccessAllowed) {
					needAuthentication = false;
				} else if (annotation instanceof SessionLess) {
					needSession = false;
				}
			}
			if (verb != null) {
				createAndRegisterEndPoint(restFullServiceClass, method, verb, path, needSession, needAuthentication);
			}
		}
	}

	private static <C extends RestfulService> void createAndRegisterEndPoint(final Class<C> restFullServiceClass, final Method method, Verb verb, String path, boolean needSession, boolean needAuthentication) {
		Assertion.checkNotNull(verb, "Verb must be specified on {0}", method.getName());
		Assertion.checkArgNotEmpty(path, "Route path must be specified on {0}", method.getName());

		final Class<?>[] paramType = method.getParameterTypes();
		final Annotation[][] parameterAnnotation = method.getParameterAnnotations();

		List<EndPointParam> endPointParams = new ArrayList<>();
		for (int i = 0; i < paramType.length; i++) {
			final String paramName = getParamName(parameterAnnotation[i]);
			//Assertion.checkArgNotEmpty(paramName, "Le paramName n'a pas été précisé sur {0}", method.getName());
			endPointParams.add(new EndPointParam(paramName, paramType[i]));
		}
		//---
		final EndPointDefinition endPointDefinition = new EndPointDefinition("EP_" + StringUtil.camelToConstCase(restFullServiceClass.getSimpleName()) + "_" + StringUtil.camelToConstCase(method.getName()), verb, path, method, needSession, needAuthentication, endPointParams);
		Home.getDefinitionSpace().put(endPointDefinition, EndPointDefinition.class);
	}

	private static String getParamName(final Annotation[] annotations) {
		for (final Annotation annotation : annotations) {
			if (annotation instanceof PathParam) {
				return ":path:" + ((PathParam) annotation).value();
			} else if (annotation instanceof QueryParam) {
				return ":query:" + ((QueryParam) annotation).value();
			}
			//			else if (annotation instanceof BodyParam) {
			//				return ":body:";
			//			}
		}
		return ":body:";//if no annotation : take request body
	}
}
