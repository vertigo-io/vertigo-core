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
package io.vertigo.labs.mail;

import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Builder;

import java.util.ArrayList;
import java.util.List;

/**
 * Builder de Mail.
 * Les m�thodes multivalu�es sont not�es en varargs (...) et peuvent �tre appel�es plusieurs fois.
 * @author pchretien, npiedeloup
 * @version $Id: MailBuilder.java,v 1.8 2014/02/27 10:32:49 pchretien Exp $
 */
public class MailBuilder implements Builder<Mail> {
	private String subject;
	private String replyTo;
	private String from;
	private String textContent;
	private String htmlContent;

	private final List<String> toAddresses = new ArrayList<>();
	private final List<String> ccAddresses = new ArrayList<>();
	private final List<KFile> attachments = new ArrayList<>();

	/**
	 * D�finit le sujet du mail.
	 * @param newSubject Sujet du mail 
	 * @return MailBuilder
	 */
	public MailBuilder withSubject(final String subject) {
		Assertion.checkArgNotEmpty(subject);
		Assertion.checkState(this.subject == null, "subject is already completed");
		//---------------------------------------------------------------------
		this.subject = subject;
		return this;
	}

	/**
	 * @param from Emetteur du mail
	 * @return MailBuilder
	 */
	public MailBuilder from(final String from) {
		Assertion.checkState(this.from == null, "from is already completed");
		Assertion.checkArgNotEmpty(from);
		//---------------------------------------------------------------------
		this.from = from;
		return this;
	}

	/**
	 * Fixe une adresse email de retour.
	 * @param newReplyTo Destinataire du mail de retour
	 * @return MailBuilder
	 */
	public MailBuilder replyTo(final String newReplyTo) {
		Assertion.checkState(replyTo == null, "replyTo is already completed");
		Assertion.checkArgNotEmpty(newReplyTo);
		//---------------------------------------------------------------------
		replyTo = newReplyTo;
		return this;
	}

	/**
	 * Ajoute une adresse en destination.
	 * @param addresses Adresses email
	 * @return MailBuilder
	 */
	public MailBuilder to(final String... addresses) {
		Assertion.checkNotNull(addresses);
		//---------------------------------------------------------------------
		for (final String address : addresses) {
			Assertion.checkArgNotEmpty(address);
			toAddresses.add(address);
		}
		return this;
	}

	/**
	 * Ajoute une adresse en copie.
	 * @param addresses Adresses email
	 * @return MailBuilder
	 */
	public MailBuilder cc(final String... addresses) {
		Assertion.checkNotNull(addresses);
		//---------------------------------------------------------------------
		for (final String address : addresses) {
			Assertion.checkArgNotEmpty(address);
			ccAddresses.add(address);
		}
		return this;
	}

	/**
	 * D�finit le contenu text du mail.
	 * @param newTextContent Contenu text 
	 * @return MailBuilder
	 */
	public MailBuilder withTextContent(final String newTextContent) {
		Assertion.checkState(textContent == null, "textContent is already completed");
		Assertion.checkArgNotEmpty(newTextContent);
		//---------------------------------------------------------------------
		textContent = newTextContent;
		return this;
	}

	/** 
	 * D�finit le contenu Html du mail.
	 * @param newHtmlContent Contenu Html 
	 * @return MailBuilder
	 */
	public MailBuilder withHtmlContent(final String newHtmlContent) {
		Assertion.checkState(htmlContent == null, "htmlContent is already completed");
		Assertion.checkArgNotEmpty(newHtmlContent);
		//---------------------------------------------------------------------
		htmlContent = newHtmlContent;
		return this;
	}

	/**
	 * Ajoute une pi�ce jointe au mail.
	 * @param files File � ajouter
	 * @return MailBuilder
	 */
	public MailBuilder withAttachments(final KFile... files) {
		Assertion.checkNotNull(files);
		//---------------------------------------------------------------------
		for (final KFile attachment : files) {
			Assertion.checkNotNull(attachment);
			attachments.add(attachment);
		}
		return this;
	}

	/** {@inheritDoc} */
	public Mail build() {
		Assertion.checkArgument(!toAddresses.isEmpty(), "aucun destinataire");
		//---------------------------------------------------------------------
		return new Mail(subject, replyTo, from, toAddresses, ccAddresses, textContent, htmlContent, attachments);
	}
}
