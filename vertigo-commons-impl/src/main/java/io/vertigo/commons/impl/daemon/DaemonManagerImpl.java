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
import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.commons.daemon.DaemonStat;
import io.vertigo.core.AppListener;
import io.vertigo.core.Home;
import io.vertigo.core.component.di.injector.Injector;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.util.ClassUtil;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

/**
 * Manager of all the daemons.
 *
 * @author TINGARGIOLA
 */
public final class DaemonManagerImpl implements DaemonManager, Activeable {
	private final DaemonExecutor daemonExecutor = new DaemonExecutor();
	private final List<DaemonInfo> daemonInfos = new ArrayList<>();
	private boolean appStarted;

	/**
	 * Construct an instance of DaemonManagerImpl.
	 */
	@Inject
	public DaemonManagerImpl() {
		Home.getApp().registerAppListener(new AppListener() {

			/** {@inheritDoc} */
			@Override
			public void onPostStart() {
				DaemonManagerImpl.this.startAllDaemons();
				appStarted = true;
			}
		});
	}

	/** {@inheritDoc} */
	@Override
	public List<DaemonStat> getStats() {
		return daemonExecutor.getStats();
	}

	/** {@inheritDoc} */
	@Override
	public void registerDaemon(final String name, final Class<? extends Daemon> daemonClass, final int periodInSeconds, final Object... constructorArgs) {
		Assertion.checkState(!appStarted, "daemon must be registerd before app has started.");
		//-----
		final DaemonInfo daemonInfo = new DaemonInfo(name, daemonClass, periodInSeconds, constructorArgs);
		daemonInfos.add(daemonInfo);
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		daemonExecutor.start();
	}

	/** {@inheritDoc} */
	@Override
	public void stop() {
		daemonExecutor.stop();
	}

	/**
	 * Démarre un démon.
	 * Celui-ci aura été préalablement enregistré.
	 * Il sera lancé puis réexécuté périodiquement.
	 * L'instance du démon est créée par injection de dépendances.
	 *
	 * @param daemonInfo Le démon à lancer.
	 */
	private void startDaemon(final DaemonInfo daemonInfo) {
		Assertion.checkNotNull(daemonInfo);
		// -----
		final Daemon daemon = createDaemon(daemonInfo);
		daemonExecutor.scheduleDaemon(daemonInfo, daemon);
	}

	/**
	 * @param daemonInfo
	 * @return Daemon
	 */
	private static Daemon createDaemon(final DaemonInfo daemonInfo) {
		final Constructor<? extends Daemon> constructor = findConstructor(daemonInfo.getDaemonClass(), daemonInfo.getConstructorArgs());
		final Daemon daemon = ClassUtil.newInstance(constructor, daemonInfo.getConstructorArgs());
		Injector.injectMembers(daemon, Home.getApp().getComponentSpace());
		return daemon;
	}

	/**
	 * Démarre l'ensemble des démons préalablement enregistré dans le spaceDefinition.
	 */
	void startAllDaemons() {
		for (final DaemonInfo daemonInfo : daemonInfos) {
			startDaemon(daemonInfo);
		}
	}

	private static <T extends Daemon> Constructor<T> findConstructor(final Class<T> clazz, final Object[] args) {
		Assertion.checkNotNull(clazz);
		//-----
		final Constructor<T>[] constructors = (Constructor<T>[]) clazz.getConstructors();
		Assertion.checkNotNull(constructors, "Aucun constructeur public identifiable");
		Assertion.checkArgument(constructors.length == 1, "Un seul constructeur public doit être déclaré sur {0}", clazz.getName());
		Assertion.checkArgument(constructors[0].getParameterTypes().length == args.length, "Les paramètres passés ne sont pas compatible avec ceux du constructeur sur {0}.", clazz.getName());
		Assertion.checkArgument(constructors[0].getAnnotation(Inject.class) == null, "Le constructeur des daemons ne support pas @Inject, utiliser @Inject sur les attributs d'instance sur {0}.", clazz.getName());
		//-----
		//On a un et un seul constructeur.
		return constructors[0];
	}
}
