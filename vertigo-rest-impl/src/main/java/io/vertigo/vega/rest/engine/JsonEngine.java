package io.vertigo.vega.rest.engine;

import io.vertigo.dynamo.domain.model.DtObject;

import java.util.Map;
import java.util.Set;

/**
 * Convert Object to Json, and json to Object.
 * Support :
 * - Exception
 * - exclude fields
 * - Security token
 * - UiObject (contains a DtObject buffer and security token if present)
 * @author npiedeloup (17 juil. 2014 11:56:17)
 */
public interface JsonEngine {

	/**
	 * Standard convert full object to Json. 
	 * @param data Object
	 * @return Json string
	 */
	String toJson(Object data);

	/**
	 * Convert object to Json but excluded fields. 
	 * @param data Object 
	 * @param excludedFields Set of fields to include (empty means all fields include)
	 * @param excludedFields Set of fields to exclude
	 * @return Json string
	 */
	String toJson(Object data, final Set<String> includedFields, Set<String> excludedFields);

	/**
	 * Convert Exception to Json
	 * @param th Throwable
	 * @return Json string
	 */
	String toJsonError(Throwable th);

	/**
	 * Convert object to Json but excluded fields. 
	 * @param data Object
	 * @param tokenId token to include in Json (as a serverSideToken field)
	 * @param excludedFields Set of fields to include (empty means all fields include)
	 * @param excludedFields Set of fields to exclude
	 * @return Json string
	 */
	String toJsonWithTokenId(Object data, String tokenId, final Set<String> includedFields, Set<String> excludedFields);

	/**
	 * Standard convert Json to object.
	 * While converting accept missing object fields and unknown object fields (and then just forgot json value) 
	 * @param <D> Object type
	 * @param json Json string
	 * @param paramClass Object class
	 * @return Object filled with json typed data 
	 */
	<D extends Object> D fromJson(String json, Class<D> paramClass);

	/**
	 * Specific convertion Json to UiObject.
	 * UiObject is used as a buffer from client input.
	 * While converting accept missing object fields and unknown object fields (and then just forgot json value) 
	 * @param <D> Object type
	 * @param json Json string
	 * @param paramClass Object class
	 * @return UiObject filled with a DtObject partially filled and the accessTOken if present 
	 */
	<D extends DtObject> UiObject<D> uiObjectFromJson(String json, Class<D> paramClass);

	/**
	 * Specific convertion Json to UiContext.
	 * UiContext is used as a buffer from client input.
	 * @param <D> Object type
	 * @param json Json string
	 * @param paramClass Object class
	 * @return UiContext filled with a DtObject partially filled and the accessTOken if present 
	 */
	UiContext uiContextFromJson(String json, Map<String, Class<?>> paramClasses);

}
