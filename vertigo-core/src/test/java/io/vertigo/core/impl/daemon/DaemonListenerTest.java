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

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.core.daemon.definitions.DaemonDefinition;

public final class DaemonListenerTest {

	private DaemonListener daemonListener;

	@BeforeEach
	public void setUp() {
		daemonListener = new DaemonListener(new DaemonDefinition("DmnTest", () -> () -> {
			//nothing
		}, 1, false), false);
	}

	@Test
	public void testOneFailureSetsInFailure() {
		fail();
		assertTrue(daemonListener.getStat().isInFailure());
	}

	@Test
	public void testOneSuccessIsNotEnoughToRecover() {
		fail();
		succeed();
		assertTrue(daemonListener.getStat().isInFailure());
	}

	@Test
	public void testTwoConsecutiveSuccessesRecover() {
		fail();
		succeed();
		succeed();
		assertFalse(daemonListener.getStat().isInFailure());
	}

	@Test
	public void testFailureBetweenSuccessesRestartsHysteresis() {
		fail();
		succeed();
		fail();
		succeed();
		assertTrue(daemonListener.getStat().isInFailure());
	}

	@Test
	public void testNeverStartedDaemonIsOverdueFromRegistration() {
		final long nowMillis = System.currentTimeMillis();
		assertTrue(Math.abs(daemonListener.getStat().getDueMillis() - (nowMillis + 1000)) <= 50, "a never started daemon is due one period after its registration");
	}

	@Test
	public void testErrorMarksTheDaemonDead() {
		daemonListener.onStart();
		daemonListener.onDead(new AssertionError("test error"));
		assertTrue(daemonListener.getStat().isDead());
	}

	@Test
	public void testExecutionsAreRecordedInWindow() {
		succeed();
		fail();
		final DaemonListener.Window window = daemonListener.takeWindow();
		assertEquals(2, window.executions);
		assertEquals(1, window.failures);
		// -----
		// the read starts a new window
		assertEquals(0, daemonListener.takeWindow().executions);
	}

	@Test
	public void testWindowsAreSummed() {
		final DaemonListener.Window window = new DaemonListener.Window();
		window.add(100, 0, true);
		final DaemonListener.Window other = new DaemonListener.Window();
		other.add(300, 200, false);
		window.addAll(other);
		// -----
		assertEquals(2, window.executions);
		assertEquals(1, window.failures);
		assertEquals(400, window.totalDurationMillis);
		assertEquals(200, window.totalLatenessMillis);
	}

	private void fail() {
		daemonListener.onStart();
		daemonListener.onFailure(new RuntimeException("test failure"));
	}

	private void succeed() {
		daemonListener.onStart();
		daemonListener.onSuccess();
	}

}
