package io.vertigo.rest.impl.rest.handler;

import io.vertigo.dynamo.domain.model.DtObject;
import io.vertigo.rest.rest.engine.UiObject;
import io.vertigo.rest.rest.metamodel.EndPointParam;
import io.vertigo.rest.rest.validation.UiContextResolver;
import io.vertigo.rest.rest.validation.UiMessageStack;
import spark.Request;

/**
* @author npiedeloup 
*/
public final class RouteContext {
	private static final String UI_MESSAGE_STACK = "UiMessageStack";

	private final Request request;
	private final UiContextResolver uiContextResolver;

	RouteContext(final Request request) {
		this.request = request;
		uiContextResolver = new UiContextResolver();
		request.attribute(UI_MESSAGE_STACK, new UiMessageStack(uiContextResolver));
	}

	public UiMessageStack getUiMessageStack() {
		return (UiMessageStack) request.attribute(UI_MESSAGE_STACK);
	}

	public void setParamValue(final EndPointParam endPointParam, final Object value) {
		request.attribute(endPointParam.getFullName(), value);
	}

	public Object getParamValue(final EndPointParam endPointParam) {
		return request.attribute(endPointParam.getFullName());
	}

	public void registerUiObject(final EndPointParam endPointParam, final UiObject uiObject) {
		request.attribute(endPointParam.getFullName(), uiObject);
	}

	public void registerUpdatedDto(final EndPointParam endPointParam, final String contextKey, final DtObject updatedDto) {
		uiContextResolver.register(contextKey, updatedDto);
		request.attribute(endPointParam.getFullName() + "-input", request.attribute(endPointParam.getFullName()));
		request.attribute(endPointParam.getFullName(), updatedDto);
	}

}
