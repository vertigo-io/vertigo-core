package io.vertigo.core.plugins.param.manifest;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.regex.Pattern;

import javax.inject.Inject;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.vertigo.core.impl.param.ParamPlugin;
import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.VSystemException;
import io.vertigo.core.param.Param;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.core.util.FileUtil;

/**
 * Plugin de gestion de configuration du manifest local.
 *
 * @author pforhan; skerdudou
 */
public final class ManifestParamPlugin implements ParamPlugin {

	private static final Logger LOG = LogManager.getLogger(ManifestParamPlugin.class);
	private static final Pattern PARAM_NAME_PATTERN = Pattern.compile("^[A-Za-z0-9\\-_]+$");

	private Optional<Manifest> manifestOpt;

	/**
	 * Constructor.
	 *
	 * @param  resourceManager Selector
	 * @throws IOException     erreur de lecture du fichier
	 */
	@Inject
	public ManifestParamPlugin(final ResourceManager resourceManager) throws IOException {
		Assertion.check()
				.isNotNull(resourceManager);
		//-----
		try {
			final URL configURL = resourceManager.resolve(FileUtil.translatePath("META-INF/MANIFEST.MF"));
			manifestOpt = Optional.of(loadManifest(configURL));
		} catch (final VSystemException e) {
			LOG.trace("Aucun MANIFEST.MF présent", e);
			manifestOpt = Optional.empty();
		}
	}

	private static Manifest loadManifest(final URL configURL) throws IOException {
		try (final InputStream input = configURL.openStream()) {
			return new Manifest(input);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Optional<Param> getParam(final String paramName) {
		Assertion.check().isNotBlank(paramName);
		//-----
		if (manifestOpt.isEmpty() || !isValidParamName(paramName)) {
			return Optional.empty();
		}
		// we have a manifest
		final Attributes attr = manifestOpt.get().getMainAttributes();
		if (attr == null) {
			return Optional.empty();
		}
		final String value = attr.getValue(paramName);

		return value != null ? Optional.of(Param.of(paramName, value)) : Optional.empty();
	}

	private static boolean isValidParamName(final String paramName) {
		return PARAM_NAME_PATTERN.matcher(paramName).find();
	}
}
