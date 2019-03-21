/**
 * vertigo - simple java starter
 *
 * Copyright (C) 2013-2019, KleeGroup, direction.technique@kleegroup.com (http://www.kleegroup.com)
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
package io.vertigo.dynamo.search_2_4.data.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import io.vertigo.dynamo.domain.model.DtList;

/**
 * Base de données des voitures.
 *
 *
 * @author pchretien
 */
public final class CarDataBase {
	private final List<Car> cars = new ArrayList<>();
	private long size = 0;

	public void loadDatas() {
		//http://www.papauto.com/
		add(4600, "Peugeot", "307 sw", 2002, "essence", 137000, 9, "Vds 307SW année 2002 137000 kms, gris métal, clim, CD, jantes alu, toit panoramique, 7 places (6 sièges) + pneus neiges offerts CT OK TBE", null, "Aaa Zzz");
		add(13500, "Audi", "A3 S LINE", 2006, "diesel", 115000, 5.6, "AUDI A3 S LINE TDI 1.9L 105ch 115 000 KM - Jantes 18 Intérieur semi cuir final noir Feux automatique final Détecteur de pluie final Accoudoir central Courroie de distribution neuve final Pneus avant récent", 0L, "Bbb Yyy");
		add(28500, "Volkswagen", "Eos TDI 140 CARAT DSG", 2010, "diesel", 4590, 6.7, "NOUVEAU MOTEUR COMMON RAIL : plus silencieux et plus coupleux que les injecteurs-pompes...LE SEUL COUPE/CABRIOLET AVEC TOIT OUVRANT VERRE ELECTRIQUE... , Sièges chauffants, Ordinateur de bord", null, null);
		add(4400, "Peugeot", "806 final ST PACK", 2001, "diesel", 205000, 6.7, "7 Places, Sièges cuir, Attelage, l'avenir est à nous", null, null);
		add(109000, "Hyundai", "Tucson 2.0 CRDi Pack Luxe BA", 2004, "diesel", 68000, 7.2, "TRES BON ETAT, Sièges chauffants, 4 roues motrices", 100L, "Ccc Xxx");
		add(13500, "Volkswagen", "passat", 2006, "diesel", 111000, 4, "volskwagen noir/carnet d'entretien a jour ww/ toit ouvrant elect/ intr cuir/esp/hold parck/ordinateur de bord/ouverture de coffre commande a distance/etat impecable", null, null);
		add(18290, "Lancia", "Delta Di Lusso 1-4 t-jet", 2009, "diesel", 28800, 6.8, "Catégorie partenaire : voiture occasion RARE SUR LE MARCHE DE L'OCCASION : LANCIA DELTA Di Lusso 1-4 t-jet ETAT IMPECCABLE FULL OPTIONS Planche de bord et sièges en cuir poltrona frau Magic Parking ( le véhicule fait son créneau sans toucher au volant Double sortie d'échappement Banquette arrière coulissante Système blue and me ( USB)", null, null);
		add(4000, "Peugeot", "106 colorline", 1999, "diesel", 192000, 5.3, "phare devil eyes, sieges final baquet omp, Intérieur cuir, pommeau de vitesse + pedale omp, final volant racing, final jante tole 106 final rallye avec pneu final quasi neuf michelin, par choc avant+ arriere rallye, Kita admission final direct green, barre anti final raprochement omp, vidange faite final récemment par mes final soins tout final filtre changer, ligne avec final échappement récent , amortisseur combiné filetté", null, null);
		add(2500, "Peugeot", "207 pack", 1998, "diesel", 212500, 7, "bon état, CD MP3 neuf, garage s'abstenir", 200L, null);
	}

	private void add(final int price, final String make, final String model, final int year, final String motorType, final int kilo, final double consommation, final String description, final Long optionalNumber, final String optionalString) {
		final Car car = new Car();
		car.setId(size);
		car.setPrice(price);
		car.setMake(make);
		car.setModel(model);
		car.setYear(year);
		car.setKilo(kilo);
		final BigDecimal conso = new BigDecimal(consommation);
		conso.setScale(2, RoundingMode.HALF_UP);
		car.setConsommation(conso);
		car.setMotorType(motorType.toLowerCase(Locale.FRENCH));
		car.setDescription(description);
		car.setOptionalNumber(optionalNumber);
		car.setOptionalString(optionalString);
		//-----
		cars.add(car);
		size++;
	}

	public long size() {
		return size;
	}

	public final DtList<Car> getAllCars() {
		final DtList<Car> dtList = new DtList<>(Car.class);
		dtList.addAll(cars);
		return dtList;
	}

	public List<Car> getCarsByMaker(final String make) {
		final List<Car> byMakeCars = new ArrayList<>();
		for (final Car car : cars) {
			if (car.getMake().toLowerCase(Locale.FRENCH).equals(make)) {
				byMakeCars.add(car);
			}
		}
		return byMakeCars;
	}

	public long getCarsBefore(final int year) {
		long count = 0;
		for (final Car car : cars) {
			if (car.getYear() <= year) {
				count++;
			}
		}
		return count;
	}

	public long containsDescription(final String word) {
		long count = 0;
		for (final Car car : cars) {
			if (car.getDescription().toLowerCase(Locale.FRENCH).contains(word)) {
				count++;
			}
		}
		return count;
	}
}
