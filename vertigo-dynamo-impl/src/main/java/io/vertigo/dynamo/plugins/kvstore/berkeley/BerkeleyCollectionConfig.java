package io.vertigo.dynamo.plugins.kvstore.berkeley;

/**
 * Collection configuration.
 * @author npiedeloup
 */
public final class BerkeleyCollectionConfig {

	private final String collectionName;
	private final long timeToLiveSeconds;
	private final boolean inMemory;

	/**
	 * Constructor.
	 * @param collectionName Collection name
	 * @param timeToLiveSeconds Elements time to live in second
	 * @param inMemory Collection store in memory
	 */
	BerkeleyCollectionConfig(final String collectionName, final long timeToLiveSeconds, final boolean inMemory) {
		this.collectionName = collectionName;
		this.timeToLiveSeconds = timeToLiveSeconds;
		this.inMemory = inMemory;
	}

	/**
	 * @return collectionName
	 */
	public final String getCollectionName() {
		return collectionName;
	}

	/**
	 * @return timeToLiveSeconds
	 */
	public final long getTimeToLiveSeconds() {
		return timeToLiveSeconds;
	}

	/**
	 * @return inMemory
	 */
	public final boolean isInMemory() {
		return inMemory;
	}
}
