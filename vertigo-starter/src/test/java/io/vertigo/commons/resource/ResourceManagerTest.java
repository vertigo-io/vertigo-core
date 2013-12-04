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

import io.vertigo.AbstractTestCase2JU4;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;
import io.vertigo.kernel.exception.VRuntimeException;
import io.vertigoimpl.commons.resource.ResourceManagerImpl;
import io.vertigoimpl.plugins.commons.resource.java.ClassPathResourceResolverPlugin;

import java.net.URL;

import javax.inject.Inject;

import org.junit.Assert;
import org.junit.Test;

/**
 * @author pchretien
 */
public final class ResourceManagerTest extends AbstractTestCase2JU4 {
	@Inject
	private ResourceManager resourceManager;

	@Override
	protected void configMe(final ComponentSpaceConfigBuilder componentSpaceConfiguilder) {
		// @formatter:off
		componentSpaceConfiguilder
		.beginModule("spaces").
			beginComponent(ResourceManager.class, ResourceManagerImpl.class)
				.beginPlugin(ClassPathResourceResolverPlugin.class).endPlugin()
			.endComponent()
		.endModule();	
		// @formatter:on
	}

	@Test(expected = NullPointerException.class)
	public void testNullResource() {
		resourceManager.resolve(null);

	}

	@Test(expected = VRuntimeException.class)
	public void testEmptyResource() {
		resourceManager.resolve("nothing");
	}

	@Test
	public void testResourceSelector() {
		final String expected = "io/vertigo/commons/resource/hello.properties";
		final URL url = resourceManager.resolve(expected);
		Assert.assertEquals(true, url.getPath().indexOf(expected) != -1);
	}
	//
	//	@Test
	//	public void subTypeOfClassSelector() {
	//		final Set<Class<? extends I2>> subTypes = resourceManager.getClassSelector().getSubTypesOf(TestModel.I2.class);
	//		Assert.assertEquals(4, subTypes.size());
	//		Assert.assertTrue(subTypes.contains(TestModel.C1.class));
	//		Assert.assertTrue(subTypes.contains(TestModel.C2.class));
	//		Assert.assertTrue(subTypes.contains(TestModel.C3.class));
	//		Assert.assertTrue(subTypes.contains(TestModel.C5.class));
	//	}
	//
	//	@Test
	//	public void SubTypeOfClassSelector() {
	//		final Set<Class<?>> subTypes = resourceManager.getClassSelector().getTypesAnnotatedWith(TestModel.AC2.class);
	//		Assert.assertEquals(3, subTypes.size());
	//		Assert.assertTrue(subTypes.contains(TestModel.C2.class));
	//		Assert.assertTrue(subTypes.contains(TestModel.C3.class));
	//		Assert.assertTrue(subTypes.contains(TestModel.I3.class));
	//	}

}
