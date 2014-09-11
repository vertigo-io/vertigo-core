package io.vertigo.struts2.impl.servlet;

import io.vertigo.core.component.Container;
import io.vertigo.core.lang.Assertion;

import java.util.Set;

import javax.servlet.http.HttpServletRequest;

/**
 * Container Vertigo wrapper de parametres HttpRequest.
 * @author npiedeloup
 */
public final class RequestContainerWrapper implements Container {
	private final HttpServletRequest request;

	/**
	 * Constructeur.
	 * @param request Request
	 */
	public RequestContainerWrapper(final HttpServletRequest request) {
		this.request = request;
	}

	/** {@inheritDoc} */
	@Override
	public boolean contains(final String id) {
		return request.getParameterMap().containsKey(id);
	}

	/** {@inheritDoc} */
	@Override
	public <T> T resolve(final String id, final Class<T> componentClass) {
		final String[] values = request.getParameterValues(id);
		Assertion.checkNotNull(values, "Le paramètre {0} est obligatoire.", id);
		final String firstValue = values.length > 0 ? values[0] : null;

		if (String[].class.equals(componentClass)) {
			return componentClass.cast(values);
		}
		if (String.class.equals(componentClass)) {
			return componentClass.cast(firstValue);
		} else if (Long.class.equals(componentClass)) {
			return firstValue == null || firstValue.isEmpty() ? null : componentClass.cast(Long.valueOf(firstValue));
		} else if (Integer.class.equals(componentClass)) {
			return firstValue == null || firstValue.isEmpty() ? null : componentClass.cast(Integer.valueOf(firstValue));
		}
		throw new IllegalArgumentException("Le type du paramètre " + id + " (" + componentClass.getName() + ") n'est pas support� (String[], String, Long, Integer)");
	}

	/** {@inheritDoc} */
	@Override
	public Set<String> keySet() {
		return request.getParameterMap().keySet();
	}

}
