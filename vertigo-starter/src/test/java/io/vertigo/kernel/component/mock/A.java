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
package io.vertigo.kernel.component.mock;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.inject.Named;

/**
 * @author prahmoune
 * @version $Id: A.java,v 1.1 2013/10/09 14:04:13 pchretien Exp $
 */
@Named("a")
public class A {
	boolean initialized;
	boolean finalized;

	public boolean isInitialized() {
		return initialized;
	}

	public boolean isFinalized() {
		return finalized;
	}

	@PostConstruct
	public void init() {
		initialized = true;
	}

	@PreDestroy
	public void destroy() {
		finalized = true;
	}

	public void throwMyException() throws MyException {
		throw new MyException();
	}
}
