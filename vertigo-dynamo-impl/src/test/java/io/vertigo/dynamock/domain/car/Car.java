package io.vertigo.dynamock.domain.car;

import io.vertigo.dynamo.domain.stereotype.DtDefinition;
import io.vertigo.dynamo.domain.stereotype.Field;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.util.DtObjectUtil;

/**
 * Attention cette classe est générée automatiquement !
 * Objet de données Car
 */
@javax.persistence.Entity
@javax.persistence.Table (name = "CAR")
@DtDefinition
public final class Car implements DtObject {

	/** SerialVersionUID. */
	private static final long serialVersionUID = 1L;

	private Long id;
	private String make;
	private String model;
	private String description;
	private Integer year;
	private Integer kilo;
	private Integer price;
	private String motorType;

	/**
	 * Champ : PRIMARY_KEY.
	 * Récupère la valeur de la propriété 'identifiant de la voiture'. 
	 * @return Long id <b>Obligatoire</b>
	 */
	@javax.persistence.Id
	@javax.persistence.SequenceGenerator(name = "sequence", sequenceName = "SEQ_CAR")
	@javax.persistence.GeneratedValue(strategy = javax.persistence.GenerationType.SEQUENCE, generator = "sequence")
	@javax.persistence.Column(name = "ID")
	@Field(domain = "DO_IDENTIFIANT", type = "PRIMARY_KEY", notNull = true, label = "identifiant de la voiture")
	public Long getId() {
		return id;
	}

	/**
	 * Champ : PRIMARY_KEY.
	 * Définit la valeur de la propriété 'identifiant de la voiture'.
	 * @param id Long <b>Obligatoire</b>
	 */
	public void setId(final Long id) {
		this.id = id;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Constructeur'. 
	 * @return String make <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "MAKE")
	@Field(domain = "DO_KEYWORD", notNull = true, label = "Constructeur")
	public String getMake() {
		return make;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Constructeur'.
	 * @param make String <b>Obligatoire</b>
	 */
	public void setMake(final String make) {
		this.make = make;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'ModÃ¨le'. 
	 * @return String model <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "MODEL")
	@Field(domain = "DO_FULL_TEXT", notNull = true, label = "ModÃ¨le")
	public String getModel() {
		return model;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'ModÃ¨le'.
	 * @param model String <b>Obligatoire</b>
	 */
	public void setModel(final String model) {
		this.model = model;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Descriptif'. 
	 * @return String description <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "DESCRIPTION")
	@Field(domain = "DO_FULL_TEXT", notNull = true, label = "Descriptif")
	public String getDescription() {
		return description;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Descriptif'.
	 * @param description String <b>Obligatoire</b>
	 */
	public void setDescription(final String description) {
		this.description = description;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'AnnÃ©e'. 
	 * @return Integer year <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "YEAR")
	@Field(domain = "DO_INTEGER", notNull = true, label = "AnnÃ©e")
	public Integer getYear() {
		return year;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'AnnÃ©e'.
	 * @param year Integer <b>Obligatoire</b>
	 */
	public void setYear(final Integer year) {
		this.year = year;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'KilomÃ©trage'. 
	 * @return Integer kilo <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "KILO")
	@Field(domain = "DO_INTEGER", notNull = true, label = "KilomÃ©trage")
	public Integer getKilo() {
		return kilo;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'KilomÃ©trage'.
	 * @param kilo Integer <b>Obligatoire</b>
	 */
	public void setKilo(final Integer kilo) {
		this.kilo = kilo;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Prix'. 
	 * @return Integer price <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "PRICE")
	@Field(domain = "DO_INTEGER", notNull = true, label = "Prix")
	public Integer getPrice() {
		return price;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Prix'.
	 * @param price Integer <b>Obligatoire</b>
	 */
	public void setPrice(final Integer price) {
		this.price = price;
	}

	/**
	 * Champ : DATA.
	 * Récupère la valeur de la propriété 'Type de moteur'. 
	 * @return String motorType <b>Obligatoire</b>
	 */
	@javax.persistence.Column(name = "MOTOR_TYPE")
	@Field(domain = "DO_KEYWORD", notNull = true, label = "Type de moteur")
	public String getMotorType() {
		return motorType;
	}

	/**
	 * Champ : DATA.
	 * Définit la valeur de la propriété 'Type de moteur'.
	 * @param motorType String <b>Obligatoire</b>
	 */
	public void setMotorType(final String motorType) {
		this.motorType = motorType;
	}

	//Aucune Association déclarée

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return DtObjectUtil.toString(this);
	}
}
