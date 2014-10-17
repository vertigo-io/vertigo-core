package io.vertigo.dynamock.domain.famille;

import io.vertigo.dynamo.domain.stereotype.DtDefinition;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données Famille
 */
@javax.persistence.Entity
@javax.persistence.Table (name = "FAMILLE")
@DtDefinition
public final class Famille implements DtObject {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long famId;
	private String libelle;

	/**
	 * Champ : PRIMARY_KEY.
	 * Récupère la valeur de la propriété 'identifiant de la famille'. 
	 * @return Long famId <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_FAMILLE")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "sequence")
	@javax.persistence.Column(name = "FAM_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "PRIMARY_KEY", notNull = true, label = "identifiant de la famille")
	public Long getFamId() {
		return famId;
	}

	/**
	 * Champ : PRIMARY_KEY.
	 * Définit la valeur de la propriété 'identifiant de la famille'.
	 * @param famId Long <b>Obligatoire</b>
	 */
	public void setFamId(final Long famId) {
		this.famId = famId;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Libelle'. 
	 * @return String libelle 
	 */
	@javax.persistence.Column(name = "LIBELLE")
	@Field(domain = "DO_STRING", label = "Libelle")
	public String getLibelle() {
		return libelle;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Libelle'.
	 * @param libelle String 
	 */
	public void setLibelle(final String libelle) {
		this.libelle = libelle;
	}

	/**
	 * Champ : COMPUTED.
	 * Récupère la valeur de la propriété calculée 'Libelle'. 
	 * @return String description 
	 */
	@javax.persistence.Column(name = "DESCRIPTION")
	@javax.persistence.Transient
	@Field(domain = "DO_LIBELLE_LONG", type = "COMPUTED", persistent = false, label = "Libelle")
	public String getDescription() {
		final StringBuilder builder = new StringBuilder();
        builder.append(getLibelle());
        builder.append('[');
        builder.append(getFamId());
        builder.append(']');
        return builder.toString();
	}

	//Aucune Association déclarée

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
