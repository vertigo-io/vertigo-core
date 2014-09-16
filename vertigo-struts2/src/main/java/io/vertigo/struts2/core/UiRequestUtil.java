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
package io.vertigo.struts2.core;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.struts2.ServletActionContext;

/**
 * Utilitaire d'accès à la Request.
 * @author npiedeloup
 */
public final class UiRequestUtil {

	private UiRequestUtil() {
		//rien 
	}

	/**
	 * @return la requête HTTP dans un context Struts
	 * @Deprecated Ne pas utiliser : cas extrement rare (certificat CPs)
	 */
	@Deprecated
	public static HttpServletRequest getHttpServletRequest() {
		return ServletActionContext.getRequest();
	}

	/**
	 * @return la session HTTP dans un context Struts
	 * @Deprecated utiliser KSecurityManager
	 */
	@Deprecated
	public static HttpSession getHttpSession() {
		return ServletActionContext.getRequest().getSession(false);
	}

	/**
	 * Invalide la session Http (ie Logout)
	 */
	public static void invalidateHttpSession() {
		final HttpSession session = ServletActionContext.getRequest().getSession(false);
		if (session != null) {
			session.invalidate();
		}
	}

	/**
	 * Retourne la valeur du paramètre de la requête HTTP à partir d'une clé.
	 * 
	 * @param key la clé du paramètre
	 * @return la valeur du paramètre
	 * @deprecated Utiliser le @Named du initContext, ou un ContextRef<Xxx> pour les autres cas
	 */
	@Deprecated
	public static String getRequestParameter(final String key) {
		return getHttpServletRequest().getParameter(key);
	}
}
