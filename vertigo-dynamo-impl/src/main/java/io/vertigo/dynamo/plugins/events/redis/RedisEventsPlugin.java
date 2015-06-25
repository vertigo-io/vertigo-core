package io.vertigo.dynamo.plugins.events.redis;

import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.event.Event;
import io.vertigo.commons.event.EventBuilder;
import io.vertigo.commons.event.EventChannel;
import io.vertigo.commons.event.EventListener;
import io.vertigo.commons.impl.event.EventPlugin;
import io.vertigo.dynamo.addons.connectors.redis.RedisConnector;
import io.vertigo.lang.Assertion;

import java.io.Serializable;
import java.util.UUID;

import javax.inject.Inject;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

/**
 * @author pchretien
 */
public final class RedisEventsPlugin implements EventPlugin {

	private final RedisConnector redisConnector;
	private final CodecManager codecManager;

	@Inject
	public RedisEventsPlugin(final RedisConnector redisConnector, final CodecManager codecManager) {
		Assertion.checkNotNull(redisConnector);
		Assertion.checkNotNull(codecManager);
		//-----
		this.redisConnector = redisConnector;
		this.codecManager = codecManager;
	}

	/** {@inheritDoc} */
	@Override
	public <P extends Serializable> void emit(final EventChannel<P> channel, final Event<P> event) {
		Assertion.checkNotNull(channel);
		Assertion.checkNotNull(event);
		//----
		try (final Jedis jedis = redisConnector.getResource()) {
			final Transaction tx = jedis.multi();
			final String base64Payload = encodeToBase64(event.getPayload());
			tx.hset("event:" + event.getUuid(), "payload", base64Payload);
			tx.lpush("events:" + channel + ":pending", "event:" + event.getUuid());
			tx.exec();
		}
	}

	private <P extends Serializable> String encodeToBase64(final P payload) {
		final byte[] binaryPayload = codecManager.getCompressedSerializationCodec().encode(payload);
		final String base64Payload = codecManager.getBase64Codec().encode(binaryPayload);
		return base64Payload;
	}

	/** {@inheritDoc} */
	@Override
	public <P extends Serializable> void register(final EventChannel<P> channel, final EventListener<P> eventsListener) {
		Assertion.checkNotNull(channel);
		Assertion.checkNotNull(eventsListener);
		//----
		new MyListener<>(channel, eventsListener, redisConnector, codecManager).start();
	}

	private static class MyListener<P extends Serializable> extends Thread {

		private final CodecManager codecManager;
		private final EventChannel<P> channel;
		private final RedisConnector redisConnector;
		private final EventListener<P> eventsListener;

		MyListener(final EventChannel<P> channel, final EventListener<P> eventsListener, final RedisConnector redisConnector, final CodecManager codecManager) {
			this.channel = channel;
			this.redisConnector = redisConnector;
			this.eventsListener = eventsListener;
			this.codecManager = codecManager;
		}

		/** {@inheritDoc} */
		@Override
		public void run() {
			while (!isInterrupted()) {
				try (final Jedis jedis = redisConnector.getResource()) {
					final String eventUUID = jedis.brpoplpush("events:" + channel + ":pending", "events:done", 10);
					final UUID uuid = UUID.fromString(eventUUID.substring("event:".length()));

					final P payload = decodeFromBase64(jedis.hget(eventUUID, "payload"));
					final Event event = new EventBuilder()
							.withUUID(uuid)
							.withPayload(payload)
							.build();
					eventsListener.onEvent(event);
				}
			}
		}

		private P decodeFromBase64(final String base64Payload) {
			final byte[] binaryPayload = codecManager.getBase64Codec().decode(base64Payload);
			final P payload = (P) codecManager.getCompressedSerializationCodec().decode(binaryPayload);
			return payload;
		}
	}
}
