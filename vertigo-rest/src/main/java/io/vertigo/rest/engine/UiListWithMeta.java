package io.vertigo.rest.engine;

import io.vertigo.kernel.lang.Assertion;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public final class  UiListWithMeta<D> {
		private final List<D> value;
		private final Map<String, Object> metas = new HashMap<>();
		
		public UiListWithMeta(final List<D> value) {
			Assertion.checkNotNull(value);
			//-----------------------------------------------------------------
			this.value = value;
		}

		void addMeta(final String key, final Object value) {
			metas.put(key, value);
		}

	}