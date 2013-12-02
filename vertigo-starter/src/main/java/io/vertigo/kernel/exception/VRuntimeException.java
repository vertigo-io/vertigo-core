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
package io.vertigo.kernel.exception;

import io.vertigo.kernel.util.StringUtil;

/**
 * Classe de base pour toutes les exceptions syst�mes.
 * 
 * @author fconstantin, pchretien
 * @version $Id: VRuntimeException.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
public final class VRuntimeException extends RuntimeException {
	private static final long serialVersionUID = -6326238492525656091L;

	/**
	 * Constructeur.
	 * 
	 * @param msg Message de l'exception
	 */
	public VRuntimeException(final String msg) {
		super(msg);
	}

	/**
	 * Constructeur.
	 *
	 * @param msg Message de l'exception
	 * @param t Exception cause
	 * @param params Param�tres de la resource
	 */
	public VRuntimeException(final String msg, final Throwable t, final Object... params) {
		//Le message peut-�tre null sur certaines exceptions java (NullPointerException par exemple)
		//Dans ce cas on positionne le nom de l'exception
		super(StringUtil.format(msg != null ? msg : t.getClass().getSimpleName(), params), t);
	}

	/**
	 * Constructeur.
	 *
	 * @param t Exception cause
	 * @param params Param�tres de la resource
	 */
	public VRuntimeException(final Throwable t, final Object... params) {
		this(t.getMessage(), t, params);
	}
}
