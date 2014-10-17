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
package io.vertigo.tempo.job;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.core.Home;
import io.vertigo.tempo.job.metamodel.JobDefinition;
import io.vertigo.util.DateBuilder;

import java.util.Date;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

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
		Home.getDefinitionSpace().put(jobDefinition, JobDefinition.class);

		jobManager.execute(jobDefinition);
		Assert.assertEquals(1, TestJob.getCount());
	}

	@Test
	public void testScheduleNow() throws InterruptedException {
		final JobDefinition jobDefinition = new JobDefinition("JB_TEST_ASYNC", TestJob.class);
		Home.getDefinitionSpace().put(jobDefinition, JobDefinition.class);

		jobManager.scheduleNow(jobDefinition);
		//Le traitement métier n'a pas encore été effectué, on le vérifie
		Assert.assertEquals(0, TestJob.getCount());

		//Le traitement métier doit avoir été effectué, on le vérifie
		Thread.sleep(500);
		Assert.assertEquals(1, TestJob.getCount());

		Thread.sleep(2500);
		//On vérifie qu'il n'a pas d'autre exécution
		Assert.assertEquals(1, TestJob.getCount());
	}

	@Test
	public void testScheduleAtDate() throws InterruptedException {
		final JobDefinition jobDefinition = new JobDefinition("JB_AT_DATE", TestJob.class);
		Home.getDefinitionSpace().put(jobDefinition, JobDefinition.class);

		final Date date = new DateBuilder(new Date()).addSeconds(1).build();
		jobManager.scheduleAtDate(jobDefinition, date);
		//Le traitement métier n'a pas encore été effectué, on le vérifie
		Assert.assertEquals(0, TestJob.getCount());

		Thread.sleep(1500);
		Assert.assertEquals(1, TestJob.getCount());

		Thread.sleep(1500);
		//On vérifie qu'il n'a pas d'autre exécution
		Assert.assertEquals(1, TestJob.getCount());
	}

	@Test
	public void testScheduleEverySecondInterval() throws InterruptedException {
		final JobDefinition jobDefinition = new JobDefinition("JB_EVERY_SECOND", TestJob.class);
		Home.getDefinitionSpace().put(jobDefinition, JobDefinition.class);

		jobManager.scheduleEverySecondInterval(jobDefinition, 1);
		//Le traitement métier n'a pas encore été effectué, on le vérifie
		Assert.assertEquals(0, TestJob.getCount());

		Thread.sleep(1500);
		Assert.assertEquals(1, TestJob.getCount());

		Thread.sleep(1000);
		Assert.assertEquals(2, TestJob.getCount());
	}

	//	@Test
	//	public void testScheduleEveryDayAtHour() throws InterruptedException {
	//		final Date date = new DateBuilder(new Date()).addSeconds(1).build();
	//		final TestJob testJob = new TestJob();
	//		jobManager.scheduleEveryDayAtHour("jb", testJob, date);
	//		//Le traitement métier n'a pas encore été effectué, on le vérifie
	//		Assert.assertEquals(0, testJob.getCount());
	//
	//		Thread.sleep(1500);
	//		Assert.assertEquals(1, testJob.getCount());
	//
	//		Thread.sleep(1500);
	//		//On vérifie qu'il n'a pas d'autre exécution
	//		Assert.assertEquals(1, testJob.getCount());
	//	}

}
