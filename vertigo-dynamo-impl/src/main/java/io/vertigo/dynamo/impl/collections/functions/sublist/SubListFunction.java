/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
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
package io.vertigo.dynamo.impl.collections.functions.sublist;

import io.vertigo.core.lang.Assertion;
import io.vertigo.dynamo.collections.DtListFunction;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Fonction de sous-liste.
 * @author pchretien
 */
public final class SubListFunction<D extends DtObject> implements DtListFunction<D> {
	private final int start, end;

	public SubListFunction(final int start, final int end) {
		this.start = start;
		this.end = end;
	}

	/** {@inheritDoc} */
	public DtList<D> apply(final DtList<D> dtc) {
		Assertion.checkNotNull(dtc);
		Assertion.checkArgument(start >= 0 && end <= dtc.size() && start <= end, "IndexOutOfBoundException, le subList n''est pas possible avec les index passés (start:{0}, end:{1}, size:{2})", String.valueOf(start), String.valueOf(end), String.valueOf(dtc.size())); //condition tirée de la javadoc de subList sur java.util.List
		//----------------------------------------------------------------------
		final DtList<D> subDtc = new DtList<>(dtc.getDefinition());
		for (int i = start; i < end; i++) {
			subDtc.add(dtc.get(i));
		}
		return subDtc;
	}
}
