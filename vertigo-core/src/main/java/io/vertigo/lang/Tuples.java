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
package io.vertigo.lang;

import java.util.Objects;

/**
 * Tuples are immutable objects.
 * @author pchretien
 */
public final class Tuples {
	private Tuples() {
		//private constructor
	}

	/**
	 * Tuple with 2 Objects.
	 * @param <T1> Type one
	 * @param <T2> Type two
	 */
	public static final class Tuple2<T1, T2> {
		private final T1 val1;
		private final T2 val2;

		/**
		* Create e new instance of Tuple2.
		*
		* @param val1 Value 1.
		* @param val2 Value 2.
		*/
		public Tuple2(final T1 val1, final T2 val2) {
			this.val1 = val1;
			this.val2 = val2;
		}

		/**
		* @return  Value#1.
		*/
		public T1 getVal1() {
			return val1;
		}

		/**
		* @return  Value#2.
		 */
		public T2 getVal2() {
			return val2;
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			return Objects.hash(val1, val2);
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(final Object object) {
			if (this == object) {
				return true;
			}
			if (object instanceof Tuple2) {
				final Tuple2<?, ?> that = Tuple2.class.cast(object);
				return Objects.equals(val1, that.val1) && Objects.equals(val2, that.val2);
			}
			return false;
		}
	}

	/**
	 * Tuple with 3 Objects.
	 * @param <T1> Type one
	 * @param <T2> Type two
	 * @param <T3> Type three
	 */
	public static final class Tuple3<T1, T2, T3> {
		private final T1 val1;
		private final T2 val2;
		private final T3 val3;

		/**
		* Create e new instance of Tuple3.
		*
		* @param val1 Value 1.
		* @param val2 Value 2.
		* @param val3 Value 3.
		*/
		public Tuple3(final T1 val1, final T2 val2, final T3 val3) {
			this.val1 = val1;
			this.val2 = val2;
			this.val3 = val3;
		}

		/**
		* @return  Value#1.
		*/
		public T1 getVal1() {
			return val1;
		}

		/**
		* @return  Value#2.
		 */
		public T2 getVal2() {
			return val2;
		}

		/**
		* @return  Value#3.
		 */
		public T3 getVal3() {
			return val3;
		}

		/** {@inheritDoc} */
		@Override
		public int hashCode() {
			return Objects.hash(val1, val2, val3);
		}

		/** {@inheritDoc} */
		@Override
		public boolean equals(final Object object) {
			if (this == object) {
				return true;
			}
			if (object instanceof Tuple3) {
				final Tuple3<?, ?, ?> that = Tuple3.class.cast(object);
				return Objects.equals(val1, that.val1) && Objects.equals(val2, that.val2) && Objects.equals(val3, that.val3);
			}
			return false;
		}
	}
}
