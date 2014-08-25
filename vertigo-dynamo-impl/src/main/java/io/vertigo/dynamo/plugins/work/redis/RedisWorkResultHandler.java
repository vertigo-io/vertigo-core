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
package io.vertigo.dynamo.plugins.work.redis;

import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Assertion;

/**
 * @author pchretien
 * $Id: RedisWorkResultHandler.java,v 1.7 2014/02/27 10:31:38 pchretien Exp $
 */
final class RedisWorkResultHandler<WR> implements WorkResultHandler<WR> {
	private final RedisDB redisDB;
	private final String workId;

	RedisWorkResultHandler(final String workId, final RedisDB redisDB) {
		Assertion.checkNotNull(workId);
		Assertion.checkNotNull(redisDB);
		//---------------------------------------------------------------------
		this.redisDB = redisDB;
		this.workId = workId;

	}

	/** {@inheritDoc} */
	public void onStart() {
		//
	}

	/** {@inheritDoc} */
	public void onDone(final boolean succeeded, final WR result, final Throwable error) {
		redisDB.putResult(new WResult(workId, succeeded, result, error));
	}
}
