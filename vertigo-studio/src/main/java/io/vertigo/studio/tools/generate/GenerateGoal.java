package io.vertigo.studio.tools.generate;

import io.vertigo.kernel.Home;
import io.vertigo.studio.mda.MdaManager;
import io.vertigo.studio.mda.Result;
import io.vertigo.studio.tools.Goal;

import java.util.Properties;

public final class GenerateGoal implements Goal {

	public void process(final Properties properties) {
		//Génération des fichiers données (code java, properties)
		//final NameSpaceConfiguration nameSpaceConfiguration = new NameSpaceConfiguration(properties);

		final MdaManager mdaManager = Home.getComponentSpace().resolve(MdaManager.class);
		final Result result = mdaManager.generate(properties);

		/* Impression du Rapport d'exécution. */
		result.displayResultMessage(System.out);
	}
}
