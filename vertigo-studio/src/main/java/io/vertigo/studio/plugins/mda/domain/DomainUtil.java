package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.dynamo.domain.metamodel.DataType;
import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;

/**
 * Helper.
 * 
 * @author emangin
 */
public final class DomainUtil {
	/**
	 * Constructeur privé pour classe utilitaire.
	 */
	private DomainUtil() {
		//RAS
	}

	/**
	 * Construite le type java (sous forme de chaine de caractère) correspondant
	 * à un Domaine.
	 * @param domain DtDomain
	 * @return String
	 */
	public static String buildJavaType(final Domain domain) {
		final DataType dataType = domain.getDataType();
		if (dataType.isPrimitive()) {
			String javaType = dataType.getJavaClass().getName();

			//On simplifie l'écriture des types primitifs
			//java.lang.String => String
			if (javaType.startsWith("java.lang.")) {
				javaType = javaType.substring("java.lang.".length());
			}
			return javaType;
		}

		//Cas des DTO et DTC
		/* Il existe deux cas :
		 *  - Soit le domaine correspond à un objet précis (DT)
		 *  - Soit le domaine est un dTO ou DTC générique.
		 */
		final String dtoClassCanonicalName;
		if (domain.hasDtDefinition()) {
			//on récupère le DT correspondant au nom passé en paramètre
			final DtDefinition dtDefinition = domain.getDtDefinition();
			dtoClassCanonicalName = dtDefinition.getClassCanonicalName();
		} else {
			dtoClassCanonicalName = io.vertigo.dynamo.domain.model.DtObject.class.getCanonicalName();
		}
		switch (dataType) {
			case DtObject:
				return dtoClassCanonicalName;
			case DtList:
				return io.vertigo.dynamo.domain.model.DtList.class.getCanonicalName() + '<' + dtoClassCanonicalName + '>';
			default:
				throw new IllegalStateException();
		}
	}

}
