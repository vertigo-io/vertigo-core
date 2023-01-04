/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2022, Vertigo.io, team@vertigo.io
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.SocketAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.SerializedLayout;
import org.apache.logging.log4j.core.net.SocketOptions;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.analytics.process.AProcess;
import io.vertigo.core.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.Param;
import io.vertigo.core.param.ParamManager;
import io.vertigo.core.param.ParamValue;

/**
 * Processes connector which use the log4j SocketAppender.
 * @author mlaroche, pchretien, npiedeloup
 */
public final class SocketLoggerAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin, Activeable {
	private static final Logger LOGGER = LogManager.getLogger(SocketLoggerAnalyticsConnectorPlugin.class);
	private static final Gson GSON = new GsonBuilder().create();
	private static final int DEFAULT_CONNECT_TIMEOUT = 250;// 250ms for connection to log4j server
	private static final int DEFAULT_DISCONNECT_TIMEOUT = 5000;// 5s for disconnection to log4j server
	private static final int DEFAULT_SERVER_PORT = 4562;// DefaultPort of SocketAppender 4650 for log4j and 4562 for log4j2

	private static final int SEND_QUEUE_MAX_SIZE = 10_000;// 10k elements
	private int logCounterEvery100 = 0;
	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new SocketLoggerAnalyticsThreadFactory());

	private Logger socketProcessLogger;
	private Logger socketHealthLogger;
	private Logger socketMetricLogger;
	private final String hostName;
	private final int port;
	private SocketAppender appender;

	private final String appName;
	private final String nodeName;

	private final ConcurrentLinkedQueue<Object> sendQueue = new ConcurrentLinkedQueue<>();

	static class SocketLoggerAnalyticsThreadFactory implements ThreadFactory {
		private static int threadCounter = 0;

		@Override
		public Thread newThread(final Runnable r) {
			return new Thread(r, "SocketLoggerAnalyticsExecutor-" + (++threadCounter));
		}
	}

	/**
	 * Constructor.
	 * @param appNameOpt the node name
	 * @param hostNameOpt hostName of the remote server
	 * @param portOpt port of the remote server
	 */
	@Inject
	public SocketLoggerAnalyticsConnectorPlugin(
			final ParamManager paramManager,
			@ParamValue("hostNameParam") final Optional<String> hostNameParamOpt,
			@ParamValue("portParam") final Optional<String> portParamOpt,
			@ParamValue("nodeNameParam") final Optional<String> nodeNameParamOpt,
			@ParamValue("envNameParam") final Optional<String> envNameParamOpt) {
		Assertion.check()
				.isNotNull(hostNameParamOpt)
				.isNotNull(portParamOpt)
				.isNotNull(nodeNameParamOpt)
				.isNotNull(envNameParamOpt);
		// ---
		appName = Node.getNode().getNodeConfig().getAppName() + envNameParamOpt.map(paramManager::getParam).map(Param::getValueAsString).map(env -> '-' + env.toLowerCase()).orElse("");
		hostName = hostNameParamOpt.map(paramManager::getParam).map(Param::getValueAsString).orElse("analytica.part.klee.lan.net");
		port = portParamOpt.map(paramManager::getParam).map(Param::getValueAsInt).orElse(DEFAULT_SERVER_PORT);
		nodeName = nodeNameParamOpt.map(paramManager::getOptionalParam).map(opt -> opt.map(Param::getValueAsString).orElseGet(SocketLoggerAnalyticsConnectorPlugin::retrieveHostName)).get();
	}

	/** {@inheritDoc} */
	@Override
	public void add(final AProcess process) {
		Assertion.check()
				.isNotNull(process);
		//---
		if (sendQueue.size() > SEND_QUEUE_MAX_SIZE) {
			if (logCounterEvery100 == 0) {
				LOGGER.error("sendQueue full (" + SEND_QUEUE_MAX_SIZE + "), loose process ");
			}
			logCounterEvery100 = ++logCounterEvery100 % 100;
		} else {
			logCounterEvery100 = 0;
			sendQueue.add(process);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void add(final Metric metric) {
		Assertion.check()
				.isNotNull(metric);
		//---
		if (sendQueue.size() > SEND_QUEUE_MAX_SIZE) {
			if (logCounterEvery100 == 0) {
				LOGGER.error("sendQueue full (" + SEND_QUEUE_MAX_SIZE + "), loose metrics ");
			}
			logCounterEvery100 = ++logCounterEvery100 % 100;
		} else {
			logCounterEvery100 = 0;
			sendQueue.add(metric);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void add(final HealthCheck healthCheck) {
		Assertion.check()
				.isNotNull(healthCheck);
		//---
		if (sendQueue.size() > SEND_QUEUE_MAX_SIZE) {
			if (logCounterEvery100 == 0) {
				LOGGER.error("sendQueue full (" + SEND_QUEUE_MAX_SIZE + "), loose healthChecks ");
			}
			logCounterEvery100 = ++logCounterEvery100 % 100;
		} else {
			logCounterEvery100 = 0;
			sendQueue.add(healthCheck);
		}
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
		try {
			//we create appender (like a resource it must be close on stop)
			appender = SocketAppender.newBuilder()
					.setName("socketAnalytics")
					.setLayout(SerializedLayout.createLayout())
					.withHost(hostName)
					.withPort(port)
					.withConnectTimeoutMillis(DEFAULT_CONNECT_TIMEOUT)
					.withSocketOptions(SocketOptions.newBuilder().setSoTimeout(5000).build())
					.withImmediateFail(true)
					.withReconnectDelayMillis(-1)// we make only one try
					.build();

			appender.start();
			final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
			final Configuration config = ctx.getConfiguration();
			config.addAppender(appender);
		} catch (final Exception e) {
			LOGGER.info("Unable to connect to analytics server", e);
		}

		final PoolerTimerTask timerTask = new PoolerTimerTask(this);
		scheduler.scheduleWithFixedDelay(timerTask, 1, 1, TimeUnit.SECONDS);
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
	//@DaemonScheduled(name = "DmnRemoteLogger", periodInSeconds = 1, analytics = false)
	public void pollQueue() {
		while (!sendQueue.isEmpty()) {
			final Object head = sendQueue.peek();

			if (head != null) {
				try {
					if (head instanceof AProcess) {
						sendProcess((AProcess) head);
					} else if (head instanceof Metric) {
						sendMetric((Metric) head);
					} else if (head instanceof HealthCheck) {
						sendHealthCheck((HealthCheck) head);
					}
					sendQueue.remove(head);
				} catch (final Exception e) {
					LOGGER.error("Can't send " + head.getClass().getSimpleName() + " to analytics server (sendQueueSize:" + sendQueue.size() + ")", e);
				}
			}
		}
	}

	private void sendProcess(final AProcess process) {
		if (appender != null && socketProcessLogger == null) {
			socketProcessLogger = createLogger("vertigo-analytics-process");
		}
		sendObject(process, socketProcessLogger);
	}

	private void sendMetric(final Metric metric) {
		if (appender != null && socketMetricLogger == null) {
			socketMetricLogger = createLogger("vertigo-analytics-metric");
		}
		sendObject(metric, socketMetricLogger);
	}

	private void sendHealthCheck(final HealthCheck healthCheck) {
		if (appender != null && socketHealthLogger == null) {
			socketHealthLogger = createLogger("vertigo-analytics-health");
		}
		sendObject(healthCheck, socketHealthLogger);

	}

	private void sendObject(final Object object, final Logger logger) {

		if (appender != null && logger.isInfoEnabled()) {
			final JsonObject log = new JsonObject();
			log.addProperty("appName", appName);
			log.addProperty("host", nodeName);
			log.add("event", GSON.toJsonTree(object));
			logger.info(GSON.toJson(log));
		}
	}

	/**
	 * @author npiedeloup
	 */
	static final class PoolerTimerTask implements Runnable {
		private static final Logger LOG = LogManager.getLogger(PoolerTimerTask.class);

		private final SocketLoggerAnalyticsConnectorPlugin socketLoggerAnalyticsConnectorPlugin;

		PoolerTimerTask(final SocketLoggerAnalyticsConnectorPlugin socketLoggerAnalyticsConnectorPlugin) {
			Assertion.check()
					.isNotNull(socketLoggerAnalyticsConnectorPlugin);
			//---
			this.socketLoggerAnalyticsConnectorPlugin = socketLoggerAnalyticsConnectorPlugin;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			try {//try catch needed to ensure execution aren't suppressed
				socketLoggerAnalyticsConnectorPlugin.pollQueue();
			} catch (final Throwable th) { //catch Throwable to not stop daemon task silently
				LOG.error("Can't pollQueue", th);
			}
		}
	}

}
