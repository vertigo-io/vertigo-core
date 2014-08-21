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
import io.vertigo.dynamo.work.WorkManager;
import io.vertigo.kernel.lang.Assertion;

import java.lang.reflect.Field;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

/**
 * Exécution d'un work.
 * 
 * @author pchretien, npiedeloup
 * @param <WR> Type du résultat
 * @param <W> Type du work
 */
final class WorkItemExecutor<WR, W> implements Callable<WR> {

	/**
	 * Pour vider les threadLocal entre deux utilisations du Thread dans le pool,
	 * on garde un accès débridé au field threadLocals de Thread.
	 * On fait le choix de ne pas vider inheritedThreadLocal qui est moins utilisé.
	 */
	private static final Field threadLocalsField;
	static {
		try {
			threadLocalsField = Thread.class.getDeclaredField("threadLocals");
		} catch (final SecurityException | NoSuchFieldException e) {
			throw new RuntimeException(e);
		}
		threadLocalsField.setAccessible(true);
	}

	private final WorkItem<WR, W> workItem;
	private final Logger logger = Logger.getLogger(WorkManager.class); //même logger que le WorkListenerImpl

	/**
	 * Constructeur.
	 * @param workItem WorkItem à traiter
	 */
	WorkItemExecutor(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		//-----------------------------------------------------------------
		this.workItem = workItem;
	}

	private static <WR, W> WR executeNow(final WorkItem<WR, W> workItem) {
		Assertion.checkNotNull(workItem);
		// ---------------------------------------------------------------------
		return workItem.getWorkEngineProvider().provide().process(workItem.getWork());
	}

	/** {@inheritDoc} */
	public WR call() {
		final WR result;
		try {
			if (workItem.getWorkResultHandler().isDefined()) {
				workItem.getWorkResultHandler().get().onStart();
			}
			//---
			result = executeNow(workItem);
			//---
			if (workItem.getWorkResultHandler().isDefined()) {
				workItem.getWorkResultHandler().get().onSuccess(result);
			}
			return result;
		} catch (final Throwable t) {
			if (workItem.getWorkResultHandler().isDefined()) {
				workItem.getWorkResultHandler().get().onFailure(t);
			}
			logError(t);
			if (t instanceof RuntimeException) {
				throw (RuntimeException) t;
			}
			throw new RuntimeException(t);
		} finally {
			try {
				//Vide le threadLocal
				cleanThreadLocals();
			} catch (final RuntimeException e) {
				//Ce n'est pas une cause de rejet du Work, on ne fait que logger
				logError(e);
			}
		}
	}

	private void logError(final Throwable e) {
		logger.error("Erreur de la tache de type : " + workItem.getWorkEngineProvider().getName(), e);
	}

	/**
	 * Vide le threadLocal du thread avant de le remettre dans le pool.
	 * Ceci protège contre les WorkEngine utilsant un ThreadLocal sans le vider. 
	 * Ces workEngine peuvent poser des problémes de fuites mémoires (Ex: le FastDateParser de Talend)
	 * Voir aussi: http://weblogs.java.net/blog/jjviana/archive/2010/06/10/threadlocal-thread-pool-bad-idea-or-dealing-apparent-glassfish-memor
	 */
	private static void cleanThreadLocals() {
		try {
			threadLocalsField.set(Thread.currentThread(), null);
		} catch (final IllegalArgumentException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}
}
