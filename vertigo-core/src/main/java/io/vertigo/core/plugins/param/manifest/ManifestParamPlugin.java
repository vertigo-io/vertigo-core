/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2024, Vertigo.io, team@vertigo.io
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
import io.vertigo.core.param.ParamValue;
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
	 * @param resourceManager Selector
	 * @throws IOException erreur de lecture du fichier
	 */
	@Inject
	public ManifestParamPlugin(final ResourceManager resourceManager,
			@ParamValue("url") final Optional<String> manifestLocationOpt) throws IOException {
		Assertion.check()
				.isNotNull(resourceManager);
		//-----
		try {
			final URL configURL = resourceManager.resolve(FileUtil.translatePath(manifestLocationOpt.orElse("META-INF/MANIFEST.MF")));
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
