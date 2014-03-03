package io.vertigo.dynamock.domain.fileinfo;

import io.vertigo.dynamo.domain.metamodel.DataStream;
import io.vertigo.dynamo.domain.metamodel.annotation.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.annotation.Field;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;
import io.vertigo.dynamo.plugins.database.connection.hibernate.DataStreamType;

import org.hibernate.annotations.Type;
import org.hibernate.annotations.TypeDef;
import org.hibernate.annotations.TypeDefs;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données KxFileInfo
 */
@javax.persistence.Entity
@javax.persistence.Table(name = "KX_FILE_INFO")
@TypeDefs(value = { @TypeDef(name = "DO_STREAM", typeClass = DataStreamType.class) })
@DtDefinition
public final class KxFileInfo implements DtObject {
	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long filId;
	private String fileName;
	private String mimeType;
	private Long length;
	private java.util.Date lastModified;
	private DataStream fileData;

	/**
	 * Champ : PRIMARY_KEY.
	 * récupère la valeur de la propriété 'Identifiant'. 
	 * @return Long filId <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_KX_FILE_INFO")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "sequence")
	@javax.persistence.Column(name = "FIL_ID")
	@Field(domain = "DO_IDENTIFIANT", type = "PRIMARY_KEY", notNull = true, label = "Identifiant")
	public final Long getFilId() {
		return filId;
	}

	/**
	 * Champ : PRIMARY_KEY.
	 * Définit la valeur de la propriété 'Identifiant'.
	 * @param filId Long <b>Obligatoire</b>
	 */
	public final void setFilId(final Long filId) {
		this.filId = filId;
	}

	/**
	 * Champ : DATA.
	 * récupère la valeur de la propriété 'Nom'. 
	 * @return String fileName <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "FILE_NAME")
	@Field(domain = "DO_STRING", notNull = true, label = "Nom")
	public final String getFileName() {
		return fileName;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Nom'.
	 * @param fileName String <b>Obligatoire</b>
	 */
	public final void setFileName(final String fileName) {
		this.fileName = fileName;
	}

	/**
	 * Champ : DATA.
	 * récupère la valeur de la propriété 'Type mime'. 
	 * @return String mimeType <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "MIME_TYPE")
	@Field(domain = "DO_STRING", notNull = true, label = "Type mime")
	public final String getMimeType() {
		return mimeType;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Type mime'.
	 * @param mimeType String <b>Obligatoire</b>
	 */
	public final void setMimeType(final String mimeType) {
		this.mimeType = mimeType;
	}

	/**
	 * Champ : DATA.
	 * récupère la valeur de la propriété 'Taille'. 
	 * @return Long length <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "LENGTH")
	@Field(domain = "DO_LONG", notNull = true, label = "Taille")
	public final Long getLength() {
		return length;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Taille'.
	 * @param length Long <b>Obligatoire</b>
	 */
	public final void setLength(final Long length) {
		this.length = length;
	}

	/**
	 * Champ : DATA.
	 * récupère la valeur de la propriété 'Date de derniére modification'. 
	 * @return java.util.Date lastModified <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "LAST_MODIFIED")
	@Field(domain = "DO_DATE", notNull = true, label = "Date de derniére modification")
	public final java.util.Date getLastModified() {
		return lastModified;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Date de derniére modification'.
	 * @param lastModified java.util.Date <b>Obligatoire</b>
	 */
	public final void setLastModified(final java.util.Date lastModified) {
		this.lastModified = lastModified;
	}

	/**
	 * Champ : DATA.
	 * récupère la valeur de la propriété 'data'. 
	 */
	@javax.persistence.Column(name = "FILE_DATA")
	@Type(type = "DO_STREAM")
	@Field(domain = "DO_STREAM", label = "data")
	public final DataStream getFileData() {
		return fileData;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'data'.
	 */
	public final void setFileData(final DataStream fileData) {
		this.fileData = fileData;
	}

	//Aucune Association déclarée

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
