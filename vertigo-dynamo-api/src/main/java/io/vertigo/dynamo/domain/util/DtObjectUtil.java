package io.vertigo.dynamo.domain.util;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.Dynamic;
import io.vertigo.dynamo.domain.metamodel.association.AssociationDefinition;
import io.vertigo.dynamo.domain.metamodel.association.AssociationSimpleDefinition;
import io.vertigo.dynamo.domain.metamodel.association.DtListURIForAssociation;
import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.dynamo.domain.model.DynaDtObject;
import io.vertigo.dynamo.domain.model.URI;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.DefinitionUtil;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.kernel.util.StringUtil;

/**
 * Utilitaire offrant des méthodes sur  DtObject.
 *
 * @author pchretien
 */
public final class DtObjectUtil {
	private static final String DT_DEFINITION_PREFIX = DefinitionUtil.getPrefix(DtDefinition.class);
	private static final char SEPARATOR = Definition.SEPARATOR;

	private DtObjectUtil() {
		//constructeur privé.
	}

	/**
	 * Crée une nouvelle instance de DtObject à partir du type spécifié.
	 *
	 * @return Nouveau DtObject
	 */
	public static DtObject createDtObject(final DtDefinition dtDefinition) {
		Assertion.checkNotNull(dtDefinition);
		//---------------------------------------------------------------------
		if (dtDefinition.isDynamic()) {
			return new DynaDtObject(dtDefinition);
		}
		//La création des DtObject n'est pas sécurisée
		return ClassUtil.newInstance(dtDefinition.getClassCanonicalName(), DtObject.class);
	}

	/**
	 * @return Valeur de la PK 
	 */
	public static Object getId(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		final DtDefinition dtDefinition = findDtDefinition(dto);
		final DtField pkField = dtDefinition.getIdField().get();
		return pkField.getDataAccessor().getValue(dto);
	}

	/**
	 * Récupération d'une URI de DTO.
	 * On récupère l'URI d'un DTO référencé par une association.
	 * Il est nécessaire que l'association soit simple. 
	 * Si l'association est multiple on ne récupère pas une URI mais une DtListURI, c'est à dire le pointeur vers une liste.  
	 * 
	 *  On recherche une URI correspondant à une association. 
	 *  Exemple : Une Commande possède un bénéficiaire.
	 *  Dans cetexemple on recherche l'URI du bénéficiaire à partir de l'objet commande. 

	 * @param associationDefinitionName Nom de la définition d'une association
	 * @param dto DtObject
	 * @return URI du DTO relié via l'association au dto passé en paramètre (Nullable)
	 */
	public static <D extends DtObject> URI<D> createURI(final DtObject dto, final String associationDefinitionName, final Class<D> dtoTargetClass) {
		Assertion.checkNotNull(associationDefinitionName);
		Assertion.checkNotNull(dto);
		Assertion.checkNotNull(dtoTargetClass);
		// ----------------------------------------------------------------------
		final AssociationDefinition associationDefinition = Home.getDefinitionSpace().resolve(associationDefinitionName, AssociationDefinition.class);
		final AssociationSimpleDefinition associationSimpleDefinition = associationDefinition.castAsAssociationSimpleDefinition();
		// 1. On recherche le nom du champ portant l'objet référencé (Exemple : personne)
		final DtDefinition reference = associationSimpleDefinition.getPrimaryAssociationNode().getDtDefinition();

		// 2. On calcule le nom de la fk.
		final DtField fkField = associationSimpleDefinition.getFKField();

		// 3. On calcule l'URI de la clé étrangère
		final Object value = fkField.getDataAccessor().getValue(dto);
		if (value == null) {
			return null;
		}
		return new URI<>(reference, value);
	}

	/**
	 * Récupération d'une URI de Collection à partir d'un dto
	 * @param dto DtObject
	 * @param associationDefinitionName Nom de l'association 
	 * @param roleName Nom du role
	 * @return URI de la collection référencée.
	 */
	public static DtListURIForAssociation createDtListURI(final DtObject dto, final String associationDefinitionName, final String roleName) {
		Assertion.checkNotNull(associationDefinitionName);
		Assertion.checkNotNull(roleName);
		Assertion.checkNotNull(dto);
		// ----------------------------------------------------------------------
		final AssociationDefinition associationDefinition = Home.getDefinitionSpace().resolve(associationDefinitionName, AssociationDefinition.class);
		return new DtListURIForAssociation(associationDefinition, createURI(dto), roleName);
	}

	private static <D extends DtObject> URI<D> createURI(final D dto) {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		final DtDefinition dtDefinition = findDtDefinition(dto);
		return new URI<>(dtDefinition, DtObjectUtil.getId(dto));
	}

	/**
	 * Représentation sous forme text d'un dtObject.
	 * @param dto dtObject
	 * @return Représentation sous forme text du dtObject.
	 */
	public static String toString(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		final StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append(findDtDefinition(dto).getName());
		stringBuilder.append('(');
		boolean first = true;
		for (final DtField dtField : findDtDefinition(dto).getFields()) {
			if (!first) {
				stringBuilder.append(", ");
			}
			stringBuilder.append(dtField.getName()).append('=');
			stringBuilder.append(dtField.getDataAccessor().getValue(dto));
			first = false;
		}
		stringBuilder.append(')');
		return stringBuilder.toString();
	}

	//=========================================================================
	//===========================STATIC========================================
	//=========================================================================
	public static DtDefinition findDtDefinition(final DtObject dto) {
		Assertion.checkNotNull(dto);
		//---------------------------------------------------------------------
		if (dto instanceof Dynamic) {
			return Dynamic.class.cast(dto).getDefinition();
		}
		return findDtDefinition(dto.getClass());
	}

	public static DtDefinition findDtDefinition(final Class<? extends DtObject> dtObjectClass) {
		Assertion.checkNotNull(dtObjectClass);
		//----------------------------------------------------------------------
		final String name = DT_DEFINITION_PREFIX + SEPARATOR + StringUtil.camelToConstCase(dtObjectClass.getSimpleName());
		return Home.getDefinitionSpace().resolve(name, DtDefinition.class);
	}
}
