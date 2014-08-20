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
package io.vertigo.dynamo.impl.work.worker.local;

import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.impl.work.worker.Worker;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Gestion asynchrone des travaux.
 * 
 * Cette classe possède une queue (BlockingQueue).
 * On y pousse des works à exécuter définis par WorkItem
 * D'un coté un Dispatcher, dépile les travaux et les envoie à un pool de workers pour être exécuter.
 * 
 * @author pchretien, npiedeloup
 */
final class WorkersPool implements Activeable {
	/** Pile des works à faire. */
	private final BlockingQueue<WorkItem<?, ?>> worksQueue;

	/** Pool de workers qui wrappent sur l'implémentation générique.*/
	private final ExecutorService workers;

	/** Worker générique threadsafe.*/
	private final Worker worker;
	private final Thread workDispatcherThread;

	/**
	 * Constructeur.
	 * @param  workerCount Nombre de workers simultanés (threads)
	 */
	WorkersPool(final Worker worker, final int workerCount) {
		Assertion.checkNotNull(worker);
		Assertion.checkArgument(workerCount >= 1, "Il faut définir au moins un thread pour gérer les traitements asynchrones.");
		//---------------------------------------------------------------------
		this.worker = worker;
		worksQueue = new LinkedBlockingQueue<>();
		workers = Executors.newFixedThreadPool(workerCount);
		workDispatcherThread = new WorkDispatcherThread(this);
	}

	/** {@inheritDoc} */
	public void start() {
		workDispatcherThread.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		workDispatcherThread.interrupt();
		try {
			workDispatcherThread.join();
		} catch (final InterruptedException e) {
			//On ne fait rien
		}
	}

	/**
	 * Work devant être exécuté
	 * WorkItem contient à la fois le Work et le callback.  
	 * @param workItem WorkItem
	 */
	<WR, W> void putWorkItem(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//-------------------------------------------------------------------
		try {
			worksQueue.put(workItem);
		} catch (final InterruptedException e) {
			//dans le cas d'une interruption on informe le demandeur 
			throw new RuntimeException("putWorkItem", e);
		}
	}

	private void executeWorkItem(final WorkItem<?, ?> workItem) {
		//On donne au pool d'éxécution une tache à accomplir.
		workers.execute(new WorkItemExecutor<>(workItem));
	}

	/**
	 * Dispatch des Work.
	 * Cette classe lit la queue des 'todo' et dispatch aux workers les travaux à réaliser 
	 */
	private static final class WorkDispatcherThread extends Thread {
		private final WorkersPool workersPool;

		WorkDispatcherThread(final WorkersPool workersPool) {
			super("worksDispatcherThread");
			Assertion.checkNotNull(workersPool);
			//-----------------------------------------------------------------
			this.workersPool = workersPool;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			/* Dépilement et affectation des taches aux workers du pool. */
			try {
				while (!isInterrupted()) {
					pollWorkItem();
				}
			} catch (final InterruptedException e) {
				//Interruption du polling, on ne revoie pas d'exception
			}
		}

		/**
		 * Récupération et exécution du travail à effectuer.
		 * On laisse remonter l'exception 
		 */
		private void pollWorkItem() throws InterruptedException {
			//take attend qu'un élément soit disponible toutes les secondes.
			//Poll attend (1s) qu'un élément soit disponible et sinon renvoit null 
			final WorkItem<?, ?> workItem = workersPool.worksQueue.poll(1, TimeUnit.SECONDS);
			if (workItem != null) {
				workersPool.executeWorkItem(workItem);
			}
		}
	}
}
