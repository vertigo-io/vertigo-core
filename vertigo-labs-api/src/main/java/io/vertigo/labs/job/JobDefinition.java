package io.vertigo.labs.job;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

/**
 * JobDefinition d�crit le travail qu'il faut ex�cuter. 
 * La tache � ex�cuter oit �tendre Runnable.
 * @author pchretien
 * @version $Id: JobDefinition.java,v 1.3 2013/10/22 10:55:30 pchretien Exp $
 */
@Prefix("JB")
public final class JobDefinition implements Definition {
	/** Nom de la d�finition. */
	private final String name;
	private final Class<? extends Runnable> jobClass;

	/**
	 * Constructeur
	 * @param name Nom de la d�finition
	 */
	public JobDefinition(final String name, final Class<? extends Runnable> jobClass) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(jobClass);
		//----------------------------------------------------------------------
		this.name = name;
		this.jobClass = jobClass;
	}

	/**
	 * Retourne la classe r�alisant l'impl�mentation de la tache.
	 * @return Classe r�alisant l'impl�mentation
	 */
	public Class<? extends Runnable> getJobClass() {
		return jobClass;
	}

	/** {@inheritDoc} */
	public String getName() {
		return name;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name;
	}
}
