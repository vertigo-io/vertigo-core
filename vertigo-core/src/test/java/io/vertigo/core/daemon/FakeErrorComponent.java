/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.daemon;

import io.vertigo.core.node.component.Component;

public final class FakeErrorComponent implements Component {

	static final String ERROR_DAEMON_NAME = "DmnError";

	@DaemonScheduled(name = ERROR_DAEMON_NAME, periodInSeconds = 1)
	public void err() {
		throw new Error("FatalError for Test");
	}

}
