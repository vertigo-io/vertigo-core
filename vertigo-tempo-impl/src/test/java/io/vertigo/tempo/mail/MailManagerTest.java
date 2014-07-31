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

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.dynamo.file.FileManager;
import io.vertigo.dynamo.file.model.KFile;
import io.vertigo.kernel.exception.VUserException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.tempo.mail.Mail;
import io.vertigo.tempo.mail.MailBuilder;
import io.vertigo.tempo.mail.MailManager;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test de l'implémentation standard.
 * 
 * @author npiedeloup
 * @version $Id: MailManagerTest.java,v 1.8 2014/01/28 18:53:45 pchretien Exp $
 */
public final class MailManagerTest extends AbstractTestCaseJU4 {
	//private static final String DT_MAIL = "Direction Technique<direction.technique@kleegroup.com>";
	//private static final String NPI_MAIL = "Nicolas Piedeloup<npiedeloup@kleegroup.com>";
	private static final String DT_MAIL = "Direction Technique<direction.technique@yopmail.com>";
	private static final String NPI_MAIL = "Nicolas Piedeloup<npiedeloup@yopmail.com>";

	@Inject
	private MailManager mailManager;
	@Inject
	private FileManager fileManager;

	/**
	 * @throws Exception manager null
	 */
	@Test
	public void testNotNull() throws Exception {
		Assertion.checkNotNull(mailManager);
	}

