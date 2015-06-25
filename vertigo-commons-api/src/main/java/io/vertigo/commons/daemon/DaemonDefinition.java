package io.vertigo.commons.daemon;

import io.vertigo.core.spaces.definiton.Definition;
import io.vertigo.core.spaces.definiton.DefinitionPrefix;
import io.vertigo.lang.Assertion;

/**
 * Daemon's definition.
 *
 * @author TINGARGIOLA
 */
@DefinitionPrefix("DMN_")
public final class DaemonDefinition implements Definition {

	/** Nom du daemon. */
	private final String name;
	private final int periodInSeconds;
	private final Class<? extends Daemon> daemonClass;

	/**
	 * Constructeur.
	 *
	 * @param name Nom du Daemon (DMN_XXX)
	 * @param daemonClass Class du démon.
	 * @param periodInSeconds La période d'exécution du démon.
	 */
	public DaemonDefinition(final String name, final Class<? extends Daemon> daemonClass, final int periodInSeconds) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(daemonClass);
		Assertion.checkArgument(periodInSeconds > 0, "period {0} must be > 0", periodInSeconds);
		// -----
		this.name = name;
		this.daemonClass = daemonClass;
		this.periodInSeconds = periodInSeconds;
	}

	/** {@inheritDoc} */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * Give the value of daemonClass.
	 *
	 * @return DaemonClass.
	 */
	public Class<? extends Daemon> getDaemonClass() {
		return daemonClass;
	}

	/**
	 * Give the value of periodInSeconds.
	 *
	 * @return PeriodInSeconds.
	 */
	public int getPeriodInSeconds() {
		return periodInSeconds;
	}
}
