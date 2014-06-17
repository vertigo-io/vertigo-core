package io.vertigo.dynamo.collections;

import io.vertigo.dynamo.Function;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Fonction de transformation d'une liste en liste.
 * @author  pchretien
 */
public interface DtListFunction<D extends DtObject> extends Function<DtList<D>, DtList<D>> {
	//
}
