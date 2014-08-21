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
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
	/** Pool de workers qui wrappent sur l'implémentation générique.*/
	private final ExecutorService workers;

	/**
	 * Constructeur.
	 * @param  workerCount Nombre de workers simultanés (threads)
	 */
	WorkersPool(final int workerCount) {
		Assertion.checkArgument(workerCount >= 1, "Il faut définir au moins un thread pour gérer les traitements asynchrones.");
		//---------------------------------------------------------------------
		workers = Executors.newFixedThreadPool(workerCount);
	}

	/** {@inheritDoc} */
	public void start() {
		//
	}

	/** {@inheritDoc} */
	public void stop() {
		//Shutdown in two phases (see doc)
		workers.shutdown();
		try {
			// Wait a while for existing tasks to terminate
			if (!workers.awaitTermination(60, TimeUnit.SECONDS)) {
				workers.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!workers.awaitTermination(60, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (final InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			workers.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
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
		workItem.setResult(workers.submit(new WorkItemExecutor<>(workItem)));
	}
}
