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
package io.vertigo.dynamo.work;

import io.vertigo.lang.Manager;

import java.util.concurrent.Callable;

/**
 * Gestion synchrone, asynchrone des taches à effectuer.
 * Chaque réalisation est effectuée par un {@link WorkEngine}.
 *
 * Les exécutions peuvent être
 *  - locales ou distribuées (par configuration)
 *  - synchrones ou asynchrones (selon la méthode appelée)
 *
 * Toutes les exécutions distribuées sont techniquement réalisées de façon asynchrones.
 * Dans le cas des appels synchrones de méthodes distribuées, un mécanisme resynchronise le résultat
 *
 * @author pchretien
 */
public interface WorkManager extends Manager {

	/**
	 * Create a new WorkProcessor.
	 * It ca be use for composing WorkEngine.
	 * @param <WR> WorkEngine result's type
	 * @param <W> Work's type : input of workEngine
	 * @param workEngineProvider WorkEngine provider
	 * @return a new WorkProcessor
	 */
	<WR, W> WorkProcessor<WR, W> createProcessor(final WorkEngineProvider<WR, W> workEngineProvider);

	/**
	 * Exécution d'un travail de façon synchrone.
	 * @param <W> Type de Work (Travail)
	 * @param <WR> Produit d'un work à l'issu de son exécution
	 * @param work Travail à exécuter
	 * @param workEngineProvider WorkEngine provider
	 * @return result
	 */
	<WR, W> WR process(final W work, final WorkEngineProvider<WR, W> workEngineProvider);

	/**
	 * Lancement asynchrone d'un travail 'dès que possible'.
	 * @param <W> Type de Work (Travail)
	 * @param <WR> Produit d'un work à l'issu de son exécution
	 * @param work Travail à exécuter
	 * @param workEngineProvider WorkEngine provider
	 * @param workResultHandler Handler permettant un callback après exécution
	 */
	<WR, W> void schedule(final W work, WorkEngineProvider<WR, W> workEngineProvider, WorkResultHandler<WR> workResultHandler);

	/**
	 * Lancement asynchrone d'un travail 'dès que possible'.
	 * @param <WR> Produit d'un work à l'issu de son exécution
	 * @param callable Travail à exécuter
	 * @param  workResultHandler Handler permettant un callback après exécution
	 */
	<WR> void schedule(final Callable<WR> callable, final WorkResultHandler<WR> workResultHandler);
}
