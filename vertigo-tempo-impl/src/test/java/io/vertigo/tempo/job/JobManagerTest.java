/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.tempo.job;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.tempo.job.metamodel.JobDefinition;

/**
 * Test de l'implémentation standard.
 *
 * @author pchretien
 */
public class JobManagerTest extends AbstractTestCaseJU4 {
	@Inject
	private JobManager jobManager;

	@Override
	protected void doSetUp() throws Exception {
		TestJob.reset();
	}

	@Test
	public void testExecute() {
		final JobDefinition jobDefinition = new JobDefinition("JB_TEST_SYNC", TestJob.class);
		getApp().getDefinitionSpace().put(jobDefinition);

		jobManager.execute(jobDefinition);
		Assert.assertEquals(1, TestJob.getCount());
	}
}
