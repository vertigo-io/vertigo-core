package io.vertigo.commons.daemon;

/**
 *
 * @author pchretien
 */
public interface DaemonStat {

	/** Daemon execution status. */
	enum Status {
		/** Waiting next execution. */
		pending,
		/** Currently running. */
		running;
	}

	/**
	 * @return Daemon name
	 */
	String getDaemonName();

	/**
	 * @return Daemon name
	 */
	Class<? extends Daemon> getDaemonClass();

	/**
	 * @return Daemon period
	 */
	int getDaemonPeriodInSecond();

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
