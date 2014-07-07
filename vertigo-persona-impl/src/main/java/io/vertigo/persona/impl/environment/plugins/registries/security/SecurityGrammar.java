package io.vertigo.persona.impl.environment.plugins.registries.security;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;

/**
 * @author pchretien
 * @version $Id: SecurityGrammar.java,v 1.3 2014/02/03 17:29:01 pchretien Exp $
 */
public final class SecurityGrammar extends GrammarProvider {
	public static final SecurityGrammar INSTANCE = new SecurityGrammar();
	/**D�finition d'un role.*/
	private final Entity roleEntity;

	/**
	 * Initialisation des metadonnees permettant de decrire le metamodele.
	 */
	private SecurityGrammar() {
		roleEntity = new EntityBuilder("Role").build();
		//---------------------------------------------------------------------
		getGrammar().registerEntity(roleEntity);
	}

	/**
	 * @return D�finition d'un role.
	 */
	public Entity getRoleEntity() {
		return roleEntity;
	}
}
