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
package io.vertigo.dynamo.impl.work;

import io.vertigo.core.lang.Activeable;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;
import io.vertigo.dynamo.impl.work.listener.WorkListener;
import io.vertigo.dynamo.impl.work.listener.WorkListenerImpl;
import io.vertigo.dynamo.impl.work.worker.Coordinator;
import io.vertigo.dynamo.impl.work.worker.distributed.DistributedCoordinator;
import io.vertigo.dynamo.impl.work.worker.local.LocalCoordinator;
import io.vertigo.dynamo.work.WorkEngine;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.dynamo.work.WorkProcessor;
import io.vertigo.dynamo.work.WorkResultHandler;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Implémentation de workManager.
 * 
 * @author pchretien, npiedeloup
 */
public final class WorkManagerImpl implements WorkManager, Activeable {
	private final WorkListener workListener;
	//There is always ONE LocalWorker, but distributedWorker is optionnal
	private final LocalCoordinator localCoordinator;
	private final Option<DistributedCoordinator> distributedCoordinator;

	/**
	 * Constructeur.
	 * @param analyticsManager Manager de la performance applicative
	 */
	@Inject
	public WorkManagerImpl(final @Named("workerCount") int workerCount, final Option<MasterPlugin> masterPlugin) {
		Assertion.checkNotNull(masterPlugin);
		//-----------------------------------------------------------------
		workListener = new WorkListenerImpl(/*analyticsManager*/);
		localCoordinator = new LocalCoordinator(workerCount);
		distributedCoordinator = masterPlugin.isDefined() ? Option.some(new DistributedCoordinator(masterPlugin.get())) : Option.<DistributedCoordinator> none();
	}

	/** {@inheritDoc} */
	public void start() {
		//coordinator n'étant pas un plugin
		//il faut le démarrer et l'arréter explicitement.
	}

	/** {@inheritDoc} */
	public void stop() {
		localCoordinator.close();
	}

	private static String createWorkId() {
		return UUID.randomUUID().toString();
	}



	/** {@inheritDoc} */
	public <WR, W> WorkProcessor<WR, W> createProcessor(final WorkEngineProvider<WR, W> workEngineProvider) {
		return new WorkProcessorImpl<>(this, workEngineProvider);
	}

	/** {@inheritDoc} */
	public <WR, W> WR process(final W work, final WorkEngineProvider<WR, W> workEngineProvider) {
		Assertion.checkNotNull(work);
		Assertion.checkNotNull(workEngineProvider);
		//---------------------------------------------------------------------
		final WorkItem<WR, W> workItem = new WorkItem<>(createWorkId(), work, workEngineProvider);
		final Future<WR> result = submit(workItem, Option.<WorkResultHandler<WR>> none());
		try {
			return result.get();
		} catch (final ExecutionException e) {
			if (e.getCause() instanceof RuntimeException) {
				throw (RuntimeException) e.getCause();
			}
			throw new RuntimeException(e.getCause());
		} catch (final InterruptedException e) {
			throw new RuntimeException(e);
		}
	}

	public <WR, W> void schedule(final W work, final WorkEngineProvider<WR, W> workEngineProvider, final WorkResultHandler<WR> workResultHandler) {
		Assertion.checkNotNull(work);
		Assertion.checkNotNull(workEngineProvider);
		Assertion.checkNotNull(workResultHandler);
		//---------------------------------------------------------------------
		final WorkItem<WR, W> workItem = new WorkItem<>(createWorkId(), work, workEngineProvider);
		submit(workItem, Option.some(workResultHandler));
	}

	public <WR, W> void schedule(final Callable<WR> callable, final WorkResultHandler<WR> workResultHandler) {
		Assertion.checkNotNull(callable);
		Assertion.checkNotNull(workResultHandler);
		//---------------------------------------------------------------------
		final WorkEngineProvider<WR, W> workEngineProvider = new WorkEngineProvider<>(new WorkEngine<WR, W>() {
			public WR process(final W dummy) {
				try {
					return callable.call();
				} catch (final Exception e) {
					throw new RuntimeException(e);
				}
			}
		});

		final WorkItem<WR, W> workItem = new WorkItem<>(createWorkId(), null, workEngineProvider);
		submit(workItem, Option.some(workResultHandler));
	}

	//-------------------------------------------------------------------------
	//-------------------------------------------------------------------------
	private <WR, W> Future<WR> submit(final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		final Coordinator coordinator = resolveCoordinator(workItem);
		//---
		workListener.onStart(workItem.getWorkType());
		boolean executed = false;
		final long start = System.currentTimeMillis();
		try {
			final Future<WR> future = coordinator.submit(workItem, workResultHandler);
			executed = true;
			return future;
		} finally {
			workListener.onFinish(workItem.getWorkType(), System.currentTimeMillis() - start, executed);
		}
	}

	private <WR, W> Coordinator resolveCoordinator(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//----------------------------------------------------------------------
		/*
		 * On recherche un Worker capable d'effectuer le travail demandé.
		 * 1- On recherche parmi les works externes
		 * 2- Si le travail n'est pas déclaré comme étant distribué on l'exécute localement
		 */
		if (distributedCoordinator.isDefined() && distributedCoordinator.get().accept(workItem)) {
			return distributedCoordinator.get();
		}
		return localCoordinator;
	}
}
