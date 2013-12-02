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

import io.vertigo.kernel.lang.MessageText;

/**
 * Classe de base pour toutes les exceptions utilisateurs.
 *
 * Les exceptions utilisateurs sont construites sur la base d'un message (multilingue).
 * Il est possible d'ajouter une cause via la m�thode initCause.  
 *
 * @author fconstantin, pchretien
 * @version $Id: VUserException.java,v 1.1 2013/10/09 14:02:58 pchretien Exp $
 */
public class VUserException extends RuntimeException {
	private static final long serialVersionUID = 3911465988816189879L;
	private final MessageText messageText;

	/**
	 * Constructeur.
	 * @param messageText Message de l'exception
	 */
	public VUserException(final MessageText messageText) {
		//Attention il convient d'utiliser une m�thode qui ne remonte d'exception.
		super(messageText.getDisplay());
		// On rerentre sur l'API des Exception en passant le message.
		this.messageText = messageText;
	}

	/**
	 * Gestion des messages d'erreur externalis�s.
	 * @return messageText.
	 */
	public final MessageText getMessageText() {
		return messageText;
	}
}
