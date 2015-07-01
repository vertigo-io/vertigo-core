package io.vertigo.vega.plugins.rest.handler.reader;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.lang.Assertion;
import io.vertigo.vega.plugins.rest.handler.RouteContext;
import io.vertigo.vega.rest.engine.JsonEngine;
import io.vertigo.vega.rest.metamodel.EndPointParam;
import io.vertigo.vega.rest.metamodel.EndPointParam.RestParamType;
import io.vertigo.vega.rest.model.UiListState;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.inject.Inject;

import spark.QueryParamsMap;
import spark.Request;

public final class QueryJsonReader implements JsonReader<String> {

	private final JsonEngine jsonWriterEngine;

	/**
	 * @param jsonWriterEngine jsonWriterEngine
	 */
	@Inject
	public QueryJsonReader(final JsonEngine jsonWriterEngine) {
		Assertion.checkNotNull(jsonWriterEngine);
		//-----
		this.jsonWriterEngine = jsonWriterEngine;
	}

	/** {@inheritDoc} */
	@Override
	public RestParamType[] getSupportedInput() {
		return new RestParamType[] { RestParamType.Query };
	}

	/** {@inheritDoc} */
	@Override
	public Class<String> getSupportedOutput() {
		return String.class;
	}

	/** {@inheritDoc} */
	@Override
	public String extractData(final Request request, final EndPointParam endPointParam, final RouteContext routeContext) {
		Assertion.checkArgument(getSupportedInput()[0].equals(endPointParam.getParamType()), "This JsonReader can't read the asked request ParamType {0}. Only {1} is supported", endPointParam.getParamType(), Arrays.toString(getSupportedInput()));
		//-----
		return readQueryValue(request.queryMap(), endPointParam);
	}

	private String readQueryValue(final QueryParamsMap queryMap, final EndPointParam endPointParam) {
		final Class<?> paramClass = endPointParam.getType();
		final String paramName = endPointParam.getName();
		if (queryMap == null) {
			return null;
		}
		if (UiListState.class.isAssignableFrom(paramClass)
				|| DtObject.class.isAssignableFrom(paramClass)) {
			return convertToJson(queryMap, endPointParam.getName());
		}
		return queryMap.get(paramName).value();
	}

	private String convertToJson(final QueryParamsMap queryMap, final String queryPrefix) {
		final String checkedQueryPrefix = queryPrefix.isEmpty() ? "" : queryPrefix + ".";
		final Map<String, Object> queryParams = new HashMap<>();
		for (final Entry<String, String[]> entry : queryMap.toMap().entrySet()) {
			if (entry.getKey().startsWith(checkedQueryPrefix)) {
				final String[] value = entry.getValue();
				final Object simplerValue = value.length == 0 ? null : value.length == 1 ? value[0] : value;
				queryParams.put(entry.getKey().substring(checkedQueryPrefix.length()), simplerValue);
			}
		}
		return jsonWriterEngine.toJson(queryParams);
	}
}
