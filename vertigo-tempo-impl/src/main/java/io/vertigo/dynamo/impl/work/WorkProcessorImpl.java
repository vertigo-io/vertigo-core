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
package io.vertigo.dynamo.impl.work;

import io.vertigo.dynamo.work.WorkEngine;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkProcessor;
import io.vertigo.lang.Assertion;

import java.util.Arrays;

/**
 *
 * @author pchretien
 * @param<R> result
 * @param<W> work
 */
final class WorkProcessorImpl<R, W> implements WorkProcessor<R, W> {
	private final WorkManager workManager;
	private final WorkEngineProvider[] workEngineProviders;

	WorkProcessorImpl(final WorkManager workManager, final WorkEngineProvider workEngineProvider) {
		this(workManager, new WorkEngineProvider[] { workEngineProvider });
		//-----
		Assertion.checkNotNull(workEngineProvider);
	}

	private WorkProcessorImpl(final WorkManager workManager, final WorkEngineProvider[] workEngineProviders) {
		Assertion.checkNotNull(workManager);
		Assertion.checkNotNull(workEngineProviders);
		//-----
		this.workEngineProviders = workEngineProviders;
		this.workManager = workManager;
	}

	/** {@inheritDoc} */
	@Override
	public <WR1> WorkProcessor<WR1, W> then(final WorkEngineProvider<WR1, R> workEngineProvider) {
		Assertion.checkNotNull(workEngineProvider);
		//-----
		final WorkEngineProvider[] list = Arrays.copyOf(workEngineProviders, workEngineProviders.length + 1);
		list[workEngineProviders.length] = workEngineProvider;
		return new WorkProcessorImpl<>(workManager, list);
	}

	/** {@inheritDoc} */
	@Override
	public <WR1> WorkProcessor<WR1, W> then(final Class<? extends WorkEngine<WR1, R>> clazz) {
		Assertion.checkNotNull(clazz);
		//-----
		return then(new WorkEngineProvider<>(clazz));
	}

	/** {@inheritDoc} */
	@Override
	public R exec(final W input) {
		Object result = input;
		for (final WorkEngineProvider workEngineProvider : workEngineProviders) {
			result = workManager.process(result, workEngineProvider);
		}
		return (R) result;
	}
}