	/**
	 * Crée un mail simple.
	 */
	@Test
	public void testSimpliestCreateMail() {
		final Mail mail = new MailBuilder()//
				.from(DT_MAIL)//
				.to(NPI_MAIL)//		
				.withSubject("-1-testSimpliestCreateMail")//
				.withTextContent("test") //
				.build();
		Assert.assertNotNull(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = NullPointerException.class)
	public void testWritableMailErrorsWithNullAddress() {
		final String to = null;
		final Mail mail = new MailBuilder()//
				.withSubject("-3-testWritableMailErrors")//
				.to(to)//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWritableMailErrorsWithEmptyAddress() {
		final Mail mail = new MailBuilder()//
				.withSubject("-4-testWritableMailErrors")//
				.to("")//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = NullPointerException.class)
	public void testWritableMailErrorsWithNullSubject() {
		final Mail mail = new MailBuilder()//
				.withSubject(null).to(NPI_MAIL)//
				.build();
		nop(mail);

	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWritableMailErrorsWithEmptySubject() {
		final Mail mail = new MailBuilder()//
				.withSubject("")//
				.to(NPI_MAIL)//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = NullPointerException.class)
	public void testWritableMailErrorsWithNullFrom() {
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.from(null)//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWritableMailErrorsWithEmptyFrom() {
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.from("")//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = NullPointerException.class)
	public void testWritableMailErrorsWithNullHtmlContent() {
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.withHtmlContent(null)//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWritableMailErrorsWithEmptyHtmlContent() {
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.withHtmlContent("")//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = NullPointerException.class)
	public void testWritableMailErrorsWithNullContent() {
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.withTextContent(null)//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWritableMailErrorsWithEmptyContent() {
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.withTextContent("")//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = NullPointerException.class)
	public void testWritableMailErrorsWithNullReply() {
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.replyTo(null)//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWritableMailErrorsWithEmptyReply() {
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.to("")//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = NullPointerException.class)
	public void testWritableMailErrorsWithNullTo() {
		final String to = null;
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.to(to)//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWritableMailErrorsWithEmptyTo() {
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//	
				.to("")//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = NullPointerException.class)
	public void testWritableMailErrorsWithNullCc() {
		final String cc = null;

		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.cc(cc)//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testWritableMailErrorsWithEmptyCc() {
		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.cc("")//
				.build();
		nop(mail);
	}

	/**
	 * Test les erreurs de creation de mail. 
	 */
	@Test(expected = NullPointerException.class)
	public void testWritableMailErrorsWithNullAttachment() {
		final KFile files = null;

		final Mail mail = new MailBuilder()//
				.withSubject("-5-testWritableMailErrors")//
				.to(DT_MAIL)//
				.withAttachments(files) //
				.build();
		nop(mail);
	}

	//	/**
	//	 * Test l'emetteur par défaut.
	//	 */
	//	@Test
	//	public void testDefaultFrom() {
	//		final Mail mail = new MailBuilder()//
	//				.to(NPI_MAIL)//		
	//				.withSubject("-6-testWritableMail")//
	//				.build();
	//		Assert.assertEquals("npiedeloup@kleegroup.com", mail.getFrom());
	//	}

	/**
	 * Test l'envoi d'un mail vide.
	 */
	@Test(expected = NullPointerException.class)
	public void testSendEmptyMail() {
		final Mail mail = new MailBuilder()//
				.to(NPI_MAIL)//		
				.withSubject("-7-testSendEmptyMail")//
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail simple.
	 */
	@Test
	public void testSendSimpliestMail() {
		final Mail mail = new MailBuilder()//
				.from(DT_MAIL)//
				.to(NPI_MAIL)//		
				.withSubject("1-testSendSimpliestMail")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un From.
	 */
	@Test
	public void testSendMailWithFrom() {
		final Mail mail = new MailBuilder()//
				.to(NPI_MAIL)//		
				.withSubject("2-testSendMailWithFrom")//
				.from(DT_MAIL)//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un To erroné.
	 */
	@Test(expected = NullPointerException.class)
	public void testSendMailWithBadTo() {
		final Mail mail = new MailBuilder()//
				.withSubject("-8-testWritableMailWithBadTo")//
				.to("NOT-A-EMAIL")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.build();

		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un From erroné.
	 */
	@Test(expected = VUserException.class)
	public void testSendMailWithBadFrom() {
		final Mail mail = new MailBuilder()//
				.to(NPI_MAIL)//		
				.withSubject("-9-testWritableMailWithBadFrom")//
				.from("NOT-A-EMAIL")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.build();

		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un ReplyTo erroné.
	 */
	@Test(expected = NullPointerException.class)
	public void testSendMailWithBadReplyTo() {
		final Mail mail = new MailBuilder()//
				.to(NPI_MAIL)//		
				.withSubject("-10-testWritableMailWithBadReplyTo")//
				.replyTo("NOT-A-EMAIL")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.build();

		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un To erroné.
	 */
	@Test(expected = NullPointerException.class)
	public void testSendMailWithBadAddTo() {
		final Mail mail = new MailBuilder()//
				.to(NPI_MAIL)//		
				.withSubject("-11-testWritableMailWithBadAddTo")//
				.to("NOT-A-EMAIL")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.build();

		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un Cc erroné.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void testSendMailWithBadCc() {
		final Mail mail = new MailBuilder()//
				.from(DT_MAIL)//
				.withSubject("-12-testWritableMailWithBadCc")//
				.cc("NOT-A-EMAIL")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.build();

		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec des destinataires multiples.
	 */
	@Test
	public void testSendMailMultipleTo() {
		final Mail mail = new MailBuilder()//
				.from(DT_MAIL)//
				.withSubject("3-testSendMailMultipleTo")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.to("Denis Challas (to) <dchallas@kleegroup.com>")//
				.to("Philippe Chretien (to)<pchretien@kleegroup.com>")//
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec des destinataires en copie.
	 */
	@Test
	public void testSendMailMultipleCc() {
		final Mail mail = new MailBuilder()//
				.from(NPI_MAIL)//
				.to(NPI_MAIL)//
				.withSubject("4-testSendMailMultipleCc")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.cc("Philippe Chretien (cc)<pchretien@kleegroup.com>")//
				.cc("Denis Challas (cc)<dchallas@kleegroup.com>")//
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un replyTo différent du From.
	 */
	@Test
	public void testSendMailDifferentReplyTo() {
		final Mail mail = new MailBuilder()//
				.from(NPI_MAIL)//		
				.to(NPI_MAIL)//		
				.withSubject("5-testSendMailDifferentReplyTo")//
				.replyTo(DT_MAIL)//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un body html.
	 */
	@Test
	public void testSendMailHtmlOnly() {
		final Mail mail = new MailBuilder()//
				.from(DT_MAIL)//
				.to(NPI_MAIL)//		
				.withSubject("6-testSendMailHtmlOnly")//
				.withHtmlContent("Mon test en <b>HTML</b>")//
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un body html et text alternatif.
	 */
	@Test
	public void testSendMailTextAndHtml() {
		final Mail mail = new MailBuilder()//
				.from(DT_MAIL)//
				.to(NPI_MAIL)//		
				.withSubject("7-testSendMailTextAndHtml")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.withHtmlContent("Mon test en <b>HTML</b>")//
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un body html et text alternatif.
	 */
	@Test
	public void testSendMailWithPJ() {
		final KFile image = TestUtil.createKFile(fileManager, "data/logo.jpg", getClass());

		final Mail mail = new MailBuilder()//
				.from(DT_MAIL)//
				.to(NPI_MAIL)//
				.withSubject("8-testSendMailWithPJ")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.withAttachments(image)//
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un body html et text alternatif.
	 */
	@Test
	public void testSendMailWithOneContentTwoPJ() {
		final Mail mail = new MailBuilder()//
				.from(DT_MAIL)//		
				.to(NPI_MAIL)//		
				.withSubject("9-testSendMailWithOneContentTwoPJ")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.withAttachments(TestUtil.createKFile(fileManager, "data/logo.jpg", getClass()))//
				.withAttachments(TestUtil.createKFile(fileManager, "data/test.txt", getClass()))//
				.build();
		mailManager.sendMail(mail);
	}

	/**
	 * Test l'envoi d'un mail avec un body html et text alternatif.
	 */
	@Test
	public void testSendMailFull() {
		final Mail mail = new MailBuilder()//
				.to(NPI_MAIL)//		
				.withSubject("-2-testWritableMail")//
				.from(DT_MAIL)//
				.withHtmlContent("Mon test en <b>HTML</b>")//
				.withTextContent("Mon test en <b>TEXT</b>")//
				.to("npiedeloup@kleegroup.com")//
				.to("Denis Challas (to) <dchallas@kleegroup.com>")//
				.to("Philippe Chretien (to)<pchretien@kleegroup.com>")//
				.to("Philippe Chretien (cc)<pchretien@kleegroup.com>")//
				.to("Denis Challas (cc)<dchallas@kleegroup.com>")//

				.withAttachments(TestUtil.createKFile(fileManager, "data/logo.jpg", getClass()))//
				.withAttachments(TestUtil.createKFile(fileManager, "data/test.txt", getClass()))//
				.build();
		mailManager.sendMail(mail);
	}
}
