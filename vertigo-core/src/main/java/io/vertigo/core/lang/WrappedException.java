/*
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2026, Vertigo.io, team@vertigo.io
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
 * Exception wrapper for converting checked exceptions to unchecked.
 * Features:
 * - Preserves original stack trace
 * - Handles InvocationTargetException unwrapping
 * - Supports custom error messages
 * - Maintains existing RuntimeExceptions
 * 
 * Used to simplify exception handling while preserving error context.
 *
 * @author npiedeloup
 */
public final class WrappedException extends RuntimeException {
	private static final long serialVersionUID = 2209962160725174080L;

	/**
	 * Constructor.
	 * 
	 * @param message Context message
	 * @param cause Original exception
	 */
	private WrappedException(final String message, final Throwable cause) {
		super(message, cause);
	}

	/**
	 * Wraps any exception into a RuntimeException.
	 * Preserves original exception if already unchecked.
	 *
	 * @param th Original exception
	 * @return RuntimeException wrapper
	 */
	public static RuntimeException wrap(final Throwable th) {
		return wrap(th, null);
	}

	/**
	 * Wraps any exception into a RuntimeException with custom message.
	 * Preserves original exception if already unchecked.
	 *
	 * @param th Original exception
	 * @param msg Custom message
	 * @param params Message parameters
	 * @return RuntimeException wrapper
	 */
	public static RuntimeException wrap(final Throwable th, final String msg, final Object... params) {
		final Throwable t = th instanceof final InvocationTargetException ite
				? ite.getTargetException()
				: th;

		//WrapException are use to wrap unkowned exception the message can be null.
		//But we check a dev's exception have got a message, so in this api (with just th) we send "no message provided" if message is null
		var hasCustomMessage = !StringUtil.isBlank(msg);
		final String message = hasCustomMessage ? StringUtil.format(msg, params) : t.getMessage();

		if (t instanceof final RuntimeException rte) {
			if (hasCustomMessage) {
				rte.addSuppressed(new VSystemException(message));
			}
			throw rte;
		}
		if (t instanceof final Error error) {
			if (hasCustomMessage) {
				error.addSuppressed(new VSystemException(message));
			}
			throw error;
		}
		throw new WrappedException(message, t);
	}

	/**
	 * Retrieves the original wrapped exception.
	 * 
	 * @return Original exception
	 */
	public Throwable unwrap() {
		return getCause();
	}
}
