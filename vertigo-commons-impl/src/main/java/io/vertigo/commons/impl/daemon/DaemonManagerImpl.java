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
import io.vertigo.core.Home;
import io.vertigo.core.di.injector.Injector;
import io.vertigo.lang.Assertion;

import javax.inject.Inject;

/**
 * Manager de gestion du deamon.
 *
 * @author TINGARGIOLA
 */
public final class DaemonManagerImpl implements DaemonManager {

	private final DaemonPlugin daemonPlugin;

	/**
	 * Construct an instance of DeamonManagerImpl.
	 *
	 * @param daemonPlugin Plugin de gestion du deamon.
	 */
	@Inject
	public DaemonManagerImpl(final DaemonPlugin daemonPlugin) {
		Assertion.checkNotNull(daemonPlugin);
		// -----
		this.daemonPlugin = daemonPlugin;
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
		daemonPlugin.scheduleDaemon(daemonDefinition.getName(), daemon, daemonDefinition.getPeriodInSeconds());
	}

	/**
	 * @param daemonDefinition
	 * @return Dameon
	 */
	private static Daemon createDaemon(final DaemonDefinition daemonDefinition) {
		return Injector.newInstance(daemonDefinition.getDaemonClass(), Home.getComponentSpace());
	}

	/** {@inheritDoc} */
	@Override
	public void startAllDaemons() {
		for (final DaemonDefinition daemonDefinition : Home.getDefinitionSpace().getAll(DaemonDefinition.class)) {
			startDaemon(daemonDefinition);
		}
	}
}
