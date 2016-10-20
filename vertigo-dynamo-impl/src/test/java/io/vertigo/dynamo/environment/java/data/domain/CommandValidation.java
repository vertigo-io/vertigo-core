/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertigo.dynamo.environment.java.data.domain;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données CommandValidation
 */
@javax.persistence.Entity
@javax.persistence.Table(name = "COMMAND_VALIDATION")
public final class CommandValidation implements Entity {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long cvaId;
	private String signerName;
	private Long cmdId;
	private io.vertigo.dynamo.environment.java.data.domain.Command command;

	/** {@inheritDoc} */
	@Override
	@javax.persistence.Transient
	public URI<CommandValidation> getURI() {
		return DtObjectUtil.createURI(this);
	}

	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'id'.
	 * @return Long cvaId <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_COMMAND_VALIDATION")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.AUTO, generator = "sequence")
	@javax.persistence.Column(name = "CVA_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "ID", required = true, label = "id")
	public Long getCvaId() {
		return cvaId;
	}

	/**
	 * Champ : ID.
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
	@Field(domain = "DO_FULL_TEXT", required = true, label = "Signer name")
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
	@io.vertigo.dynamo.domain.stereotype.Association(name = "A_CMD_CVA", fkFieldName = "CMD_ID", primaryDtDefinitionName = "DT_COMMAND", primaryIsNavigable = true, primaryRole = "Command", primaryLabel = "Command", primaryMultiplicity = "0..1", foreignDtDefinitionName = "DT_COMMAND_VALIDATION", foreignIsNavigable = false, foreignRole = "CommandValidation", foreignLabel = "Command validation", foreignMultiplicity = "0..*")
	public io.vertigo.dynamo.environment.java.data.domain.Command getCommand() {
		final io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.domain.Command> fkURI = getCommandURI();
		if (fkURI == null) {
			return null;
		}
		//On est toujours dans un mode lazy. On s'assure cependant que l'objet associé n'a pas changé
		if (command != null) {
			// On s'assure que l'objet correspond à la bonne clé
			if (!fkURI.equals(command.getURI())) {
				command = null;
			}
		}
		if (command == null) {
			command = io.vertigo.app.Home.getApp().getComponentSpace().resolve(io.vertigo.dynamo.store.StoreManager.class).getDataStore().readOne(fkURI);
		}
		return command;
	}

	/**
	 * Retourne l'URI: Command.
	 * @return URI de l'association
	 */
	@javax.persistence.Transient
	@io.vertigo.dynamo.domain.stereotype.Association(name = "A_CMD_CVA", fkFieldName = "CMD_ID", primaryDtDefinitionName = "DT_COMMAND", primaryIsNavigable = true, primaryRole = "Command", primaryLabel = "Command", primaryMultiplicity = "0..1", foreignDtDefinitionName = "DT_COMMAND_VALIDATION", foreignIsNavigable = false, foreignRole = "CommandValidation", foreignLabel = "Command validation", foreignMultiplicity = "0..*")
	public io.vertigo.dynamo.domain.model.URI<io.vertigo.dynamo.environment.java.data.domain.Command> getCommandURI() {
		return io.vertigo.dynamo.domain.util.DtObjectUtil.createURI(this, "A_CMD_CVA", io.vertigo.dynamo.environment.java.data.domain.Command.class);
	}

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
