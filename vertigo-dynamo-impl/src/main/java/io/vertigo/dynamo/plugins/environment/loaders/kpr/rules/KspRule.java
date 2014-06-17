package io.vertigo.dynamo.plugins.environment.loaders.kpr.rules;

import static io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.Syntax.SPACES;
import io.vertigo.commons.parser.AbstractRule;
import io.vertigo.commons.parser.Choice;
import io.vertigo.commons.parser.FirstOfRule;
import io.vertigo.commons.parser.ManyRule;
import io.vertigo.commons.parser.Rule;
import io.vertigo.commons.parser.SequenceRule;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinition;
import io.vertigo.dynamo.impl.environment.kernel.model.DynamicDefinitionBuilder;
import io.vertigo.kernel.lang.Assertion;

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
public final class KspRule extends AbstractRule<Void, List<?>> {
	private final DynamicDefinitionRepository dynamicModelrepository;

	/**
	 * Constructeur.
	 * @param dynamicModelrepository Grammaire
	 */
	public KspRule(final DynamicDefinitionRepository dynamicModelrepository) {
		super();
		Assertion.checkNotNull(dynamicModelrepository);
		//----------------------------------------------------------------------
		this.dynamicModelrepository = dynamicModelrepository;

	}

	@Override
	protected Rule<List<?>> createMainRule() {
		final Rule<DynamicDefinition> definitionRule = new DynamicDefinitionRule("create", dynamicModelrepository);
		final Rule<DynamicDefinition> templateRule = new DynamicDefinitionRule("alter", dynamicModelrepository);
		final Rule<Choice> firstOfRule = new FirstOfRule(//"definition or template")
				definitionRule, //0
				templateRule //1 
		);
		final Rule<List<Choice>> manyRule = new ManyRule<>(firstOfRule, true, true);
		return new SequenceRule(//
				SPACES,//
				new PackageRule(),//1
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
