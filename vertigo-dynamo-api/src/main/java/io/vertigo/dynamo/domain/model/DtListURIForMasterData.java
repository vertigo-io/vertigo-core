package io.vertigo.dynamo.domain.model;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;

/**
 * Gestion d'une liste de référence.
 * Une liste de référence est effectué au titre d'un type de référentiel (MasterDataDefinition).
 * En effet un même type de référentiel (Article par exemple) comporte plusieurs listes :
 *
 * -Tous les articles
 * -Tous les articles actifs
 * -Tous les articles en promotion (donc actifs...)
 *
 * @author pchretien
 */
public final class DtListURIForMasterData extends DtListURI {
	private static final long serialVersionUID = -7808114745411163474L;

	private final String code;

	/**
	 * Constructeur.
	 * @param dtDefinition Définition de la liste de référentiel
	 * @param code Code de la liste de référence. Tous les codes commencent par MDL_.
	 */
	public DtListURIForMasterData(final DtDefinition dtDefinition, final String code) {
		super(dtDefinition);
		//----------------------------------------------------------------------
		this.code = code;
	}

	/**
	 * Code de la liste de référence (identifiant).
	 * Une liste de référence est au type de liste de référence ce qu'un DTO est à un DT.
	 * @return Code de la liste de référence.
	 */
	public String getCode() {
		return code;
	}
}
