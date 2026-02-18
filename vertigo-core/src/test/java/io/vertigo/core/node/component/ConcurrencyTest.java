/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.component;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;

import io.vertigo.core.AbstractTestCaseJU5;
import io.vertigo.core.analytics.AnalyticsManager;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.data.BioManager;
import io.vertigo.core.node.component.data.BioManagerImpl;
import io.vertigo.core.node.component.data.MathManager;
import io.vertigo.core.node.component.data.MathManagerImpl;
import io.vertigo.core.node.component.data.SimpleMathPlugin;
import io.vertigo.core.node.config.BootConfig;
import io.vertigo.core.node.config.ModuleConfig;
import io.vertigo.core.node.config.NodeConfig;
import io.vertigo.core.param.Param;
import jakarta.inject.Inject;

/**
 * Tests de concurrence pour valider le comportement thread-safe
 * de ComponentSpace, DefinitionSpace et AnalyticsManager.
 *
 * @author pchretien
 */
public final class ConcurrencyTest extends AbstractTestCaseJU5 {
	private static final int THREAD_COUNT = 20;
	private static final int ITERATIONS_PER_THREAD = 100;

	@Inject
	private BioManager bioManager;
	@Inject
	private MathManager mathManager;
	@Inject
	private AnalyticsManager analyticsManager;

	@Override
	protected NodeConfig buildNodeConfig() {
		return NodeConfig.builder()
				.withBoot(BootConfig.builder()
						.withLocales("fr_FR")
						.build())
				.addModule(ModuleConfig.builder("bio")
						.addComponent(BioManager.class, BioManagerImpl.class)
						.addComponent(MathManager.class, MathManagerImpl.class,
								Param.of("start", "100"))
						.addPlugin(SimpleMathPlugin.class,
								Param.of("factor", "20"))
						.build())
				.build();
	}

	@Test
	public void testConcurrentComponentSpaceResolve() throws Exception {
		final ComponentSpace componentSpace = Node.getNode().getComponentSpace();
		final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		final CountDownLatch startLatch = new CountDownLatch(1);
		final List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
		final List<Future<?>> futures = new ArrayList<>();

		for (int i = 0; i < THREAD_COUNT; i++) {
			futures.add(executor.submit(() -> {
				try {
					startLatch.await();
					for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
						final BioManager resolved = componentSpace.resolve(BioManager.class);
						assertNotNull(resolved);
						final MathManager mathResolved = componentSpace.resolve(MathManager.class);
						assertNotNull(mathResolved);
					}
				} catch (final Throwable t) {
					errors.add(t);
				}
			}));
		}

		// Demarre tous les threads simultanement
		startLatch.countDown();

		for (final Future<?> future : futures) {
			future.get(30, TimeUnit.SECONDS);
		}
		executor.shutdown();
		assertTrue(errors.isEmpty(), "Errors during concurrent resolve: " + errors);
	}

	@Test
	public void testConcurrentComponentExecution() throws Exception {
		final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		final CountDownLatch startLatch = new CountDownLatch(1);
		final List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
		final List<Future<?>> futures = new ArrayList<>();

		for (int i = 0; i < THREAD_COUNT; i++) {
			futures.add(executor.submit(() -> {
				try {
					startLatch.await();
					for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
						// BioManager.add utilise MathManager qui utilise MathPlugin
						final int result = bioManager.add(1, 2, 3);
						assertEquals(366, result);
					}
				} catch (final Throwable t) {
					errors.add(t);
				}
			}));
		}

		startLatch.countDown();

		for (final Future<?> future : futures) {
			future.get(30, TimeUnit.SECONDS);
		}
		executor.shutdown();
		assertTrue(errors.isEmpty(), "Errors during concurrent execution: " + errors);
	}

	@Test
	public void testConcurrentAnalyticsTrace() throws Exception {
		final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		final CountDownLatch startLatch = new CountDownLatch(1);
		final List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
		final List<Future<?>> futures = new ArrayList<>();

		for (int i = 0; i < THREAD_COUNT; i++) {
			final int threadId = i;
			futures.add(executor.submit(() -> {
				try {
					startLatch.await();
					for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
						final int iteration = j;
						analyticsManager.trace("test", "concurrentOp-" + threadId, tracer -> {
							tracer.incMeasure("count", 1);
							// Executer une operation reelle dans le tracer
							final int result = mathManager.add(threadId, iteration);
							tracer.setMeasure("result", result);
						});
					}
				} catch (final Throwable t) {
					errors.add(t);
				}
			}));
		}

		startLatch.countDown();

		for (final Future<?> future : futures) {
			future.get(30, TimeUnit.SECONDS);
		}
		executor.shutdown();
		assertTrue(errors.isEmpty(), "Errors during concurrent analytics trace: " + errors);
	}

	@Test
	public void testConcurrentAnalyticsTraceWithReturn() throws Exception {
		final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		final CountDownLatch startLatch = new CountDownLatch(1);
		final List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
		final List<Future<?>> futures = new ArrayList<>();

		for (int i = 0; i < THREAD_COUNT; i++) {
			final int threadId = i;
			futures.add(executor.submit(() -> {
				try {
					startLatch.await();
					for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
						final int iteration = j;
						final int result = analyticsManager.traceWithReturn("test", "concurrentCalc-" + threadId, tracer -> {
							return mathManager.add(threadId, iteration);
						});
						// start(100) + factor(20) + threadId + j
						assertEquals(120 + threadId + iteration, result);
					}
				} catch (final Throwable t) {
					errors.add(t);
				}
			}));
		}

		startLatch.countDown();

		for (final Future<?> future : futures) {
			future.get(30, TimeUnit.SECONDS);
		}
		executor.shutdown();
		assertTrue(errors.isEmpty(), "Errors during concurrent traceWithReturn: " + errors);
	}

	@Test
	public void testConcurrentMixedOperations() throws Exception {
		final ComponentSpace componentSpace = Node.getNode().getComponentSpace();
		final ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
		final CountDownLatch startLatch = new CountDownLatch(1);
		final List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());
		final List<Future<?>> futures = new ArrayList<>();

		for (int i = 0; i < THREAD_COUNT; i++) {
			final int threadId = i;
			futures.add(executor.submit(() -> {
				try {
					startLatch.await();
					for (int j = 0; j < ITERATIONS_PER_THREAD; j++) {
						// Mix de resolve, execution et analytics
						final BioManager bio = componentSpace.resolve(BioManager.class);
						assertNotNull(bio);
						assertTrue(componentSpace.contains("bioManager"));

						analyticsManager.trace("test", "mixed-" + threadId, tracer -> {
							final int result = bio.add(1, 2);
							tracer.setMeasure("result", result);
						});
					}
				} catch (final Throwable t) {
					errors.add(t);
				}
			}));
		}

		startLatch.countDown();

		for (final Future<?> future : futures) {
			future.get(30, TimeUnit.SECONDS);
		}
		executor.shutdown();
		assertTrue(errors.isEmpty(), "Errors during concurrent mixed operations: " + errors);
	}
}
