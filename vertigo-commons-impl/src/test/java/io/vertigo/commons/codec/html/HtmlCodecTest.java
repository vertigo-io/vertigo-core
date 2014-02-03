package io.vertigo.commons.codec.html;

import io.vertigo.commons.codec.AbstractCodecTest;
import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test l'encodeur HTML.
 * @author npiedeloup
 * $Id: HtmlCodecTest.java,v 1.4 2013/11/15 15:50:56 pchretien Exp $
 */
public final class HtmlCodecTest extends AbstractCodecTest<String, String> {
	/** {@inheritDoc} */
	@Override
	public Codec<String, String> obtainCodec(final CodecManager codecManager) {
		return codecManager.getHtmlCodec();
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testNull() throws Exception {
		Assert.assertEquals("", codec.encode(null));
		Assert.assertEquals("", codec.encode(""));
		Assert.assertEquals(null, codec.decode(null));
		Assert.assertEquals("", codec.decode(""));
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() {
		//Carcat�res simples sans encodage
		Assert.assertEquals(codec.encode("abcdefghijklmnopqrstuvwxyz"), "abcdefghijklmnopqrstuvwxyz");
		Assert.assertEquals(codec.encode("0123456789"), "0123456789");
		Assert.assertEquals(codec.encode("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Assert.assertEquals(codec.encode(" "), " ");
		Assert.assertEquals(codec.encode(""), "");
		Assert.assertEquals(codec.encode("/"), "/");
		Assert.assertEquals(codec.encode(";"), ";");
		Assert.assertEquals(codec.encode("-"), "-");
		//Mixte de caract�res simples
		Assert.assertEquals(codec.encode("abcdef; 01234/ABCDEF-"), "abcdef; 01234/ABCDEF-");
		//Accents
		Assert.assertEquals(codec.encode("é"), "&eacute;");
		Assert.assertEquals(codec.encode("è"), "&egrave;");
		//Symbole euro
		Assert.assertEquals(codec.encode(Character.toString((char) 8364)), "&euro;");
		Assert.assertEquals(codec.encode(Character.toString((char) 128)), "&euro;");
		// < >
		Assert.assertEquals(codec.encode("<tag>"), "&lt;tag&gt;");
		// caract�res sp�ciaux :  "
		Assert.assertEquals(codec.encode("\""), "&quot;");

		/*
		 Pour r�aliser des benchs de perfs
		 for (int i = 0; i < 100; i++) {
		  String s = TestUtils.randomUserString(100000);
		  final String codedValue = encoder.encodeString(s);
		  final String decodedValue = decoder.decode(codedValue);
		  assertEquals(s, decodedValue);
		 }*/
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testDecode() {
		//Carcat�res simples sans encodage
		Assert.assertEquals(codec.decode("abcdefghijklmnopqrstuvwxyz"), "abcdefghijklmnopqrstuvwxyz");
		Assert.assertEquals(codec.decode("0123456789"), "0123456789");
		Assert.assertEquals(codec.decode("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Assert.assertEquals(codec.decode(" "), " ");
		Assert.assertEquals(codec.decode(""), "");
		Assert.assertEquals(codec.decode("/"), "/");
		Assert.assertEquals(codec.decode("&"), "&");
		Assert.assertEquals(codec.decode(";"), ";");
		Assert.assertEquals(codec.decode("-"), "-");
		//Mixte de caract�res simples
		Assert.assertEquals(codec.decode("abcdef; 01234/ABCDEF-"), "abcdef; 01234/ABCDEF-");
		//Accents
		Assert.assertEquals(codec.decode("&eacute;"), "é");
		Assert.assertEquals(codec.decode("&egrave;"), "è");
		//Symbole euro (dans HtmlCodec, l'euro est actuellement transform� en char 128)
		Assert.assertEquals(codec.decode("&euro;"), Character.toString((char) 128));
		// < >
		Assert.assertEquals(codec.decode("&lt;tag&gt;"), "<tag>");
		// caract�res sp�ciaux :  "
		Assert.assertEquals(codec.decode("&quot;"), "\"");
	}

	/** {@inheritDoc} */
	@Override
	@Test(expected = Exception.class)
	public void testFailDecode() throws Exception {
		codec.decode("&eplat;");
	}

	/**
	 * Test les chaines qui ne doivent pas �tre d�cod�e.
	 */
	@Test
	public void testNotDecode() {
		Assert.assertEquals("&eplat", codec.decode("&eplat"));
		Assert.assertEquals("&testtroplong;", codec.decode("&testtroplong;"));
		Assert.assertEquals("& deux moutons", codec.decode("& deux moutons"));
	}

	/**
	 * Test les chaines qui ne doivent pas �tre encod�e.
	 */
	@Test
	public void testNotEncode() {
		Assert.assertEquals("&#88;", codec.encode("&#88;"));
		Assert.assertEquals("&#885;", codec.encode("&#885;"));
		Assert.assertEquals("&#8859;", codec.encode("&#8859;"));
	}
}
