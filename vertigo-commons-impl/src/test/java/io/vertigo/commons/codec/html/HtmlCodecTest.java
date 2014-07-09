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
package io.vertigo.commons.codec.html;

import io.vertigo.commons.codec.AbstractCodecTest;
import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test l'encodeur HTML.
 * @author npiedeloup
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
		//Caractères simples sans encodage
		Assert.assertEquals(codec.encode("abcdefghijklmnopqrstuvwxyz"), "abcdefghijklmnopqrstuvwxyz");
		Assert.assertEquals(codec.encode("0123456789"), "0123456789");
		Assert.assertEquals(codec.encode("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Assert.assertEquals(codec.encode(" "), " ");
		Assert.assertEquals(codec.encode(""), "");
		Assert.assertEquals(codec.encode("/"), "/");
		Assert.assertEquals(codec.encode(";"), ";");
		Assert.assertEquals(codec.encode("-"), "-");
		//Mixte de caractères simples
		Assert.assertEquals(codec.encode("abcdef; 01234/ABCDEF-"), "abcdef; 01234/ABCDEF-");
		//Accents
		Assert.assertEquals(codec.encode("é"), "&eacute;");
		Assert.assertEquals(codec.encode("è"), "&egrave;");
		//Symbole euro
		Assert.assertEquals(codec.encode(Character.toString((char) 8364)), "&euro;");
		Assert.assertEquals(codec.encode(Character.toString((char) 128)), "&euro;");
		// < >
		Assert.assertEquals(codec.encode("<tag>"), "&lt;tag&gt;");
		// caractères spéciaux :  "
		Assert.assertEquals(codec.encode("\""), "&quot;");

		/*
		 Pour réaliser des benchs de perfs
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
		//Caractères simples sans encodage
		Assert.assertEquals(codec.decode("abcdefghijklmnopqrstuvwxyz"), "abcdefghijklmnopqrstuvwxyz");
		Assert.assertEquals(codec.decode("0123456789"), "0123456789");
		Assert.assertEquals(codec.decode("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		Assert.assertEquals(codec.decode(" "), " ");
		Assert.assertEquals(codec.decode(""), "");
		Assert.assertEquals(codec.decode("/"), "/");
		Assert.assertEquals(codec.decode("&"), "&");
		Assert.assertEquals(codec.decode(";"), ";");
		Assert.assertEquals(codec.decode("-"), "-");
		//Mixte de caractères simples
		Assert.assertEquals(codec.decode("abcdef; 01234/ABCDEF-"), "abcdef; 01234/ABCDEF-");
		//Accents
		Assert.assertEquals(codec.decode("&eacute;"), "é");
		Assert.assertEquals(codec.decode("&egrave;"), "è");
		//Symbole euro (dans HtmlCodec, l'euro est actuellement transformé en char 128)
		Assert.assertEquals(codec.decode("&euro;"), Character.toString((char) 128));
		// < >
		Assert.assertEquals(codec.decode("&lt;tag&gt;"), "<tag>");
		// caractères spéciaux :  "
		Assert.assertEquals(codec.decode("&quot;"), "\"");
	}

	/** {@inheritDoc} */
	@Override
	@Test(expected = Exception.class)
	public void testFailDecode() throws Exception {
		codec.decode("&eplat;");
	}

	/**
	 * Test les chaines qui ne doivent pas être décodée.
	 */
	@Test
	public void testNotDecode() {
		Assert.assertEquals("&eplat", codec.decode("&eplat"));
		Assert.assertEquals("&testtroplong;", codec.decode("&testtroplong;"));
		Assert.assertEquals("& deux moutons", codec.decode("& deux moutons"));
	}

	/**
	 * Test les chaines qui ne doivent pas être encodée.
	 */
	@Test
	public void testNotEncode() {
		Assert.assertEquals("&#88;", codec.encode("&#88;"));
		Assert.assertEquals("&#885;", codec.encode("&#885;"));
		Assert.assertEquals("&#8859;", codec.encode("&#8859;"));
	}
}
