package io.vertigo.dynamo.collections;

import io.vertigo.dynamo.Function;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Fonction de transformation d'une liste en liste.
 * @author  pchretien
 * @version $Id: DtListFunction.java,v 1.2 2014/01/20 17:45:23 pchretien Exp $
 */
public interface DtListFunction<D extends DtObject> extends Function<DtList<D>, DtList<D>> {
	//
}
