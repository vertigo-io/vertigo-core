package io.vertigo.rest;

import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.metamodel.Definition;
import io.vertigo.kernel.metamodel.Prefix;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * End point definition.
 * @author npiedeloup
 */
@Prefix("EP_")
public class EndPointDefinition implements Definition {
	public final class EndPointParam {
		private final String name;
		private final Class<?> type;

		EndPointParam(final String name, final Class<?> type) {
			this.name = name;
			this.type = type;
		}

		public String getName() {
			return name;
		}

		public Class<?> getType() {
			return type;
		}
	}

	public enum Verb {
		GET, POST, PUT, DELETE,
	}

	private final String name;
	private final String path;
	private final Verb verb;

	private final Method method; //Function gérant l'exectution du EndPoint	
	private final boolean needSession;
	private final boolean needAuthentification;

	private final List<EndPointParam> endPointParams = new ArrayList<>();

	public EndPointDefinition(final String name, final Verb verb, final String path, final Method method, final boolean needSession, final boolean needAuthentification) {
		Assertion.checkArgNotEmpty(name);
		Assertion.checkNotNull(verb);
		Assertion.checkArgNotEmpty(path);
		Assertion.checkNotNull(method);
		//---------------------------------------------------------------------
		this.name = name;
		this.verb = verb;
		this.path = path;

		this.method = method;
		this.needSession = needSession;
		this.needAuthentification = needAuthentification;
	}

	public String getName() {
		return name;
	}

	public String getPath() {
		return path;
	}

	public Verb getVerb() {
		return verb;
	}

	public Method getMethod() {
		return method;
	}

	public List<EndPointParam> getEndPointParams() {
		return Collections.unmodifiableList(endPointParams);
	}

	public void addParam(final String paramName, final Class paramType) {
		final EndPointParam endPointParam = new EndPointParam(paramName, paramType);
		endPointParams.add(endPointParam);
	}

	public boolean isNeedSession() {
		return needSession;
	}

	public boolean isNeedAuthentification() {
		return needAuthentification;
	}
}
