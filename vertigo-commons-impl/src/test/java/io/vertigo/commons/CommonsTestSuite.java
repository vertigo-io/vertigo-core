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
package io.vertigo.commons;

import io.vertigo.commons.analytics.AnalyticsManagerTest;
import io.vertigo.commons.cache.CacheManagerTest;
import io.vertigo.commons.codec.base64.Base64CodecTest;
import io.vertigo.commons.codec.compressedSerialization.CompressedSerializationCodecTest;
import io.vertigo.commons.codec.compression.CompressionCodecTest;
import io.vertigo.commons.codec.crypto.AES128CodecTest;
import io.vertigo.commons.codec.crypto.TripleDESCodecTest;
import io.vertigo.commons.codec.csv.CSVCodecTest;
import io.vertigo.commons.codec.hash.HashCodecMD5Test;
import io.vertigo.commons.codec.hash.HashCodecSha1Test;
import io.vertigo.commons.codec.hash.HashCodecSha256Test;
import io.vertigo.commons.codec.html.HtmlCodecTest;
import io.vertigo.commons.codec.serialization.SerializationCodecTest;
import io.vertigo.commons.parser.CalculatorTest;
import io.vertigo.commons.parser.ParserTest;
import io.vertigo.commons.script.ScriptManagerTest;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
@RunWith(Suite.class)
@SuiteClasses({
		//--analytics
		AnalyticsManagerTest.class,
		//--cache
		CacheManagerTest.class,
		//--codec
		Base64CodecTest.class,
		CompressedSerializationCodecTest.class,
		CompressionCodecTest.class,
		AES128CodecTest.class,
		TripleDESCodecTest.class,
		CSVCodecTest.class,
		HashCodecMD5Test.class,
		HashCodecSha1Test.class,
		HashCodecSha256Test.class,
		HtmlCodecTest.class,
		SerializationCodecTest.class,
		//--script
		ScriptManagerTest.class,
		//--parser
		ParserTest.class,
		CalculatorTest.class,
})
public final class CommonsTestSuite {
	//
}
