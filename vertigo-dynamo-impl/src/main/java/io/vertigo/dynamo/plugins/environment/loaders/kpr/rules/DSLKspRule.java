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
package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.DSLSyntaxRules.SPACES;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;

import java.util.List;

/**
 * règle de composition d'un fichier KSP.
 * Permet de parser un texte au format KSP.
 * Un  fichier KSP ne contient que
 *  - des déclarations de définition,
 *  - des déclarations de droits.
 *
 * Tout fichier ksp commence par une entête ou est précisé le nom du pacakage.
 *
 * @author pchretien
 */
public final class DSLKspRule extends AbstractRule<Void, List<?>> {
	private final DynamicDefinitionRepository dynamicModelrepository;

	/**
	 * Constructeur.
	 * @param dynamicModelrepository Grammaire
	 */
	public DSLKspRule(final DynamicDefinitionRepository dynamicModelrepository) {
		super();
		Assertion.checkNotNull(dynamicModelrepository);
		//----------------------------------------------------------------------
		this.dynamicModelrepository = dynamicModelrepository;

	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final Rule<DynamicDefinition> definitionRule = new DSLDynamicDefinitionRule("create", dynamicModelrepository);
		final Rule<DynamicDefinition> templateRule = new DSLDynamicDefinitionRule("alter", dynamicModelrepository);
		final Rule<Choice> firstOfRule = new FirstOfRule(//"definition or template")
				definitionRule, //0
				templateRule //1 
		);
		final Rule<List<Choice>> manyRule = new ManyRule<>(firstOfRule, true, true);
		return new SequenceRule(//
				SPACES,//
				new DSLPackageRule(),//1
				SPACES, //
				manyRule); //3
	}

	@Override
	protected Void handle(final List<?> parsing) {
		final String packageName = (String) parsing.get(1);
		final List<Choice> tuples = (List<Choice>) parsing.get(3);

		for (final Choice item : tuples) {
			//Tant qu'il y a du texte, il doit correspondre
			// - à des définitions qui appartiennent toutes au même package.
			// - à des gestion de droits.
			switch (item.getValue()) {
				case 0:
					//On positionne le Package
					final DynamicDefinitionBuilder dynamicDefinition = (DynamicDefinitionBuilder) item.getResult();
					dynamicDefinition.withPackageName(packageName);
					handleDefinitionRule((DynamicDefinition) item.getResult());
					break;
				case 1:
					handleTemplateRule((DynamicDefinition) item.getResult());
					break;
				default:
					throw new IllegalArgumentException("case " + item.getValue() + " not implemented");
			}
		}
		return null;
	}

	private void handleTemplateRule(final DynamicDefinition dynamicDefinition) {
		dynamicModelrepository.addTemplate(dynamicDefinition);
	}

	private void handleDefinitionRule(final DynamicDefinition dynamicDefinition) {
		dynamicModelrepository.addDefinition(dynamicDefinition);
	}
}
