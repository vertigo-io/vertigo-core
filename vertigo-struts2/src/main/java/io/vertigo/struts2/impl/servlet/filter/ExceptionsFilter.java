package io.vertigo.struts2.impl.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

/**
 * Implémentation de javax.servlet.Filter utilisée pour placer dans la request l'execption.
 * @author npiedeloup
 */
public final class ExceptionsFilter implements Filter {

	/** {@inheritDoc} */
	@Override
	public void doFilter(final ServletRequest req, final ServletResponse res, final FilterChain chain) throws IOException, ServletException {
		try {
			chain.doFilter(req, res);
		} catch (final Throwable th) {
			req.setAttribute("javax.servlet.error.exception", th);
			throw th;
		}
	}

	@Override
	public void destroy() {
		//rien
	}

	@Override
	public void init(final FilterConfig arg0) throws ServletException {
		//rien
	}
}
