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
package io.vertigo.commons.eventbus;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.eventbus.data.BlueColorEvent;
import io.vertigo.commons.eventbus.data.DummyEvent;
import io.vertigo.commons.eventbus.data.MySuscriber;
import io.vertigo.commons.eventbus.data.RedColorEvent;
import io.vertigo.commons.eventbus.data.WhiteColorEvent;

/**
 * @author pchretien 
 */
public final class EventBusManagerTest extends AbstractTestCaseJU4 implements EventListener<Event> {

	@Inject
	private EventBusManager eventBusManager;

	private MySuscriber mySuscriber1, mySuscriber2;
	private int deadEvents = 0;

	@Override
	protected void doSetUp() throws Exception {
		mySuscriber1 = new MySuscriber();
		eventBusManager.register(mySuscriber1);
		//-----
		mySuscriber2 = new MySuscriber();
		eventBusManager.register(mySuscriber2);
		//-----
		eventBusManager.registerDead(this);
	}

	@Test
	public void testSimple() {
		eventBusManager.post(new BlueColorEvent());
		eventBusManager.post(new WhiteColorEvent());
		eventBusManager.post(new RedColorEvent());

		Assert.assertEquals(1, mySuscriber1.getBlueCount());
		Assert.assertEquals(1, mySuscriber1.getRedCount());
		Assert.assertEquals(3, mySuscriber1.getCount());

		Assert.assertEquals(1, mySuscriber2.getBlueCount());
		Assert.assertEquals(1, mySuscriber2.getRedCount());
		Assert.assertEquals(3, mySuscriber2.getCount());

		Assert.assertEquals(0, deadEvents);
	}

	@Test
	public void testDeadEvent() {
		Assert.assertEquals(0, deadEvents);
		eventBusManager.post(new DummyEvent());
		Assert.assertEquals(1, deadEvents);
	}

	@Override
	public void onEvent(final Event event) {
		deadEvents++;
	}
}
