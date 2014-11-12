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
package io.vertigo.struts2.impl.unknownhandler;

import io.vertigo.lang.Option;
import io.vertigo.struts2.impl.MethodUtil;
import io.vertigo.struts2.impl.servlet.RequestContainerWrapper;
import io.vertigo.util.StringUtil;

import java.lang.reflect.Method;

import org.apache.struts2.ServletActionContext;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.Result;
import com.opensymphony.xwork2.UnknownHandler;
import com.opensymphony.xwork2.XWorkException;
import com.opensymphony.xwork2.config.entities.ActionConfig;

/**
 * Gestion du passage de paramètres aux Actions.
 * A ajouter dans le struts.xml :
 * <bean type="com.opensymphony.xwork2.UnknownHandler" name="handler" class="io.vertigo.struts2.impl.unknownhandler.InjectParamsToActionMethodHandler"/>
 *
 * Pour en déclarer plusieurs rechercher "Stacking Unknown Handlers".
 * @see "http://struts.apache.org/release/2.3.x/docs/unknown-handlers.html"
 *
 * @author npiedeloup
 */
public class InjectParamsToActionMethodHandler implements UnknownHandler {

	/** {@inheritDoc} */
	@Override
	public ActionConfig handleUnknownAction(final String namespace, final String actionName) throws XWorkException {
		//Non pris en charge
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Result handleUnknownResult(final ActionContext actionContext, final String actionName, final ActionConfig actionConfig, final String resultCode) throws XWorkException {
		//Non pris en charge
		return null;
	}

	/** {@inheritDoc} */
	@Override
	public Object handleUnknownActionMethod(final Object action, final String methodName) throws NoSuchMethodException {
		Option<Method> actionMethod = MethodUtil.findMethodByName(action.getClass(), methodName);
		if (actionMethod.isEmpty()) {
			//method non trouvée, on test doXXX comme struts2 le fait de base
			final String prefixedMethodName = getPrefixedMethodName(methodName);
			actionMethod = MethodUtil.findMethodByName(action.getClass(), prefixedMethodName);
			if (actionMethod.isEmpty()) {
				//method non trouvée
				return null;
			}
		}
		final RequestContainerWrapper container = new RequestContainerWrapper(ServletActionContext.getRequest());
		return MethodUtil.invoke(action, actionMethod.get(), container);
	}

	private static String getPrefixedMethodName(final String methodName) {
		return "do" + StringUtil.first2UpperCase(methodName);
	}
}
