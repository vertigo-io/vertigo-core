package io.vertigo.studio.impl.masterdata;

import io.vertigo.core.component.Plugin;
import io.vertigo.studio.masterdata.MasterDataValues;

/**
 * Plugin that retrieve raw values of masterdata.
 * @author mlaroche
 *
 */
public interface MasterDataValueProviderPlugin extends Plugin {

	/**
	 * Return masterdata values
	 * @return masterdata values
	 */
	MasterDataValues getValues();

}
