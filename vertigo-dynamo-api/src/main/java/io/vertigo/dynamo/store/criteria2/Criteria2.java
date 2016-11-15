/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2016, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.store.criteria2;

import java.io.Serializable;
import java.util.function.Predicate;

import io.vertigo.dynamo.domain.model.Entity;
import io.vertigo.lang.Assertion;
import io.vertigo.lang.Tuples;

/**
 * Critère générique.
 *
 * @author pchretien
 * @param <E> the type of entity
 */
public class Criteria2<E extends Entity> implements Serializable {
	private static final long serialVersionUID = -990254492823334724L;
	private final CriteriaBool<E> criteriaBool;

	public Criteria2(final CriteriaBool<E> criteriaBool) {
		Assertion.checkNotNull(criteriaBool);
		//---
		this.criteriaBool = criteriaBool;
	}

	public Predicate<E> toPredicate() {
		return criteriaBool.toPredicate();
	}

	public Tuples.Tuple2<String, Ctx> toSql() {
		final Ctx ctx = new Ctx();
		final String sql = criteriaBool.toSql(ctx);
		return new Tuples.Tuple2<>(sql, ctx);

	}

}
