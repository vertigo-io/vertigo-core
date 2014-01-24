package io.vertigo.commons.codec.csv;

import io.vertigo.commons.codec.AbstractEncoderTest;
import io.vertigo.commons.codec.CodecManager;
import io.vertigo.commons.codec.Encoder;

import org.junit.Assert;
import org.junit.Test;

/**
 * Test du codec de validation pour un fichier CVS.
 * @author dchallas
 */
public final class CSVCodecTest extends AbstractEncoderTest<Encoder<String, String>, String, String> {
	/** {@inheritDoc} */
	@Override
	public Encoder<String, String> obtainCodec(final CodecManager codecManager) {
		return codecManager.getCsvEncoder();
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testNull() throws Exception {
		Assert.assertEquals("", codec.encode(null));
		// assertNull(csvCodec.decode(null));
	}

	/** {@inheritDoc} */
	@Override
	@Test
	public void testEncode() throws Exception {
		checkEncode("", "");
		checkEncode("1", "1");
		checkEncode("2,2", "2,2");
		checkEncode("3,3,", "3,3,");
		checkEncode(",4,4 4,4 ", ",4,4 4,4 ");
		checkEncode("\"5,5", "\"\"5,5");
		checkEncode("abc:def\13", "abc:def" + (char) 11);
	}
}
