/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2025, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.lang;

import java.lang.reflect.InvocationTargetException;

import io.vertigo.core.util.StringUtil;

/**
 * Encapsulates checked Exception inside a RuntimeException.
 * Inspired by gnu.mapping.WrappedException.
 *
 * @author npiedeloup
 */
public final class WrappedException extends RuntimeException {
	private static final long serialVersionUID = 2209962160725174080L;

	/**
	 * Constructor.
	 * @param message the context message
	 * @param cause the cause exception
	 */
	private WrappedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Coerces argument to a RuntimeException.
	 * Re-throws as a non-checked exception.
	 * This method never returns, in spite of the return type.
	 * This allows the call to be written as: throw WrappedExcepton.rethrow(th) so javac and the verifier can know the code doesn't return.
	 * @param th Cause exception
	 * @return RuntimeException runtime
	 */
	public static RuntimeException wrap(final Throwable th) {
		return wrap(th, th.getMessage());
	}

	/**
	 * Coerces argument to a RuntimeException.
	 * Re-throw as a non-checked exception. This method never returns, in spite of the return type.
	 * This allows the call to be written as: throw WrappedExcepton.rethrow(th) so javac and the verifier can know the code doesn't return.
	 * @param th Cause exception
	 * @param msg Context message
	 * @param params Context message params
	 * @return RuntimeException runtime
	 */
	public static RuntimeException wrap(final Throwable th, final String msg, final Object... params) {
		final Throwable t = (th instanceof InvocationTargetException ite)
				? ite.getTargetException()
				: th;

		//WrapException are use to wrap unkowned exception the message can be null.
		//But we check a dev's exception have got a message, so in this api (with just th) we send "no message provided" if message is null
		final String message = StringUtil.isBlank(msg) ? "no message provided" : StringUtil.format(msg, params);
		if (t instanceof RuntimeException rte) {
			rte.addSuppressed(new VSystemException(message));
			throw rte;
		}
		if (t instanceof Error error) {
			error.addSuppressed(new VSystemException(message));
			throw error;
		}
		throw new WrappedException(message, t);
	}

	/**
	 * Gets the orginal exception.
	 * @return the orginal exception that has been wrapped
	 */
	public Throwable unwrap() {
		return getCause();
	}
}
