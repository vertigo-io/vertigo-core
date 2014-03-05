package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.kernel.lang.Assertion;

import java.util.List;

import freemarker.ext.beans.StringModel;
import freemarker.template.TemplateMethodModelEx;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

/**
 * Méthode Freemarker 'annotations'.
 * si config.setSharedVariable("annotations", new TemplateMethodAnnotations(parameters));
 * Pour DtDefinition, le second parametre permet de déterminer isAbstract (default : false)
 * Exemple : annotations(dtDefinition)
 * Exemple : annotations(dtDefinition, true)
 * Exemple : annotations(dtField)
 * Exemple : annotations(associationNode)
 * TemplateMethodModelEx : les params sont considérés comme des Objets.
 * 
 * @author  dchallas
 * @version $Id: TemplateMethodAnnotations.java,v 1.6 2014/02/03 17:48:13 pchretien Exp $
 */
public final class TemplateMethodAnnotations implements TemplateMethodModelEx {
	private final AnnotationWriter annotationWriter;

	/**
	 * Constructeur.
	 * @param generateJpaAnnotations Indique s'il on génère les annotations Jpa
	 */
	public TemplateMethodAnnotations(final boolean generateJpaAnnotations) {
		if (!generateJpaAnnotations) {
			annotationWriter = new AnnotationWriter();
		} else {
			annotationWriter = new JpaAnnotationWriter();
		}
	}

	/** {@inheritDoc}*/
	public TemplateModel exec(final List params) throws TemplateModelException {
		Assertion.checkArgument(!params.isEmpty(), "Un parametre de type [DtField, DtDefinition, AssociationNode] est obligatoire");
		//----------------------------------------------------------------------
		final Object type = ((StringModel) params.get(0)).getWrappedObject();

		if (type instanceof DtDefinition) {
			return new TemplateAnnotationLines(annotationWriter, (DtDefinition) type);
		} else if (type instanceof DtField) {
			final Object type2 = ((StringModel) params.get(1)).getWrappedObject();
			return new TemplateAnnotationLines(annotationWriter, (DtField) type, (DtDefinition) type2);
		} else if (type instanceof AssociationNode) {
			return new TemplateAnnotationLines(annotationWriter, (AssociationNode) type);
		} else {
			throw new TemplateModelException("Le type '" + type.getClass() + "' n''est pas dans la liste [DtField, DtDefinition, AssociationNode]");
		}
	}
}
