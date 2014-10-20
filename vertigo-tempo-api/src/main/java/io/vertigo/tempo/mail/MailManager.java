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
package io.vertigo.tempo.mail;

import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.lang.Component;

import java.util.Date;

/**
 * Gestionnaire centralisé des mails.
 *
 * @author npiedeloup
 */
public interface MailManager extends Component {
	/**
	 * Envoyer un mail.
	 * @param mail Mail à envoyer
	 */
	void sendMail(Mail mail);

	/**
	 * Envoyer un mail de façon asynchrone.
	 * @param mail Mail à envoyer
	 * param HandlerResultat de l'envoi du mail
	 */
	void sendMailASync(Mail mail, final WorkResultHandler<Date> workResultHandler);
}
