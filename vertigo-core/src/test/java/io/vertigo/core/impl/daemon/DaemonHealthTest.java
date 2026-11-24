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
package io.vertigo.core.impl.daemon;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;

import org.junit.jupiter.api.Test;

import io.vertigo.core.analytics.health.HealthMeasure;
import io.vertigo.core.analytics.health.HealthStatus;
import io.vertigo.core.daemon.DaemonStat;
import io.vertigo.core.daemon.definitions.DaemonDefinition;

public final class DaemonHealthTest {

	private static final long MIN = 120_000;
	private static final long MAX = 7_200_000;
	private static final long NOW = 1_000_000_000L;
	private static final DaemonDefinition DAEMON_DEFINITION = new DaemonDefinition("DmnTest", () -> () -> {
		//nothing
	}, 1, false);

	@Test
	public void testLimitIsMinForShortPeriods() {
		assertEquals(120_000, DaemonManagerImpl.limitMillis(1, MIN, MAX));
	}

	@Test
	public void testLimitIsFourPeriodsBetweenMinAndMax() {
		assertEquals(480_000, DaemonManagerImpl.limitMillis(120, MIN, MAX));
	}

	@Test
	public void testLimitIsMaxForLongPeriods() {
		assertEquals(7_200_000, DaemonManagerImpl.limitMillis(86_400, MIN, MAX));
	}

	@Test
	public void testLimitDoesNotOverflow() {
		assertEquals(7_200_000, DaemonManagerImpl.limitMillis(604_800, MIN, MAX));
	}

	@Test
	public void testAliveGreenBelowLimit() {
		assertEquals(HealthStatus.GREEN, checkAlive(running(NOW - 119_000)).status());
	}

	@Test
	public void testAliveYellowBeyondLimit() {
		final HealthMeasure measure = checkAlive(running(NOW - 121_000));
		assertEquals(HealthStatus.YELLOW, measure.status());
		assertTrue(measure.message().contains("DmnTest"));
	}

	@Test
	public void testAliveStaysYellowForVeryLongRun() {
		assertEquals(HealthStatus.YELLOW, checkAlive(running(NOW - 5 * 3_600_000L)).status());
	}

	@Test
	public void testAliveRedWhenDead() {
		final HealthMeasure measure = checkAlive(dead(NOW - 1000));
		assertEquals(HealthStatus.RED, measure.status());
		assertTrue(measure.message().contains("DmnTest") && measure.message().contains("dead"));
	}

	@Test
	public void testDeadDaemonIsNotReportedAsLongRunning() {
		final HealthMeasure measure = checkAlive(dead(NOW - 5 * 3_600_000L));
		assertEquals(HealthStatus.RED, measure.status());
		assertFalse(measure.message().contains("running for"));
	}

	@Test
	public void testPoolGreenWhenJitter() {
		assertEquals(HealthStatus.GREEN, checkPool(pending(NOW - 30_000), false).status());
	}

	@Test
	public void testPoolYellowWhenWaitingBeyondLimit() {
		final HealthMeasure measure = checkPool(pending(NOW - 121_000), false);
		assertEquals(HealthStatus.YELLOW, measure.status());
		assertTrue(measure.message().contains("DmnTest"));
	}

	@Test
	public void testPoolRedWhenWaitingBeyondTwiceMax() {
		assertEquals(HealthStatus.RED, checkPool(pending(NOW - 14_400_001), false).status());
	}

	@Test
	public void testPoolRedWhenShutdownWhileActive() {
		final HealthMeasure measure = checkPool(pending(NOW + 1000), true);
		assertEquals(HealthStatus.RED, measure.status());
		assertTrue(measure.message().contains("shut down"));
	}

	@Test
	public void testRunningDaemonIsNotWaiting() {
		assertEquals(HealthStatus.GREEN, checkPool(running(NOW - 5 * 3_600_000L), false).status());
	}

	private static HealthMeasure checkAlive(final DaemonStatImpl daemonStat) {
		return DaemonManagerImpl.checkDaemonsAlive(List.of(daemonStat), NOW, MIN, MAX);
	}

	private static HealthMeasure checkPool(final DaemonStatImpl daemonStat, final boolean shutdownWhileActive) {
		return DaemonManagerImpl.checkDaemonPoolUtilization(List.of(daemonStat), NOW, MIN, MAX, shutdownWhileActive);
	}

	private static DaemonStatImpl running(final long startMillis) {
		return new DaemonStatImpl(DAEMON_DEFINITION, 0, 0, DaemonStat.Status.running, true, startMillis, startMillis, false, false);
	}

	private static DaemonStatImpl dead(final long startMillis) {
		return new DaemonStatImpl(DAEMON_DEFINITION, 0, 0, DaemonStat.Status.running, true, startMillis, startMillis, true, false);
	}

	private static DaemonStatImpl pending(final long dueMillis) {
		return new DaemonStatImpl(DAEMON_DEFINITION, 0, 0, DaemonStat.Status.pending, true, 0, dueMillis, false, false);
	}

}
