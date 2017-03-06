/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.tempo.plugins.mail.javaxmail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

import io.vertigo.core.component.ComponentInfo;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.VFile;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Describable;
import io.vertigo.lang.MessageKey;
import io.vertigo.lang.MessageText;
import io.vertigo.lang.VUserException;
import io.vertigo.lang.WrappedException;
import io.vertigo.tempo.impl.mail.Resources;
import io.vertigo.tempo.impl.mail.SendMailPlugin;
import io.vertigo.tempo.mail.Mail;
import io.vertigo.util.StringUtil;

/**
 * Plugin de gestion des mails, pour l'implémentation du jdk.
 *
 * @author npiedeloup
 */
public final class JavaxSendMailPlugin implements SendMailPlugin, Describable {

	private static final String CHARSET_USED = "ISO-8859-1";
	private final FileManager fileManager;
	private final String mailStoreProtocol;
	private final String mailHost;
	private final boolean developmentMode;
	private final String developmentMailTo;
	private final Optional<Integer> mailPort;
	private final Optional<String> mailLogin;
	private final Optional<String> mailPassword;
	/** Compteur de mails envoyés. */
	private int mailSent;

	/**
	 * Crée le plugin d'envoie de mail.
	 *
	 * @param fileManager Manager de gestion des fichiers
	 * @param mailStoreProtocol Protocole utilisé
	 * @param mailHost Serveur de mail
	 * @param developmentMode Indique s'il le mode developpement est activé (surcharge des emails destinataires)
	 * @param developmentMailTo Email destinataire forcé pour développement
	 * @param mailPort port à utiliser (facultatif)
	 * @param mailLogin Login à utiliser lors de la connexion au serveur mail (facultatif)
	 * @param mailPassword mot de passe à utiliser lors de la connexion au serveur mail (facultatif)
	 */
	@Inject
	public JavaxSendMailPlugin(
			final FileManager fileManager,
			@Named("storeProtocol") final String mailStoreProtocol,
			@Named("host") final String mailHost,
			@Named("developmentMode") final boolean developmentMode,
			@Named("developmentMailTo") final String developmentMailTo,
			@Named("port") final Optional<Integer> mailPort,
			@Named("login") final Optional<String> mailLogin,
			@Named("pwd") final Optional<String> mailPassword) {
		Assertion.checkNotNull(fileManager);
		Assertion.checkArgNotEmpty(mailStoreProtocol);
		Assertion.checkArgNotEmpty(mailHost);
		Assertion.checkArgNotEmpty(developmentMailTo);
		Assertion.checkNotNull(mailPort);
		Assertion.checkNotNull(mailLogin);
		Assertion.checkNotNull(mailPassword);
		Assertion.when(mailLogin.isPresent())
				.check(() -> !StringUtil.isEmpty(mailLogin.get()), // if set, login can't be empty
						"When defined Login can't be empty");
		Assertion.checkArgument(!mailLogin.isPresent() ^ mailPassword.isPresent(), // login and password must be null or not null both
				"Password is required when login is defined");
		//-----
		this.fileManager = fileManager;
		this.mailStoreProtocol = mailStoreProtocol;
		this.mailHost = mailHost;
		this.developmentMailTo = developmentMailTo;
		this.developmentMode = developmentMode;
		this.mailPort = mailPort;
		this.mailLogin = mailLogin;
		this.mailPassword = mailPassword;
	}

	/** {@inheritDoc} */
	@Override
	public void sendMail(final Mail mail) {
		Assertion.checkNotNull(mail);
		//-----
		try {
			final Session session = createSession();
			final Message message = createMessage(mail, session);
			Transport.send(message);
			mailSent++; // on ne synchronize pas pour des stats peu importantes
		} catch (final MessagingException e) {
			throw createMailException(Resources.TEMPO_MAIL_SERVER_TIMEOUT, e, mailHost, mailPort.isPresent() ? mailPort.get() : "default");
		} catch (final UnsupportedEncodingException e) {
			throw WrappedException.wrap(e, "Probleme d'encodage lors de l'envoi du mail");
		}
	}

	private Message createMessage(final Mail mail, final Session session) throws MessagingException, UnsupportedEncodingException {
		final Message message = new MimeMessage(session);
		setFromAddress(mail.getFrom(), message);
		if (mail.getReplyTo() != null) {
			setReplyToAddress(mail.getReplyTo(), message);
		}
		setToAddress(mail.getToList(), message);
		setCcAddress(mail.getCcList(), message);
		if (mail.getSubject() != null) {
			message.setSubject(MimeUtility.encodeWord(mail.getSubject(), CHARSET_USED, "Q"));
		}
		message.setHeader("X-Mailer", "Java");
		message.setSentDate(new Date());
		final List<VFile> attachments = mail.getAttachments();
		if (attachments.isEmpty()) {
			setBodyContent(mail.getTextContent(), mail.getHtmlContent(), message);
		} else {
			final Multipart multiPart = new MimeMultipart();
			final BodyPart bodyPart = new MimeBodyPart();
			setBodyContent(mail.getTextContent(), mail.getHtmlContent(), bodyPart);
			multiPart.addBodyPart(bodyPart);
			for (final VFile vFile : attachments) {
				final BodyPart bodyFile = createBodyFile(vFile);
				multiPart.addBodyPart(bodyFile);
			}
			message.setContent(multiPart);
		}
		return message;
	}

