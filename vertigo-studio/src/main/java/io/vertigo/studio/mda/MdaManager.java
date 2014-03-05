package io.vertigo.studio.mda;

import io.vertigo.kernel.component.Manager;

import java.util.List;
import java.util.Properties;

/**
 * Manager MDA.
 * @author pchretien
 * @version $Id: MdaManager.java,v 1.2 2013/10/22 10:59:11 pchretien Exp $
 */
public interface MdaManager extends Manager {
	/**
	 * @return Liste des plugins de génération.
	 */
	List<GeneratorPlugin<Configuration>> getGeneratorPlugins();

	/**
	 * Génération des fichiers. 
	 * @param properties Configuration de la génération
	 * @return Rapport resultat de la génération
	 */
	Result generate(Properties properties);
}
