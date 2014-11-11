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
package io.vertigo.dynamo.work.mock;

import io.vertigo.dynamo.work.WorkEngine;

public final class SlowWorkEngine implements WorkEngine<Boolean, SlowWork> {

	/** {@inheritDoc} */
	@Override
	public Boolean process(final SlowWork work) {
		try {
			final long sleepTimeAvg = work.getSleepTime();
			final long sleepTimeMax = Math.round(sleepTimeAvg * 1.1d); //+10%
			final long sleepTimeMin = Math.round(sleepTimeAvg * 0.9d); //-10%
			Thread.sleep((sleepTimeMax + sleepTimeMin) / 2);
		} catch (final InterruptedException e) {
			return false;
		}
		return true;
	}
}
