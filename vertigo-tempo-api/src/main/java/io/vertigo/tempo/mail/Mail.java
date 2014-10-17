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

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.lang.Assertion;

import java.util.Collections;
import java.util.List;

/**
 * Message à envoyer par mail.
 * Les adresses email respectent le format RFC822.
 * Eles sont de la forme
 *  - "user@host.domain"
 *  ou
 *  - "Personal Name <user@host.domain>"
 * @author npiedeloup
 */
public final class Mail {
	private final String subject;
	private final String fromAddress;
	/**
	 * on autorise le null, car le protocol SMTP assure déjà la stratégie de choix d'email de retour.
	 */
	private final String replyTo;
	private final String textContent;
	private final String htmlContent;

	private final List<String> toAddresses;
	private final List<String> ccAddresses;
	private final List<KFile> attachments;

	/**
	 * Constructeur utilisé par le Builder.
	 * @param subject Sujet du mail;
	 * @param replyTo Addresse de retour
	 * @param fromAddress Addresse de l'emetteur
	 * @param toAddresses Addresses de destination
	 * @param ccAddresses Addresses en copie
	 * @param textContent Contenu text
	 * @param htmlContent Contenu html
	 * @param attachments Liste des pièces jointes
	 */
	Mail(final String subject, final String replyTo, final String fromAddress, final List<String> toAddresses, final List<String> ccAddresses, final String textContent, final String htmlContent, final List<KFile> attachments) {
		Assertion.checkArgNotEmpty(subject, "Sujet du mail obligatoire");
		//Assertion.notEmpty(replyTo);
		Assertion.checkArgNotEmpty(fromAddress, "Adresse email de l'émetteur obligatoire");
		Assertion.checkNotNull(toAddresses);
		Assertion.checkArgument(!toAddresses.isEmpty(), "Le mail doit avoir au moins un destinataire.");
		Assertion.checkNotNull(ccAddresses);
		Assertion.checkArgument(textContent != null || htmlContent != null, "Le mail doit avoir un contenu, soit en text, soit en html");
		Assertion.checkNotNull(attachments);
		//---------------------------------------------------------------------
		this.subject = subject;
		this.replyTo = replyTo;
		this.fromAddress = fromAddress;
		this.textContent = textContent;
		this.htmlContent = htmlContent;
		this.toAddresses = Collections.unmodifiableList(toAddresses);
		this.ccAddresses = Collections.unmodifiableList(ccAddresses);
		this.attachments = Collections.unmodifiableList(attachments);
	}

	/**
	 * @return Adresse mail de l'émetteur
	 */
	public String getFrom() {
		return fromAddress;
	}

	/**
	 * @return Adresse mail de retour de mail (null, si non fixée)
	 */
	public String getReplyTo() {
		return replyTo;
	}

	/**
	 * @return Liste des adresses mail destinataires
	 */
	public List<String> getToList() {
		return toAddresses;
	}

	/**
	 * @return Liste des adresses mail en copie
	 */
	public List<String> getCcList() {
		return ccAddresses;
	}

	/**
	 * @return Sujet du mail
	 */
	public String getSubject() {
		return subject;
	}

	/**
	 * @return Contenu texte du mail
	 */
	public String getTextContent() {
		return textContent;
	}

	/**
	 * @return Contenu HTML du mail
	 */
	public String getHtmlContent() {
		return htmlContent;
	}

	/**
	 * @return Liste des pièces jointes
	 */
	public List<KFile> getAttachments() {
		return attachments;
	}
}
