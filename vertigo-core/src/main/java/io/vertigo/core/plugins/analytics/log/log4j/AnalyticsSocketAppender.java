package io.vertigo.core.plugins.analytics.log.log4j;

import java.io.Serializable;

import org.apache.logging.log4j.core.AbstractLifeCycle;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.SocketAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.PluginBuilderFactory;
import org.apache.logging.log4j.core.net.AbstractSocketManager;
import org.apache.logging.log4j.core.net.Advertiser;
import org.apache.logging.log4j.core.net.Protocol;

import io.vertigo.core.lang.VSystemException;

public class AnalyticsSocketAppender extends SocketAppender {

	public AnalyticsSocketAppender(final String name, final Layout<? extends Serializable> layout, final Filter filter, final AbstractSocketManager manager, final boolean ignoreExceptions,
			final boolean immediateFlush,
			final Advertiser advertiser, final Property[] properties) {
		super(name, layout, filter, manager, ignoreExceptions, immediateFlush, advertiser, properties);
	}

	/**
	 * Builds a AnalyticsSocketAppender.
	 * <ul>
	 * <li>Removed deprecated "delayMillis", use "reconnectionDelayMillis".</li>
	 * <li>Removed deprecated "reconnectionDelay", use "reconnectionDelayMillis".</li>
	 * </ul>
	 */
	public static class Builder extends AbstractBuilder<Builder>
			implements org.apache.logging.log4j.core.util.Builder<AnalyticsSocketAppender> {

		@SuppressWarnings("resource")
		@Override
		public AnalyticsSocketAppender build() {
			final boolean immediateFlush = isImmediateFlush();
			final boolean bufferedIo = isBufferedIo();
			final Layout<? extends Serializable> layout = getLayout();
			if (layout == null) {
				AbstractLifeCycle.LOGGER.error("No layout provided for AnalyticsSocketAppender");
				return null;
			}

			final String name = getName();
			if (name == null) {
				AbstractLifeCycle.LOGGER.error("No name provided for AnalyticsSocketAppender");
				return null;
			}

			final Protocol protocol = getProtocol();
			if (protocol != null && protocol != Protocol.TCP) {
				throw new VSystemException("Only TCP protocol is supported");
			}

			final AbstractSocketManager manager = AnalyticsTcpSocketManager.getSocketManager(getHost(), getPort(), getConnectTimeoutMillis(), getReconnectDelayMillis(), getImmediateFail(), layout,
					getBufferSize(), getSocketOptions());

			return new AnalyticsSocketAppender(name, layout, getFilter(), manager, isIgnoreExceptions(),
					!bufferedIo || immediateFlush, getAdvertise() ? getConfiguration().getAdvertiser() : null,
					getPropertyArray());
		}
	}

	@PluginBuilderFactory
	public static Builder newAnalyticsBuilder() {
		return new Builder();
	}
}
