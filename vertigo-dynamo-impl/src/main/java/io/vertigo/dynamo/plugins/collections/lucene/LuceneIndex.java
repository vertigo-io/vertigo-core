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
package io.vertigo.dynamo.plugins.collections.lucene;

import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.domain.model.DtObject;

import java.io.IOException;
import java.io.Serializable;

import org.apache.lucene.search.Query;
import org.apache.lucene.search.Sort;

/**
 * Interface réprésentant un IndexLucene.
 *
 * @author npiedeloup
 * @param <D> type d'objet indexé
 */
public interface LuceneIndex<D extends DtObject> extends Serializable {
	void addAll(final DtList<D> fullDtc, final boolean storeValue) throws IOException;

	DtList<D> executeQuery(final Query query, final int skip, final int top, final Sort sort) throws IOException;

}
