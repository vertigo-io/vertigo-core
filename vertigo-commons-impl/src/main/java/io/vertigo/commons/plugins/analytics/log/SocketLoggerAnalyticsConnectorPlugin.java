package io.vertigo.commons.plugins.analytics.log;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.Optional;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.net.SocketAppender;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertigo.commons.impl.analytics.AProcess;
import io.vertigo.commons.impl.analytics.AnalyticsConnectorPlugin;
import io.vertigo.lang.Assertion;

/**
 * Processes connector which use the log4j SocketAppender.
 * @author mlaroche, pchretien, npiedeloup
 */
public final class SocketLoggerAnalyticsConnectorPlugin implements AnalyticsConnectorPlugin {
	private static final Gson GSON = new GsonBuilder().create();
	private static final int DEFAULT_SERVER_PORT = 4560;// DefaultPort of SocketAppender

	private final Logger socketLogger;
	private final String nodeIdentifier;

	/**
	 * Contructor
	 * @param hostNameOpt hostName of the remote server
	 * @param portOpt port of the remote server
	 */
	@Inject
	public SocketLoggerAnalyticsConnectorPlugin(@Named("appName") final String appName,
			@Named("hostName") final Optional<String> hostNameOpt,
			@Named("port") final Optional<Integer> portOpt) {
		Assertion.checkArgNotEmpty(appName);
		Assertion.checkNotNull(hostNameOpt);
		Assertion.checkNotNull(portOpt);
		// ---
		final String hostName = hostNameOpt.orElse("analytica.part.klee.lan.net");
		final int port = portOpt.orElse(DEFAULT_SERVER_PORT);
		nodeIdentifier = appName + ":" + retrieveHostName();
		//---
		socketLogger = createLogger(hostName, port);

	}

	/** {@inheritDoc} */
	@Override
	public void add(final AProcess process) {
		if (socketLogger.isInfoEnabled()) {
			final StringBuilder log = new StringBuilder(nodeIdentifier)
					.append(" - ")
					.append(GSON.toJson(Collections.singletonList(process)));
			socketLogger.info(log.toString());
		}
	}

	private static String retrieveHostName() {
		try {
			return InetAddress.getLocalHost().getHostName();
		} catch (final UnknownHostException e) {
			Logger.getRootLogger().info(e);
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
		logger.addAppender(appender);
		logger.setLevel(Level.INFO);
		return logger;
	}

}