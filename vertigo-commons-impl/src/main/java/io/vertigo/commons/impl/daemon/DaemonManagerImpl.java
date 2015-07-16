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
package io.vertigo.commons.impl.daemon;

import io.vertigo.commons.daemon.Daemon;
import io.vertigo.commons.daemon.DaemonDefinition;
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.commons.daemon.DaemonStat;
import io.vertigo.core.AppListener;
import io.vertigo.core.Home;
import io.vertigo.core.di.injector.Injector;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;

import java.util.List;

import javax.inject.Inject;

/**
 * Manager de gestion du deamon.
 *
 * @author TINGARGIOLA
 */
public final class DaemonManagerImpl implements DaemonManager, Activeable {
	private final DaemonExecutor daemonExecutor;

	/**
	 * Construct an instance of DeamonManagerImpl.
	 */
	@Inject
	public DaemonManagerImpl() {
		this.daemonExecutor = new DaemonExecutor();

		Home.getApp().registerAppListener(new AppListener() {

			@Override
			public void onPostStart() {
				startAllDaemons();
			}
		});
	}

	@Override
	public List<DaemonStat> getSats() {
		return daemonExecutor.getSats();
	}

	/**
	 * Démarre un démon.
	 * Celui-ci aura été préalablement enregistré.
	 * Il sera lancé puis réexécuté périodiquement.
	 * L'instance du démon est créée par injection de dépendances.
	 *
	 * @param daemonDefinition Le démon à lancer.
	 */
	private void startDaemon(final DaemonDefinition daemonDefinition) {
		Assertion.checkNotNull(daemonDefinition);
		// -----
		final Daemon daemon = createDaemon(daemonDefinition);
		daemonExecutor.scheduleDaemon(daemonDefinition, daemon);
	}

	/**
	 * @param daemonDefinition
	 * @return Dameon
	 */
	private static Daemon createDaemon(final DaemonDefinition daemonDefinition) {
		return Injector.newInstance(daemonDefinition.getDaemonClass(), Home.getComponentSpace());
	}

	/**
	 * Démarre l'ensemble des démons préalablement enregistré dans le spaceDefinition.
	 */
	private void startAllDaemons() {
		for (final DaemonDefinition daemonDefinition : Home.getDefinitionSpace().getAll(DaemonDefinition.class)) {
			startDaemon(daemonDefinition);
		}
	}

	@Override
	public void start() {
		daemonExecutor.start();
	}

	@Override
	public void stop() {
		daemonExecutor.stop();
	}
}
