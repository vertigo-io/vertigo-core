package io.vertigo.studio.plugins.mda.task.test;

import java.util.Arrays;
import java.util.List;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.model.DtList;

/**
 * Représente une expression de valeur factice.
 * @author sezratty
 */
public class DumExpression {

	private final List<String> imports;
	private final String rawValue;
	private final boolean isRequired;

	/**
	 * Créé une expression factice.
	 * @param domain Domaine.
	 * @param isRequired Si champ obligatoire.
	 * @return Expression factice.
	 */
	public static DumExpression create(final Domain domain, final boolean isRequired) {
		if (domain.getScope().isPrimitive()) {
			switch (domain.getDataType()) {
				case Boolean:
					return new DumExpression("dum().booleen()", isRequired);
				case Long:
					return new DumExpression("dum().id()", isRequired);
				case Integer:
					return new DumExpression("dum().entier()", isRequired);
				case BigDecimal:
					return new DumExpression("dum().decimal()", isRequired);
				case Double:
					return new DumExpression("dum().montant()", isRequired);
				case String:
					return new DumExpression("dum().code()", isRequired);
				case Date:
					return new DumExpression("dum().date()", isRequired);
				case DataStream:
					return new DumExpression("null", false); // TODO dummy stream
				default:
					return null;
			}

		}

		if (domain.getScope().isDataObject() || domain.getScope().isValueObject()) {
			if (domain.isMultiple()) {
				final DtDefinition dtcDef = domain.getDtDefinition();
				return new DumExpression(
						"dum().dumList(" + dtcDef.getClassSimpleName() + ".class)",
						isRequired,
						dtcDef.getClassCanonicalName(),
						DtList.class.getCanonicalName());
			}
			final DtDefinition dtoDef = domain.getDtDefinition();
			return new DumExpression(
					"dum().dum(" + dtoDef.getClassSimpleName() + ".class)",
					isRequired,
					dtoDef.getClassCanonicalName());
		}

		return null;
	}

	private DumExpression(final String rawValue, final boolean isRequired, final String... imports) {
		this.rawValue = rawValue;
		this.isRequired = isRequired;
		this.imports = Arrays.asList(imports);
	}

	/**
	 * @return Expression de la valeur factice.
	 */
	public String getValue() {
		return isRequired ? rawValue : "Optional.of(" + rawValue + ")";
	}

	/**
	 * @return Liste des imports pour l'expression.
	 */
	public List<String> getImports() {
		return imports;
	}
}
