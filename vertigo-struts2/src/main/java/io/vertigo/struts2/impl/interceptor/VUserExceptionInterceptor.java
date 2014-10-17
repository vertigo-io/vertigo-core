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

import io.vertigo.lang.VUserException;
import io.vertigo.struts2.core.AbstractActionSupport;
import io.vertigo.struts2.core.UiError;
import io.vertigo.struts2.core.ValidationUserException;

import com.opensymphony.xwork2.Action;
import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.ActionSupport;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

/**
 * Interceptor Struts des exceptions de type UserException pour ajouter les messages à la page et la r�afficher.
 * @author npiedeloup
 */
public class VUserExceptionInterceptor extends AbstractInterceptor {
	private static final long serialVersionUID = -3416159964166247585L;

	//private final ExceptionPreResultListener exceptionPreResultListener = new ExceptionPreResultListener();

	/** {@inheritDoc} */
	@Override
	public String intercept(final ActionInvocation actionInvocation) throws Exception {
		//actionInvocation.addPreResultListener(exceptionPreResultListener);
		try {
			return actionInvocation.invoke();
		} catch (final ValidationUserException e) {
			final AbstractActionSupport action = (AbstractActionSupport) actionInvocation.getAction();
			for (final UiError uiError : e.getUiErrors()) {
				if (uiError.getDtObject() != null) {
					action.getUiMessageStack().error(uiError.getErrorMessage().getDisplay(), uiError.getDtObject(), uiError.getFieldName());
				} else {
					action.getUiMessageStack().error(uiError.getErrorMessage().getDisplay());
				}
			}
			return Action.NONE;
		} catch (final VUserException e) {
			final ActionSupport action = (ActionSupport) actionInvocation.getAction();
			action.addActionError(e.getMessage());
			return Action.NONE;
		}
	}
	//	private class ExceptionPreResultListener implements PreResultListener {
	//		@Override
	//		public void beforeResult(final ActionInvocation invocation, final String resultCode) {
	//			// perform operation necessary before Result execution
	//		}
	//	}
}
