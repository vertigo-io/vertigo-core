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
package io.vertigo.engines.command;

import io.vertigo.app.AppListener;
import io.vertigo.app.Home;
import io.vertigo.app.config.AppConfig;
import io.vertigo.core.command.VCommand;
import io.vertigo.core.command.VCommandExecutor;
import io.vertigo.core.command.VResponse;
import io.vertigo.core.engines.VCommandEngine;
import io.vertigo.core.spaces.definiton.DefinitionSpace;
import io.vertigo.engines.command.samples.VPingCommandExecutor;
import io.vertigo.engines.command.samples.VSystemCommandExecutor;
import io.vertigo.engines.command.tcp.VServer;
import io.vertigo.lang.Activeable;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Component;
import io.vertigo.util.MapBuilder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author pchretien
 */
public final class TcpVCommandEngine implements VCommandEngine, Component, Activeable {
	private final int port;
	private final Map<String, VCommandExecutor> commmandExecutors = new LinkedHashMap<>();
	private Thread tcpServerThread;

	/**
	 * @param port port
	 */
	@Inject
	public TcpVCommandEngine(@Named("port") final int port) {
		this.port = port;
	}

	/** {@inheritDoc} */
	@Override
	public void registerCommandExecutor(final String name, final VCommandExecutor commandExecutor) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(commandExecutor);
		Assertion.checkArgument(!commmandExecutors.containsKey(name), "command '{0}' is already registered'", name);
		//-----
		commmandExecutors.put(name, commandExecutor);
	}

	/** {@inheritDoc} */
	@Override
	public void start() {
		Home.getApp().registerAppListener(new AppListener() {
			@Override
			public void onPostStart() {
				registerCommandAndStartServer();
			}
		});
	}

	private void registerCommandAndStartServer() {
		//Chargement des commandes
		registerCommandExecutor("ping", new VPingCommandExecutor());
		registerCommandExecutor("system", new VSystemCommandExecutor());

		//---
		registerCommandExecutor("help", new VCommandExecutor<Set<String>>() {
			//All commands are listed
			/** {@inheritDoc} */
			@Override
			public Set<String> exec(final VCommand command) {
				Assertion.checkNotNull(command);
				//Assertion.checkArgument(command.getName());
				//-----
				return commmandExecutors.keySet();
			}
		});

		registerCommandExecutor("config", new VCommandExecutor<AppConfig>() {
			@Override
			public AppConfig exec(final VCommand command) {
				return Home.getApp().getConfig();
			}
		});

		registerCommandExecutor("definitions", new VCommandExecutor<DefinitionSpace>() {
			/** {@inheritDoc} */
			@Override
			public DefinitionSpace exec(final VCommand command) {
				Assertion.checkNotNull(command);
				//-----
				return Home.getApp().getDefinitionSpace();
			}
		});

		scanAllComponents();

		final VServer tcpServer = new VServer(this, port);
		tcpServerThread = new Thread(tcpServer);
		tcpServerThread.start();

		//	new TcpBroadcaster().hello(port);
	}

	private void scanAllComponents() {
		final MapBuilder<String, VCommandExecutor> mapBuilder = new MapBuilder<>();
		for (final String componentId : Home.getApp().getComponentSpace().keySet()) {
			CommandScannerUtil.scan(mapBuilder, componentId, Home.getApp().getComponentSpace().resolve(componentId, Object.class));
		}

		for (final Entry<String, VCommandExecutor> entry : mapBuilder.build().entrySet()) {
			registerCommandExecutor(entry.getKey(), entry.getValue());
		}
	}

	@Override
	public void stop() {
		tcpServerThread.interrupt();
		try {
			tcpServerThread.join();
		} catch (final InterruptedException e) {
			//
		}
	}

	private Object exec(final VCommand command) {
		Assertion.checkNotNull(command);
		Assertion.checkArgument(commmandExecutors.containsKey(command.getName()), "command '{0}' unknown", command.getName());
		//-----
		final VCommandExecutor<?> commandExecutor = commmandExecutors.get(command.getName());
		return commandExecutor.exec(command);
	}

	static final class VError {
		private final String error;

		VError(final String error) {
			this.error = error;
		}

		public String getError() {
			return error;
		}
	}

	@Override
	public VResponse onCommand(final VCommand command) {
		return VResponse.createResponse(JsonUtil.toJson(exec(command)));
	}

}
