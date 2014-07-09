package io.vertigo.labs.plugins.mail.javaxmail;

import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.component.ComponentInfo;
import io.vertigo.kernel.component.Describable;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.exception.VUserException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.MessageKey;
import io.vertigo.kernel.lang.MessageText;
import io.vertigo.labs.impl.mail.Resources;
import io.vertigo.labs.impl.mail.SendMailPlugin;
import io.vertigo.labs.mail.Mail;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import javax.inject.Inject;
import javax.inject.Named;
import javax.mail.BodyPart;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;

/**
 * Plugin de gestion des mails, pour l'impl�mentation du jdk.
 * @author npiedeloup
 * @version $Id: JavaxSendMailPlugin.java,v 1.10 2014/01/28 18:49:55 pchretien Exp $
 */
public final class JavaxSendMailPlugin implements SendMailPlugin, Describable {
	private static final String CHARSET_USED = "ISO-8859-1";
	private final FileManager fileManager;
	private final String mailStoreProtocol;
	private final String mailHost;
	private final boolean developmentMode;
	private final String developmentMailTo;
	/** Compteur de mails envoy�s. */
	private int mailSent = 0;

	/**
	 * Cr�e le plugin d'envoie de mail.
	 * @param fileManager Manager de gestion des fichiers
	 * @param mailStoreProtocol Protocole utilis�
	 * @param mailHost Serveur de mail
	 * @param developmentMode Indique s'il le mode developpement est activ� (surcharge des emails destinataires)
	 * @param developmentMailTo Email destinataire forc� pour d�veloppement
	 */
	@Inject
	public JavaxSendMailPlugin(final FileManager fileManager, @Named("storeProtocol") final String mailStoreProtocol, @Named("host") final String mailHost, @Named("developmentMode") final boolean developmentMode, @Named("developmentMailTo") final String developmentMailTo) {
		Assertion.checkNotNull(fileManager);
		Assertion.checkArgNotEmpty(mailStoreProtocol);
		Assertion.checkArgNotEmpty(mailHost);
		Assertion.checkArgNotEmpty(developmentMailTo);
		//---------------------------------------------------------------------
		this.fileManager = fileManager;
		this.mailStoreProtocol = mailStoreProtocol;
		this.mailHost = mailHost;
		this.developmentMailTo = developmentMailTo;
		this.developmentMode = developmentMode;
	}

	/** {@inheritDoc} */
	public void sendMail(final Mail mail) {
		Assertion.checkNotNull(mail);
		//---------------------------------------------------------------------
		try {
			final Properties properties = new Properties();
			properties.put("mail.store.protocol", mailStoreProtocol);
			properties.put("mail.host", mailHost);
			properties.put("mail.debug", "false");

			final Session session = Session.getDefaultInstance(properties);
			session.setDebug(false);

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

			final List<KFile> attachments = mail.getAttachments();
			if (attachments.isEmpty()) {
				setBodyContent(mail.getTextContent(), mail.getHtmlContent(), message);
			} else {
				final Multipart multiPart = new MimeMultipart();
				final BodyPart bodyPart = new MimeBodyPart();
				setBodyContent(mail.getTextContent(), mail.getHtmlContent(), bodyPart);
				multiPart.addBodyPart(bodyPart);

				for (final KFile kFile : attachments) {
					final BodyPart bodyFile = createBodyFile(kFile);
					multiPart.addBodyPart(bodyFile);
				}
				message.setContent(multiPart);
			}

			Transport.send(message);
			mailSent++; //on ne synchronize pas pour des stats peu importantes		
		} catch (final MessagingException e) {
			throw createMailException(Resources.KASPER_MAIL_SERVER_TIMEOUT, e);
		} catch (final UnsupportedEncodingException e) {
			throw new VRuntimeException("Probl�me d'encodage lors de l'envoi du mail", e, mailHost, mailStoreProtocol);
		}
	}

