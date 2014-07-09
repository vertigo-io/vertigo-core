package io.vertigo.labs.impl.mail;

import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.dynamo.work.WorkItem;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.labs.mail.Mail;
import io.vertigo.labs.mail.MailManager;

import java.util.Date;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Impl�mentation standard de la gestion centralis�e des droits d'acc�s.
 *
 * @author npiedeloup
 * @version $Id: MailManagerImpl.java,v 1.6 2014/07/04 13:11:22 pchretien Exp $
 */
public final class MailManagerImpl implements MailManager {
	private final WorkManager workManager;
	private final SendMailPlugin sendMailPlugin;

	/** 
	 * Constructeur.
	 * @param localeManager Manager des messages localis�s
	 * @param sendMailPlugin Plugin d'envoi de mail
	 */
	@Inject
	public MailManagerImpl(final WorkManager workManager, final LocaleManager localeManager, final SendMailPlugin sendMailPlugin) {
		super();
		Assertion.checkNotNull(workManager);
		Assertion.checkNotNull(localeManager);
		Assertion.checkNotNull(sendMailPlugin);
		//---------------------------------------------------
		this.workManager = workManager;
		localeManager.add("io.vertigo.labs.impl.mail.Mail", io.vertigo.labs.impl.mail.Resources.values());
		this.sendMailPlugin = sendMailPlugin;
	}

	/** {@inheritDoc} */
	public void sendMail(final Mail mail) {
		Assertion.checkNotNull(mail);
		//---------------------------------------------------------------------
		sendMailPlugin.sendMail(mail);
	}

	/** {@inheritDoc} */
	public void sendMailASync(final Mail mail, final WorkResultHandler<Date> workResultHandler) {
		Assertion.checkNotNull(mail);
		//---------------------------------------------------------------------
		WorkItem<Date, ?> workItem = new WorkItem<>(new Callable<Date>() {
			public Date call() {
				sendMail(mail);
				return new Date();
			}
		}, workResultHandler);
		workManager.schedule(workItem);
	}

}
