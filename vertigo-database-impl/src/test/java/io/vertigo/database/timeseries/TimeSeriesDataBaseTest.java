/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2018, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.database.timeseries;

import java.time.Instant;
import java.util.Collections;

import javax.inject.Inject;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.vertigo.app.AutoCloseableApp;
import io.vertigo.app.Home;
import io.vertigo.core.component.di.injector.DIInjector;

/**
 * Test of the IoT services.
 *
 * @author mlaroche
 */
public final class TimeSeriesDataBaseTest {

	private static AutoCloseableApp app;

	@BeforeAll
	public static final void setUp() throws Exception {
		app = new AutoCloseableApp(TimeSeriesDataBaseTestAppConfig.config());
	}

	@AfterAll
	public static final void tearDown() throws Exception {
		if (app != null) {
			app.close();
		}
	}

	public final void setUpInjection() throws Exception {
		if (app != null) {
			DIInjector.injectMembers(this, Home.getApp().getComponentSpace());
		}
	}

	@BeforeEach
	public void doSetUp() throws Exception {
		setUpInjection();
	}

	@Inject
	private TimeSeriesDataBaseManager timeSeriesDataBaseManager;

	public void testInsertMeasure() {
		final Measure measure = Measure.builder("test")
				.time(Instant.now())
				.addField("temp", 12)
				.build();
		timeSeriesDataBaseManager.insertMeasure("vertigo-test", measure);
	}

	@Test
	public void testReadMeasures() {
		timeSeriesDataBaseManager.getTimeSeries(
				"vertigo-test",
				Collections.singletonList("temp:mean"),
				DataFilter.builder("test").build(),
				TimeFilter.builder("now() - 1h", "now()").withTimeDim("1m").build());
	}
}
