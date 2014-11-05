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
package io.vertigo.commons.codec;

import io.vertigo.AbstractTestCaseJU4;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.core.config.AppConfigBuilder;

import javax.inject.Inject;

import org.junit.Assert;

/**
 * @author dchallas
 * @param <S> Type Source à encoder
 * @param <T> Type cible, résultat de l'encodage
 */
public abstract class AbstractEncoderTest<C extends Encoder<S, T>, S, T> extends AbstractTestCaseJU4 {
	protected static final String TEXT = "Les sanglots longs des violons de l'automne blessent mon coeur d'une langueur monotone.";
	protected C codec;

	@Inject
	private CodecManager codecManager;

	@Override
	protected void configMe(final AppConfigBuilder appConfigBuilder) {
		// @formatter:off
		appConfigBuilder
			.beginModule("commons").
				beginComponent(CodecManager.class, CodecManagerImpl.class).endComponent()
			.endModule();
		// @formatter:on
	}

	protected abstract C obtainCodec(CodecManager inCodecManager);

	/** {@inheritDoc} */
	@Override
	public final void doSetUp() {
		codec = obtainCodec(codecManager);
	}

	/**
	 * test l'encodage et le décodage avec les chaines null.
	 * @throws Exception si problème
	 */
	public abstract void testNull() throws Exception;

	/**
	 * test l'encodage de chaines non null.
	 * @throws Exception si problème
	 */
	public abstract void testEncode() throws Exception;

	protected final void checkEncode(final S value, final T expectedEncodedValue) {
		final T encodedValue = codec.encode(value);
		Assert.assertEquals(expectedEncodedValue, encodedValue);
		checkEncodedValue(encodedValue);
	}

	protected void checkEncodedValue(final T encodedValue) {
		// à implementer si besoin
	}

	protected final CodecManager getCodecManager() {
		return codecManager;
	}

}
