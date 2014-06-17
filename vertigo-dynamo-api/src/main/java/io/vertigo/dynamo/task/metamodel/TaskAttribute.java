package io.vertigo.dynamo.task.metamodel;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.kernel.lang.Assertion;

/**
 * Attribut d'une tache.
 * Il s'agit soit :
 *  - d'un type primitif
 *  - d'un type complexe : DTO ou DTC
 * Dans tous les cas il s'agit d'un {@link io.vertigo.dynamo.domain.metamodel.Domain}.
 * 
 * Le paramètre peut être :
 * 
 *  - en entrée, ou en sortie
 *  - obligatoire ou facultatif
 *
 * @author  fconstantin, pchretien
 */
public final class TaskAttribute {
	/** Nom de l'attribut. */
	private final String name;

	/**
	 * Sens de l'attribut IN
	 * Sens de l'attribut OUT = !IN
	 */
	private final boolean in;

	/** Domaine de l'attribut. */
	private final Domain domain;

	/** Attribut obligatoire (not_nul) ou non. */
	private final boolean notNull;

	/**
	 * Constructeur
	 *
	 * @param attributeName Nom de l'attribut
	 * @param domain Domaine de l'attribut
	 * @param notNull Null ?
	 * @param in in=SET ; !in= GET
	 */
	TaskAttribute(final String attributeName, final Domain domain, final boolean notNull, final boolean in) {
		Assertion.checkNotNull(attributeName);
		Assertion.checkNotNull(domain);
		//----------------------------------------------------------------------
		name = attributeName;
		this.domain = domain;
		this.notNull = notNull;
		this.in = in;
	}

	/**
	 * @return Nom de l'attribut.
	 */
	public String getName() {
		return name;
	}

	/**
	 * Retourne si la propriété est non null
	 * (Obligatoire en entrée ou en sortie selon le paramètre inout).
	 *
	 * @return Si la propriété est non null
	 */
	public boolean isNotNull() {
		return notNull;
	}

	/**
	 * VRAI si l'attribut est entrant
	 * FAUX si l'attribut est créé par la tache donc sortant.
	 * <br/>
	 * Conformément à java, les objets complexes peuvent être modifiés par la tache.
	 * Par exemple, tel DTO se verra doté d'une clé primaire lors de son premier
	 * enregistrement en base de données.
	 * @return Si l'attribut est entrant.
	 */
	public boolean isIn() {
		return in;
	}

	/**
	 * @return Domain Domaine de l'attribut
	 */
	public Domain getDomain() {
		return domain;
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return name + " : domain=" + domain + ", in=" + in + ", notnull=" + notNull;
	}
}
