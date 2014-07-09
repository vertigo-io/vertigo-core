/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
