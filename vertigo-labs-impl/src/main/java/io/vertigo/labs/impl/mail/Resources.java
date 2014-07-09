package io.vertigo.labs.impl.mail;

import io.vertigo.kernel.lang.MessageKey;

/**
 * Dictionnaire des ressources.
 *
 * @author  npiedeloup
 * @version $Id: Resources.java,v 1.2 2013/10/22 10:53:57 pchretien Exp $
*/
public enum Resources implements MessageKey {
	/**
	 * L''adresse email {0} n''est pas correct.
	 */
	KASPER_MAIL_ADRESS_MAIL_INVALID,

	/**
	 * Le serveur mail {0}:{1} ne r�pond pas.
	 */
	KASPER_MAIL_SERVER_TIMEOUT,

}
