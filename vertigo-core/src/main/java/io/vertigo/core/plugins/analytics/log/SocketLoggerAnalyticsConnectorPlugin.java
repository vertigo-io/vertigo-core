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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.inject.Inject;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.SocketAppender;
import org.apache.logging.log4j.core.config.AppenderRef;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.DefaultConfiguration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.layout.SerializedLayout;
import org.apache.logging.log4j.core.net.SocketOptions;
import org.apache.logging.log4j.layout.template.json.JsonTemplateLayout;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;

import io.vertigo.core.analytics.health.HealthCheck;
import io.vertigo.core.analytics.metric.Metric;
import io.vertigo.core.analytics.trace.TraceSpan;
import io.vertigo.core.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.json.CoreJsonAdapters;
import io.vertigo.core.node.Node;
import io.vertigo.core.node.component.Activeable;
import io.vertigo.core.param.Param;
import io.vertigo.core.param.ParamManager;
import io.vertigo.core.param.ParamValue;
import io.vertigo.core.plugins.analytics.log.log4j.AnalyticsSocketAppender;
import io.vertigo.core.plugins.analytics.log.log4j.AnalyticsSocketAppender.Builder;
import io.vertigo.core.util.NamedThreadFactory;

/**
 * Processes connector which use the log4j SocketAppender.
 *
 * @author mlaroche, pchretien, npiedeloup
 */
public final class SocketLoggerAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin, Activeable {
	private static final int TEN_SECONDS = 10 * 1000;
	private static final Logger LOGGER = LogManager.getLogger(SocketLoggerAnalyticsConnectorPlugin.class);
	private static final Gson GSON = CoreJsonAdapters.addCoreGsonConfig(new GsonBuilder(), false).create();
	private static final int DEFAULT_CONNECT_TIMEOUT = 250;// 250ms for connection to log4j server
	private static final int DEFAULT_SOCKET_TIMEOUT = 5000;// 5s for socket to log4j server
	private static final int DEFAULT_DISCONNECT_TIMEOUT = 5000;// 5s for disconnection to log4j server
	private static final int DEFAULT_SERVER_PORT = 4562;// DefaultPort of SocketAppender 4650 for log4j and 4562 for log4j2
	private static final int SEND_QUEUE_MAX_SIZE = 10_000;// 10k elements

	private Logger socketProcessLogger;
	private Logger socketHealthLogger;
	private Logger socketMetricLogger;

	private final String appName;
	private final String nodeName;

	private final String hostName;
	private final int port;
	private SocketAppender appender;
	private final boolean devConfig;
	private final int bufferSize;
	private final int batchSize;
	private final boolean jsonLayout;
	private final boolean compressPayload;
	private final boolean compressOutputStream;

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(new NamedThreadFactory("v-socketLoggerAnalytics-"));

	private volatile int logSendCount = 0;
	private volatile int logErrorCount = 0;
	private volatile long logErrorEvery10sTime = 0;
	//private final ConcurrentLinkedQueue<Object> sendQueue = new ConcurrentLinkedQueue<>();
	private final Queue<Object> sendQueue = new LinkedBlockingQueue<>(SEND_QUEUE_MAX_SIZE + 1);

