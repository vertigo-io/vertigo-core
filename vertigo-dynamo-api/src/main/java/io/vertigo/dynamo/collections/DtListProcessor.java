/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.collections;

import java.util.function.Function;
import java.util.function.UnaryOperator;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

/**
 * Processor that can be composed of filters or sorters
 * and be applied on a list.
 * @author pchretien
 * @param <D> Type of list's element
 */
public interface DtListProcessor<D extends DtObject> extends Function<DtList<D>, DtList<D>> {
	/**
	 * Add any function that transform a list into an another list.
	 * @param listFunction List function
	 * @return new DtListProcessor completed with this function
	 */
	DtListProcessor<D> add(UnaryOperator<DtList<D>> listFunction);

	/**
	 * Constructeur de la function de filtrage à partir d'un filtre de liste.
	 *
	 * @param listFilter Filtre de liste
	 * @return Function de filtrage
	 */
	DtListProcessor<D> filter(final ListFilter listFilter);
}