	private void setFromAddress(final String from, final Message message) throws MessagingException {
		Assertion.checkNotNull(from);
		Assertion.checkNotNull(message);
		//---------------------------------------------------------------------

		try {
			message.setFrom(createInternetAddress(from));
		} catch (final AddressException e) {
			//on catch ici, pour pouvoir indiquer l'adresse qui pose pb
			throw createMailException(Resources.KASPER_MAIL_ADRESS_MAIL_INVALID, e, from);
		}
	}

	private void setReplyToAddress(final String replyTo, final Message message) throws MessagingException {
		Assertion.checkNotNull(message);
		Assertion.checkNotNull(replyTo);
		//---------------------------------------------------------------------
		try {
			final InternetAddress[] replyToArray = { createInternetAddress(replyTo) };
			message.setReplyTo(replyToArray);
		} catch (final AddressException e) {
			//on catch ici, pour pouvoir indiquer l'adresse qui pose pb
			throw createMailException(Resources.KASPER_MAIL_ADRESS_MAIL_INVALID, e, replyTo);
		}
	}

	private InternetAddress createInternetAddress(final String address) throws AddressException {
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
		Assertion.checkArgument(!addressList.isEmpty(), "La liste des destinataires ne doit pas �tre vide");
		Assertion.checkNotNull(message);
		//---------------------------------------------------------------------
		final InternetAddress[] addresses = new InternetAddress[addressList.size()];
		for (int i = 0; i < addressList.size(); i++) {
			try {
				addresses[i] = createInternetAddress(addressList.get(i));
				if (developmentMode) {
					//en mode debug on change l'adresse, mais on laisse le nom.
					addresses[i].setAddress(developmentMailTo);
				}
			} catch (final AddressException e) {
				//on catch ici, pour pouvoir indiquer l'adresse qui pose pb
				throw createMailException(Resources.KASPER_MAIL_ADRESS_MAIL_INVALID, e, addressList.get(i));
			}
		}
		message.setRecipients(type, addresses);

	}

	private void setBodyContent(final String textContent, final String htmlContent, final Part bodyPart) throws MessagingException {
		Assertion.checkArgument(textContent != null || htmlContent != null, "Le mail n'a pas de contenu, ni en text, ni en html");
		Assertion.checkNotNull(bodyPart);
		//---------------------------------------------------------------------
		if (textContent != null && htmlContent != null) {
			final Multipart multipart = new MimeMultipart("alternative");

			final BodyPart plainMessageBodyPart = new MimeBodyPart();
			plainMessageBodyPart.setContent(textContent, "text/plain");
			multipart.addBodyPart(plainMessageBodyPart);

			final BodyPart htmlMessageBodyPart = new MimeBodyPart();
			htmlMessageBodyPart.setContent(htmlContent, "text/html");
			multipart.addBodyPart(htmlMessageBodyPart);

			bodyPart.setContent(multipart);
		} else if (textContent != null) {
			bodyPart.setText(textContent);
		} else if (htmlContent != null) {
			bodyPart.setContent(htmlContent, "text/html");
		}
	}

	private BodyPart createBodyFile(final KFile kFile) throws MessagingException {
		try {
			final File file = fileManager.obtainReadOnlyFile(kFile);
			final MimeBodyPart bodyFile = new MimeBodyPart();
			bodyFile.attachFile(file);
			bodyFile.setFileName(kFile.getFileName());
			return bodyFile;
		} catch (final IOException e) {
			throw new VRuntimeException("Erreur de lecture des pi�ces jointes", null, "");
		}
	}

	private static VUserException createMailException(final MessageKey messageKey, final MessagingException messagingException, final Serializable... params) {
		final VUserException mailException = new VUserException(new MessageText(null, messageKey, params));
		mailException.initCause(messagingException);
		return mailException;
	}

	/** {@inheritDoc} */
	public List<ComponentInfo> getInfos() {
		final List<ComponentInfo> componentInfos = new ArrayList<>();
		componentInfos.add(new ComponentInfo("mail.sent", mailSent));
		return componentInfos;
	}
}
