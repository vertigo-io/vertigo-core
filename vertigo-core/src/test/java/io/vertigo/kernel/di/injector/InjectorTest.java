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
package io.vertigo.kernel.di.injector;

import io.vertigo.core.component.Container;
import io.vertigo.kernel.di.A;
import io.vertigo.kernel.di.B;
import io.vertigo.kernel.di.DIException;
import io.vertigo.kernel.di.E;
import io.vertigo.kernel.di.F;
import io.vertigo.kernel.di.configurator.ParamsContainer;
import io.vertigo.kernel.di.injector.Injector;
import io.vertigo.kernel.lang.Assertion;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

/**
 * Voir sur reactor pour l'arbre des dépendances des objets A==>F.  
 * @author pchretien
 */
public final class InjectorTest {
	private final Injector injector = new Injector();

	private static class MyContainer implements Container {
		private final Map<String, Object> map = new HashMap<>();

		public boolean contains(final String id) {
			Assertion.checkArgNotEmpty(id);
			//-----------------------------------------------------------------
			return map.containsKey(id);
		}

		public void put(final String id, final Object object) {
			Assertion.checkArgNotEmpty(id);
			Assertion.checkNotNull(object);
			//-----------------------------------------------------------------
			map.put(id, object);
		}

		public <C> C resolve(final String id, final Class<C> componentClass) {
			Assertion.checkArgNotEmpty(id);
			Assertion.checkNotNull(componentClass);
			//-----------------------------------------------------------------
			final Object object = map.get(id);
			Assertion.checkNotNull(object, "{0} not found", id);
			return componentClass.cast(object);
		}

		public Set<String> keySet() {
			return map.keySet();
		}
	}

	private static void nop(final Object o) {
		//NOP
	}

	@Test
	public void testA() {
		final A a = injector.newInstance(A.class, new ParamsContainer(Collections.<String, String> emptyMap()));
		nop(a);
	}

	@Test(expected = DIException.class)
	public void testBFail() {
		final B b = injector.newInstance(B.class, new ParamsContainer(Collections.<String, String> emptyMap()));
		nop(b);
	}

	@Test
	public void testB() {
		final MyContainer container = new MyContainer();
		final A a = injector.newInstance(A.class, container);
		container.put("a", a);
		final B b = injector.newInstance(B.class, container);
		Assert.assertEquals(b.getA(), a);
	}

	@Test
	public void testE() {
		final MyContainer container = new MyContainer();
		final A a = injector.newInstance(A.class, container);
		container.put("a", a);
		final E e = injector.newInstance(E.class, container);
		Assert.assertTrue(e.getA().isDefined());
		Assert.assertEquals(e.getA().get(), a);
		Assert.assertTrue(e.getB().isEmpty());
	}

	@Test
	public void testF() {
		final MyContainer container = new MyContainer();
		final A a = injector.newInstance(A.class, container);
		container.put("a", a);
		container.put("param1", "test1");
		container.put("param2", "test2");
		container.put("param3", "test3");
		final F f = injector.newInstance(F.class, container);
		Assert.assertEquals(f.getA(), a);
		Assert.assertEquals(f.getParam1(), "test1");
		Assert.assertEquals(f.getParam2(), "test2");
		Assert.assertTrue(f.getParam3().isDefined());
		Assert.assertEquals(f.getParam3().get(), "test3");
		Assert.assertTrue(f.getParam4().isEmpty());
	}
}
