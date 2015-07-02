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
package io.vertigo.commons.daemon;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author TINGARGIOLA
 */
public final class DaemonManagerTest extends AbstractTestCaseJU4 {

	@Inject
	private DaemonManager daemonManager;

	@Override
	public void doSetUp() {
		final DaemonDefinition daemonDefinition = new DaemonDefinition("DMN_SIMPLE", SimpleDaemon.class, 3);
		Home.getDefinitionSpace().put(daemonDefinition);
	}

	@Test
	public void testSimple() throws Exception {
		Assert.assertEquals(0, SimpleDaemon.executions);
		// -----
		daemonManager.startAllDaemons();
		Assert.assertEquals(0, SimpleDaemon.executions);
		Thread.sleep(3000);
		Assert.assertTrue(SimpleDaemon.executions > 0);
	}

	public static final class SimpleDaemon implements Daemon {

		static int executions = 0;

		/** {@inheritDoc} */
		@Override
		public void run() throws Exception {
			executions++;
		}
	}
}
