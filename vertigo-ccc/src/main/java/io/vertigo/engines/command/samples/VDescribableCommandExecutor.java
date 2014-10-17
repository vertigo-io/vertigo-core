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
package io.vertigo.engines.command.samples;

import io.vertigo.core.Home;
import io.vertigo.core.command.VCommand;
import io.vertigo.core.command.VCommandExecutor;
import io.vertigo.core.spaces.component.ComponentInfo;
import io.vertigo.core.spaces.component.Describable;
import io.vertigo.lang.Assertion;

import java.util.List;

public final class VDescribableCommandExecutor implements VCommandExecutor<List<ComponentInfo>> {
	public List<ComponentInfo> exec(VCommand command) {
		Assertion.checkNotNull(command);
		//Assertion.checkArgument(command.getName());
		System.out.println(">>> find:" + command.getName());
		System.out.println(">>> Home:" + Home.getComponentSpace().keySet());
		//---------------------------------------------------------------------
		Object component = Home.getComponentSpace().resolve(command.getName(), Object.class);

		//			if (component instanceof Describable) {
		return Describable.class.cast(component).getInfos();
		//		}
	}
}
