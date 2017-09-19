package io.vertigo.studio.plugins.masterdata.json;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;
import javax.inject.Named;

import com.google.gson.Gson;

import io.vertigo.core.resource.ResourceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;
import io.vertigo.studio.impl.masterdata.MasterDataValueProviderPlugin;
import io.vertigo.studio.masterdata.MasterDataValues;

/**
 * Plugin for retrieving masterdata values from a json file.
 * @author mlaroche
 *
 */
public class JsonMasterDataValueProvider implements MasterDataValueProviderPlugin {

	private final ResourceManager resourceManager;

	private final String fileName;
	private final Gson gson = new Gson();

	/**
	 * Constructor
	 * @param resourceManager resourceManager
	 * @param fileName the json file where masterdata values are stored
	 */
	@Inject
	public JsonMasterDataValueProvider(
			final ResourceManager resourceManager,
			final @Named("fileName") String fileName) {
		Assertion.checkArgNotEmpty(fileName);
		//---
		this.resourceManager = resourceManager;
		this.fileName = fileName;
	}

	/** {@inheritDoc} */
	@Override
	public MasterDataValues getValues() {
		final String jsonFileAsString = parseFile(resourceManager.resolve(fileName));
		return gson.fromJson(jsonFileAsString, MasterDataValues.class);

	}

	private static String parseFile(final URL url) {
		try (final BufferedReader reader = new BufferedReader(
				new InputStreamReader(url.openStream(), StandardCharsets.UTF_8))) {
			final StringBuilder buff = new StringBuilder();
			String line = reader.readLine();
			while (line != null) {
				buff.append(line);
				line = reader.readLine();
				buff.append("\r\n");
			}
			return buff.toString();
		} catch (final IOException e) {
			throw WrappedException.wrap(e, "Error reading json file : '{0}'", url);
		}
	}

}
