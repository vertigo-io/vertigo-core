package io.vertigo.vega.impl.rest.multipart;

import io.vertigo.dynamo.file.model.KFile;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import spark.QueryParamsMap;
import spark.Request;
import spark.Session;

/**
 * Wrapping de la requête Http pour la gestion des requête multipart.
 * 
 * @author npiedeloup
 * @version $Id: RequestWrapper.java,v 1.6 2013/06/25 10:57:08 pchretien Exp $
 */
public final class RequestWrapper extends Request {

	private final Map<String, String[]> parameters = new HashMap<>();
	private final Request innerRequest;
	private final HttpServletRequest rawRequest;
	private final QueryParamsMap queryParamsMap;

	/**
	 * Creates a new KRequestWrapper object.
	 * 
	 * @param request Requête à gérer.
	 * @throws Exception Exception de lecture du flux
	 */
	RequestWrapper(final Request innerRequest, final Map<String, String[]> parameters, final Map<String, KFile> uploadedFiles, final Map<String, RuntimeException> tooBigFiles) {
		this.innerRequest = innerRequest;
		rawRequest = new HttpRequestWrapper(innerRequest.raw(), parameters, uploadedFiles, tooBigFiles);
		queryParamsMap = new QueryParamsMap(rawRequest);
	}

	@Override
	public String params(final String param) {
		return innerRequest.params(param);
	}

	@Override
	public String[] splat() {
		return innerRequest.splat();
	}

	@Override
	public String requestMethod() {
		return innerRequest.requestMethod();
	}

	@Override
	public String scheme() {
		return innerRequest.scheme();
	}

	@Override
	public String host() {
		return innerRequest.host();
	}

	@Override
	public String userAgent() {
		return innerRequest.userAgent();
	}

	@Override
	public int port() {
		return innerRequest.port();
	}

	@Override
	public String pathInfo() {
		return innerRequest.pathInfo();
	}

	@Override
	public String servletPath() {
		return innerRequest.servletPath();
	}

	@Override
	public String contextPath() {
		return innerRequest.contextPath();
	}

	@Override
	public String url() {
		return innerRequest.url();
	}

	@Override
	public String contentType() {
		return innerRequest.contentType();
	}

	@Override
	public String ip() {
		return innerRequest.ip();
	}

	@Override
	public String body() {
		return innerRequest.body();
	}

	@Override
	public int contentLength() {
		return innerRequest.contentLength();
	}

	@Override
	public String queryParams(final String queryParam) {
		final String[] values = parameters.get(queryParam);
		if (values == null) {
			return null;
		}
		// 1ère occurence.
		return values[0];
		//return innerRequest.queryParams(queryParam);
	}

	@Override
	public String headers(final String header) {
		return innerRequest.headers(header);
	}

	@Override
	public Set<String> queryParams() {
		return Collections.unmodifiableSet(parameters.keySet());
		//return innerRequest.queryParams();
	}

	@Override
	public Set<String> headers() {
		return innerRequest.headers();
	}

	@Override
	public String queryString() {
		return innerRequest.queryString();
	}

	@Override
	public void attribute(final String attribute, final Object value) {
		innerRequest.attribute(attribute, value);
	}

	@Override
	public Object attribute(final String attribute) {
		return innerRequest.attribute(attribute);
	}

	@Override
	public Set<String> attributes() {
		return innerRequest.attributes();
	}

	@Override
	public HttpServletRequest raw() {
		return rawRequest;
	}

	@Override
	public QueryParamsMap queryMap() {
		return queryParamsMap;
	}

	@Override
	public QueryParamsMap queryMap(final String key) {
		return queryParamsMap.get(key);
	}

	@Override
	public Session session() {
		return innerRequest.session();
	}

	@Override
	public Session session(final boolean create) {
		return innerRequest.session(create);
	}

	@Override
	public Map<String, String> cookies() {
		return innerRequest.cookies();
	}

	@Override
	public String cookie(final String name) {
		return innerRequest.cookie(name);
	}

}
