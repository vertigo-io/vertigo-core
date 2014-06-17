package io.vertigo.dynamo.impl.environment;

import io.vertigo.dynamo.impl.environment.kernel.meta.Entity;
import io.vertigo.dynamo.impl.environment.kernel.meta.EntityBuilder;
import io.vertigo.dynamo.impl.environment.kernel.meta.GrammarProvider;

/**
 * @author pchretien
 */
public final class KernelGrammar extends GrammarProvider {
	public static final KernelGrammar INSTANCE = new KernelGrammar();

	/** Mot-clé des MetaDefinitions de DataType. */
	private static final String DATA_TYPE_META_DEFINITION = "DataType";

	/**Type Primitif.*/
	private final Entity dataTypeEntiy;

	/**Définition d'un champ de DT.*/

	/**
	 * Initialisation des métadonnées permettant de décrire le métamodèle .
	 */
	private KernelGrammar() {
		dataTypeEntiy = new EntityBuilder(DATA_TYPE_META_DEFINITION).build();
		//---------------------------------------------------------------------
		getGrammar().registerEntity(dataTypeEntiy);
	}

	/**
	 * @return Type primitif.
	 */
	public Entity getDataTypeEntity() {
		return dataTypeEntiy;
	}
}