	/**
	 * Constructor.
	 *
	 * @param appNameOpt the node name
	 * @param hostNameOpt hostName of the remote server
	 * @param portOpt port of the remote server
	 * @param bufferSizeOpt size of the offline buffer in Mo
	 */
	@Inject
	public SocketLoggerAnalyticsConnectorPlugin(
			final ParamManager paramManager,
			@ParamValue("hostName") final Optional<String> hostNameOpt,
			@ParamValue("port") final Optional<Integer> portOpt,
			@ParamValue("hostNameParam") final Optional<String> hostNameParamOpt,
			@ParamValue("portParam") final Optional<String> portParamOpt,
			@ParamValue("nodeNameParam") final Optional<String> nodeNameParamOpt,
			@ParamValue("envNameParam") final Optional<String> envNameParamOpt,
			@ParamValue("bufferSize") final Optional<Integer> bufferSizeOpt,
			@ParamValue("batchSize") final Optional<Integer> batchSizeOpt,
			@ParamValue("jsonLayout") final Optional<Boolean> jsonLayoutOpt,
			@ParamValue("compressPayload") final Optional<Boolean> compressPayloadOpt, //may be removed soon : not usefull
			@ParamValue("compressOutputStream") final Optional<Boolean> compressOutputStreamOpt) {
		Assertion.check()
				.isNotNull(hostNameOpt)
				.isNotNull(portOpt)
				.isNotNull(hostNameParamOpt)
				.isNotNull(portParamOpt)
				.isNotNull(nodeNameParamOpt)
				.isNotNull(envNameParamOpt)
				.when(hostNameOpt.isPresent(), () -> Assertion.check().isTrue(hostNameParamOpt.isEmpty(), "hostName and hostNameParam are exclusive"))
				.when(hostNameParamOpt.isPresent(), () -> Assertion.check().isTrue(hostNameOpt.isEmpty(), "hostName and hostNameParam are exclusive"))
				.when(portOpt.isPresent(), () -> Assertion.check().isTrue(portParamOpt.isEmpty(), "port and portParam are exclusive"))
				.when(portParamOpt.isPresent(), () -> Assertion.check().isTrue(portOpt.isEmpty(), "port and portParam are exclusive"))
				.when(jsonLayoutOpt.orElse(true), () -> Assertion.check().isFalse(compressPayloadOpt.orElse(false), "jsonLayout doesn't support compressPayload"));
		// ---
		appName = Node.getNode().getNodeConfig().appName() + envNameParamOpt.map(paramManager::getParam).map(Param::getValueAsString).map(env -> '-' + env.toLowerCase()).orElse("");
		hostName = hostNameOpt.orElseGet(() -> hostNameParamOpt.map(paramManager::getParam).map(Param::getValueAsString).orElse("analytica.part.klee.lan.net"));
		devConfig = hostNameParamOpt.isEmpty();
		port = portOpt.orElseGet(() -> portParamOpt.map(paramManager::getParam).map(Param::getValueAsInt).orElse(DEFAULT_SERVER_PORT));
		nodeName = nodeNameParamOpt
				.map(paramName -> paramManager.getOptionalParam(paramName).map(Param::getValueAsString).orElseGet(SocketLoggerAnalyticsConnectorPlugin::retrieveHostName))
				.orElseGet(SocketLoggerAnalyticsConnectorPlugin::retrieveHostName);
		bufferSize = bufferSizeOpt.orElse(50);
		batchSize = batchSizeOpt.orElse(1);
		jsonLayout = jsonLayoutOpt.orElse(true);
		compressPayload = compressPayloadOpt.orElse(false);
		compressOutputStream = compressOutputStreamOpt.orElse(true);
	}

	/** {@inheritDoc} */
	@Override
	public void add(final TraceSpan span) {
		Assertion.check().isNotNull(span);
		//---
		if (!sendQueueFull()) {
			sendQueue.add(span);
		}
	}

	private boolean sendQueueFull() {
		final boolean isFull = sendQueue.size() >= SEND_QUEUE_MAX_SIZE;
		if (isFull) {
			logErrorCount++;
			if (System.currentTimeMillis() - logErrorEvery10sTime > TEN_SECONDS) {
				LOGGER.error("sendQueue full (" + SEND_QUEUE_MAX_SIZE + "), loose " + logErrorCount + " events (in:" + (logSendCount + logErrorCount) / 10 + "/s ; out:" + logSendCount / 10 + "/s)");
				logErrorCount = 0;
				logSendCount = 0;
				logErrorEvery10sTime = System.currentTimeMillis();
			}
		}
		return isFull;
	}

	/** {@inheritDoc} */
	@Override
	public void add(final Metric metric) {
		Assertion.check()
				.isNotNull(metric);
		//---
		if (!sendQueueFull()) {
			sendQueue.add(metric);
		}
	}

