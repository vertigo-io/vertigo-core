/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.commons.plugins.analytics.log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import io.vertigo.commons.daemon.DaemonManager;
import io.vertigo.commons.impl.analytics.AProcess;
import io.vertigo.commons.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.commons.impl.daemon.DaemonDefinition;
import io.vertigo.core.definition.Definition;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.SimpleDefinitionProvider;
import io.vertigo.lang.Assertion;

/**
 * Processes connector which use the log4j SocketAppender.
 * @author mlaroche, pchretien, npiedeloup
 */
public final class SocketLoggerAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin, SimpleDefinitionProvider {
	private static final Gson GSON = new GsonBuilder().create();
	private static final int DEFAULT_SERVER_PORT = 4560;// DefaultPort of SocketAppender

	private Logger socketLogger;
	private final String hostName;
	private final int port;

	private final String appName;
	private final String localHostName;

	private final ConcurrentLinkedQueue<AProcess> processQueue = new ConcurrentLinkedQueue<>();

	/**
	 * Constructor.
	 * @param daemonManager the daemonManager
	 * @param appName the app name
	 * @param hostNameOpt hostName of the remote server
	 * @param portOpt port of the remote server
	 */
	@Inject
	public SocketLoggerAnalyticsConnectorPlugin(
			final DaemonManager daemonManager,
			@Named("appName") final String appName,
			@Named("hostName") final Optional<String> hostNameOpt,
			@Named("port") final Optional<Integer> portOpt) {
		Assertion.checkArgNotEmpty(appName);
		Assertion.checkNotNull(hostNameOpt);
		Assertion.checkNotNull(portOpt);
		// ---
		hostName = hostNameOpt.orElse("analytica.part.klee.lan.net");
		port = portOpt.orElse(DEFAULT_SERVER_PORT);
		this.appName = appName;
		localHostName = retrieveHostName();
	}

	@Override
	public List<? extends Definition> provideDefinitions(final DefinitionSpace definitionSpace) {
		return Collections.singletonList(new DaemonDefinition(
				"DMN_REMOTE_LOGGER",
				() -> this::pollQueue,
				1));
	}

	/** {@inheritDoc} */
	@Override
	public void add(final AProcess process) {
		Assertion.checkNotNull(process);
		//---
		processQueue.add(process);
	}

	private static String retrieveHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			Logger.getRootLogger().info("Cannot retrieve hostname", e);
			return "UnknownHost";
		}
	}

	private static Logger createLogger(final String hostName, final int port) {
		// If it doesn't exist we create it with the right appender
		final Logger logger = Logger.getLogger(SocketLoggerAnalyticsConnectorPlugin.class);
		// Create an appender
		final SocketAppender appender = new SocketAppender(hostName, port);
		// we make only one try
		appender.setReconnectionDelay(0);
		//---
		logger.removeAllAppenders();
		logger.addAppender(appender);
		logger.setLevel(Level.INFO);
		logger.setAdditivity(false);
		return logger;
	}

	private void pollQueue() {
		while (!processQueue.isEmpty()) {
			final AProcess head = processQueue.poll();
			if (head != null) {
				sendProcess(head);
			}
		}

	}

	private void sendProcess(final AProcess process) {
		if (socketLogger == null) {
			socketLogger = createLogger(hostName, port);
		}
		if (socketLogger.isInfoEnabled()) {
			final JsonObject log = new JsonObject();
			log.addProperty("appName", appName);
			log.addProperty("host", localHostName);
			log.add("event", GSON.toJsonTree(process));
			socketLogger.info(GSON.toJson(log));
		}
	}

}
