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
package io.vertigo.kernel.component;

import io.vertigo.AbstractTestCase2JU4;
import io.vertigo.kernel.Home;
import io.vertigo.kernel.component.mock.A;
import io.vertigo.kernel.component.mock.B;
import io.vertigo.kernel.component.mock.C;
import io.vertigo.kernel.component.mock.Computer;
import io.vertigo.kernel.component.mock.F;
import io.vertigo.kernel.component.mock.MyException;

import org.junit.Assert;
import org.junit.Test;


public abstract class AbstractAspectTestCase extends AbstractTestCase2JU4 {
	private A a;
	private B b;
	private C c;

	@Test
	public final void testNo() {
		final Computer computer = Home.getComponentSpace().resolve(Computer.class);
		Assert.assertEquals(66, computer.no(66));
	}

	@Test
	public final void testOneMoreOnMethod() {
		final Computer comp = Home.getComponentSpace().resolve(Computer.class);
		//On vérifie que l'intercepteur ajoute bien 1 à la somme de 2+3
		Assert.assertEquals(6, comp.sum(2, 3));
	}

	@Test
	public final void testOneMoreOnClass() {
		final F f = Home.getComponentSpace().resolve(F.class);
		//On vérifie que l'intercepteur ajoute bien 1 à la somme de 2+3
		Assert.assertEquals(11, f.getValue(10));
		Assert.assertEquals(12, f.getValue2(10));
		Assert.assertEquals(22, f.getValue3(10));

	}

	@Test
	public final void testOneMoreTenMore() {
		final Computer comp = Home.getComponentSpace().resolve(Computer.class);
		//On vérifie que l'intercepteur ajoute bien 1 à la somme de 2+3
		Assert.assertEquals(17, comp.multi(2, 3));
	}

	@Override
	protected void doAfterTearDown() throws Exception {
		if (a != null) {
			Assert.assertEquals(true, a.isInitialized());
			Assert.assertEquals(true, a.isFinalized());
		}
		if (b != null) {
			Assert.assertEquals(true, b.isInitialized());
			Assert.assertEquals(true, b.isFinalized());
		}
		if (c != null) {
			Assert.assertEquals(true, c.isInitialized());
			Assert.assertEquals(true, c.isFinalized());
		}
	}

	@Test
	public void testNonProxiedWithAnnotation() {
		a = Home.getComponentSpace().resolve("a", A.class);
		Assert.assertEquals(true, a.isInitialized());
		Assert.assertEquals(false, a.isFinalized());
	}

	@Test
	public void testProxyWithInterface() {
		b = Home.getComponentSpace().resolve(B.class);
		Assert.assertEquals(true, b.isInitialized());
		Assert.assertEquals(false, b.isFinalized());
	}

	@Test
	public void testProxyWithObjectInterface() {
		c = Home.getComponentSpace().resolve("c", C.class);
		Assert.assertEquals(true, c.isInitialized());
		Assert.assertEquals(false, c.isFinalized());
	}

	@Test(expected = MyException.class)
	public void testBeanMyException() throws MyException {
		a = Home.getComponentSpace().resolve("a", A.class);
		a.throwMyException();
	}

	@Test(expected = MyException.class)
	public void testProxyWithInterfaceMyException() throws MyException {
		b = Home.getComponentSpace().resolve("b", B.class);
		b.throwMyException();
	}
}
