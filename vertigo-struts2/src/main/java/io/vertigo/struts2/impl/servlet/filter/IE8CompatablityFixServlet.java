package io.vertigo.struts2.impl.servlet.filter;

import io.vertigo.core.lang.Assertion;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Filter qui permet de gérer la compatibilité avec IE8, en ajoutant le header X-UA-Compatible dans les responses.
 * @author npiedeloup
 */
public class IE8CompatablityFixServlet implements Filter {
	private static final List<String> ACCEPTED_MODES = Arrays.asList("edge", "9", "8", "7", "5", "EmulateIE7");
	private String mode;

	/** {@inheritDoc} */
	public void init(final FilterConfig filterConfig) throws ServletException {
		mode = filterConfig.getInitParameter("mode");
		Assertion.checkState(ACCEPTED_MODES.contains(mode), "Mode de compatibilité IE non géré {0} (modes ok :{1})", mode, ACCEPTED_MODES);
	}

	/** {@inheritDoc} */
	public void doFilter(final ServletRequest request, final ServletResponse response, final FilterChain chain) throws IOException, ServletException {
		((HttpServletResponse) response).setHeader("X-UA-Compatible", "IE=" + mode);
		chain.doFilter(request, response);
	}

	/** {@inheritDoc} */
	public void destroy() {
		//rien
	}

}
