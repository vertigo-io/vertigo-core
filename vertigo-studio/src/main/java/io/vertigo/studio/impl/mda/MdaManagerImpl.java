package io.vertigo.studio.impl.mda;

import io.vertigo.kernel.lang.Activeable;
import io.vertigo.studio.mda.Configuration;
import io.vertigo.studio.mda.GeneratorPlugin;
import io.vertigo.studio.mda.MdaManager;
import io.vertigo.studio.mda.Result;

import java.util.List;
import java.util.Properties;

import javax.inject.Inject;

/**
 * Implémentation du MDA.
 * 
 * @author pchretien, dchallas
 * @version $Id: MdaManagerImpl.java,v 1.2 2013/10/22 12:36:17 pchretien Exp $
 */
public final class MdaManagerImpl implements MdaManager, Activeable {
	@Inject
	private List<GeneratorPlugin<Configuration>> generatorPlugins;

	/** {@inheritDoc} */
	public void start() {
		//Pour des raisons d'optimisation mémoire on ne garde pas la liste des plugins dans le manager.
		//Car ceux ci ne sont utilisé qu'une seule fois.
		//En revanche on test au démarrage la présence des class des plugins. (et on ne s'occupe pas du resultat)
		//getGeneratorPlugins();
	}

	/** {@inheritDoc} */
	public void stop() {
		//
	}

	/** {@inheritDoc} */
	public List<GeneratorPlugin<Configuration>> getGeneratorPlugins() {
		//		final List<GeneratorPlugin<Configuration>> generatorPlugins = new ArrayList<GeneratorPlugin<Configuration>>();
		//		for (final Plugin plugin : Home.getContainer().getPlugins(MdaManager.class)) {
		//			Assertion.precondition(plugin instanceof GeneratorPlugin, "Les plugins doivent tous être des GeneratorPlugin ({0})", plugin.getClass().getName());
		//			generatorPlugins.add((GeneratorPlugin<Configuration>) plugin);
		//		}
		return java.util.Collections.unmodifiableList(generatorPlugins);
	}

	/** {@inheritDoc} */
	public Result generate(final Properties properties) {
		//Création d'un objet listant les résultats
		final Result result = new ResultImpl();
		//Génèration des objets issus de la modélisation
		for (final GeneratorPlugin<Configuration> generatorPlugin : getGeneratorPlugins()) {
			final Configuration c = generatorPlugin.createConfiguration(properties);
			generatorPlugin.generate(c, result);
		}
		return result;
	}
}
