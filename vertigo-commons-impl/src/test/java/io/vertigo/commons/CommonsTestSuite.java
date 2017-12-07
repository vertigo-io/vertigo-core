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
package io.vertigo.commons;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import io.vertigo.commons.analytics.health.HealthAnalyticsTest;
import io.vertigo.commons.analytics.metric.MetricAnalyticsTest;
import io.vertigo.commons.analytics.process.ProcessAnalyticsTest;
import io.vertigo.commons.cache.ehcache.EhCacheManagerTest;
import io.vertigo.commons.cache.memory.MemoryCacheManagerTest;
import io.vertigo.commons.cache.redis.RedisCacheManagerTest;
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
import io.vertigo.commons.daemon.DaemonManagerTest;
import io.vertigo.commons.eventbus.EventBusManagerTest;
import io.vertigo.commons.node.DbNodeRegistryPluginTest;
import io.vertigo.commons.node.RedisNodeRegistryPluginTest;
import io.vertigo.commons.node.SingleNodeRegistryPluginTest;
import io.vertigo.commons.peg.CalculatorTest;
import io.vertigo.commons.peg.ParserTest;
import io.vertigo.commons.peg.PegRulesTest;
import io.vertigo.commons.script.ScriptManagerTest;
import io.vertigo.commons.transaction.VTransactionBeforeAfterCommitTest;
import io.vertigo.commons.transaction.VTransactionManagerTest;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
@RunWith(Suite.class)
@SuiteClasses({
		//--analytics
		ProcessAnalyticsTest.class,
		HealthAnalyticsTest.class,
		MetricAnalyticsTest.class,
		//--cache
		EhCacheManagerTest.class,
		MemoryCacheManagerTest.class,
		RedisCacheManagerTest.class,
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
		//--daemon
		DaemonManagerTest.class,
		//--node
		SingleNodeRegistryPluginTest.class,
		RedisNodeRegistryPluginTest.class,
		DbNodeRegistryPluginTest.class,
		//--script
		ScriptManagerTest.class,
		//--parser
		PegRulesTest.class,
		ParserTest.class,
		CalculatorTest.class,
		EventBusManagerTest.class,
		//--transaction
		VTransactionManagerTest.class,
		VTransactionBeforeAfterCommitTest.class
})

public final class CommonsTestSuite {
	//
}