	private Session createSession() {
		final Properties properties = new Properties();
		properties.put("mail.store.protocol", mailStoreProtocol);
		properties.put("mail.host", mailHost);
		if (mailPort.isPresent()) {
			properties.put("mail.port", mailPort.get());
		}
		properties.put("mail.debug", "false");
		final Session session;
		if (mailLogin.isPresent()) {
			properties.put("mail.smtp.ssl.trust", mailHost);
			properties.put("mail.smtp.starttls.enable", true);
			properties.put("mail.smtp.auth", "true");

			final String username = mailLogin.get();
			final String password = mailPassword.get();
			session = Session.getInstance(properties, new javax.mail.Authenticator() {

				@Override
				protected PasswordAuthentication getPasswordAuthentication() {
					return new PasswordAuthentication(username, password);
				}
			});
		} else {
			session = Session.getDefaultInstance(properties);
		}
		session.setDebug(false);
		return session;
	}

	private static void setFromAddress(final String from, final Message message) throws MessagingException {
		Assertion.checkNotNull(from);
		Assertion.checkNotNull(message);
		//-----
		try {
			message.setFrom(createInternetAddress(from));
		} catch (final AddressException e) {
			// on catch ici, pour pouvoir indiquer l'adresse qui pose pb
			throw createMailException(Resources.TEMPO_MAIL_ADRESS_MAIL_INVALID, e, from);
		}
	}

	private static void setReplyToAddress(final String replyTo, final Message message) throws MessagingException {
		Assertion.checkNotNull(message);
		Assertion.checkNotNull(replyTo);
		//-----
		try {
			final InternetAddress[] replyToArray = { createInternetAddress(replyTo) };
			message.setReplyTo(replyToArray);
		} catch (final AddressException e) {
			// on catch ici, pour pouvoir indiquer l'adresse qui pose pb
			throw createMailException(Resources.TEMPO_MAIL_ADRESS_MAIL_INVALID, e, replyTo);
		}
	}

	private static InternetAddress createInternetAddress(final String address) throws AddressException {
		final InternetAddress internetAddress = new InternetAddress(address);
		internetAddress.validate();
		return internetAddress;
	}

	private void setToAddress(final List<String> addressList, final Message message) throws MessagingException {
		setDestAddress(addressList, message, Message.RecipientType.TO);
	}

	private void setCcAddress(final List<String> addressList, final Message message) throws MessagingException {
		if (!addressList.isEmpty()) {
			setDestAddress(addressList, message, Message.RecipientType.CC);
		}
	}

	private void setDestAddress(final List<String> addressList, final Message message, final Message.RecipientType type) throws MessagingException {
		Assertion.checkNotNull(addressList);
		Assertion.checkArgument(!addressList.isEmpty(), "La liste des destinataires ne doit pas être vide");
		Assertion.checkNotNull(message);
		//-----
		final InternetAddress[] addresses = new InternetAddress[addressList.size()];
		for (int i = 0; i < addressList.size(); i++) {
			try {
				addresses[i] = createInternetAddress(addressList.get(i));
				if (developmentMode) {
					// en mode debug on change l'adresse, mais on laisse le nom.
					addresses[i].setAddress(developmentMailTo);
				}
			} catch (final AddressException e) {
				// on catch ici, pour pouvoir indiquer l'adresse qui pose pb
				throw createMailException(Resources.TEMPO_MAIL_ADRESS_MAIL_INVALID, e, addressList.get(i));
			}
		}
		message.setRecipients(type, addresses);
	}

	private static void setBodyContent(final String textContent, final String htmlContent, final Part bodyPart) throws MessagingException {
		Assertion.checkArgument(textContent != null || htmlContent != null, "Le mail n'a pas de contenu, ni en text, ni en html");
		Assertion.checkNotNull(bodyPart);
		//-----
		if (textContent != null && htmlContent != null) {
			final Multipart multipart = new MimeMultipart("alternative");
			final BodyPart plainMessageBodyPart = new MimeBodyPart();
			plainMessageBodyPart.setContent(textContent, "text/plain; charset=" + CHARSET_USED);
			multipart.addBodyPart(plainMessageBodyPart);
			final BodyPart htmlMessageBodyPart = new MimeBodyPart();
			htmlMessageBodyPart.setContent(htmlContent, "text/html; charset=" + CHARSET_USED);
			multipart.addBodyPart(htmlMessageBodyPart);
			bodyPart.setContent(multipart);
		} else if (textContent != null) {
			bodyPart.setText(textContent);
		} else if (htmlContent != null) {
			bodyPart.setContent(htmlContent, "text/html; charset=" + CHARSET_USED);
		}
	}

	private BodyPart createBodyFile(final VFile vFile) throws MessagingException {
		try {
			final File file = fileManager.obtainReadOnlyFile(vFile);
			final MimeBodyPart bodyFile = new MimeBodyPart();
			bodyFile.attachFile(file);
			bodyFile.setFileName(vFile.getFileName());
			return bodyFile;
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Can't read attached file");
		}
	}

	private static VUserException createMailException(final MessageKey messageKey, final MessagingException messagingException, final Serializable... params) {
		final VUserException mailException = new VUserException(new MessageText(null, messageKey, params));
		mailException.initCause(messagingException);
		return mailException;
	}

	/** {@inheritDoc} */
	@Override
	public List<ComponentInfo> getInfos() {
		final List<ComponentInfo> componentInfos = new ArrayList<>();
		componentInfos.add(new ComponentInfo("mail.sent", mailSent));
		return componentInfos;
	}
}
