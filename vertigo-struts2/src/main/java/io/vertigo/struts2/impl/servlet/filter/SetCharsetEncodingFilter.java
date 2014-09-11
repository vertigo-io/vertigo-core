package io.vertigo.struts2.impl.servlet.filter;

import io.vertigo.core.lang.Assertion;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Implémentation de javax.servlet.Filter utilisée affecter le charset de la request.
 * Doit-être le premier filter pour être efficace.
 * Le charset utilisé doit-être compatible avec la finalit� de la donnée (typiquement avec la BDD).
 * @author npiedeloup
 */
public final class SetCharsetEncodingFilter implements Filter {
	private String charset;

	/** {@inheritDoc} */
	@Override
	public void init(final FilterConfig filterConfig) throws ServletException {
		charset = filterConfig.getInitParameter("charset");
		Assertion.checkArgNotEmpty(charset);
	}

	/** {@inheritDoc} */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		request.setCharacterEncoding(charset);
		chain.doFilter(request, response);
	}

	/** {@inheritDoc} */
	@Override
	public void destroy() {
		//rien
	}
}
