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
package io.vertigo.commons.resource;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;

/**
 *
 */
public interface TestModel {
	public @Retention(RUNTIME)
	@Inherited
	@interface MAI1 {
		//
	}

	public @Retention(RUNTIME)
	@MAI1
	@interface AI1 {
		//
	}

	public @AI1
	interface I1 {
		//
	}

	public @Retention(RUNTIME)
	@Inherited
	@interface AI2 {
		//
	}

	public @AI2
	interface I2 extends I1 {
		//
	}

	public @Retention(RUNTIME)
	@Inherited
	@interface AC1 {
		//
	}

	public @Retention(RUNTIME)
	@interface AC1n {
		//
	}

	public @AC1
	@AC1n
	class C1 implements I2 {
		//
	}

	public @Retention(RUNTIME)
	@interface AC2 {
		public abstract String value();
	}

	public @AC2("grr...")
	class C2 extends C1 {
		//
	}

	public @AC2("ugh?!")
	class C3 extends C1 {
		//
	}

	public @Retention(RUNTIME)
	@interface AM1 {
		public abstract String value();
	}

	public @Retention(RUNTIME)
	@interface AF1 {
		public abstract String value();
	}

	public class C4 {
		@AF1("1")
		private String f1;
		@AF1("2")
		protected String f2;
		protected String f3;

		public C4() {}

		@AM1("1")
		public C4(@AM1("1") final String f1) {
			this.f1 = f1;
		}

		@AM1("1")
		protected void m1() {
			//
		}

		@AM1("1")
		public void m1(final int integer, final String... strings) {
			//
		}

		@AM1("1")
		public void m1(final int[][] integer, final String[][] strings) {
			//
		}

		@AM1("2")
		public String m3() {
			return null;
		}

		public String m4(@AM1("2") final String string) {
			return null;
		}

		public C3 c2toC3(final C2 c2) {
			return null;
		}

		public int add(final int i1, final int i2) {
			return i1 + i2;
		}
	}

	public class C5 extends C3 {
		//
	}

	public @AC2("ugh?!")
	interface I3 {
		//
	}

	public class C6 implements I3 {
		//
	}
}