	/** {@inheritDoc} */
	@Override
	public void add(final HealthCheck healthCheck) {
		Assertion.check()
				.isNotNull(healthCheck);
		//---
		if (!sendQueueFull()) {
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
		final Builder appenderBuilder = AnalyticsSocketAppender.newAnalyticsBuilder()
				.setName("socketAnalytics");
		if (jsonLayout) {
			appenderBuilder.setLayout(JsonTemplateLayout.newBuilder()
					.setConfiguration(new DefaultConfiguration()).build());
		} else {
			appenderBuilder.setLayout(SerializedLayout.createLayout());
		}
		appenderBuilder.setHost(hostName)
				.setPort(port)
				.setCompress(compressOutputStream)
				.setConnectTimeoutMillis(DEFAULT_CONNECT_TIMEOUT)
				.setSocketOptions(SocketOptions.newBuilder()
						.setSoTimeout(DEFAULT_SOCKET_TIMEOUT).build());

		if (devConfig) {
			appenderBuilder
					.setImmediateFail(true)
					.setImmediateFlush(true)
					.setReconnectDelayMillis(-1)// we make only one try (documentation is incorrect 0 => defaults to 30s)
					.setBufferSize(bufferSize * 1024 * 1024) // in Mo, used for keeping logs while disconnected
			;
		} else {
			appenderBuilder
					.setImmediateFail(false)
					.setImmediateFlush(false)
					.setReconnectDelayMillis(10000) // 10s
					.setBufferSize(bufferSize * 1024 * 1024) // in Mo, used for keeping logs while disconnected
			;
		}

		//we create appender (like a resource it must be close on stop)
		appender = appenderBuilder.build();
		appender.start();
		final LoggerContext ctx = (LoggerContext) LogManager.getContext(false);
		final Configuration config = ctx.getConfiguration();
		config.addAppender(appender);

		final PoolerTimerTask timerTask = new PoolerTimerTask(this);
		scheduler.scheduleWithFixedDelay(timerTask, 1, 1, TimeUnit.SECONDS);
	}

	@Override
	public void stop() {
		scheduler.shutdown();
		pollQueue();
		forceSendBatch();
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
	public void pollQueue() {
		//checkAppenders exists
		checkSocketProcessLogger();
		try {
			//check each run, if its time to send
			checkAndSendBatchIfNeeded();

			while (!sendQueue.isEmpty()) {
				final Object head = sendQueue.peek();
				if (head != null) {
					if (head instanceof TraceSpan) {
						spanBatch.add((TraceSpan) head);
					} else if (head instanceof Metric) {
						metricBatch.add((Metric) head);
					} else if (head instanceof HealthCheck) {
						healthCheckBatch.add((HealthCheck) head);
					}
					sendQueue.remove(head);
					logSendCount++;
				}
				//check each loop, if batchs were full
				checkAndSendBatchIfNeeded();
			}
		} catch (final Exception e) {
			LOGGER.error("Can't send data to analytics server (sendQueueSize:" + sendQueue.size() + ")", e);
			//stop and wait next loop
		}
	}

	private long spanBatchSend = 0;
	private final List<TraceSpan> spanBatch = new ArrayList<>();
	private long metricBatchSend = 0;
	private final List<Metric> metricBatch = new ArrayList<>();
	private long healthCheckBatchSend = 0;
	private final List<HealthCheck> healthCheckBatch = new ArrayList<>();

	private void checkSocketProcessLogger() {
		if (appender != null) {
			if (socketProcessLogger == null) {
				socketProcessLogger = createLogger("vertigo-analytics-process");
			}
			if (socketMetricLogger == null) {
				socketMetricLogger = createLogger("vertigo-analytics-metric");
			}
			if (socketHealthLogger == null) {
				socketHealthLogger = createLogger("vertigo-analytics-health");
			}
		}
	}

	private void checkAndSendBatchIfNeeded() {
		checkAndSendBatchIfNeeded(spanBatch, spanBatchSend, (time) -> spanBatchSend = time, socketProcessLogger);
		checkAndSendBatchIfNeeded(metricBatch, metricBatchSend, (time) -> metricBatchSend = time, socketMetricLogger);
		checkAndSendBatchIfNeeded(healthCheckBatch, healthCheckBatchSend, (time) -> healthCheckBatchSend = time, socketHealthLogger);
	}

	private void checkAndSendBatchIfNeeded(final List<?> items, final long batchTime, final Consumer<Long> batchTimeSetter, final Logger logger) {
		if (!items.isEmpty()) {
			if (batchSize > 1 && items.size() == 1) {
				batchTimeSetter.accept(System.currentTimeMillis());
			} else if (items.size() >= batchSize || System.currentTimeMillis() - batchTime > TEN_SECONDS) {
				sendObjects(items, logger);
				items.clear();
			}
		}
	}

	private void forceSendBatch() {
		sendObjects(spanBatch, socketProcessLogger);
		sendObjects(metricBatch, socketMetricLogger);
		sendObjects(healthCheckBatch, socketHealthLogger);
	}

	private void sendObjects(final List<?> list, final Logger logger) {
		if (appender != null && logger.isInfoEnabled() && !list.isEmpty()) {
			final JsonObject log = new JsonObject();
			log.addProperty("appName", appName);
			log.addProperty("host", nodeName);
			if (list.size() == 1) {
				log.add("event", GSON.toJsonTree(list.get(0)));
			} else {
				log.add("events", GSON.toJsonTree(list));
			}
			final String jsonEvent = GSON.toJson(log);
			if (compressPayload) {
				logger.info(new JsonCompressedByteMessage(jsonEvent));
			} else {
				logger.info(jsonEvent);
			}
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
