package io.vertigo.dynamo.domain.metamodel;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.kernel.lang.Assertion;

import java.util.Date;

/**
 * Types.
 * On distingue :
 * - les types primitifs,
 * - types complexes.
 * 
 * Les types complexes permettent de créer des objets composites.
 * Ils unifient le socle autour de la notion clé de domaine.
 *
 * @author  pchretien
 */
public enum KDataType {
	/** Integer. */
	Integer(Integer.class, true),
	/** Double. */
	Double(Double.class, true),
	/** Boolean. */
	Boolean(Boolean.class, true),
	/** String. */
	String(String.class, true),
	/** Date. */
	Date(Date.class, true),
	/** BigDecimal. */
	BigDecimal(java.math.BigDecimal.class, true),
	/** Long. */
	Long(Long.class, true),
	/** DataStream. */
	DataStream(DataStream.class, true),
	/** DtObject. */
	DtObject(DtObject.class, false),
	/** DtList. */
	DtList(DtList.class, false);

	/**
	 * S'agit-il d'un type primitif
	 */
	private final boolean primitive;

	/**
	 * Classe java que le Type encapsule.
	 */
	private final Class<?> javaClass;

	/**
	 * Constructeur.
	 *
	 * @param javaClass Classe java encapsulée
	 * @param primitive Si il s'agit d'un type primitif (sinon composite)
	 */
	private KDataType(final Class<?> javaClass, final boolean primitive) {
		Assertion.checkNotNull(javaClass);
		//----------------------------------------------------------------------
		//Le nom est égal au type sous forme de String
		this.javaClass = javaClass;
		this.primitive = primitive;
	}

	/**
	 * Teste si la valeur passée en paramétre est est conforme au type.
	 * Lance une exception avec message adequat si pb.
	 * @param value Valeur é tester
	 */
	public void checkValue(final Object value) {
		//Il suffit de vérifier que la valeur passée est une instance de la classe java définie pour le type Dynamo.
		//Le test doit être effectué car le cast est non fiable par les generics
		if (value != null && !javaClass.isInstance(value)) {
			throw new ClassCastException("Valeur " + value + " ne correspond pas au type :" + this);
		}
	}

	/**
	 * @return Classe java encapsulé/wrappée par le type
	 */
	public Class<?> getJavaClass() {
		return javaClass;
	}

	/**
	 * Vérifie l'égalité de deux valeurs non nulles.
	 *
	 * @param a Valeur 1
	 * @param b Valeur 2
	 * @return Si égales
	 */
	public boolean equals(final Object a, final Object b) {
		if (a == null && b == null) {
			//Si les deux objets sont null alors on considère qu'ils sont égaux
			return true;
		}

		if (a == null || b == null) {
			//Si un seul des objets est null alors ils sont différents
			return false;
		}
		//A partir de maintenant a et b sont non null
		return a.equals(b);
	}

	/**
	 * Il extiste deux types de types primitifs
	 * - les types simples ou primitifs (Integer, Long, String...),
	 * - les types composites (DtObject et DtList).
	 * @return boolean S'il s'agit d'un type primitif de la grammaire
	 */
	public boolean isPrimitive() {
		return primitive;
	}
}
