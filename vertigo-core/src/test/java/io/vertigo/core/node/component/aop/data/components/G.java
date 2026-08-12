/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2023, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.component.aop.data.components;

import io.vertigo.core.lang.Assertion;
import io.vertigo.core.node.component.Component;
import io.vertigo.core.node.component.aop.data.aspects.OneMore;
import jakarta.inject.Inject;

/**
 * A component using constructor injection with an aspected method.
 *
 * @author skerdudou
 */
public class G implements Component {
	private final Computer computer;

	@Inject
	public G(final Computer computer) {
		Assertion.check().isNotNull(computer);
		//-----
		this.computer = computer;
	}

	@OneMore
	public int getValue(final int value) {
		return value;
	}

	public int sumByComputer(final int i, final int j) {
		return computer.sum(i, j);
	}
}
