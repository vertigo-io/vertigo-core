package io.vertigo.commons.daemon;

/**
 * 
 * @author pchretien
 */
public interface DaemonStat {
	DaemonDefinition getDaemonDefinition();

	public static enum Status {
		pending,
		running;
	}

	long getCount();

	long getSuccesses();

	long getFailures();

	Status getStatus();
}
