/**
 *
 */
package io.vertigo.account.plugins.authorization.loaders;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.vertigo.account.authorization.metamodel.Permission;
import io.vertigo.account.authorization.metamodel.SecuredEntity;
import io.vertigo.app.config.DefinitionResourceConfig;
import io.vertigo.core.definition.DefinitionProvider;
import io.vertigo.core.definition.DefinitionSpace;
import io.vertigo.core.definition.DefinitionSupplier;
import io.vertigo.core.resource.ResourceManager;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.WrappedException;

/**
 * Loader du fichier de configuration de la sécurité avancée.
 *
 * @author jgarnier
 */
public final class JsonSecurityDefinitionProvider implements DefinitionProvider {

	private final ResourceManager resourceManager;
	private final List<DefinitionSupplier> definitionSuppliers;

	/**
	 * Constructor.
	 *
	 * @param resourceManager the resourceManager
	 */
	@Inject
	public JsonSecurityDefinitionProvider(final ResourceManager resourceManager) {
		Assertion.checkNotNull(resourceManager);
		// -----
		this.resourceManager = resourceManager;
		definitionSuppliers = new ArrayList<>();
	}

	/** {@inheritDoc} */
	@Override
	public List<DefinitionSupplier> get(final DefinitionSpace definitionSpace) {
		return definitionSuppliers;
	}

	/** {@inheritDoc} */
	@Override
	public void addDefinitionResourceConfig(final DefinitionResourceConfig definitionResourceConfig) {
		Assertion.checkState("security".equals(definitionResourceConfig.getType()), "Type {0} not supported",
				definitionResourceConfig.getType());
		// -----
		final URL authConfURL = resourceManager.resolve(definitionResourceConfig.getPath());
		final Gson gson = createGson();
		try {
			final String confJson = parseFile(authConfURL);
			final AdvancedSecurityConfiguration config = gson.fromJson(confJson, AdvancedSecurityConfiguration.class);
			registerDefinitions(config);
		} catch (final Exception e) {
			throw WrappedException.wrap(e, "Erreur durant la lecture du fichier JSON " + authConfURL);
		}
	}

	private static Gson createGson() {
		return new GsonBuilder()
				.setPrettyPrinting()
				//TODO  registerTypeAdapter(String.class, new EmptyStringAsNull<>())// add "" <=> null
				.registerTypeAdapter(SecuredEntity.class, new SecuredEntityDeserializer())
				.registerTypeAdapter(Permission.class, new PermissionDeserializer())
				.create();
	}

	private static String parseFile(final URL url) throws IOException {
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
		}
	}

	private void registerDefinitions(final AdvancedSecurityConfiguration config) {
		registerPermissions(config.getPermissions());
		registerSecurityEntities(config.getSecuredEntities());
	}

	private void registerPermissions(final List<Permission> permissions) {
		permissions.stream()
				.forEach(prm -> definitionSuppliers.add(ds -> prm)); //on register les Permissions globales
	}

	private void registerSecurityEntities(final List<SecuredEntity> securityEntities) {
		securityEntities.stream()
				.forEach(sec -> definitionSuppliers.add(ds -> sec)); //on register les SecuredEntities

		securityEntities.stream()
				.flatMap(securityEntity -> securityEntity.getOperations().stream())
				.forEach(prm -> definitionSuppliers.add(ds -> prm)); //on register les Permissions associées aux opérations
	}

}
