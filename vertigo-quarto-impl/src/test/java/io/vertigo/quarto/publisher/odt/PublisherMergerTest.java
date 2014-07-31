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
package io.vertigo.quarto.publisher.odt;

import io.vertigo.quarto.publisher.AbstractPublisherMergerTest;

/**
 * Test de l'implémentation standard.
 * 
 * @author npiedeloup
 * @version $Id: PublisherMergerTest.java,v 1.1 2013/07/11 13:25:42 npiedeloup Exp $
 */
public final class PublisherMergerTest extends AbstractPublisherMergerTest {

	/** {@inheritDoc} */
	@Override
	protected final String getExtension() {
		return "odt";
	}
}
