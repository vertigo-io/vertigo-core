package io.vertigo.labs.impl.mail;

import io.vertigo.kernel.component.Plugin;
import io.vertigo.labs.mail.Mail;

/**
 * Plugin d'envoi de mail.
 * @author npiedeloup
 * @version $Id: SendMailPlugin.java,v 1.3 2013/10/22 10:53:57 pchretien Exp $
 */
public interface SendMailPlugin extends Plugin {
	/**
	 * Envoyer un mail.
	 * @param mail Mail � envoyer
	 */
	void sendMail(Mail mail);
}
