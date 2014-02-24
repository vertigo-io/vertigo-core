package io.vertigo.commons.codec;

import io.vertigo.AbstractTestCase2JU4;
import io.vertigo.commons.impl.codec.CodecManagerImpl;
import io.vertigo.kernel.di.configurator.ComponentSpaceConfigBuilder;

import javax.inject.Inject;

import org.junit.Assert;

/**
 * @author dchallas
 * @param <S> Type Source � encoder
 * @param <T> Type cible, r�sultat de l'encodage
 */
public abstract class AbstractEncoderTest<C extends Encoder<S, T>, S, T> extends AbstractTestCase2JU4 {
	protected static final String TEXT = "Les sanglots longs des violons de l'automne blessent mon coeur d'une langueur monotone.";
	protected C codec;

	@Inject
	private CodecManager codecManager;

	@Override
	protected void configMe(final ComponentSpaceConfigBuilder componentSpaceConfigBuilder) {
		// @formatter:off
		componentSpaceConfigBuilder
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
	 * test l'encodage et le d�codage avec les chaines null.
	 * @throws Exception si probl�me
	 */
	public abstract void testNull() throws Exception;

	/**
	 * test l'encodage de chaines non null.
	 * @throws Exception si probl�me
	 */
	public abstract void testEncode() throws Exception;

	protected final void checkEncode(final S value, final T expectedEncodedValue) {
		final T encodedValue = codec.encode(value);
		Assert.assertEquals(expectedEncodedValue, encodedValue);
		checkEncodedValue(encodedValue);
	}

	/**
	 * 
	 * @param encodedValue
	 */
	protected void checkEncodedValue(final T encodedValue) {
		// � implementer si besoin
	}

	protected final CodecManager getCodecManager() {
		return codecManager;
	}

}
