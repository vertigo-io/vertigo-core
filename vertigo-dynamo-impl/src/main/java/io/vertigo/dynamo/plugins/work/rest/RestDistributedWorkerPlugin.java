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
package io.vertigo.dynamo.plugins.work.rest;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.dynamo.impl.work.DistributedWorkerPlugin;
import io.vertigo.dynamo.impl.work.WorkItem;
import io.vertigo.dynamo.work.WorkEngineProvider;
import io.vertigo.dynamo.work.WorkResultHandler;
import io.vertigo.kernel.lang.Activeable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Exécution synchrone et distante des Works avec un transfert par WS REST.
 * 
 * @author npiedeloup, pchretien
 * @version $Id: RestDistributedWorkerPlugin.java,v 1.13 2014/02/27 10:31:19 pchretien Exp $
 */
public final class RestDistributedWorkerPlugin implements DistributedWorkerPlugin, Activeable {
	//	private final long timeoutSeconds;
	private final Set<String> workTypes;
	private final RestQueue multipleWorkQueues;
	private final WorkQueueRestServer workQueueRestServer;

	/**
	 * Constructeur.
	 * @param timeoutSeconds Timeout des travaux en attente de traitement
	 * @param workTypesAsString Liste des types de work distribués (séparateur ;)
	 * @param codecManager Manager d'encodage/decodage
	 */
	@Inject
	public RestDistributedWorkerPlugin(@Named("timeoutSeconds") final long timeoutSeconds, @Named("workTypes") final String workTypesAsString, final CodecManager codecManager) {
		Assertion.checkArgument(timeoutSeconds < 10000, "Le timeout s'exprime en seconde.");
		Assertion.checkArgNotEmpty(workTypesAsString);
		//---------------------------------------------------------------------
		//	this.timeoutSeconds = timeoutSeconds;
		final String[] workTypesArray = workTypesAsString.split(";");//
		workTypes = new HashSet<>(Arrays.asList(workTypesArray));
		multipleWorkQueues = new RestQueue();
		workQueueRestServer = new WorkQueueRestServer(multipleWorkQueues, 20 * 1000, codecManager);
	}

	/**
	 * @return Serveur REST de la WorkQueue. (appelée par l'implé Jersey)
	 */
	public WorkQueueRestServer getWorkQueueRestServer() {
		return workQueueRestServer;
	}

	/** {@inheritDoc} */
	public void start() {
		workQueueRestServer.start();
	}

	/** {@inheritDoc} */
	public void stop() {
		workQueueRestServer.stop();
	}

	/** {@inheritDoc} */
	public <WR, W> boolean canProcess(final WorkEngineProvider<WR, W> workEngineProvider) {
		return workTypes.contains(obtainWorkType(workEngineProvider));
	}

	/** {@inheritDoc} */
	public <WR, W> Future<WR> submit(final WorkItem<WR, W> workItem, final Option<WorkResultHandler<WR>> workResultHandler) {
		final String workType = obtainWorkType(workItem);
		return multipleWorkQueues.submit(workType, workItem, workResultHandler);
	}

	//	/** {@inheritDoc} */
	//	public <WR, W> void process(final WorkItem<WR, W> workitem2) {
	//		Assertion.checkNotNull(workitem2);
	//		//---------------------------------------------------------------------
	//		final WorkResultHandlerSync<WR> workResultHandler = new WorkResultHandlerSync<>();
	//		final WorkItem<WR, W> workItem = new WorkItem<>(workitem2.getWork(), workitem2.getWorkEngineProvider(), workResultHandler);
	//
	//		final String workType = obtainWorkType(workitem2);
	//		multipleWorkQueues.putWorkItem(workType, workItem);
	//		workResultHandler.waitResult(timeoutSeconds); //on attend le résultat
	//		//---
	//		workitem2.setResult(workResultHandler.getResultOrThrowError());//retourne le résultat ou lance l'erreur
	//	}
	//
	//	/** {@inheritDoc} */
	//	public <WR, W> void schedule(final WorkItem<WR, W> workItem) {
	//		Assertion.checkNotNull(workItem);
	//		//---------------------------------------------------------------------
	//		final String workType = obtainWorkType(workItem);
	//		multipleWorkQueues.putWorkItem(workType, workItem);
	//	}

	private <WR, W> String obtainWorkType(final WorkEngineProvider workEngineProvider) {
		return workEngineProvider.getName();
	}

	private <WR, W> String obtainWorkType(final WorkItem<WR, W> workitem) {
		return obtainWorkType(workitem.getWorkEngineProvider());
	}

	//	private static final class WorkResultHandlerSync<WR> implements WorkResultHandler<WR> {
	//		private boolean started = false;
	//		private boolean finished = false;
	//		private WR result;
	//		private Throwable error;
	//
	//		/** {@inheritDoc} */
	//		public void onStart() {
	//			started = true;
	//		}
	//
	//		/**
	//		 * Attend la fin de ce Work.
	//		 * @param waitTimeoutSeconds temps maximum d'attente en seconde
	//		 */
	//		public synchronized void waitResult(final long waitTimeoutSeconds) {
	//			if (!finished) {
	//				try {
	//					wait(waitTimeoutSeconds * 1000); //attend un notify
	//				} catch (final InterruptedException e) {
	//					throw new RuntimeException("Arret demandé : on stop le travail en cours");
	//				}
	//				if (!finished) {
	//					if (!started) {
	//						error = new RuntimeException("Timeout : le traitement n'a pas été pris en charge en " + waitTimeoutSeconds + "s");
	//					} else {
	//						error = new RuntimeException("Timeout : le traitement ne s'est pas terminé en " + waitTimeoutSeconds + "s");
	//					}
	//					//TODO : si timeout retirer de la file, ou désactiver le handler
	//				}
	//			}
	//		}
	//
	//		/** {@inheritDoc} */
	//		public synchronized void onDone(final boolean succeeded, final WR newResult, final Throwable newError) {
	//			if (succeeded) {
	//				this.result = newResult;
	//				error = null; //si on a eut une erreur avant, ou un timeout : on reset l'erreur
	//			} else {
	//				this.error = newError;
	//			}
	//			finished = true;
	//			notifyAll(); //débloque le wait
	//		}
	//
	//		/**
	//		 * Retourne le résultat ou lance l'erreur reçu le cas echant.
	//		 * @return résultat
	//		 */
	//		public WR getResultOrThrowError() {
	//			if (error != null) {
	//				//si il ya une erreur 
	//				if (error instanceof Error) {
	//					throw Error.class.cast(error);
	//				}
	//				if (error instanceof RuntimeException) {
	//					throw RuntimeException.class.cast(error);
	//				}
	//				throw new RuntimeException(error);
	//			}
	//			return result;
	//		}
	//	}

}
