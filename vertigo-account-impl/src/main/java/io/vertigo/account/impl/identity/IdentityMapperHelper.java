/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2017, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.account.impl.identity;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import io.vertigo.dynamo.domain.metamodel.DtDefinition;
import io.vertigo.lang.Assertion;

/**
 * @author npiedeloup
 * @param <S> Source type
 * @param <D> Destination type
 */
public final class IdentityMapperHelper<S, D> {

	private final Optional<DtDefinition> sourceDtDefinition;
	private final Optional<Class<? extends Enum>> destEnum;
	private final Optional<DtDefinition> destDtDefinition;

	private final String sourceToDestMappingStr;
	private final Set<String> reservedDestField = new HashSet<>();
	private final Set<D> mandatoryDestField = new HashSet<>();

	private final Map<D, S> destToSourceMapping = new HashMap<>(); //DestAttribute from SourceAttribute
	private final Map<String, S> reservedToSourceMapping = new HashMap<>(); //reservedField from SourceAttribute

	/**
	 * Constructor.
	 * @param destDtDefinition Destination dtDefinition
	 * @param sourceToDestMappingStr source to dest mapping
	 */
	public IdentityMapperHelper(final DtDefinition destDtDefinition, final String sourceToDestMappingStr) {
		sourceDtDefinition = Optional.empty();
		destEnum = Optional.empty();
		this.destDtDefinition = Optional.of(destDtDefinition);
		this.sourceToDestMappingStr = sourceToDestMappingStr;
	}

	/**
	 * Constructor.
	 * @param sourceDtDefinition Source dtDefinition
	 * @param destEnum Destination enum
	 * @param sourceToDestMappingStr source to dest mapping
	 */
	public IdentityMapperHelper(final DtDefinition sourceDtDefinition, final Class<? extends Enum> destEnum, final String sourceToDestMappingStr) {
		this.sourceDtDefinition = Optional.of(sourceDtDefinition);
		this.destEnum = Optional.of(destEnum);
		destDtDefinition = Optional.empty();
		this.sourceToDestMappingStr = sourceToDestMappingStr;
	}

	public IdentityMapperHelper<S, D> withReservedDestField(final String... fieldNames) {
		Assertion.checkNotNull(fieldNames);
		//-----
		reservedDestField.addAll(Arrays.asList(fieldNames));
		return this;
	}

	public IdentityMapperHelper<S, D> withMandatoryDestField(final D... fields) {
		Assertion.checkNotNull(fields);
		//-----
		mandatoryDestField.addAll(Arrays.asList(fields));
		return this;
	}

	public IdentityMapperHelper<S, D> parseAttributeMapping() {
		for (final String mapping : sourceToDestMappingStr.split("\\s*,\\s*")) {
			final String[] splitedMapping = mapping.split("\\s*:\\s*");
			Assertion.checkArgument(splitedMapping.length == 2, "Mapping should respect the pattern sourceFields:destFields :(like sourceAttr1:destAttr1, sourceAttr2:destAttr2, ... (check : {0})", sourceToDestMappingStr);
			Assertion.when(sourceDtDefinition.isPresent()).check(() -> sourceDtDefinition.get().contains(splitedMapping[2]), "sourceField {1} must be in DtDefinition {1}", splitedMapping[2], sourceDtDefinition.orElse(null));
			//It's reverse compared to config String : we keep a map of key:destAttribute -> value:sourceAttribute
			final S source;
			if (sourceDtDefinition.isPresent()) {
				source = (S) sourceDtDefinition.get().getField(splitedMapping[2]);
			} else {
				source = (S) splitedMapping[2];
			}
			if (!reservedDestField.contains(splitedMapping[1])) {
				Assertion.when(destDtDefinition.isPresent()).check(() -> destDtDefinition.get().contains(splitedMapping[1]), "destField {1} must be in DtDefinition {1}", splitedMapping[1], destDtDefinition.orElse(null));
				final D dest;
				if (destDtDefinition.isPresent()) {
					dest = (D) destDtDefinition.get().getField(splitedMapping[1]);
				} else if (destEnum.isPresent()) {
					dest = (D) Enum.valueOf(destEnum.get(), splitedMapping[1]);
				} else {
					dest = (D) splitedMapping[1];
				}

				destToSourceMapping.put(dest, source);
			} else {
				reservedToSourceMapping.put(splitedMapping[1], source);
			}
		}
		for (final D destField : mandatoryDestField) {
			Assertion.checkNotNull(destToSourceMapping.get(destField), "Mapping must declare mapping for destProperty {0}" + destField);
		}

		return this;
	}

	public Collection<D> destAttributes() {
		return destToSourceMapping.keySet();
	}

	public Collection<S> sourceAttributes() {
		return destToSourceMapping.values();
	}

	public S getSourceAttribute(final D dest) {
		return destToSourceMapping.get(dest);
	}

	public S getReservedSourceAttribute(final String reservedField) {
		return reservedToSourceMapping.get(reservedField);
	}

	public DtDefinition getDestDefinition() {
		return destDtDefinition.get();
	}

	public S getSourceIdField() {
		Assertion.checkArgument(destDtDefinition.isPresent() || sourceDtDefinition.isPresent(), "Can't determine id field, if nor source nor dest are Entity");
		if (destDtDefinition.isPresent() && destDtDefinition.get().getIdField().isPresent()) {
			return getSourceAttribute((D) destDtDefinition.get().getIdField().get());
		} else if (sourceDtDefinition.isPresent()) {
			Assertion.checkArgument(sourceDtDefinition.get().getIdField().isPresent(), "Can't determine id field, if nor source nor dest are Entity");
			return (S) sourceDtDefinition.get().getIdField().get();
		}
		throw new IllegalArgumentException("Can't determine id field, if nor source nor dest are Entity");
	}

}
