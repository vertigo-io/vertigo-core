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
package io.vertigo.tempo.impl.mail;

import io.vertigo.commons.locale.LocaleManager;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.lang.Assertion;
import io.vertigo.tempo.mail.Mail;
import io.vertigo.tempo.mail.MailManager;

import java.util.Date;
import java.util.concurrent.Callable;

import javax.inject.Inject;

/**
 * Implémentation standard de la gestion centralisée des droits d'accès.
 *
 * @author npiedeloup
 */
public final class MailManagerImpl implements MailManager {
	private final WorkManager workManager;
	private final SendMailPlugin sendMailPlugin;

	/**
	 * Constructeur.
	 * @param localeManager Manager des messages localisés
	 * @param sendMailPlugin Plugin d'envoi de mail
	 */
	@Inject
	public MailManagerImpl(final WorkManager workManager, final LocaleManager localeManager, final SendMailPlugin sendMailPlugin) {
		super();
		Assertion.checkNotNull(workManager);
		Assertion.checkNotNull(localeManager);
		Assertion.checkNotNull(sendMailPlugin);
		//-----
		this.workManager = workManager;
		localeManager.add("io.vertigo.tempo.impl.mail.Mail", io.vertigo.tempo.impl.mail.Resources.values());
		this.sendMailPlugin = sendMailPlugin;
	}

	/** {@inheritDoc} */
	@Override
	public void sendMail(final Mail mail) {
		Assertion.checkNotNull(mail);
		//-----
		sendMailPlugin.sendMail(mail);
	}

	/** {@inheritDoc} */
	@Override
	public void sendMailASync(final Mail mail, final WorkResultHandler<Date> workResultHandler) {
		Assertion.checkNotNull(mail);
		//-----
		workManager.schedule(new Callable<Date>() {
			@Override
			public Date call() {
				sendMail(mail);
				return new Date();
			}
		}, workResultHandler);
	}
}
