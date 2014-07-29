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
package io.vertigo.labs.impl.gedcom;

import io.vertigo.commons.resource.ResourceManager;
import io.vertigo.dynamo.domain.model.DtList;
import io.vertigo.dynamo.kvdatastore.KVDataStoreManager;
import io.vertigo.dynamo.transaction.KTransactionManager;
import io.vertigo.dynamo.transaction.KTransactionWritable;
import io.vertigo.kernel.lang.Assertion;
import io.vertigo.kernel.lang.Option;
import io.vertigo.labs.gedcom.GedcomManager;
import io.vertigo.labs.gedcom.Individual;
import io.vertigo.labs.geocoder.GeoCoderManager;
import io.vertigo.labs.geocoder.GeoLocation;

import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;
import javax.inject.Named;

import org.gedcom4j.model.Family;
import org.gedcom4j.model.IndividualEvent;
import org.gedcom4j.parser.GedcomParser;

public final class GedcomManagerImpl implements GedcomManager {
	private final String storeName;
	private final GedcomParser gp;
	private final GeoCoderManager geoCoderManager;
	private final KTransactionManager transactionManager;
	private final KVDataStoreManager kvDataStoreManager;
	private final Map<String, GeoLocation> cache = Collections.synchronizedMap(new HashMap<String, GeoLocation>());
	private final Map<String, DtList<Individual>> children = new HashMap<>();

	@Inject
	public GedcomManagerImpl(@Named("storeName") final String storeName, final KVDataStoreManager kvDataStoreManager, final KTransactionManager transactionManager, final GeoCoderManager geoCoderManager, final ResourceManager resourceManager, @Named("gedcom") final String gedcomResource) {
		Assertion.checkArgNotEmpty(storeName);
		Assertion.checkNotNull(kvDataStoreManager);
		Assertion.checkNotNull(transactionManager);
		Assertion.checkNotNull(geoCoderManager);
		Assertion.checkNotNull(resourceManager);
		Assertion.checkNotNull(gedcomResource);
		// ---------------------------------------------------------------------
		this.storeName = storeName;
		this.kvDataStoreManager = kvDataStoreManager;
		this.transactionManager = transactionManager;
		this.geoCoderManager = geoCoderManager;

		final URL gedcomURL = resourceManager.resolve(gedcomResource);
		gp = new GedcomParser();
		try {
			gp.load(gedcomURL.getFile());
		} catch (final Exception e) {
			throw new RuntimeException("chargement du fichier gedcom '" + gedcomResource + "' impossible", e);
		}
	}

	private static String buildId(final org.gedcom4j.model.Individual gindividual) {
		return gindividual.xref.toString();
	}

	public DtList<Individual> getAllIndividuals() {
		final Map<String, Individual> map = new HashMap<>();
		final DtList<Individual> individuals = new DtList<>(Individual.class);
		for (final org.gedcom4j.model.Individual gindividual : getIndividuals()) {
			final String id = buildId(gindividual);
			final Individual individual = new Individual();
			map.put(id, individual);
			individuals.add(individual);
			individual.setId(id);

			individual.setGivenName(gindividual.names.get(0).givenName.value);
			individual.setSurName(gindividual.names.get(0).surname.value);
			if (gindividual.sex != null) {
				individual.setSex(gindividual.sex.value);
			}
			for (final IndividualEvent individualEvent : gindividual.events) {

				switch (individualEvent.type) {
					case DEATH:
						if (individualEvent.date != null) {
							individual.setDeathDate(individualEvent.date.value);
						}
						if (individualEvent.place != null) {
							individual.setDeathPlace(individualEvent.place.placeName);
							individual.setLocation(buildLocation(individualEvent.place.placeName));
						}

						break;
					case BIRTH:
						if (individualEvent.date != null) {
							individual.setBirthDate(individualEvent.date.value);
						}
						if (individualEvent.place != null) {
							individual.setBirthPlace(individualEvent.place.placeName);
							individual.setLocation(buildLocation(individualEvent.place.placeName));
						}
						break;
					case ADOPTION:
					case ARRIVAL:
					case BAPTISM:
					case BAR_MITZVAH:
					case BAS_MITZVAH:
					case BLESSING:
					case BURIAL:
					case CENSUS:
					case CHRISTENING:
					case CHRISTENING_ADULT:
					case CONFIRMATION:
					case CREMATION:
					case EMIGRATION:
					case EVENT:
					case FIRST_COMMUNION:
					case GRADUATION:
					case IMMIGRATION:
					case NATURALIZATION:
					case ORDINATION:
					case PROBATE:
					case RETIREMENT:
					case WILL:
					default:
						//on ne gère que les evts précédents
						break;
				}
			}

		}
		//Relations 
		for (final org.gedcom4j.model.Individual gindividual : getIndividuals()) {
			final String id = buildId(gindividual);
			final DtList<Individual> descendants = new DtList<>(Individual.class);
			children.put(id, descendants);
			for (final org.gedcom4j.model.Individual descendant : gindividual.getDescendants()) {
				descendants.add(map.get(buildId(descendant)));
			}
		}
		return individuals;
	}

