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

import io.vertigo.struts2.core.AbstractActionSupport;

import java.io.Serializable;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;
import com.opensymphony.xwork2.interceptor.PreResultListener;

/**
 * Interceptor Struts figeant le context.
 * @author npiedeloup
 */
public class KActionContextStoreInterceptor extends AbstractInterceptor {
	private static final long serialVersionUID = -3416159964166247585L;
	private final PreResultListener exceptionPreResultListener = new StoreContextPreResultListener();

	/** {@inheritDoc} */
	@Override
	public String intercept(final ActionInvocation actionInvocation) throws Exception {
		actionInvocation.addPreResultListener(exceptionPreResultListener);
		return actionInvocation.invoke();
	}

	private static class StoreContextPreResultListener implements PreResultListener, Serializable {
		private static final long serialVersionUID = 3177335059999813691L;

		/**
		 * Constructor.
		 */
		public StoreContextPreResultListener() {
			//nothing
		}

		/** {@inheritDoc} */
		@Override
		public void beforeResult(final ActionInvocation actionInvocation, final String resultCode) {
			final AbstractActionSupport action = (AbstractActionSupport) actionInvocation.getAction();
			action.storeContext();
		}
	}
}
