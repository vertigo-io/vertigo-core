package io.vertigo.studio.plugins.mda.search;

import io.vertigo.dynamo.domain.metamodel.Domain;
import io.vertigo.dynamo.domain.metamodel.DtProperty;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;

import java.util.List;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateMethodModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Récuperation du type de l'index du domain.
 * 
 * @author  dchallas
 */
public final class TemplateMethodIndexType implements TemplateMethodModel {

	/** {@inheritDoc}*/
	public TemplateModel exec(final List params) throws TemplateModelException {
		final String domainUrn = (String) params.get(0);
		final Domain domain = Home.getDefinitionSpace().resolve(domainUrn, Domain.class);

		// On peut préciser pour chaque domaine le type d'indexation
		String fieldType = domain.getProperties().getValue(DtProperty.INDEX_TYPE);

		// Calcul automatique  par default.
		switch (domain.getDataType()) {
			case Boolean:
				// native
			case Date:
				// native
			case Double:
				// native
			case Integer:
				// native
			case Long:
				// native
				if (fieldType == null) {
					fieldType = domain.getDataType().toString().toLowerCase();
				}
				break;
			case String:
				if (fieldType == null) {
					throw new IllegalArgumentException("## Précisez la valeur \"indexType\" dans le domain [" + domain + "].");
				}
				break;
			case DataStream:
				// IllegalArgumentException
			case BigDecimal:
				// IllegalArgumentException
			case DtObject:
				// IllegalArgumentException
			case DtList:
				// IllegalArgumentException
			default:
				// IllegalArgumentException
				throw new IllegalArgumentException("Type de donnée non pris en charge pour l'indexation [" + domain + "].");

		}
		Assertion.checkNotNull(fieldType);
		return new SimpleScalar(fieldType);

	}
}