	private GeoLocation buildLocation(final String address) {
		Assertion.checkArgNotEmpty(address);
		//---------------------------------------------------------------------
		final String key = address.trim().toLowerCase();
		//System.out.println("buildLocation "+key);
		GeoLocation geoLocation;
		try (KTransactionWritable transaction = transactionManager.createCurrentTransaction();) {
			geoLocation = cache.get(key);
			if (geoLocation == null) {
				final Option<GeoLocation> storedLocation = kvDataStoreManager.find(storeName, key, GeoLocation.class);
				//System.out.println("    cache "+storedLocation.isDefined());
				if (storedLocation.isEmpty()) {
					geoLocation = geoCoderManager.findLocation(key);
					//-----------------
					kvDataStoreManager.put(storeName, key, geoLocation);
					transaction.commit();
				} else {
					geoLocation = storedLocation.get();
				}
			}
		}
		return geoLocation;
	}

	public Collection<org.gedcom4j.model.Individual> getIndividuals() {
		return gp.gedcom.individuals.values();
	}

	public Collection<Family> getFamilies() {
		return gp.gedcom.families.values();
		// Submitter submitter =
		// gp.gedcom.submitters.values().iterator().next();
		// for (Family f : gp.gedcom.families.values()) {
		// if (f.husband != null && f.wife != null) {
		// System.out.println(f.husband.names.get(0).basic
		// + " married " + f.wife.names.get(0).basic);
		// }
		// }
	}

	public DtList<Individual> getChildren(final Individual individual) {
		Assertion.checkNotNull(individual);
		//---------------------------------------------------------------------
		final String id = individual.getId();
		return children.containsKey(id) ? children.get(id) : new DtList<Individual>(Individual.class);
	}
	// public findFamily(String name){
	//
	// }

	//	public void test() {
	//		Set<StringWithCustomTags> names = new HashSet<>();
	//		for (org.gedcom4j.model.Individual individual : getIndividuals()) {
	//			names.add(individual.names.get(0).givenName);
	//		}
	//		for (StringWithCustomTags name : names) {
	//			System.out.println("-" + name);
	//		}
	//
	//		Set<String> eventTypes = new HashSet<>();
	//		int ff = 0;
	//		int fhn = 0;
	//		int fwn = 0;
	//		int ns0 = 0, ns1 = 0, nsx = 0;
	//		for (Family family : getFamilies()) {
	//			/*	    if (i++ > 10)
	//				     break;
	//				     */
	//			ff++;
	//			if (family.wife == null || family.husband == null) {
	//				if (family.wife == null)
	//					fwn++;
	//				if (family.husband == null)
	//					fhn++;
	//				if (family.husband == null && family.wife == null)
	//					System.out.println("Family null " + family);
	//			} else {
	//				/*		System.out.println("Family");
	//						System.out.println("  Husband "
	//							+ family.husband.formattedName());
	//						System.out.println("  Husband " + family.husband.names.size());
	//						System.out.println("  Husband " + family.husband.names);
	//						System.out.println("  Wife " + family.wife.formattedName());
	//				//		System.out.println("  Wife " + family.wife.names.get(0).basic);
	//						System.out.println("  Wife " + family.wife.names.get(0).givenName);//
	//				//		System.out.println("  Wife " + family.wife.names.get(0).nickname);
	//						System.out.println("  Wife " + family.wife.names.get(0).surname); //
	//						System.out.println("  Wife " + family.wife.events); //
	//				*/
	//			}
	//			if (family.wife != null) {
	//				if (family.wife.names.size() == 0)
	//					ns0++;
	//				if (family.wife.names.size() == 1)
	//					ns1++;
	//				if (family.wife.names.size() > 1)
	//					nsx++;
	//				for (IndividualEvent event : family.wife.events) {
	//					eventTypes.add(event.type.name());
	//				}
	//			}
	//			if (family.husband != null) {
	//				if (family.husband.names.size() == 0)
	//					ns0++;
	//				if (family.husband.names.size() == 1)
	//					ns1++;
	//				if (family.husband.names.size() > 1)
	//					nsx++;
	//				for (IndividualEvent event : family.husband.events) {
	//					eventTypes.add(event.type.name());
	//				}
	//			}
	//			for (org.gedcom4j.model.Individual child : family.children) {
	//				for (IndividualEvent event : child.events) {
	//					eventTypes.add(event.type.name());
	//				}
	//			}
	//		}
	//		System.out.println("Familles " + ff);
	//		System.out.println("Familles HN" + fhn);
	//		System.out.println("Familles WN" + fwn);
	//		System.out.println("NS0" + ns0);
	//		System.out.println("NS1" + ns1);
	//		System.out.println("NSX" + nsx);
	//		System.out.println("EventTypes" + eventTypes);
	//
	//	}
}
