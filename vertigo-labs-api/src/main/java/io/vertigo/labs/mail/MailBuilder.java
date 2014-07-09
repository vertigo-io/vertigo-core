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
	private String fromAddress;
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
	public MailBuilder withSubject(final String newSubject) {
		Assertion.checkState(subject == null, "subject is already completed");
		Assertion.checkArgNotEmpty(newSubject);
		//---------------------------------------------------------------------
		subject = newSubject;
		return this;
	}

	/**
	 * @param from Emetteur du mail
	 * @return MailBuilder
	 */
	public MailBuilder from(final String from) {
		Assertion.checkState(fromAddress == null, "from is already completed");
		Assertion.checkArgNotEmpty(from);
		//---------------------------------------------------------------------
		fromAddress = from;
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
		return new Mail(subject, replyTo, fromAddress, toAddresses, ccAddresses, textContent, htmlContent, attachments);
	}
}
