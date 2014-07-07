package io.vertigo.studio.plugins.mda.security;

import io.vertigo.kernel.Home;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.persona.security.model.Role;
import io.vertigo.studio.mda.Result;
import io.vertigo.studio.plugins.mda.AbstractGeneratorPlugin;
import io.vertigo.studio.plugins.mda.FileGenerator;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Generation des objets relatifs au module Securite. 
 *  
 * @author pchretien
 * @version $Id: SecurityGeneratorPlugin.java,v 1.6 2014/03/10 13:41:43 npiedeloup Exp $
 */
public final class SecurityGeneratorPlugin extends AbstractGeneratorPlugin<SecurityConfiguration> {
	/** {@inheritDoc}  */
	public SecurityConfiguration createConfiguration(final Properties properties) {
		return new SecurityConfiguration(properties);
	}

	private Collection<Role> getRoles() {
		// return Home.getNameSpace().getDefinitions(Role.class);
		return Home.getDefinitionSpace().getAll(Role.class);
	}

	/** {@inheritDoc} */
	public void generate(final SecurityConfiguration securityConfiguration, final Result result) {
		Assertion.checkNotNull(securityConfiguration);
		Assertion.checkNotNull(result);
		//---------------------------------------------------------------------
		generateRole(securityConfiguration, result);
	}

	private void generateRole(final SecurityConfiguration securityConfiguration, final Result result) {
		final Collection<Role> roles = getRoles();
		if (!roles.isEmpty()) {
			//On ne genere aucun fichier si aucun rele.
			//				final Roles2java roles2Java = new Roles2java(packageName, roleList, parameters);

			final Map<String, Object> mapRoot = new HashMap<>();
			mapRoot.put("roles", roles);
			mapRoot.put("classSimpleName", "Role");
			mapRoot.put("packageName", securityConfiguration.getSecurityPackage());
			final FileGenerator super2java = getFileGenerator(securityConfiguration, mapRoot, "Role", securityConfiguration.getSecurityPackage(), ".java", "role.ftl");
			super2java.generateFile(result, true);
		}
	}
}
