package io.vertigo.dynamo.domain.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.DefinitionReference;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * URI d'une DTC.
 *	
 * @author pchretien
 * @version $Id: DtListURI.java,v 1.4 2013/10/22 15:14:53 pchretien Exp $
 */
public abstract class DtListURI implements Serializable {
	/**
	 * Expression régulière vérifiée par les URN. 
	 */
	public static final Pattern REGEX_URN = Pattern.compile("[a-zA-Z0-9_:@$-]{5,80}");
	private static final long serialVersionUID = -1L;
	private final DefinitionReference<DtDefinition> dtDefinitionRef;

	/**
	 * URN de la ressource (Nom complet)
	 */
	private transient String urn;

	/**
	 * Constructeur.
	 * @param dtDefinition Definition de la ressource
	 */
	public DtListURI(final DtDefinition dtDefinition) {
		dtDefinitionRef = new DefinitionReference<>(dtDefinition);
	}

	/**
	 * @return Définition de la ressource.
	 */
	public DtDefinition getDtDefinition() {
		return dtDefinitionRef.get();
	}

	/** {@inheritDoc} */
	@Override
	public final int hashCode() {
		return toURN().hashCode();
	}

	/** {@inheritDoc} */
	@Override
	public final boolean equals(final Object o) {
		if (o instanceof DtListURI) {
			return ((DtListURI) o).toURN().equals(toURN());
		}
		return false;
	}

	/**
	 * Construit une URN à partir de l'URI.
	 * Une URN est la  représentation unique d'une URI sous forme de chaine de caractères.
	 * Cette chaine peut s'insérer telle que dans une URL en tant que paramètre 
	 * et ne contient donc aucun caractère spécial.
	 * Une URN respecte la regex exprimée ci dessus.
	 * @return URN de la ressource.
	 * @deprecated cette URN n'est pas déserializable, ne plus utiliser. 
	 */
	@Deprecated
	public final synchronized String toURN() {
		//synchronized car appellée par des traitements métiers (notament le broker)
		if (urn == null) {
			urn = DtListURICodec.writeURN(this);
			Assertion.checkArgument(REGEX_URN.matcher(urn).matches(), "urn {0} doit matcher le pattern {1}", urn, REGEX_URN);
		}
		return urn;
	}

	/** {@inheritDoc} */
	@Override
	public final String toString() {
		//on surcharge le toString car il est utilisé dans les logs d'erreur. et celui par défaut utilise le hashcode.
		return "urn[" + getClass().getName() + "]::" + toURN();
	}

	/**
	 * Codec de l'URI en URN.
	 */
	static final class DtListURICodec {
		private static final char D2A_SEPARATOR = '@';
		private static final String ALL_PREFIX = "ALL_";
		private static final String CRITERIA_PREFIX = "CRITERIA";

		static String writeURN(final DtListURI uri) {
			if (uri instanceof DtListURIAll) {
				return writeDtListURNAll(uri);
			} else if (uri instanceof DtListURIForAssociation) {
				return writeDtListURNForAssociation(DtListURIForAssociation.class.cast(uri));
			} else if (uri instanceof DtListURIForMasterData) {
				return writeDtListURNForMasterData(DtListURIForMasterData.class.cast(uri));
			} else if (uri instanceof DtListURIForCriteria) {
				return writeDtListURNForDtCriteria(DtListURIForCriteria.class.cast(uri));
			} else {
				throw new IllegalArgumentException("uri " + uri.getClass().getName() + " non serializable");
			}
		}

		/**
		 * Ecriture d'une URI sous forme d'une URN (chaine de caractères).
		 *
		 * @param uri URI à transcrire
		 * @return URN
		 */
		private static String writeDtListURNAll(final DtListURI uri) {
			return ALL_PREFIX + uri.getDtDefinition().getName();
		}

		/**
		 * Ecriture d'une URI sous forme d'une URN (chaine de caractères).
		 *
		 * @param uri URI à transcrire
		 * @return URN
		 */
		private static String writeDtListURNForAssociation(final DtListURIForAssociation uri) {
			return uri.getAssociationDefinition().getName() + D2A_SEPARATOR + uri.getRoleName() + D2A_SEPARATOR + uri.getSource().toURN();
		}

		/**
		 * Ecriture d'une URI sous forme d'une URN (chaine de caractères).
		 *
		 * @param uri URI à transcrire
		 * @return URN
		 */
		private static String writeDtListURNForMasterData(final DtListURIForMasterData uri) {
			if (uri.getCode() != null) {
				return uri.getDtDefinition().getName() + D2A_SEPARATOR + uri.getCode();
			}
			return uri.getDtDefinition().getName();
		}

		/**
		 * Ecriture d'une URI sous forme d'une URN (chaine de caractères).
		 * 
		 * @param uri URI à transcrire
		 * @return URN
		 */
		private static String writeDtListURNForDtCriteria(final DtListURIForCriteria<?> uri) {
			return CRITERIA_PREFIX + D2A_SEPARATOR + uri.getCriteria().hashCode();
		}
	}
}
