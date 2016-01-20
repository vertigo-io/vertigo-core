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
package io.vertigo.dynamo.work.distributed.redis;

import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.dynamo.plugins.work.redis.RedisDB;
import io.vertigo.dynamo.work.AbstractWorkManagerTest;
import io.vertigo.lang.Option;

/**
 * @author pchretien
 */
public class RedisWorkManagerTest extends AbstractWorkManagerTest {
	@Override
	protected void doSetUp() throws Exception {
		final RedisDB redisDB = new RedisDB(new CodecManagerImpl(), "kasper-redis", 6379, 10, Option.<String> none());
		redisDB.start();
		redisDB.reset();
		redisDB.stop();
	}
}
