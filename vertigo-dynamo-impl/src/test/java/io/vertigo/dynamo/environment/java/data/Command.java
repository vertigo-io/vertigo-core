package io.vertigo.dynamo.environment.java.data;

import io.vertigo.dynamo.domain.model.KeyConcept;
import io.vertigo.dynamo.domain.stereotype.DtDefinition;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données Command
 */
@javax.persistence.Entity
@javax.persistence.Table(name = "COMMAND")
@DtDefinition
public final class Command implements KeyConcept {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long cmdId;
	private Long ctyId;
	private Long citId;
	private io.vertigo.dynamo.environment.java.data.CommandType commandType;
	private io.vertigo.dynamo.environment.java.data.City city;

	/**
	 * Champ : PRIMARY_KEY.
	 * Récupère la valeur de la propriété 'id'.
	 * @return Long cmdId <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_COMMAND")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO, generator = "sequence")
	@javax.persistence.Column(name = "CMD_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "PRIMARY_KEY", notNull = true, label = "id")
	public Long getCmdId() {
		return cmdId;
	}

	/**
	 * Champ : PRIMARY_KEY.
	 * Définit la valeur de la propriété 'id'.
	 * @param cmdId Long <b>Obligatoire</b>
	 */
	public void setCmdId(final Long cmdId) {
		this.cmdId = cmdId;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'Command type'.
	 * @return Long ctyId
	 */
	@javax.persistence.Column(name = "CTY_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "FOREIGN_KEY", label = "Command type")
	public Long getCtyId() {
		return ctyId;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'Command type'.
	 * @param ctyId Long
	 */
	public void setCtyId(final Long ctyId) {
		this.ctyId = ctyId;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'City'.
	 * @return Long citId
	 */
	@javax.persistence.Column(name = "CIT_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "FOREIGN_KEY", label = "City")
	public Long getCitId() {
		return citId;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'City'.
	 * @param citId Long
	 */
	public void setCitId(final Long citId) {
		this.citId = citId;
	}

	// Association : Attachment non navigable

	// Association : Command validation non navigable
	/**
	 * Association : Command type.
	 * @return io.vertigo.dynamo.environment.java.data.CommandType
	 */
	@javax.persistence.Transient
	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_CTY_CMD",
			fkFieldName = "CTY_ID",
			primaryDtDefinitionName = "DT_COMMAND_TYPE",
			primaryIsNavigable = true,
			primaryRole = "CommandType",
			primaryLabel = "Command type",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_COMMAND",
			foreignIsNavigable = false,
			foreignRole = "Command",
			foreignLabel = "Command",
			foreignMultiplicity = "0..*"
			)
			public io.vertigo.dynamo.environment.java.data.CommandType getCommandType() {
		final io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.CommandType> fkURI = getCommandTypeURI();
		if (fkURI == null) {
			return null;
		}
		//On est toujours dans un mode lazy. On s'assure cependant que l'objet associé n'a pas changé
		if (commandType != null) {
			// On s'assure que l'objet correspond à la bonne clé
			final io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.CommandType> uri;
			uri = new io.vertigo.dynamo.domain.model.URI<>(io.vertigo.dynamo.domain.util.DtObjectUtil.findDtDefinition(commandType), io.vertigo.dynamo.domain.util.DtObjectUtil.getId(commandType));
			if (!fkURI.toURN().equals(uri.toURN())) {
				commandType = null;
			}
		}
		if (commandType == null) {
			commandType = io.vertigo.core.Home.getComponentSpace().resolve(io.vertigo.dynamo.persistence.PersistenceManager.class).getDataStore().get(fkURI);
		}
		return commandType;
	}

	/**
	 * Retourne l'URI: Command type.
	 * @return URI de l'association
	 */
	@javax.persistence.Transient
	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_CTY_CMD",
			fkFieldName = "CTY_ID",
			primaryDtDefinitionName = "DT_COMMAND_TYPE",
			primaryIsNavigable = true,
			primaryRole = "CommandType",
			primaryLabel = "Command type",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_COMMAND",
			foreignIsNavigable = false,
			foreignRole = "Command",
			foreignLabel = "Command",
			foreignMultiplicity = "0..*"
			)
			public io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.CommandType> getCommandTypeURI() {
		return io.vertigo.dynamo.domain.util.DtObjectUtil.createURI(this, "A_CTY_CMD", io.vertigo.dynamo.environment.java.data.CommandType.class);
	}

	/**
	 * Association : City.
	 * @return io.vertigo.dynamo.environment.java.data.City
	 */
	@javax.persistence.Transient
	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_CIT_CMD",
			fkFieldName = "CIT_ID",
			primaryDtDefinitionName = "DT_CITY",
			primaryIsNavigable = true,
			primaryRole = "City",
			primaryLabel = "City",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_COMMAND",
			foreignIsNavigable = false,
			foreignRole = "Command",
			foreignLabel = "Command",
			foreignMultiplicity = "0..*"
			)
			public io.vertigo.dynamo.environment.java.data.City getCity() {
		final io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.City> fkURI = getCityURI();
		if (fkURI == null) {
			return null;
		}
		//On est toujours dans un mode lazy. On s'assure cependant que l'objet associé n'a pas changé
		if (city != null) {
			// On s'assure que l'objet correspond à la bonne clé
			final io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.City> uri;
			uri = new io.vertigo.dynamo.domain.model.URI<>(io.vertigo.dynamo.domain.util.DtObjectUtil.findDtDefinition(city), io.vertigo.dynamo.domain.util.DtObjectUtil.getId(city));
			if (!fkURI.toURN().equals(uri.toURN())) {
				city = null;
			}
		}
		if (city == null) {
			city = io.vertigo.core.Home.getComponentSpace().resolve(io.vertigo.dynamo.persistence.PersistenceManager.class).getDataStore().get(fkURI);
		}
		return city;
	}

	/**
	 * Retourne l'URI: City.
	 * @return URI de l'association
	 */
	@javax.persistence.Transient
	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_CIT_CMD",
			fkFieldName = "CIT_ID",
			primaryDtDefinitionName = "DT_CITY",
			primaryIsNavigable = true,
			primaryRole = "City",
			primaryLabel = "City",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_COMMAND",
			foreignIsNavigable = false,
			foreignRole = "Command",
			foreignLabel = "Command",
			foreignMultiplicity = "0..*"
			)
			public io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.City> getCityURI() {
		return io.vertigo.dynamo.domain.util.DtObjectUtil.createURI(this, "A_CIT_CMD", io.vertigo.dynamo.environment.java.data.City.class);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
