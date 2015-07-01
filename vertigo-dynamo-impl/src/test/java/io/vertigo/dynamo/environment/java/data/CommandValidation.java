package io.vertigo.dynamo.environment.java.data;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.stereotype.DtDefinition;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données CommandValidation
 */
@javax.persistence.Entity
@javax.persistence.Table(name = "COMMAND_VALIDATION")
@DtDefinition
public final class CommandValidation implements DtObject {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long cvaId;
	private String signerName;
	private Long cmdId;
	private io.vertigo.dynamo.environment.java.data.Command command;

	/**
	 * Champ : PRIMARY_KEY.
	 * Récupère la valeur de la propriété 'id'.
	 * @return Long cvaId <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_COMMAND_VALIDATION")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO, generator = "sequence")
	@javax.persistence.Column(name = "CVA_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "PRIMARY_KEY", notNull = true, label = "id")
	public Long getCvaId() {
		return cvaId;
	}

	/**
	 * Champ : PRIMARY_KEY.
	 * Définit la valeur de la propriété 'id'.
	 * @param cvaId Long <b>Obligatoire</b>
	 */
	public void setCvaId(final Long cvaId) {
		this.cvaId = cvaId;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Signer name'.
	 * @return String signerName <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "SIGNER_NAME")
	@Field(domain = "DO_FULL_TEXT", notNull = true, label = "Signer name")
	public String getSignerName() {
		return signerName;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Signer name'.
	 * @param signerName String <b>Obligatoire</b>
	 */
	public void setSignerName(final String signerName) {
		this.signerName = signerName;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Récupère la valeur de la propriété 'Command'.
	 * @return Long cmdId
	 */
	@javax.persistence.Column(name = "CMD_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "FOREIGN_KEY", label = "Command")
	public Long getCmdId() {
		return cmdId;
	}

	/**
	 * Champ : FOREIGN_KEY.
	 * Définit la valeur de la propriété 'Command'.
	 * @param cmdId Long
	 */
	public void setCmdId(final Long cmdId) {
		this.cmdId = cmdId;
	}

	/**
	 * Association : Command.
	 * @return io.vertigo.dynamo.environment.java.data.Command
	 */
	@javax.persistence.Transient
	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_CMD_CVA",
			fkFieldName = "CMD_ID",
			primaryDtDefinitionName = "DT_COMMAND",
			primaryIsNavigable = true,
			primaryRole = "Command",
			primaryLabel = "Command",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_COMMAND_VALIDATION",
			foreignIsNavigable = false,
			foreignRole = "CommandValidation",
			foreignLabel = "Command validation",
			foreignMultiplicity = "0..*"
			)
			public io.vertigo.dynamo.environment.java.data.Command getCommand() {
		final io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.Command> fkURI = getCommandURI();
		if (fkURI == null) {
			return null;
		}
		//On est toujours dans un mode lazy. On s'assure cependant que l'objet associé n'a pas changé
		if (command != null) {
			// On s'assure que l'objet correspond à la bonne clé
			final io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.Command> uri;
			uri = new io.vertigo.dynamo.domain.model.URI<>(io.vertigo.dynamo.domain.util.DtObjectUtil.findDtDefinition(command), io.vertigo.dynamo.domain.util.DtObjectUtil.getId(command));
			if (!fkURI.toURN().equals(uri.toURN())) {
				command = null;
			}
		}
		if (command == null) {
			command = io.vertigo.core.Home.getComponentSpace().resolve(io.vertigo.dynamo.store.StoreManager.class).getDataStore().get(fkURI);
		}
		return command;
	}

	/**
	 * Retourne l'URI: Command.
	 * @return URI de l'association
	 */
	@javax.persistence.Transient
	@io.vertigo.dynamo.domain.stereotype.Association(
			name = "A_CMD_CVA",
			fkFieldName = "CMD_ID",
			primaryDtDefinitionName = "DT_COMMAND",
			primaryIsNavigable = true,
			primaryRole = "Command",
			primaryLabel = "Command",
			primaryMultiplicity = "0..1",
			foreignDtDefinitionName = "DT_COMMAND_VALIDATION",
			foreignIsNavigable = false,
			foreignRole = "CommandValidation",
			foreignLabel = "Command validation",
			foreignMultiplicity = "0..*"
			)
			public io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.Command> getCommandURI() {
		return io.vertigo.dynamo.domain.util.DtObjectUtil.createURI(this, "A_CMD_CVA", io.vertigo.dynamo.environment.java.data.Command.class);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
