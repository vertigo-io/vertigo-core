package io.vertigo.vega.rest.validation;

import io.vertigo.dynamo.domain.model.DtObject;

import java.util.Set;

/**
 * Validator of DtObject.
 * Could check an object, for a modified fields set and append detected errors in an DtObjectErrors.
 * @author npiedeloup
 * @param <O> Type of DtObject
 */
public interface DtObjectValidator<O extends DtObject> {

	/**
	 * Effectue les validations prévu d'un objet.
	 * @param dtObject Objet à tester
	 * @param modifiedFieldNameSet Liste des champs modifiés
	 * @param dtObjectErrors Pile des erreurs
	 */
	void validate(O dtObject, Set<String> modifiedFieldNameSet, DtObjectErrors dtObjectErrors);

}
