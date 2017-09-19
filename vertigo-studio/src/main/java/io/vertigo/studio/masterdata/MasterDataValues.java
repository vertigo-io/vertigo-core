package io.vertigo.studio.masterdata;

import java.util.HashMap;
import java.util.Map;

/**
 * A raw masterdata values is just a map of key/value pairs :
 * 	-key : the name of the masterdata name
 *  -value: a map of all the values. each value is identified by a name
 *
 * @author mlaroche
 *
 */
public class MasterDataValues extends HashMap<String, Map<String, MasterDataValue>> {
	private static final long serialVersionUID = -5252370330938111223L;
}
