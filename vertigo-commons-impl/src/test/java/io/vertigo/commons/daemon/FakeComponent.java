/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.daemon;

import java.util.Collections;
import java.util.List;

import javax.inject.Inject;

import io.vertigo.core.component.Component;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.lang.Assertion;

public class FakeComponent implements Component, SimpleDefinitionProvider {
	private int executions = 0;

	@Inject
	public FakeComponent() {
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new DaemonDefinition("DMN_SIMPLE", () -> new SimpleDaemon(this), 2));
	}

	public int getExecutionCount() {
		return executions;
	}

	private void execute() {
		executions++;
		if (executions == 1) {
			throw new IllegalStateException();
		}
	}

	public static final class SimpleDaemon implements Daemon {
		private final FakeComponent fakeComponent;

		SimpleDaemon(final FakeComponent fakeComponent) {
			Assertion.checkNotNull(fakeComponent);
			//---
			this.fakeComponent = fakeComponent;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			fakeComponent.execute();
		}
	}
}
