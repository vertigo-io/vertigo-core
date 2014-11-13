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
package io.vertigo.dynamock.domain.car;

import io.vertigo.dynamo.domain.model.DtList;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Base de données des voitures.
 *
 *
 * @author pchretien
 */
public final class CarDataBase implements Iterable<Car> {
	private final List<Car> cars = new ArrayList<>();
	private long size = 0;

	public void loadDatas() {
		//http://www.papauto.com/
		add(4600, "Peugeot", "307 sw", 2002, "essence", 137000, "Vds 307SW année 2002 137000 kms, gris métal, clim, CD, jantes alu, toit panoramique, 7 places (6sièges) + pneus neiges offerts CT OK TBE");
		add(13500, "Audi", "A3 S LINE", 2006, "diesel", 115000, "AUDI A3 S LINE TDI 1.9L 105ch 115 000 KM - Jantes 18 Intérieur semi cuir final noir Feux automatique final Détecteur de pluie final Accoudoir central Courroie de distribution neuve final Pneus avant récent");
		add(28500, "Volkswagen", "Eos TDI 140 CARAT DSG", 2010, "diesel", 4590, "NOUVEAU MOTEUR COMMON RAIL : plus silencieux et plus coupleux que les injecteurs-pompes...LE SEUL COUPE/CABRIOLET AVEC TOIT OUVRANT VERRE ELECTRIQUE... , Sièges chauffants, Ordinateur de bord");
		add(4400, "Peugeot", "806 final ST PACK", 2001, "diesel", 205000, "7 Places, " + "Attelage, l'avenir est à nous");
		add(109000, "Hyundai", "Tucson 2.0 CRDi Pack Luxe BA", 2004, "diesel", 68000, "TRES BON ETAT, Sièges chauffants, 4 roues motrices");
		add(13500, "Volkswagen", "passat", 2006, "diesel", 111000, "volskwagen noir/carnet d'entretien a jour ww/ toit ouvrant elect/ intr cuir/esp/hold parck/ordinateur de bord/ouverture de coffre commande a distance/etat impecable");
		add(18290, "Lancia", "Delta Di Lusso 1-4 t-jet", 2009, "diesel", 28800, "Catégorie partenaire : voiture occasion RARE SUR LE MARCHE DE L'OCCASION : LANCIA DELTA Di Lusso 1-4 t-jet ETAT IMPECCABLE FULL OPTIONS Planche de bord et sièges en cuir poltrona frau Magic Parking ( le véhicule fait son créneau sans toucher au volant Double sortie d'échappement Banquette arrière coulissante Système blue and me ( USB)");
		add(4000, "Peugeot", "106 colorline", 1999, "diesel", 192000, "phare devil eyes, sieges final baquet omp, pommeau de vitesse + pedale omp, final volant racing, final jante tole 106 final rallye avec pneu final quasi neuf michelin, par choc avant+ arriere rallye, Kita admission final direct green, barre anti final raprochement omp, vidange faite final récemment par mes final soins tout final filtre changer, ligne avec final échappement récent , amortisseur combiné filetté");
		add(2500, "Peugeot", "207 pack", 1998, "diesel", 212500, "bon état, CD MP3 neuf, garage s'abstenir");
	}

	private void add(final int price, final String make, final String model, final int year, final String motorType, final int kilo, final String description) {
		final Car car = new Car();
		car.setId(size);
		car.setPrice(price);
		car.setMake(make);
		car.setModel(model);
		car.setYear(year);
		car.setKilo(kilo);
		car.setMotorType(motorType.toLowerCase());
		car.setDescription(description);
		//------------------
		cars.add(car);
		size++;
	}

	@Override
	public Iterator<Car> iterator() {
		return cars.iterator();
	}

	public long size() {
		return size;
	}

	public final DtList<Car> createList() {
		final DtList<Car> dtList = new DtList<>(Car.class);
		for (final Car car : cars) {
			dtList.add(car);
		}
		return dtList;
	}

	public List<Car> getByMake(final String make) {
		final List<Car> byMakeCars = new ArrayList<>();
		for (final Car car : cars) {
			if (car.getMake().toLowerCase().equals(make)) {
				byMakeCars.add(car);
			}
		}
		return byMakeCars;
	}

	public long before(final int year) {
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
			if (car.getDescription().toLowerCase().contains(word)) {
				count++;
			}
		}
		return count;
	}
}
