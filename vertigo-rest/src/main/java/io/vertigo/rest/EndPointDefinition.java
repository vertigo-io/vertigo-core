/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
 * KleeGroup, Centre d'affaire la Boursidiere - BP 159 - 92357 Le Plessis Robinson Cedex - France
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
	public static final class EndPointParam {
		private final String name;
		private final Class<?> type;

		EndPointParam(final String name, final Class<?> type) {
			Assertion.checkArgNotEmpty(name);
			Assertion.checkNotNull(type);
			//-----------------------------------------------------------------
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

	//TODO 
	//TODO 
	//TODO must be immmutable, without any add,set or any methods like that
	//TODO 
	//TODO 
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
