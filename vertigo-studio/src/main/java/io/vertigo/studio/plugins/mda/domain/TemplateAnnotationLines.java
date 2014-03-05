package io.vertigo.studio.plugins.mda.domain;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.dynamo.domain.metamodel.DtField;
import io.vertigo.dynamo.domain.metamodel.association.AssociationNode;
import io.vertigo.kernel.lang.Assertion;

import java.util.List;

import freemarker.template.SimpleScalar;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateSequenceModel;

/**
 * Permet de construire les lignes décritants l'annotation.
 * 
 * @author dchallas
 * @version $Id: TemplateAnnotationLines.java,v 1.6 2014/02/03 17:48:13 pchretien Exp $
 */
final class TemplateAnnotationLines implements TemplateSequenceModel {
	private final List<String> lines;

	/**
	 * Annotations pour la dtDefinition.
	 * @param annotationWriter AnnotationWriter
	 * @param dtDefinition DtDefinition
	 */
	TemplateAnnotationLines(final AnnotationWriter annotationWriter, final DtDefinition dtDefinition) {
		this(annotationWriter.writeAnnotations(dtDefinition));
	}

	/**
	 * Annotation pour un champ.
	 * @param annotationWriter AnnotationWriter
	 * @param dtField Champ
	 * @param dtDefinition DtDefinition
	 */
	TemplateAnnotationLines(final AnnotationWriter annotationWriter, final DtField dtField, final DtDefinition dtDefinition) {
		this(annotationWriter.writeAnnotations(dtField, dtDefinition));
	}

	/**
	 * Annotations pour une association.
	 * @param annotationWriter AnnotationWriter
	 * @param annotationNode définition de l'association
	 */
	TemplateAnnotationLines(final AnnotationWriter annotationWriter, final AssociationNode annotationNode) {
		this(annotationWriter.writeAnnotations(annotationNode));
	}

	private TemplateAnnotationLines(final List<String> lines) {
		Assertion.checkNotNull(lines);
		//-----------------------------------------------------------------
		this.lines = lines;
	}

	/** {@inheritDoc} */
	public TemplateModel get(final int index) {
		return new SimpleScalar(lines.get(index));
	}

	/** {@inheritDoc} */
	public int size() {
		return lines.size();
	}
}
