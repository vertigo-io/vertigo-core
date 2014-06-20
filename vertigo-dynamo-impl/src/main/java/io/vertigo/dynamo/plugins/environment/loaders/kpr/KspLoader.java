package io.vertigo.dynamo.plugins.environment.loaders.kpr;

import io.vertigo.commons.parser.NotFoundException;
import io.vertigo.commons.parser.Rule;
import io.vertigo.dynamo.impl.environment.kernel.impl.model.DynamicDefinitionRepository;
import io.vertigo.dynamo.plugins.environment.loaders.kpr.rules.KspRule;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.util.StringUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.Charset;

/**
 * Parser d'un fichier KSP.
 *
 * @author pchretien
 */
final class KspLoader {
	private static final String CHARSET = "ISO-8859-1";
	private final URL kspURL;

	/**
	 * Constructeur.
	 * @param kspURL URL du fichier KSP.
	 */
	KspLoader(final URL kspURL) {
		Assertion.checkNotNull(kspURL);
		//----------------------------------------------------------------------
		this.kspURL = kspURL;
	}

	void load(final DynamicDefinitionRepository dynamicModelrepository) {
		Assertion.checkNotNull(dynamicModelrepository);
		try {
			final String s = parseFile();
			final Rule<Void> rule = new KspRule(dynamicModelrepository);
			rule.createParser().parse(s, 0);
		} catch (final NotFoundException e) {
			final String message = StringUtil.format("Echec de lecture du fichier KSP {0}\n{1}", kspURL.getFile(), e.getFullMessage());
			throw new VRuntimeException(message, e);
		} catch (final Exception e) {
			final String message = StringUtil.format("Echec de lecture du fichier KSP {0}\n{1}", kspURL.getFile(), e.getMessage());
			throw new VRuntimeException(message, e);
		}
	}

	/**
	 * Transforme un fichier en une chaine de caractère parsable.
	 * @return String Chaine parsable correspondant au fichier.
	 * @throws IOException Erreur d'entrée/sortie
	 */
	private String parseFile() throws IOException {
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(kspURL.openStream(), Charset.forName(CHARSET)))) {
			final StringBuilder buff = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				buff.append(line);
				line = reader.readLine();
				buff.append("\r\n");
			}
			return buff.toString();
		}
	}
}
