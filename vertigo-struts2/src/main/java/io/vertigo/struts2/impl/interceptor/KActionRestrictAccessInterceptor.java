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
package io.vertigo.struts2.impl.interceptor;

import io.vertigo.lang.Assertion;
import io.vertigo.lang.Option;
import io.vertigo.struts2.core.GET;
import io.vertigo.struts2.impl.MethodUtil;

import java.lang.reflect.Method;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * Interceptor Struts limitant l'access direct aux Actions.
 * @author npiedeloup
 */
public class KActionRestrictAccessInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = -6847302589734386523L;

	/** {@inheritDoc} */
	@Override
	public String intercept(final ActionInvocation actionInvocation) throws Exception {
		final HttpServletRequest request = ServletActionContext.getRequest();
		final String methodName = actionInvocation.getProxy().getMethod();
		//si on est en GET, et que l'on appelle une action spécifique (autre que execute)
		//on test la présence de l'annotation @GET
		if ("GET".equals(request.getMethod()) && !"execute".equals(methodName)) {
			final Option<Method> actionMethod = MethodUtil.findMethodByName(actionInvocation.getAction().getClass(), methodName);
			Assertion.checkArgument(actionMethod.isDefined(), "Method {0} not found in {1}", methodName, actionInvocation.getAction().getClass());
			if (!actionMethod.get().isAnnotationPresent(GET.class)) {
				throw new IllegalAccessException("Vous ne pouvez pas appeler " + actionInvocation.getAction().getClass().getSimpleName() + "." + methodName + " directement.");
			}
		}
		return actionInvocation.invoke();
	}
}
