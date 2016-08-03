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
package io.vertigo.dynamo.store.data.domain.fileinfo;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données VxFileInfo
 */
@javax.persistence.Entity
@javax.persistence.Table(name = "VX_FILE_INFO")
@org.hibernate.annotations.TypeDefs(value = { @org.hibernate.annotations.TypeDef(name = "DO_STREAM", typeClass = io.vertigo.dynamo.plugins.database.connection.hibernate.DataStreamType.class) })
public final class VxFileInfo implements Entity {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long filId;
	private String fileName;
	private String mimeType;
	private Long length;
	private java.util.Date lastModified;
	private io.vertigo.dynamo.domain.metamodel.DataStream fileData;

	/**
	 * Champ : ID.
	 * Récupère la valeur de la propriété 'Identifiant'.
	 * @return Long filId <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_VX_FILE_INFO")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "sequence")
	@javax.persistence.Column(name = "FIL_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "ID", required = true, label = "Identifiant")
	public Long getFilId() {
		return filId;
	}

	/**
	 * Champ : ID.
	 * Définit la valeur de la propriété 'Identifiant'.
	 * @param filId Long <b>Obligatoire</b>
	 */
	public void setFilId(final Long filId) {
		this.filId = filId;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Nom'.
	 * @return String fileName <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "FILE_NAME")
	@Field(domain = "DO_STRING", required = true, label = "Nom")
	public String getFileName() {
		return fileName;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Nom'.
	 * @param fileName String <b>Obligatoire</b>
	 */
	public void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Type mime'.
	 * @return String mimeType <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "MIME_TYPE")
	@Field(domain = "DO_STRING", required = true, label = "Type mime")
	public String getMimeType() {
		return mimeType;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Type mime'.
	 * @param mimeType String <b>Obligatoire</b>
	 */
	public void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Taille'.
	 * @return Long length <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "LENGTH")
	@Field(domain = "DO_LONG", required = true, label = "Taille")
	public Long getLength() {
		return length;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Taille'.
	 * @param length Long <b>Obligatoire</b>
	 */
	public void setLength(final Long length) {
		this.length = length;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Date de derniÃ¨re modification'.
	 * @return java.util.Date lastModified <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "LAST_MODIFIED")
	@Field(domain = "DO_DATE", required = true, label = "Date de derniÃ¨re modification")
	public java.util.Date getLastModified() {
		return lastModified;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Date de derniÃ¨re modification'.
	 * @param lastModified java.util.Date <b>Obligatoire</b>
	 */
	public void setLastModified(final java.util.Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'data'.
	 * @return io.vertigo.dynamo.domain.metamodel.DataStream fileData
	 */
	@javax.persistence.Column(name = "FILE_DATA")
	@org.hibernate.annotations.Type(type = "DO_STREAM")
	@Field(domain = "DO_STREAM", label = "data")
	public io.vertigo.dynamo.domain.metamodel.DataStream getFileData() {
		return fileData;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'data'.
	 * @param fileData io.vertigo.dynamo.domain.metamodel.DataStream
	 */
	public void setFileData(final io.vertigo.dynamo.domain.metamodel.DataStream fileData) {
		this.fileData = fileData;
	}

	//Aucune Association déclarée

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
