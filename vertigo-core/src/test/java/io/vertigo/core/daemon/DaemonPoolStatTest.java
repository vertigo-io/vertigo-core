/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.daemon;

import javax.inject.Inject;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;

public final class DaemonPoolStatTest extends AbstractTestCaseJU5 {

	@Inject
	private DaemonManager daemonManager;
	@Inject
	private FakePoolSaturationComponent fakePoolSaturationComponent;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.addModule(ModuleConfig.builder("myApp")
						.addComponent(FakePoolSaturationComponent.class)
						.build())
				.build();
	}

	@Override
	protected void doTearDown() {
		fakePoolSaturationComponent.releaseLatch().countDown();
	}

	@Test
	public void poolStatsReportOverdueDaemonsAndRecover() throws InterruptedException {
		Thread.sleep(4000); // first runs at t+1s: both threads blocked, the victim is due but waiting for a free thread
		//---
		final DaemonPoolStat saturated = daemonManager.getPoolStat();
		Assertions.assertEquals(2, saturated.corePoolSize());
		Assertions.assertEquals(2, saturated.activeThreads());
		Assertions.assertTrue(saturated.overdueTasks() >= 1, "the victim must be overdue: " + saturated);
		Assertions.assertTrue(saturated.maxOverdueMillis() > 1000, "the victim must wait more than 1s: " + saturated);
		//---
		fakePoolSaturationComponent.releaseLatch().countDown();
		final DaemonStat victim = awaitVictimLateExecution();
		Assertions.assertTrue(victim.getLastLatenessMillis() > 0, "the victim must have started late: " + victim);
		Assertions.assertTrue(victim.getLastEndMillis() > 0, "the victim must have completed: " + victim);
		Assertions.assertTrue(victim.isLastExecSuccess());
		//---
		final DaemonPoolStat recovered = daemonManager.getPoolStat();
		Assertions.assertTrue(recovered.maxOverdueMillis() < 1000, "the pool must be healthy after release: " + recovered);
	}

	private DaemonStat awaitVictimLateExecution() throws InterruptedException {
		final long deadline = System.currentTimeMillis() + 10_000;
		while (System.currentTimeMillis() < deadline) {
			final DaemonStat victim = findDaemonStat(FakePoolSaturationComponent.VICTIM_NAME);
			if (victim.getLastEndMillis() > 0 && victim.getLastLatenessMillis() > 0) {
				return victim;
			}
			Thread.sleep(100);
		}
		Assertions.fail("the victim daemon never completed a late execution");
		return null;
	}

	private DaemonStat findDaemonStat(final String daemonName) {
		return daemonManager.getStats().stream()
				.filter(stat -> daemonName.equals(stat.getDaemonName()))
				.findFirst()
				.orElseThrow();
	}

}
