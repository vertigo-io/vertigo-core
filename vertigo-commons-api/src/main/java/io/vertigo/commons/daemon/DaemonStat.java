package io.vertigo.commons.daemon;

/**
 *
 * @author pchretien
 */
public interface DaemonStat {

	/** Deamon execution status. */
	static enum Status {
		/** Waiting next execution. */
		pending,
		/** Currently running. */
		running;
	}

	/**
	 * @return Daemon definition
	 */
	DaemonDefinition getDaemonDefinition();

	/**
	 * @return Nb exec for daemon start
	 */
	long getCount();

	/**
	 * @return Nb successes for daemon start
	 */
	long getSuccesses();

	/**
	 * @return Nb failures for daemon start
	 */
	long getFailures();

	/**
	 * @return Current status
	 */
	Status getStatus();

	/**
	 * @return if last exec was a success
	 */
	boolean isLastExecSuccess();
}
