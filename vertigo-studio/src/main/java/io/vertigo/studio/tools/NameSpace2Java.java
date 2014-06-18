package io.vertigo.studio.tools;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfig;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.ClassUtil;
import io.vertigo.studio.impl.mda.PropertiesUtil;
import io.vertigo.studio.tools.generate.GenerateGoal;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Génération des fichiers Java et SQL à patrir de fichiers template freemarker.
 * 
 * @author dchallas, pchretien
 */
public final class NameSpace2Java {

	private NameSpace2Java() {
		super();
	}

	/**
	 * Lancement du générateur de classes Java.
	 * à partir des déclarations (ksp, oom..)
	 * @param args Le premier argument [0] précise le nom du fichier properties de paramétrage
	 */
	public static void main(String[] args) {
		if (!(args.length == 1)) {
			throw new IllegalArgumentException("Usage : java io.vertigo.studio.tools.NameSpace2Java \"<<pathToParams.properties>>\" ");
		}
		// ---------------------------------------------------------------------
		final Properties properties = createProperties(args[0]);
		final ComponentSpaceConfig componentSpaceConfig = PropertiesUtil.parse(properties, true);

		Home.start(componentSpaceConfig);
		try {
			final List<Class<? extends Goal>> goalClazzList = new ArrayList<>();
			//-------------------------------
			goalClazzList.add(GenerateGoal.class);
			//		goalClazzList.add(ReportingGoal.class);
			//-------------------------------
			process(properties, goalClazzList);
		} finally {
			Home.stop();
		}
	}

	private static void process(final Properties properties, final List<Class<? extends Goal>> goalClazzList) {
		for (final Class<? extends Goal> goalClazz : goalClazzList) {
			final Goal goal = ClassUtil.newInstance(goalClazz);
			goal.process(properties);
		}
	}

	private static Properties createProperties(final String propertiesPath) {
		return createParameters(propertiesPath);
	}

	/**
	 * Création d'un objet Java Properties à partir d'un nom de fichier.
	 * @param fileName Nom du fichier.
	 * @return Properties
	 */
	private static Properties createParameters(final String fileName) {
		try {
			return doCreateParameters(fileName);
		} catch (final IOException e) {
			throw new VRuntimeException("Impossible de récupérer le fichier de configuration.", e);
		}
	}

	private static Properties doCreateParameters(final String fileName) throws IOException {
		Assertion.checkNotNull(fileName);
		//---------------------------------------------------------------------
		final URL url = PropertiesUtil.createURL(fileName);
		try (final InputStream input = url.openStream()) {
			final Properties properties = new Properties();
			properties.load(input);
			return properties;
		}
	}

}
