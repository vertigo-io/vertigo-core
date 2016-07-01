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

import javax.inject.Inject;

import io.vertigo.core.locale.LocaleManager;
import io.vertigo.lang.Assertion;
import io.vertigo.tempo.mail.Mail;
import io.vertigo.tempo.mail.MailManager;

/**
 * This class is the standard impl of the mailManager.
 * This class have a single plugin to parameterize the way that mails are sent.
 *
 * @author npiedeloup
 */
public final class MailManagerImpl implements MailManager {
	private final SendMailPlugin sendMailPlugin;

	/**
	 * Constructor.
	 * @param localeManager the manager of the localized messages
	 * @param sendMailPlugin the plugin that sends mails
	 */
	@Inject
	public MailManagerImpl(final LocaleManager localeManager, final SendMailPlugin sendMailPlugin) {
		Assertion.checkNotNull(localeManager);
		Assertion.checkNotNull(sendMailPlugin);
		//-----
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
}
