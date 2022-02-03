/**
 * vertigo - application development platform
 *
 * Copyright (C) 2013-2022, Vertigo.io, team@vertigo.io
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
package io.vertigo.core.node.component.aop.data.aspects;

import io.vertigo.core.node.component.aop.Aspect;
import io.vertigo.core.node.component.aop.AspectMethodInvocation;

/**
 * @author pchretien
 */
public final class TenMoreAspect implements Aspect {
	@Override
	public Integer invoke(final Object[] args, final AspectMethodInvocation methodInvocation) {
		return (Integer) methodInvocation.proceed(args) + 10;
	}

	@Override
	public Class<TenMore> getAnnotationType() {
		return TenMore.class;
	}
}
