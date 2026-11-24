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

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import io.vertigo.core.node.component.Component;

public final class FakePoolSaturationComponent implements Component {

	static final String BLOCKER_1_NAME = "DmnPoolBlocker1";
	static final String BLOCKER_2_NAME = "DmnPoolBlocker2";
	static final String VICTIM_NAME = "DmnPoolVictim";

	private final CountDownLatch release = new CountDownLatch(1);

	CountDownLatch releaseLatch() {
		return release;
	}

	@DaemonScheduled(name = BLOCKER_1_NAME, periodInSeconds = 1)
	public void blocker1() throws InterruptedException {
		release.await(30, TimeUnit.SECONDS);
	}

	@DaemonScheduled(name = BLOCKER_2_NAME, periodInSeconds = 1)
	public void blocker2() throws InterruptedException {
		release.await(30, TimeUnit.SECONDS);
	}

	@DaemonScheduled(name = VICTIM_NAME, periodInSeconds = 1)
	public void victim() {
		//fast no-op
	}

}
