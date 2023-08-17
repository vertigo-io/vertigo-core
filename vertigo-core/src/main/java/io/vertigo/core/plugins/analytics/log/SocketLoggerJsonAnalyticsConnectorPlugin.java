/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.plugins.analytics.log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Optional;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.net.ssl.KeyManagerFactory;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.SocketAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.JsonLayout;
import org.apache.logging.log4j.core.net.ssl.SslConfiguration;
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfiguration;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.analytics.trace.TraceSpan;
import io.vertigo.core.daemon.DaemonScheduled;
import io.vertigo.core.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.WrappedException;
import io.vertigo.core.lang.json.CoreJsonAdapters;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.Param;
import io.vertigo.core.param.ParamManager;
import io.vertigo.core.param.ParamValue;

/**
 * Processes connector which use the log4j SocketAppender.
 * @author mlaroche, pchretien, npiedeloup
 */
public final class SocketLoggerJsonAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin, Activeable {
	private static final Gson GSON = CoreJsonAdapters.V_CORE_GSON;
	private static final int DEFAULT_CONNECT_TIMEOUT = 250;// 250ms for connection to log4j server
	private static final int DEFAULT_DISCONNECT_TIMEOUT = 5000;// 5s for disconnection to log4j server
	private static final int DEFAULT_SERVER_PORT = 4563;// DefaultPort of SocketAppender 4562 for log4j2 and 4563 for json over tls

	private Logger socketProcessLogger;
	private Logger socketHealthLogger;
	private Logger socketMetricLogger;
	private final String hostName;
	private final int port;
	private SocketAppender appender;

	private final String appName;
	private final String localHostName;

	private final ConcurrentLinkedQueue<TraceSpan> processQueue = new ConcurrentLinkedQueue<>();

	private final Optional<String> trustStoreUrl;
	private final Optional<String> trustStorePassword;

	/**
	 * Constructor.
	 * @param appNameOpt the node name
	 * @param hostNameOpt hostName of the remote server
	 * @param portOpt port of the remote server
	 */
	@Inject
	public SocketLoggerJsonAnalyticsConnectorPlugin(
			final ParamManager paramManager,
			@ParamValue("appName") final Optional<String> appNameOpt,
			@ParamValue("hostName") final Optional<String> hostNameOpt,
			@ParamValue("port") final Optional<Integer> portOpt,
			@ParamValue("trustStoreUrl") final Optional<String> trustStoreUrlOpt,
			@ParamValue("trustStorePassword") final Optional<String> trustStorePasswordOpt) {
		Assertion.check()
				.isNotNull(appNameOpt)
				.isNotNull(hostNameOpt)
				.isNotNull(portOpt);
		// ---
		appName = appNameOpt.orElseGet(() -> Node.getNode().getNodeConfig().appName());
		hostName = hostNameOpt.orElse("analytica.part.klee.lan.net");
		port = portOpt.orElse(DEFAULT_SERVER_PORT);
		localHostName = retrieveHostName();

		trustStoreUrl = trustStoreUrlOpt.isPresent() ? trustStoreUrlOpt : paramManager.getOptionalParam("VERTIGO_ANALYTICS_TRUSTSTORE_URL").map(Param::getValueAsString);
		trustStorePassword = trustStorePasswordOpt.isPresent() ? trustStorePasswordOpt : paramManager.getOptionalParam("VERTIGO_ANALYTICS_TRUSTSTORE_PASSWORD").map(Param::getValueAsString);
	}

	/** {@inheritDoc} */
	@Override
	public void add(final TraceSpan process) {
		Assertion.check()
				.isNotNull(process);
		//---
		processQueue.add(process);
	}

	/** {@inheritDoc} */
	@Override
	public void add(final Metric metric) {
		if (socketMetricLogger == null) {
			socketMetricLogger = createLogger("vertigo-analytics-metric");
		}
		sendObject(metric, socketMetricLogger);

	}

	/** {@inheritDoc} */
	@Override
	public void add(final HealthCheck healthCheck) {
		if (socketHealthLogger == null) {
			socketHealthLogger = createLogger("vertigo-analytics-health");
		}
		sendObject(healthCheck, socketHealthLogger);

	}

	private static String retrieveHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			LogManager.getRootLogger().info("Cannot retrieve hostname", e);
			return "UnknownHost";
		}
	}

	@Override
	public void start() {

		//we create appender (like a resource it must be close on stop)
		try {
			final var trutsStoreConfig = trustStoreUrl.isPresent() ? TrustStoreConfiguration.createKeyStoreConfiguration(trustStoreUrl.get(), trustStorePassword.get().toCharArray(), null, null, "PKCS12", KeyManagerFactory
					.getDefaultAlgorithm()) : null;

			appender = SocketAppender.newBuilder()
					.setName("socketAnalytics")
					.setLayout(JsonLayout.createDefaultLayout())
					.setHost(hostName)
					.setPort(port)
					.setConnectTimeoutMillis(DEFAULT_CONNECT_TIMEOUT)
					.setImmediateFail(true)
					.setReconnectDelayMillis(0)// we make only one try
					.setSslConfiguration(
							SslConfiguration.createSSLConfiguration(
									"TLSv1.2",
									null,
									trutsStoreConfig))
					.build();

			appender.start();
			final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			final Configuration config = ctx.getConfiguration();
			config.addAppender(appender);
		} catch (final Exception e) {
			throw WrappedException.wrap(e);
		}
	}

	@Override
	public void stop() {
		appender.stop(DEFAULT_DISCONNECT_TIMEOUT, TimeUnit.MILLISECONDS);
		appender = null;
	}

	private Logger createLogger(final String loggerName) {
		Assertion.check()
				.isNotNull(appender, "SocketLogger is not started, cannot create logger and send analytics data. Wait until node is started.");
		// If it doesn't exist we create it with the right appender

		final LoggerContext context = (LoggerContext) LogManager.getContext(false); //on ne close pas : car ca stop le context
		final Configuration config = context.getConfiguration();
		final LoggerConfig loggerConfig = LoggerConfig.createLogger(false, Level.INFO, loggerName, "true", new AppenderRef[] {}, null, config, null);

		loggerConfig.addAppender(appender, null, null);
		config.addLogger(loggerName, loggerConfig);
		context.updateLoggers();
		return LogManager.getLogger(loggerName);
	}

	/**
	 * Daemon to unstack processes to end them
	 */
	@DaemonScheduled(name = "DmnRemoteLogger", periodInSeconds = 1, analytics = false)
	public void pollQueue() {
		while (!processQueue.isEmpty()) {
			final TraceSpan head = processQueue.poll();
			if (head != null) {
				sendProcess(head);
			}
		}

	}

	private void sendProcess(final TraceSpan process) {
		if (socketProcessLogger == null) {
			socketProcessLogger = createLogger("vertigo-analytics-process");
		}
		sendObject(process, socketProcessLogger);
	}

	private void sendObject(final Object object, final Logger logger) {

		if (logger.isInfoEnabled()) {
			final JsonObject log = new JsonObject();
			log.addProperty("appName", appName);
			log.addProperty("host", localHostName);
			log.add("event", GSON.toJsonTree(object));
			logger.info(GSON.toJson(log));
		}
	}

}
