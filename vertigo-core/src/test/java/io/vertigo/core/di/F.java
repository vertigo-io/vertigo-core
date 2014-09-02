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
package io.vertigo.core.di;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.lang.Option;

import javax.inject.Inject;
import javax.inject.Named;

public final class F {
	@Inject
	@Named("a")
	private Object a;

	@Inject
	@Named("param2")
	private String param2;

	private final String param1;
	private final Option<String> param3;
	private final Option<String> param4;

	@Inject
	public F(final @Named("param1") String param1, final @Named("param3") Option<String> param3, final @Named("param4") Option<String> param4) {
		Assertion.checkNotNull(param1);
		Assertion.checkNotNull(param3);
		Assertion.checkNotNull(param4);
		//---------------------------------------------------------------------
		this.param1 = param1;
		this.param3 = param3;
		this.param4 = param4;
	}

	public A getA() {
		return (A) a;
	}

	public String getParam1() {
		return param1;
	}

	public String getParam2() {
		return param2;
	}

	public Option<String> getParam3() {
		return param3;
	}

	public Option<String> getParam4() {
		return param4;
	}
}
