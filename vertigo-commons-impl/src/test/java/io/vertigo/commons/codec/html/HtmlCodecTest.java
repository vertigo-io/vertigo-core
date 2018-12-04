/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.commons.codec.AbstractCodecTest;
import io.vertigo.commons.codec.Codec;
import io.vertigo.commons.codec.CodecManager;

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
	public void testNull() {
		assertEquals("", codec.encode(null));
		assertEquals("", codec.encode(""));
		assertEquals(null, codec.decode(null));
		assertEquals("", codec.decode(""));
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() {
		//Caractères simples sans encodage
		assertEquals(codec.encode("abcdefghijklmnopqrstuvwxyz"), "abcdefghijklmnopqrstuvwxyz");
		assertEquals(codec.encode("0123456789"), "0123456789");
		assertEquals(codec.encode("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		assertEquals(codec.encode(" "), " ");
		assertEquals(codec.encode(""), "");
		assertEquals(codec.encode("/"), "/");
		assertEquals(codec.encode(";"), ";");
		assertEquals(codec.encode("-"), "-");
		//Mixte de caractères simples
		assertEquals(codec.encode("abcdef; 01234/ABCDEF-"), "abcdef; 01234/ABCDEF-");
		//Accents
		assertEquals(codec.encode("é"), "&eacute;");
		assertEquals(codec.encode("è"), "&egrave;");
		//Symbole euro
		assertEquals(codec.encode(Character.toString((char) 8364)), "&euro;");
		assertEquals(codec.encode(Character.toString((char) 128)), "&euro;");
		// < >
		assertEquals(codec.encode("<tag>"), "&lt;tag&gt;");
		// caractères spéciaux :  "
		assertEquals(codec.encode("\""), "&quot;");

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
		assertEquals(codec.decode("abcdefghijklmnopqrstuvwxyz"), "abcdefghijklmnopqrstuvwxyz");
		assertEquals(codec.decode("0123456789"), "0123456789");
		assertEquals(codec.decode("ABCDEFGHIJKLMNOPQRSTUVWXYZ"), "ABCDEFGHIJKLMNOPQRSTUVWXYZ");
		assertEquals(codec.decode(" "), " ");
		assertEquals(codec.decode(""), "");
		assertEquals(codec.decode("/"), "/");
		assertEquals(codec.decode("&"), "&");
		assertEquals(codec.decode(";"), ";");
		assertEquals(codec.decode("-"), "-");
		//Mixte de caractères simples
		assertEquals(codec.decode("abcdef; 01234/ABCDEF-"), "abcdef; 01234/ABCDEF-");
		//Accents
		assertEquals(codec.decode("&eacute;"), "é");
		assertEquals(codec.decode("&egrave;"), "è");
		//Symbole euro (dans HtmlCodec, l'euro est actuellement transformé en char 128)
		assertEquals(codec.decode("&euro;"), Character.toString((char) 128));
		// < >
		assertEquals(codec.decode("&lt;tag&gt;"), "<tag>");
		// caractères spéciaux :  "
		assertEquals(codec.decode("&quot;"), "\"");
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testFailDecode() {
		Assertions.assertThrows(Exception.class, () -> codec.decode("&eplat;"));
	}

	/**
	 * Test les chaines qui ne doivent pas être décodée.
	 */
	@Test
	public void testNotDecode() {
		assertEquals("&eplat", codec.decode("&eplat"));
		assertEquals("&testtroplong;", codec.decode("&testtroplong;"));
		assertEquals("& deux moutons", codec.decode("& deux moutons"));
	}

	/**
	 * Test les chaines qui ne doivent pas être encodée.
	 */
	@Test
	public void testNotEncode() {
		assertEquals("&#88;", codec.encode("&#88;"));
		assertEquals("&#885;", codec.encode("&#885;"));
		assertEquals("&#8859;", codec.encode("&#8859;"));
	}
}
