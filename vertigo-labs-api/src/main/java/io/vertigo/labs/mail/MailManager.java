package io.vertigo.labs.mail;

import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.component.Manager;

import java.util.Date;

/**
 * Gestionnaire centralis� des mails.
 *
 * @author npiedeloup
 * @version $Id: MailManager.java,v 1.3 2014/01/20 18:56:41 pchretien Exp $
 */
public interface MailManager extends Manager {
	/**
	 * Envoyer un mail.
	 * @param mail Mail � envoyer
	 */
	void sendMail(Mail mail);

	/**
	* Envoyer un mail de fa�on asynchrone.
	* @param mail Mail � envoyer
	* param Handlerr�sultat de l'envoi du mail
	*/
	void sendMailASync(Mail mail, final WorkResultHandler<Date> workResultHandler);
}
