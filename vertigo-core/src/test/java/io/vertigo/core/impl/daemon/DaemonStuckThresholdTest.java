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

import org.junit.jupiter.api.Test;

public final class DaemonStuckThresholdTest {

	@Test
	public void floorAppliesToShortPeriods() {
		assertEquals(120_000, DaemonManagerImpl.stuckThresholdMillis(1, 120_000));
	}

	@Test
	public void thresholdIsFourPeriodsWhenAboveFloor() {
		assertEquals(480_000, DaemonManagerImpl.stuckThresholdMillis(120, 120_000));
	}

	@Test
	public void capEqualsFourPeriodsAtFortyMinutes() {
		assertEquals(9_600_000, DaemonManagerImpl.stuckThresholdMillis(2400, 120_000));
	}

	@Test
	public void capIsPeriodPlusTwoHoursForLargePeriods() {
		assertEquals(10_800_000, DaemonManagerImpl.stuckThresholdMillis(3600, 120_000));
	}

	@Test
	public void dailyDaemonIsDeadAfter26Hours() {
		assertEquals(93_600_000, DaemonManagerImpl.stuckThresholdMillis(86_400, 120_000));
	}

	@Test
	public void floorCanBeLowered() {
		assertEquals(4_000, DaemonManagerImpl.stuckThresholdMillis(1, 2_000));
	}

}
