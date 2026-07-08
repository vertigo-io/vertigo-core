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
package io.vertigo.core.util;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Test of the file manipulation utility.
 *
 * @author skerdudou
 */
public final class FileUtilTest {

	@Test
	public void testCheckUserPath() {
		Assertions.assertDoesNotThrow(() -> FileUtil.checkUserPath("test.txt"));
		Assertions.assertDoesNotThrow(() -> FileUtil.checkUserPath("/test.txt"));
		Assertions.assertDoesNotThrow(() -> FileUtil.checkUserPath("./test.txt"));
		Assertions.assertDoesNotThrow(() -> FileUtil.checkUserPath("my/long/test.txt"));

		Assertions.assertThrows(IllegalStateException.class, () -> FileUtil.checkUserPath("../test.txt"));
		Assertions.assertThrows(IllegalStateException.class, () -> FileUtil.checkUserPath("test/../../secret.txt"));
		Assertions.assertThrows(IllegalStateException.class, () -> FileUtil.checkUserPath("test.txt\0secret.txt"));
		Assertions.assertThrows(IllegalStateException.class, () -> FileUtil.checkUserPath("../test.txt\0"));
	}

}
